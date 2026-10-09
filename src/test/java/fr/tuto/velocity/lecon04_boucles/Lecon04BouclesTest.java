package fr.tuto.velocity.lecon04_boucles;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.junit.jupiter.api.Test;

import fr.tuto.velocity.domaine.Patient;
import fr.tuto.velocity.domaine.Sejour;
import fr.tuto.velocity.domaine.Sexe;
import fr.tuto.velocity.domaine.Traitement;
import fr.tuto.velocity.moteur.FabriqueMoteur;
import fr.tuto.velocity.moteur.Rendu;

/**
 * LEÇON 4 — #foreach et la variable de boucle $foreach (nouveauté 1.7).
 */
class Lecon04BouclesTest {

    private final VelocityEngine moteur = FabriqueMoteur.moteurEnMemoire();
    private final VelocityContext contexte = new VelocityContext();

    private String rendre(String modele) {
        return Rendu.evaluer(moteur, modele, contexte);
    }

    @Test
    void foreach_parcourt_une_liste() {
        contexte.put("services", Arrays.asList("Urgences", "Cardiologie", "Pneumologie"));

        assertThat(rendre("#foreach($s in $services)[$s]#end"))
                .isEqualTo("[Urgences][Cardiologie][Pneumologie]");
    }

    @Test
    void foreach_expose_le_compteur_et_l_index() {
        contexte.put("services", Arrays.asList("Urgences", "Cardiologie"));

        // count commence à 1, index à 0. ($velocityCount existe encore mais est déprécié.)
        assertThat(rendre("#foreach($s in $services)$foreach.count/$foreach.index:$s #end"))
                .isEqualTo("1/0:Urgences 2/1:Cardiologie ");
    }

    @Test
    void hasNext_permet_de_placer_un_separateur_entre_les_elements() {
        contexte.put("services", Arrays.asList("Urgences", "Cardiologie", "Pneumologie"));

        assertThat(rendre("#foreach($s in $services)$s#if($foreach.hasNext), #end#end"))
                .isEqualTo("Urgences, Cardiologie, Pneumologie");
    }

    @Test
    void un_intervalle_se_parcourt_comme_une_liste() {
        assertThat(rendre("#foreach($i in [1..5])$i#end")).isEqualTo("12345");
        assertThat(rendre("#foreach($i in [3..1])$i#end")).isEqualTo("321");
    }

    @Test
    void break_interrompt_la_boucle() {
        assertThat(rendre("#foreach($i in [1..10])#if($i > 3)#break($foreach)#end$i#end")).isEqualTo("123");
    }

    @Test
    void on_parcourt_une_map_par_ses_cles() {
        Map<String, Integer> lits = new LinkedHashMap<>();
        lits.put("Cardiologie", 24);
        lits.put("Pneumologie", 18);
        contexte.put("lits", lits);

        assertThat(rendre("#foreach($service in $lits.keySet())$service=$lits.get($service);#end"))
                .isEqualTo("Cardiologie=24;Pneumologie=18;");
    }

    @Test
    void une_liste_vide_ne_produit_rien_il_faut_gerer_le_cas_soi_meme() {
        contexte.put("traitements", Collections.emptyList());

        // Velocity 1.7 n'a pas de #else pour #foreach.
        String modele = "#if($traitements.isEmpty())Aucun traitement#{else}#foreach($t in $traitements)$t#end#end";
        assertThat(rendre(modele)).isEqualTo("Aucun traitement");
    }

    @Test
    void une_ligne_ne_contenant_qu_une_directive_ne_laisse_pas_de_saut_de_ligne() {
        contexte.put("services", Arrays.asList("Urgences", "Cardiologie"));

        // Le saut de ligne qui suit #foreach(...) et #end est absorbé :
        // seules les lignes de contenu produisent du texte.
        String modele = "#foreach($s in $services)\n- $s\n#end";

        assertThat(rendre(modele)).isEqualTo("- Urgences\n- Cardiologie\n");
    }

    @Test
    void on_parcourt_des_objets_metier() {
        Patient patient = Patient.builder().nom("Dupont").prenom("Marie").sexe(Sexe.F)
                .dateNaissance(LocalDate.of(1958, 5, 12)).build();
        Sejour sejour = Sejour.builder().patient(patient).service("Cardiologie").medecin("Dr Martin")
                .dateEntree(LocalDate.of(2026, 3, 2))
                .traitement(new Traitement("Bisoprolol 2,5 mg", "1 le matin"))
                .traitement(new Traitement("Atorvastatine 40", "1 le soir"))
                .build();
        contexte.put("sejour", sejour);

        String modele = "#foreach($t in $sejour.traitements)$foreach.count. $t.medicament ($t.posologie)\n#end";

        assertThat(rendre(modele)).isEqualTo("1. Bisoprolol 2,5 mg (1 le matin)\n2. Atorvastatine 40 (1 le soir)\n");
    }
}
