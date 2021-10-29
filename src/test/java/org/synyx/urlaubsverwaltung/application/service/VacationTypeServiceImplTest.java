package org.synyx.urlaubsverwaltung.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.application.dao.VacationTypeRepository;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.OVERTIME;

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

        final VacationType holiday = new VacationType();
        holiday.setCategory(HOLIDAY);
        holiday.setActive(true);

        final VacationType overtime = new VacationType();
        overtime.setCategory(OVERTIME);
        overtime.setActive(true);

        final VacationType overtimeActive = new VacationType();
        overtimeActive.setCategory(OVERTIME);
        overtimeActive.setActive(true);

        when(vacationTypeRepository.findByActiveIsTrue()).thenReturn(List.of(holiday, overtimeActive, overtime));

        final List<VacationType> typesWithoutCategory = sut.getActiveVacationTypesWithoutCategory(OVERTIME);
        assertThat(typesWithoutCategory)
            .hasSize(1)
            .containsExactly(holiday);
    }

    @Test
    void getActiveVacationTypes() {

        final VacationType holiday = new VacationType();
        holiday.setCategory(HOLIDAY);
        holiday.setActive(true);

        final VacationType overtimeActive = new VacationType();
        overtimeActive.setCategory(OVERTIME);
        overtimeActive.setActive(true);

        when(vacationTypeRepository.findByActiveIsTrue()).thenReturn(List.of(holiday, overtimeActive));

        final List<VacationType> activeVacationTypes = sut.getActiveVacationTypes();
        assertThat(activeVacationTypes)
            .hasSize(2)
            .containsExactly(holiday, overtimeActive);
    }

    @Test
    void getAllVacationTypes() {
        final VacationType holiday = new VacationType();
        holiday.setCategory(HOLIDAY);
        holiday.setActive(true);

        final VacationType overtime = new VacationType();
        overtime.setCategory(OVERTIME);
        overtime.setActive(false);

        when(vacationTypeRepository.findAll()).thenReturn(List.of(holiday, overtime));

        final List<VacationType> allVacationTypes = sut.getAllVacationTypes();

        assertThat(allVacationTypes).hasSize(2).containsExactly(holiday, overtime);
    }
}
