# language: fr
Fonctionnalité: Règles de base du langage VTL (Velocity Template Language)
  Chaque exemple est une petite spécification exécutable :
  une variable, un modèle, et le texte attendu.
  Les guillemets français « » délimitent les valeurs pour éviter tout conflit
  avec les guillemets droits utilisés dans le VTL.

  Plan du Scénario: <règle>
    Soit la variable « prenom » vaut « <valeur> »
    Quand je rends le modèle « <modèle> »
    Alors j'obtiens « <attendu> »

    Exemples:
      | règle                                   | valeur | modèle                                        | attendu             |
      | substitution d'une référence            | Marie  | Bonjour $prenom !                             | Bonjour Marie !     |
      | notation formelle ${...}                | Mari   | ${prenom}ette                                 | Mariette            |
      | appel de méthode Java                   | marie  | $prenom.toUpperCase()                         | MARIE               |
      | condition #if / #else                   | Marie  | #if($prenom == "Marie")oui#{else}non#end      | oui                 |
      | variable locale avec #set               | Marie  | #set($n = $prenom.length())$n lettres         | 5 lettres           |
      | boucle sur un intervalle                | Marie  | #foreach($i in [1..3])$i#end                  | 123                 |
      | séparateur avec $foreach.hasNext        | Marie  | #foreach($i in [1..3])$i#if($foreach.hasNext), #end#end | 1, 2, 3   |
      | division entière                        | Marie  | #set($x = 7 / 2)$x                            | 3                   |

  Scénario: une référence inconnue est recopiée telle quelle
    Quand je rends le modèle « Bonjour $inconnu »
    Alors j'obtiens « Bonjour $inconnu »

  Scénario: une référence silencieuse inconnue ne produit rien
    Quand je rends le modèle « [$!inconnu] »
    Alors j'obtiens « [] »
