package org.synyx.urlaubsverwaltung.blackoutperiod;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;

import java.util.List;

import static java.util.Locale.GERMAN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BlackoutPeriodDescriptionsTest {

    private final StaticMessageSource messageSource = new StaticMessageSource();

    BlackoutPeriodDescriptionsTest() {
        messageSource.addMessage("blackoutperiod.day.description", GERMAN, "Urlaubssperre: {0}");
        messageSource.addMessage("blackoutperiod.day.description.restricted", GERMAN, "Urlaubssperre: {0} ({1})");
    }

    @Test
    void describesBlackoutPeriodForAllVacationTypesByTitle() {
        final BlackoutPeriod blackoutPeriod = new BlackoutPeriod();
        blackoutPeriod.setTitle("Inventur");
        blackoutPeriod.setAllVacationTypes(true);

        assertThat(BlackoutPeriodDescriptions.describe(blackoutPeriod, messageSource, GERMAN)).isEqualTo("Urlaubssperre: Inventur");
    }

    @Test
    void describesRestrictedBlackoutPeriodWithVacationTypes() {
        final VacationType<?> overtime = mock(VacationType.class);
        when(overtime.getLabel(GERMAN)).thenReturn("Überstundenabbau");
        final VacationType<?> unpaid = mock(VacationType.class);
        when(unpaid.getLabel(GERMAN)).thenReturn("Unbezahlter Urlaub");

        final BlackoutPeriod blackoutPeriod = new BlackoutPeriod();
        blackoutPeriod.setTitle("Projektphase X");
        blackoutPeriod.setAllVacationTypes(false);
        blackoutPeriod.setVacationTypes(List.of(overtime, unpaid));

        assertThat(BlackoutPeriodDescriptions.describe(blackoutPeriod, messageSource, GERMAN))
            .isEqualTo("Urlaubssperre: Projektphase X (Überstundenabbau, Unbezahlter Urlaub)");
    }

    @Test
    void joinsSeveralBlackoutPeriods() {
        final BlackoutPeriod first = new BlackoutPeriod();
        first.setTitle("Inventur");
        first.setAllVacationTypes(true);
        final BlackoutPeriod second = new BlackoutPeriod();
        second.setTitle("Jahresabschluss");
        second.setAllVacationTypes(true);

        final List<String> descriptions = List.of(
            BlackoutPeriodDescriptions.describe(first, messageSource, GERMAN),
            BlackoutPeriodDescriptions.describe(second, messageSource, GERMAN));

        assertThat(BlackoutPeriodDescriptions.join(descriptions))
            .isEqualTo("Urlaubssperre: Inventur · Urlaubssperre: Jahresabschluss");
    }
}
