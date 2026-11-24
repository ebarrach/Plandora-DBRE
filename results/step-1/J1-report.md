# J1 - Inventaire et reconstruction documentaire

Référence : **SVN r348** ; snapshot Git `12c9a7b8355ef311fd37b9afa30e18fabe652979`. Sources conservées dans `code/plandora/reference/r348/`. Tous les constats ci-dessous sont documentaires, sans base active. Extraction complémentaire distincte de SQLInspect.

## Résultats observés

- 24 tables, 175 colonnes, 39 FK internes déclarées, 30 FK de frontière. Les quatre repères du prompt sont retrouvés, sans ajustement des données.
- Aucun CREATE INDEX explicite dans les 24 tables ; les PK sont des contraintes déclarées, pas des index supplémentaires inventoriés. Aucun index de support implicite des FK n’est présenté comme déclaré.
- Les 39 FK internes passent les contrôles documentaires : colonnes présentes, arité, appariement ordonné, types exacts normalisés et clé cible candidate. Les mêmes contrôles sont faits sur les 30 FK de frontière ; une anomalie est signalée ci-dessous.
- Nullabilité : conserver séparément NULL explicite, NOT NULL explicite et absence de clause. La PK implique une obligation selon la sémantique MySQL ; cette implication ne réécrit pas le texte du DDL. DEFAULT absent reste absent. Aucun comportement TIMESTAMP dépendant d’une version du serveur n’est affirmé.
- Les contraintes composites restent des tuples ordonnés. Le minimum inverse est toujours 0 ; son maximum est 1 seulement si le tuple source contient une clé candidate entière. Sinon, maximum N. Le minimum vers la cible est 1 si toutes les colonnes sont requises, sinon 0, sous réserve d’une clé cible candidate.

## Trois niveaux et lecture

1. **Physical schema** : 24 tables et 175 déclarations de colonnes, types, nullabilité, defaults, PK et 39 FK déclarées exclusivement. `inventory.json` est l’inventaire détaillé ; `scope-mysql.sql` est un sous-ensemble sans DROP ni INSERT pour extraction dans un outil de modélisation.
2. **Logical schema** : relations et identifiants conservés ; optionalités déduites du DDL ; les deux références récupérées I1/I2 sont orange et ne deviennent pas des FK déclarées. Les hypothèses H1-H3 sont exclues des liens.
3. **Conceptual schema candidate** : objets métier, associations nommées, entités associatives et événements historiques. Les identifiants composites restent visibles pour assurer la traçabilité. `table-mapping.csv` relie exactement les 24 tables aux abstractions métier ; `associations.json` porte les cardinalités et preuves.

Les SVG et PDF utilisent des cartes et un registre F1-F39 pour conserver une lecture non ambiguë sans croisements. Une ligne de registre est une référence complète, avec tuple source, tuple cible, deux multiplicités et ligne DDL. Le fichier draw.io contient trois pages éditables et les connecteurs attachés aux cartes, masqués initialement ; leurs libellés restent visibles dans le registre. Dans diagrams.net, éditer le XML ou rendre les cellules d’arêtes visibles pour afficher ces connecteurs. Aucun import externe n’a été exécuté.

## Liens récupérés et hypothèses

- **I1 observé dans une requête** : `plan_relation.plan_related_id -> planning.id`, jointure explicite dans `java/com/pandora/dao/PlanningRelationDAO.java:41-44` (préfixe de référence ci-dessus). La FK correspondante est absente du DDL. Le DAO insère les deux identifiants aux lignes 164-167. Une jointure ne démontre pas l’absence d’orphelins.
- **I2 inféré du mapping objet** : `task.created_by -> tool_user.id`, `TaskDAO.java:1001-1011` construit un UserTO ; `UserDAO.java:92-98` associe son identifiant à tool_user. C’est une référence applicative étayée, sans contrainte déclarée ni preuve de validité des valeurs.
- **H1** `customer.function_id -> function.id` : non validé. `CustomerDAO.java:111-118` lit `u.function_id`, pas `c.function_id` ; son INSERT aux lignes 335-338 omet cette colonne. `CustomerFunctionDAO.java:69-71` utilise une autre table, `customer_function`, hors périmètre. Ne pas confondre ces trois emplacements.
- **H2/H3** `requeriment_status.accept_project_id` et `.parent_id` : candidats uniquement. Aucun usage Java exact de accept_project_id trouvé ; les SELECT et le mapping de `RequirementStatusDAO.java:26-27,113-119` omettent ces deux colonnes. Les autres parent_id ne prouvent rien pour cette table. Recherche complète et extraits dans les JSON dédiés ; une absence dans cette recherche ne prouve pas l’inutilité dans toutes les configurations.

## Interprétation conceptuelle et cardinalités

- **Project participation** est l’appellation candidate de `customer` : sa clé `(id, project_id)` associe un utilisateur à un projet, les FK customer_user/customer_project l’étayent. `CustomerDAO.java:214-217,272-281` joint tool_user et filtre le projet ; `CustomerTO.java:8` étend UserTO. Il ne s’agit donc pas nécessairement d’une organisation cliente.
- `resource` et `leader` sont des spécialisations candidates de cette participation, sur la même clé composite. `ResourceTO.java:10`, `LeaderTO.java:7`, `ResourceDAO.java:373-379` et `LeaderDAO.java:198-203` confortent le rôle métier. Les FK sur PK prouvent une inclusion d’identifiants et une occurrence parent unique par sous-type. Elles ne prouvent aucune totalité ni disjonction. `root` est un sous-type supplémentaire hors périmètre.
- `project`, `requeriment` (Request) et `task` spécialisent potentiellement **Planning item** : FK id->planning.id et jointures ProjectDAO:169-174, RequirementDAO:250-252, TaskDAO:405-409. Le parent peut ne relever d’aucun de ces trois sous-types, ou d’autres types hors périmètre. Totalité/disjonction restent inconnues.
- **Assignment** (`resource_task`) relie une Task à une Resource participation identifiée par `(resource_id, project_id)` ; son statut est un Assignment status, même si le nom SQL est task_status. Le DDL seul n’impose pas que resource_task.project_id égale task.project_id : ne pas ajouter cette contrainte métier.
- **Actual allocation** et **Planned allocation** dépendent du tuple d’affectation, puis de sequence. `ResourceTaskAllocDAO.java:55-64` et `ResourceTaskAllocPlannedDAO.java:27-35` le confirment. Les unités de alloc_time et la signification temporelle exacte de sequence ne sont pas inventées.
- **Capacity slot** est identifié par participation ressource et (cap_year,cap_month,cap_day). Le DAO utilise resource_id et project_id ensemble (`ResourceDAO.java:387`, `ResourceCapacityDAO.java:333-339`). Aucune validité calendaire ni unité de capacité n’a été testée.
- **Transitions / histories** : project_history, requeriment_history, task_history sont des événements identifiés par leur propriétaire, statut et creation_date ; task_history vise une affectation complète. DDL et DAO ne prouvent ni une chaîne de transitions complète ni l’unicité d’un ordre métier au-delà des PK.
- `requeriment.user_id` vise tool_user dans le DDL, bien que le DAO le transforme en CustomerTO (RequirementDAO:1138-1170). La participation du demandeur au projet n’est donc pas une contrainte déclarée. ProjectDAO:479-491 normalise aussi des sentinelles parent/company ; aucune donnée réelle n’a été inspectée.

## Dépendances externes et anomalie

`external_dependencies.csv` conserve les 30 FK entrantes/sortantes avec ordre, noms et lignes. Les objets extérieurs ne sont pas ajoutés aux 24 tables. Notamment root->leader et les extensions déclarées de planning (artifact, invoice, occurrence, cost) restent des frontières. PlanningDAO.java:177-229 traite aussi RiskTO et ExpenseTO : ces usages applicatifs ne prouvent pas une FK id->planning déclarée pour risk ou expense.

**Anomalie observée** : `mysql.sql:1153`, cus_func_customer référence `customer(id)` seul alors que la PK est `(id, project_id)`. La cible n’est pas une clé candidate déclarée. Les colonnes et types correspondent, mais on ne peut pas conclure à une cible unique. Certains comportements MySQL historiques concernant les index non uniques dépendent de version/engine et ne sont pas validés ici. Ce n’est pas une erreur du parseur ni une réparation autorisée du DDL.

## Cohérence avec les DAO

Les 39 contraintes internes ont une ligne dans `dao-review.csv` et `dao-review.json`, avec plage de requête ou de mapping, extrait et conclusion. « Accord documentaire » décrit la cohérence de l’opération citée ; cela ne signifie ni exécution réussie, ni audit exhaustif des chemins de code, ni validation de contenu.

## HSQL distinct

Comparaison colonne par colonne produite dans `hsql-comparison.json` ; 51 écarts de type/nullabilité/default explicites sont conservés avec les deux sources. Aucun mélange des dialectes.

## Outils et limites réelles

- Python 3.12 et reportlab/PyMuPDF déjà présents ; génération, parsing XML/CSV/JSON et contrôles structurels reproductibles. Les images PNG de toutes les pages PDF sont conservées pour revue.
- DB-MAIN 11.0.2 détecté, exécutable démarré ; sonde native JIDBM : version=11.0.2, platform=WIN64. Premier essai échoué (DLL dépendantes introuvables), second réussi avec le bin DB-MAIN ajouté au PATH du seul processus. Aucun projet chargé, créé ou importé par JIDBM. Voir `method/design/DB-MAIN-J1.md`.
- Aucun accès Lucidchart disponible : compatibilité d’import non vérifiée, aucun import annoncé. Le .drawio est du XML mxGraph éditable, sans garantie d’acceptation par Lucidchart.
- MySQL, HSQLDB, SQLInspect, tests de données : non exécutés. Aucun contenu de base ni résultat de compilation Plandora n’est affirmé. Aucun export SQLInspect n’a été créé ou modifié.

## Reproduction

```powershell
python code/tools/schema/j1.py
python code/tools/schema/j1_document.py
python code/tools/validation/j1_validate.py
python code/tools/validation/reference_manifest.py
git diff --exit-code -- code/plandora/reference/
```

## Descriptions ClickUp prêtes à copier

**J1.1 FR** — Inventaire documentaire du DDL MySQL r348 : 24 tables, 175 colonnes, 39 FK internes et 30 FK de frontière ; déclarations et lignes sources conservées. **EN** — Inventory r348 MySQL DDL with exact declarations, ordered keys and source evidence; external dependencies kept separate.

**J1.2 FR** — Schémas physique, logique reconstruit et conceptuel candidat, exports draw.io/SVG/PDF, traçabilité des 24 tables et distinction des références récupérées. **EN** — Produce physical, reconstructed logical and candidate conceptual models with editable exports and traceable mappings.

**J1.3 FR** — Vérification documentaire des FK et des DAO ; signalement de cus_func_customer, références applicatives et hypothèses non validées ; import DB-MAIN à effectuer selon le guide. **EN** — Validate FK structure and cited DAO operations; report the boundary key anomaly and unresolved hypotheses. DB-MAIN import remains a manual verification step.
