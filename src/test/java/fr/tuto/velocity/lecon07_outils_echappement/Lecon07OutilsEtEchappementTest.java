package fr.tuto.velocity.lecon07_outils_echappement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.Properties;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.app.event.implement.EscapeHtmlReference;
import org.apache.velocity.exception.ParseErrorException;
import org.junit.jupiter.api.Test;

import fr.tuto.velocity.moteur.FabriqueMoteur;
import fr.tuto.velocity.moteur.Rendu;
import fr.tuto.velocity.outils.OutilDates;
import fr.tuto.velocity.outils.OutilTexte;

/**
 * LEÇON 7 — Outils Java dans le contexte, échappement, gestionnaires d'événements.
 */
class Lecon07OutilsEtEchappementTest {

    private final VelocityEngine moteur = FabriqueMoteur.moteurEnMemoire();
    private final VelocityContext contexte = new VelocityContext();

    private String rendre(String modele) {
        return Rendu.evaluer(moteur, modele, contexte);
    }

    @Test
    void sans_outil_une_date_java_time_s_affiche_au_format_iso() {
        contexte.put("entree", LocalDate.of(2026, 3, 2));

        assertThat(rendre("$entree")).isEqualTo("2026-03-02");
    }

    @Test
    void un_outil_place_dans_le_contexte_formate_les_dates() {
        contexte.put("entree", LocalDate.of(2026, 3, 2));
        contexte.put("naissance", LocalDate.of(1958, 5, 12));
        contexte.put("dates", new OutilDates());

        assertThat(rendre("$dates.court($entree)")).isEqualTo("02/03/2026");
        assertThat(rendre("$dates.enLettres($entree)")).isEqualTo("2 mars 2026");
        assertThat(rendre("$dates.age($naissance, $entree) ans")).isEqualTo("67 ans");
    }

    @Test
    void un_outil_texte_fournit_une_valeur_par_defaut() {
        contexte.put("texte", new OutilTexte());
        contexte.put("vide", "  ");

        assertThat(rendre("$texte.parDefaut($vide, \"non renseigné\")")).isEqualTo("non renseigné");
        assertThat(rendre("$texte.majuscules(\"élodie\")")).isEqualTo("ÉLODIE");
    }

    @Test
    void l_antislash_echappe_une_reference_definie() {
        contexte.put("prenom", "Marie");

        assertThat(rendre("\\$prenom vaut $prenom")).isEqualTo("$prenom vaut Marie");
    }

    @Test
    void piege_l_antislash_devant_une_reference_inconnue_est_conserve() {
        // La référence n'existant pas, Velocity recopie tout, antislash compris.
        assertThat(rendre("\\$inconnu")).isEqualTo("\\$inconnu");
    }

    @Test
    void un_dollar_ou_un_diese_suivis_de_texte_restent_du_texte() {
        assertThat(rendre("Prix : 12 $ - langage C# et Java"))
                .isEqualTo("Prix : 12 $ - langage C# et Java");
    }

    @Test
    void piege_un_diese_en_toute_fin_de_modele_leve_ParseErrorException() {
        // Le lexer de Velocity 1.7 attend une directive après « # » et bute sur la fin du texte.
        assertThatThrownBy(() -> rendre("langage C#"))
                .isInstanceOf(ParseErrorException.class);
    }

    @Test
    void un_bloc_non_interprete_est_recopie_a_l_identique() {
        contexte.put("prenom", "Marie");

        // Pratique pour produire... un modèle Velocity, ou du JavaScript plein de $.
        assertThat(rendre("#[[$prenom #if($x)]]# / $prenom"))
                .isEqualTo("$prenom #if($x) / Marie");
    }

    @Test
    void EscapeHtmlReference_protege_une_sortie_html() {
        Properties proprietes = FabriqueMoteur.proprietesDeBase();
        proprietes.setProperty("eventhandler.referenceinsertion.class", EscapeHtmlReference.class.getName());
        VelocityEngine moteurHtml = FabriqueMoteur.demarrer(proprietes);
        contexte.put("commentaire", "<script>alert('x')</script> & co");

        String resultat = Rendu.evaluer(moteurHtml, "<p>$commentaire</p>", contexte);

        // Seules les VALEURS des références sont échappées, pas le balisage du modèle.
        assertThat(resultat).isEqualTo("<p>&lt;script&gt;alert('x')&lt;/script&gt; &amp; co</p>");
    }

    @Test
    void piege_EscapeHtmlReference_transforme_aussi_les_accents_en_entites() {
        // Il s'appuie sur commons-lang 2 (StringEscapeUtils.escapeHtml), qui échappe
        // tous les caractères Latin-1. C'est valide en HTML, mais surprenant.
        Properties proprietes = FabriqueMoteur.proprietesDeBase();
        proprietes.setProperty("eventhandler.referenceinsertion.class", EscapeHtmlReference.class.getName());
        VelocityEngine moteurHtml = FabriqueMoteur.demarrer(proprietes);
        contexte.put("prenom", "Hélène");

        assertThat(Rendu.evaluer(moteurHtml, "$prenom", contexte)).isEqualTo("H&eacute;l&egrave;ne");
    }

    @Test
    void un_gestionnaire_attache_au_contexte_transforme_les_valeurs_a_l_insertion() {
        contexte.put("nir", "185057800608436");
        contexte.put("nom", "Dupont");
        EventCartridge cartouche = new EventCartridge();
        cartouche.addEventHandler(new MasquageNir());
        cartouche.attachToContext(contexte);

        assertThat(rendre("$nom : $nir")).isEqualTo("Dupont : ************436");
    }
}
