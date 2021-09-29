package org.synyx.urlaubsverwaltung.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

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

        sut.validateAnnualVacation(form, errors, BigDecimal.valueOf(40));
        verify(errors).rejectValue("annualVacationDays", "error.entry.mandatory");
    }

    @Test
    void ensureAnnualVacationMustNotBeNegative() {
        final AccountForm form = new AccountForm(2013);
        form.setAnnualVacationDays(BigDecimal.valueOf(-1));

        sut.validateAnnualVacation(form, errors, BigDecimal.valueOf(40));
        verify(errors).rejectValue("annualVacationDays", "error.entry.min", new Object[]{"0"}, "");
    }

    @Test
    void ensureAnnualVacationMustBeInteger() {
        final AccountForm form = new AccountForm(2013);
        form.setAnnualVacationDays(BigDecimal.valueOf(10.1));

        sut.validateAnnualVacation(form, errors, BigDecimal.valueOf(40));
        verify(errors).rejectValue("annualVacationDays", "error.entry.integer");
    }

    @Test
    void ensureAnnualVacationMustNotBeGreaterThanMaximumDaysConfiguredInSettings() {

        int maxDays = 40;
        final AccountForm form = new AccountForm(2013);
        form.setAnnualVacationDays(new BigDecimal(maxDays + 1));

        sut.validateAnnualVacation(form, errors, BigDecimal.valueOf(maxDays));
        verify(errors).rejectValue("annualVacationDays", "error.entry.max", new Object[]{"40"}, "");
    }

    @Test
    void ensureValidAnnualVacationHasNoValidationError() {
        final AccountForm form = new AccountForm(2013);
        form.setAnnualVacationDays(new BigDecimal("28"));

        sut.validateAnnualVacation(form, errors, BigDecimal.valueOf(40));
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
        verify(errors).rejectValue("actualVacationDays", "error.entry.max", new Object[]{"30"}, "");
    }

    @Test
    void ensureActualVacationMustBeIntegerOrHalf() {

        final AccountForm form = new AccountForm(2013);
        form.setAnnualVacationDays(BigDecimal.valueOf(11));
        form.setActualVacationDays(BigDecimal.valueOf(10.1));

        sut.validateActualVacation(form, errors);
        verify(errors).rejectValue("actualVacationDays", "error.entry.fullOrHalfNumber");
    }

    @Test
    void ensureValidActualVacationHasNoValidationErrorForFullHour() {
        final AccountForm form = new AccountForm(2013);
        form.setAnnualVacationDays(new BigDecimal("30"));
        form.setActualVacationDays(new BigDecimal("28"));

        sut.validateActualVacation(form, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureValidActualVacationWithDecimalHasNoValidationErrorForFullHour() {
        final AccountForm form = new AccountForm(2013);
        form.setAnnualVacationDays(new BigDecimal("30.0"));
        form.setActualVacationDays(new BigDecimal("28.0"));

        sut.validateActualVacation(form, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureValidActualVacationHasNoValidationErrorForHalfAnHour() {
        final AccountForm form = new AccountForm(2013);
        form.setAnnualVacationDays(new BigDecimal("30"));
        form.setActualVacationDays(new BigDecimal("28.5"));

        sut.validateActualVacation(form, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureRemainingVacationDaysMustNotBeNull() {
        final AccountForm form = new AccountForm(2013);
        form.setRemainingVacationDays(null);

        sut.validateRemainingVacationDays(form, errors, BigDecimal.valueOf(40));
        verify(errors).rejectValue("remainingVacationDays", "error.entry.mandatory");
    }

    @Test
    void ensureRemainingVacationDaysMustNotBeNegative() {
        final AccountForm form = new AccountForm(2013);
        form.setRemainingVacationDays(BigDecimal.valueOf(-1));

        sut.validateRemainingVacationDays(form, errors, BigDecimal.valueOf(40));
        verify(errors).rejectValue("remainingVacationDays", "error.entry.min", new Object[]{"0"}, "");
    }

    @Test
    void ensureRemainingVacationDaysMustBeFullOrHalf() {
        final AccountForm form = new AccountForm(2013);
        form.setRemainingVacationDays(new BigDecimal("10.3"));
        form.setRemainingVacationDaysNotExpiring(new BigDecimal(11));

        sut.validateRemainingVacationDays(form, errors, BigDecimal.valueOf(40));
        verify(errors).rejectValue("remainingVacationDays", "error.entry.fullOrHalfNumber");
    }

    @Test
    void ensureRemainingVacationDaysMustNotBeGreaterThanGivenMax() {
        final AccountForm form = new AccountForm(2013);
        form.setRemainingVacationDays(new BigDecimal("41"));
        form.setRemainingVacationDaysNotExpiring(new BigDecimal(10));

        sut.validateRemainingVacationDays(form, errors, BigDecimal.valueOf(40));
        verify(errors).rejectValue("remainingVacationDays", "person.form.annualVacation.error.remainingVacationDays.tooBig", new Object[]{"40"}, "");
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
        verify(errors).rejectValue("remainingVacationDaysNotExpiring", "person.form.annualVacation.error.remainingVacationDaysNotExpiring.tooBig", new Object[]{"5"}, "");
    }

    @Test
    void ensureValidRemainingVacationDaysHaveNoValidationErrorForFullHour() {
        final AccountForm form = new AccountForm(2013);
        form.setRemainingVacationDays(new BigDecimal("5"));
        form.setRemainingVacationDaysNotExpiring(new BigDecimal("5"));

        sut.validateRemainingVacationDays(form, errors, BigDecimal.valueOf(40));
        verifyNoInteractions(errors);
    }

    @Test
    void ensureValidRemainingVacationDaysWithDecimalHaveNoValidationErrorForFullHour() {
        final AccountForm form = new AccountForm(2013);
        form.setRemainingVacationDays(new BigDecimal("5.0"));
        form.setRemainingVacationDaysNotExpiring(new BigDecimal("5.0"));

        sut.validateRemainingVacationDays(form, errors, BigDecimal.valueOf(40));
        verifyNoInteractions(errors);
    }

    @Test
    void ensureValidRemainingVacationDaysHaveNoValidationErrorForHalfAnHour() {
        final AccountForm form = new AccountForm(2013);
        form.setRemainingVacationDays(new BigDecimal("5.5"));
        form.setRemainingVacationDaysNotExpiring(new BigDecimal("5.5"));

        sut.validateRemainingVacationDays(form, errors, BigDecimal.valueOf(40));
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
    void ensureCommentHasNoValidationError() {
        final AccountForm form = new AccountForm(2017);
        form.setComment("blabla");

        sut.validateComment(form, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureCommentWithNullHasNoValidationError() {
        final AccountForm form = new AccountForm(2017);
        form.setComment(null);

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
