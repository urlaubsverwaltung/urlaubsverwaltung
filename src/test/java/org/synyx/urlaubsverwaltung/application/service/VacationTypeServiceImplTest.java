package org.synyx.urlaubsverwaltung.application.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.synyx.urlaubsverwaltung.application.dao.VacationTypeRepository;
import org.synyx.urlaubsverwaltung.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class VacationTypeServiceImplTest {

    @Mock
    private VacationTypeRepository vacationTypeRepository;

    @Test
    public void getVacationTypesFilteredBy() {

        VacationTypeServiceImpl sut = new VacationTypeServiceImpl(vacationTypeRepository);

        final VacationType holiday = new VacationType();
        holiday.setCategory(VacationCategory.HOLIDAY);

        final VacationType overtime = new VacationType();
        overtime.setCategory(VacationCategory.OVERTIME);

        final VacationType specialLeave = new VacationType();
        specialLeave.setCategory(VacationCategory.SPECIALLEAVE);

        when(vacationTypeRepository.findAll()).thenReturn(asList(holiday, overtime, specialLeave));

        final List<VacationType> noOvertimeType = sut.getVacationTypesFilteredBy(VacationCategory.OVERTIME);

        assertThat(noOvertimeType).hasSize(2).containsExactly(holiday, specialLeave);
    }
}
