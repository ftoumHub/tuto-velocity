package fr.tuto.velocity.domaine;

import java.util.Objects;

public final class Traitement {

    private final String medicament;
    private final String posologie;

    public Traitement(String medicament, String posologie) {
        this.medicament = Objects.requireNonNull(medicament, "medicament");
        this.posologie = Objects.requireNonNull(posologie, "posologie");
    }

    public String getMedicament() {
        return medicament;
    }

    public String getPosologie() {
        return posologie;
    }
}
