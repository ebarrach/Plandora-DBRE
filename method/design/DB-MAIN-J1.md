# DB-MAIN / JIDBM : vérification réelle et reprise manuelle J1

## Installation observée le 17 novembre 2026

- Exécutable : `C:\Program Files\DB-Main 11.0.2\bin\db_main.exe`.
- Interface Java : `bin\jidbm.jar`, bibliothèque native `bin\dbm_core.dll`.
- Manuels : `doc\DB-MAIN-Reference-Manual.pdf`, `doc\JIDBM-reference-manual.pdf`, Javadoc dans `doc\JIDBM-javadoc\`.
- Un lancement sans argument, avec répertoire de travail `code/experiments/j1/` et fenêtre cachée, a créé le processus 2716 ; après trois secondes il n'avait pas terminé. Ce constat confirme un démarrage de processus, pas un import SQL ni un projet valide.
- Pas de surface native de contrôle graphique disponible dans cette session ; aucune action de menu effectuée.

## Signatures installées et différence avec le manuel

Vérification effectuée avec `javap -classpath "C:\Program Files\DB-Main 11.0.2\bin\jidbm.jar"` :

```java
DBMLibrary.loadDBMProject(String) throws IOException
DBMLibrary.loadDBMProjectFromISL(String) throws IOException
DBMLibrary.loadDBMProjectFromXML(String) throws IOException
DBMLibrary.getCurrentProject() // native long, pas DBMProject
DBMProject(long)               // pas de constructeur sans argument dans le JAR installé
DBMProject.createSchema(String,String,String,Date,Date,char,DBMSchema)
DBMSchema.extractSql(DBMSchema,String,String) // int, noms/usage des paramètres non inférés
```

Le manuel JIDBM, page PDF 20 (page imprimée 12), §3.1.2.1, dit explicitement que JIDBM ne permet pas de créer un projet neuf : utiliser `File/New Project…` dans DB-MAIN. Les exemples des pages PDF 14–15 montrent un chargement d'un fichier projet existant, mais utilisent aussi des signatures anciennes (`new DBMProject()`, `getCurrentProject()` objet) qui ne correspondent pas au JAR installé. Ne pas recopier ces exemples sans adaptation aux signatures.

Aucun fichier initial autorisé n'a été fourni. Aucun exemple installé n'a été chargé ni modifié ; aucun .lun n'est fabriqué ou annoncé. L'interface documentée permet d'enrichir un projet existant, mais cette session n'a pas exécuté d'extraction SQL DB-MAIN.

## Sonde native exécutée sans projet

Source : `code/experiments/j1/DBMainProbe.java` ; compilation et exécution réelles :

```powershell
javac -encoding UTF-8 --release 8 -classpath 'C:\Program Files\DB-Main 11.0.2\bin\jidbm.jar' -d code/experiments/j1 code/experiments/j1/DBMainProbe.java
$env:PATH='C:\Program Files\DB-Main 11.0.2\bin;'+$env:PATH
& 'C:\Program Files\DB-Main 11.0.2\java\jre\bin\java.exe' '-Djava.library.path=C:\Program Files\DB-Main 11.0.2\bin' -classpath 'code/experiments/j1;C:\Program Files\DB-Main 11.0.2\bin\jidbm.jar' DBMainProbe
```

La compilation a réussi avec trois avertissements sur la cible Java 8 obsolète. Premier lancement sans adaptation du PATH : échec `Can't find dependent libraries`, puis `UnsatisfiedLinkError`. Second lancement avec le PATH ci-dessus : code de sortie 0, sortie :

```text
version=11.0.2
platform=WIN64
No project loaded or modified.
```

Le PATH n'a été changé que pour le processus PowerShell de la sonde, sans modification persistante du poste.

## Manipulations à effectuer dans DB-MAIN

Procédure appuyée sur le manuel local **DB-MAIN Reference Manual**, §11.2, page PDF 114 (imprimée 94), et §11.5.1, page PDF 116 (imprimée 96). Les libellés ci-dessous sont ceux du manuel anglais ; l'acceptation du dialecte MySQL historique doit encore être vérifiée dans l'application.

1. Ouvrir `C:\Program Files\DB-Main 11.0.2\bin\db_main.exe` normalement. Utiliser **File → New project…**. Nommer le projet `Plandora J1 r348`. Ne pas ouvrir ni écraser un projet de travail existant.
2. Utiliser **File → Save project as…** et enregistrer un nouveau fichier `D:\Projects\Java\Plandora-DBRE\code\experiments\j1\Plandora-J1-r348.lun`. Si ce nom existe déjà, choisir un nouveau nom ; ne pas écraser l'original. Ce fichier reste à créer par vous.
3. Utiliser **File → Extract → SQL…**. Sélectionner le fichier déjà préparé `D:\Projects\Java\Plandora-DBRE\results\step-1\physical-schema\scope-mysql.sql`. Il contient exactement les 24 CREATE TABLE et les 39 ALTER FK internes, sans DROP, INSERT ni FK extérieures. Ne pas exécuter ce fichier sur une base active.
4. Conserver les diagnostics de l'extracteur. Vérifier les 24 tables et 175 colonnes, les 39 FK et leurs tuples ordonnés à l'aide de `physical-schema\inventory.json`, `physical-schema\tables.csv` et `foreign-keys\foreign_keys.csv`. Les index implicites affichés par un outil ne doivent pas être requalifiés en déclarations présentes dans le DDL. Si la syntaxe historique `primary key pk_name (...)` est refusée, conserver le diagnostic ; ne pas modifier la référence. Une adaptation séparée devra être tracée avant réimport.
5. Conserver ce schéma sous le nom **Physical - declared DDL**. Créer les deux autres schémas par les fonctions de copie/édition disponibles dans votre interface ; leurs libellés exacts n'ont pas été vérifiés dans une session graphique. Pour **Logical - reconstructed**, suivre `logical-schema\logical.pdf` et `foreign-keys\inferred_links.csv` : ajouter I1 et I2 dans un style distinct, sans les présenter comme déclarées. Ne pas ajouter H1-H3.
6. Pour **Conceptual - candidate**, suivre `conceptual-schema\table-mapping.csv`, `conceptual-schema\associations.json` et `conceptual-schema\conceptual.pdf`. Nommer les associations, garder les rôles de participation et les entités d'affectation/allocation/capacité/historique. Conserver la totalité et la disjonction des spécialisations comme inconnues. Les détails de manipulation des sous-types dépendent de l'interface affichée et restent à vérifier.
7. Enregistrer dans le nouveau projet seulement. Le .drawio livré est un autre export éditable ; son import par DB-MAIN n'est pas documenté ici. Ne pas tenter de l'ouvrir comme un .lun. Les fichiers SVG/PDF sont des supports de lecture, pas des projets DB-MAIN natifs.

Le manuel §11.2.2 indique que l'ouverture d'un .lun crée automatiquement un fichier de verrouillage. Aucune protection n'a été désactivée par l'agent.

## Lucidchart

Aucun connecteur ou accès Lucidchart disponible dans le catalogue de session. Aucun format d'import Lucidchart n'a été vérifié et aucun import n'a été effectué. Le fichier `results/step-1/physical-schema/j1-schemas.drawio` est du XML mxGraph à trois pages ; il n'est pas annoncé comme validé pour Lucidchart.
