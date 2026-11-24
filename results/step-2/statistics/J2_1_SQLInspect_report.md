# J2.1 — SQLInspect analysis (Plandora r348)

## Scope and tools

- Target: Plandora source-code revision r348, Java and MySQL.
- SQLInspect version: 0.4.2 (as configured in Eclipse for this analysis).
- SQL dialect: MySQL; schema definition input: `mysql.sql`.
- Post-processing: Python 3.10+ (Python standard library, no external dependencies).
- This document describes the SQLInspect-based J2.1 extraction, **not** the independent J2.2 source-code analysis.

## Reproduction

From the repository root:

```powershell
python code/scripts/generer_j2_1_csv.py --exports results/step-2/sqlinspect --output results/step-2/sqlinspect/J2_1_sqlinspect_detected_accesses.csv
python code/scripts/generer_j2_1_statistiques.py --exports results/step-2/sqlinspect --inventory results/step-2/sqlinspect/J2_1_sqlinspect_detected_accesses.csv --output results/step-2/statistics
```

The inventory is derived from `pandora-queries.xml` and cross-referenced to `pandora-taa.xml` by Java file path and execution line. Distinct hotspots are unique `(file path, execution line)` pairs. The inventory has one row per SQLInspect query entry; multiple query entries can belong to the same hotspot. The end-line field represents the JDBC call line and not necessarily the entire SQL string's source span.

## Observed results

- SQLInspect query entries: **728**.
- Distinct JDBC hotspot locations in the exported inventory: **433**.
- SQLInspect native Java metrics (source export): **Deletes=89, Hotspots=433, Inserts=67, Queries=728, Selects=489, Updates=61**.
- Imported LS: **83 tables**, **563 columns**.
- SQL smells: **16**, including any findings whose source is `mysql.sql` rather than a Java file.
- Query entries with `{{...}}` placeholders: **695**.
- Query entries linked to a TAA statement by execution location: **712**; entries without a matching TAA statement: **16**. A match does not guarantee complete table/column resolution.
- Query entries whose execution file is under `com.pandora.dao`: **725** (99.6%).

### Query types, classified from SQL text (not native SQLInspect metrics)

| SQL text classification | Query entries |
|---|---:|
| DELETE | 89 |
| INSERT | 67 |
| SELECT | 502 |
| UNRESOLVED | 9 |
| UPDATE | 61 |

These counts can differ from SQLInspect's native metrics because dynamic/unresolved statements and the tool's query classification have different definitions. In particular, do **not** substitute the text-based SELECT count for the native `Selects` metric.

### Heuristic query complexity (independent post-processing)

The heuristic strips literal strings and SQLInspect placeholders, then measures explicit `JOIN`, parenthesized `SELECT`, `GROUP BY`, `HAVING`, and `UNION` signals. Score = joins + 2×subqueries + group-by + having + union. Scores 0–1 are *low*, 2–3 *moderate*, and 4+ *high*; unresolved SQL is *unclassified*. These labels are **not** native SQLInspect metrics and are **not** a complete SQL parser; comma joins, nested/dynamic fragments, and other constructs can be underestimated.

| Heuristic category | Query entries |
|---|---:|
| high | 59 |
| low | 519 |
| moderate | 141 |
| unclassified | 9 |

### Top 10 source files by exported query entries

| Java file | Query entries | Distinct hotspots |
|---|---:|---:|
| `java/com/pandora/dao/RequirementDAO.java` | 209 | 18 |
| `java/com/pandora/dao/ReportDAO.java` | 46 | 18 |
| `java/com/pandora/dao/ResourceTaskDAO.java` | 38 | 16 |
| `java/com/pandora/dao/CostDAO.java` | 29 | 29 |
| `java/com/pandora/dao/TaskDAO.java` | 27 | 22 |
| `java/com/pandora/dao/OccurrenceDAO.java` | 24 | 21 |
| `java/com/pandora/dao/SurveyDAO.java` | 24 | 21 |
| `java/com/pandora/dao/ProjectDAO.java` | 18 | 10 |
| `java/com/pandora/dao/RepositoryDAO.java` | 16 | 15 |
| `java/com/pandora/dao/TaskNodeTemplateDAO.java` | 16 | 15 |

The complete distributions are in the CSV files in this folder.

## Deliverables

- `../sqlinspect/pandora-queries.xml`: original detected SQL query records.
- `../sqlinspect/pandora-taa.xml`: SQLInspect table/column access analysis.
- `../sqlinspect/pandora-schema.json`: exported LS schema.
- `../sqlinspect/pandora-JavaMetrics.xml`: native Java/query metrics.
- `../sqlinspect/pandora-SQLMetrics.xml`: native SQL metrics export.
- `../sqlinspect/pandora-smells.xml`: reported SQL smells.
- `../sqlinspect/pandora.json`: additional raw SQLInspect export.
- `../sqlinspect/J2_1_sqlinspect_detected_accesses.csv`: one row per query entry, including execution location and optional TAA evidence.
- `J2_query_types.csv`, `J2_query_distribution_by_file.csv`, `J2_query_complexity_heuristic.csv`, `J2_sql_smells.csv`: reproducible post-processing statistics.
- `J2_1_summary.json`: machine-readable validation counters.

## Limitations and next step

- Query reconstruction may be incomplete for string concatenation, dynamically generated SQL, JDBC wrappers, and placeholders such as `{{na}}` / `{{question}}`.
- This data set records **SQLInspect detections**, not independently verified database execution paths.
- Unmatched TAA entries and unresolved SQL are evidence gaps, **not** proof of unused tables or columns.
- SQLInspect table/column information is only linked by execution location; where multiple statements share a location, the TAA associations are not necessarily attributable to one specific query variant.
- J2.2 must independently scan Plandora r348 Java source, inspect missed candidates, distinguish real JDBC executions from application wrappers, and cross-check SQLInspect results.
- The final logical subschema (LSS) is not established by this J2.1 report alone; it requires validated source-level evidence and schema-constraint review.
