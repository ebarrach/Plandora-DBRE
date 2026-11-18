"""J1 documentary reconstruction. Inputs are read-only; no database connection.
Run from repository root: python code/tools/schema/j1.py
Requires already-installed reportlab and PyMuPDF for renderings.
"""
import csv
import hashlib
import json
import pathlib
import re
import subprocess
import textwrap
import xml.etree.ElementTree as ET
from html import escape

ROOT = pathlib.Path(__file__).resolve().parents[3]
OUT = ROOT / 'results/step-1'
SOURCE = 'code/plandora/reference/r348/mysql.sql'
REV = subprocess.check_output(['git', 'rev-parse', 'HEAD'], cwd=ROOT, text=True).strip()
SCOPE = (ROOT / 'method/analysis/scope-tables.txt').read_text().split()
assert len(SCOPE) == len(set(SCOPE)) == 24

def write(path, content):
    p = ROOT / path
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(content, encoding='utf-8')

def js(path, obj):
    write(path, json.dumps(obj, ensure_ascii=False, indent=2) + '\n')

def csvfile(path, rows, fields=None):
    p = ROOT / path
    p.parent.mkdir(parents=True, exist_ok=True)
    with p.open('w', newline='', encoding='utf-8-sig') as h:
        w = csv.DictWriter(h, fieldnames=fields or list(rows[0]))
        w.writeheader()
        w.writerows({k: json.dumps(v, ensure_ascii=False) if isinstance(v, (list, dict)) else v for k, v in r.items()} for r in rows)

def split_sql(s):
    return re.split(r",(?![^()]*\))(?=(?:[^']*'[^']*')*[^']*$)", s)

def parse(path):
    s = (ROOT / path).read_text(encoding='latin1')
    line = lambda pos: s.count('\n', 0, pos) + 1
    tables = {}
    for m in re.finditer(r'create\s+table\s+(\w+)\s*\((.*?)\)\s*;', s, re.I | re.S):
        t = dict(name=m[1], columns=[], primary_key=None, unique_keys=[], indexes=[], source=path,
                 start_line=line(m.start()), end_line=line(m.end()), ddl=m[0])
        offset = m.start(2)
        for part in split_sql(m[2]):
            pos = offset + len(part) - len(part.lstrip())
            offset += len(part) + 1
            raw = part.strip()
            key = re.fullmatch(r'(primary\s+key|unique(?:\s+key)?)\s*(\w+)?\s*\((.*?)\)', raw, re.I | re.S)
            if key:
                k = dict(name=key[2], columns=[x.strip() for x in key[3].split(',')], line=line(pos))
                if key[1].lower().startswith('primary'): t['primary_key'] = k
                else: t['unique_keys'].append(k)
                continue
            col = re.fullmatch(r'(\w+)\s+([a-z]+(?:\s*\([^)]*\))?)(.*)', raw, re.I | re.S)
            if not col: raise ValueError((path, raw))
            tail = col[3]
            default = re.search(r"\bdefault\s+('(?:[^']|'')*'|\S+)", tail, re.I)
            t['columns'].append(dict(name=col[1], ordinal=len(t['columns'])+1, type=col[2],
                null_declaration='NOT NULL' if re.search(r'not\s+null', tail, re.I) else 'NULL' if re.search(r'\bnull\b', tail, re.I) else None,
                default_explicit=default[1] if default else None, declaration=raw, line=line(pos)))
        tables[m[1]] = t
    fks = []
    for m in re.finditer(r'alter\s+table\s+(\w+)\s+add\s+constraint\s+(\w+)\s+foreign\s+key\s*\((.*?)\)\s+references\s+(\w+)\s*\((.*?)\)([^;]*);', s, re.I):
        fks.append(dict(table=m[1], constraint=m[2], columns=[x.strip() for x in m[3].split(',')],
            target=m[4], target_columns=[x.strip() for x in m[5].split(',')], actions_explicit=m[6].strip() or None,
            source=path, start_line=line(m.start()), end_line=line(m.end()), revision='SVN r348', repository_revision=REV))
    for m in re.finditer(r'create\s+(unique\s+)?index\s+(\w+)\s+on\s+(\w+)\s*\((.*?)\)\s*;', s, re.I):
        tables[m[3]]['indexes'].append(dict(name=m[2], unique=bool(m[1]), columns=[x.strip() for x in m[4].split(',')], line=line(m.start())))
    # Fail loudly if a future source introduces unhandled key/index syntax.
    for stmt in re.findall(r'[^;]+;', s):
        if re.match(r'\s*(?:alter|create)\b', stmt, re.I) and re.search(r'\b(?:index|unique)\b', stmt, re.I) and not re.search(r'create\s+(?:unique\s+)?index', stmt, re.I):
            if not re.search(r'create\s+table', stmt, re.I): raise ValueError('Unhandled index declaration: '+stmt)
    return s, tables, fks

def evidence(path, start, end=None):
    return dict(revision='SVN r348', repository_revision=REV, path=path, start_line=start, end_line=end or start)

def java_lines(name):
    p = 'code/plandora/reference/r348/java/com/pandora/' + name
    return p, (ROOT / p).read_text(encoding='latin1').splitlines()

def source_hits(pattern):
    found = []
    for p in sorted((ROOT/'code/plandora/reference/r348/java').rglob('*.java')):
        for i, line in enumerate(p.read_text(encoding='latin1').splitlines(), 1):
            if re.search(pattern, line, re.I):
                found.append(dict(path=p.relative_to(ROOT).as_posix(), line=i, text=line.strip()))
    return found

def main():
    s, all_tables, all_fks = parse(SOURCE)
    tables = [all_tables[t] for t in SCOPE]
    selected = [f for f in all_fks if f['table'] in SCOPE or f['target'] in SCOPE]
    checks = []
    for f in selected:
        f['scope'] = 'internal' if f['table'] in SCOPE and f['target'] in SCOPE else 'outgoing' if f['table'] in SCOPE else 'incoming'
        a, b = all_tables[f['table']], all_tables[f['target']]
        ac, bc = {c['name']: c for c in a['columns']}, {c['name']: c for c in b['columns']}
        exists = all(x in ac for x in f['columns']) and all(x in bc for x in f['target_columns'])
        norm = lambda t: re.sub(r'\s+', '', t).lower()
        target_keys = [b['primary_key']] + b['unique_keys']
        candidate = any(k and k['columns'] == f['target_columns'] for k in target_keys)
        # A UNIQUE superset does not make the FK unique; a contained candidate key does.
        source_unique = any(k and set(k['columns']) <= set(f['columns']) for k in [a['primary_key']] + a['unique_keys'])
        nulls = [ac[x]['null_declaration'] for x in f['columns']] if exists else []
        pkcols = a['primary_key']['columns'] if a['primary_key'] else []
        required = all(ac[x]['null_declaration']=='NOT NULL' or x in pkcols for x in f['columns'])
        f['source_to_target'] = ('1..1' if required else '0..1') if candidate else 'unknown (target not candidate key)'
        f['target_to_source'] = '0..1' if source_unique else '0..N'
        f['cardinality_basis'] = 'Ordered target candidate key; NOT NULL/PK semantics; source candidate key containment. Collection minimum unproved, retained 0.'
        row = dict(constraint=f['constraint'], scope=f['scope'], columns_exist=exists,
                   arity_equal=len(f['columns'])==len(f['target_columns']),
                   ordered_type_compatibility=exists and all(norm(ac[x]['type'])==norm(bc[y]['type']) for x,y in zip(f['columns'],f['target_columns'])),
                   ordered_target_candidate_key=candidate, null_declarations=nulls,
                   source_to_target=f['source_to_target'], target_to_source=f['target_to_source'],
            source=SOURCE, start_line=f['start_line'], end_line=f['end_line'])
        checks.append(row)
    internal = [f for f in selected if f['scope']=='internal']
    external = [f for f in selected if f['scope']!='internal']
    inferred = []
    for table, cols, target, tc, file, start, end, strength, basis in [
        ('plan_relation',['plan_related_id'],'planning',['id'],'dao/PlanningRelationDAO.java',40,44,'observed-query','Equality join pr.plan_related_id = p.id'),
        ('task',['created_by'],'tool_user',['id'],'dao/TaskDAO.java',1001,1011,'inferred-object-mapping','created_by mapped to UserTO; UserDAO establishes persistence in tool_user'),
    ]:
        p, lines = java_lines(file)
        inferred.append(dict(id='I'+str(len(inferred)+1),table=table,columns=cols,target=target,target_columns=tc,
            classification=strength,basis=basis,evidence=[evidence(p,start,end)],
            enforcement='not declared; application relationship, orphan values not ruled out',
            source_to_target='0..1 (candidate, existence unproved)',target_to_source='0..N (candidate)'))
    inferred[1]['evidence'].append(evidence('code/plandora/reference/r348/java/com/pandora/dao/UserDAO.java',92,98))
    # Retain complete source matches for required investigations; avoid promotion by name alone.
    investigations = {k:source_hits(v) for k,v in {
        'customer_function':'function_id|customer_function',
        'status_project':'accept_project_id|acceptProjectId|acceptProject',
        'status_parent':'parent_id|parentId|setParent|getParent',
        'user_persistence':'from tool_user|into tool_user',
    }.items()}
    js('results/step-1/foreign-keys/source-investigations.json',investigations)
    inv = dict(reference='SVN r348',repository_revision=REV,source=SOURCE,source_sha256=hashlib.sha256((ROOT/SOURCE).read_bytes()).hexdigest(),
        scope=SCOPE,tables=tables,foreign_keys=internal,external_dependencies=external,
        semantics={'null_declaration':None,'null_note':'null means no explicit NULL/NOT NULL clause, not non-nullability; PK is implicitly non-null under MySQL semantics',
            'default_note':'null means no explicit DEFAULT clause; no engine/version default is asserted',
            'indexes_note':'only CREATE INDEX statements and explicit key declarations; no implicit FK index is counted'},
        origin='complementary documentary extraction; not SQLInspect; not database introspection')
    js('results/step-1/physical-schema/inventory.json',inv)
    csvfile('results/step-1/physical-schema/tables.csv',[dict(table=t['name'],source=SOURCE,revision='SVN r348',repository_revision=REV,**c) for t in tables for c in t['columns']])
    csvfile('results/step-1/foreign-keys/foreign_keys.csv',internal)
    csvfile('results/step-1/foreign-keys/external_dependencies.csv',external)
    csvfile('results/step-1/foreign-keys/inferred_links.csv',inferred)
    csvfile('results/step-1/foreign-keys/validation.csv',checks)
    metrics=dict(tables=len(tables),columns=sum(len(t['columns']) for t in tables),internal_fks=len(internal),boundary_fks=len(external),
                 declared_indexes=sum(len(t['indexes']) for t in tables),structural_failures=[c for c in checks if not all(c[k] for k in ['columns_exist','arity_equal','ordered_type_compatibility','ordered_target_candidate_key'])])
    js('results/step-1/metrics/ddl-checks.json',metrics)
    # A safe import input: selected CREATEs and internal ALTERs only. No DROP, INSERT, USE, or external objects.
    ddl = '-- Documentary subset of SVN r348 mysql.sql; import into a modelling tool only.\n-- Original licenses remain in code/plandora/reference/r348.\n'
    ddl += '\n\n'.join('-- '+SOURCE+':'+str(t['start_line'])+'-'+str(t['end_line'])+'\n'+t['ddl'] for t in tables)
    ddl += '\n\n'+'\n'.join(s.splitlines()[f['start_line']-1] for f in internal)+'\n'
    write('results/step-1/physical-schema/scope-mysql.sql',ddl)
    # Compare dialects without merging their declarations.
    try:
        _, ht, hf = parse('code/plandora/reference/r348/HSQL.sql')
        diffs=[]
        for t in tables:
            other=ht.get(t['name'])
            if not other: diffs.append(dict(table=t['name'],difference='missing HSQL table'));continue
            for c in t['columns']:
                oc=next((x for x in other['columns'] if x['name']==c['name']),None)
                if not oc or any(c[k]!=oc[k] for k in ['type','null_declaration','default_explicit']):
                    diffs.append(dict(table=t['name'],mysql=c,hsql=oc))
        js('results/step-1/physical-schema/hsql-comparison.json',dict(differences=diffs,hsql_fk_count=len(hf),note='Separate dialect, never merged with mysql inventory'))
    except ValueError as e:
        js('results/step-1/physical-schema/hsql-comparison.json',dict(status='parser limitation',detail=str(e)))
    render(tables,internal,inferred)
    print(json.dumps(metrics,ensure_ascii=False,indent=2))

CONCEPTS = dict(planning='Planning item',plan_relation='Planning dependency',project='Project',project_status='Project status',project_history='Project transition',
    requeriment='Request',requeriment_status='Request status',requeriment_history='Request transition',task='Task',task_status='Assignment status',task_history='Assignment transition',
    resource_task='Assignment',resource_task_alloc='Actual allocation',resource_task_alloc_plann='Planned allocation',resource_capacity='Capacity slot',tool_user='User',
    customer='Project participation',resource='Resource participation',leader='Leader participation',company='Company',department='Department',area='Area',function='Function',category='Project category')

def association(f):
    c=f['constraint']
    named={
        'user_department':'belongs to department','user_area':'belongs to area','user_function':'holds function','user_company':'affiliated with company',
        'project_parent_project':'has parent project','project_status_project':'has current project status','proj_company':'belongs to company',
        'req_cat':'classified by category','req_project':'requested in project','req_status_req':'has current request status','req_user':'requested by user',
        'task_proj':'scheduled in project','task_req':'implements request','task_cat':'classified by category','task_task':'has parent task',
        'task_res_task':'assigns task','res_res_task':'assigns resource participation','res_task_status_task':'has assignment status','approver_res_task_fk':'approved by user',
        'task_hist_res_task':'records assignment transition','task_hist_status_task':'records resulting assignment status','task_hist_user':'handled by user',
        'req_hist_req':'records request transition','req_hist_status':'records resulting request status','req_hist_user':'handled by user',
        'prj_hist_prj':'records project transition','prj_hist_status':'records resulting project status','plan_plan_relation':'has originating planning item',
        'cat_project':'defines category in project',
    }
    if c in named:return named[c]
    if c in ['project_parent','req_parent','task_parent']:return 'specializes planning (candidate)'
    if c in ['resource_customer','leader_resource']:return 'specializes participation (candidate)'
    if f['table'].endswith('_history'): return 'records '+f['target']
    if f['table'].startswith('resource_task_alloc'):return 'allocates assignment'
    if f['table']=='resource_capacity':return 'provides capacity for'
    if f['table']=='customer':return 'participates as user' if f['target']=='tool_user' else 'participates in project'
    if f['table']=='resource_task':return 'assigns / '+f['target']
    if f['target'].endswith('_status'):return 'has current status'
    if f['table']==f['target']:return 'has parent'
    return 'relates via '+','.join(f['columns'])

def render(tables,fks,inferred):
    from reportlab.pdfgen import canvas
    from reportlab.lib.colors import HexColor
    import fitz
    mx=ET.Element('mxfile',host='app.diagrams.net',version='24.0.0')
    mapping=[]
    for t in tables:
        mapping.append(dict(table=t['name'],conceptual_element=CONCEPTS[t['name']],kind='association entity' if t['name'] in ['customer','resource_task','plan_relation'] else 'entity / subtype / event',
            evidence=evidence(SOURCE,t['start_line'],t['end_line']),interpretation='candidate business abstraction; key constraints retained; no totality/disjointness asserted'))
    csvfile('results/step-1/conceptual-schema/table-mapping.csv',mapping)
    relations=[dict(id='F'+str(i+1),name=association(f),**f) for i,f in enumerate(fks)]
    js('results/step-1/conceptual-schema/associations.json',relations)
    for level,title in [('physical','Physical schema - declared MySQL DDL'),('logical','Logical schema - reconstructed relations'),('conceptual','Conceptual schema - candidate business model')]:
        folder='results/step-1/'+level+'-schema'
        diagram=ET.SubElement(mx,'diagram',name=level.title(),id=level)
        model=ET.SubElement(diagram,'mxGraphModel',dx='1600',dy='1200',grid='1',gridSize='10',page='1',pageWidth='1800',pageHeight='4200')
        root=ET.SubElement(model,'root');ET.SubElement(root,'mxCell',id='0');ET.SubElement(root,'mxCell',id='1',parent='0')
        # Numbered reference register avoids ambiguous crossings in the static exports.
        cards=[]
        for t in tables:
            pk=t['primary_key']['columns'] if t['primary_key'] else []
            if level=='conceptual':
                lines=[CONCEPTS[t['name']], 'maps: '+t['name'], 'Identifier: '+', '.join(pk)]
                if t['name'] in ['project','requeriment','task','resource','leader']:lines+=['Candidate subtype; inclusion only','Totality/disjointness: unknown']
                else:lines+=['Properties: '+', '.join(c['name'] for c in t['columns'] if c['name'] not in pk)]
                lines+=['Associations: '+', '.join(r['id'] for r in relations if r['table']==t['name'])]
            else:
                lines=[t['name'], 'PK ('+', '.join(pk)+')']
                for c in t['columns']:
                    refs=[r['id'] for r in relations if r['table']==t['name'] and c['name'] in r['columns']]
                    v=c['name']
                    if level=='physical':v+=' : '+c['type']+' '+(c['null_declaration'] or '[NULL unspecified]')
                    else:v+=' : '+('required' if c['null_declaration']=='NOT NULL' or c['name'] in pk else 'optional')
                    if c['default_explicit'] is not None:v+=' DEFAULT '+c['default_explicit']
                    if refs:v+=' ['+','.join(refs)+']'
                    lines+=textwrap.wrap(v,64,subsequent_indent='  ')
                for ix in t['indexes']:lines+=['INDEX '+ix['name']+' ('+', '.join(ix['columns'])+')']
            lines+=['r348 mysql.sql:'+str(t['start_line'])+'-'+str(t['end_line'])]
            cards.append((t['name'],lines))
        # SVG / editable draw.io use four columns and variable row heights.
        positions={};y=115
        for row in range(6):
            batch=cards[row*4:row*4+4];heights=[]
            for col,(name,lines) in enumerate(batch):
                wrapped=[v for x in lines for v in (textwrap.wrap(x,64) or [''])]
                h=34+len(wrapped)*15;heights.append(h);positions[name]=(30+col*460,y,440,h,wrapped)
            y+=max(heights)+25
        register=[]
        for r in relations:
            label=(r['name']+' | ') if level=='conceptual' else ''
            register += [(False, r['id']+' '+label+r['table']+'('+','.join(r['columns'])+') -> '+r['target']+'('+','.join(r['target_columns'])+')'),
                         (False, '   '+r['source_to_target']+' ; reverse '+r['target_to_source']+' | '+r['constraint']+' | mysql.sql:'+str(r['start_line']))]
        if level!='physical':
            for f in inferred:register += [(True,f['id']+' RECOVERED '+f['table']+'.'+','.join(f['columns'])+' -> '+f['target']+'.'+','.join(f['target_columns'])),(True,'   Candidate 0..1 / 0..N; unenforced; '+f['evidence'][0]['path'].split('/')[-1]+':'+str(f['evidence'][0]['start_line'])+'-'+str(f['evidence'][0]['end_line']))]
        height=y+80+len(register)*19
        svg=[f'<svg xmlns="http://www.w3.org/2000/svg" width="1880" height="{height}" viewBox="0 0 1880 {height}">','<rect width="100%" height="100%" fill="#f6f8fc"/>',f'<text x="30" y="40" font-family="sans-serif" font-size="25" fill="#15324f">{escape(title)}</text>',f'<text x="30" y="70" font-family="sans-serif" font-size="14">SVN r348 | 24 tables | numbered references below | repository {REV[:12]}</text>','<text x="30" y="92" font-family="sans-serif" font-size="14">NULL unspecified is distinct from explicit NULL. Blue = declared; orange dashed = recovered. No live database.</text>']
        for name,(x,cy,w,h,lines) in positions.items():
            svg.append(f'<rect x="{x}" y="{cy}" width="{w}" height="{h}" rx="7" fill="white" stroke="#56738f"/>')
            for i,txt in enumerate(lines):svg.append(f'<text x="{x+12}" y="{cy+24+i*15}" font-family="monospace" font-size="11" fill="#15324f">{escape(txt)}</text>')
            cell=ET.SubElement(root,'mxCell',id=name,value='\n'.join(lines),style='rounded=1;whiteSpace=wrap;html=0;align=left;verticalAlign=top;spacing=10;fontFamily=Courier New;fontSize=11;fillColor=#ffffff;strokeColor=#56738f;',vertex='1',parent='1')
            ET.SubElement(cell,'mxGeometry',x=str(x),y=str(cy),width=str(w),height=str(h),attrib={'as':'geometry'})
        y+=30
        svg.append(f'<text x="30" y="{y}" font-family="sans-serif" font-size="20">Relationship register - child to parent; reverse multiplicity after semicolon</text>')
        for i,(rec,txt) in enumerate(register):
            yy=y+30+i*19;color='#a65c00' if rec else '#15324f'
            svg.append(f'<text x="30" y="{yy}" font-family="monospace" font-size="13" fill="{color}">{escape(txt)}</text>')
            cell=ET.SubElement(root,'mxCell',id='register'+str(i),value=txt,style='text;html=0;align=left;fontFamily=Courier New;fontSize=13;fontColor='+color+';',vertex='1',parent='1')
            ET.SubElement(cell,'mxGeometry',x='30',y=str(yy-15),width='1800',height='19',attrib={'as':'geometry'})
        # Edges are editable but hidden by default to keep the dense overview readable.
        for r in relations:
            edge=ET.SubElement(root,'mxCell',id=r['id'],value=r['id']+' '+r['source_to_target']+' / '+r['target_to_source'],style='edgeStyle=orthogonalEdgeStyle;endArrow=open;strokeColor=#56738f;',edge='1',parent='1',source=r['table'],target=r['target'],visible='0')
            ET.SubElement(edge,'mxGeometry',relative='1',attrib={'as':'geometry'})
        if level!='physical':
            for f in inferred:
                e=ET.SubElement(root,'mxCell',id=f['id'],value=f['id']+' recovered, unenforced',style='edgeStyle=orthogonalEdgeStyle;dashed=1;strokeColor=#a65c00;endArrow=open;',edge='1',parent='1',source=f['table'],target=f['target'],visible='0')
                ET.SubElement(e,'mxGeometry',relative='1',attrib={'as':'geometry'})
        svg.append('</svg>');write(folder+'/'+level+'.svg','\n'.join(svg))
        pdf=ROOT/folder/(level+'.pdf');c=canvas.Canvas(str(pdf),pagesize=(1200,850));c.setTitle(title)
        page=0
        def header(subtitle):
            nonlocal page
            page+=1;c.setFillColor(HexColor('#15324f'));c.setFont('Helvetica-Bold',19);c.drawString(32,815,title)
            c.setFont('Helvetica',10);c.drawString(32,794,subtitle+' | SVN r348 | '+REV[:12]);c.drawRightString(1165,22,str(page))
        for start in range(0,len(cards),6):
            header('24 tables; details '+str(start+1)+'-'+str(min(start+6,24))+'; references F# in following register')
            for ix,(name,_) in enumerate(cards[start:start+6]):
                lines=positions[name][4];x=32+(ix%3)*390;top=770-(ix//3)*365
                c.setStrokeColor(HexColor('#56738f'));c.roundRect(x,top-345,375,345,6)
                c.setFont('Courier',8.2)
                assert len(lines)*12<330,(name,len(lines))
                for j,txt in enumerate(lines):c.drawString(x+9,top-19-j*12,txt)
            c.showPage()
        for start in range(0,len(register),40):
            header('Relationship register; child -> parent / reverse; blue declared, orange recovered')
            for j,(rec,txt) in enumerate(register[start:start+40]):
                c.setFillColor(HexColor('#a65c00' if rec else '#15324f'));c.setFont('Courier',8.5);c.drawString(32,765-j*18,txt)
            c.showPage()
        c.save()
        doc=fitz.open(pdf)
        for page_no,p in enumerate(doc):
            p.get_pixmap(matrix=fitz.Matrix(1,1)).save(str(ROOT/folder/(level+'-page-'+str(page_no+1)+'.png')))
    write('results/step-1/physical-schema/j1-schemas.drawio',ET.tostring(mx,encoding='unicode'))

if __name__=='__main__': main()
