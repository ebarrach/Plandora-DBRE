#!/usr/bin/env python3
"""Plandora r348 DBRE step 2: single-command, reproducible evidence pipeline.

Python 3.10+, standard library only. Reads SQLInspect exports and Java sources,
produces J2.2 AI-assisted candidate review and SQL evidence only.
LSS construction and J2.3 are outside the scope of this deliverable.

IMPORTANT: Static code references != runtime proof. Lexical matches != type-aware
Java analysis. No automatically inferred JDBC hotspot is described as confirmed.
"""
from __future__ import annotations

import argparse
from collections import Counter, defaultdict
import csv
import hashlib
import json
import re
import sys
from pathlib import Path
import xml.etree.ElementTree as ET
import zipfile

EXEC_METHODS = {"execute", "executeQuery", "executeUpdate", "executeLargeUpdate", "executeBatch", "executeLargeBatch", "addBatch"}
PREP_METHODS = {"prepareStatement", "prepareCall", "createStatement"}
ALL_METHODS = EXEC_METHODS | PREP_METHODS
CALL_RE = re.compile(r'\b(?P<receiver>[A-Za-z_$][\w$]*)\s*\.\s*(?P<method>' + '|'.join(sorted(ALL_METHODS, key=len, reverse=True)) + r')\s*\(')
SQL_START = re.compile(r'^\s*(SELECT|INSERT|UPDATE|DELETE|CREATE|ALTER|DROP|REPLACE|TRUNCATE|WITH)\b', re.I)
TABLE_RX = re.compile(r'\b(?:FROM|JOIN|UPDATE|INTO|DELETE\s+FROM)\s+[`\"]?([a-zA-Z_]\w*)', re.I)
LITERAL_RE = re.compile(r'"(?:\\.|[^"\\])*"', re.S)
REPOSITORY_TABLES = {"db_repository_item", "db_repository_version", "db_repository_sequence"}

# AI-assisted, source-excerpt-reviewed case studies for Plandora SVN r348.
# This is an auditable analysis of supplied r348 source evidence, NOT an LLM
# invocation performed by this Python script. Changed source lines are left
# unreviewed instead of silently receiving a prior conclusion.
# Status concerns the *primitive JDBC hotspot at this call*, not whether
# the application-level call may eventually access a database.
REVIEWED_JAVA_SHA256 = 'ee77307bf7f25193e624bd97ea9bfc6f6ef5635678f540b7862584519b112432'
AI_REVIEW = {
    ('java/com/pandora/bus/alert/FileToTableNotification.java', 97): ('APPLICATION_SQL_CALL', 'DbQueryDelegate receiver; DELETE statement uses a configuration-selected table. Runtime target is not statically enumerable.', 'INDIRECT_SQL_WITH_DYNAMIC_TARGET'),
    ('java/com/pandora/bus/alert/FileToTableNotification.java', 148): ('APPLICATION_SQL_CALL', 'DbQueryDelegate executes constructed INSERT statements in a batch; table and fields depend on configuration.', 'INDIRECT_SQL_WITH_DYNAMIC_TARGET'),
    ('java/com/pandora/bus/alert/NotificationBUS.java', 78): ('APPLICATION_SQL_CALL', 'DbQueryBUS receives an SQL statement obtained from notification configuration; application-level method.', 'INDIRECT_SQL_WITH_DYNAMIC_TEXT'),
    ('java/com/pandora/bus/DbQueryBUS.java', 56): ('APPLICATION_SQL_CALL', 'Receiver dao is constructed as DbQueryDAO; forwards SQL to its executeQuery method.', 'INDIRECT_SQL_CALL'),
    ('java/com/pandora/bus/DbQueryBUS.java', 67): ('APPLICATION_SQL_CALL', 'DbQueryDAO batch overload; forwards SQL and parameters.', 'INDIRECT_SQL_CALL'),
    ('java/com/pandora/bus/repository/WrapperDBRepository.java', 243): ('APPLICATION_SQL_CALL', 'DbQueryDelegate executes DELETE statements for db_repository_version and db_repository_item.', 'INDIRECT_SQL_REPOSITORY'),
    ('java/com/pandora/bus/repository/WrapperDBRepository.java', 325): ('APPLICATION_SQL_CALL', 'DbQueryDelegate executes constructed repository INSERT statements; db_repository_version SQL is explicit.', 'INDIRECT_SQL_REPOSITORY'),
    ('java/com/pandora/bus/repository/WrapperDBRepository.java', 454): ('APPLICATION_SQL_CALL', 'DbQueryDelegate executes constructed repository INSERT statements for item and version.', 'INDIRECT_SQL_REPOSITORY'),
    ('java/com/pandora/bus/repository/WrapperDBRepository.java', 498): ('APPLICATION_SQL_CALL', 'DbQueryDelegate executes UPDATE of db_repository_sequence(id, project_id); not a JDBC primitive at this line.', 'INDIRECT_SQL_REPOSITORY'),
    ('java/com/pandora/bus/repository/WrapperDBRepository.java', 500): ('APPLICATION_SQL_CALL', 'DbQueryDelegate executes INSERT into db_repository_sequence(id, project_id); not a JDBC primitive at this line.', 'INDIRECT_SQL_REPOSITORY'),
    ('java/com/pandora/dao/DbQueryDAO.java', 94): ('INTERNAL_DAO_FORWARDING', 'this.executeQuery(sql, null, c) invokes a same-class overload; true JDBC calls occur downstream.', 'DAO_OVERLOAD_FORWARDING'),
    ('java/com/pandora/dao/DbQueryDAO.java', 110): ('INTERNAL_DAO_FORWARDING', 'this.executeQuery(theSql, params.get(i), c) invokes a same-class overload; true JDBC calls occur downstream.', 'DAO_OVERLOAD_FORWARDING'),
    ('java/com/pandora/delegate/DbQueryDelegate.java', 53): ('APPLICATION_SQL_CALL', 'DbQueryDelegate forwards to its DbQueryBUS instance.', 'INDIRECT_SQL_CALL'),
    ('java/com/pandora/delegate/DbQueryDelegate.java', 57): ('APPLICATION_SQL_CALL', 'DbQueryDelegate forwards SQL and parameters to DbQueryBUS.', 'INDIRECT_SQL_CALL'),
    ('java/com/pandora/gui/struts/action/DbAction.java', 75): ('APPLICATION_SQL_CALL', 'Web action sends split SQL statements through DbQueryDelegate; this line is not JDBC Statement.executeQuery.', 'INDIRECT_SQL_WITH_DYNAMIC_TEXT'),
    ('java/com/tests/integration/ConnectorTest.java', 35): ('NON_JDBC_EXECUTE', 'Integration Connector.execute accepts a payload and URL target; it is unrelated to JDBC.', 'OUT_OF_SCOPE_NOT_JDBC'),
}

# Source-driven reference search is restricted to these 3 repository tables.
# The names must also occur in the imported LS and in Java SQL string fragments.


def write_csv(path: Path, columns: list[str], records: list[dict]):
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open('w', encoding='utf-8-sig', newline='') as fh:
        w = csv.DictWriter(fh, fieldnames=columns, extrasaction='ignore')
        w.writeheader()
        w.writerows(records)


def dump_json(path: Path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2, ensure_ascii=False) + '\n', encoding='utf-8')


def normpath(value: str) -> str:
    s = (value or '').replace('\\', '/')
    m = re.search(r'(?:^|/)java/', s, re.I)
    return s[m.start():].lstrip('/') if m else s.lstrip('/')


def scan_java(source: Path) -> dict[str, str]:
    files = {}
    if source.is_file():
        if not zipfile.is_zipfile(source):
            raise ValueError(f'Not a ZIP file: {source}')
        with zipfile.ZipFile(source) as z:
            for name in sorted(z.namelist()):
                if not name.endswith('.java') or '/java/' not in '/' + name or name.startswith('__MACOSX/'):
                    continue
                key = normpath(name)
                if key in files:
                    raise ValueError(f'Duplicate Java file path: {key}')
                files[key] = z.read(name).decode('utf-8-sig', errors='replace')
    else:
        root = source / 'java' if (source / 'java').is_dir() else source
        if not root.exists():
            raise ValueError(f'Source path not found: {source}')
        for path in sorted(root.rglob('*.java')):
            key = normpath(path.relative_to(root).as_posix())
            if not key.startswith('java/'):
                key = 'java/' + key
            if key in files:
                raise ValueError(f'Duplicate Java file path: {key}')
            files[key] = path.read_text('utf-8-sig', errors='replace')
    if not files:
        raise ValueError('0 Java files found: check --source. Nothing was generated.')
    return files


def mask_comments_keep_strings(text: str) -> str:
    """Masks Java comments; preserves all offsets, lines, and string literals."""
    out = list(text)
    i, mode = 0, 'code'
    while i < len(text):
        pair = text[i:i+2]
        ch = text[i]
        if mode == 'code':
            if pair == '//':
                out[i] = out[i+1] = ' '; i += 2; mode = 'line'; continue
            if pair == '/*':
                out[i] = out[i+1] = ' '; i += 2; mode = 'block'; continue
            if ch == '"': mode = 'string'
            elif ch == "'": mode = 'char'
        elif mode == 'line':
            if ch == '\n': mode = 'code'
            else: out[i] = ' '
        elif mode == 'block':
            if pair == '*/':
                out[i] = out[i+1] = ' '; i += 2; mode = 'code'; continue
            if ch != '\n': out[i] = ' '
        elif mode in ('string', 'char'):
            if ch == '\\' and i + 1 < len(text): i += 2; continue
            if ch == ('"' if mode == 'string' else "'"): mode = 'code'
        i += 1
    return ''.join(out)


def mask_strings(text: str) -> str:
    """Mask Java string/character contents, preserving line/character positions."""
    out = list(text)
    mode = 'code'
    i = 0
    while i < len(text):
        c = text[i]
        if mode == 'code' and c in ('"', "'"):
            mode = c; out[i] = ' '
        elif mode != 'code':
            if c == '\\' and i+1 < len(text):
                out[i] = ' '; out[i+1] = ' '; i += 2; continue
            if c == mode: mode = 'code'
            if c != '\n': out[i] = ' '
        i += 1
    return ''.join(out)


def as_int(s) -> int:
    try: return int(s or 0)
    except (TypeError, ValueError): return 0


def main():
    p = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    p.add_argument('--exports', type=Path, required=True, help='Directory with pandora-queries.xml, pandora-taa.xml, pandora-schema.json, pandora-JavaMetrics.xml')
    p.add_argument('--source', type=Path, required=True, help='r348 root directory (containing java/), java/ itself, or sources ZIP')
    p.add_argument('--output', type=Path, default=Path('results/step-2'), help='Root results directory; writes complementary/ only')
    p.add_argument('--context', type=int, default=15, help='Lines around candidate call to include in evidence text (default 15)')
    p.add_argument('--cleanup-legacy', action='store_true', help='Remove earlier generated files outside the J2.2 AI approach deliverable (never SQLInspect inputs)')
    args = p.parse_args()
    if args.context < 0: p.error('--context must be >= 0')
    mandatory = ['pandora-queries.xml','pandora-taa.xml','pandora-schema.json','pandora-JavaMetrics.xml']
    for x in mandatory:
        if not (args.exports / x).is_file(): p.error(f'Missing SQLInspect file: {args.exports / x}')
    if not args.source.exists(): p.error(f'Source not found: {args.source}')

    java = scan_java(args.source)  # refuse zero sources BEFORE touching results
    source_fingerprint = hashlib.sha256()
    for filepath, content in sorted(java.items()):
        source_fingerprint.update(filepath.encode('utf-8'))
        source_fingerprint.update(b'\0')
        source_fingerprint.update(content.encode('utf-8'))
        source_fingerprint.update(b'\0')
    source_sha = source_fingerprint.hexdigest()
    reviewed_source_matches = source_sha == REVIEWED_JAVA_SHA256

    textmap = {k.lower(): (k, v) for k,v in java.items()}
    query_root = ET.parse(args.exports / 'pandora-queries.xml').getroot()
    taa_root = ET.parse(args.exports / 'pandora-taa.xml').getroot()
    java_metrics = ET.parse(args.exports / 'pandora-JavaMetrics.xml').getroot()
    schema = json.loads((args.exports / 'pandora-schema.json').read_text('utf-8-sig'))
    tables = {t['Name'].lower(): t for db in schema['Databases'] for sch in db['Schemas'] for t in sch['Tables']}
    if not tables: raise ValueError('Schema contains no tables')
    total_columns = sum(len(t['Columns']) for t in tables.values())

    native_metrics = {}
    for n in java_metrics.findall('./JavaElement/Metrics/Metric'):
        native_metrics[n.get('name','')] = as_int(n.get('value'))
    if not native_metrics:
        for n in java_metrics.findall('.//Metric'):
            native_metrics.setdefault(n.get('name',''), as_int(n.get('value')))

    table_locs, column_locs = defaultdict(set), defaultdict(set)
    table_access = defaultdict(lambda: {'tables':set(),'columns':set()})
    for stmt in taa_root.findall('.//Statement'):
        path = normpath(stmt.get('Path','')).lower()
        if not path.endswith('.java'): continue  # never count mysql.sql DDL as program usage
        line = as_int(stmt.get('Line'))
        key = (path,line)
        for node in stmt.findall('./Tables/Table'):
            tbl = node.get('name','').lower().split('.')[-1]
            if tbl in tables:
                table_locs[tbl].add(key); table_access[key]['tables'].add(tbl)
        for node in stmt.findall('./Columns/Column'):
            parts = node.get('name','').lower().split('.')
            if len(parts) >= 2:
                tbl,col = parts[-2:]
                if tbl in tables and any(c['Name'].lower()==col for c in tables[tbl]['Columns']):
                    column_locs[(tbl,col)].add(key); table_access[key]['columns'].add(f'{tbl}.{col}')
                    table_locs[tbl].add(key)

    queries, q_by_loc, types = [], defaultdict(list), Counter()
    for q in query_root.findall('./Query'):
        path = normpath(q.findtext('ExecFile',''))
        line = as_int(q.findtext('ExecLine'))
        sql = q.findtext('Value','') or ''
        kind_match = SQL_START.match(sql)
        kind = kind_match.group(1).upper() if kind_match else 'UNRESOLVED'
        types[kind] += 1
        key = (path.lower(),line)
        q_by_loc[key].append(q.get('id',''))
        queries.append({'query_id':q.get('id',''), 'file_path':path, 'line':line,'type':kind,'sql':sql,
                        'dynamic': '{{' in sql,'execution_call':q.findtext('ExecString',''),
                        'tables':sorted(table_access[key]['tables']),'columns':sorted(table_access[key]['columns'])})
    locations = set(q_by_loc)

    # Token search is lexical, not fully type-aware. Match calls only in Java code, NOT string literals.
    calls, candidate_rows, literal_rows = [], [], []
    for path, content in java.items():
        comments_cleaned = mask_comments_keep_strings(content)
        masked = mask_strings(comments_cleaned)
        lines = content.splitlines()
        for m in CALL_RE.finditer(masked):
            line = masked.count('\n',0,m.start())+1
            receiver, method = m.group('receiver'),m.group('method')
            sql_ids = q_by_loc.get((path.lower(),line),[])
            row = {'file_path':path,'start_line':line,'end_line':line,'receiver':receiver,'method':method,
                   'source_line':lines[line-1].strip()[:500] if line<=len(lines) else '',
                   'sqlinspect_query_ids':';'.join(sql_ids), 'category': 'SQLINSPECT_MATCH' if sql_ids else ('EXECUTION_CANDIDATE' if method in EXEC_METHODS else 'PREPARATION_CANDIDATE'),
                   'review_status':'ALREADY_DETECTED' if sql_ids else 'REQUIRES_REVIEW'}
            calls.append(row)
            if not sql_ids and method in EXEC_METHODS: candidate_rows.append(row)
        # Literal evidence: SQL keywords and schema-table names inside string fragments.
        # We keep these as candidates; they must not become proven runtime uses automatically.
        for lineno, raw in enumerate(comments_cleaned.splitlines(),1):
            strings = [v.group()[1:-1] for v in LITERAL_RE.finditer(raw)]
            if not strings: continue
            joined = ' '.join(strings)
            for tbl in {v.lower() for v in TABLE_RX.findall(joined)} & set(tables):
                literal_rows.append({'file_path':path,'line':lineno,'table':tbl.lower(),'source_line':lines[lineno-1].strip()[:600],
                                     'status':'SQL_LITERAL_REFERENCE_CANDIDATE'})
    callfields = ['file_path','start_line','end_line','receiver','method','source_line','sqlinspect_query_ids','category','review_status']
    comp = args.output/'complementary'
    lss = args.output/'logical-subschema'
    # Full call inventory belongs to grep/scripts workstream; only J2.2 AI review is delivered.
    write_csv(comp/'J2_2_sql_literal_table_candidates.csv',['file_path','line','table','source_line','status'],literal_rows)

    # Excerpts + conservative classification based on observable receiver/call context.
    excerpts = []
    reviews = []
    jdbc_lines = sorted(locations)
    for c in candidate_rows:
        path, line = c['file_path'],int(c['start_line'])
        lines = java[path].splitlines()
        excerpts += ['='*70,f'File: {path}',f'Candidate line: {line}','='*70]
        for ln in range(max(1,line-args.context),min(len(lines),line+args.context)+1):
            source_line = lines[ln - 1].rstrip()
            if source_line:
                excerpts.append(f'{">>" if ln == line else "  "} {ln:5}: {source_line}')
            else:
                excerpts.append(f'{" >>" if ln == line else "   "}{ln:5}: [blank line]')
        excerpts.append('')
        rec, method = c['receiver'],c['method']
        surrounding = '\n'.join(lines[max(0,line-25):line])
        cls, reason = 'UNRESOLVED', 'Lexical match: receiver type and downstream execution need verification'
        if rec == 'this' and method=='executeQuery':
            cls,reason = 'INTERNAL_DAO_FORWARDING','this.executeQuery calls an overload in the containing class (not a primitive JDBC method)'
        elif rec in ('dao','dbBus','bus','del','db') and method=='executeQuery':
            cls,reason = 'APPLICATION_SQL_CALL','Receiver calls an application executeQuery method; must trace to JDBC execution site'
        elif rec == 'connector' and method == 'execute' and ('new Connector(' in surrounding or 'Connector connector' in surrounding):
            cls,reason = 'NON_JDBC_EXECUTE','Connector.execute is an integration API, not JDBC Statement.execute'
        elif rec in ('statement','stmt','pstmt','ps','preparedStatement'):
            cls,reason = 'POSSIBLE_JDBC_MISSED','JDBC-like receiver name; confirm declared type and scope before counting'
        reviews.append({**c,'classification':cls,'reason':reason,
                        'new_jdbc_hotspot_status':'UNCONFIRMED','evidence_file':'J2_2_code_evidence.txt',
                        'review_level':'AUTOMATED_TRIAGE_NOT_HUMAN_VERIFIED'})
    # Deterministic evidence review: classify the same 16 supplied candidates only
    # when the actual source location/method is present in the analyzed revision.
    # This is a recorded AI-assisted source analysis, not a type-resolved tool run.
    review_seen = set()
    for row in reviews:
        key = (row['file_path'], int(row['start_line']))
        row['ai_assisted_review'] = 'NOT_REVIEWED'
        row['ai_review_finding'] = ''
        row['ai_review_access_kind'] = ''
        row['ai_review_verification'] = 'MANUAL_INDEPENDENT_CHECK_RECOMMENDED'
        row['additional_primitive_jdbc_hotspot'] = 'NOT_CONFIRMED'
        if reviewed_source_matches and key in AI_REVIEW and row['method'] == ('execute' if key[0].endswith('ConnectorTest.java') else 'executeQuery'):
            expected, note, access_kind = AI_REVIEW[key]
            if row['classification'] == expected:
                row['ai_assisted_review'] = 'REVIEWED_AGAINST_SOURCE_EXCERPT'
                row['ai_review_finding'] = note
                row['ai_review_access_kind'] = access_kind
                row['ai_review_verification'] = 'CONSISTENT_WITH_SOURCE_EXCERPT_AND_KNOWN_DAO_CHAIN'
                review_seen.add(key)
    # Do not claim completion of AI-assisted review if the analyzed source differs.
    review_complete = len(review_seen) == len(AI_REVIEW) == len(candidate_rows)
    check_ids = {ln: q_by_loc.get(('java/com/pandora/dao/dbquerydao.java', ln), []) for ln in (139,176,264)}
    dao_jdbc_crosscheck = check_ids == {139:['573'],176:['574'],264:['575']}
    (comp/'J2_2_code_evidence.txt').write_text('\n'.join(excerpts).rstrip()+'\n', encoding='utf-8')
    review_fields = callfields+['classification','reason','new_jdbc_hotspot_status','evidence_file','review_level','ai_assisted_review','ai_review_finding','ai_review_access_kind','ai_review_verification','additional_primitive_jdbc_hotspot']
    write_csv(comp/'J2_2_additional_execution_candidates_TO_REVIEW.csv',review_fields,reviews)
    # Do not create an empty 'confirmed' CSV: absence of evidence is not confirmation.

    # Compare SQLInspect locations to scanned call sites. Report unmatched as review items.
    seen = {(r['file_path'].lower(),int(r['start_line'])) for r in calls if r['sqlinspect_query_ids']}
    missing = [{'file_path':q['file_path'],'line':q['line'],'query_id':q['query_id'],'reason':'NO_LEXICAL_CALL_MATCH'}
               for q in queries if (q['file_path'].lower(),q['line']) not in seen]
    # Only create a discrepancy file when there are actual discrepancies.
    # A zero-row CSV from an earlier run may be open in Excel on Windows;
    # avoid rewriting it, which would raise WinError 32 before cleanup.
    if missing:
        write_csv(comp/'J2_1_sqlinspect_locations_not_found_in_source.csv',
                  ['file_path','line','query_id','reason'],missing)

    missing_java = sorted({q['file_path'] for q in queries if q['file_path'].lower() not in textmap})
    dynamic_count = sum(q['dynamic'] for q in queries)
    unres_count = types.get('UNRESOLVED',0)
    safe = lambda x: x.replace('|','\\|')
    report = f'''# J2.2 — AI-assisted SQL Access Review (Plandora r348)

## Reproduction

Python 3.10+; Python standard library only. From the repository root:

```powershell
python code/scripts/analyse_j2_complet.py --exports results/step-2/sqlinspect --source code/plandora/reference/r348 --output results/step-2
```

Input: SQLInspect XML/JSON exports and tracked Plandora r348 Java sources. Output: `complementary/` only. The original SQLInspect J2.1 exports are not modified.

## Coverage and SQLInspect baseline

- Java files scanned: **{len(java)}**.
- SQLInspect query records: **{len(queries)}**.
- Distinct SQLInspect execution locations: **{len(locations)}**.
- Source Java calls with SQLInspect site match: **{len(seen)}** distinct sites.
- SQLInspect query records without same-line lexical API match: **{len(missing)}** (not necessarily missed JDBC; inspect CSV).
- Source execution-like calls without direct SQLInspect location match: **{len(candidate_rows)}** candidates.
- SQLInspect query records containing dynamic placeholders: **{dynamic_count}**.
- Textually unresolved query records: **{unres_count}**.
- Native SQLInspect metrics: {json.dumps(native_metrics, sort_keys=True)}.
- SQL text-prefix classification: {json.dumps(dict(types), sort_keys=True)}. These counts are NOT SQLInspect's native type metrics.

## Candidate classification (automated triage)

| Classification | Count |
|---|---:|
'''
    for cls,n in Counter(r['classification'] for r in reviews).most_common():report+=f'| {cls} | {n} |\n'
    report+=f'''
This is a name/context heuristic, not a Java type-resolved call graph. `J2_2_additional_execution_candidates_TO_REVIEW.csv` includes both mechanical triage and a separately marked AI-assisted review of the 16 known source excerpts. The AI-assisted review is not an LLM call made by the script and is not a Java type-resolved call graph. No confirmed-new-JDBC file is produced because this automation cannot certify new primitive JDBC hotspots. Any confirmed extra site must be added after independent review. Do not equate this with proof that no missed JDBC accesses exist.

Known manually checked JDBC sites in `java/com/pandora/dao/DbQueryDAO.java` (SQLInspect IDs 573, 574, 575) must be cross-checked against your exported SQLInspect inventory at lines 139, 176, 264. Present in this run: {[(ln, q_by_loc.get(('java/com/pandora/dao/dbquerydao.java',ln),[])) for ln in (139,176,264)]}.

## J2.2 AI-assisted approach (separate from SQLInspect)

**Question.** Can a second, AI-assisted inspection of the r348 Java implementation identify additional database access, SQL construction, or missed primitive JDBC execution locations beyond J2.1?

**Method.** (1) Independently scan r348 Java for execute-like methods and SQL string fragments, without using SQLInspect results to generate candidates; (2) reconcile each candidate by file and line with the exported SQLInspect locations; (3) inspect 15-line source windows on either side of each unmatched call; (4) use AI-assisted code reasoning to separate JDBC primitives, application wrappers, internal DAO overloads and unrelated APIs; (5) cross-check known primitive JDBC operations against SQLInspect query IDs; (6) retain uncertain paths rather than manufacturing proof. The reviewed decisions are encoded in the script and checked against the scan locations at every run.

**Reproducibility and disclosure.** The Python script runs entirely offline using lexical rules and a recorded AI-assisted review of source excerpts. It does **not** invoke a generative model, train a model, or benchmark AI accuracy. AI assistance was used to interpret the supplied code evidence and formulate the review; the independent Java scanner is Python, not AI. This distinction matters for the academic claim.

- Source fingerprint matches AI-reviewed reference r348: **{reviewed_source_matches}**. If false, conclusions are left unreviewed until the changed source is inspected.
- Source-excerpt reviewed candidates: **{sum(r['ai_assisted_review']=='REVIEWED_AGAINST_SOURCE_EXCERPT' for r in reviews)}/{len(candidate_rows)}**; review mapping complete: **{review_complete}**.
- Source-reviewed classifications: **{json.dumps(dict(Counter(r['classification'] for r in reviews if r['ai_assisted_review']=='REVIEWED_AGAINST_SOURCE_EXCERPT')), sort_keys=True)}**.
- Independently confirmed **additional primitive JDBC hotspots among these 16 candidates**: **{0 if review_complete else "not established on changed sources"}**; this is **not** a proof of global JDBC completeness.
- Existing DAO primitive JDBC execution sites 139, 176 and 264 correspond to SQLInspect IDs 573, 574 and 575: **{dao_jdbc_crosscheck}**.

### Source-reviewed candidate decisions

| Source call | Classification | AI-assisted evidence interpretation |
|---|---|---|
{chr(10).join('| `'+r['file_path'].split('/')[-1]+':'+str(r['start_line'])+'` | '+r['classification']+' | '+r['ai_review_finding'].replace('|','/')+' |' for r in reviews)}

**Specific SQL evidence omitted by the SQLInspect TAA:** `WrapperDBRepository.java` constructs SQL for `db_repository_item`, `db_repository_version` and `db_repository_sequence`, including DELETE, INSERT, SELECT and UPDATE operations through `DbQueryDelegate` / `DbQueryBUS` / `DbQueryDAO`. These are additional *table/column references*, **not** 13 or 16 new JDBC execution primitives. `FileToTableNotification.java` builds statements from runtime-selected table/column names; the target schema cannot be exhaustively recovered statically. `DbAction.java` and `NotificationBUS.java` can accept dynamic SQL, so matching a small set of literals is not complete application coverage.

**Scope and remaining limits.** The repository README describes a validated project scope of 24 tables, while the complete SQLInspect LS export contains 83 tables; this J2.2 comparison analyzes the entire available LS and does not claim that all 83 belong to the agreed validated scope. This exercise did not execute Plandora, resolve every Java receiver type, follow all reflection paths, or verify arbitrary runtime-configured statements. A final reduced LSS would require constraint and dynamic-path verification and is a separate completion criterion from the J2.2 AI-assisted comparison.

## Handover to the other workstreams

The AI-assisted review identifies additional SQL construction in `WrapperDBRepository.java` for `db_repository_item`, `db_repository_version` and `db_repository_sequence`. The locations and relevant source fragments are in `J2_2_sql_literal_table_candidates.csv` and the reviewed-candidate CSV. This is a **completed evidence handover**, not a claim to deliver a final logical subschema. Final LSS construction, independent grep/scripts deliverables, and J2.3 preparation are outside this report's scope.

## Important SQL dynamic cases

- `FileToTableNotification.java` constructs DELETE/INSERT statements with a runtime-selected table and fields; the target cannot be enumerated reliably statically.
- `WrapperDBRepository.java` references repository tables in SQL construction through `DbQueryDelegate`; the delegation itself is not a JDBC `Statement` call.
- `DbQueryDAO.java` funnels dynamic SQL through `PreparedStatement` operations; a detected hotspot can have an unresolved SQL text.

## Limitations and completion criteria

- Lexical Python scanning can produce false positives from application-defined methods sharing JDBC names.
- It may miss reflection, dynamically chosen SQL strings or other persistence APIs.
- Matching by file and line does not establish Java receiver types or link every caller to every JDBC execution.
- SQLInspect TAA references are positive static evidence, not runtime-use measurements.
- The results concern the 16 unmatched candidates, not a formal proof that SQLInspect has no other omissions.
- Dynamic runtime SQL targets remain inherently indeterminate without runtime configuration.

## Output inventory

- `complementary/J2_2_additional_execution_candidates_TO_REVIEW.csv`: 16 candidates including source-reviewed AI interpretations (the name retains the original automated triage meaning; the review columns indicate completed source-excerpt review).
- `complementary/J2_2_sql_literal_table_candidates.csv`: independently recovered SQL table-literal references.
- `complementary/J2_2_code_evidence.txt`: original numbered Java excerpts.
- `complementary/J2_2_run_manifest.json`: source fingerprints and review reproducibility.
- `complementary/J2_2_AI_approach_report_EN.md`: final report for J2.2 AI approach, with limitations.
'''
    (comp/'J2_2_AI_approach_report_EN.md').write_text(report,encoding='utf-8')
    dump_json(comp/'J2_2_run_manifest.json',{'java_sources_sha256':source_sha, 'confirmed_additional_jdbc_sites':[], 'confirmed_status':'NOT_ESTABLISHED_BY_AUTOMATED_ANALYSIS', 'java_count':len(java),'query_records':len(queries),'hotspots':len(locations),
        'candidate_count':len(candidate_rows), 'ai_assisted_reviewed_candidate_count':len(review_seen), 'ai_assisted_review_complete':review_complete, 'reference_source_fingerprint_matches':reviewed_source_matches, 'dao_sqlinspect_crosscheck':dao_jdbc_crosscheck, 'schema_tables':len(tables),'schema_columns':total_columns,
        'input_sha256':{f:hashlib.sha256((args.exports/f).read_bytes()).hexdigest() for f in mandatory},
        'source_path':str(args.source),'method':'Python lexical preselection + recorded AI-assisted source-excerpt review (no LLM API call)'})
    if args.cleanup_legacy:
        # Explicit allowlist; never touch SQLInspect or statistics directories.
        old_comp = (
            'J2_1_sqlinspect_detected_accesses.csv', 'J2_query_distribution_by_file.csv',
            'J2_query_types.csv', 'J2_sql_smells.csv',
            'J2_2_candidate_review_PRELIMINARY.csv', 'J2_2_candidate_validation_REVIEW.csv',
            'J2_2_additional_JDBC_hotspots_CONFIRMED.csv',
            'J2_LSS_PROVISOIRE.json', 'J2_LSS_table_review.csv', 'J2_analysis_report.md',
            'J2_2_all_source_calls.csv', 'J2_2_LSS_review_report_EN.md',
        )
        old_lss = ('J2_LSS_table_review.csv', 'J2_LSS_column_evidence.csv', 'J2_LSS_evidence_based_PROVISIONAL.json')
        deleted, locked = [], []
        def try_remove(path):
            try:
                path.unlink()
                deleted.append(str(path))
            except (PermissionError, OSError) as exc:
                # Windows Excel/OneDrive/antivirus can keep files open.
                # The validated outputs have already been generated.
                locked.append(f'{path}: {exc}')
        for folder, names in ((comp, old_comp), (lss, old_lss)):
            for name in names:
                target = folder / name
                if target.is_file():
                    try_remove(target)
        discrepancy = comp / 'J2_1_sqlinspect_locations_not_found_in_source.csv'
        if not missing and discrepancy.is_file():
            try:
                with discrepancy.open(encoding='utf-8-sig', newline='') as fh:
                    empty = not any(csv.DictReader(fh))
                if empty:
                    try_remove(discrepancy)
            except (PermissionError, OSError) as exc:
                locked.append(f'{discrepancy}: {exc}')
        print(f'Legacy duplicate files removed: {len(deleted)}')
        if locked:
            print(f'WARNING: {len(locked)} legacy file(s) could not be removed (open in another process).', file=sys.stderr)
            for item in locked:
                print('WARNING: ' + item, file=sys.stderr)
            print('Close Excel/CSV previews and delete these leftover files later; analysis succeeded.', file=sys.stderr)
    print('J2.2 AI approach outputs generated (no provisional LSS).')
    print(f'Java: {len(java)} | SQLInspect: {len(queries)} queries, {len(locations)} sites | review candidates: {len(candidate_rows)}')
    print(f'Output: {args.output.resolve()}')
    print(f'J2.2 AI-assisted source review: {len(review_seen)}/{len(candidate_rows)} candidates; source fingerprint matched: {reviewed_source_matches}; DAO crosscheck: {dao_jdbc_crosscheck}')
    if not review_complete: print('WARNING: J2.2 source-excerpt review not complete for this source revision; do not mark J2.2 reviewed.', file=sys.stderr)
    print('NOTICE: review uses recorded source-excerpt reasoning, not an in-script LLM or formal Java type analysis; LSS is outside this deliverable.')


if __name__ == '__main__':
    try: main()
    except (ValueError,OSError,ET.ParseError,zipfile.BadZipFile,KeyError) as exc:
        print(f'ERROR: {exc}',file=sys.stderr)
        sys.exit(1)
