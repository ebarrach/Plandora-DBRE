# Environment

## Observed on the Windows workstation

User-supplied terminal output confirms:
- Java runtime: Temurin 1.8.0_504, 64-bit.
- javac: 1.8.0_504.
- Plandora source root: code/plandora/reference/r348/java.
- Original Ant build declares ISO-8859-1 encoding.
- Original Eclipse classpath references J2SE-1.5.
- It also contains the external /SGU/lib/postgresql-42.2.27.jre7.jar path.
- Several JasperReports versions occur in the classpath.

These observations do not prove successful compilation or runtime compatibility.

## IntelliJ setup

Open the repository root. Configure a Java module using the existing
code/plandora/reference/r348 directory as its content root and its java
directory as Sources. Keep generated IDE configuration ignored by Git.

Use the installed Java 8 JDK as an initial compatibility candidate.
Check the required language level against the sources; the historical
J2SE-1.5 label does not establish that every recent source uses only Java 5.

Set the reference Java source encoding to ISO-8859-1 in local IDE settings.
Add required bundled JARs from lib and inspect missing/conflicting dependencies.
Do not silently rewrite .classpath, build.xml or original sources.
Do not replace the absent external PostgreSQL path without recording the decision.

Keep compiler output outside the reference tree.
Do not invoke the original Ant build there: resetdir deletes build/dist and
deployment paths, and the default war target copies into Tomcat.
Use an experimental copy under code/experiments for builds or deployment.

## Other tools

- REA integration: see REA.md.
- Eclipse/SQLInspect: use the course-recommended compatible installation.
- Database server, JDBC driver, Ant and analysis-script dependencies:
  record actual versions and validation results when installed.
- A modern JDK for analysis tools can coexist with Java 8 for the legacy target.
