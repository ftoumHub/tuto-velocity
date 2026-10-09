package fr.tuto.velocity.lecon03_conditions;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.junit.jupiter.api.Test;

import fr.tuto.velocity.moteur.FabriqueMoteur;
import fr.tuto.velocity.moteur.Rendu;

/**
 * LEÇON 3 — #set, expressions et #if / #elseif / #else.
 */
class Lecon03SetEtConditionsTest {

    private final VelocityEngine moteur = FabriqueMoteur.moteurEnMemoire();
    private final VelocityContext contexte = new VelocityContext();

    private String rendre(String modele) {
        return Rendu.evaluer(moteur, modele, contexte);
    }

    @Test
    void set_cree_une_variable_dans_le_contexte() {
        assertThat(rendre("#set($service = \"Cardiologie\")Service : $service")).isEqualTo("Service : Cardiologie");

        // La variable est bel et bien écrite dans le contexte Java.
        assertThat(contexte.get("service")).isEqualTo("Cardiologie");
    }

    @Test
    void les_guillemets_doubles_interpolent_les_guillemets_simples_non() {
        contexte.put("prenom", "Marie");

        assertThat(rendre("#set($a = \"Bonjour $prenom\")$a")).isEqualTo("Bonjour Marie");
        assertThat(rendre("#set($b = 'Bonjour $prenom')$b")).isEqualTo("Bonjour $prenom");
    }

    @Test
    void l_arithmetique_sur_des_entiers_reste_entiere() {
        assertThat(rendre("#set($x = 7 / 2)$x")).isEqualTo("3");
        assertThat(rendre("#set($x = 7 % 2)$x")).isEqualTo("1");
        assertThat(rendre("#set($x = (2 + 3) * 4)$x")).isEqualTo("20");
    }

    @Test
    void if_elseif_else_choisit_la_premiere_branche_vraie() {
        String modele = "#if($age < 18)pédiatrie#elseif($age >= 75)gériatrie#{else}adulte#end";

        contexte.put("age", 8);
        assertThat(rendre(modele)).isEqualTo("pédiatrie");
        contexte.put("age", 80);
        assertThat(rendre(modele)).isEqualTo("gériatrie");
        contexte.put("age", 40);
        assertThat(rendre(modele)).isEqualTo("adulte");
    }

    @Test
    void les_operateurs_logiques_existent_en_symboles_et_en_mots() {
        contexte.put("a", true);
        contexte.put("b", false);

        assertThat(rendre("#if($a && !$b)oui#end")).isEqualTo("oui");
        assertThat(rendre("#if($a and not $b)oui#end")).isEqualTo("oui");
        assertThat(rendre("#if($b || $a)oui#end")).isEqualTo("oui");
        assertThat(rendre("#if(3 gt 2 and 2 eq 2)oui#end")).isEqualTo("oui");
    }

    @Test
    void une_valeur_absente_ou_false_est_fausse() {
        contexte.put("faux", Boolean.FALSE);

        assertThat(rendre("#if($inconnu)vrai#{else}faux#end")).isEqualTo("faux");
        assertThat(rendre("#if($faux)vrai#{else}faux#end")).isEqualTo("faux");
    }

    @Test
    void piege_une_chaine_vide_est_vraie_en_velocity_1_7() {
        // Tout objet non null (autre que Boolean.FALSE) est « vrai », même "" ou une liste vide.
        contexte.put("vide", "");

        assertThat(rendre("#if($vide)vrai#{else}faux#end")).isEqualTo("vrai");
        // Il faut donc tester explicitement le contenu :
        assertThat(rendre("#if($vide && $vide.length() > 0)vrai#{else}faux#end")).isEqualTo("faux");
    }

    @Test
    void piege_set_avec_une_valeur_null_ne_modifie_pas_la_variable() {
        // Par défaut (directive.set.null.allowed=false), #set ignore une affectation à null.
        // Dans une boucle, l'ancienne valeur « fuit » donc d'une itération à l'autre.
        assertThat(rendre("#set($x = \"avant\")#set($x = $inconnu)$x"))
                .isEqualTo("avant");
    }

    @Test
    void la_directive_formelle_colle_une_directive_a_du_texte() {
        contexte.put("feminin", false);

        // Écrit « #elseMonsieur », Velocity chercherait une directive nommée elseMonsieur.
        // Les accolades #{else} marquent la fin du nom de la directive.
        assertThat(rendre("#if($feminin)Madame#{else}Monsieur#end")).isEqualTo("Monsieur");
    }
}
