package org.synyx.urlaubsverwaltung.application.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeEntity;
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
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.YELLOW;

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

        final VacationTypeEntity oldVacationTypeEntity = new VacationTypeEntity();
        oldVacationTypeEntity.setCategory(UNPAIDLEAVE);

        final Application oldApplication = new Application();
        oldApplication.setVacationType(oldVacationTypeEntity);
        oldApplication.setHours(Duration.ofHours(8));

        final ApplicationForLeaveFormVacationTypeDto vacationTypeDto = new ApplicationForLeaveFormVacationTypeDto();
        vacationTypeDto.setId(2L);
        vacationTypeDto.setMessageKey("message_key");
        vacationTypeDto.setCategory(OVERTIME);

        final ApplicationForLeaveForm applicationForLeaveForm = new ApplicationForLeaveForm();
        applicationForLeaveForm.setVacationType(vacationTypeDto);
        applicationForLeaveForm.setHours(BigDecimal.ONE);

        final VacationType newVacationType = new VacationType(2L, true, OVERTIME, "message_key", true, true, YELLOW, false);
        when(vacationTypeService.getById(2L)).thenReturn(Optional.of(newVacationType));

        final Application newApplication = sut.merge(oldApplication, applicationForLeaveForm);
        assertThat(newApplication.getHours()).isEqualTo(Duration.ofHours(1));
        assertThat(newApplication.getVacationType().getCategory()).isEqualTo(OVERTIME);
    }

    @Test
    void ensureReasonForEditingVacationSpecialLeave() {

        final VacationTypeEntity oldVacationType = new VacationTypeEntity();
        oldVacationType.setId(1L);
        oldVacationType.setCategory(SPECIALLEAVE);

        final Application oldApplication = new Application();
        oldApplication.setVacationType(oldVacationType);
        oldApplication.setReason("Wedding!");

        final ApplicationForLeaveFormVacationTypeDto vacationTypeDto = new ApplicationForLeaveFormVacationTypeDto();
        vacationTypeDto.setId(1L);
        vacationTypeDto.setMessageKey("message_key");
        vacationTypeDto.setCategory(SPECIALLEAVE);

        final ApplicationForLeaveForm applicationForLeaveForm = new ApplicationForLeaveForm();
        applicationForLeaveForm.setVacationType(vacationTypeDto);
        applicationForLeaveForm.setReason("Birth of a child");

        final VacationType newVacationType = new VacationType(1L, true, SPECIALLEAVE, "message_key", true, true, YELLOW, false);
        when(vacationTypeService.getById(1L)).thenReturn(Optional.of(newVacationType));

        final Application newApplication = sut.merge(oldApplication, applicationForLeaveForm);
        assertThat(newApplication.getReason()).isEqualTo("Birth of a child");
        assertThat(newApplication.getVacationType().getCategory()).isEqualTo(SPECIALLEAVE);
    }

    @Test
    void ensureNoOvertimeReductionForConvertingVacationOvertimeToHoliday() {

        final VacationTypeEntity oldVacationType = new VacationTypeEntity();
        oldVacationType.setId(1L);
        oldVacationType.setCategory(OVERTIME);

        final Application oldApplication = new Application();
        oldApplication.setVacationType(oldVacationType);
        oldApplication.setHours(Duration.ofHours(8));

        final ApplicationForLeaveFormVacationTypeDto vacationTypeDto = new ApplicationForLeaveFormVacationTypeDto();
        vacationTypeDto.setId(2L);
        vacationTypeDto.setMessageKey("message_key");
        vacationTypeDto.setCategory(HOLIDAY);

        final ApplicationForLeaveForm applicationForLeaveForm = new ApplicationForLeaveForm();
        applicationForLeaveForm.setVacationType(vacationTypeDto);

        final VacationType newVacationType = new VacationType(2L, true, HOLIDAY, "message_key", true, true, YELLOW, false);
        when(vacationTypeService.getById(2L)).thenReturn(Optional.of(newVacationType));

        final Application newApplication = sut.merge(oldApplication, applicationForLeaveForm);
        assertThat(newApplication.getHours()).isNull();
        assertThat(newApplication.getVacationType().getCategory()).isEqualTo(HOLIDAY);
    }

    @Test
    void ensureNoReasonForConvertingVacationSpecialLeaveToHoliday() {

        final VacationTypeEntity oldVacationType = new VacationTypeEntity();
        oldVacationType.setId(1L);
        oldVacationType.setCategory(SPECIALLEAVE);

        final Application oldApplication = new Application();
        oldApplication.setVacationType(oldVacationType);
        oldApplication.setReason("Wedding!");

        final ApplicationForLeaveFormVacationTypeDto vacationTypeDto = new ApplicationForLeaveFormVacationTypeDto();
        vacationTypeDto.setId(2L);
        vacationTypeDto.setMessageKey("message_key");
        vacationTypeDto.setCategory(HOLIDAY);

        final ApplicationForLeaveForm applicationForLeaveForm = new ApplicationForLeaveForm();
        applicationForLeaveForm.setVacationType(vacationTypeDto);

        final VacationType newVacationType = new VacationType(2L, true, HOLIDAY, "message_key", true, true, YELLOW, false);
        when(vacationTypeService.getById(2L)).thenReturn(Optional.of(newVacationType));

        final Application newApplication = sut.merge(oldApplication, applicationForLeaveForm);
        assertThat(newApplication.getReason()).isNull();
        assertThat(newApplication.getVacationType().getCategory()).isEqualTo(HOLIDAY);
    }
}
