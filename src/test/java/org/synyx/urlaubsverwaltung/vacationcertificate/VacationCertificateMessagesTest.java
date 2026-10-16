package org.synyx.urlaubsverwaltung.vacationcertificate;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.math.BigDecimal;
import java.util.Locale;

import static java.util.Locale.ENGLISH;
import static java.util.Locale.GERMAN;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The day counts are printed on a signed document, so one day must read "1 Arbeitstag", not "1 Arbeitstage".
 */
class VacationCertificateMessagesTest {

    private static final Locale GREEK = Locale.of("el");

    private final ResourceBundleMessageSource messageSource = messageSource();

    @Test
    void ensureGrantedTotalInSingularAndPlural() {
        assertThat(msg("vacationcertificate.document.granted.total", GERMAN, days("1"))).isEqualTo("Gewährt insgesamt: 1 Arbeitstag");
        assertThat(msg("vacationcertificate.document.granted.total", GERMAN, days("2.5"))).isEqualTo("Gewährt insgesamt: 2,5 Arbeitstage");
        assertThat(msg("vacationcertificate.document.granted.total", GERMAN, days("0.5"))).isEqualTo("Gewährt insgesamt: 0,5 Arbeitstage");
        assertThat(msg("vacationcertificate.document.granted.total", ENGLISH, days("1"))).isEqualTo("Granted in total: 1 working day");
        assertThat(msg("vacationcertificate.document.granted.total", ENGLISH, days("3"))).isEqualTo("Granted in total: 3 working days");
        assertThat(msg("vacationcertificate.document.granted.total", GREEK, days("1"))).isEqualTo("Σύνολο χορηγηθείσας: 1 εργάσιμη ημέρα");
    }

    @Test
    void ensureGrantedFromRemainingInSingularAndPlural() {
        assertThat(msg("vacationcertificate.document.granted.total.fromRemaining", GERMAN, days("3"), days("1")))
            .isEqualTo("Gewährt insgesamt: 3 Arbeitstage, davon 1 Arbeitstag Resturlaub aus dem Vorjahr");
        assertThat(msg("vacationcertificate.document.granted.total.fromRemaining", GERMAN, days("1"), days("0.5")))
            .isEqualTo("Gewährt insgesamt: 1 Arbeitstag, davon 0,5 Arbeitstage Resturlaub aus dem Vorjahr");
        assertThat(msg("vacationcertificate.document.granted.total.fromRemaining", ENGLISH, days("3"), days("1")))
            .isEqualTo("Granted in total: 3 working days, of which 1 working day remaining vacation of the previous year");
    }

    @Test
    void ensureCompensatedDaysInSingularAndPlural() {
        assertThat(msg("vacationcertificate.document.compensated", GERMAN, new BigDecimal("1.00"))).isEqualTo("Für 1 Arbeitstag wurde Urlaubsabgeltung gezahlt.");
        assertThat(msg("vacationcertificate.document.compensated", GERMAN, new BigDecimal("2.50"))).isEqualTo("Für 2,5 Arbeitstage wurde Urlaubsabgeltung gezahlt.");
        assertThat(msg("vacationcertificate.document.compensated", ENGLISH, new BigDecimal("1.00"))).isEqualTo("Vacation compensation was paid for 1 working day.");
    }

    @Test
    void ensureEntitlementInSingularAndPlural() {
        assertThat(msg("vacationcertificate.document.entitlement.days", GERMAN, days("1"))).isEqualTo("1 Arbeitstag");
        assertThat(msg("vacationcertificate.document.entitlement.days", GERMAN, days("30"))).isEqualTo("30 Arbeitstage");
        assertThat(msg("vacationcertificate.document.entitlement.daysPerWeek", GERMAN, days("30"), days("4.5"))).isEqualTo("30 Arbeitstage bei einer 4,5-Tage-Woche");
        assertThat(msg("vacationcertificate.document.entitlement.daysPerWeek", ENGLISH, days("1"), days("5"))).isEqualTo("1 working day based on a 5-day week");
    }

    private String msg(String key, Locale locale, Object... args) {
        return messageSource.getMessage(key, args, locale);
    }

    private static BigDecimal days(String days) {
        return new BigDecimal(days);
    }

    private static ResourceBundleMessageSource messageSource() {
        final ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("messages");
        source.setDefaultEncoding("UTF-8");
        source.setFallbackToSystemLocale(false);
        return source;
    }
}
