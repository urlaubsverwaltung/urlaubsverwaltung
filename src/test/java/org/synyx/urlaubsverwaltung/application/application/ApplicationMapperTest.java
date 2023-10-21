package org.synyx.urlaubsverwaltung.application.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.StaticMessageSource;
import org.synyx.urlaubsverwaltung.application.vacationtype.ProvidedVacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.SPECIALLEAVE;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.UNPAIDLEAVE;

@ExtendWith(MockitoExtension.class)
class ApplicationMapperTest {

    private ApplicationMapper sut;

    @Mock
    private VacationTypeService vacationTypeService;

    @BeforeEach
    void setUp() {
        sut = new ApplicationMapper(vacationTypeService);
    }

    @Test
    void ensureOvertimeReductionForEditingVacationOvertime() {

        final VacationType<?> oldVacationType = ProvidedVacationType.builder(new StaticMessageSource())
            .id(1L)
            .category(UNPAIDLEAVE)
            .build();

        final Application oldApplication = new Application();
        oldApplication.setVacationType(oldVacationType);
        oldApplication.setHours(Duration.ofHours(8));

        final ApplicationForLeaveFormVacationTypeDto vacationTypeDto = new ApplicationForLeaveFormVacationTypeDto();
        vacationTypeDto.setId(2L);
        vacationTypeDto.setLabel("message_key");
        vacationTypeDto.setCategory(OVERTIME);

        final ApplicationForLeaveForm applicationForLeaveForm = new ApplicationForLeaveForm();
        applicationForLeaveForm.setVacationType(vacationTypeDto);
        applicationForLeaveForm.setHours(BigDecimal.ONE);

        final VacationType<?> newVacationType = ProvidedVacationType.builder(new StaticMessageSource()).id(2L).category(OVERTIME).build();
        when(vacationTypeService.getById(2L)).thenReturn(Optional.of(newVacationType));

        final Application newApplication = sut.merge(oldApplication, applicationForLeaveForm);
        assertThat(newApplication.getHours()).isEqualTo(Duration.ofHours(1));
        assertThat(newApplication.getVacationType().getCategory()).isEqualTo(OVERTIME);
    }

    @Test
    void ensureReasonForEditingVacationSpecialLeave() {

        final VacationType<?> oldVacationType = ProvidedVacationType.builder(new StaticMessageSource())
            .id(1L)
            .category(SPECIALLEAVE)
            .build();

        final Application oldApplication = new Application();
        oldApplication.setVacationType(oldVacationType);
        oldApplication.setReason("Wedding!");

        final ApplicationForLeaveFormVacationTypeDto vacationTypeDto = new ApplicationForLeaveFormVacationTypeDto();
        vacationTypeDto.setId(1L);
        vacationTypeDto.setLabel("message_key");
        vacationTypeDto.setCategory(SPECIALLEAVE);

        final ApplicationForLeaveForm applicationForLeaveForm = new ApplicationForLeaveForm();
        applicationForLeaveForm.setVacationType(vacationTypeDto);
        applicationForLeaveForm.setReason("Birth of a child");

        final VacationType<?> newVacationType = ProvidedVacationType.builder(new StaticMessageSource()).id(1L).category(SPECIALLEAVE).build();
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(newVacationType));

        final Application newApplication = sut.merge(oldApplication, applicationForLeaveForm);
        assertThat(newApplication.getReason()).isEqualTo("Birth of a child");
        assertThat(newApplication.getVacationType().getCategory()).isEqualTo(SPECIALLEAVE);
    }

    @Test
    void ensureNoOvertimeReductionForConvertingVacationOvertimeToHoliday() {

        final VacationType<?> oldVacationType = ProvidedVacationType.builder(new StaticMessageSource())
            .id(1L)
            .category(OVERTIME)
            .build();

        final Application oldApplication = new Application();
        oldApplication.setVacationType(oldVacationType);
        oldApplication.setHours(Duration.ofHours(8));

        final ApplicationForLeaveFormVacationTypeDto vacationTypeDto = new ApplicationForLeaveFormVacationTypeDto();
        vacationTypeDto.setId(2L);
        vacationTypeDto.setLabel("message_key");
        vacationTypeDto.setCategory(HOLIDAY);

        final ApplicationForLeaveForm applicationForLeaveForm = new ApplicationForLeaveForm();
        applicationForLeaveForm.setVacationType(vacationTypeDto);

        final VacationType<?> newVacationType = ProvidedVacationType.builder(new StaticMessageSource()).id(2L).category(HOLIDAY).build();
        when(vacationTypeService.getById(2L)).thenReturn(Optional.of(newVacationType));

        final Application newApplication = sut.merge(oldApplication, applicationForLeaveForm);
        assertThat(newApplication.getHours()).isNull();
        assertThat(newApplication.getVacationType().getCategory()).isEqualTo(HOLIDAY);
    }

    @Test
    void ensureNoReasonForConvertingVacationSpecialLeaveToHoliday() {

        final VacationType<?> oldVacationType = ProvidedVacationType.builder(new StaticMessageSource())
            .id(1L)
            .category(SPECIALLEAVE)
            .build();

        final Application oldApplication = new Application();
        oldApplication.setVacationType(oldVacationType);
        oldApplication.setReason("Wedding!");

        final ApplicationForLeaveFormVacationTypeDto vacationTypeDto = new ApplicationForLeaveFormVacationTypeDto();
        vacationTypeDto.setId(2L);
        vacationTypeDto.setLabel("message_key");
        vacationTypeDto.setCategory(HOLIDAY);

        final ApplicationForLeaveForm applicationForLeaveForm = new ApplicationForLeaveForm();
        applicationForLeaveForm.setVacationType(vacationTypeDto);

        final VacationType<?> newVacationType = ProvidedVacationType.builder(new StaticMessageSource()).id(2L).category(HOLIDAY).build();
        when(vacationTypeService.getById(2L)).thenReturn(Optional.of(newVacationType));

        final Application newApplication = sut.merge(oldApplication, applicationForLeaveForm);
        assertThat(newApplication.getReason()).isNull();
        assertThat(newApplication.getVacationType().getCategory()).isEqualTo(HOLIDAY);
    }
}
