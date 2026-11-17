"""Generate reviewed documentary findings from the J1 inventory and source anchors."""
import json
import pathlib
import sys
sys.path.insert(0,str(pathlib.Path(__file__).resolve().parent))
from j1 import ROOT, REV, SOURCE, write, js, csvfile, evidence, source_hits

I=json.loads((ROOT/'results/step-1/physical-schema/inventory.json').read_text(encoding='utf8'))
T={t['name']:t for t in I['tables']}
F=I['foreign_keys']
BASE='code/plandora/reference/r348/java/com/pandora/'

# Ranges inspected during the documentary review. They are not executable tests.
anchors={
 'user_department':('UserDAO',550,580,'DepartmentTO mapping'),
 'user_area':('UserDAO',550,580,'AreaTO mapping'),
 'user_function':('UserDAO',550,583,'FunctionTO mapping'),
 'user_company':('UserDAO',550,590,'CompanyTO nullable mapping'),
 'customer_user':('CustomerDAO',111,120,'Equality join c.id=u.id'),
 'resource_customer':('ResourceDAO',373,379,'Inherited CustomerDAO population, composite project context'),
 'leader_resource':('LeaderDAO',198,203,'Inherited ResourceDAO population'),
 'customer_project':('CustomerDAO',427,436,'ProjectTO from project_id; identity scoped by (id, project_id) at 272-281'),
 'project_parent_project':('ProjectDAO',479,486,'Parent ProjectTO; self/null normalized to sentinel 0 by application'),
 'project_status_project':('ProjectDAO',474,477,'Current ProjectStatusTO mapping'),
 'proj_company':('ProjectDAO',488,492,'CompanyTO mapping; -1 treated as sentinel by application'),
 'req_parent':('RequirementDAO',250,252,'Equality join r.id=p.id'),
 'req_cat':('RequirementDAO',1138,1170,'CategoryTO from CATEGORY_ID'),
 'project_parent':('ProjectDAO',169,174,'Equality join project p / planning a, p.id=a.id'),
 'req_project':('RequirementDAO',1138,1170,'ProjectTO from project_id'),
 'req_status_req':('RequirementDAO',1138,1170,'RequirementStatusTO from REQUERIMENT_STATUS_ID'),
 'req_user':('RequirementDAO',1138,1170,'CustomerTO requester from USER_ID; DDL only guarantees tool_user, not project participation'),
 'req_hist_req':('RequirementHistoryDAO',150,163,'Requirement identifier retained'),
 'req_hist_status':('RequirementHistoryDAO',150,163,'RequirementStatusTO mapping'),
 'req_hist_user':('RequirementHistoryDAO',150,163,'UserTO handler mapping'),
 'prj_hist_prj':('ProjectHistoryDAO',29,35,'INSERT binds ProjectHistoryTO.projectId'),
 'prj_hist_status':('ProjectHistoryDAO',29,35,'INSERT binds status identifier'),
 'task_parent':('TaskDAO',405,409,'Equality join t.id=p.id'),
 'task_proj':('TaskDAO',1000,1024,'ProjectTO mapping'),
 'task_req':('TaskDAO',1000,1024,'Optional RequirementTO mapping'),
 'task_cat':('TaskDAO',1000,1024,'CategoryTO mapping'),
 'task_task':('TaskDAO',1000,1024,'Optional parent TaskTO mapping'),
 'task_res_task':('ResourceTaskDAO',1574,1597,'TaskTO from task_id'),
 'res_res_task':('ResourceTaskDAO',1574,1597,'ResourceTO receives resource_id and ProjectTO project_id'),
 'res_task_status_task':('ResourceTaskDAO',1574,1597,'TaskStatusTO mapping for assignment'),
 'approver_res_task_fk':('ResourceTaskDAO',1737,1746,'UPDATE binds approver object identifier; no proof all existing values resolve'),
 'task_hist_res_task':('TaskHistoryDAO',446,472,'ResourceTaskTO assembled with task/resource/project tuple'),
 'task_hist_status_task':('TaskHistoryDAO',446,456,'TaskStatusTO mapping'),
 'task_hist_user':('TaskHistoryDAO',446,456,'UserTO handler mapping'),
 'alloc_res_task':('ResourceTaskAllocDAO',55,64,'SELECT filters ordered task/resource/project tuple and binds assignment'),
 'alloc_plan_res_task':('ResourceTaskAllocPlannedDAO',27,35,'INSERT binds ordered task/resource/project tuple'),
 'plan_plan_relation':('PlanningRelationDAO',61,64,'Equality join pr.planning_id=p.id'),
 'res_cap_resource':('ResourceDAO',373,388,'Capacity lookup uses resource id and project id; DAO maps both at ResourceCapacityDAO:333-339'),
 'cat_project':('CategoryDAO',75,78,'LEFT OUTER JOIN c.project_id=p.id; optional project'),
}
assert set(anchors)=={f['constraint'] for f in F}, (set(anchors)-{f['constraint'] for f in F},{f['constraint'] for f in F}-set(anchors))
rows=[]
for f in F:
    file,a,b,note=anchors[f['constraint']]
    p=BASE+'dao/'+file+'.java'
    lines=(ROOT/p).read_text(encoding='latin1').splitlines()
    rows.append(dict(constraint=f['constraint'],result='documentary agreement in cited operation; not runtime validation',
        interpretation=note,query_or_mapping=evidence(p,a,b),excerpt='\n'.join(lines[a-1:b]),ddl=evidence(SOURCE,f['start_line'],f['end_line'])))
csvfile('results/step-1/foreign-keys/dao-review.csv',rows)
js('results/step-1/foreign-keys/dao-review.json',rows)

hypotheses=[]
for table,col,target in [('customer','function_id','function.id'),('requeriment_status','accept_project_id','project.id'),('requeriment_status','parent_id','requeriment_status.id')]:
    c=next(c for c in T[table]['columns'] if c['name']==col)
    hypotheses.append(dict(id='H'+str(len(hypotheses)+1),table=table,column=col,candidate_target=target,status='unverified; excluded from reconstructed edges',
        ddl=evidence(SOURCE,c['line']),reason='Column name/type suggest a relationship; no direct use establishing this column to target was found in the inspected Java tree'))
csvfile('results/step-1/logical-schema/hypotheses.csv',hypotheses)
js('results/step-1/foreign-keys/required-investigations.json',dict(
    customer_function=dict(ddl=hypotheses[0],observed='CustomerDAO selects u.function_id, not c.function_id. CustomerFunctionDAO uses a separate out-of-scope association table.',
        evidence=[evidence(BASE+'dao/CustomerDAO.java',111,118),evidence(BASE+'dao/CustomerDAO.java',335,338),evidence(BASE+'dao/CustomerFunctionDAO.java',69,71)]),
    status_project=dict(ddl=hypotheses[1],exact_java_hits=source_hits('accept_project_id'),observed='No exact Java occurrence. RequirementStatusDAO SELECT and mapping omit this column.',evidence=[evidence(BASE+'dao/RequirementStatusDAO.java',26,27),evidence(BASE+'dao/RequirementStatusDAO.java',113,119)]),
    status_parent=dict(ddl=hypotheses[2],observed='Generic parent_id occurrences exist elsewhere, but RequirementStatusDAO SELECT/mapping omit this column; no target established.',
        evidence=[evidence(BASE+'dao/RequirementStatusDAO.java',26,27),evidence(BASE+'dao/RequirementStatusDAO.java',113,119)])))

metric=json.loads((ROOT/'results/step-1/metrics/ddl-checks.json').read_text())
hsql=json.loads((ROOT/'results/step-1/physical-schema/hsql-comparison.json').read_text())
def loc(e):return f"`{e['path']}:{e['start_line']}-{e['end_line']}`"
lines=[
 '# J1 - Inventaire et reconstruction documentaire',
 '',f'Référence : **SVN r348** ; snapshot Git `{REV}`. Sources conservées dans `code/plandora/reference/r348/`. Tous les constats ci-dessous sont documentaires, sans base active. Extraction complémentaire distincte de SQLInspect.',
 '', '## Résultats observés', '',
 f"- {metric['tables']} tables, {metric['columns']} colonnes, {metric['internal_fks']} FK internes déclarées, {metric['boundary_fks']} FK de frontière. Les quatre repères du prompt sont retrouvés, sans ajustement des données.",
 '- Aucun CREATE INDEX explicite dans les 24 tables ; les PK sont des contraintes déclarées, pas des index supplémentaires inventoriés. Aucun index de support implicite des FK n’est présenté comme déclaré.',
 '- Les 39 FK internes passent les contrôles documentaires : colonnes présentes, arité, appariement ordonné, types exacts normalisés et clé cible candidate. Les mêmes contrôles sont faits sur les 30 FK de frontière ; une anomalie est signalée ci-dessous.',
 '- Nullabilité : conserver séparément NULL explicite, NOT NULL explicite et absence de clause. La PK implique une obligation selon la sémantique MySQL ; cette implication ne réécrit pas le texte du DDL. DEFAULT absent reste absent. Aucun comportement TIMESTAMP dépendant d’une version du serveur n’est affirmé.',
 '- Les contraintes composites restent des tuples ordonnés. Le minimum inverse est toujours 0 ; son maximum est 1 seulement si le tuple source contient une clé candidate entière. Sinon, maximum N. Le minimum vers la cible est 1 si toutes les colonnes sont requises, sinon 0, sous réserve d’une clé cible candidate.',
 '', '## Trois niveaux et lecture', '',
 '1. **Physical schema** : 24 tables et 175 déclarations de colonnes, types, nullabilité, defaults, PK et 39 FK déclarées exclusivement. `inventory.json` est l’inventaire détaillé ; `scope-mysql.sql` est un sous-ensemble sans DROP ni INSERT pour extraction dans un outil de modélisation.',
 '2. **Logical schema** : relations et identifiants conservés ; optionalités déduites du DDL ; les deux références récupérées I1/I2 sont orange et ne deviennent pas des FK déclarées. Les hypothèses H1-H3 sont exclues des liens.',
 '3. **Conceptual schema candidate** : objets métier, associations nommées, entités associatives et événements historiques. Les identifiants composites restent visibles pour assurer la traçabilité. `table-mapping.csv` relie exactement les 24 tables aux abstractions métier ; `associations.json` porte les cardinalités et preuves.',
 '',
 'Les SVG et PDF utilisent des cartes et un registre F1-F39 pour conserver une lecture non ambiguë sans croisements. Une ligne de registre est une référence complète, avec tuple source, tuple cible, deux multiplicités et ligne DDL. Le fichier draw.io contient trois pages éditables et les connecteurs attachés aux cartes, masqués initialement ; leurs libellés restent visibles dans le registre. Dans diagrams.net, éditer le XML ou rendre les cellules d’arêtes visibles pour afficher ces connecteurs. Aucun import externe n’a été exécuté.',
 '', '## Liens récupérés et hypothèses', '',
 '- **I1 observé dans une requête** : `plan_relation.plan_related_id -> planning.id`, jointure explicite dans `java/com/pandora/dao/PlanningRelationDAO.java:41-44` (préfixe de référence ci-dessus). La FK correspondante est absente du DDL. Le DAO insère les deux identifiants aux lignes 164-167. Une jointure ne démontre pas l’absence d’orphelins.',
 '- **I2 inféré du mapping objet** : `task.created_by -> tool_user.id`, `TaskDAO.java:1001-1011` construit un UserTO ; `UserDAO.java:92-98` associe son identifiant à tool_user. C’est une référence applicative étayée, sans contrainte déclarée ni preuve de validité des valeurs.',
 '- **H1** `customer.function_id -> function.id` : non validé. `CustomerDAO.java:111-118` lit `u.function_id`, pas `c.function_id` ; son INSERT aux lignes 335-338 omet cette colonne. `CustomerFunctionDAO.java:69-71` utilise une autre table, `customer_function`, hors périmètre. Ne pas confondre ces trois emplacements.',
 '- **H2/H3** `requeriment_status.accept_project_id` et `.parent_id` : candidats uniquement. Aucun usage Java exact de accept_project_id trouvé ; les SELECT et le mapping de `RequirementStatusDAO.java:26-27,113-119` omettent ces deux colonnes. Les autres parent_id ne prouvent rien pour cette table. Recherche complète et extraits dans les JSON dédiés ; une absence dans cette recherche ne prouve pas l’inutilité dans toutes les configurations.',
 '', '## Interprétation conceptuelle et cardinalités', '',
 '- **Project participation** est l’appellation candidate de `customer` : sa clé `(id, project_id)` associe un utilisateur à un projet, les FK customer_user/customer_project l’étayent. `CustomerDAO.java:214-217,272-281` joint tool_user et filtre le projet ; `CustomerTO.java:8` étend UserTO. Il ne s’agit donc pas nécessairement d’une organisation cliente.',
 '- `resource` et `leader` sont des spécialisations candidates de cette participation, sur la même clé composite. `ResourceTO.java:10`, `LeaderTO.java:7`, `ResourceDAO.java:373-379` et `LeaderDAO.java:198-203` confortent le rôle métier. Les FK sur PK prouvent une inclusion d’identifiants et une occurrence parent unique par sous-type. Elles ne prouvent aucune totalité ni disjonction. `root` est un sous-type supplémentaire hors périmètre.',
 '- `project`, `requeriment` (Request) et `task` spécialisent potentiellement **Planning item** : FK id->planning.id et jointures ProjectDAO:169-174, RequirementDAO:250-252, TaskDAO:405-409. Le parent peut ne relever d’aucun de ces trois sous-types, ou d’autres types hors périmètre. Totalité/disjonction restent inconnues.',
 '- **Assignment** (`resource_task`) relie une Task à une Resource participation identifiée par `(resource_id, project_id)` ; son statut est un Assignment status, même si le nom SQL est task_status. Le DDL seul n’impose pas que resource_task.project_id égale task.project_id : ne pas ajouter cette contrainte métier.',
 '- **Actual allocation** et **Planned allocation** dépendent du tuple d’affectation, puis de sequence. `ResourceTaskAllocDAO.java:55-64` et `ResourceTaskAllocPlannedDAO.java:27-35` le confirment. Les unités de alloc_time et la signification temporelle exacte de sequence ne sont pas inventées.',
 '- **Capacity slot** est identifié par participation ressource et (cap_year,cap_month,cap_day). Le DAO utilise resource_id et project_id ensemble (`ResourceDAO.java:387`, `ResourceCapacityDAO.java:333-339`). Aucune validité calendaire ni unité de capacité n’a été testée.',
 '- **Transitions / histories** : project_history, requeriment_history, task_history sont des événements identifiés par leur propriétaire, statut et creation_date ; task_history vise une affectation complète. DDL et DAO ne prouvent ni une chaîne de transitions complète ni l’unicité d’un ordre métier au-delà des PK.',
 '- `requeriment.user_id` vise tool_user dans le DDL, bien que le DAO le transforme en CustomerTO (RequirementDAO:1138-1170). La participation du demandeur au projet n’est donc pas une contrainte déclarée. ProjectDAO:479-491 normalise aussi des sentinelles parent/company ; aucune donnée réelle n’a été inspectée.',
 '', '## Dépendances externes et anomalie', '',
 '`external_dependencies.csv` conserve les 30 FK entrantes/sortantes avec ordre, noms et lignes. Les objets extérieurs ne sont pas ajoutés aux 24 tables. Notamment root->leader et les extensions déclarées de planning (artifact, invoice, occurrence, cost) restent des frontières. PlanningDAO.java:177-229 traite aussi RiskTO et ExpenseTO : ces usages applicatifs ne prouvent pas une FK id->planning déclarée pour risk ou expense.',
 '',
 '**Anomalie observée** : `mysql.sql:1153`, cus_func_customer référence `customer(id)` seul alors que la PK est `(id, project_id)`. La cible n’est pas une clé candidate déclarée. Les colonnes et types correspondent, mais on ne peut pas conclure à une cible unique. Certains comportements MySQL historiques concernant les index non uniques dépendent de version/engine et ne sont pas validés ici. Ce n’est pas une erreur du parseur ni une réparation autorisée du DDL.',
 '', '## Cohérence avec les DAO', '',
 'Les 39 contraintes internes ont une ligne dans `dao-review.csv` et `dao-review.json`, avec plage de requête ou de mapping, extrait et conclusion. « Accord documentaire » décrit la cohérence de l’opération citée ; cela ne signifie ni exécution réussie, ni audit exhaustif des chemins de code, ni validation de contenu.',
 '', '## HSQL distinct', '',
 ('Comparaison colonne par colonne produite dans `hsql-comparison.json` ; '+str(len(hsql.get('differences',[])))+' écarts de type/nullabilité/default explicites sont conservés avec les deux sources. Aucun mélange des dialectes.' if 'differences' in hsql else 'Comparaison limitée : '+hsql.get('detail','voir hsql-comparison.json')),
 '', '## Outils et limites réelles', '',
 '- Python 3.12 et reportlab/PyMuPDF déjà présents ; génération, parsing XML/CSV/JSON et contrôles structurels reproductibles. Les images PNG de toutes les pages PDF sont conservées pour revue.',
 '- DB-MAIN 11.0.2 détecté, exécutable démarré ; sonde native JIDBM : version=11.0.2, platform=WIN64. Premier essai échoué (DLL dépendantes introuvables), second réussi avec le bin DB-MAIN ajouté au PATH du seul processus. Aucun projet chargé, créé ou importé par JIDBM. Voir `method/design/DB-MAIN-J1.md`.',
 '- Aucun accès Lucidchart disponible : compatibilité d’import non vérifiée, aucun import annoncé. Le .drawio est du XML mxGraph éditable, sans garantie d’acceptation par Lucidchart.',
 '- MySQL, HSQLDB, SQLInspect, tests de données : non exécutés. Aucun contenu de base ni résultat de compilation Plandora n’est affirmé. Aucun export SQLInspect n’a été créé ou modifié.',
 '', '## Reproduction', '',
 '```powershell', 'python code/tools/schema/j1.py', 'python code/tools/schema/j1_document.py', 'python code/tools/validation/j1_validate.py', 'python code/tools/validation/reference_manifest.py', 'git diff --exit-code -- code/plandora/reference/', '```',
 '', '## Descriptions ClickUp prêtes à copier', '',
 '**J1.1 FR** — Inventaire documentaire du DDL MySQL r348 : 24 tables, 175 colonnes, 39 FK internes et 30 FK de frontière ; déclarations et lignes sources conservées. **EN** — Inventory r348 MySQL DDL with exact declarations, ordered keys and source evidence; external dependencies kept separate.',
 '', '**J1.2 FR** — Schémas physique, logique reconstruit et conceptuel candidat, exports draw.io/SVG/PDF, traçabilité des 24 tables et distinction des références récupérées. **EN** — Produce physical, reconstructed logical and candidate conceptual models with editable exports and traceable mappings.',
 '', '**J1.3 FR** — Vérification documentaire des FK et des DAO ; signalement de cus_func_customer, références applicatives et hypothèses non validées ; import DB-MAIN à effectuer selon le guide. **EN** — Validate FK structure and cited DAO operations; report the boundary key anomaly and unresolved hypotheses. DB-MAIN import remains a manual verification step.',
 ]
write('results/step-1/J1-report.md','\n'.join(lines)+'\n')
write('method/design/J1-method.md',f'''# Méthode J1

Snapshot `{REV}`, référence SVN r348. Les générateurs `code/tools/schema/j1.py` et `j1_document.py` lisent uniquement la référence. Le premier parse les CREATE TABLE, PK/UNIQUE, CREATE INDEX et ALTER TABLE ADD CONSTRAINT FOREIGN KEY du dialecte présent ; il ne constitue pas un parseur SQL universel. Les tests indépendants du validateur comparent les totaux et tuples au texte source.

1. Inventorier chaque colonne et les contraintes ordonnées avec lignes sources, absence de clause distincte d'une propriété implicite.
2. Construire le niveau physique depuis le DDL exclusivement ; conserver les dépendances extérieures dans un registre.
3. Reconstruire les références logiques : F = déclaré, I = récupéré dans Java, H = hypothèse exclue du modèle.
4. Nommer les abstractions conceptuelles ; garder les entités associatives et événements quand leurs attributs/identifiants portent une signification. Ne déduire ni totalité ni disjonction d'une inclusion de PK.
5. Valider toutes les FK sélectionnées, comparer les mappings DAO cités, générer CSV/JSON/draw.io/SVG/PDF, rouvrir les formats et inspecter les rendus.

La revue DAO reste locale aux opérations citées ; aucune assertion de données ou de couverture complète du programme. Les exports complémentaires J1 ne sont pas des exports SQLInspect. DB-MAIN et Lucidchart ne sont pas des prérequis à cette extraction ; leurs limites et étapes manuelles sont documentées.
''')
print('Documentary report and 39 DAO review rows generated.')
