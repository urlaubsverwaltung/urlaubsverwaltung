
package org.synyx.urlaubsverwaltung.web.validator;

import com.google.common.base.Optional;

import org.joda.time.DateMidnight;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.validation.Errors;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.mail.MailNotification;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.security.Role;
import org.synyx.urlaubsverwaltung.web.person.PersonForm;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Unit test for {@link PersonValidator}.
 *
 * @author  Aljona Murygina
 */
public class PersonValidatorTest {

    private PersonValidator validator;
    private PersonForm form;
    private Errors errors = Mockito.mock(Errors.class);

    private PersonService personService = Mockito.mock(PersonService.class);

    @Before
    public void setUp() {

        validator = new PersonValidator(personService);
        form = new PersonForm(2013);

        Mockito.reset(errors);
    }


    // TEST OF SUPPORTS METHOD

    @Test
    public void ensureSupportsOnlyPersonFormClass() {

        boolean returnValue;

        returnValue = validator.supports(null);
        assertFalse(returnValue);

        returnValue = validator.supports(Application.class);
        assertFalse(returnValue);

        returnValue = validator.supports(PersonForm.class);
        assertTrue(returnValue);
    }


    // VALIDATION OF NAME FIELD

    @Test
    public void ensureNameMustNotBeNull() {

        validator.validateName(null, "nameField", errors);
        Mockito.verify(errors).rejectValue("nameField", "error.mandatory.field");
    }


    @Test
    public void ensureNameMustNotBeEmpty() {

        validator.validateName("", "nameField", errors);
        Mockito.verify(errors).rejectValue("nameField", "error.mandatory.field");
    }


    @Test
    public void ensureNameMustNotBeTooLong() {

        validator.validateName("AAAAAAAAaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "nameField",
            errors);
        Mockito.verify(errors).rejectValue("nameField", "error.length");
    }


    @Test
    public void ensureValidNameHasNoValidationError() {

        validator.validateName("Hans-Peter", "nameField", errors);
        Mockito.verifyZeroInteractions(errors);
    }


    // VALIDATION OF EMAIL FIELD

    @Test
    public void ensureEmailMustNotBeNull() {

        validator.validateEmail(null, errors);
        Mockito.verify(errors).rejectValue("email", "error.mandatory.field");
    }


    @Test
    public void ensureEmailMustNotBeEmpty() {

        validator.validateEmail("", errors);
        Mockito.verify(errors).rejectValue("email", "error.mandatory.field");
    }


    @Test
    public void ensureEmailWithoutAtIsInvalid() {

        validator.validateEmail("fraulyoner(at)verwaltung.de", errors);
        Mockito.verify(errors).rejectValue("email", "error.email");
    }


    @Test
    public void ensureEmailWithMoreThanOneAtIsInvalid() {

        validator.validateEmail("fraulyoner@verw@ltung.de", errors);
        Mockito.verify(errors).rejectValue("email", "error.email");
    }


    @Test
    public void ensureEmailWithAtOnInvalidPlaceIsInvalid() {

        validator.validateEmail("@fraulyonerverwaltung.de", errors);
        Mockito.verify(errors).rejectValue("email", "error.email");
    }


    @Test
    public void ensureEmailWithInvalidHostNameIsInvalid() {

        validator.validateEmail("fraulyoner@verwaltungde", errors);
        Mockito.verify(errors).rejectValue("email", "error.email");
    }


    @Test
    public void ensureEmailMustNotBeTooLong() {

        validator.validateEmail("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa@net.de", errors);
        Mockito.verify(errors).rejectValue("email", "error.length");
    }


    @Test
    public void ensureValidEmailHasNoValidationError() {

        validator.validateEmail("müller@verwaltung.com.de", errors);
        Mockito.verifyZeroInteractions(errors);
    }


    // VALIDATION OF ANNUAL VACATION FIELD

    @Test
    public void ensureAnnualVacationMustNotBeNull() {

        form.setAnnualVacationDays(null);
        validator.validateAnnualVacation(form, errors);
        Mockito.verify(errors).rejectValue("annualVacationDays", "error.mandatory.field");
    }


    @Test
    public void ensureAnnualVacationMustNotBeGreaterThanOneYear() {

        form.setAnnualVacationDays(new BigDecimal("367"));
        validator.validateAnnualVacation(form, errors);
        Mockito.verify(errors).rejectValue("annualVacationDays", "error.entry");
    }


    @Test
    public void ensureValidAnnualVacationHasNoValidationError() {

        form.setAnnualVacationDays(new BigDecimal("28"));
        validator.validateAnnualVacation(form, errors);
        Mockito.verifyZeroInteractions(errors);
    }


    // VALIDATION OF REMAINING VACATION DAYS FIELD

    @Test
    public void ensureRemainingVacationDaysMustNotBeNull() {

        form.setRemainingVacationDays(null);
        validator.validateRemainingVacationDays(form, errors);
        Mockito.verify(errors).rejectValue("remainingVacationDays", "error.mandatory.field");
    }


    @Test
    public void ensureRemainingVacationDaysMustNotBeGreaterThanOneYear() {

        form.setRemainingVacationDays(new BigDecimal("367"));
        validator.validateRemainingVacationDays(form, errors);
        Mockito.verify(errors).rejectValue("remainingVacationDays", "error.entry");
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
        Mockito.verify(errors).rejectValue("remainingVacationDaysNotExpiring", "error.mandatory.field");
    }


    @Test
    public void ensureRemainingVacationDaysNotExpiringMustNotBeGreaterThanRemainingVacationDays() {

        form.setRemainingVacationDays(new BigDecimal("6"));
        form.setRemainingVacationDaysNotExpiring(new BigDecimal("7"));
        validator.validateRemainingVacationDays(form, errors);
        Mockito.verify(errors).rejectValue("remainingVacationDaysNotExpiring", "error.entry");
    }


    // VALIDATION OF USERNAME

    @Test
    public void ensureUsernameMustBeUnique() {

        Mockito.when(personService.getPersonByLogin("foo")).thenReturn(Optional.of(new Person()));
        validator.validateLogin("foo", errors);
        Mockito.verify(errors).rejectValue("loginName", "error.login.unique");
    }


    @Test
    public void ensureUniqueUsernameHasNoValidationError() {

        Mockito.when(personService.getPersonByLogin("foo")).thenReturn(Optional.<Person>absent());
        validator.validateLogin("foo", errors);
        Mockito.verify(errors, Mockito.never()).rejectValue(Mockito.anyString(), Mockito.anyString());
    }


    // VALIDATION OF PERIOD

    @Test
    public void ensureHolidaysAccountValidFromMustNotBeNull() {

        form.setHolidaysAccountValidFrom(null);

        validator.validatePeriod(form, errors);

        Mockito.verify(errors).rejectValue("holidaysAccountValidFrom", "error.mandatory.field");
    }


    @Test
    public void ensureHolidaysAccountValidToMustNotBeNull() {

        form.setHolidaysAccountValidTo(null);

        validator.validatePeriod(form, errors);

        Mockito.verify(errors).rejectValue("holidaysAccountValidTo", "error.mandatory.field");
    }


    @Test
    public void ensureFromOfPeriodMustBeBeforeTo() {

        // invalid period: 1.5.2013 - 1.1.2013

        form.setHolidaysAccountValidFrom(new DateMidnight(2013, 5, 1));
        form.setHolidaysAccountValidTo(new DateMidnight(2013, 1, 1));

        validator.validatePeriod(form, errors);

        Mockito.verify(errors).reject("error.period");
    }


    @Test
    public void ensurePeriodMustBeGreaterThanOnlyOneDay() {

        // invalid period: 5.1.2013 - 5.1.2013

        form.setHolidaysAccountValidFrom(new DateMidnight(2013, 5, 1));
        form.setHolidaysAccountValidTo(new DateMidnight(2013, 5, 1));

        validator.validatePeriod(form, errors);

        Mockito.verify(errors).reject("error.period");
    }


    @Test
    public void ensurePeriodMustBeWithinTheProvidedYear() {

        form = new PersonForm(2014);

        form.setHolidaysAccountValidFrom(new DateMidnight(2013, 1, 1));
        form.setHolidaysAccountValidTo(new DateMidnight(2013, 5, 1));

        validator.validatePeriod(form, errors);

        Mockito.verify(errors).reject("error.period");
    }


    @Test
    public void ensureValidPeriodHasNoValidationError() {

        // valid period: 1.5.2013 - 5.5.2013

        form.setHolidaysAccountValidFrom(new DateMidnight(2013, 5, 1));
        form.setHolidaysAccountValidTo(new DateMidnight(2013, 5, 5));

        validator.validatePeriod(form, errors);

        Mockito.verifyZeroInteractions(errors);
    }


    // VALIDATION OF PERMISSIONS

    @Test
    public void ensureAtLeastOneRoleMustBeSelected() {

        form.setPermissions(new ArrayList<Role>());

        validator.validatePermissions(form, errors);

        Mockito.verify(errors).rejectValue("permissions", "role.error.least");
    }


    @Test
    public void ensureIfSelectedInactiveAsRoleNoOtherRoleCanBeSelected() {

        form.setPermissions(Arrays.asList(Role.INACTIVE, Role.USER));

        validator.validatePermissions(form, errors);

        Mockito.verify(errors).rejectValue("permissions", "role.error.inactive");
    }


    @Test
    public void ensureValidRoleSelectionHasNoValidationError() {

        form.setPermissions(Arrays.asList(Role.USER, Role.BOSS));

        validator.validatePermissions(form, errors);

        Mockito.verifyZeroInteractions(errors);
    }


    // VALIDATION OF MAIL NOTIFICATIONS

    @Test
    public void ensureBossMailNotificationIsOnlyValidIfBossRoleSelected() {

        form.setPermissions(Arrays.asList(Role.USER));
        form.setNotifications(Arrays.asList(MailNotification.NOTIFICATION_USER, MailNotification.NOTIFICATION_BOSS));

        validator.validateNotifications(form, errors);

        Mockito.verify(errors).rejectValue("notifications", "notification.error");
    }


    @Test
    public void ensureOfficeMailNotificationIsOnlyValidIfOfficeRoleSelected() {

        form.setPermissions(Arrays.asList(Role.USER, Role.BOSS));
        form.setNotifications(Arrays.asList(MailNotification.NOTIFICATION_USER, MailNotification.NOTIFICATION_BOSS,
                MailNotification.NOTIFICATION_OFFICE));

        validator.validateNotifications(form, errors);

        Mockito.verify(errors).rejectValue("notifications", "notification.error");
    }


    @Test
    public void ensureValidNotificationSelectionHasNoValidationError() {

        form.setPermissions(Arrays.asList(Role.USER, Role.BOSS));
        form.setNotifications(Arrays.asList(MailNotification.NOTIFICATION_USER, MailNotification.NOTIFICATION_BOSS));

        validator.validatePermissions(form, errors);

        Mockito.verifyZeroInteractions(errors);
    }
}
