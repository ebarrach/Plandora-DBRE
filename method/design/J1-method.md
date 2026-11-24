# Méthode J1

Snapshot `12c9a7b8355ef311fd37b9afa30e18fabe652979`, référence SVN r348. Les générateurs `code/tools/schema/j1.py` et `j1_document.py` lisent uniquement la référence. Le premier parse les CREATE TABLE, PK/UNIQUE, CREATE INDEX et ALTER TABLE ADD CONSTRAINT FOREIGN KEY du dialecte présent ; il ne constitue pas un parseur SQL universel. Les tests indépendants du validateur comparent les totaux et tuples au texte source.

1. Inventorier chaque colonne et les contraintes ordonnées avec lignes sources, absence de clause distincte d'une propriété implicite.
2. Construire le niveau physique depuis le DDL exclusivement ; conserver les dépendances extérieures dans un registre.
3. Reconstruire les références logiques : F = déclaré, I = récupéré dans Java, H = hypothèse exclue du modèle.
4. Nommer les abstractions conceptuelles ; garder les entités associatives et événements quand leurs attributs/identifiants portent une signification. Ne déduire ni totalité ni disjonction d'une inclusion de PK.
5. Valider toutes les FK sélectionnées, comparer les mappings DAO cités, générer CSV/JSON/draw.io/SVG/PDF, rouvrir les formats et inspecter les rendus.

La revue DAO reste locale aux opérations citées ; aucune assertion de données ou de couverture complète du programme. Les exports complémentaires J1 ne sont pas des exports SQLInspect. DB-MAIN et Lucidchart ne sont pas des prérequis à cette extraction ; leurs limites et étapes manuelles sont documentées.
