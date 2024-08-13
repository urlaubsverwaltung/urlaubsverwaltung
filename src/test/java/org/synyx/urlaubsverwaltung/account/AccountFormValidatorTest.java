package org.synyx.urlaubsverwaltung.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
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

        sut.validateAnnualVacation(form, errors, BigDecimal.valueOf(40), this::getSettings);
        verify(errors).rejectValue("annualVacationDays", "error.entry.mandatory");
    }

    @Test
    void ensureAnnualVacationMustNotBeNegative() {

        final Settings settings = new Settings();
        settings.getApplicationSettings().setAllowHalfDays(true);
        when(settingsService.getSettings()).thenReturn(settings);

        final AccountForm form = new AccountForm(2013);
        form.setAnnualVacationDays(BigDecimal.valueOf(-1));

        sut.validateAnnualVacation(form, errors, BigDecimal.valueOf(40), this::getSettings);
        verify(errors).rejectValue("annualVacationDays", "error.entry.min", new Object[]{"0"}, "");
    }


    @ParameterizedTest
    @ValueSource(strings = {"0.000000000000000", "1", "39.000000000000000"})
    void ensureAnnualVacationMustBeIntegerIfHalfDayIsNotActive(final BigDecimal input) {

        final Settings settings = new Settings();
        settings.getApplicationSettings().setAllowHalfDays(false);
        when(settingsService.getSettings()).thenReturn(settings);

        final AccountForm form = new AccountForm(2013);
        form.setAnnualVacationDays(input);

        sut.validateAnnualVacation(form, errors, BigDecimal.valueOf(40), this::getSettings);
        verifyNoInteractions(errors);
    }

    @ParameterizedTest
    @ValueSource(strings = {"10.000000000000009", "11.000000000000001"})
    void ensureAnnualVacationCannotBeNotIntegerIfHalfDayIsNotActive(final BigDecimal input) {

        final Settings settings = new Settings();
        settings.getApplicationSettings().setAllowHalfDays(false);
        when(settingsService.getSettings()).thenReturn(settings);

        final AccountForm form = new AccountForm(2013);
        form.setAnnualVacationDays(input);

        sut.validateAnnualVacation(form, errors, BigDecimal.valueOf(40), this::getSettings);
        verify(errors).rejectValue("annualVacationDays", "error.entry.integer");
    }

    @ParameterizedTest
    @ValueSource(strings = {"10", "10.500000000000000"})
    void ensureAnnualVacationMustBeFullOrHalfDaysIfHalfDayIsActive(final BigDecimal input) {

        final Settings settings = new Settings();
        settings.getApplicationSettings().setAllowHalfDays(true);
        when(settingsService.getSettings()).thenReturn(settings);

        final AccountForm form = new AccountForm(2013);
        form.setAnnualVacationDays(input);

        sut.validateAnnualVacation(form, errors, BigDecimal.valueOf(40), this::getSettings);
        verifyNoInteractions(errors);
    }

    @ParameterizedTest
    @ValueSource(strings = {"10.400000000000009", "10.500000000000001"})
    void ensureAnnualVacationCannotBeNotFullOrHalfDaysIfHalfDayIsActive(final BigDecimal input) {

        final Settings settings = new Settings();
        settings.getApplicationSettings().setAllowHalfDays(true);
        when(settingsService.getSettings()).thenReturn(settings);

        final AccountForm form = new AccountForm(2013);
        form.setAnnualVacationDays(input);

        sut.validateAnnualVacation(form, errors, BigDecimal.valueOf(40), this::getSettings);
        verify(errors).rejectValue("annualVacationDays", "error.entry.fullOrHalfNumber");
    }

    @Test
    void ensureAnnualVacationMustNotBeGreaterThanMaximumDaysConfiguredInSettings() {

        final Settings settings = new Settings();
        settings.getApplicationSettings().setAllowHalfDays(true);
        when(settingsService.getSettings()).thenReturn(settings);

        int maxDays = 40;
        final AccountForm form = new AccountForm(2013);
        form.setAnnualVacationDays(new BigDecimal(maxDays + 1));

        sut.validateAnnualVacation(form, errors, BigDecimal.valueOf(maxDays), this::getSettings);
        verify(errors).rejectValue("annualVacationDays", "error.entry.max", new Object[]{BigDecimal.valueOf(40)}, "");
    }

    @Test
    void ensureActualVacationMustNotBeNull() {
        final AccountForm form = new AccountForm(2013);
        form.setActualVacationDays(null);
        sut.validateActualVacation(form, errors);
        verify(errors).rejectValue("actualVacationDays", "error.entry.mandatory");
    }

    @Test
    void ensureActualVacationMustNotBeNegative() {
        final AccountForm form = new AccountForm(2013);
        form.setActualVacationDays(BigDecimal.valueOf(-1));

        sut.validateActualVacation(form, errors);
        verify(errors).rejectValue("actualVacationDays", "error.entry.min", new Object[]{"0"}, "");
    }

    @Test
    void ensureActualVacationMustNotBeGreaterThanAnnualVacation() {
        final AccountForm form = new AccountForm(2013);
        form.setAnnualVacationDays(new BigDecimal("30"));
        form.setActualVacationDays(new BigDecimal("31"));

        sut.validateActualVacation(form, errors);
        verify(errors).rejectValue("actualVacationDays", "error.entry.max", new Object[]{BigDecimal.valueOf(30)}, "");
    }

    private static Stream<Arguments> wrongAnnualAndActualVacationDays() {
        return Stream.of(
            Arguments.of("11", "10.1"),
            Arguments.of("11", "10.000000000000009"),
            Arguments.of("11", "11.000000000000001")
        );
    }

    @ParameterizedTest
    @MethodSource("wrongAnnualAndActualVacationDays")
    void ensureActualVacationMustBeIntegerOrHalf(final BigDecimal annualVacationDays, final BigDecimal actualVacationDays) {

        final AccountForm form = new AccountForm(2013);
        form.setAnnualVacationDays(annualVacationDays);
        form.setActualVacationDays(actualVacationDays);

        sut.validateActualVacation(form, errors);
        verify(errors).rejectValue("actualVacationDays", "error.entry.fullOrHalfNumber");
    }

    private static Stream<Arguments> annualAndActualSource() {
        return Stream.of(
            Arguments.of("30", "28"),
            Arguments.of("30.0", "28.0"),
            Arguments.of("30", "28.5")
        );
    }

    @ParameterizedTest
    @MethodSource("annualAndActualSource")
    void ensureValidActualVacationHasNoValidationErrorForFullHour(final BigDecimal annualVacationDay, final BigDecimal actualVacationDay) {
        final AccountForm form = new AccountForm(2013);
        form.setAnnualVacationDays(annualVacationDay);
        form.setActualVacationDays(actualVacationDay);

        sut.validateActualVacation(form, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureRemainingVacationDaysMustNotBeNull() {
        final AccountForm form = new AccountForm(2013);
        form.setRemainingVacationDays(null);

        sut.validateRemainingVacationDays(form, errors);
        verify(errors).rejectValue("remainingVacationDays", "error.entry.mandatory");
    }

    @Test
    void ensureRemainingVacationDaysMustNotBeNegative() {
        final AccountForm form = new AccountForm(2013);
        form.setRemainingVacationDays(BigDecimal.valueOf(-1));

        sut.validateRemainingVacationDays(form, errors);
        verify(errors).rejectValue("remainingVacationDays", "error.entry.min", new Object[]{"0"}, "");
    }

    @Test
    void ensureRemainingVacationDaysMustBeFullOrHalf() {
        final AccountForm form = new AccountForm(2013);
        form.setRemainingVacationDays(new BigDecimal("10.3"));
        form.setRemainingVacationDaysNotExpiring(new BigDecimal(11));

        sut.validateRemainingVacationDays(form, errors);
        verify(errors).rejectValue("remainingVacationDays", "error.entry.fullOrHalfNumber");
    }

    @Test
    void ensureRemainingVacationDaysNotExpiringMustBeNegative() {
        final AccountForm form = new AccountForm(2013);
        form.setRemainingVacationDays(BigDecimal.valueOf(10));
        form.setRemainingVacationDaysNotExpiring(BigDecimal.valueOf(-1));

        sut.validateRemainingVacationDaysNotExpiring(form, errors);
        verify(errors).rejectValue("remainingVacationDaysNotExpiring", "error.entry.min", new Object[]{"0"}, "");
    }

    @Test
    void ensureRemainingVacationDaysNotExpiringMustNotBeNull() {
        final AccountForm form = new AccountForm(2013);
        form.setRemainingVacationDaysNotExpiring(null);

        sut.validateRemainingVacationDaysNotExpiring(form, errors);
        verify(errors).rejectValue("remainingVacationDaysNotExpiring", "error.entry.mandatory");
    }

    @Test
    void ensureRemainingVacationDaysNotExpiringMustNotBeNullEvenIfVacationDaysAreNotNull() {
        final AccountForm form = new AccountForm(2013);
        form.setRemainingVacationDays(BigDecimal.valueOf(10));
        form.setRemainingVacationDaysNotExpiring(null);

        sut.validateRemainingVacationDaysNotExpiring(form, errors);
        verify(errors).rejectValue("remainingVacationDaysNotExpiring", "error.entry.mandatory");
    }

    @Test
    void ensureRemainingVacationDaysNotExpiringMustBeFullOrHalf() {
        final AccountForm form = new AccountForm(2013);
        form.setRemainingVacationDays(new BigDecimal(10));
        form.setRemainingVacationDaysNotExpiring(new BigDecimal("10.3"));

        sut.validateRemainingVacationDaysNotExpiring(form, errors);
        verify(errors).rejectValue("remainingVacationDaysNotExpiring", "error.entry.fullOrHalfNumber");
    }

    @Test
    void ensureRemainingVacationDaysNotExpiringMustNotBeGreaterThanRemainingVacationDays() {
        final AccountForm form = new AccountForm(2013);
        form.setRemainingVacationDays(new BigDecimal("5"));
        form.setRemainingVacationDaysNotExpiring(new BigDecimal("6"));

        sut.validateRemainingVacationDaysNotExpiring(form, errors);
        verify(errors).rejectValue("remainingVacationDaysNotExpiring", "person.form.annualVacation.error.remainingVacationDaysNotExpiring.tooBig", new Object[]{BigDecimal.valueOf(5)}, "");
    }

    private static Stream<Arguments> remainingVacationDaysAndNotExpiring() {
        return Stream.of(
            Arguments.of("5", "5"),
            Arguments.of("5.0", "5.0"),
            Arguments.of("5.5", "5.5")
        );
    }

    @ParameterizedTest
    @MethodSource("remainingVacationDaysAndNotExpiring")
    void ensureValidRemainingVacationDaysHaveNoValidationErrorForFullHour(final BigDecimal remainingVacationDays, final BigDecimal remainingVacationDaysNotExpiring) {
        final AccountForm form = new AccountForm(2013);
        form.setRemainingVacationDays(remainingVacationDays);
        form.setRemainingVacationDaysNotExpiring(remainingVacationDaysNotExpiring);

        sut.validateRemainingVacationDays(form, errors);
        verifyNoInteractions(errors);
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
    void ensureInvalidPeriodForToDateAfterFromDate() {
        final AccountForm form = new AccountForm(2021);
        form.setHolidaysAccountValidFrom(LocalDate.of(2021, Month.MARCH, 1));
        form.setHolidaysAccountValidTo(LocalDate.of(2021, Month.JANUARY, 1));

        sut.validatePeriod(form, errors);
        verify(errors).rejectValue("holidaysAccountValidTo", "person.form.annualVacation.error.holidaysAccountValidTo.invalidRangeReversed");
        verify(errors).rejectValue("holidaysAccountValidFrom", "person.form.annualVacation.error.holidaysAccountValidFrom.invalidRangeReversed");
    }

    @Test
    void ensureInvalidPeriodForToDateEqualsFromDate() {
        final AccountForm form = new AccountForm(2021);
        form.setHolidaysAccountValidFrom(LocalDate.of(2021, Month.MARCH, 1));
        form.setHolidaysAccountValidTo(LocalDate.of(2021, Month.MARCH, 1));

        sut.validatePeriod(form, errors);
        verify(errors).rejectValue("holidaysAccountValidTo", "person.form.annualVacation.error.holidaysAccountValidTo.invalidRange");
        verify(errors).rejectValue("holidaysAccountValidFrom", "person.form.annualVacation.error.holidaysAccountValidFrom.invalidRange");
    }

    @Test
    void ensureInvalidPeriodForFromDateWrongYear() {
        final AccountForm form = new AccountForm(2021);
        form.setHolidaysAccountValidFrom(LocalDate.of(2022, Month.MARCH, 1));
        form.setHolidaysAccountValidTo(LocalDate.of(2021, Month.MARCH, 1));

        sut.validatePeriod(form, errors);
        verify(errors).rejectValue("holidaysAccountValidFrom", "person.form.annualVacation.error.holidaysAccountValidFrom.invalidYear", new Object[]{"2021"}, "");
    }

    @Test
    void ensureInvalidPeriodForToDateWringYear() {
        final AccountForm form = new AccountForm(2021);
        form.setHolidaysAccountValidFrom(LocalDate.of(2021, Month.MARCH, 1));
        form.setHolidaysAccountValidTo(LocalDate.of(2020, Month.MARCH, 1));

        sut.validatePeriod(form, errors);
        verify(errors).rejectValue("holidaysAccountValidTo", "person.form.annualVacation.error.holidaysAccountValidTo.invalidYear", new Object[]{"2021"}, "");
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
    void ensureHolidaysAccountExpiryDateMustNotBeNullWhenGlobalExpiryIsDisabledAndLocalExpiryEnabled() {

        final Settings settings = new Settings();
        settings.getAccountSettings().setDoRemainingVacationDaysExpireGlobally(false);
        when(settingsService.getSettings()).thenReturn(settings);

        final AccountForm form = new AccountForm(2013);
        form.setDoRemainingVacationDaysExpireLocally(true);
        form.setExpiryDateLocally(null);

        sut.validateExpiryDateLocally(form, errors, this::getSettings);
        verify(errors).rejectValue("expiryDateLocally", "error.entry.mandatory");
    }

    @Test
    void ensureHolidaysAccountExpiryDateCanBeNull() {

        mockExpiryDateGlobal(LocalDate.of(2013, 2, 1));

        final AccountForm form = new AccountForm(2013);
        form.setDoRemainingVacationDaysExpireLocally(true);
        form.setExpiryDateLocally(null);

        sut.validateExpiryDateLocally(form, errors, this::getSettings);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureInvalidExpiryDateWrongYear() {

        mockExpiryDateGlobal(LocalDate.of(2021, 4, 1));

        final AccountForm form = new AccountForm(2021);
        form.setExpiryDateLocally(LocalDate.of(2022, Month.MARCH, 1));

        sut.validateExpiryDateLocally(form, errors, this::getSettings);
        verify(errors).rejectValue("expiryDateLocally", "person.form.annualVacation.error.expiryDateLocally.invalidYear", new Object[]{"2021"}, "");
    }

    @Test
    void ensureExpiryDateHasNoValidationError() {

        mockExpiryDateGlobal(LocalDate.of(2013, 4, 1));

        final AccountForm form = new AccountForm(2013);
        form.setDoRemainingVacationDaysExpireLocally(true);
        form.setExpiryDateLocally(LocalDate.of(2013, 5, 1));

        sut.validateExpiryDateLocally(form, errors, this::getSettings);
        verifyNoInteractions(errors);
    }

    private Settings getSettings() {
        return settingsService.getSettings();
    }

    private void mockExpiryDateGlobal(LocalDate date) {
        final Settings settings = new Settings();
        settings.getAccountSettings().setDoRemainingVacationDaysExpireGlobally(true);
        settings.getAccountSettings().setExpiryDateDayOfMonth(date.getDayOfMonth());
        settings.getAccountSettings().setExpiryDateMonth(date.getMonth());
        when(settingsService.getSettings()).thenReturn(settings);
    }
}
