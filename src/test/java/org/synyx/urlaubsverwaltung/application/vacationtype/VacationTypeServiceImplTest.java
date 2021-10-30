package org.synyx.urlaubsverwaltung.application.vacationtype;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OVERTIME;

@ExtendWith(MockitoExtension.class)
class VacationTypeServiceImplTest {

    @Mock
    private VacationTypeRepository vacationTypeRepository;

    private VacationTypeServiceImpl sut;

    @BeforeEach
    void setUp() {
        sut = new VacationTypeServiceImpl(vacationTypeRepository);
    }

    @Test
    void getActiveVacationTypesFilteredBy() {

        final VacationTypeEntity holiday = new VacationTypeEntity();
        holiday.setId(1);
        holiday.setCategory(HOLIDAY);
        holiday.setActive(true);

        final VacationTypeEntity overtime = new VacationTypeEntity();
        overtime.setId(2);
        overtime.setCategory(OVERTIME);
        overtime.setActive(true);

        final VacationTypeEntity overtimeActive = new VacationTypeEntity();
        overtimeActive.setId(3);
        overtimeActive.setCategory(OVERTIME);
        overtimeActive.setActive(true);

        when(vacationTypeRepository.findByActiveIsTrue()).thenReturn(List.of(holiday, overtimeActive, overtime));

        final List<VacationType> typesWithoutCategory = sut.getActiveVacationTypesWithoutCategory(OVERTIME);
        assertThat(typesWithoutCategory).hasSize(1);
        assertThat(typesWithoutCategory.get(0).getId()).isEqualTo(1);
    }

    @Test
    void getActiveVacationTypes() {

        final VacationTypeEntity holiday = new VacationTypeEntity();
        holiday.setId(1);
        holiday.setCategory(HOLIDAY);
        holiday.setActive(true);

        final VacationTypeEntity overtimeActive = new VacationTypeEntity();
        overtimeActive.setId(2);
        overtimeActive.setCategory(OVERTIME);
        overtimeActive.setActive(true);

        when(vacationTypeRepository.findByActiveIsTrue()).thenReturn(List.of(holiday, overtimeActive));

        final List<VacationType> activeVacationTypes = sut.getActiveVacationTypes();
        assertThat(activeVacationTypes).hasSize(2);
        assertThat(activeVacationTypes.get(0).getId()).isEqualTo(1);
        assertThat(activeVacationTypes.get(1).getId()).isEqualTo(2);
    }

    @Test
    void getAllVacationTypes() {
        final VacationTypeEntity holiday = new VacationTypeEntity();
        holiday.setId(1);
        holiday.setCategory(HOLIDAY);
        holiday.setActive(true);

        final VacationTypeEntity overtime = new VacationTypeEntity();
        overtime.setId(2);
        overtime.setCategory(OVERTIME);
        overtime.setActive(false);

        when(vacationTypeRepository.findAll()).thenReturn(List.of(holiday, overtime));

        final List<VacationType> allVacationTypes = sut.getAllVacationTypes();
        assertThat(allVacationTypes).hasSize(2);
        assertThat(allVacationTypes.get(0).getId()).isEqualTo(1);
        assertThat(allVacationTypes.get(1).getId()).isEqualTo(2);
    }
}
