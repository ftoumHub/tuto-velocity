package fr.tuto.velocity.courrier.application;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import fr.tuto.velocity.courrier.domaine.CourrierImpossibleException;
import fr.tuto.velocity.courrier.domaine.MoteurDeModeles;
import fr.tuto.velocity.domaine.Sejour;

/**
 * Cas d'usage : produire le courrier de sortie d'un séjour clôturé.
 *
 * <p>Il valide les règles métier, prépare les données, puis délègue la mise en forme
 * au port {@link MoteurDeModeles}. Aucune dépendance à Velocity ici.</p>
 */
public final class GenerateurCourrierDeSortie {

    public static final String MODELE = "courrier-sortie";

    private final MoteurDeModeles moteur;

    public GenerateurCourrierDeSortie(MoteurDeModeles moteur) {
        this.moteur = Objects.requireNonNull(moteur, "moteur");
    }

    public String generer(Sejour sejour, LocalDate dateDuCourrier) {
        if (!sejour.estCloture()) {
            throw new CourrierImpossibleException("Le séjour n'est pas clôturé : date de sortie absente");
        }
        Map<String, Object> donnees = new HashMap<String, Object>();
        donnees.put("patient", sejour.getPatient());
        donnees.put("sejour", sejour);
        donnees.put("traitements", sejour.getTraitements());
        donnees.put("dateCourrier", dateDuCourrier);
        return moteur.rendre(MODELE, donnees);
    }
}
