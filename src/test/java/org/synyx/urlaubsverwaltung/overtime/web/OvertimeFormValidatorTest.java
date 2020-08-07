package org.synyx.urlaubsverwaltung.overtime.web;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Consumer;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OvertimeFormValidatorTest {

    private OvertimeFormValidator validator;

    private OvertimeForm overtimeForm;
    private Settings settings;

    @Mock
    private Errors errors;
    @Mock
    private OvertimeService overtimeService;
    @Mock
    private SettingsService settingsService;

    @Before
    public void setUp() {

        validator = new OvertimeFormValidator(overtimeService, settingsService);

        final Overtime overtimeRecord = TestDataCreator.createOvertimeRecord();
        overtimeForm = new OvertimeForm(overtimeRecord);

        settings = new Settings();
        settings.getWorkingTimeSettings().setOvertimeActive(true);

        when(settingsService.getSettings()).thenReturn(settings);
        when(overtimeService.getLeftOvertimeForPerson(any(Person.class))).thenReturn(BigDecimal.ZERO);
    }


    // Support method --------------------------------------------------------------------------------------------------

    @Test
    public void ensureSupportsOvertimeFormClass() {
        assertThat(validator.supports(OvertimeForm.class)).isTrue();
    }


    @Test
    public void ensureDoesNotSupportOtherClass() {
        assertThat(validator.supports(Person.class)).isFalse();
    }


    // Validate method -------------------------------------------------------------------------------------------------

    @Test
    public void ensureNoErrorsIfValid() {

        validator.validate(overtimeForm, errors);

        verifyZeroInteractions(errors);
    }


    // Validate period -------------------------------------------------------------------------------------------------

    @Test
    public void ensureStartDateIsMandatory() {

        overtimeForm.setStartDate(null);

        validator.validate(overtimeForm, errors);

        verify(errors).rejectValue("startDate", "error.entry.mandatory");
    }


    @Test
    public void ensureEndDateIsMandatory() {

        overtimeForm.setEndDate(null);

        validator.validate(overtimeForm, errors);

        verify(errors).rejectValue("endDate", "error.entry.mandatory");
    }


    @Test
    public void ensureStartAndEndDateCanBeEquals() {

        Instant now = Instant.now();

        overtimeForm.setStartDate(now);
        overtimeForm.setEndDate(now);

        validator.validate(overtimeForm, errors);

        verifyZeroInteractions(errors);
    }


    @Test
    public void ensureStartDateCanNotBeAfterEndDate() {

        overtimeForm.setStartDate(overtimeForm.getEndDate().plus(3, DAYS));

        validator.validate(overtimeForm, errors);

        verify(errors).rejectValue("endDate", "error.entry.invalidPeriod");
    }


    @Test
    public void ensureNoErrorMessageForMandatoryIfStartDateIsNullBecauseOfTypeMismatch() {

        when(errors.hasFieldErrors("startDate")).thenReturn(true);

        overtimeForm.setStartDate(null);

        validator.validate(overtimeForm, errors);

        verify(errors).hasFieldErrors("startDate");
        verify(errors, never()).rejectValue("startDate", "error.entry.mandatory");
    }


    @Test
    public void ensureNoErrorMessageForMandatoryIfEndDateIsNullBecauseOfTypeMismatch() {

        when(errors.hasFieldErrors("endDate")).thenReturn(true);

        overtimeForm.setEndDate(null);

        validator.validate(overtimeForm, errors);

        verify(errors).hasFieldErrors("endDate");
        verify(errors, never()).rejectValue("endDate", "error.entry.mandatory");
    }


    // Validate number of hours ----------------------------------------------------------------------------------------

    @Test
    public void ensureNumberOfHoursIsMandatory() {

        overtimeForm.setNumberOfHours(null);

        validator.validate(overtimeForm, errors);

        verify(errors).rejectValue("numberOfHours", "error.entry.mandatory");
    }


    @Test
    public void ensureNumberOfHoursCanBeNegative() {

        overtimeForm.setNumberOfHours(BigDecimal.ONE.negate());

        validator.validate(overtimeForm, errors);

        verifyZeroInteractions(errors);
    }


    @Test
    public void ensureNumberOfHoursCanBeZero() {

        overtimeForm.setNumberOfHours(BigDecimal.ZERO);

        validator.validate(overtimeForm, errors);

        verifyZeroInteractions(errors);
    }


    @Test
    public void ensureNumberOfHoursCanBeADecimalNumber() {

        overtimeForm.setNumberOfHours(new BigDecimal("0.5"));

        validator.validate(overtimeForm, errors);

        verifyZeroInteractions(errors);
    }


    @Test
    public void ensureNoErrorMessageForMandatoryIfNumberOfHoursIsNullBecauseOfTypeMismatch() {

        when(errors.hasFieldErrors("numberOfHours")).thenReturn(true);

        overtimeForm.setNumberOfHours(null);

        validator.validate(overtimeForm, errors);

        verify(errors).hasFieldErrors("numberOfHours");
        verify(errors, never()).rejectValue("endDate", "error.entry.mandatory");
    }


    // Validate using overtime settings --------------------------------------------------------------------------------

    @Test
    public void ensureCanNotRecordOvertimeIfOvertimeManagementIsDeactivated() {

        settings.getWorkingTimeSettings().setOvertimeActive(false);

        validator.validate(overtimeForm, errors);

        verify(errors).reject("overtime.record.error.deactivated");
    }


    @Test
    public void ensureCanNotRecordOvertimeIfMaximumOvertimeIsZero() {

        settings.getWorkingTimeSettings().setMaximumOvertime(0);

        // just not important how many number of hours, can not record overtime!
        overtimeForm.setNumberOfHours(BigDecimal.ZERO);

        validator.validate(overtimeForm, errors);

        verify(errors).reject("overtime.record.error.deactivated");

        verify(settingsService).getSettings();
        verifyZeroInteractions(overtimeService);
    }


    @Test
    public void ensureCanRecordOvertimeIfMaximumOvertimeReachedButNotExceeded() {

        settings.getWorkingTimeSettings().setMaximumOvertime(16);

        when(overtimeService.getLeftOvertimeForPerson(any(Person.class))).thenReturn(new BigDecimal("8"));

        overtimeForm.setNumberOfHours(new BigDecimal("8"));

        validator.validate(overtimeForm, errors);

        verifyZeroInteractions(errors);

        verify(settingsService).getSettings();
        verify(overtimeService).getLeftOvertimeForPerson(overtimeForm.getPerson());
    }


    @Test
    public void ensureCanNotRecordOvertimeIfMaximumOvertimeExceeded() {

        settings.getWorkingTimeSettings().setMaximumOvertime(16);

        when(overtimeService.getLeftOvertimeForPerson(any(Person.class)))
            .thenReturn(new BigDecimal("8"));

        overtimeForm.setNumberOfHours(new BigDecimal("8.5"));

        validator.validate(overtimeForm, errors);

        verify(errors)
            .rejectValue("numberOfHours", "overtime.data.numberOfHours.error.maxOvertime",
                new Object[]{new BigDecimal("16")}, null);

        verify(settingsService).getSettings();
        verify(overtimeService).getLeftOvertimeForPerson(overtimeForm.getPerson());
    }


    @Test
    public void ensureCanNotRecordOvertimeIfMinimumOvertimeExceeded() {

        settings.getWorkingTimeSettings().setMinimumOvertime(10);

        when(overtimeService.getLeftOvertimeForPerson(any(Person.class)))
            .thenReturn(new BigDecimal("-9"));

        overtimeForm.setNumberOfHours(new BigDecimal("-1.5"));

        validator.validate(overtimeForm, errors);

        verify(errors)
            .rejectValue("numberOfHours", "overtime.data.numberOfHours.error.minOvertime",
                new Object[]{new BigDecimal("10")}, null);

        verify(settingsService).getSettings();
        verify(overtimeService).getLeftOvertimeForPerson(overtimeForm.getPerson());
    }


    // Validate changes in existing overtime record --------------------------------------------------------------------

    @Test
    public void foo() {

        settings.getWorkingTimeSettings().setMaximumOvertime(100);

        when(overtimeService.getLeftOvertimeForPerson(any(Person.class)))
            .thenReturn(new BigDecimal("99.5"));

        overtimeForm.setNumberOfHours(new BigDecimal("2"));
        overtimeForm.setId(42);

        Overtime originalOvertimeRecord = TestDataCreator.createOvertimeRecord();
        originalOvertimeRecord.setHours(new BigDecimal("3"));

        when(overtimeService.getOvertimeById(anyInt()))
            .thenReturn(Optional.of(originalOvertimeRecord));

        validator.validate(overtimeForm, errors);

        verifyZeroInteractions(errors);

        verify(overtimeService).getOvertimeById(overtimeForm.getId());
        verify(settingsService).getSettings();
        verify(overtimeService).getLeftOvertimeForPerson(overtimeForm.getPerson());
    }


    @Test
    public void ensureCanEditOvertimeRecordChangingPositiveHours() {

        settings.getWorkingTimeSettings().setMaximumOvertime(4);

        when(overtimeService.getLeftOvertimeForPerson(any(Person.class)))
            .thenReturn(new BigDecimal("3.5"));

        overtimeForm.setNumberOfHours(new BigDecimal("3"));
        overtimeForm.setId(42);

        Overtime originalOvertimeRecord = TestDataCreator.createOvertimeRecord();
        originalOvertimeRecord.setHours(new BigDecimal("2.5"));

        when(overtimeService.getOvertimeById(anyInt()))
            .thenReturn(Optional.of(originalOvertimeRecord));

        validator.validate(overtimeForm, errors);

        verifyZeroInteractions(errors);

        verify(overtimeService).getOvertimeById(overtimeForm.getId());
        verify(settingsService).getSettings();
        verify(overtimeService).getLeftOvertimeForPerson(overtimeForm.getPerson());
    }


    @Test
    public void ensureCanEditOvertimeRecordChangingNegativeHours() throws IllegalAccessException {

        settings.getWorkingTimeSettings().setMinimumOvertime(4);

        when(overtimeService.getLeftOvertimeForPerson(any(Person.class)))
            .thenReturn(new BigDecimal("-3.5"));

        overtimeForm.setNumberOfHours(new BigDecimal("-3"));

        overtimeForm.setId(42);

        Overtime originalOvertimeRecord = TestDataCreator.createOvertimeRecord();
        originalOvertimeRecord.setHours(new BigDecimal("-2.5"));

        when(overtimeService.getOvertimeById(anyInt())).thenReturn(Optional.of(originalOvertimeRecord));

        validator.validate(overtimeForm, errors);

        verifyZeroInteractions(errors);

        verify(overtimeService).getOvertimeById(overtimeForm.getId());
        verify(settingsService).getSettings();
        verify(overtimeService).getLeftOvertimeForPerson(overtimeForm.getPerson());
    }


    // Validate comment ------------------------------------------------------------------------------------------------

    @Test
    public void ensureCommentInsideMaximumCharacterLength() {

        overtimeForm.setComment(
            "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore "
                + "et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores");

        validator.validate(overtimeForm, errors);

        verifyZeroInteractions(errors);
    }

    @Test
    public void ensureCommentIsNotMandatory() {

        Consumer<String> assertMayBeEmpty = (comment) -> {
            overtimeForm.setComment(comment);

            validator.validate(overtimeForm, errors);

            verifyZeroInteractions(errors);
        };

        assertMayBeEmpty.accept(null);
        assertMayBeEmpty.accept("");
    }


    @Test
    public void ensureCommentHasMaximumCharacterLength() {

        overtimeForm.setComment(
            "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore "
                + "et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores e");

        validator.validate(overtimeForm, errors);

        verify(errors).rejectValue("comment", "error.entry.tooManyChars");
    }
}
