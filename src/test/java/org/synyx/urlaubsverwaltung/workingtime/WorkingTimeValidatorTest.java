package org.synyx.urlaubsverwaltung.workingtime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.application.application.Application;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;


class WorkingTimeValidatorTest {

    private WorkingTimeValidator validator;
    private WorkingTimeForm form;
    private Errors errors;

    @BeforeEach
    void setUp() {

        validator = new WorkingTimeValidator();

        errors = mock(Errors.class);

        form = new WorkingTimeForm();
        form.setFederalState(FederalState.GERMANY_BAYERN);
        form.setValidFrom(LocalDate.now(UTC));
        form.setWorkingDays(Arrays.asList(1, 2, 3, 4, 5));
    }


    // TEST OF SUPPORTS METHOD
    @Test
    void ensureSupportsOnlyWorkingTimeFormClass() {

        boolean returnValue;

        returnValue = validator.supports(null);
        assertThat(returnValue).isFalse();

        returnValue = validator.supports(Application.class);
        assertThat(returnValue).isFalse();

        returnValue = validator.supports(WorkingTimeForm.class);
        assertThat(returnValue).isTrue();
    }


    // VALIDATION OF WORKING TIMES

    @Test
    void ensureValidFromCanNotBeNull() {

        form.setValidFrom(null);

        validator.validate(form, errors);

        verify(errors).rejectValue("validFrom", "error.entry.mandatory");
    }


    @Test
    void ensureWeekDaysCanNotBeNull() {

        form.setWorkingDays(null);

        validator.validate(form, errors);

        verify(errors).rejectValue("workingDays", "person.form.workingTime.error.mandatory");
    }


    @Test
    void ensureAtLeastOneWeekDayMustBeSelectedAsWorkingTime() {

        form.setWorkingDays(Collections.emptyList());

        validator.validate(form, errors);

        verify(errors).rejectValue("workingDays", "person.form.workingTime.error.mandatory");
    }


    @Test
    void ensureValidWeekDaySelectionHasNoValidationError() {

        form.setWorkingDays(Arrays.asList(1, 2));

        validator.validate(form, errors);

        verifyNoInteractions(errors);
    }


    @Test
    void ensureFederalStateCanBeNull() {

        form.setFederalState(null);

        validator.validate(form, errors);

        verifyNoInteractions(errors);
    }
}
