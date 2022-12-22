package org.synyx.urlaubsverwaltung.application.vacationtype;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.ORANGE;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.YELLOW;

class VacationTypeViewModelServiceTest {

    @Test
    void ensureVacationTypeColors() {

        final VacationTypeService vacationTypeService = mock(VacationTypeService.class);
        final VacationTypeViewModelService sut = new VacationTypeViewModelService(vacationTypeService);

        final VacationType personalHoliday = new VacationType();
        personalHoliday.setId(1L);
        personalHoliday.setColor(YELLOW);

        final VacationType companyHoliday = new VacationType();
        companyHoliday.setId(2L);
        companyHoliday.setColor(ORANGE);

        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(personalHoliday, companyHoliday));

        final List<VacationTypeDto> actual = sut.getVacationTypeColors();

        assertThat(actual).containsExactly(
            new VacationTypeDto(1L, YELLOW),
            new VacationTypeDto(2L, ORANGE)
        );
    }
}
