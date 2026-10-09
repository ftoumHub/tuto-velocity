# Apache Velocity 1.7 — tutoriel progressif

Ce tutoriel accompagne le projet Maven. Chaque leçon correspond à une classe de test
dans `src/test/java/fr/tuto/velocity/leconNN_*` : lire la leçon, puis lire (et casser !)
les tests. Chaque méthode de test est une règle du langage énoncée en français.

```
mvn test                                   # tout
mvn test -Dtest=Lecon03SetEtConditionsTest # une leçon
mvn test -Dtest=LancerCucumberTest         # les scénarios Cucumber
```

| Leçon | Thème | Classe de test |
|------:|-------|----------------|
| 1 | Moteur, contexte, références | `Lecon01PremierRenduTest` |
| 2 | Propriétés, méthodes, Map, List | `Lecon02ReferencesTest` |
| 3 | `#set`, expressions, `#if` | `Lecon03SetEtConditionsTest` |
| 4 | `#foreach` et `$foreach` | `Lecon04BouclesTest` |
| 5 | Modèles dans des fichiers | `Lecon05ChargementDeModelesTest` |
| 6 | `#parse`, `#include`, macros, `#define` | `Lecon06ReutilisationTest` |
| 7 | Outils, échappement, événements | `Lecon07OutilsEtEchappementTest` |
| 8 | Erreurs et mode strict | `Lecon08ErreursEtModeStrictTest` |
| 9 | Cas complet en architecture hexagonale | `Lecon09*Test` + Cucumber |

---

## Leçon 0 — Le projet

* **Java 8** : `maven.compiler.source/target = 1.8`. Sur un JDK 9+ le profil `jdk9plus`
  passe `--release 8`, ce qui interdit aussi l'usage d'API postérieures à Java 8.
* **Velocity 1.7** (`org.apache.velocity:velocity:1.7`) : dernière version de la
  branche 1.x (2010). Elle tire `commons-collections 3.2.1` et `commons-lang 2.4`.
  Velocity 2.x a changé d'artifactId (`velocity-engine-core`) et de nombreux
  comportements : ce tutoriel décrit **uniquement la 1.7**.
* **JUnit 5 + AssertJ** pour les tests unitaires, **Cucumber 7** exécuté par la
  plateforme JUnit 5 (`cucumber-junit-platform-engine` + `junit-platform-suite`).

```
src/main/java/fr/tuto/velocity
├── moteur/          FabriqueMoteur (configuration), Rendu (evaluate / merge)
├── domaine/         Patient, Sejour, Traitement, Sexe
├── outils/          OutilDates ($dates), OutilTexte ($texte)
└── courrier/        leçon 9 : domaine (port) / application (cas d'usage) / infrastructure (Velocity)
src/main/resources/modeles   courrier-sortie.vm, entete.vm, macros-courrier.vm, mentions.txt
src/test/resources/lecons    petits modèles des leçons 5 et 6
src/test/resources/features  scénarios Cucumber en français
```

---

## Leçon 1 — Moteur, contexte, modèle

Velocity assemble trois choses :

1. un **moteur** (`VelocityEngine`) configuré par des propriétés puis initialisé **une
   fois** — il est thread-safe ensuite ;
2. un **contexte** (`VelocityContext`) : des variables nommées, de n'importe quel type ;
3. un **modèle** : du texte avec des *références* (`$x`) et des *directives* (`#if`).

```java
VelocityEngine moteur = new VelocityEngine();
moteur.init(proprietes);

VelocityContext contexte = new VelocityContext();
contexte.put("prenom", "Marie");

StringWriter sortie = new StringWriter();
moteur.evaluate(contexte, sortie, "logTag", "Bonjour $prenom !");
```

`FabriqueMoteur` centralise la configuration ; `Rendu.evaluer(...)` encapsule
`evaluate`. On y coupe les logs (`NullLogChute`) pour garder des tests lisibles — en
production, branchez plutôt `Log4JLogChute` ou `CommonsLogLogChute`.

**Références**

| Écriture | Sens |
|---|---|
| `$prenom` | notation abrégée |
| `${prenom}ette` | notation formelle : délimite le nom |
| `$!prenom` / `$!{prenom}` | référence *silencieuse* : rien si la valeur est absente |
| `## ...` / `#* ... *#` | commentaire de ligne / de bloc |

> **À retenir.** Une référence inconnue (ou qui vaut `null`) est **recopiée telle
> quelle** : `Bonjour $inconnu` produit `Bonjour $inconnu`, sans erreur. C'est
> pratique… et dangereux dans un document officiel. La leçon 8 montre comment
> l'interdire.

Un contexte peut aussi être construit depuis une `Map`, ou **envelopper** un autre
contexte (`new VelocityContext(commun)`) pour partager des valeurs communes.

---

## Leçon 2 — Naviguer dans les objets

`$patient.nom` est une **propriété**. Velocity 1.7 essaie, dans l'ordre :

1. `getNom()`
2. `getnom()`
3. `get("nom")` — c'est ce qui rend `$map.cle` possible
4. `isNom()` — pour les booléens : `$patient.feminin` → `isFeminin()`

Toute méthode publique reste appelable explicitement, avec arguments :
`$patient.nom.substring(0, 3)`, `$patient.prenom.length()`.

Nouveauté 1.7, la **notation indexée** : `$services[1]`, `$codes["CARDIO"]`.
Les tableaux Java sont enveloppés et exposent `size()` et `get(i)`.

> **Pièges d'introspection**
> * La classe doit être **publique** : les méthodes d'une classe privée, anonyme ou d'un
>   lambda sont invisibles.
> * Des accesseurs *fluides* (`nom()`, style record ou Lombok `fluent=true`) ne sont
>   **pas** trouvés par `$patient.nom` ; il faut écrire `$patient.nom()`.
>   C'est pourquoi le domaine du projet utilise des getters classiques.
> * Une propriété qui vaut `null` se comporte comme une référence inconnue :
>   `Motif : $sejour.motif` → `Motif : $sejour.motif`. Utilisez `$!sejour.motif`.

---

## Leçon 3 — `#set`, expressions et conditions

```velocity
#set($service = "Cardiologie")
#set($message = "Bonjour $prenom")   ## guillemets doubles : interpolé
#set($brut    = 'Bonjour $prenom')   ## guillemets simples : littéral
#set($x = (2 + 3) * 4)
```

`#set` écrit **dans le contexte** : la variable est visible ensuite depuis Java.

**Conditions**

```velocity
#if($age < 18)pédiatrie#elseif($age >= 75)gériatrie#{else}adulte#end
```

| Symbole | Mot | | Symbole | Mot |
|---|---|---|---|---|
| `==` | `eq` | | `&&` | `and` |
| `!=` | `ne` | | `\|\|` | `or` |
| `>` `>=` | `gt` `ge` | | `!` | `not` |
| `<` `<=` | `lt` `le` | | | |

**Vérité en 1.7** : est *faux* uniquement `null` (ou référence absente) et
`Boolean.FALSE`. Tout le reste est vrai.

> **Pièges**
> * `#if($vide)` est **vrai** pour une chaîne vide ou une liste vide.
>   Testez `$liste.isEmpty()` ou `$chaine.length() > 0`.
> * `7 / 2` vaut `3` : l'arithmétique entière reste entière.
> * `#set($x = $valeurNull)` **ne modifie pas** `$x` (propriété
>   `directive.set.null.allowed=false` par défaut). Dans une boucle, la valeur de
>   l'itération précédente « fuit ». Réinitialisez explicitement (`#set($x = "")`).
> * `#elseMonsieur` est lu comme une directive inconnue : écrivez `#{else}Monsieur`.
>   La notation formelle existe pour toutes les directives (`#{if}`, `#{end}`...).

---

## Leçon 4 — Boucles

```velocity
#foreach($s in $services)$s#if($foreach.hasNext), #end#end
```

La variable `$foreach` (nouveauté 1.7) expose `count` (à partir de 1), `index`
(à partir de 0) et `hasNext`. L'ancien `$velocityCount` fonctionne encore mais est
déprécié. On l'active explicitement avec `foreach.provide.scope.control=true`.

* Intervalles : `[1..5]`, `[3..1]`.
* Sortie anticipée : `#break($foreach)`.
* Map : `#foreach($cle in $map.keySet())$cle=$map.get($cle)#end`.
* Pas de `#else` sur `#foreach` en 1.7 : gérez le cas vide avec `#if($l.isEmpty())`.

**Espaces et sauts de ligne.** Le saut de ligne qui suit immédiatement la parenthèse
fermante d'une directive (ou `#end`, `#else`) est absorbé. En revanche,
**l'indentation placée devant une directive est recopiée**. D'où la règle de style
adoptée dans les modèles du projet : directives en colonne 0, contenu indenté si besoin.

```velocity
#foreach($t in $traitements)
  $foreach.count. $t.medicament – $t.posologie
#end
```

---

## Leçon 5 — Modèles dans des fichiers

```java
Properties p = new Properties();
p.setProperty("resource.loader", "classpath");
p.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
p.setProperty("classpath.resource.loader.cache", "true");
p.setProperty("input.encoding", "UTF-8");

Template modele = moteur.getTemplate("lecons/bienvenue.vm", "UTF-8");
modele.merge(contexte, writer);
```

* `getTemplate` **analyse** le fichier ; le `Template` obtenu est réutilisable avec
  autant de contextes que l'on veut (et mis en cache par le moteur).
* Sans configuration, Velocity 1.7 utilise le `FileResourceLoader` avec le chemin `.`
  (répertoire courant). Pour des modèles modifiables sans redéploiement, pointez
  `file.resource.loader.path` vers un répertoire et désactivez le cache (ou réglez
  `file.resource.loader.modificationCheckInterval`).
* Modèle absent → `ResourceNotFoundException`.

> **Piège d'encodage.** Précisez toujours l'encodage (`input.encoding` et/ou le
> second argument de `getTemplate`). Un fichier UTF-8 lu en ISO-8859-1 transforme
> `hôpital` en `hÃ´pital` — le test le démontre.

---

## Leçon 6 — Réutiliser

| Directive | Effet |
|---|---|
| `#parse("x.vm")` | insère **et interprète** un autre modèle, avec le même contexte |
| `#include("x.txt")` | insère le fichier **brut**, sans l'interpréter |
| `#macro(nom $a $b)...#end` | définit une macro, appelée par `#nom("x" "y")` |
| `#@nom()...#end` | macro de **bloc** (1.7) : le contenu arrive dans `$bodyContent` |
| `#define($bloc)...#end` | nomme un bloc, **réévalué** à chaque affichage de `$bloc` |

**Bibliothèque de macros** : `velocimacro.library = modeles/macros-courrier.vm`
charge les macros au démarrage, disponibles dans tous les modèles.

> **Pièges**
> * Écrivez une macro sur **une seule ligne** si elle sert au milieu d'une phrase :
>   sinon chaque appel injecte un saut de ligne.
> * Les arguments sont séparés par des **espaces**.
> * En 1.x, le tiret est autorisé dans un identifiant : `$nom-Martin` cherche la
>   variable `nom-Martin`. Écrivez `${nom}-Martin`.
> * Les arguments de macro sont passés *par nom* (ré-évalués à chaque usage) :
>   évitez d'y passer un appel de méthode coûteux ou à effet de bord.

---

## Leçon 7 — Outils, échappement, événements

**Outils.** Velocity 1.7 ne connaît pas `java.time` : `$entree` affiche `2026-03-02`.
Plutôt que de coder du formatage dans le modèle, on dépose des objets Java dédiés :

```java
contexte.put("dates", new OutilDates());   // $dates.court($d), $dates.enLettres($d)
contexte.put("texte", new OutilTexte());   // $texte.majuscules($s), $texte.parDefaut($v, "—")
```

La logique reste en Java, testable unitairement ; le modèle se contente d'appeler.
(Le projet Velocity Tools propose `DateTool`, `NumberTool`, `EscapeTool`… mais basés
sur `java.util.Date`.)

**Échappement**

| Écriture | Résultat |
|---|---|
| `\$prenom` (défini) | `$prenom` |
| `\$inconnu` (non défini) | `\$inconnu` — l'antislash reste ! |
| `Prix : 12 $ - langage C# et Java` | inchangés (pas d'identifiant après `$` ou `#`) |
| `C#` en toute fin de modèle | `ParseErrorException` — piège du lexer 1.7 |
| `#[[ ... ]]#` | bloc recopié sans aucune interprétation |

**Événements.** Un `ReferenceInsertionEventHandler` intercepte chaque valeur avant
écriture :

* global, par configuration :
  `eventhandler.referenceinsertion.class = org.apache.velocity.app.event.implement.EscapeHtmlReference`
  — indispensable pour du HTML (XSS). Attention : il s'appuie sur commons-lang 2 et
  convertit aussi les accents en entités (`Hélène` → `H&eacute;l&egrave;ne`). Il exige
  aussi Jakarta ORO (`oro:oro:2.0.8`), dépendance *optionnelle* de Velocity 1.7 qu'il
  faut déclarer soi-même dans le `pom.xml` ;
* par rendu, via un `EventCartridge` attaché au contexte — exemple `MasquageNir`, qui
  masque toute référence dont le nom contient « nir ».

---

## Leçon 8 — Erreurs et mode strict

| Exception (toutes filles de `VelocityException`, non vérifiées) | Cause |
|---|---|
| `ParseErrorException` | syntaxe invalide (directive non fermée…) |
| `ResourceNotFoundException` | modèle introuvable |
| `MethodInvocationException` | une méthode Java appelée par le modèle a levé une exception (`getWrappedThrowable()`) |

Par défaut, une référence ou une méthode inconnue est silencieusement recopiée.
**Le mode strict** (`runtime.references.strict=true`) en fait une erreur :

* `$inconnu`, `$prenom.methodeInexistante()` → exception ;
* exception documentée : `#if($inconnu)` (référence simple, sans propriété) reste
  permis et vaut faux.

> **Recommandation.** Activez le mode strict pour tout document à valeur légale ou
> médicale : mieux vaut un rendu qui échoue qu'un courrier contenant
> « `$patient.nom` ». C'est le choix fait dans l'adaptateur de la leçon 9.

---

## Leçon 9 — Un cas complet : le courrier de sortie

```
courrier.domaine          MoteurDeModeles (port)  ·  CourrierImpossibleException
courrier.application      GenerateurCourrierDeSortie (cas d'usage, sans Velocity)
courrier.infrastructure   MoteurDeModelesVelocity (adaptateur)
```

* Le **cas d'usage** valide la règle métier (séjour clôturé), prépare les données et
  choisit le modèle. Il est testé avec un **espion** du port
  (`Lecon09CasDUsageTest`) : aucun Velocity dans ce test.
* L'**adaptateur** configure le moteur une fois (classpath, bibliothèque de macros,
  mode strict), ajoute les outils de présentation (`$dates`, `$texte`) et rend
  `modeles/<nom>.vm`. Il est testé avec le vrai modèle
  (`Lecon09AdaptateurVelocityTest`).
* Le modèle `courrier-sortie.vm` réunit tout le tutoriel : commentaires, `#parse` de
  l'en-tête, macros `#civilite` et `#accord` (accords en genre), outils de date,
  `#if` sur une donnée facultative, `#foreach` numéroté, `#include` de mentions
  non interprétées.

### Les scénarios Cucumber

`LancerCucumberTest` est une suite JUnit 5 qui délègue au moteur Cucumber les fichiers
de `src/test/resources/features` (écrits en français grâce à `# language: fr`).

* `regles_vtl.feature` : un **plan de scénario** où chaque ligne d'exemples est une
  règle du langage (variable → modèle → résultat). Les valeurs sont délimitées par
  des guillemets français `« »` et les étapes utilisent des expressions régulières,
  car le VTL contient `( ) { }` — caractères spéciaux des *Cucumber Expressions*.
* `courrier_de_sortie.feature` : la spécification métier du courrier, lisible par un
  médecin ou un fonctionnel (civilité, accords, motif facultatif, traitements
  numérotés, refus d'un séjour en cours).

Cucumber crée une instance neuve de chaque classe d'étapes par scénario : les champs
de `CourrierDeSortieSteps` constituent l'état isolé d'un scénario. Le moteur Velocity,
coûteux à construire, est partagé en `static`.

---

## Aide-mémoire des propriétés utilisées

```properties
runtime.log.logsystem.class      = org.apache.velocity.runtime.log.NullLogChute
input.encoding                   = UTF-8
output.encoding                  = UTF-8
foreach.provide.scope.control    = true
resource.loader                  = classpath
classpath.resource.loader.class  = org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader
classpath.resource.loader.cache  = true
velocimacro.library              = modeles/macros-courrier.vm
runtime.references.strict        = true
eventhandler.referenceinsertion.class = org.apache.velocity.app.event.implement.EscapeHtmlReference
```

## Pour aller plus loin

1. Ajouter un modèle d'**ordonnance de sortie** qui réutilise l'en-tête et les macros.
2. Écrire un `ReferenceInsertionEventHandler` qui remplace toute valeur vide par
   « non renseigné » et le brancher dans l'adaptateur.
3. Passer `velocimacro.permissions.allow.inline.local.scope=true` et observer, par un
   test, qu'une macro définie dans un modèle n'est plus visible depuis un autre.
4. Préparer une migration vers Velocity 2.x : lister, test par test, ceux qui
   changeraient de comportement (vérité des chaînes vides, tirets dans les
   identifiants, `#set` à null, `$foreach`…).
