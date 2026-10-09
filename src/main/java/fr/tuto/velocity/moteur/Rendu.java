package fr.tuto.velocity.moteur;

import java.io.StringWriter;

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.VelocityException;

/**
 * Deux façons de produire du texte avec Velocity :
 * <ul>
 *   <li>{@link #evaluer} : le modèle est une chaîne en mémoire ;</li>
 *   <li>{@link #fusionner} : le modèle est un fichier chargé par un resource loader.</li>
 * </ul>
 * Les exceptions Velocity ({@code ParseErrorException}, {@code MethodInvocationException},
 * {@code ResourceNotFoundException}) héritent toutes de {@link VelocityException}
 * (non vérifiée) : on les laisse remonter telles quelles.
 */
public final class Rendu {

    private Rendu() {
    }

    public static String evaluer(VelocityEngine moteur, String modele, Context contexte) {
        StringWriter sortie = new StringWriter();
        try {
            // Le 3e argument (« logTag ») sert uniquement à identifier le modèle dans les logs et erreurs.
            moteur.evaluate(contexte, sortie, "modele-en-memoire", modele);
        } catch (VelocityException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return sortie.toString();
    }

    public static String fusionner(VelocityEngine moteur, String nomModele, Context contexte) {
        return fusionner(moteur, nomModele, "UTF-8", contexte);
    }

    public static String fusionner(VelocityEngine moteur, String nomModele, String encodage, Context contexte) {
        StringWriter sortie = new StringWriter();
        try {
            Template modele = moteur.getTemplate(nomModele, encodage);
            modele.merge(contexte, sortie);
        } catch (VelocityException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return sortie.toString();
    }
}
