package org.synyx.urlaubsverwaltung.companyclosure.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.blackoutperiod.BlackoutPeriod;
import org.synyx.urlaubsverwaltung.blackoutperiod.BlackoutPeriodService;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureEntry;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureService;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureVersion;
import org.synyx.urlaubsverwaltung.period.DayLength;

import java.time.Clock;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static java.time.Month.DECEMBER;
import static java.util.Locale.ENGLISH;
import static java.util.Locale.GERMAN;
import static java.time.Month.JANUARY;
import static java.time.Month.NOVEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;
import static org.synyx.urlaubsverwaltung.period.DayLength.ZERO;

@ExtendWith(MockitoExtension.class)
class CompanyClosureFormValidatorTest {

    private CompanyClosureFormValidator sut;

    @Mock
    private CompanyClosureService companyClosureService;

    @Mock
    private BlackoutPeriodService blackoutPeriodService;

    @BeforeEach
    void setUp() {
        final Clock clock = Clock.fixed(LocalDate.of(2026, NOVEMBER, 6).atStartOfDay(ZoneId.of("UTC")).toInstant(), ZoneId.of("UTC"));
        sut = new CompanyClosureFormValidator(companyClosureService, blackoutPeriodService, clock);
    }

    @Test
    void supportsCompanyClosureFormDto() {
        assertThat(sut.supports(CompanyClosureFormDto.class)).isTrue();
        assertThat(sut.supports(Object.class)).isFalse();
    }

    @Test
    void acceptsValidForm() {
        final Errors errors = validate(validForm());

        assertThat(errors.hasErrors()).isFalse();
    }

    @Test
    void rejectsValidFromInPreviousYear() {
        final CompanyClosureFormDto form = validForm();
        form.setValidFrom(LocalDate.of(2025, DECEMBER, 31));

        final Errors errors = validate(form);

        assertThat(errors.getFieldError("validFrom").getCode()).isEqualTo("company-closure.error.validFrom.previousYear");
    }

    @Test
    void acceptsFirstOfCurrentYear() {
        final CompanyClosureFormDto form = validForm();
        form.setValidFrom(LocalDate.of(2026, JANUARY, 1));

        assertThat(validate(form).hasErrors()).isFalse();
    }

    @Test
    void rejectsMissingValidFrom() {
        final CompanyClosureFormDto form = validForm();
        form.setValidFrom(null);

        assertThat(validate(form).getFieldError("validFrom").getCode()).isEqualTo("error.entry.mandatory");
    }

    @Test
    void rejectsMissingDayLength() {
        final CompanyClosureFormDto form = validForm();
        form.setDayLength(null);

        assertThat(validate(form).getFieldError("dayLength").getCode()).isEqualTo("error.entry.mandatory");
    }

    @Test
    void rejectsBlankNamesInEveryLanguage() {
        final CompanyClosureFormDto form = validForm();
        form.setNames(List.of(new CompanyClosureNameFormDto(GERMAN, "  "), new CompanyClosureNameFormDto(ENGLISH, "")));

        assertThat(validate(form).getFieldError("names").getCode()).isEqualTo("company-closure.error.names.mandatory");
    }

    @Test
    void rejectsMissingNames() {
        final CompanyClosureFormDto form = validForm();
        form.setNames(null);

        assertThat(validate(form).getFieldError("names").getCode()).isEqualTo("company-closure.error.names.mandatory");
    }

    @Test
    void acceptsNameInOneLanguageOnly() {
        final CompanyClosureFormDto form = validForm();
        form.setNames(List.of(new CompanyClosureNameFormDto(GERMAN, ""), new CompanyClosureNameFormDto(ENGLISH, "Bridge day")));

        assertThat(validate(form).hasErrors()).isFalse();
    }

    @Test
    void acceptsLongName() {
        final CompanyClosureFormDto form = validForm();
        form.setNames(List.of(new CompanyClosureNameFormDto(GERMAN, "x".repeat(1000))));

        assertThat(validate(form).hasErrors()).isFalse();
    }

    @Test
    void rejectsMissingDayAndMonthOnCreate() {
        final CompanyClosureFormDto form = validForm();
        form.setDay(null);
        form.setMonth(null);

        final Errors errors = validateNew(form);

        assertThat(errors.getFieldError("day").getCode()).isEqualTo("error.entry.mandatory");
        assertThat(errors.getFieldError("month").getCode()).isEqualTo("error.entry.mandatory");
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 32})
    void rejectsDayOutOfRange(int day) {
        final CompanyClosureFormDto form = validForm();
        form.setDay(day);

        final Errors errors = validateNew(form);

        assertThat(errors.getFieldError("day").getCode()).isEqualTo("company-closure.error.day.range");
        assertThat(errors.hasFieldErrors("month")).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 13})
    void rejectsMonthOutOfRange(int month) {
        final CompanyClosureFormDto form = validForm();
        form.setMonth(month);

        final Errors errors = validateNew(form);

        assertThat(errors.getFieldError("month").getCode()).isEqualTo("company-closure.error.month.range");
        assertThat(errors.hasFieldErrors("day")).isFalse();
    }

    @Test
    void keepsErrorOfDayThatIsNotANumber() {
        final CompanyClosureFormDto form = validForm();
        form.setDay(null);
        final Errors errors = new BeanPropertyBindingResult(form, "companyClosure");
        errors.rejectValue("day", "typeMismatch");

        sut.validateNew(form, errors);

        assertThat(errors.getFieldErrors("day")).hasSize(1);
        assertThat(errors.getFieldError("day").getCode()).isEqualTo("typeMismatch");
    }

    @Test
    void rejectsInvalidDate() {
        final CompanyClosureFormDto form = validForm();
        form.setDay(31);
        form.setMonth(4);

        assertThat(validateNew(form).getFieldError("monthDay").getCode()).isEqualTo("company-closure.error.monthDay.invalid");
    }

    @Test
    void rejectsLeapDay() {
        final CompanyClosureFormDto form = validForm();
        form.setDay(29);
        form.setMonth(2);

        assertThat(validateNew(form).getFieldError("monthDay").getCode()).isEqualTo("company-closure.error.monthDay.leapDay");
    }

    @Test
    void rejectsExistingDayOnCreate() {
        when(companyClosureService.getCompanyClosureEntries()).thenReturn(List.of(
            new CompanyClosureEntry(MonthDay.of(DECEMBER, 24), locale -> "Heiligabend", Map.of(), List.of(new CompanyClosureVersion(NOON, null)))));
        final CompanyClosureFormDto form = validForm();
        form.setDay(24);
        form.setMonth(12);

        assertThat(validateNew(form).getFieldError("monthDay").getCode()).isEqualTo("company-closure.error.monthDay.exists");
    }

    @Test
    void acceptsNewDayOnCreate() {
        when(companyClosureService.getCompanyClosureEntries()).thenReturn(List.of(
            new CompanyClosureEntry(MonthDay.of(DECEMBER, 24), locale -> "Heiligabend", Map.of(), List.of(new CompanyClosureVersion(NOON, null)))));

        assertThat(validateNew(validForm()).hasErrors()).isFalse();
    }

    @Test
    void rejectsWhenCompanyWideBlackoutCoversAFutureClosureDay() {
        when(blackoutPeriodService.getAllBlackoutPeriods()).thenReturn(List.of(
            blackout("Weihnachtssperre", LocalDate.of(2027, DECEMBER, 20), LocalDate.of(2027, DECEMBER, 31), true)));
        final CompanyClosureFormDto form = validForm();
        form.setDayLength(FULL);

        final Errors errors = validateEdit(MonthDay.of(DECEMBER, 24), form);

        assertThat(errors.getGlobalError().getCode()).isEqualTo("company-closure.error.blackout-period");
        assertThat(errors.getGlobalError().getArguments()).containsExactly("Weihnachtssperre");
    }

    @Test
    void rejectsWhenCompanyWideBlackoutSpansYearBoundary() {
        when(blackoutPeriodService.getAllBlackoutPeriods()).thenReturn(List.of(
            blackout("Jahreswechsel", LocalDate.of(2027, DECEMBER, 20), LocalDate.of(2028, JANUARY, 5), true)));

        final Errors errors = validateEdit(MonthDay.of(JANUARY, 2), validForm());

        assertThat(errors.getGlobalError().getCode()).isEqualTo("company-closure.error.blackout-period");
    }

    @Test
    void rejectsNewDayInsideCompanyWideBlackout() {
        when(blackoutPeriodService.getAllBlackoutPeriods()).thenReturn(List.of(
            blackout("Weihnachtssperre", LocalDate.of(2027, DECEMBER, 20), LocalDate.of(2027, DECEMBER, 31), true)));
        final CompanyClosureFormDto form = validForm();
        form.setDay(22);
        form.setMonth(12);

        final Errors errors = validateNew(form);

        assertThat(errors.getGlobalError().getCode()).isEqualTo("company-closure.error.blackout-period");
        assertThat(errors.getGlobalError().getArguments()).containsExactly("Weihnachtssperre");
    }

    @Test
    void allowsDepartmentBlackoutPeriod() {
        when(blackoutPeriodService.getAllBlackoutPeriods()).thenReturn(List.of(
            blackout("Abteilung", LocalDate.of(2027, DECEMBER, 20), LocalDate.of(2027, DECEMBER, 31), false)));

        assertThat(validateEdit(MonthDay.of(DECEMBER, 24), validForm()).hasErrors()).isFalse();
    }

    @Test
    void allowsBlackoutBeforeValidFrom() {
        when(blackoutPeriodService.getAllBlackoutPeriods()).thenReturn(List.of(
            blackout("Alt", LocalDate.of(2026, JANUARY, 1), LocalDate.of(2026, JANUARY, 5), true)));
        final CompanyClosureFormDto form = validForm();
        form.setValidFrom(LocalDate.of(2026, NOVEMBER, 6));

        assertThat(validateEdit(MonthDay.of(JANUARY, 2), form).hasErrors()).isFalse();
    }

    @Test
    void allowsZero() {
        final CompanyClosureFormDto form = validForm();
        form.setDayLength(ZERO);

        assertThat(validateEdit(MonthDay.of(DECEMBER, 24), form).hasErrors()).isFalse();
    }

    @Test
    void allowsRenameWhenCompanyWideBlackoutCoversDay() {
        coveringBlackout();

        assertThat(validateEdit(MonthDay.of(DECEMBER, 24), FULL, validForm()).hasErrors()).isFalse();
    }

    @Test
    void allowsUnchangedDayLength() {
        coveringBlackout();
        final CompanyClosureFormDto form = validForm();
        form.setDayLength(NOON);

        assertThat(validateEdit(MonthDay.of(DECEMBER, 24), NOON, form).hasErrors()).isFalse();
    }

    @Test
    void allowsReductionFromFullToNoon() {
        coveringBlackout();
        final CompanyClosureFormDto form = validForm();
        form.setDayLength(NOON);

        assertThat(validateEdit(MonthDay.of(DECEMBER, 24), FULL, form).hasErrors()).isFalse();
    }

    @Test
    void rejectsExtensionFromNoonToFull() {
        coveringBlackout();

        final Errors errors = validateEdit(MonthDay.of(DECEMBER, 24), NOON, validForm());

        assertThat(errors.getGlobalError().getCode()).isEqualTo("company-closure.error.blackout-period");
    }

    @Test
    void ignoresBlackoutInYearsOfTheNextVersion() {
        // FULL from 2026-11-06 only applies until the MORNING version of 2028, the blackout of 2030 is out of reach
        when(blackoutPeriodService.getAllBlackoutPeriods()).thenReturn(List.of(
            blackout("Weihnachtssperre 2030", LocalDate.of(2030, DECEMBER, 20), LocalDate.of(2030, DECEMBER, 31), true)));

        final Errors errors = validateEdit(christmasEveWithMorningFrom2028(), validForm());

        assertThat(errors.hasErrors()).isFalse();
    }

    @Test
    void rejectsBlackoutBeforeTheNextVersion() {
        coveringBlackout();

        final Errors errors = validateEdit(christmasEveWithMorningFrom2028(), validForm());

        assertThat(errors.getGlobalError().getCode()).isEqualTo("company-closure.error.blackout-period");
    }

    @Test
    void rejectsLaterValidFromThatExtendsTheVersionBeforeIntoTheBlackout() {
        // FULL applies until 2026, moving ZERO from 2027 to 2028 closes 24.12.2027 inside the blackout
        coveringBlackout();
        final CompanyClosureFormDto form = formMovingVersion(LocalDate.of(2027, JANUARY, 1), ZERO, LocalDate.of(2028, JANUARY, 1));

        final Errors errors = validateEdit(christmasEveFullUntil(LocalDate.of(2027, JANUARY, 1)), form);

        assertThat(errors.getGlobalError().getCode()).isEqualTo("company-closure.error.blackout-period");
        assertThat(errors.getGlobalError().getArguments()).containsExactly("Weihnachtssperre");
    }

    @Test
    void allowsMovingAVersionWhenTheDaysInTheBlackoutStayAsTheyAre() {
        // 24.12.2027 is closed already and stays closed, the move only concerns 2028
        coveringBlackout();
        final CompanyClosureFormDto form = formMovingVersion(LocalDate.of(2028, JANUARY, 1), ZERO, LocalDate.of(2029, JANUARY, 1));

        final Errors errors = validateEdit(christmasEveFullUntil(LocalDate.of(2028, JANUARY, 1)), form);

        assertThat(errors.hasErrors()).isFalse();
    }

    private static CompanyClosureEntry christmasEveFullUntil(LocalDate zeroFrom) {
        final List<CompanyClosureVersion> versions = List.of(
            new CompanyClosureVersion(FULL, null),
            new CompanyClosureVersion(ZERO, zeroFrom));
        return new CompanyClosureEntry(MonthDay.of(DECEMBER, 24), locale -> "Heiligabend", Map.of(), versions);
    }

    /**
     * @return a form that only moves the version starting at {@code originalValidFrom}, without a new version
     */
    private static CompanyClosureFormDto formMovingVersion(LocalDate originalValidFrom, DayLength dayLength, LocalDate validFrom) {
        final CompanyClosureVersionFormDto version = new CompanyClosureVersionFormDto(dayLength, originalValidFrom);
        version.setValidFrom(validFrom);
        final CompanyClosureFormDto form = validForm();
        form.setDayLength(FULL);
        form.setValidFrom(null);
        form.setVersions(List.of(version));
        return form;
    }

    private static CompanyClosureEntry christmasEveWithMorningFrom2028() {
        final List<CompanyClosureVersion> versions = List.of(
            new CompanyClosureVersion(NOON, null),
            new CompanyClosureVersion(MORNING, LocalDate.of(2028, JANUARY, 1)));
        return new CompanyClosureEntry(MonthDay.of(DECEMBER, 24), locale -> "Heiligabend", Map.of(), versions);
    }

    private Errors validateEdit(CompanyClosureEntry entry, CompanyClosureFormDto form) {
        final Errors errors = new BeanPropertyBindingResult(form, "companyClosure");
        sut.validateEdit(entry, form, errors);
        return errors;
    }

    private void coveringBlackout() {
        when(blackoutPeriodService.getAllBlackoutPeriods()).thenReturn(List.of(
            blackout("Weihnachtssperre", LocalDate.of(2027, DECEMBER, 20), LocalDate.of(2027, DECEMBER, 31), true)));
    }

    private static BlackoutPeriod blackout(String title, LocalDate start, LocalDate end, boolean companyWide) {
        final BlackoutPeriod blackoutPeriod = new BlackoutPeriod();
        blackoutPeriod.setTitle(title);
        blackoutPeriod.setStartDate(start);
        blackoutPeriod.setEndDate(end);
        blackoutPeriod.setCompanyWide(companyWide);
        return blackoutPeriod;
    }

    private Errors validateEdit(MonthDay monthDay, CompanyClosureFormDto form) {
        return validateEdit(monthDay, ZERO, form);
    }

    private Errors validateEdit(MonthDay monthDay, DayLength existing, CompanyClosureFormDto form) {
        final CompanyClosureEntry entry = new CompanyClosureEntry(monthDay, locale -> "Alt", Map.of(), List.of(new CompanyClosureVersion(existing, null)));
        final Errors errors = new BeanPropertyBindingResult(form, "companyClosure");
        sut.validateEdit(entry, form, errors);
        return errors;
    }

    private static CompanyClosureFormDto validForm() {
        final CompanyClosureFormDto form = new CompanyClosureFormDto();
        form.setDay(15);
        form.setMonth(5);
        form.setNames(List.of(new CompanyClosureNameFormDto(GERMAN, "Brückentag")));
        form.setDayLength(FULL);
        form.setValidFrom(LocalDate.of(2026, NOVEMBER, 6));
        return form;
    }

    private Errors validate(CompanyClosureFormDto form) {
        final Errors errors = new BeanPropertyBindingResult(form, "companyClosure");
        sut.validate(form, errors);
        return errors;
    }

    private Errors validateNew(CompanyClosureFormDto form) {
        final Errors errors = new BeanPropertyBindingResult(form, "companyClosure");
        sut.validateNew(form, errors);
        return errors;
    }
}
