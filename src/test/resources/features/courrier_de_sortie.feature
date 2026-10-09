# language: fr
Fonctionnalité: Génération du courrier de sortie d'hospitalisation
  En tant que médecin du service,
  je veux produire automatiquement le courrier de sortie à destination du médecin traitant,
  afin de ne pas ressaisir les informations du séjour.

  Contexte:
    Soit le patient suivant :
      | nom               | Dupont     |
      | prénom            | Marie      |
      | sexe              | F          |
      | date de naissance | 12/05/1958 |
    Et le médecin référent "Dr Martin"

  Scénario: le courrier reprend l'identité et les dates du séjour
    Soit un séjour en "Cardiologie" du "02/03/2026" au "09/03/2026"
    Quand je génère le courrier de sortie le "10/03/2026"
    Alors le courrier contient "Service de Cardiologie"
    Et le courrier contient "Le 10 mars 2026"
    Et le courrier contient "J'ai pris en charge Madame Marie DUPONT, née le 12/05/1958"
    Et le courrier contient "hospitalisée du 02/03/2026 au 09/03/2026."
    Et le courrier contient "Dr Martin"

  Plan du Scénario: la civilité et les accords dépendent du sexe du patient
    Soit le patient est de sexe "<sexe>"
    Et un séjour en "Cardiologie" du "02/03/2026" au "09/03/2026"
    Quand je génère le courrier de sortie le "10/03/2026"
    Alors le courrier contient "<civilité> Marie DUPONT, <né> le 12/05/1958, <hospitalisé> du"

    Exemples:
      | sexe | civilité | né   | hospitalisé  |
      | F    | Madame   | née  | hospitalisée |
      | M    | Monsieur | né   | hospitalisé  |

  Scénario: le motif n'apparaît que s'il est renseigné
    Soit un séjour en "Cardiologie" du "02/03/2026" au "09/03/2026"
    Et le motif d'hospitalisation "douleur thoracique"
    Quand je génère le courrier de sortie le "10/03/2026"
    Alors le courrier contient "au 09/03/2026 pour douleur thoracique."

  Scénario: sans motif, la phrase se termine après les dates
    Soit un séjour en "Cardiologie" du "02/03/2026" au "09/03/2026"
    Quand je génère le courrier de sortie le "10/03/2026"
    Alors le courrier ne contient pas "pour"

  Scénario: les traitements de sortie sont numérotés
    Soit un séjour en "Cardiologie" du "02/03/2026" au "09/03/2026"
    Et les traitements de sortie suivants :
      | médicament        | posologie                |
      | Bisoprolol 2,5 mg | 1 comprimé le matin      |
      | Atorvastatine 40  | 1 comprimé le soir       |
    Quand je génère le courrier de sortie le "10/03/2026"
    Alors le courrier contient "Traitement de sortie :"
    Et le courrier contient "1. Bisoprolol 2,5 mg – 1 comprimé le matin"
    Et le courrier contient "2. Atorvastatine 40 – 1 comprimé le soir"
    Et le courrier ne contient pas "Aucun traitement"

  Scénario: sans traitement, une mention explicite est imprimée
    Soit un séjour en "Cardiologie" du "02/03/2026" au "09/03/2026"
    Quand je génère le courrier de sortie le "10/03/2026"
    Alors le courrier contient "Aucun traitement de sortie."

  Scénario: les mentions légales sont incluses sans être interprétées
    Soit un séjour en "Cardiologie" du "02/03/2026" au "09/03/2026"
    Quand je génère le courrier de sortie le "10/03/2026"
    Alors le courrier contient "$rien n'y est interprété"

  Scénario: un séjour non clôturé ne peut pas produire de courrier
    Soit un séjour en "Cardiologie" débuté le "02/03/2026" et toujours en cours
    Quand je génère le courrier de sortie le "10/03/2026"
    Alors la génération est refusée avec le message "Le séjour n'est pas clôturé : date de sortie absente"
