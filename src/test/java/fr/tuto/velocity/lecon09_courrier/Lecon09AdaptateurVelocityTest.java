package fr.tuto.velocity.lecon09_courrier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.exception.VelocityException;
import org.junit.jupiter.api.Test;

import fr.tuto.velocity.courrier.application.GenerateurCourrierDeSortie;
import fr.tuto.velocity.courrier.infrastructure.MoteurDeModelesVelocity;
import fr.tuto.velocity.domaine.Patient;
import fr.tuto.velocity.domaine.Sejour;
import fr.tuto.velocity.domaine.Sexe;
import fr.tuto.velocity.domaine.Traitement;

/**
 * LEÇON 9 (2/2) — L'adaptateur Velocity avec le vrai modèle {@code modeles/courrier-sortie.vm}.
 */
class Lecon09AdaptateurVelocityTest {

    private final MoteurDeModelesVelocity moteur = new MoteurDeModelesVelocity();
    private final GenerateurCourrierDeSortie generateur = new GenerateurCourrierDeSortie(moteur);

    private static Sejour.Builder sejour(Sexe sexe) {
        Patient patient = Patient.builder().nom("Dupont").prenom("Marie").sexe(sexe)
                .dateNaissance(LocalDate.of(1958, 5, 12)).build();
        return Sejour.builder().patient(patient).service("Cardiologie").medecin("Dr Martin")
                .dateEntree(LocalDate.of(2026, 3, 2)).dateSortie(LocalDate.of(2026, 3, 9));
    }

    @Test
    void le_courrier_complet_est_produit() {
        Sejour sejour = sejour(Sexe.F).motif("douleur thoracique")
                .traitement(new Traitement("Bisoprolol 2,5 mg", "1 comprimé le matin"))
                .build();

        String courrier = generateur.generer(sejour, LocalDate.of(2026, 3, 10));

        assertThat(courrier)
                .startsWith("CENTRE HOSPITALIER DE DÉMONSTRATION\nService de Cardiologie\n")
                .contains("Le 10 mars 2026")
                .contains("J'ai pris en charge Madame Marie DUPONT, née le 12/05/1958, "
                        + "hospitalisée du 02/03/2026 au 09/03/2026 pour douleur thoracique.")
                .contains("  1. Bisoprolol 2,5 mg – 1 comprimé le matin\n")
                .contains("Bien confraternellement,\nDr Martin\n")
                .doesNotContain("##");
    }

    @Test
    void les_accords_suivent_le_sexe_du_patient() {
        String courrier = generateur.generer(sejour(Sexe.M).build(), LocalDate.of(2026, 3, 10));

        assertThat(courrier).contains("Monsieur Marie DUPONT, né le 12/05/1958, hospitalisé du");
    }

    @Test
    void le_mode_strict_refuse_un_modele_qui_utilise_une_donnee_absente() {
        // Données incomplètes (pas de $sejour) : en mode strict, pas de « $sejour.service » imprimé.
        Map<String, Object> incomplet = new HashMap<>();

        assertThatThrownBy(() -> moteur.rendre("courrier-sortie", incomplet))
                .isInstanceOf(VelocityException.class);
    }
}
