# J2.2 Manual inventory of detected accesses

This document describes the manual part of J2.2: finding the database accesses that SQLInspect does not report. The source code is searched with standard command-line tools, and every result is read by hand to decide whether it is a real database access, where its SQL comes from and which tables it uses.

The results are compared with the SQLInspect inventory of J2.1, on our 24 tables of Plandora r348.

### Step 1: Find every place where Java sends SQL to the database

Every database access in Java ends with a JDBC call such as `executeQuery(` or `executeUpdate(`. I search for these calls in all Java files.

```
grep -rnE "\.(executeQuery|executeUpdate|executeBatch|execute)\(" --include='*.java' code/plandora/reference/r348 > results/step-2/complementary/manual/01_jdbc_calls.txt
```

Result: 457 lines in `01_jdbc_calls.txt`.

### Step 2: Compare the manual list with SQLInspect

SQLInspect gives a file and a line for every query it finds. I put both lists in the same form (`file:line`), sort them and compare them with `comm`.

```
cd results/step-2/complementary/manual
tail -n +2 ../../sqlinspect/J2_1_sqlinspect_detected_accesses.csv | cut -d, -f2,4 | tr ',' ':' | sort -u > sqlinspect_places.txt
cut -d: -f1,2 01_jdbc_calls.txt | sed 's|code/plandora/reference/r348/||' | sort -u > manual_places.txt
comm -13 sqlinspect_places.txt manual_places.txt > 02_not_in_sqlinspect.txt
comm -23 sqlinspect_places.txt manual_places.txt | wc -l
```

Result: SQLInspect reports 433 places, and all 433 are also in the manual list (the check gives 0). 24 places are only in the manual list (`02_not_in_sqlinspect.txt`).

### Step 3: Read the 24 places that SQLInspect does not report

I opened each place in IntelliJ, and decided what it is.

| Place | What it is                                                                                                  | DB access?           |
|---|-------------------------------------------------------------------------------------------------------------|----------------------|
| ConnectorTest.java: 35, 39, 43, 48, 55, 59, 63, 67 | connector.execute() sends data over HTTP, not to the db, lines 39-67 are commented anyway                   | no                   |
| DbQueryDelegate.java:50 | comment                                                                                                     | no                   |
| DbQueryBUS.java: 56, 67, DbQueryDAO.java: 94, 110, DbQueryDelegate.java: 53, 57 | only passes the sql string to the next method, the real call is DbQueryDAO.java:264 (already in SQLInspect) | no, middle step      |
| FileToTableNotification.java: 97, 148 | delete and insert are built here, the table name comes from the notification config                         | yes, but table unknown                |
| WrapperDBRepository.java: 243, 325, 454, 498, 500 | SQL is written right here (delete, insert, update on the db_repository tables)                              | yes                  |
| NotificationBUS.java:78 | SQL comes from the notification table in the DB                                                             | yes, but sql unknown |
| DbAction.java:75 | the admin types the sql on a web page                                                                       | yes, but sql unknown |

Result: 9 false, 6 middle steps, 9 real accesses that SQLInspect doesn't show.

### Step 4: Find all the calls to the SQL executor

Step 3 shows that Plandora has its own sql executor (DbQueryDelegate -> DbQueryBUS -> DbQueryDAO). SQLInspect only sees its last JDBC call (DbQueryDAO.java:264) without the sql, the real sql is written in the code that calls the executor.

The executor has 3 methods that take sql: executeQuery, performQuery and getMetaData. Step 1 only found the executeQuery calls, so here I search for the other two, outside the executor classes.

```
grep -rnE "\.(performQuery|getMetaData)\(" --include='*.java' code/plandora/reference/r348 | grep -vE "DbQuery(DAO|BUS|Delegate)\.java" | cut -d: -f1,2 > results/step-2/complementary/manual/04_executor_calls.txt
```

Result: 34 places in 04_executor_calls.txt. In 24 of them the SQL is written right in the method (mostly the dashboard gadgets and WrapperDBRepository). In 10: sql comes from data or from the admin: the notification, notification_field, report and meta_field tables, artifact templates and the admin sql page. In ReplicateTableNotification.java:164 sql is built in the method, but the table name comes from the notification config. Marks for each place are in 04_check_temp.txt.

### Step 5: Find SQL outside the Java code

SQLInspect only reads Java files. Plandora also has SQL in 3 other places: report templates (.jrxml), compiled reports (.jasper) and the rows of the report table, where the SQL is stored as data.

```
grep -n "<queryString" code/plandora/reference/r348/*.jrxml | cut -d: -f1,2 > results/step-2/complementary/manual/05_report_queries.txt
grep -ohE '"[A-Za-z_]+\.jasper"' code/plandora/reference/r348/*.jrxml | tr -d '"' | sort -u > results/step-2/complementary/manual/05_subreports.txt
for f in code/plandora/reference/r348/*.jasper; do [ -f "${f%.jasper}.jrxml" ] || echo "$f"; done > results/step-2/complementary/manual/05_compiled_without_source.txt
grep -Hn "^insert into report (" code/plandora/reference/r348/mysql.sql | cut -d: -f1,2 > results/step-2/complementary/manual/05_sql_in_report_table.txt
```

.jasper files are binary, so here's commands where strings reads the SQL inside, and grep checks if anything uses them

```
for f in $(cat results/step-2/complementary/manual/05_compiled_without_source.txt); do n=$(basename $f);
echo "$n | sql: $(strings $f | grep -i -m1 'select ' | cut -c1-60) | used by: $(grep -rl "$n" code/plandora/reference/r348 --include='*.java' --include='*.jrxml' --include='*.sql' --include='*.xml' --include='*.jsp' | tr '\n' ' ')"; done > results/step-2/complementary/manual/05_compiled_check.txt
```

from [05_compiled_check.txt](05_compiled_check.txt) we can see nothing uses these 3 files, so their sql never runs.

Result: 12 report templates are subreports, their sql runs. The 10 main templates don't run their own sql, they get their data from the SQL stored in the report table (6 rows in mysql.sql, all of them run). The 3 compiled reports are not used by anything.

### Step 6: Put all the accesses in one csv file

All places marked + or +? in [02_check_temp.txt](02_check_temp.txt), [04_check_temp.txt](04_check_temp.txt) and [05_check_temp.txt](05_check_temp.txt) are real database accesses that SQLInspect doesn't show. This command will put them in one list.

```
M=results/step-2/complementary/manual
{
  echo "id,file_path,line,kind,sql_known"
  cat $M/02_check_temp.txt $M/04_check_temp.txt $M/05_check_temp.txt \
  | grep -E ' \+\??$' \
  | sed 's|^code/plandora/reference/r348/||' \
  | awk '{
      split($1, a, ":"); file = a[1]; line = a[2]
      if (file ~ /\.jrxml$/) kind = "jasper subreport"
      else if (file ~ /mysql\.sql$/) kind = "sql stored in report table"
      else kind = "executor call"
      known = ($2 == "+?") ? "no" : "yes"
      printf "M%02d,%s,%s,%s,%s\n", NR, file, line, kind, known
    }'
} > $M/J2_2_manual_detected_accesses.csv
```

Result: 61 accesses in J2_2_manual_detected_accesses.csv: 43 executor calls (29 with sql in the code, 14 where the sql or table name comes from data, from notification config or from admin), 12 jasper subreports and 6 sql queries stored in the report table.