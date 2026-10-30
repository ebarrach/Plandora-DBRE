# Working rules

- The validated scope contains 24 tables; see method/analysis/scope-tables.txt.
- Do not modify code/plandora/reference/.
- Put experiments in code/experiments/ and analysis tools in code/tools/.
- Work on feature branches based on develop.
- main receives reviewed pull requests from develop only.
- Support findings with revision, repository-relative path and line numbers.
- Separate observed facts, inferred relationships and unverified hypotheses.
- Keep SQLInspect exports separate from complementary extraction results.
- Record query locations with start/end lines or call lines.
- Do not claim SQLInspect ran unless it actually ran.
- Do not invent measurements, database contents or successful tests.
- Document dependencies outside the selected database scope.
- Preserve upstream licence notices.