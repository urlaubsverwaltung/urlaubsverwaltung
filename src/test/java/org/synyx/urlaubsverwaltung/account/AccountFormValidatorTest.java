package org.synyx.urlaubsverwaltung.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class AccountFormValidatorTest {

    private AccountFormValidator sut;

    @Mock
    private SettingsService settingsService;
    @Mock
    private Errors errors;

    @BeforeEach
    void setUp() {
        sut = new AccountFormValidator(settingsService);
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

        final AccountForm form = new AccountForm(2013);
        form.setAnnualVacationDays(null);

        sut.validateAnnualVacation(form, errors);
        verify(errors).rejectValue("annualVacationDays", "error.entry.mandatory");
    }

    @Test
    void ensureAnnualVacationMustBeInteger() {

        final AccountForm form = new AccountForm(2013);
        form.setAnnualVacationDays(BigDecimal.valueOf(10.1));

        sut.validateAnnualVacation(form, errors);
        verify(errors).rejectValue("annualVacationDays", "error.entry.integer");
    }

    @Test
    void ensureAnnualVacationMustNotBeGreaterThanMaximumDaysConfiguredInSettings() {

        final Settings settings = new Settings();
        when(settingsService.getSettings()).thenReturn(settings);

        int maxDays = 40;
        settings.getAccountSettings().setMaximumAnnualVacationDays(maxDays);

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
    void ensureActualVacationMustBeIntegerOrHalf() {

        final AccountForm form = new AccountForm(2013);
        form.setAnnualVacationDays(BigDecimal.valueOf(11));
        form.setActualVacationDays(BigDecimal.valueOf(10.1));

        sut.validateActualVacation(form, errors);
        verify(errors).rejectValue("actualVacationDays", "error.entry.fullOrHalfHour");
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
    void ensureRemainingVacationDaysMustBeFullOrHalf() {
        final Settings settings = new Settings();
        when(settingsService.getSettings()).thenReturn(settings);

        final AccountForm form = new AccountForm(2013);
        form.setRemainingVacationDays(new BigDecimal(10.3));
        form.setRemainingVacationDaysNotExpiring(new BigDecimal(11));

        sut.validateRemainingVacationDays(form, errors);
        verify(errors).rejectValue("remainingVacationDays", "error.entry.fullOrHalfHour");
    }

    @Test
    void ensureRemainingVacationDaysNotExpiringMustBeFullOrHalf() {
        final Settings settings = new Settings();
        when(settingsService.getSettings()).thenReturn(settings);

        final AccountForm form = new AccountForm(2013);
        form.setRemainingVacationDays(new BigDecimal(10));
        form.setRemainingVacationDaysNotExpiring(new BigDecimal(10.3));

        sut.validateRemainingVacationDays(form, errors);
        verify(errors).rejectValue("remainingVacationDaysNotExpiring", "error.entry.fullOrHalfHour");
    }

    @Test
    void ensureRemainingVacationDaysMustNotBeGreaterThanOneYear() {
        final Settings settings = new Settings();
        when(settingsService.getSettings()).thenReturn(settings);

        final AccountForm form = new AccountForm(2013);
        form.setRemainingVacationDays(new BigDecimal("367"));
        form.setRemainingVacationDaysNotExpiring(new BigDecimal(10));

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

    private static Stream<Arguments> fromDateBeforeToDate() {
        return Stream.of(
            Arguments.of(2013, LocalDate.of(2013, 5, 1), LocalDate.of(2013, 1, 1)),
            Arguments.of(2013, LocalDate.of(2013, 5, 1), LocalDate.of(2013, 5, 1)),
            Arguments.of(2014, LocalDate.of(2013, 1, 1), LocalDate.of(2013, 5, 1))
        );
    }

    @ParameterizedTest
    @MethodSource("fromDateBeforeToDate")
    void ensureInvalidPeriod(int year, LocalDate validFrom, LocalDate validTo) {
        final AccountForm form = new AccountForm(year);
        form.setHolidaysAccountValidFrom(validFrom);
        form.setHolidaysAccountValidTo(validTo);

        sut.validatePeriod(form, errors);
        verify(errors).reject("error.entry.invalidPeriod");
    }

    @Test
    void ensureValidPeriodHasNoValidationError() {
        final AccountForm form = new AccountForm(2013);
        form.setHolidaysAccountValidFrom(LocalDate.of(2013, 5, 1));
        form.setHolidaysAccountValidTo(LocalDate.of(2013, 5, 5));

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
