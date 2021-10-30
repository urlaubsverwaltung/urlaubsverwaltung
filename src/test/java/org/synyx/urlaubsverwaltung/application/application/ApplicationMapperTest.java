package org.synyx.urlaubsverwaltung.application.application;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;

import java.math.BigDecimal;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.SPECIALLEAVE;

class ApplicationMapperTest {

    @Test
    void ensureOvertimeReductionForEditingVacationOvertime() {

        final VacationType oldVacationType = new VacationType();
        oldVacationType.setCategory(OVERTIME);
        final Application oldApplication = new Application();
        oldApplication.setVacationType(oldVacationType);
        oldApplication.setHours(Duration.ofHours(8));

        final VacationType newVacationType = new VacationType();
        newVacationType.setCategory(OVERTIME);
        final ApplicationForLeaveForm applicationForLeaveForm = new ApplicationForLeaveForm();
        applicationForLeaveForm.setVacationType(newVacationType);
        applicationForLeaveForm.setHours(BigDecimal.ONE);

        final Application newApplication = ApplicationMapper.merge(oldApplication, applicationForLeaveForm);
        assertThat(newApplication.getHours()).isEqualTo(Duration.ofHours(1));
        assertThat(newApplication.getVacationType().getCategory()).isEqualTo(OVERTIME);
    }

    @Test
    void ensureReasonForEditingVacationSpecialLeave() {

        final VacationType oldVacationType = new VacationType();
        oldVacationType.setCategory(SPECIALLEAVE);
        final Application oldApplication = new Application();
        oldApplication.setVacationType(oldVacationType);
        oldApplication.setReason("Wedding!");

        final VacationType newVacationType = new VacationType();
        newVacationType.setCategory(SPECIALLEAVE);
        final ApplicationForLeaveForm applicationForLeaveForm = new ApplicationForLeaveForm();
        applicationForLeaveForm.setVacationType(newVacationType);
        applicationForLeaveForm.setReason("Birth of a child");

        final Application newApplication = ApplicationMapper.merge(oldApplication, applicationForLeaveForm);
        assertThat(newApplication.getReason()).isEqualTo("Birth of a child");
        assertThat(newApplication.getVacationType().getCategory()).isEqualTo(SPECIALLEAVE);
    }

    @Test
    void ensureNoOvertimeReductionForConvertingVacationOvertimeToHoliday() {

        final VacationType oldVacationType = new VacationType();
        oldVacationType.setCategory(OVERTIME);
        final Application oldApplication = new Application();
        oldApplication.setVacationType(oldVacationType);
        oldApplication.setHours(Duration.ofHours(8));

        final VacationType newVacationType = new VacationType();
        newVacationType.setCategory(HOLIDAY);
        final ApplicationForLeaveForm applicationForLeaveForm = new ApplicationForLeaveForm();
        applicationForLeaveForm.setVacationType(newVacationType);

        final Application newApplication = ApplicationMapper.merge(oldApplication, applicationForLeaveForm);
        assertThat(newApplication.getHours()).isNull();
        assertThat(newApplication.getVacationType().getCategory()).isEqualTo(HOLIDAY);
    }

    @Test
    void ensureNoReasonForConvertingVacationSpecialLeaveToHoliday() {

        final VacationType oldVacationType = new VacationType();
        oldVacationType.setCategory(SPECIALLEAVE);
        final Application oldApplication = new Application();
        oldApplication.setVacationType(oldVacationType);
        oldApplication.setReason("Wedding!");

        final VacationType newVacationType = new VacationType();
        newVacationType.setCategory(HOLIDAY);
        final ApplicationForLeaveForm applicationForLeaveForm = new ApplicationForLeaveForm();
        applicationForLeaveForm.setVacationType(newVacationType);

        final Application newApplication = ApplicationMapper.merge(oldApplication, applicationForLeaveForm);
        assertThat(newApplication.getReason()).isNull();
        assertThat(newApplication.getVacationType().getCategory()).isEqualTo(HOLIDAY);
    }
}
