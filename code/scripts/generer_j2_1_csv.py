#!/usr/bin/env python3
"""Convert SQLInspect query and table-access XML exports into a reproducible CSV.
Python >= 3.10; standard library only. Does not inspect Java source code.
"""
import argparse
import collections
import csv
import re
import xml.etree.ElementTree as ET
from pathlib import Path

SQL_START = re.compile(r'^\s*(?:\{\{(?:na|question)\}\}\s*)*(SELECT|INSERT|UPDATE|DELETE|CREATE|ALTER|DROP|REPLACE|TRUNCATE|WITH|MERGE|CALL)\b', re.I)
FIELDS = [
    'query_id', 'file_path', 'start_line', 'end_line',
    'execution_call', 'sql_type', 'sql_text', 'has_dynamic_fragments',
    'tables_sqlinspect', 'columns_sqlinspect', 'taa_match', 'callstack_depth',
]


def relative_java_path(path):
    """Strip machine-specific prefix; preserve the Java source-relative path."""
    path = (path or '').replace('\\', '/')
    position = path.lower().rfind('/java/')
    if position >= 0:
        return path[position + 1:]
    if path.lower().startswith('java/'):
        return path
    return path  # cannot safely determine relative location


def location(path, line):
    return (relative_java_path(path).casefold(), int(line or 0))


def terminal_qualified_name(value, count):
    components = (value or '').split('.')
    return '.'.join(components[-count:]).lower() if len(components) >= count else ''


def main():
    parser = argparse.ArgumentParser(description='Generate the J2.1 SQLInspect JDBC inventory CSV')
    parser.add_argument('--exports', type=Path, default=Path('exports'))
    parser.add_argument('--output', type=Path, default=Path('results/step-2/sqlinspect/J2_1_sqlinspect_detected_accesses.csv'))
    args = parser.parse_args()

    queries_xml = args.exports / 'pandora-queries.xml'
    taa_xml = args.exports / 'pandora-taa.xml'
    for f in (queries_xml, taa_xml):
        if not f.is_file():
            parser.error(f'Fichier introuvable : {f}')

    try:
        query_root = ET.parse(queries_xml).getroot()
        taa_root = ET.parse(taa_xml).getroot()
    except ET.ParseError as e:
        parser.error(f'XML invalide : {e}')

    taa_by_location = collections.defaultdict(lambda: {'tables': set(), 'columns': set()})
    for stmt in taa_root.findall('.//Statement'):
        path = stmt.get('Path', '')
        if not path.lower().endswith('.java'):
            continue  # exclude mysql.sql DDL, which is not application usage
        key = location(path, stmt.get('Line', '0'))
        entry = taa_by_location[key]
        for table in stmt.findall('./Tables/Table'):
            name = terminal_qualified_name(table.get('name'), 1)
            if name:
                entry['tables'].add(name)
        for column in stmt.findall('./Columns/Column'):
            name = terminal_qualified_name(column.get('name'), 2)
            if name:
                entry['columns'].add(name)

    rows = []
    types = collections.Counter()
    for query in query_root.findall('./Query'):
        get = lambda tag: query.findtext(tag, default='') or ''
        file_path = relative_java_path(get('ExecFile'))
        line = int(get('ExecLine') or '0')
        sql = get('Value')
        match = SQL_START.match(sql)
        query_type = match.group(1).upper() if match else 'UNRESOLVED'
        key = location(file_path, line)
        taa = taa_by_location.get(key)
        rows.append({
            'query_id': query.get('id', ''),
            'file_path': file_path,
            'start_line': line,
            'end_line': line,  # JDBC call line; not the span of the SQL string
            'execution_call': get('ExecString'),
            'sql_type': query_type,
            'sql_text': sql,
            'has_dynamic_fragments': 'true' if '{{' in sql else 'false',
            'tables_sqlinspect': ';'.join(sorted(taa['tables'])) if taa else '',
            'columns_sqlinspect': ';'.join(sorted(taa['columns'])) if taa else '',
            'taa_match': 'true' if taa is not None else 'false',
            'callstack_depth': len(query.findall('./CallStack/Call')),
        })
        types[query_type] += 1

    args.output.parent.mkdir(parents=True, exist_ok=True)
    with args.output.open('w', encoding='utf-8-sig', newline='') as handle:
        writer = csv.DictWriter(handle, fieldnames=FIELDS)
        writer.writeheader()
        writer.writerows(rows)

    print(f'CSV créé : {args.output.resolve()}')
    print(f'Requêtes : {len(rows)}')
    print(f'Emplacements JDBC distincts : {len({location(r["file_path"], r["start_line"]) for r in rows})}')
    print(f'Entrées avec correspondance TAA : {sum(r["taa_match"] == "true" for r in rows)}')
    print(f'Entrées sans correspondance TAA : {sum(r["taa_match"] == "false" for r in rows)}')
    print('Types (classification textuelle) :', dict(sorted(types.items())))
    print('ATTENTION : TAA et type SQL peuvent être incomplets pour les requêtes dynamiques.')


if __name__ == '__main__':
    main()
