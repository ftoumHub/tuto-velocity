package fr.tuto.velocity.lecon09_courrier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import fr.tuto.velocity.courrier.application.GenerateurCourrierDeSortie;
import fr.tuto.velocity.courrier.domaine.CourrierImpossibleException;
import fr.tuto.velocity.courrier.domaine.MoteurDeModeles;
import fr.tuto.velocity.domaine.Patient;
import fr.tuto.velocity.domaine.Sejour;
import fr.tuto.velocity.domaine.Sexe;
import fr.tuto.velocity.domaine.Traitement;

/**
 * LEÇON 9 (1/2) — Le cas d'usage se teste SANS Velocity, grâce au port MoteurDeModeles.
 *
 * <p>On vérifie ce que le cas d'usage décide (validation, choix du modèle, données
 * transmises), pas la mise en forme : c'est le rôle de l'adaptateur.</p>
 */
class Lecon09CasDUsageTest {

    /** Faux moteur qui mémorise l'appel reçu. */
    static final class MoteurEspion implements MoteurDeModeles {
        String nomModele;
        Map<String, Object> donnees = new HashMap<String, Object>();

        @Override
        public String rendre(String nomModele, Map<String, Object> donnees) {
            this.nomModele = nomModele;
            this.donnees = donnees;
            return "courrier";
        }
    }

    private final MoteurEspion moteur = new MoteurEspion();
    private final GenerateurCourrierDeSortie generateur = new GenerateurCourrierDeSortie(moteur);

    private static Sejour.Builder sejourDeMarie() {
        Patient marie = Patient.builder().nom("Dupont").prenom("Marie").sexe(Sexe.F)
                .dateNaissance(LocalDate.of(1958, 5, 12)).build();
        return Sejour.builder().patient(marie).service("Cardiologie").medecin("Dr Martin")
                .dateEntree(LocalDate.of(2026, 3, 2));
    }

    @Test
    void un_sejour_cloture_est_rendu_avec_le_modele_de_courrier_de_sortie() {
        Sejour sejour = sejourDeMarie().dateSortie(LocalDate.of(2026, 3, 9))
                .traitement(new Traitement("Bisoprolol 2,5 mg", "1 le matin")).build();

        String courrier = generateur.generer(sejour, LocalDate.of(2026, 3, 10));

        assertThat(courrier).isEqualTo("courrier");
        assertThat(moteur.nomModele).isEqualTo("courrier-sortie");
        assertThat(moteur.donnees)
                .containsEntry("sejour", sejour)
                .containsEntry("patient", sejour.getPatient())
                .containsEntry("traitements", sejour.getTraitements())
                .containsEntry("dateCourrier", LocalDate.of(2026, 3, 10));
    }

    @Test
    void un_sejour_en_cours_est_refuse_sans_solliciter_le_moteur() {
        Sejour sejour = sejourDeMarie().build();

        assertThatThrownBy(() -> generateur.generer(sejour, LocalDate.of(2026, 3, 10)))
                .isInstanceOf(CourrierImpossibleException.class)
                .hasMessage("Le séjour n'est pas clôturé : date de sortie absente");
        assertThat(moteur.nomModele).isNull();
    }
}
