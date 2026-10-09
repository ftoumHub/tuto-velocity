package fr.tuto.velocity.courrier.domaine;

import java.util.Map;

/**
 * Port de sortie : « transformer un nom de modèle et des données en texte ».
 * Le cas d'usage ne sait pas que Velocity existe.
 */
public interface MoteurDeModeles {

    String rendre(String nomModele, Map<String, Object> donnees);
}
