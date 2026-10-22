package org.synyx.urlaubsverwaltung.application.vacationtype;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.ORANGE;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.YELLOW;

class VacationTypeViewModelServiceTest {

    @Test
    void ensureVacationTypeColors() {

        final VacationTypeService vacationTypeService = mock(VacationTypeService.class);
        final VacationTypeViewModelService sut = new VacationTypeViewModelService(vacationTypeService);

        final VacationType<?> personalHoliday = ProvidedVacationType.builder(new StaticMessageSource()).id(1L).color(YELLOW).build();
        final VacationType<?> companyHoliday = ProvidedVacationType.builder(new StaticMessageSource()).id(2L).color(ORANGE).build();
        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(personalHoliday, companyHoliday));

        final List<VacationTypeDto> actual = sut.getVacationTypeColors();

        assertThat(actual).containsExactly(
            new VacationTypeDto(1L, YELLOW),
            new VacationTypeDto(2L, ORANGE)
        );
    }

    @Test
    void ensureVacationTypeLabels() {

        final VacationTypeService vacationTypeService = mock(VacationTypeService.class);
        final VacationTypeViewModelService sut = new VacationTypeViewModelService(vacationTypeService);

        final StaticMessageSource messageSource = new StaticMessageSource();
        messageSource.addMessage("application.data.vacationType.holiday", Locale.GERMAN, "Erholungsurlaub");
        messageSource.addMessage("application.data.vacationType.specialleave", Locale.GERMAN, "Sonderurlaub");

        final VacationType<?> holiday = ProvidedVacationType.builder(messageSource).id(1L).messageKey("application.data.vacationType.holiday").build();
        final VacationType<?> specialLeave = ProvidedVacationType.builder(messageSource).id(2L).messageKey("application.data.vacationType.specialleave").build();
        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(holiday, specialLeave));

        assertThat(sut.getVacationTypeLabels(Locale.GERMAN)).containsExactly(
            entry(1L, "Erholungsurlaub"),
            entry(2L, "Sonderurlaub")
        );
    }
}
