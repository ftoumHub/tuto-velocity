package fr.tuto.velocity.lecon06_macros;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.junit.jupiter.api.Test;

import fr.tuto.velocity.moteur.FabriqueMoteur;
import fr.tuto.velocity.moteur.Rendu;

/**
 * LEÇON 6 — Réutiliser : #parse, #include, #macro, bibliothèque de macros, #define.
 */
class Lecon06ReutilisationTest {

    private final VelocityContext contexte = new VelocityContext();

    Lecon06ReutilisationTest() {
        contexte.put("prenom", "Marie");
        contexte.put("prix", "12 €");
    }

    @Test
    void parse_insere_un_autre_modele_et_l_interprete_avec_le_meme_contexte() {
        VelocityEngine moteur = FabriqueMoteur.moteurClasspath();

        String resultat = Rendu.fusionner(moteur, "lecons/page.vm", contexte);

        assertThat(resultat).isEqualTo("== En-tête de Marie ==\nCorps de la page pour Marie\n");
    }

    @Test
    void include_insere_un_fichier_sans_l_interpreter() {
        VelocityEngine moteur = FabriqueMoteur.moteurClasspath();

        String resultat = Rendu.evaluer(moteur, "#include(\"lecons/brut.txt\")", contexte);

        assertThat(resultat).isEqualTo("Prix : $prix (non interprété)\n");
    }

    @Test
    void une_macro_definie_dans_le_modele_est_appelable_comme_une_directive() {
        VelocityEngine moteur = FabriqueMoteur.moteurEnMemoire();

        String modele = "#macro(salut $qui)Bonjour $qui !#end#salut(\"Paul\") #salut($prenom)";

        assertThat(Rendu.evaluer(moteur, modele, contexte)).isEqualTo("Bonjour Paul ! Bonjour Marie !");
    }

    @Test
    void les_arguments_d_une_macro_se_separent_par_des_espaces() {
        VelocityEngine moteur = FabriqueMoteur.moteurEnMemoire();

        String modele = "#macro(intervalle $debut $fin)du ${debut} au ${fin}#end#intervalle(\"02/03\" \"09/03\")";

        assertThat(Rendu.evaluer(moteur, modele, contexte)).isEqualTo("du 02/03 au 09/03");
    }

    @Test
    void piege_le_tiret_fait_partie_des_noms_de_reference_en_1_7() {
        VelocityEngine moteur = FabriqueMoteur.moteurEnMemoire();
        contexte.put("nom", "Dupont");

        // En 1.x, un identifiant peut contenir « - » : $nom-Martin est lu comme la
        // référence inconnue « nom-Martin ». La notation formelle règle le problème.
        assertThat(Rendu.evaluer(moteur, "$nom-Martin", contexte)).isEqualTo("$nom-Martin");
        assertThat(Rendu.evaluer(moteur, "${nom}-Martin", contexte)).isEqualTo("Dupont-Martin");
    }

    @Test
    void une_bibliotheque_de_macros_est_chargee_au_demarrage_du_moteur() {
        Properties proprietes = FabriqueMoteur.avecChargementClasspath(FabriqueMoteur.proprietesDeBase());
        proprietes.setProperty("velocimacro.library", "lecons/macros-lecon.vm");
        VelocityEngine moteur = FabriqueMoteur.demarrer(proprietes);

        String resultat = Rendu.evaluer(moteur, "#majuscule($prenom) #puce(\"Cardiologie\")", contexte);

        assertThat(resultat).isEqualTo("MARIE • Cardiologie");
    }

    @Test
    void une_macro_de_bloc_recoit_son_contenu_dans_bodyContent() {
        // Nouveauté 1.7 : appel avec #@nom(...) ... #end
        VelocityEngine moteur = FabriqueMoteur.moteurEnMemoire();

        String modele = "#macro(encadre)[ $bodyContent ]#end#@encadre()Bonjour $prenom#end";

        assertThat(Rendu.evaluer(moteur, modele, contexte)).isEqualTo("[ Bonjour Marie ]");
    }

    @Test
    void define_nomme_un_bloc_evalue_a_chaque_utilisation() {
        VelocityEngine moteur = FabriqueMoteur.moteurEnMemoire();

        // Le bloc n'est pas évalué au moment du #define mais à chaque affichage de $salutation.
        String modele = "#define($salutation)Bonjour $prenom#end$salutation / #set($prenom = \"Paul\")$salutation";

        assertThat(Rendu.evaluer(moteur, modele, contexte)).isEqualTo("Bonjour Marie / Bonjour Paul");
    }
}
