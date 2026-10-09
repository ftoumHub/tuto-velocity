package fr.tuto.velocity.lecon07_outils_echappement;

import org.apache.velocity.app.event.ReferenceInsertionEventHandler;

/**
 * Gestionnaire d'événement appelé juste avant l'écriture de chaque référence.
 * Ici : toute référence dont le nom contient « nir » est masquée, sauf ses 3 derniers
 * caractères. Le modèle n'a pas à y penser, la règle est centralisée.
 */
public final class MasquageNir implements ReferenceInsertionEventHandler {

    @Override
    public Object referenceInsert(String reference, Object valeur) {
        if (valeur == null || !reference.toLowerCase().contains("nir")) {
            return valeur;
        }
        String texte = valeur.toString();
        int visibles = Math.min(3, texte.length());
        StringBuilder masque = new StringBuilder();
        for (int i = 0; i < texte.length() - visibles; i++) {
            masque.append('*');
        }
        return masque.append(texte.substring(texte.length() - visibles)).toString();
    }
}
