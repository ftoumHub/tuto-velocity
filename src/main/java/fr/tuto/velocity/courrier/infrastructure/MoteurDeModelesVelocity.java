package fr.tuto.velocity.courrier.infrastructure;

import java.util.Map;
import java.util.Properties;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import fr.tuto.velocity.courrier.domaine.MoteurDeModeles;
import fr.tuto.velocity.moteur.FabriqueMoteur;
import fr.tuto.velocity.moteur.Rendu;
import fr.tuto.velocity.outils.OutilDates;
import fr.tuto.velocity.outils.OutilTexte;

/**
 * Adaptateur Velocity du port {@link MoteurDeModeles}.
 *
 * <ul>
 *   <li>les modèles sont lus dans le classpath, sous {@code modeles/} ;</li>
 *   <li>les macros communes sont chargées une fois pour toutes (velocimacro.library) ;</li>
 *   <li>les outils de présentation ($dates, $texte) sont ajoutés ici, pas dans le cas d'usage ;</li>
 *   <li>le mode strict transforme toute référence inconnue en erreur : indispensable
 *       pour un document médical, où « $patient.nom » imprimé tel quel serait inacceptable.</li>
 * </ul>
 */
public final class MoteurDeModelesVelocity implements MoteurDeModeles {

    private static final String REPERTOIRE = "modeles/";
    private static final String BIBLIOTHEQUE_MACROS = REPERTOIRE + "macros-courrier.vm";

    private final VelocityEngine moteur;
    private final OutilDates dates = new OutilDates();
    private final OutilTexte texte = new OutilTexte();

    public MoteurDeModelesVelocity() {
        Properties proprietes = FabriqueMoteur.avecChargementClasspath(FabriqueMoteur.proprietesDeBase());
        proprietes.setProperty("velocimacro.library", BIBLIOTHEQUE_MACROS);
        proprietes.setProperty("runtime.references.strict", "true");
        this.moteur = FabriqueMoteur.demarrer(proprietes);
    }

    @Override
    public String rendre(String nomModele, Map<String, Object> donnees) {
        VelocityContext contexte = new VelocityContext();
        for (Map.Entry<String, Object> donnee : donnees.entrySet()) {
            contexte.put(donnee.getKey(), donnee.getValue());
        }
        contexte.put("dates", dates);
        contexte.put("texte", texte);
        return Rendu.fusionner(moteur, REPERTOIRE + nomModele + ".vm", contexte);
    }
}
