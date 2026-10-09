package fr.tuto.velocity.lecon02_references;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.tuto.velocity.domaine.Patient;
import fr.tuto.velocity.domaine.Sejour;
import fr.tuto.velocity.domaine.Sexe;
import fr.tuto.velocity.moteur.FabriqueMoteur;
import fr.tuto.velocity.moteur.Rendu;

/**
 * LEÇON 2 — Naviguer dans les objets : propriétés, méthodes, Map, List.
 *
 * <p>{@code $patient.nom} est une <em>propriété</em> : Velocity essaie dans l'ordre
 * {@code getNom()}, {@code getnom()}, {@code get("nom")}, puis {@code isNom()}.</p>
 */
class Lecon02ReferencesTest {

    private final VelocityEngine moteur = FabriqueMoteur.moteurEnMemoire();
    private final VelocityContext contexte = new VelocityContext();

    @BeforeEach
    void preparer_le_contexte() {
        Patient patient = Patient.builder()
                .ipp("IPP001").nom("Dupont").prenom("Marie").sexe(Sexe.F)
                .dateNaissance(LocalDate.of(1958, 5, 12))
                .build();
        Sejour sejour = Sejour.builder()
                .patient(patient).service("Cardiologie").medecin("Dr Martin")
                .dateEntree(LocalDate.of(2026, 3, 2))
                .build();

        contexte.put("patient", patient);
        contexte.put("sejour", sejour);
    }

    private String rendre(String modele) {
        return Rendu.evaluer(moteur, modele, contexte);
    }

    @Test
    void une_propriete_appelle_le_getter_correspondant() {
        assertThat(rendre("$patient.prenom $patient.nom")).isEqualTo("Marie Dupont");
    }

    @Test
    void on_peut_aussi_appeler_le_getter_explicitement() {
        assertThat(rendre("$patient.getNom()")).isEqualTo("Dupont");
    }

    @Test
    void une_propriete_booleenne_utilise_le_prefixe_is() {
        assertThat(rendre("$patient.feminin")).isEqualTo("true");
    }

    @Test
    void les_proprietes_se_chainent() {
        assertThat(rendre("$sejour.patient.nom en $sejour.service")).isEqualTo("Dupont en Cardiologie");
    }

    @Test
    void toute_methode_publique_est_appelable_avec_des_arguments() {
        assertThat(rendre("$patient.nom.toUpperCase() ($patient.prenom.length() lettres)"))
                .isEqualTo("DUPONT (5 lettres)");
        assertThat(rendre("$patient.nom.substring(0, 3)")).isEqualTo("Dup");
    }

    @Test
    void une_propriete_qui_vaut_null_est_recopiee_telle_quelle() {
        // Le motif n'est pas renseigné : même règle que pour une variable inconnue.
        assertThat(rendre("Motif : $sejour.motif")).isEqualTo("Motif : $sejour.motif");
        assertThat(rendre("Motif : $!sejour.motif")).isEqualTo("Motif : ");
    }

    @Test
    void une_map_s_utilise_comme_un_objet_a_proprietes() {
        Map<String, Object> lit = new LinkedHashMap<>();
        lit.put("chambre", "204");
        lit.put("numero", "B");
        contexte.put("lit", lit);

        // $lit.chambre est traduit en lit.get("chambre")
        assertThat(rendre("Chambre $lit.chambre, lit $lit.get(\"numero\")")).isEqualTo("Chambre 204, lit B");
    }

    @Test
    void la_notation_indexee_est_disponible_depuis_la_version_1_7() {
        List<String> services = Arrays.asList("Urgences", "Cardiologie", "Pneumologie");
        Map<String, String> codes = new LinkedHashMap<>();
        codes.put("CARDIO", "Cardiologie");
        contexte.put("services", services);
        contexte.put("codes", codes);

        assertThat(rendre("$services[1] / $codes[\"CARDIO\"]")).isEqualTo("Cardiologie / Cardiologie");
    }

    @Test
    void les_listes_et_tableaux_exposent_les_methodes_des_collections() {
        contexte.put("services", Arrays.asList("Urgences", "Cardiologie"));
        contexte.put("tableau", new String[] {"a", "b", "c"});

        assertThat(rendre("$services.size() $services.get(0)")).isEqualTo("2 Urgences");
        // Velocity enveloppe les tableaux Java pour leur donner size() et get(i).
        assertThat(rendre("$tableau.size() $tableau.get(2)")).isEqualTo("3 c");
    }
}
