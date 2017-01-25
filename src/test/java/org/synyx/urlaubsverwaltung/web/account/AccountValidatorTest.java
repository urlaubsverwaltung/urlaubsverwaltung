package org.synyx.urlaubsverwaltung.web.account;

import org.joda.time.DateMidnight;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.settings.Settings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;

import java.math.BigDecimal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class AccountValidatorTest {

    private AccountValidator validator;

    private AccountForm form;
    private Settings settings;
    private Errors errors;

    @Before
    public void setUp() {

        SettingsService settingsService = Mockito.mock(SettingsService.class);

        settings = new Settings();
        Mockito.when(settingsService.getSettings()).thenReturn(settings);

        validator = new AccountValidator(settingsService);

        form = new AccountForm(2013);

        errors = Mockito.mock(Errors.class);
    }


    // TEST OF SUPPORTS METHOD

    @Test
    public void ensureSupportsOnlyAccountFormClass() {

        boolean returnValue;

        returnValue = validator.supports(null);
        assertFalse(returnValue);

        returnValue = validator.supports(Application.class);
        assertFalse(returnValue);

        returnValue = validator.supports(AccountForm.class);
        assertTrue(returnValue);
    }


    // VALIDATION OF ANNUAL VACATION FIELD

    @Test
    public void ensureAnnualVacationMustNotBeNull() {

        form.setAnnualVacationDays(null);
        validator.validateAnnualVacation(form, errors);
        Mockito.verify(errors).rejectValue("annualVacationDays", "error.entry.mandatory");
    }


    @Test
    public void ensureAnnualVacationMustNotBeGreaterThanMaximumDaysConfiguredInSettings() {

        int maxDays = 40;

        settings.getAbsenceSettings().setMaximumAnnualVacationDays(maxDays);

        form.setAnnualVacationDays(new BigDecimal(maxDays + 1));

        validator.validateAnnualVacation(form, errors);
        Mockito.verify(errors).rejectValue("annualVacationDays", "error.entry.invalid");
    }


    @Test
    public void ensureValidAnnualVacationHasNoValidationError() {

        form.setAnnualVacationDays(new BigDecimal("28"));
        validator.validateAnnualVacation(form, errors);
        Mockito.verifyZeroInteractions(errors);
    }


    // VALIDATION OF ACTUAL VACATION FIELD

    @Test
    public void ensureActualVacationMustNotBeNull() {

        form.setActualVacationDays(null);
        validator.validateActualVacation(form, errors);
        Mockito.verify(errors).rejectValue("actualVacationDays", "error.entry.mandatory");
    }


    @Test
    public void ensureActualVacationMustNotBeGreaterThanAnnualVacation() {

        form.setAnnualVacationDays(new BigDecimal("30"));
        form.setActualVacationDays(new BigDecimal("31"));

        validator.validateActualVacation(form, errors);
        Mockito.verify(errors).rejectValue("actualVacationDays", "error.entry.invalid");
    }


    @Test
    public void ensureValidActualVacationHasNoValidationError() {

        form.setAnnualVacationDays(new BigDecimal("30"));
        form.setActualVacationDays(new BigDecimal("28"));

        validator.validateActualVacation(form, errors);
        Mockito.verifyZeroInteractions(errors);
    }


    // VALIDATION OF REMAINING VACATION DAYS FIELD

    @Test
    public void ensureRemainingVacationDaysMustNotBeNull() {

        form.setRemainingVacationDays(null);
        validator.validateRemainingVacationDays(form, errors);
        Mockito.verify(errors).rejectValue("remainingVacationDays", "error.entry.mandatory");
    }


    @Test
    public void ensureRemainingVacationDaysMustNotBeGreaterThanOneYear() {

        form.setRemainingVacationDays(new BigDecimal("367"));
        validator.validateRemainingVacationDays(form, errors);
        Mockito.verify(errors).rejectValue("remainingVacationDays", "error.entry.invalid");
    }


    @Test
    public void ensureValidRemainingVacationDaysHaveNoValidationError() {

        form.setRemainingVacationDays(new BigDecimal("5"));
        form.setRemainingVacationDaysNotExpiring(new BigDecimal("5"));
        validator.validateRemainingVacationDays(form, errors);
        Mockito.verifyZeroInteractions(errors);
    }


    // VALIDATION OF REMAINING VACATION DAYS NOT EXPIRING FIELD

    @Test
    public void ensureRemainingVacationDaysNotExpiringMustNotBeNull() {

        form.setRemainingVacationDaysNotExpiring(null);
        validator.validateRemainingVacationDays(form, errors);
        Mockito.verify(errors).rejectValue("remainingVacationDaysNotExpiring", "error.entry.mandatory");
    }


    @Test
    public void ensureRemainingVacationDaysNotExpiringMustNotBeGreaterThanRemainingVacationDays() {

        form.setRemainingVacationDays(new BigDecimal("5"));
        form.setRemainingVacationDaysNotExpiring(new BigDecimal("6"));
        validator.validateRemainingVacationDays(form, errors);
        Mockito.verify(errors).rejectValue("remainingVacationDaysNotExpiring", "error.entry.invalid");
    }


    // VALIDATION OF PERIOD

    @Test
    public void ensureHolidaysAccountValidFromMustNotBeNull() {

        form.setHolidaysAccountValidFrom(null);

        validator.validatePeriod(form, errors);

        Mockito.verify(errors).rejectValue("holidaysAccountValidFrom", "error.entry.mandatory");
    }


    @Test
    public void ensureHolidaysAccountValidToMustNotBeNull() {

        form.setHolidaysAccountValidTo(null);

        validator.validatePeriod(form, errors);

        Mockito.verify(errors).rejectValue("holidaysAccountValidTo", "error.entry.mandatory");
    }


    @Test
    public void ensureFromOfPeriodMustBeBeforeTo() {

        // invalid period: 1.5.2013 - 1.1.2013

        form.setHolidaysAccountValidFrom(new DateMidnight(2013, 5, 1));
        form.setHolidaysAccountValidTo(new DateMidnight(2013, 1, 1));

        validator.validatePeriod(form, errors);

        Mockito.verify(errors).reject("error.entry.invalidPeriod");
    }


    @Test
    public void ensurePeriodMustBeGreaterThanOnlyOneDay() {

        // invalid period: 5.1.2013 - 5.1.2013

        form.setHolidaysAccountValidFrom(new DateMidnight(2013, 5, 1));
        form.setHolidaysAccountValidTo(new DateMidnight(2013, 5, 1));

        validator.validatePeriod(form, errors);

        Mockito.verify(errors).reject("error.entry.invalidPeriod");
    }


    @Test
    public void ensurePeriodMustBeWithinTheProvidedYear() {

        form = new AccountForm(2014);

        form.setHolidaysAccountValidFrom(new DateMidnight(2013, 1, 1));
        form.setHolidaysAccountValidTo(new DateMidnight(2013, 5, 1));

        validator.validatePeriod(form, errors);

        Mockito.verify(errors).reject("error.entry.invalidPeriod");
    }


    @Test
    public void ensureValidPeriodHasNoValidationError() {

        // valid period: 1.5.2013 - 5.5.2013

        form.setHolidaysAccountValidFrom(new DateMidnight(2013, 5, 1));
        form.setHolidaysAccountValidTo(new DateMidnight(2013, 5, 5));

        validator.validatePeriod(form, errors);

        Mockito.verifyZeroInteractions(errors);
    }

    @Test
    public void ensureCommentHasNoValidationError() {

        form = new AccountForm(2017);
        form.setComment("blabla");

        validator.validateComment(form, errors);

        Mockito.verifyZeroInteractions(errors);
    }

    @Test
    public void ensureCommentHasLengthValidationError() {

        form = new AccountForm(2017);
        form.setComment("blablablablablablablablablablablablablablablablablablablablablablablablablablablablablabla" +
                "blablablablablablablablablablablablablablablablablablablablablablablablablablablablablablablabla" +
                "blablablablablablablablablablablablablablablablablablablablablablablablablablablablablablablablabla" +
                "bla");

        validator.validateComment(form, errors);

        Mockito.verify(errors).rejectValue("comment","error.entry.commentTooLong");

    }

}
