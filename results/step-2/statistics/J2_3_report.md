# J2.3 Statistics, logical subschema and coverage gaps

This part keeps what touches our 24 tables, using three inputs: the SQLInspect inventory of J2.1, the manual list of J2.2 and the schema of step 1 (175 columns). The numbers come from [j2_3_analysis.py](../../../code/scripts/j2_3_analysis.py), run from the repository root. It writes [J2_3_scope_statistics.txt](J2_3_scope_statistics.txt) and [LSS_columns.csv](../logical-subschema/LSS_columns.csv).

## Statistics on our 24 tables

503 of the 728 SQLInspect queries touch at least one of our 24 tables. They come from 215 places in the code.

- Reading dominates: 417 SELECT, 39 DELETE, 27 UPDATE, 20 INSERT.
- Most queries are simple: 317 low, 132 moderate and 54 high complexity (output of J2.1).
- One file holds 41% of them: RequirementDAO.java (208). It builds its sql from pieces, so SQLInspect lists many variants of the same query.
- All of them are in the DAO layer (com.pandora.dao).
- Most used tables: planning (309), tool_user (281), project (272), task and category (243 each). Least used: department and project_history (1 each), function (3).

On top of that, the manual J2.2 found 61 accesses that SQLInspect does not report: 43 calls to Plandora's own sql executor, 12 Jasper subreports and 6 sql queries stored in the report table. For 11 of them the sql is only known at runtime.

## Logical subschema (LSS)

A column is in the LSS if at least one query uses it. SQLInspect gives the columns of each query, with one trap: for INSERT and DELETE it lists every column of the table, even the ones that are not in the statement (for example RequirementDAO.java:818 inserts into requeriment without is_acceptance, but SQLInspect lists it). For these two types only the columns written in the statement are kept. The columns that remain unused were then searched in the whole code base (Java, reports, JSP and SQL files), which also covers the places found in J2.2.

Result: the 24 tables are all used, and 168 of the 175 columns. The 7 unused columns:

| Column | Why |
|---|---|
| requeriment.is_acceptance | only in the table definition (mysql.sql:260) |
| requeriment_status.accept_project_id | only in the table definition (mysql.sql:222) |
| requeriment_status.parent_id | only in the table definition (mysql.sql:223), the DAO reads only id, name, description and state_machine_order |
| resource_task.is_acceptance_task | only in the table definition (mysql.sql:288) |
| customer.function_id | never in the code, the function_id used by the DAOs is the one of tool_user, and the link is kept in the table customer_function |
| resource.capacity_per_day | old column: the install script copies it into the table resource_capacity (mysql.sql:1354-1362) |
| resource.cost_per_hour | same as above, it also appears in EVA.jrxml (a report that is never run) and in test code |

The database has no triggers, procedures or views, so nothing on the db side uses these columns either. None of the 7 is a foreign key, so all the relationships of the logical schema stay in the LSS.

Some columns are used but only written, never read: the whole table project_history (only an insert, ProjectHistoryDAO.java:29) and resource_task.approver_id (only an update, ResourceTaskDAO.java:1741).

## Coverage gaps

What SQLInspect does not see (details in the J2.2 manual report):
- SQL sent through Plandora's executor (DbQueryDelegate -> DbQueryBUS -> DbQueryDAO): SQLInspect sees only the executor's own JDBC calls (DbQueryDAO.java:139, 176, 264) without the sql, and not the 43 places where the sql is written;
- SQL in Jasper subreports (XML files) and SQL stored as data in the report table;
- SQL that only exists at runtime (admin SQL page, notifications, KPI reports, meta fields): no static analysis can know it.

## For the checkpoint (24 November)

- J2.1: SQLInspect found 728 queries in 433 places, 503 of them touch our 24 tables.
- J2.2: 61 more accesses that SQLInspect does not report, mostly calls to Plandora's own SQL executor.
- J2.3: all 24 tables are used, 168 of 175 columns. 7 columns are dead, 2 of them are left over from an old version.
- Next: the LSS diagram in DB-MAIN and step 3 (what-if scenarios).

## Limitations

- 11 accesses run sql that is only known at runtime, they could touch any column.
- Only the mysql.sql seed rows of the report table are counted, HSQL.sql has 3 more.
- The LSS relies on the columns named in the queries.