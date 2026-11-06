package org.synyx.urlaubsverwaltung.companyclosure.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureEntry;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureService;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureVersion;

import java.time.Clock;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.ZoneId;
import java.util.List;

import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.time.Month.NOVEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;

@ExtendWith(MockitoExtension.class)
class CompanyClosureFormValidatorTest {

    private CompanyClosureFormValidator sut;

    @Mock
    private CompanyClosureService companyClosureService;

    @BeforeEach
    void setUp() {
        final Clock clock = Clock.fixed(LocalDate.of(2026, NOVEMBER, 6).atStartOfDay(ZoneId.of("UTC")).toInstant(), ZoneId.of("UTC"));
        sut = new CompanyClosureFormValidator(companyClosureService, clock);
    }

    @Test
    void supportsCompanyClosureForm() {
        assertThat(sut.supports(CompanyClosureForm.class)).isTrue();
        assertThat(sut.supports(Object.class)).isFalse();
    }

    @Test
    void acceptsValidForm() {
        final Errors errors = validate(validForm());

        assertThat(errors.hasErrors()).isFalse();
    }

    @Test
    void rejectsValidFromInPreviousYear() {
        final CompanyClosureForm form = validForm();
        form.setValidFrom(LocalDate.of(2025, DECEMBER, 31));

        final Errors errors = validate(form);

        assertThat(errors.getFieldError("validFrom").getCode()).isEqualTo("company-closure.error.validFrom.previousYear");
    }

    @Test
    void acceptsFirstOfCurrentYear() {
        final CompanyClosureForm form = validForm();
        form.setValidFrom(LocalDate.of(2026, JANUARY, 1));

        assertThat(validate(form).hasErrors()).isFalse();
    }

    @Test
    void rejectsMissingValidFrom() {
        final CompanyClosureForm form = validForm();
        form.setValidFrom(null);

        assertThat(validate(form).getFieldError("validFrom").getCode()).isEqualTo("error.entry.mandatory");
    }

    @Test
    void rejectsMissingDayLength() {
        final CompanyClosureForm form = validForm();
        form.setDayLength(null);

        assertThat(validate(form).getFieldError("dayLength").getCode()).isEqualTo("error.entry.mandatory");
    }

    @Test
    void rejectsBlankName() {
        final CompanyClosureForm form = validForm();
        form.setName("  ");

        assertThat(validate(form).getFieldError("name").getCode()).isEqualTo("error.entry.mandatory");
    }

    @Test
    void rejectsTooLongName() {
        final CompanyClosureForm form = validForm();
        form.setName("x".repeat(101));

        assertThat(validate(form).getFieldError("name").getCode()).isEqualTo("error.entry.tooManyChars");
    }

    @Test
    void acceptsNameWithMaxLength() {
        final CompanyClosureForm form = validForm();
        form.setName("x".repeat(100));

        assertThat(validate(form).hasErrors()).isFalse();
    }

    @Test
    void rejectsMissingDayAndMonthOnCreate() {
        final CompanyClosureForm form = validForm();
        form.setDay(null);
        form.setMonth(null);

        final Errors errors = validateNew(form);

        assertThat(errors.getFieldError("day").getCode()).isEqualTo("error.entry.mandatory");
        assertThat(errors.getFieldError("month").getCode()).isEqualTo("error.entry.mandatory");
    }

    @Test
    void rejectsInvalidDate() {
        final CompanyClosureForm form = validForm();
        form.setDay(31);
        form.setMonth(4);

        assertThat(validateNew(form).getFieldError("day").getCode()).isEqualTo("company-closure.error.monthDay.invalid");
    }

    @Test
    void rejectsLeapDay() {
        final CompanyClosureForm form = validForm();
        form.setDay(29);
        form.setMonth(2);

        assertThat(validateNew(form).getFieldError("day").getCode()).isEqualTo("company-closure.error.monthDay.leapDay");
    }

    @Test
    void rejectsExistingDayOnCreate() {
        when(companyClosureService.getCompanyClosureEntries()).thenReturn(List.of(
            new CompanyClosureEntry(MonthDay.of(DECEMBER, 24), "Heiligabend", List.of(new CompanyClosureVersion(NOON, null)))));
        final CompanyClosureForm form = validForm();
        form.setDay(24);
        form.setMonth(12);

        assertThat(validateNew(form).getFieldError("day").getCode()).isEqualTo("company-closure.error.monthDay.exists");
    }

    @Test
    void acceptsNewDayOnCreate() {
        when(companyClosureService.getCompanyClosureEntries()).thenReturn(List.of(
            new CompanyClosureEntry(MonthDay.of(DECEMBER, 24), "Heiligabend", List.of(new CompanyClosureVersion(NOON, null)))));

        assertThat(validateNew(validForm()).hasErrors()).isFalse();
    }

    private static CompanyClosureForm validForm() {
        final CompanyClosureForm form = new CompanyClosureForm();
        form.setDay(15);
        form.setMonth(5);
        form.setName("Brückentag");
        form.setDayLength(FULL);
        form.setValidFrom(LocalDate.of(2026, NOVEMBER, 6));
        return form;
    }

    private Errors validate(CompanyClosureForm form) {
        final Errors errors = new BeanPropertyBindingResult(form, "companyClosure");
        sut.validate(form, errors);
        return errors;
    }

    private Errors validateNew(CompanyClosureForm form) {
        final Errors errors = new BeanPropertyBindingResult(form, "companyClosure");
        sut.validateNew(form, errors);
        return errors;
    }
}
