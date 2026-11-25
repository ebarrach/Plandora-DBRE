import csv
import re
from collections import Counter

SCOPE = 'method/analysis/scope-tables.txt'
COLUMNS = 'results/step-1/physical-schema/tables.csv'
QUERIES = 'results/step-2/sqlinspect/J2_1_sqlinspect_detected_accesses.csv'
COMPLEXITY = 'results/step-2/statistics/J2_query_complexity_heuristic.csv'
MANUAL = 'results/step-2/complementary/manual/J2_2_manual_detected_accesses.csv'
STATISTICS = 'results/step-2/statistics/J2_3_scope_statistics.txt'
LSS = 'results/step-2/logical-subschema/LSS_columns.csv'


def read_csv(path):
    with open(path, encoding='utf-8-sig') as f:
        return list(csv.DictReader(f))


scope = set(open(SCOPE).read().split())
queries = read_csv(QUERIES)
complexity = {r['query_id']: r['heuristic_complexity'] for r in read_csv(COMPLEXITY)}
manual = read_csv(MANUAL)

# statistics: only the SQLInspect queries that touch at least one of our 24 tables
in_scope = [q for q in queries if scope & set(q['tables_sqlinspect'].split(';'))]
places = {(q['file_path'], q['end_line']) for q in in_scope}
groups = [
    ('Type', Counter(q['sql_type'] for q in in_scope).most_common()),
    ('Complexity', Counter(complexity[q['query_id']] for q in in_scope).most_common()),
    ('File (top 10)', Counter(q['file_path'].split('/')[-1] for q in in_scope).most_common(10)),
    ('Table', Counter(t for q in in_scope for t in q['tables_sqlinspect'].split(';') if t in scope).most_common()),
    ('Manual J2.2 accesses by kind', Counter(m['kind'] for m in manual).most_common()),
    ('Manual J2.2 accesses, SQL known', Counter(m['sql_known'] for m in manual).most_common()),
]
with open(STATISTICS, 'w') as f:
    f.write(f'SQLInspect queries: {len(queries)}\n')
    f.write(f'Queries touching our 24 tables: {len(in_scope)}, in {len(places)} places in the code\n')
    f.write(f'Accesses found only by the manual J2.2: {len(manual)}\n')
    for title, counts in groups:
        f.write(f'\n{title}\n')
        for name, n in counts:
            f.write(f'  {name}: {n}\n')

# LSS: a column is used if at least one query touches it
example = {}
for q in queries:
    sql = q['sql_text'].lower()
    for name in q['columns_sqlinspect'].lower().split(';'):
        if '.' not in name:
            continue
        table, column = name.split('.', 1)
        # SQLInspect lists every column of the table for INSERT and DELETE, keep only the ones in the statement
        if q['sql_type'] in ('INSERT', 'DELETE') and not re.search(r'\b' + column + r'\b', sql):
            continue
        example.setdefault((table, column), q['file_path'].split('/')[-1] + ':' + q['end_line'])

columns = [(r['table'], r['name'].lower()) for r in read_csv(COLUMNS)]
with open(LSS, 'w', newline='') as f:
    w = csv.writer(f)
    w.writerow(['table', 'column', 'used', 'example'])
    for table, column in columns:
        w.writerow([table, column, 'yes' if (table, column) in example else 'no', example.get((table, column), '')])

unused = [f'{t}.{c}' for t, c in columns if (t, c) not in example]
print(f'{len(in_scope)} queries on our 24 tables -> {STATISTICS}')
print(f'LSS: {len(columns) - len(unused)}/{len(columns)} columns used -> {LSS}')
print('not used:', ', '.join(unused))