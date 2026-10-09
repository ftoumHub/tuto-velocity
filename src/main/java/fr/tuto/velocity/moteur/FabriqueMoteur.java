package fr.tuto.velocity.moteur;

import java.util.Properties;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.log.NullLogChute;
import org.apache.velocity.runtime.log.SystemLogChute;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

/**
 * Point d'entrée unique pour fabriquer un {@link VelocityEngine} configuré.
 *
 * <p>Dans Velocity 1.7, tout se configure par des propriétés « clé = valeur »
 * (les mêmes que dans un fichier {@code velocity.properties}). On les écrit ici
 * en toutes lettres plutôt qu'avec les constantes de {@code RuntimeConstants}
 * pour qu'on puisse les retrouver telles quelles dans la documentation.</p>
 *
 * <p>Un moteur est thread-safe une fois initialisé : en production on en crée un
 * seul, au démarrage, et on le réutilise.</p>
 */
public final class FabriqueMoteur {

    private FabriqueMoteur() {
    }

    /** Propriétés communes à tous les moteurs du tutoriel. */
    public static Properties proprietesDeBase() {
        Properties proprietes = new Properties();
        // Velocity 1.7 cherche un système de log (Avalon, Log4J, commons-logging, JUL...).
        // On le fait taire pour garder la sortie des tests lisible.
        proprietes.setProperty("runtime.log.logsystem.class", NullLogChute.class.getName());
        // Encodage des fichiers modèles lus sur disque ou dans le classpath.
        proprietes.setProperty("input.encoding", "UTF-8");
        proprietes.setProperty("output.encoding", "UTF-8");
        // Active explicitement la variable $foreach ($foreach.count, $foreach.hasNext...).
        proprietes.setProperty("foreach.provide.scope.control", "true");
        return proprietes;
    }

    /** Ajoute un chargeur de ressources qui lit les modèles dans le classpath. */
    public static Properties avecChargementClasspath(Properties proprietes) {
        proprietes.setProperty("resource.loader", "classpath");
        proprietes.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        proprietes.setProperty("classpath.resource.loader.cache", "true");
        return proprietes;
    }

    /**
     * Remplace le {@code NullLogChute} par {@code SystemLogChute}, qui écrit dans la console
     * sans aucune dépendance supplémentaire.
     *
     * @param niveau niveau minimal affiché : {@code trace}, {@code debug}, {@code info},
     *               {@code warn} ou {@code error}
     */
    public static Properties avecLogsConsole(Properties proprietes, String niveau) {
        proprietes.setProperty("runtime.log.logsystem.class", SystemLogChute.class.getName());
        proprietes.setProperty("runtime.log.logsystem.system.level", niveau);
        return proprietes;
    }

    /** Moteur minimal : il ne sait évaluer que des modèles passés sous forme de chaîne. */
    public static VelocityEngine moteurEnMemoire() {
        return demarrer(proprietesDeBase());
    }

    /** Moteur capable de charger des fichiers {@code .vm} depuis le classpath. */
    public static VelocityEngine moteurClasspath() {
        return demarrer(avecChargementClasspath(proprietesDeBase()));
    }

    /** Initialise un moteur avec les propriétés fournies. */
    public static VelocityEngine demarrer(Properties proprietes) {
        VelocityEngine moteur = new VelocityEngine();
        try {
            moteur.init(proprietes);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Impossible d'initialiser Velocity", e);
        }
        return moteur;
    }
}
