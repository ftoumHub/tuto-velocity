package fr.tuto.velocity.domaine;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Patient du tutoriel.
 *
 * <p>Attention : Velocity 1.7 résout {@code $patient.nom} en cherchant, dans l'ordre,
 * {@code getNom()}, {@code getnom()}, {@code get("nom")} puis {@code isNom()}.
 * Des accesseurs « fluides » ({@code nom()}) ne seraient PAS trouvés par la notation
 * propriété : il faudrait écrire {@code $patient.nom()}. D'où les getters classiques.</p>
 *
 * <p>La classe doit aussi être {@code public} : l'introspection de Velocity ignore
 * les méthodes des classes non publiques.</p>
 */
public final class Patient {

    private final String ipp;
    private final String nir;
    private final String nom;
    private final String prenom;
    private final Sexe sexe;
    private final LocalDate dateNaissance;

    private Patient(Builder builder) {
        this.ipp = builder.ipp;
        this.nir = builder.nir;
        this.nom = Objects.requireNonNull(builder.nom, "nom");
        this.prenom = Objects.requireNonNull(builder.prenom, "prenom");
        this.sexe = Objects.requireNonNull(builder.sexe, "sexe");
        this.dateNaissance = Objects.requireNonNull(builder.dateNaissance, "dateNaissance");
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getIpp() {
        return ipp;
    }

    public String getNir() {
        return nir;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public Sexe getSexe() {
        return sexe;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    /** Accessible en VTL par {@code $patient.feminin} (préfixe « is » pour un booléen). */
    public boolean isFeminin() {
        return sexe == Sexe.F;
    }

    public static final class Builder {
        private String ipp;
        private String nir;
        private String nom;
        private String prenom;
        private Sexe sexe;
        private LocalDate dateNaissance;

        private Builder() {
        }

        public Builder ipp(String ipp) {
            this.ipp = ipp;
            return this;
        }

        public Builder nir(String nir) {
            this.nir = nir;
            return this;
        }

        public Builder nom(String nom) {
            this.nom = nom;
            return this;
        }

        public Builder prenom(String prenom) {
            this.prenom = prenom;
            return this;
        }

        public Builder sexe(Sexe sexe) {
            this.sexe = sexe;
            return this;
        }

        public Builder dateNaissance(LocalDate dateNaissance) {
            this.dateNaissance = dateNaissance;
            return this;
        }

        public Patient build() {
            return new Patient(this);
        }
    }
}
