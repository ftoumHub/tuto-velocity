package fr.tuto.velocity.domaine;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Sejour {

    private final Patient patient;
    private final String service;
    private final String medecin;
    private final LocalDate dateEntree;
    private final LocalDate dateSortie;
    private final String motif;
    private final List<Traitement> traitements;

    private Sejour(Builder builder) {
        this.patient = Objects.requireNonNull(builder.patient, "patient");
        this.service = Objects.requireNonNull(builder.service, "service");
        this.medecin = Objects.requireNonNull(builder.medecin, "medecin");
        this.dateEntree = Objects.requireNonNull(builder.dateEntree, "dateEntree");
        this.dateSortie = builder.dateSortie;
        this.motif = builder.motif;
        this.traitements = Collections.unmodifiableList(new ArrayList<Traitement>(builder.traitements));
    }

    public static Builder builder() {
        return new Builder();
    }

    public Patient getPatient() {
        return patient;
    }

    public String getService() {
        return service;
    }

    public String getMedecin() {
        return medecin;
    }

    public LocalDate getDateEntree() {
        return dateEntree;
    }

    /** Peut être {@code null} tant que le séjour n'est pas clôturé. */
    public LocalDate getDateSortie() {
        return dateSortie;
    }

    /** Facultatif : peut être {@code null}. */
    public String getMotif() {
        return motif;
    }

    public List<Traitement> getTraitements() {
        return traitements;
    }

    public boolean estCloture() {
        return dateSortie != null;
    }

    public static final class Builder {
        private Patient patient;
        private String service;
        private String medecin;
        private LocalDate dateEntree;
        private LocalDate dateSortie;
        private String motif;
        private final List<Traitement> traitements = new ArrayList<Traitement>();

        private Builder() {
        }

        public Builder patient(Patient patient) {
            this.patient = patient;
            return this;
        }

        public Builder service(String service) {
            this.service = service;
            return this;
        }

        public Builder medecin(String medecin) {
            this.medecin = medecin;
            return this;
        }

        public Builder dateEntree(LocalDate dateEntree) {
            this.dateEntree = dateEntree;
            return this;
        }

        public Builder dateSortie(LocalDate dateSortie) {
            this.dateSortie = dateSortie;
            return this;
        }

        public Builder motif(String motif) {
            this.motif = motif;
            return this;
        }

        public Builder traitement(Traitement traitement) {
            this.traitements.add(traitement);
            return this;
        }

        public Builder traitements(List<Traitement> traitements) {
            this.traitements.addAll(traitements);
            return this;
        }

        public Sejour build() {
            return new Sejour(this);
        }
    }
}
