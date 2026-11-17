# Journal des opérations J1 - 17 novembre 2026

Snapshot de référence : SVN r348, Git `12c9a7b8355ef311fd37b9afa30e18fabe652979`.
Branche : `feature/j1-schemas`, créée depuis develop avant les écritures ; aucun commit/push/merge.

## Commandes exécutées et résultats observés

| Commande / opération | Résultat réel |
| --- | --- |
| `git status --short --branch`, `git rev-parse HEAD` | Branche feature/j1-schemas ; arbre initial propre ; révision ci-dessus |
| Détection PowerShell des commandes et répertoires installés | Python disponible sous le compte propriétaire ; reportlab, fitz, pypdf, PIL présents ; dot absent du PATH ; aucun ajout de dépendance |
| Lecture des sept documents préalables du prompt | Effectuée avant la première modification |
| `javap -classpath ...\jidbm.jar` sur DBMLibrary, DBMProject, DBMSchema, DBMEnvironment | Signatures réelles examinées ; pas de création de projet neuf par JIDBM documentée |
| Lecture des PDF DB-MAIN et JIDBM via PyMuPDF | Menus File/New project, File/Extract/SQL et prérequis de projet confirmés ; exemples de signatures anciennes identifiés |
| `Start-Process ...\db_main.exe -WindowStyle Hidden -PassThru`, vérification après 3 s | PID 2716 actif ; aucun projet/import exécuté ; processus toujours présent au dernier contrôle |
| `javac -encoding UTF-8 --release 8 -classpath ...\jidbm.jar -d code/experiments/j1 code/experiments/j1/DBMainProbe.java` | Compilation réussie, 3 avertissements de cible Java 8 obsolète |
| Sonde JIDBM sans bin DB-MAIN dans PATH du processus | Échec : DLL dépendantes introuvables, UnsatisfiedLinkError |
| Même sonde, bin DB-MAIN ajouté au PATH du processus seulement | Code 0 ; version=11.0.2, platform=WIN64 ; aucun projet chargé/modifié |
| `python code/tools/schema/j1.py` | Code 0 ; 24 tables, 175 colonnes, 39 FK internes, 30 FK de frontière, aucun CREATE INDEX dans le périmètre |
| `python code/tools/schema/j1_document.py` | Code 0 ; 39 revues DAO, rapport FR/EN, 3 hypothèses distinctes |
| `python code/tools/validation/j1_validate.py` | Code 0 ; 1 348 assertions documentaires/de formats, aucun échec ; anomalie cus_func_customer attendue et conservée |
| `python code/tools/validation/reference_manifest.py` | `Files: 1531; missing: 0; extra: 0; changed: 0` |
| `git diff --exit-code -- code/plandora/reference/` | Code 0, aucune sortie |
| Inspection des PNG des PDF | Planche des 20 pages et vues pleine taille représentatives inspectées ; pas de texte coupé constaté |
| `python code/tools/schema/j1_preview.py` | Code 0 ; planche des 20 pages finales et 3 aperçus SVG générés ; les trois aperçus SVG ont été inspectés |
| Comparaison indépendante HSQL | 51 écarts colonne/type/nullabilité/default consignés séparément ; aucune fusion des dialectes |
| `git diff --check` et `git status --porcelain -- code/plandora/reference/` finaux | Code 0 ; aucune sortie pour la référence |

## Incidents corrigés et limites

- Le compte sandbox ne trouvait pas certains programmes et refusait une écriture dans le workspace ; les opérations autorisées ont été effectuées sous le compte propriétaire, sans modifier les ACL ou safe.directory.
- Première exécution de l'extracteur : garde trop large sur les mots index/unique dans les chaînes INSERT. Échec réel ; garde restreinte aux déclarations CREATE/ALTER puis nouvelle exécution réussie. Aucun INSERT n'a été exécuté.
- Une première recherche visait RequerimentStatusDAO.java ; le vrai fichier est RequirementStatusDAO.java et a été lu.
- Aucune exécution MySQL/HSQLDB/SQLInspect, aucun test de données, aucune extraction DB-MAIN et aucun import Lucidchart.
- Le catalogue MCP consulté n'exposait pas de DB-MAIN/JIDBM/Lucidchart ; l'accès DB-MAIN testé est local, via exécutable et bibliothèque native installés.
- La réponse REA historique du 30 octobre n'a pas été réutilisée comme preuve d'une analyse J1. Aucun outil d'analyse REA n'a été exécuté pour J1.
- Aucun fichier existant supprimé, aucun fichier de référence ni configuration REA/DB-MAIN modifié. Les sources originales et leurs licences restent intactes.
