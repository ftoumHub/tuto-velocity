package fr.tuto.velocity.cucumber;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.velocity.VelocityContext;

import fr.tuto.velocity.moteur.FabriqueMoteur;
import fr.tuto.velocity.moteur.Rendu;
import io.cucumber.java.fr.Alors;
import io.cucumber.java.fr.Quand;
import io.cucumber.java.fr.Soit;

/**
 * Étapes de regles_vtl.feature. Expressions régulières (^...$) plutôt que
 * Cucumber Expressions, car le VTL contient des caractères spéciaux : ( ) { } /
 */
public class ReglesVtlSteps {

    private final VelocityContext contexte = new VelocityContext();
    private String resultat;

    @Soit("^la variable « (.*) » vaut « (.*) »$")
    public void la_variable_vaut(String nom, String valeur) {
        contexte.put(nom, valeur);
    }

    @Quand("^je rends le modèle « (.*) »$")
    public void je_rends_le_modele(String modele) {
        resultat = Rendu.evaluer(FabriqueMoteur.moteurEnMemoire(), modele, contexte);
    }

    @Alors("^j'obtiens « (.*) »$")
    public void j_obtiens(String attendu) {
        assertThat(resultat).isEqualTo(attendu);
    }
}
