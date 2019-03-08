package org.synyx.urlaubsverwaltung.web.workingtime;

import org.joda.time.DateMidnight;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.settings.FederalState;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class WorkingTimeValidatorTest {

    private WorkingTimeValidator validator;
    private WorkingTimeForm form;
    private Errors errors;

    @Before
    public void setUp() {

        validator = new WorkingTimeValidator();

        errors = Mockito.mock(Errors.class);

        form = new WorkingTimeForm();
        form.setFederalState(FederalState.BAYERN);
        form.setValidFrom(DateMidnight.now());
        form.setWorkingDays(Arrays.asList(1, 2, 3, 4, 5));
    }


    // TEST OF SUPPORTS METHOD
    @Test
    public void ensureSupportsOnlyWorkingTimeFormClass() {

        boolean returnValue;

        returnValue = validator.supports(null);
        assertFalse(returnValue);

        returnValue = validator.supports(Application.class);
        assertFalse(returnValue);

        returnValue = validator.supports(WorkingTimeForm.class);
        assertTrue(returnValue);
    }


    // VALIDATION OF WORKING TIMES

    @Test
    public void ensureValidFromCanNotBeNull() {

        form.setValidFrom(null);

        validator.validate(form, errors);

        Mockito.verify(errors).rejectValue("validFrom", "error.entry.mandatory");
    }


    @Test
    public void ensureWeekDaysCanNotBeNull() {

        form.setWorkingDays(null);

        validator.validate(form, errors);

        Mockito.verify(errors).rejectValue("workingDays", "person.form.workingTime.error.mandatory");
    }


    @Test
    public void ensureAtLeastOneWeekDayMustBeSelectedAsWorkingTime() {

        form.setWorkingDays(Collections.emptyList());

        validator.validate(form, errors);

        Mockito.verify(errors).rejectValue("workingDays", "person.form.workingTime.error.mandatory");
    }


    @Test
    public void ensureValidWeekDaySelectionHasNoValidationError() {

        form.setWorkingDays(Arrays.asList(1, 2));

        validator.validate(form, errors);

        Mockito.verifyZeroInteractions(errors);
    }


    @Test
    public void ensureFederalStateCanBeNull() {

        form.setFederalState(null);

        validator.validate(form, errors);

        Mockito.verifyZeroInteractions(errors);
    }
}
