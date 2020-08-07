package org.synyx.urlaubsverwaltung.account.web;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.math.BigDecimal;
import java.time.Instant;

import static java.time.LocalDate.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class AccountValidatorTest {

    private AccountValidator sut;

    @Mock
    private SettingsService settingsService;
    @Mock
    private Errors errors;

    private Settings settings;

    @Before
    public void setUp() {
        settings = new Settings();
        when(settingsService.getSettings()).thenReturn(settings);

        sut = new AccountValidator(settingsService);
    }

    @Test
    public void ensureSupportsOnlyAccountFormClass() {

        boolean returnValue;

        returnValue = sut.supports(null);
        assertThat(returnValue).isFalse();

        returnValue = sut.supports(Application.class);
        assertThat(returnValue).isFalse();

        returnValue = sut.supports(AccountForm.class);
        assertThat(returnValue).isTrue();
    }

    @Test
    public void ensureAnnualVacationMustNotBeNull() {
        final AccountForm form = new AccountForm(2013);
        form.setAnnualVacationDays(null);

        sut.validateAnnualVacation(form, errors);
        verify(errors).rejectValue("annualVacationDays", "error.entry.mandatory");
    }

    @Test
    public void ensureAnnualVacationMustNotBeGreaterThanMaximumDaysConfiguredInSettings() {
        int maxDays = 40;
        settings.getAbsenceSettings().setMaximumAnnualVacationDays(maxDays);

        final AccountForm form = new AccountForm(2013);
        form.setAnnualVacationDays(new BigDecimal(maxDays + 1));

        sut.validateAnnualVacation(form, errors);
        verify(errors).rejectValue("annualVacationDays", "error.entry.invalid");
    }

    @Test
    public void ensureValidAnnualVacationHasNoValidationError() {
        final AccountForm form = new AccountForm(2013);
        form.setAnnualVacationDays(new BigDecimal("28"));

        sut.validateAnnualVacation(form, errors);
        verifyZeroInteractions(errors);
    }

    @Test
    public void ensureActualVacationMustNotBeNull() {
        final AccountForm form = new AccountForm(2013);
        form.setActualVacationDays(null);
        sut.validateActualVacation(form, errors);
        verify(errors).rejectValue("actualVacationDays", "error.entry.mandatory");
    }

    @Test
    public void ensureActualVacationMustNotBeGreaterThanAnnualVacation() {
        final AccountForm form = new AccountForm(2013);
        form.setAnnualVacationDays(new BigDecimal("30"));
        form.setActualVacationDays(new BigDecimal("31"));

        sut.validateActualVacation(form, errors);
        verify(errors).rejectValue("actualVacationDays", "error.entry.invalid");
    }

    @Test
    public void ensureValidActualVacationHasNoValidationError() {
        final AccountForm form = new AccountForm(2013);
        form.setAnnualVacationDays(new BigDecimal("30"));
        form.setActualVacationDays(new BigDecimal("28"));

        sut.validateActualVacation(form, errors);
        verifyZeroInteractions(errors);
    }

    @Test
    public void ensureRemainingVacationDaysMustNotBeNull() {
        final AccountForm form = new AccountForm(2013);
        form.setRemainingVacationDays(null);

        sut.validateRemainingVacationDays(form, errors);
        verify(errors).rejectValue("remainingVacationDays", "error.entry.mandatory");
    }

    @Test
    public void ensureRemainingVacationDaysMustNotBeGreaterThanOneYear() {
        final AccountForm form = new AccountForm(2013);
        form.setRemainingVacationDays(new BigDecimal("367"));

        sut.validateRemainingVacationDays(form, errors);
        verify(errors).rejectValue("remainingVacationDays", "error.entry.invalid");
    }

    @Test
    public void ensureValidRemainingVacationDaysHaveNoValidationError() {
        final AccountForm form = new AccountForm(2013);
        form.setRemainingVacationDays(new BigDecimal("5"));
        form.setRemainingVacationDaysNotExpiring(new BigDecimal("5"));

        sut.validateRemainingVacationDays(form, errors);
        verifyZeroInteractions(errors);
    }

    @Test
    public void ensureRemainingVacationDaysNotExpiringMustNotBeNull() {
        final AccountForm form = new AccountForm(2013);
        form.setRemainingVacationDaysNotExpiring(null);

        sut.validateRemainingVacationDays(form, errors);
        verify(errors).rejectValue("remainingVacationDaysNotExpiring", "error.entry.mandatory");
    }

    @Test
    public void ensureRemainingVacationDaysNotExpiringMustNotBeGreaterThanRemainingVacationDays() {
        final AccountForm form = new AccountForm(2013);
        form.setRemainingVacationDays(new BigDecimal("5"));
        form.setRemainingVacationDaysNotExpiring(new BigDecimal("6"));

        sut.validateRemainingVacationDays(form, errors);
        verify(errors).rejectValue("remainingVacationDaysNotExpiring", "error.entry.invalid");
    }

    @Test
    public void ensureHolidaysAccountValidFromMustNotBeNull() {
        final AccountForm form = new AccountForm(2013);
        form.setHolidaysAccountValidFrom(null);

        sut.validatePeriod(form, errors);
        verify(errors).rejectValue("holidaysAccountValidFrom", "error.entry.mandatory");
    }

    @Test
    public void ensureHolidaysAccountValidToMustNotBeNull() {
        final AccountForm form = new AccountForm(2013);
        form.setHolidaysAccountValidTo(null);

        sut.validatePeriod(form, errors);
        verify(errors).rejectValue("holidaysAccountValidTo", "error.entry.mandatory");
    }

    @Test
    public void ensureFromOfPeriodMustBeBeforeTo() {
        final AccountForm form = new AccountForm(2013);
        form.setHolidaysAccountValidFrom(Instant.from(of(2013, 5, 1)));
        form.setHolidaysAccountValidTo(Instant.from(of(2013, 1, 1)));

        sut.validatePeriod(form, errors);
        verify(errors).reject("error.entry.invalidPeriod");
    }

    @Test
    public void ensurePeriodMustBeGreaterThanOnlyOneDay() {
        final AccountForm form = new AccountForm(2013);
        form.setHolidaysAccountValidFrom(Instant.from(of(2013, 5, 1)));
        form.setHolidaysAccountValidTo(Instant.from(of(2013, 5, 1)));

        sut.validatePeriod(form, errors);
        verify(errors).reject("error.entry.invalidPeriod");
    }

    @Test
    public void ensurePeriodMustBeWithinTheProvidedYear() {
        final AccountForm form = new AccountForm(2014);
        form.setHolidaysAccountValidFrom(Instant.from(of(2013, 1, 1)));
        form.setHolidaysAccountValidTo(Instant.from(of(2013, 5, 1)));

        sut.validatePeriod(form, errors);
        verify(errors).reject("error.entry.invalidPeriod");
    }

    @Test
    public void ensureValidPeriodHasNoValidationError() {
        final AccountForm form = new AccountForm(2013);
        form.setHolidaysAccountValidFrom(Instant.from(of(2013, 5, 1)));
        form.setHolidaysAccountValidTo(Instant.from(of(2013, 5, 5)));

        sut.validatePeriod(form, errors);
        verifyZeroInteractions(errors);
    }

    @Test
    public void ensureCommentHasNoValidationError() {
        final AccountForm form = new AccountForm(2017);
        form.setComment("blabla");

        sut.validateComment(form, errors);
        verifyZeroInteractions(errors);
    }

    @Test
    public void ensureCommentHasLengthValidationError() {
        final AccountForm form = new AccountForm(2017);
        form.setComment("blablablablablablablablablablablablablablablablablablablablablablablablablablablablablabla" +
            "blablablablablablablablablablablablablablablablablablablablablablablablablablablablablablablabla" +
            "blablablablablablablablablablablablablablablablablablablablablablablablablablablablablablablablabla" +
            "bla");

        sut.validateComment(form, errors);
        verify(errors).rejectValue("comment", "error.entry.commentTooLong");
    }
}
