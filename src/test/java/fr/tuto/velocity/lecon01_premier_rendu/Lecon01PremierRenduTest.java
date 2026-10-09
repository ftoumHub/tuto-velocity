package fr.tuto.velocity.lecon01_premier_rendu;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.junit.jupiter.api.Test;

import fr.tuto.velocity.moteur.FabriqueMoteur;
import fr.tuto.velocity.moteur.Rendu;

/**
 * LEÇON 1 — Le trio moteur / contexte / modèle.
 *
 * <pre>
 *   VelocityEngine  : le moteur, configuré puis initialisé une seule fois
 *   VelocityContext : un sac de variables nommées (clé String → n'importe quel objet)
 *   modèle          : du texte contenant des références ($x) et des directives (#if...)
 * </pre>
 *
 * <p>Ici le moteur écrit ses logs dans la console (niveau {@code debug}) : on y voit
 * l'initialisation du moteur, puis les messages émis pendant les rendus (par exemple
 * « Null reference ... $inconnu cannot be resolved »). Le moteur est {@code static} :
 * initialisé une seule fois pour toute la classe, ses logs d'initialisation n'apparaissent
 * qu'une fois.</p>
 */
class Lecon01PremierRenduTest {

    private static final VelocityEngine moteur =
            FabriqueMoteur.demarrer(FabriqueMoteur.avecLogsConsole(FabriqueMoteur.proprietesDeBase(), "debug"));

    @Test
    void un_modele_sans_reference_est_recopie_tel_quel() {
        String modele = "Bonjour tout le monde";
        String resultat = Rendu.evaluer(moteur, modele, new VelocityContext());

        assertThat(resultat).isEqualTo("Bonjour tout le monde");
    }

    @Test
    void une_reference_est_remplacee_par_la_valeur_du_contexte() {
        VelocityContext contexte = new VelocityContext();
        contexte.put("prenom", "Marie");

        String resultat = Rendu.evaluer(moteur, "Bonjour $prenom !", contexte);

        assertThat(resultat).isEqualTo("Bonjour Marie !");
    }

    @Test
    void la_valeur_est_convertie_en_texte_par_toString() {
        VelocityContext contexte = new VelocityContext();
        contexte.put("lits", 42);
        contexte.put("occupe", Boolean.TRUE);

        String resultat = Rendu.evaluer(moteur, "$lits lits, occupé : $occupe", contexte);

        assertThat(resultat).isEqualTo("42 lits, occupé : true");
    }

    @Test
    void la_notation_formelle_delimite_le_nom_de_la_reference() {
        VelocityContext contexte = new VelocityContext();
        contexte.put("prenom", "Mari");

        // Sans accolades, Velocity cherche une variable nommée « prenomette »...
        assertThat(Rendu.evaluer(moteur, "$prenomette", contexte)).isEqualTo("$prenomette");
        // ... avec accolades, il sait où s'arrête le nom.
        assertThat(Rendu.evaluer(moteur, "${prenom}ette", contexte)).isEqualTo("Mariette");
    }

    @Test
    void une_reference_inconnue_est_recopiee_telle_quelle() {
        // Comportement par défaut (hors mode strict, cf. leçon 8) : aucune erreur, le texte reste.
        String resultat = Rendu.evaluer(moteur, "Bonjour $inconnu", new VelocityContext());

        assertThat(resultat).isEqualTo("Bonjour $inconnu");
    }

    @Test
    void la_reference_silencieuse_ne_produit_rien_si_la_valeur_est_absente() {
        String resultat = Rendu.evaluer(moteur, "[$!inconnu] [$!{inconnu}]", new VelocityContext());

        assertThat(resultat).isEqualTo("[] []");
    }

    @Test
    void les_commentaires_ne_sont_pas_rendus() {
        // « ## » commente jusqu'à la fin de ligne (saut de ligne compris),
        // « #* ... *# » commente un bloc.
        String modele = "A## commentaire de ligne\nB#* commentaire\nde bloc *#C";

        assertThat(Rendu.evaluer(moteur, modele, new VelocityContext())).isEqualTo("ABC");
    }

    @Test
    void un_contexte_peut_etre_construit_depuis_une_map() {
        Map<String, Object> donnees = new HashMap<>();
        donnees.put("service", "Cardiologie");

        String resultat = Rendu.evaluer(moteur, "Service : $service", new VelocityContext(donnees));

        assertThat(resultat).isEqualTo("Service : Cardiologie");
    }

    @Test
    void un_contexte_peut_en_envelopper_un_autre() {
        // Pratique pour partager des valeurs communes (établissement, outils...)
        // sans les recopier dans chaque contexte de rendu.
        VelocityContext commun = new VelocityContext();
        commun.put("etablissement", "CH Démo");
        VelocityContext local = new VelocityContext(commun);
        local.put("service", "Cardiologie");

        String resultat = Rendu.evaluer(moteur, "$etablissement / $service", local);

        assertThat(resultat).isEqualTo("CH Démo / Cardiologie");
    }
}
