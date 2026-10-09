# CLAUDE.md

Ce fichier guide Claude Code (claude.ai/code) lorsqu'il travaille sur ce dépôt.

## Le projet

Tutoriel progressif d'**Apache Velocity 1.7** (Java 8, Maven, JUnit 5, AssertJ, Cucumber 7).
Le cours est dans `TUTORIEL.md` (9 leçons) ; chaque leçon correspond à une classe de test
`src/test/java/fr/tuto/velocity/leconNN_*/LeconNN*Test.java` dont chaque méthode énonce une
règle du langage VTL en français.

## Commandes

```
mvn test                                    # tous les tests (JUnit + Cucumber)
mvn test -Dtest=Lecon03SetEtConditionsTest  # une seule leçon
mvn test -Dtest=Lecon03SetEtConditionsTest#nom_de_methode  # un seul test
mvn test -Dtest=LancerCucumberTest          # uniquement les scénarios Cucumber
```

## Contraintes techniques

- **Java 8 strict** : sur un JDK 9+, le profil `jdk9plus` active `--release 8`. Ne pas
  utiliser d'API postérieure à Java 8 (`var`, `List.of`, `String.isBlank`, records…).
  Le code existant écrit par exemple `new HashMap<String, Object>()` explicitement.
- **Velocity 1.7 uniquement** (`org.apache.velocity:velocity`), pas la 2.x
  (`velocity-engine-core`) dont de nombreux comportements diffèrent.
- `oro:oro:2.0.8` est déclaré explicitement : dépendance optionnelle de Velocity 1.7, requise
  par `EscapeHtmlReference` (sinon `NoClassDefFoundError` à l'initialisation du moteur).
- Les classes exposées aux modèles doivent être **`public`** et utiliser des **getters
  classiques** (`getNom()`) : l'introspection de Velocity 1.7 ne trouve pas les accesseurs
  « fluides » (`nom()`) via `$objet.nom`.
- Encodage UTF-8 partout (sources, modèles `.vm`).

## Architecture

```
src/main/java/fr/tuto/velocity
├── moteur/          FabriqueMoteur (configuration par propriétés), Rendu (evaluer / fusionner)
├── domaine/         Patient, Sejour, Traitement, Sexe
├── outils/          OutilDates ($dates), OutilTexte ($texte) — outils de présentation
└── courrier/        leçon 9, architecture hexagonale :
    ├── domaine/         MoteurDeModeles (port), CourrierImpossibleException
    ├── application/     GenerateurCourrierDeSortie (cas d'usage, sans dépendance à Velocity)
    └── infrastructure/  MoteurDeModelesVelocity (adaptateur : classpath, macros, mode strict)
src/main/resources/modeles   courrier-sortie.vm, entete.vm, macros-courrier.vm, mentions.txt
src/test/resources/lecons    petits modèles des leçons 5 et 6
src/test/resources/features  scénarios Cucumber en français (# language: fr)
```

- Toute création de `VelocityEngine` passe par `FabriqueMoteur` ; les propriétés y sont
  écrites en toutes lettres (pas les constantes `RuntimeConstants`) pour correspondre à la
  documentation. Les logs sont coupés par `NullLogChute`.
- `Rendu.evaluer` (modèle en chaîne) et `Rendu.fusionner` (fichier chargé) laissent remonter
  les `VelocityException` telles quelles.
- Le cas d'usage ne doit jamais importer Velocity ; il est testé avec un espion du port
  (`Lecon09CasDUsageTest`). L'adaptateur est testé avec les vrais modèles.
- L'adaptateur active `runtime.references.strict=true` : toute référence inconnue dans un
  modèle provoque une erreur.

## Tests Cucumber

- `LancerCucumberTest` est une suite JUnit 5 (`@Suite`, `@IncludeEngines("cucumber")`) qui
  exécute `src/test/resources/features` avec la glue `fr.tuto.velocity.cucumber`.
- Dans `regles_vtl.feature`, les valeurs sont délimitées par des guillemets français `« »` et
  les étapes utilisent des **expressions régulières** (le VTL contient `( ) { }`, spéciaux pour
  les Cucumber Expressions).
- Les classes d'étapes sont instanciées à chaque scénario ; le moteur Velocity, coûteux, est
  partagé en `static`.

## Conventions

- Tout est en **français** : noms de classes, méthodes, variables, commentaires, Javadoc,
  scénarios Gherkin.
- Méthodes de test en `snake_case` décrivant une règle
  (ex. `une_reference_est_remplacee_par_la_valeur_du_contexte`), assertions AssertJ.
- Commentaires pédagogiques : expliquer le *pourquoi* d'un comportement de Velocity.
- Toute évolution d'une leçon doit rester cohérente avec `TUTORIEL.md` (et inversement).
