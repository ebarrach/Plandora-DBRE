# J2.2 — AI-assisted SQL Access Review (Plandora r348)

## Reproduction

Python 3.10+; Python standard library only. From the repository root:

```powershell
python code/scripts/analyse_j2_complet.py --exports results/step-2/sqlinspect --source code/plandora/reference/r348 --output results/step-2
```

Input: SQLInspect XML/JSON exports and tracked Plandora r348 Java sources. Output: `complementary/` only. The original SQLInspect J2.1 exports are not modified.

## Coverage and SQLInspect baseline

- Java files scanned: **760**.
- SQLInspect query records: **728**.
- Distinct SQLInspect execution locations: **433**.
- Source Java calls with SQLInspect site match: **433** distinct sites.
- SQLInspect query records without same-line lexical API match: **0** (not necessarily missed JDBC; inspect CSV).
- Source execution-like calls without direct SQLInspect location match: **16** candidates.
- SQLInspect query records containing dynamic placeholders: **695**.
- Textually unresolved query records: **9**.
- Native SQLInspect metrics: {"Deletes": 89, "Hotspots": 433, "Inserts": 67, "Queries": 728, "Selects": 489, "Updates": 61}.
- SQL text-prefix classification: {"DELETE": 89, "INSERT": 67, "SELECT": 502, "UNRESOLVED": 9, "UPDATE": 61}. These counts are NOT SQLInspect's native type metrics.

## Candidate classification (automated triage)

| Classification | Count |
|---|---:|
| APPLICATION_SQL_CALL | 13 |
| INTERNAL_DAO_FORWARDING | 2 |
| NON_JDBC_EXECUTE | 1 |

This is a name/context heuristic, not a Java type-resolved call graph. `J2_2_additional_execution_candidates_TO_REVIEW.csv` includes both mechanical triage and a separately marked AI-assisted review of the 16 known source excerpts. The AI-assisted review is not an LLM call made by the script and is not a Java type-resolved call graph. No confirmed-new-JDBC file is produced because this automation cannot certify new primitive JDBC hotspots. Any confirmed extra site must be added after independent review. Do not equate this with proof that no missed JDBC accesses exist.

Known manually checked JDBC sites in `java/com/pandora/dao/DbQueryDAO.java` (SQLInspect IDs 573, 574, 575) must be cross-checked against your exported SQLInspect inventory at lines 139, 176, 264. Present in this run: [(139, ['573']), (176, ['574']), (264, ['575'])].

## J2.2 AI-assisted approach (separate from SQLInspect)

**Question.** Can a second, AI-assisted inspection of the r348 Java implementation identify additional database access, SQL construction, or missed primitive JDBC execution locations beyond J2.1?

**Method.** (1) Independently scan r348 Java for execute-like methods and SQL string fragments, without using SQLInspect results to generate candidates; (2) reconcile each candidate by file and line with the exported SQLInspect locations; (3) inspect 15-line source windows on either side of each unmatched call; (4) use AI-assisted code reasoning to separate JDBC primitives, application wrappers, internal DAO overloads and unrelated APIs; (5) cross-check known primitive JDBC operations against SQLInspect query IDs; (6) retain uncertain paths rather than manufacturing proof. The reviewed decisions are encoded in the script and checked against the scan locations at every run.

**Reproducibility and disclosure.** The Python script runs entirely offline using lexical rules and a recorded AI-assisted review of source excerpts. It does **not** invoke a generative model, train a model, or benchmark AI accuracy. AI assistance was used to interpret the supplied code evidence and formulate the review; the independent Java scanner is Python, not AI. This distinction matters for the academic claim.

- Source fingerprint matches AI-reviewed reference r348: **True**. If false, conclusions are left unreviewed until the changed source is inspected.
- Source-excerpt reviewed candidates: **16/16**; review mapping complete: **True**.
- Source-reviewed classifications: **{"APPLICATION_SQL_CALL": 13, "INTERNAL_DAO_FORWARDING": 2, "NON_JDBC_EXECUTE": 1}**.
- Independently confirmed **additional primitive JDBC hotspots among these 16 candidates**: **0**; this is **not** a proof of global JDBC completeness.
- Existing DAO primitive JDBC execution sites 139, 176 and 264 correspond to SQLInspect IDs 573, 574 and 575: **True**.

### Source-reviewed candidate decisions

| Source call | Classification | AI-assisted evidence interpretation |
|---|---|---|
| `FileToTableNotification.java:97` | APPLICATION_SQL_CALL | DbQueryDelegate receiver; DELETE statement uses a configuration-selected table. Runtime target is not statically enumerable. |
| `FileToTableNotification.java:148` | APPLICATION_SQL_CALL | DbQueryDelegate executes constructed INSERT statements in a batch; table and fields depend on configuration. |
| `NotificationBUS.java:78` | APPLICATION_SQL_CALL | DbQueryBUS receives an SQL statement obtained from notification configuration; application-level method. |
| `DbQueryBUS.java:56` | APPLICATION_SQL_CALL | Receiver dao is constructed as DbQueryDAO; forwards SQL to its executeQuery method. |
| `DbQueryBUS.java:67` | APPLICATION_SQL_CALL | DbQueryDAO batch overload; forwards SQL and parameters. |
| `WrapperDBRepository.java:243` | APPLICATION_SQL_CALL | DbQueryDelegate executes DELETE statements for db_repository_version and db_repository_item. |
| `WrapperDBRepository.java:325` | APPLICATION_SQL_CALL | DbQueryDelegate executes constructed repository INSERT statements; db_repository_version SQL is explicit. |
| `WrapperDBRepository.java:454` | APPLICATION_SQL_CALL | DbQueryDelegate executes constructed repository INSERT statements for item and version. |
| `WrapperDBRepository.java:498` | APPLICATION_SQL_CALL | DbQueryDelegate executes UPDATE of db_repository_sequence(id, project_id); not a JDBC primitive at this line. |
| `WrapperDBRepository.java:500` | APPLICATION_SQL_CALL | DbQueryDelegate executes INSERT into db_repository_sequence(id, project_id); not a JDBC primitive at this line. |
| `DbQueryDAO.java:94` | INTERNAL_DAO_FORWARDING | this.executeQuery(sql, null, c) invokes a same-class overload; true JDBC calls occur downstream. |
| `DbQueryDAO.java:110` | INTERNAL_DAO_FORWARDING | this.executeQuery(theSql, params.get(i), c) invokes a same-class overload; true JDBC calls occur downstream. |
| `DbQueryDelegate.java:53` | APPLICATION_SQL_CALL | DbQueryDelegate forwards to its DbQueryBUS instance. |
| `DbQueryDelegate.java:57` | APPLICATION_SQL_CALL | DbQueryDelegate forwards SQL and parameters to DbQueryBUS. |
| `DbAction.java:75` | APPLICATION_SQL_CALL | Web action sends split SQL statements through DbQueryDelegate; this line is not JDBC Statement.executeQuery. |
| `ConnectorTest.java:35` | NON_JDBC_EXECUTE | Integration Connector.execute accepts a payload and URL target; it is unrelated to JDBC. |

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
