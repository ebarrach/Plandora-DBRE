"""Independent documentary and artifact checks; no DB connections or source writes."""
import csv
import hashlib
import json
import pathlib
import re
import subprocess
import xml.etree.ElementTree as ET
import fitz

R=pathlib.Path(__file__).resolve().parents[3]
O=R/'results/step-1'
inv=json.loads((O/'physical-schema/inventory.json').read_text(encoding='utf8'))
ddl=(R/inv['source']).read_text(encoding='latin1')
scope=(R/'method/analysis/scope-tables.txt').read_text().split()
results={}
def check(name,condition):
    results[name]=bool(condition)
    assert condition,name

check('24_unique_tables',len(inv['tables'])==len(set(scope))==24)
check('scope_order_preserved',[t['name'] for t in inv['tables']]==scope)
check('source_digest',hashlib.sha256((R/inv['source']).read_bytes()).hexdigest()==inv['source_sha256'])
check('175_columns',sum(len(t['columns']) for t in inv['tables'])==175)
check('39_internal_fk',len(inv['foreign_keys'])==39)
check('30_boundary_fk',len(inv['external_dependencies'])==30)
for t in inv['tables']:
    source='\n'.join(ddl.splitlines()[t['start_line']-1:t['end_line']])
    check(t['name']+'_create_anchor',bool(re.match(r'create\s+table\s+'+t['name']+r'\s*\(',source,re.I)))
    check(t['name']+'_column_unique',len({c['name'] for c in t['columns']})==len(t['columns']))
    for c in t['columns']:
        check(t['name']+'.'+c['name']+'_source',c['declaration'] in ddl.splitlines()[c['line']-1])
    check(t['name']+'_ordered_pk',bool(re.search(r'primary\s+key\s+'+re.escape(t['primary_key']['name'])+r'\s*\(\s*'+r'\s*,\s*'.join(map(re.escape,t['primary_key']['columns']))+r'\s*\)',source,re.I)))
for f in inv['foreign_keys']+inv['external_dependencies']:
    source=ddl.splitlines()[f['start_line']-1]
    pattern=r'foreign\s+key\s*\(\s*'+r'\s*,\s*'.join(map(re.escape,f['columns']))+r'\s*\)\s+references\s+'+f['target']+r'\s*\(\s*'+r'\s*,\s*'.join(map(re.escape,f['target_columns']))+r'\s*\)'
    check(f['constraint']+'_tuple_source',bool(re.search(pattern,source,re.I)))
validation=list(csv.DictReader((O/'foreign-keys/validation.csv').open(encoding='utf-8-sig')))
check('all_internal_structural_checks',all(all(r[k]=='True' for k in ['columns_exist','arity_equal','ordered_type_compatibility','ordered_target_candidate_key']) for r in validation if r['scope']=='internal'))
failures=[r['constraint'] for r in validation if r['ordered_target_candidate_key']=='False']
check('boundary_anomaly_preserved',failures==['cus_func_customer'])
review=json.loads((O/'foreign-keys/dao-review.json').read_text(encoding='utf8'))
check('39_dao_review_records',{r['constraint'] for r in review}=={f['constraint'] for f in inv['foreign_keys']})
for row in review:
    e=row['query_or_mapping'];lines=(R/e['path']).read_text(encoding='latin1').splitlines()
    check(row['constraint']+'_dao_excerpt',row['excerpt']=='\n'.join(lines[e['start_line']-1:e['end_line']]))
check('two_recovered_links',len(list(csv.DictReader((O/'foreign-keys/inferred_links.csv').open(encoding='utf-8-sig'))))==2)
check('three_unverified_hypotheses',len(list(csv.DictReader((O/'logical-schema/hypotheses.csv').open(encoding='utf-8-sig'))))==3)
mx=ET.parse(O/'physical-schema/j1-schemas.drawio').getroot()
check('drawio_three_pages',len(mx.findall('diagram'))==3)
for d in mx.findall('diagram'):
    ids={x.get('id') for x in d.findall('.//mxCell')}
    check(d.get('name')+'_24_nodes',set(scope)<=ids)
    check(d.get('name')+'_39_fk_edges',sum(x.get('edge')=='1' and x.get('id','').startswith('F') for x in d.findall('.//mxCell'))==39)
pdfs={}
for level in ['physical','logical','conceptual']:
    folder=O/(level+'-schema')
    svg=ET.parse(folder/(level+'.svg')).getroot()
    check(level+'_svg_valid',svg.tag.endswith('svg'))
    doc=fitz.open(folder/(level+'.pdf'));pdfs[level]=len(doc)
    text='\n'.join(p.get_text() for p in doc)
    check(level+'_all_tables_in_pdf',all(t in text for t in scope))
    check(level+'_rendered_pages',all((folder/(level+'-page-'+str(i+1)+'.png')).exists() for i in range(len(doc))))
    for i,p in enumerate(doc):
        for b in p.get_text('dict')['blocks']:
            if 'lines' not in b:continue
            for line in b['lines']:
                for span in line['spans']:
                    x0,y0,x1,y1=span['bbox']
                    check(level+f'_p{i}_text_bounds_{len(results)}',x0>=0 and y0>=0 and x1<=p.rect.width and y1<=p.rect.height)
subset=(O/'physical-schema/scope-mysql.sql').read_text(encoding='utf8')
check('safe_subset_no_dml_drop',not re.search(r'^\s*(DROP|INSERT|DELETE|UPDATE|USE)\b',subset,re.I|re.M))
check('subset_24_creates',len(re.findall(r'^create table ',subset,re.I|re.M))==24)
check('subset_39_alters',len(re.findall(r'^alter table ',subset,re.I|re.M))==39)
changed=subprocess.check_output(['git','status','--porcelain','--','code/plandora/reference/'],cwd=R,text=True)
check('reference_worktree_unchanged',not changed.strip())
result=dict(status='passed',checks=len(results),failed=[k for k,v in results.items() if not v],pdf_pages=pdfs,
    limitations=['Documentary/static checks, no live DB, no runtime DAO tests','Expected anomaly cus_func_customer is preserved, not corrected','DB-MAIN import and Lucidchart import not executed'],results=results)
(O/'metrics/artifact-validation.json').write_text(json.dumps(result,indent=2),encoding='utf8')
print(json.dumps({k:v for k,v in result.items() if k!='results'},indent=2))
