package fr.tuto.velocity.cucumber;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.tuto.velocity.courrier.application.GenerateurCourrierDeSortie;
import fr.tuto.velocity.courrier.domaine.CourrierImpossibleException;
import fr.tuto.velocity.courrier.infrastructure.MoteurDeModelesVelocity;
import fr.tuto.velocity.domaine.Patient;
import fr.tuto.velocity.domaine.Sejour;
import fr.tuto.velocity.domaine.Sexe;
import fr.tuto.velocity.domaine.Traitement;
import io.cucumber.java.fr.Alors;
import io.cucumber.java.fr.Et;
import io.cucumber.java.fr.Quand;
import io.cucumber.java.fr.Soit;

/**
 * Étapes de courrier_de_sortie.feature.
 * Cucumber crée une nouvelle instance de cette classe pour chaque scénario :
 * les champs ci-dessous constituent donc l'état isolé d'un scénario.
 */
public class CourrierDeSortieSteps {

    private static final DateTimeFormatter JJ_MM_AAAA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Le moteur Velocity est coûteux à créer : un seul pour toute l'exécution.
    private static final GenerateurCourrierDeSortie GENERATEUR =
            new GenerateurCourrierDeSortie(new MoteurDeModelesVelocity());

    private final Patient.Builder patient = Patient.builder();
    private final Sejour.Builder sejour = Sejour.builder();
    private final List<Traitement> traitements = new ArrayList<Traitement>();

    private String courrier;
    private CourrierImpossibleException refus;

    private static LocalDate date(String jjMmAaaa) {
        return LocalDate.parse(jjMmAaaa, JJ_MM_AAAA);
    }

    @Soit("le patient suivant :")
    public void le_patient_suivant(Map<String, String> champs) {
        patient.nom(champs.get("nom"))
                .prenom(champs.get("prénom"))
                .sexe(Sexe.valueOf(champs.get("sexe")))
                .dateNaissance(date(champs.get("date de naissance")));
    }

    @Soit("le patient est de sexe {string}")
    public void le_patient_est_de_sexe(String sexe) {
        patient.sexe(Sexe.valueOf(sexe));
    }

    @Et("le médecin référent {string}")
    public void le_medecin_referent(String medecin) {
        sejour.medecin(medecin);
    }

    @Soit("un séjour en {string} du {string} au {string}")
    public void un_sejour_du_au(String service, String entree, String sortie) {
        sejour.service(service).dateEntree(date(entree)).dateSortie(date(sortie));
    }

    @Soit("un séjour en {string} débuté le {string} et toujours en cours")
    public void un_sejour_en_cours(String service, String entree) {
        sejour.service(service).dateEntree(date(entree));
    }

    @Et("le motif d'hospitalisation {string}")
    public void le_motif(String motif) {
        sejour.motif(motif);
    }

    @Et("les traitements de sortie suivants :")
    public void les_traitements(List<Map<String, String>> lignes) {
        for (Map<String, String> ligne : lignes) {
            traitements.add(new Traitement(ligne.get("médicament"), ligne.get("posologie")));
        }
    }

    @Quand("je génère le courrier de sortie le {string}")
    public void je_genere_le_courrier(String dateDuCourrier) {
        Sejour sejourComplet = sejour.patient(patient.build()).traitements(traitements).build();
        try {
            courrier = GENERATEUR.generer(sejourComplet, date(dateDuCourrier));
        } catch (CourrierImpossibleException e) {
            refus = e;
        }
    }

    @Alors("le courrier contient {string}")
    public void le_courrier_contient(String extrait) {
        assertThat(courrier).contains(extrait);
    }

    @Alors("le courrier ne contient pas {string}")
    public void le_courrier_ne_contient_pas(String extrait) {
        assertThat(courrier).doesNotContain(extrait);
    }

    @Alors("la génération est refusée avec le message {string}")
    public void la_generation_est_refusee(String message) {
        assertThat(courrier).isNull();
        assertThat(refus).isNotNull().hasMessage(message);
    }
}
