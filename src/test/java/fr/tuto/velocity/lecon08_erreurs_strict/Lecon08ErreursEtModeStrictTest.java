package fr.tuto.velocity.lecon08_erreurs_strict;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Properties;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.VelocityException;
import org.junit.jupiter.api.Test;

import fr.tuto.velocity.moteur.FabriqueMoteur;
import fr.tuto.velocity.moteur.Rendu;

/**
 * LEÇON 8 — Quand ça se passe mal : erreurs de syntaxe, exceptions Java, mode strict.
 */
class Lecon08ErreursEtModeStrictTest {

    private final VelocityContext contexte = new VelocityContext();

    /** Objet dont un getter échoue, pour simuler une erreur côté Java. */
    public static final class Capricieux {
        public String getValeur() {
            throw new IllegalStateException("base de données indisponible");
        }
    }

    private static VelocityEngine moteurStrict() {
        Properties proprietes = FabriqueMoteur.proprietesDeBase();
        proprietes.setProperty("runtime.references.strict", "true");
        return FabriqueMoteur.demarrer(proprietes);
    }

    @Test
    void une_directive_non_fermee_leve_ParseErrorException() {
        VelocityEngine moteur = FabriqueMoteur.moteurEnMemoire();

        assertThatThrownBy(() -> Rendu.evaluer(moteur, "#foreach($i in [1..3])$i", contexte))
                .isInstanceOf(ParseErrorException.class);
    }

    @Test
    void une_exception_dans_un_getter_est_enveloppee_dans_MethodInvocationException() {
        VelocityEngine moteur = FabriqueMoteur.moteurEnMemoire();
        contexte.put("objet", new Capricieux());

        assertThatThrownBy(() -> Rendu.evaluer(moteur, "$objet.valeur", contexte))
                .isInstanceOf(MethodInvocationException.class)
                .satisfies(e -> assertThat(((MethodInvocationException) e).getWrappedThrowable())
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessage("base de données indisponible"));
    }

    @Test
    void hors_mode_strict_une_methode_inexistante_est_recopiee_sans_erreur() {
        VelocityEngine moteur = FabriqueMoteur.moteurEnMemoire();
        contexte.put("prenom", "Marie");

        assertThat(Rendu.evaluer(moteur, "$prenom.methodeInexistante()", contexte))
                .isEqualTo("$prenom.methodeInexistante()");
    }

    @Test
    void en_mode_strict_une_reference_inconnue_est_une_erreur() {
        assertThatThrownBy(() -> Rendu.evaluer(moteurStrict(), "Bonjour $inconnu", contexte))
                .isInstanceOf(VelocityException.class)
                .hasMessageContaining("$inconnu");
    }

    @Test
    void en_mode_strict_une_methode_inexistante_est_une_erreur() {
        contexte.put("prenom", "Marie");

        assertThatThrownBy(() -> Rendu.evaluer(moteurStrict(), "$prenom.methodeInexistante()", contexte))
                .isInstanceOf(VelocityException.class);
    }

    @Test
    void en_mode_strict_un_if_sur_une_reference_simple_absente_reste_permis() {
        // Exception documentée : #if($x) sans propriété ni méthode ne lève rien, il vaut faux.
        assertThat(Rendu.evaluer(moteurStrict(), "#if($inconnu)oui#{else}non#end", contexte)).isEqualTo("non");
    }
}
