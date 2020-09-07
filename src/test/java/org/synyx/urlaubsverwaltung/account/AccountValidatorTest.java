package org.synyx.urlaubsverwaltung.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.math.BigDecimal;

import static java.time.LocalDate.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class AccountValidatorTest {

    private AccountValidator sut;

    @Mock
    private SettingsService settingsService;
    @Mock
    private Errors errors;

    @BeforeEach
    void setUp() {
        sut = new AccountValidator(settingsService);
    }

    @Test
    void ensureSupportsOnlyAccountFormClass() {

        boolean returnValue;

        returnValue = sut.supports(null);
        assertThat(returnValue).isFalse();

        returnValue = sut.supports(Application.class);
        assertThat(returnValue).isFalse();

        returnValue = sut.supports(AccountForm.class);
        assertThat(returnValue).isTrue();
    }

    @Test
    void ensureAnnualVacationMustNotBeNull() {
        final Settings settings = new Settings();
        when(settingsService.getSettings()).thenReturn(settings);

        final AccountForm form = new AccountForm(2013);
        form.setAnnualVacationDays(null);

        sut.validateAnnualVacation(form, errors);
        verify(errors).rejectValue("annualVacationDays", "error.entry.mandatory");
    }

    @Test
    void ensureAnnualVacationMustNotBeGreaterThanMaximumDaysConfiguredInSettings() {

        final Settings settings = new Settings();
        when(settingsService.getSettings()).thenReturn(settings);

        int maxDays = 40;
        settings.getAbsenceSettings().setMaximumAnnualVacationDays(maxDays);

        final AccountForm form = new AccountForm(2013);
        form.setAnnualVacationDays(new BigDecimal(maxDays + 1));

        sut.validateAnnualVacation(form, errors);
        verify(errors).rejectValue("annualVacationDays", "error.entry.invalid");
    }

    @Test
    void ensureValidAnnualVacationHasNoValidationError() {
        final Settings settings = new Settings();
        when(settingsService.getSettings()).thenReturn(settings);

        final AccountForm form = new AccountForm(2013);
        form.setAnnualVacationDays(new BigDecimal("28"));

        sut.validateAnnualVacation(form, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureActualVacationMustNotBeNull() {
        final AccountForm form = new AccountForm(2013);
        form.setActualVacationDays(null);
        sut.validateActualVacation(form, errors);
        verify(errors).rejectValue("actualVacationDays", "error.entry.mandatory");
    }

    @Test
    void ensureActualVacationMustNotBeGreaterThanAnnualVacation() {
        final AccountForm form = new AccountForm(2013);
        form.setAnnualVacationDays(new BigDecimal("30"));
        form.setActualVacationDays(new BigDecimal("31"));

        sut.validateActualVacation(form, errors);
        verify(errors).rejectValue("actualVacationDays", "error.entry.invalid");
    }

    @Test
    void ensureValidActualVacationHasNoValidationError() {
        final AccountForm form = new AccountForm(2013);
        form.setAnnualVacationDays(new BigDecimal("30"));
        form.setActualVacationDays(new BigDecimal("28"));

        sut.validateActualVacation(form, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureRemainingVacationDaysMustNotBeNull() {
        final Settings settings = new Settings();
        when(settingsService.getSettings()).thenReturn(settings);

        final AccountForm form = new AccountForm(2013);
        form.setRemainingVacationDays(null);

        sut.validateRemainingVacationDays(form, errors);
        verify(errors).rejectValue("remainingVacationDays", "error.entry.mandatory");
    }

    @Test
    void ensureRemainingVacationDaysMustNotBeGreaterThanOneYear() {
        final Settings settings = new Settings();
        when(settingsService.getSettings()).thenReturn(settings);

        final AccountForm form = new AccountForm(2013);
        form.setRemainingVacationDays(new BigDecimal("367"));

        sut.validateRemainingVacationDays(form, errors);
        verify(errors).rejectValue("remainingVacationDays", "error.entry.invalid");
    }

    @Test
    void ensureValidRemainingVacationDaysHaveNoValidationError() {
        final Settings settings = new Settings();
        when(settingsService.getSettings()).thenReturn(settings);

        final AccountForm form = new AccountForm(2013);
        form.setRemainingVacationDays(new BigDecimal("5"));
        form.setRemainingVacationDaysNotExpiring(new BigDecimal("5"));

        sut.validateRemainingVacationDays(form, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureRemainingVacationDaysNotExpiringMustNotBeNull() {
        final Settings settings = new Settings();
        when(settingsService.getSettings()).thenReturn(settings);

        final AccountForm form = new AccountForm(2013);
        form.setRemainingVacationDaysNotExpiring(null);

        sut.validateRemainingVacationDays(form, errors);
        verify(errors).rejectValue("remainingVacationDaysNotExpiring", "error.entry.mandatory");
    }

    @Test
    void ensureRemainingVacationDaysNotExpiringMustNotBeGreaterThanRemainingVacationDays() {
        final Settings settings = new Settings();
        when(settingsService.getSettings()).thenReturn(settings);

        final AccountForm form = new AccountForm(2013);
        form.setRemainingVacationDays(new BigDecimal("5"));
        form.setRemainingVacationDaysNotExpiring(new BigDecimal("6"));

        sut.validateRemainingVacationDays(form, errors);
        verify(errors).rejectValue("remainingVacationDaysNotExpiring", "error.entry.invalid");
    }

    @Test
    void ensureHolidaysAccountValidFromMustNotBeNull() {
        final AccountForm form = new AccountForm(2013);
        form.setHolidaysAccountValidFrom(null);

        sut.validatePeriod(form, errors);
        verify(errors).rejectValue("holidaysAccountValidFrom", "error.entry.mandatory");
    }

    @Test
    void ensureHolidaysAccountValidToMustNotBeNull() {
        final AccountForm form = new AccountForm(2013);
        form.setHolidaysAccountValidTo(null);

        sut.validatePeriod(form, errors);
        verify(errors).rejectValue("holidaysAccountValidTo", "error.entry.mandatory");
    }

    @Test
    void ensureFromOfPeriodMustBeBeforeTo() {
        final AccountForm form = new AccountForm(2013);
        form.setHolidaysAccountValidFrom(of(2013, 5, 1));
        form.setHolidaysAccountValidTo(of(2013, 1, 1));

        sut.validatePeriod(form, errors);
        verify(errors).reject("error.entry.invalidPeriod");
    }

    @Test
    void ensurePeriodMustBeGreaterThanOnlyOneDay() {
        final AccountForm form = new AccountForm(2013);
        form.setHolidaysAccountValidFrom(of(2013, 5, 1));
        form.setHolidaysAccountValidTo(of(2013, 5, 1));

        sut.validatePeriod(form, errors);
        verify(errors).reject("error.entry.invalidPeriod");
    }

    @Test
    void ensurePeriodMustBeWithinTheProvidedYear() {
        final AccountForm form = new AccountForm(2014);
        form.setHolidaysAccountValidFrom(of(2013, 1, 1));
        form.setHolidaysAccountValidTo(of(2013, 5, 1));

        sut.validatePeriod(form, errors);
        verify(errors).reject("error.entry.invalidPeriod");
    }

    @Test
    void ensureValidPeriodHasNoValidationError() {
        final AccountForm form = new AccountForm(2013);
        form.setHolidaysAccountValidFrom(of(2013, 5, 1));
        form.setHolidaysAccountValidTo(of(2013, 5, 5));

        sut.validatePeriod(form, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureCommentHasNoValidationError() {
        final AccountForm form = new AccountForm(2017);
        form.setComment("blabla");

        sut.validateComment(form, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureCommentHasLengthValidationError() {
        final AccountForm form = new AccountForm(2017);
        form.setComment("blablablablablablablablablablablablablablablablablablablablablablablablablablablablablabla" +
            "blablablablablablablablablablablablablablablablablablablablablablablablablablablablablablablabla" +
            "blablablablablablablablablablablablablablablablablablablablablablablablablablablablablablablablabla" +
            "bla");

        sut.validateComment(form, errors);
        verify(errors).rejectValue("comment", "error.entry.commentTooLong");
    }
}
