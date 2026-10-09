package fr.tuto.velocity.courrier.domaine;

public final class CourrierImpossibleException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CourrierImpossibleException(String message) {
        super(message);
    }
}
