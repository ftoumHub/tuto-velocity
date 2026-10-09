package fr.tuto.velocity.lecon05_chargement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.StringWriter;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

import fr.tuto.velocity.moteur.FabriqueMoteur;
import fr.tuto.velocity.moteur.Rendu;

/**
 * LEÇON 5 — Modèles dans des fichiers : resource loader, Template, encodage.
 *
 * <p>Le fichier {@code src/test/resources/lecons/bienvenue.vm} est encodé en UTF-8.</p>
 */
class Lecon05ChargementDeModelesTest {

    @Test
    void un_modele_se_charge_depuis_le_classpath() {
        VelocityEngine moteur = FabriqueMoteur.moteurClasspath();
        VelocityContext contexte = new VelocityContext();
        contexte.put("prenom", "Marie");

        String resultat = Rendu.fusionner(moteur, "lecons/bienvenue.vm", contexte);

        assertThat(resultat).isEqualTo("Bonjour Marie, bienvenue à l'hôpital.\n");
    }

    @Test
    void un_template_charge_une_fois_se_fusionne_avec_plusieurs_contextes() throws Exception {
        VelocityEngine moteur = FabriqueMoteur.moteurClasspath();
        // getTemplate analyse le fichier une fois ; le Template est ensuite réutilisable
        // (et mis en cache par le moteur si classpath.resource.loader.cache=true).
        Template modele = moteur.getTemplate("lecons/bienvenue.vm", "UTF-8");

        assertThat(fusionner(modele, "Marie")).startsWith("Bonjour Marie");
        assertThat(fusionner(modele, "Paul")).startsWith("Bonjour Paul");
    }

    private static String fusionner(Template modele, String prenom) throws Exception {
        VelocityContext contexte = new VelocityContext();
        contexte.put("prenom", prenom);
        StringWriter sortie = new StringWriter();
        modele.merge(contexte, sortie);
        return sortie.toString();
    }

    @Test
    void piege_un_mauvais_encodage_abime_les_accents() {
        VelocityEngine moteur = FabriqueMoteur.moteurClasspath();
        VelocityContext contexte = new VelocityContext();
        contexte.put("prenom", "Marie");

        // Le fichier est en UTF-8 mais on demande de le lire en ISO-8859-1 :
        // chaque caractère accentué (2 octets en UTF-8) devient 2 caractères.
        String resultat = Rendu.fusionner(moteur, "lecons/bienvenue.vm", "ISO-8859-1", contexte);

        assertThat(resultat).contains("hÃ´pital").doesNotContain("hôpital");
    }

    @Test
    void un_modele_introuvable_leve_ResourceNotFoundException() {
        VelocityEngine moteur = FabriqueMoteur.moteurClasspath();

        assertThatThrownBy(() -> Rendu.fusionner(moteur, "lecons/absent.vm", new VelocityContext()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void sans_resource_loader_classpath_le_moteur_cherche_sur_le_disque() {
        // Par défaut, Velocity 1.7 utilise le FileResourceLoader avec file.resource.loader.path=".".
        // Le fichier n'est donc pas trouvé depuis la racine du projet.
        VelocityEngine moteur = FabriqueMoteur.moteurEnMemoire();

        assertThatThrownBy(() -> Rendu.fusionner(moteur, "lecons/bienvenue.vm", new VelocityContext()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void le_file_resource_loader_lit_un_repertoire_du_disque() {
        // Utile pour des modèles modifiables sans redéploiement.
        Properties proprietes = FabriqueMoteur.proprietesDeBase();
        proprietes.setProperty("resource.loader", "file");
        proprietes.setProperty("file.resource.loader.path", "src/test/resources");
        proprietes.setProperty("file.resource.loader.cache", "false");
        VelocityEngine moteur = FabriqueMoteur.demarrer(proprietes);
        VelocityContext contexte = new VelocityContext();
        contexte.put("prenom", "Marie");

        assertThat(Rendu.fusionner(moteur, "lecons/bienvenue.vm", contexte)).startsWith("Bonjour Marie");
    }
}
