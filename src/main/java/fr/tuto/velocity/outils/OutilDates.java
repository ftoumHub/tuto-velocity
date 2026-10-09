package fr.tuto.velocity.outils;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * « Outil » à déposer dans le contexte sous le nom {@code $dates}.
 *
 * <p>Velocity 1.7 ne connaît pas {@code java.time} : sans outil, une {@link LocalDate}
 * s'affiche via son {@code toString()} ({@code 2026-03-02}). La bonne pratique est de
 * garder la logique de formatage en Java, testable, et de n'appeler que des méthodes
 * simples depuis le modèle.</p>
 */
public final class OutilDates {

    private static final DateTimeFormatter COURT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter EN_LETTRES = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRANCE);

    /** {@code $dates.court($d)} → 02/03/2026 */
    public String court(LocalDate date) {
        return date == null ? "" : date.format(COURT);
    }

    /** {@code $dates.enLettres($d)} → 2 mars 2026 */
    public String enLettres(LocalDate date) {
        return date == null ? "" : date.format(EN_LETTRES);
    }

    /** {@code $dates.age($naissance, $reference)} → âge en années révolues. */
    public int age(LocalDate naissance, LocalDate aLaDate) {
        return Period.between(naissance, aLaDate).getYears();
    }
}
