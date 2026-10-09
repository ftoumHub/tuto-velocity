package fr.tuto.velocity.outils;

import java.util.Locale;

/** Outil déposé dans le contexte sous le nom {@code $texte}. */
public final class OutilTexte {

    /** {@code $texte.majuscules("dupont")} → DUPONT (locale française explicite). */
    public String majuscules(String valeur) {
        return valeur == null ? "" : valeur.toUpperCase(Locale.FRANCE);
    }

    /** {@code $texte.parDefaut($valeur, "non renseigné")} */
    public Object parDefaut(Object valeur, Object defaut) {
        if (valeur == null) {
            return defaut;
        }
        if (valeur instanceof String && ((String) valeur).trim().isEmpty()) {
            return defaut;
        }
        return valeur;
    }
}
