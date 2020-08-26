package org.synyx.urlaubsverwaltung.overtime.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Consumer;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createOvertimeRecord;

@ExtendWith(MockitoExtension.class)
class OvertimeFormValidatorTest {

    private OvertimeFormValidator sut;

    @Mock
    private Errors errors;
    @Mock
    private OvertimeService overtimeService;
    @Mock
    private SettingsService settingsService;

    @BeforeEach
    void setUp() {
        sut = new OvertimeFormValidator(overtimeService, settingsService);
    }

    // Support method --------------------------------------------------------------------------------------------------

    @Test
    void ensureSupportsOvertimeFormClass() {
        assertThat(sut.supports(OvertimeForm.class)).isTrue();
    }

    @Test
    void ensureDoesNotSupportOtherClass() {
        assertThat(sut.supports(Person.class)).isFalse();
    }

    // Validate method -------------------------------------------------------------------------------------------------
    @Test
    void ensureNoErrorsIfValid() {

        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setOvertimeActive(true);
        when(settingsService.getSettings()).thenReturn(settings);

        when(overtimeService.getLeftOvertimeForPerson(any(Person.class))).thenReturn(BigDecimal.ZERO);

        final OvertimeForm overtimeForm = new OvertimeForm(createOvertimeRecord());
        sut.validate(overtimeForm, errors);

        verifyNoInteractions(errors);
    }

    // Validate period -------------------------------------------------------------------------------------------------
    @Test
    void ensureStartDateIsMandatory() {

        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setOvertimeActive(true);
        when(settingsService.getSettings()).thenReturn(settings);
        when(overtimeService.getLeftOvertimeForPerson(any(Person.class))).thenReturn(BigDecimal.ZERO);

        final OvertimeForm overtimeForm = new OvertimeForm(createOvertimeRecord());
        overtimeForm.setStartDate(null);

        sut.validate(overtimeForm, errors);

        verify(errors).rejectValue("startDate", "error.entry.mandatory");
    }


    @Test
    void ensureEndDateIsMandatory() {

        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setOvertimeActive(true);
        when(settingsService.getSettings()).thenReturn(settings);
        when(overtimeService.getLeftOvertimeForPerson(any(Person.class))).thenReturn(BigDecimal.ZERO);

        final OvertimeForm overtimeForm = new OvertimeForm(createOvertimeRecord());
        overtimeForm.setEndDate(null);

        sut.validate(overtimeForm, errors);

        verify(errors).rejectValue("endDate", "error.entry.mandatory");
    }

    @Test
    void ensureStartAndEndDateCanBeEquals() {

        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setOvertimeActive(true);
        when(settingsService.getSettings()).thenReturn(settings);

        when(overtimeService.getLeftOvertimeForPerson(any(Person.class))).thenReturn(BigDecimal.ZERO);

        LocalDate now = LocalDate.now(UTC);

        final OvertimeForm overtimeForm = new OvertimeForm(createOvertimeRecord());
        overtimeForm.setStartDate(now);
        overtimeForm.setEndDate(now);

        sut.validate(overtimeForm, errors);

        verifyNoInteractions(errors);
    }

    @Test
    void ensureStartDateCanNotBeAfterEndDate() {

        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setOvertimeActive(true);
        when(settingsService.getSettings()).thenReturn(settings);

        when(overtimeService.getLeftOvertimeForPerson(any(Person.class))).thenReturn(BigDecimal.ZERO);

        final OvertimeForm overtimeForm = new OvertimeForm(createOvertimeRecord());
        overtimeForm.setStartDate(overtimeForm.getEndDate().plusDays(3));

        sut.validate(overtimeForm, errors);

        verify(errors).rejectValue("endDate", "error.entry.invalidPeriod");
    }

    @Test
    void ensureNoErrorMessageForMandatoryIfStartDateIsNullBecauseOfTypeMismatch() {

        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setOvertimeActive(true);
        when(settingsService.getSettings()).thenReturn(settings);
        when(errors.hasFieldErrors("startDate")).thenReturn(true);
        when(overtimeService.getLeftOvertimeForPerson(any(Person.class))).thenReturn(BigDecimal.ZERO);

        final OvertimeForm overtimeForm = new OvertimeForm(createOvertimeRecord());
        overtimeForm.setStartDate(null);

        sut.validate(overtimeForm, errors);

        verify(errors).hasFieldErrors("startDate");
        verify(errors, never()).rejectValue("startDate", "error.entry.mandatory");
    }

    @Test
    void ensureNoErrorMessageForMandatoryIfEndDateIsNullBecauseOfTypeMismatch() {

        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setOvertimeActive(true);
        when(settingsService.getSettings()).thenReturn(settings);
        when(overtimeService.getLeftOvertimeForPerson(any(Person.class))).thenReturn(BigDecimal.ZERO);
        when(errors.hasFieldErrors("endDate")).thenReturn(true);

        final OvertimeForm overtimeForm = new OvertimeForm(createOvertimeRecord());
        overtimeForm.setEndDate(null);

        sut.validate(overtimeForm, errors);

        verify(errors).hasFieldErrors("endDate");
        verify(errors, never()).rejectValue("endDate", "error.entry.mandatory");
    }

    // Validate number of hours ----------------------------------------------------------------------------------------
    @Test
    void ensureNumberOfHoursIsMandatory() {

        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setOvertimeActive(true);
        when(settingsService.getSettings()).thenReturn(settings);

        final OvertimeForm overtimeForm = new OvertimeForm(createOvertimeRecord());
        overtimeForm.setNumberOfHours(null);

        sut.validate(overtimeForm, errors);

        verify(errors).rejectValue("numberOfHours", "error.entry.mandatory");
    }

    @Test
    void ensureNumberOfHoursCanBeNegative() {

        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setOvertimeActive(true);
        when(settingsService.getSettings()).thenReturn(settings);

        when(overtimeService.getLeftOvertimeForPerson(any(Person.class))).thenReturn(BigDecimal.ZERO);

        final OvertimeForm overtimeForm = new OvertimeForm(createOvertimeRecord());
        overtimeForm.setNumberOfHours(BigDecimal.ONE.negate());

        sut.validate(overtimeForm, errors);

        verifyNoInteractions(errors);
    }

    @Test
    void ensureNumberOfHoursCanBeZero() {

        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setOvertimeActive(true);
        when(settingsService.getSettings()).thenReturn(settings);

        when(overtimeService.getLeftOvertimeForPerson(any(Person.class))).thenReturn(BigDecimal.ZERO);

        final OvertimeForm overtimeForm = new OvertimeForm(createOvertimeRecord());
        overtimeForm.setNumberOfHours(BigDecimal.ZERO);

        sut.validate(overtimeForm, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureNumberOfHoursCanBeADecimalNumber() {

        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setOvertimeActive(true);
        when(settingsService.getSettings()).thenReturn(settings);

        when(overtimeService.getLeftOvertimeForPerson(any(Person.class))).thenReturn(BigDecimal.ZERO);

        final OvertimeForm overtimeForm = new OvertimeForm(createOvertimeRecord());
        overtimeForm.setNumberOfHours(new BigDecimal("0.5"));

        sut.validate(overtimeForm, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureNoErrorMessageForMandatoryIfNumberOfHoursIsNullBecauseOfTypeMismatch() {

        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setOvertimeActive(true);
        when(settingsService.getSettings()).thenReturn(settings);
        when(errors.hasFieldErrors("numberOfHours")).thenReturn(true);

        final OvertimeForm overtimeForm = new OvertimeForm(createOvertimeRecord());
        overtimeForm.setNumberOfHours(null);

        sut.validate(overtimeForm, errors);
        verify(errors).hasFieldErrors("numberOfHours");
        verify(errors, never()).rejectValue("endDate", "error.entry.mandatory");
    }

    // Validate using overtime settings --------------------------------------------------------------------------------
    @Test
    void ensureCanNotRecordOvertimeIfOvertimeManagementIsDeactivated() {

        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setOvertimeActive(false);
        when(settingsService.getSettings()).thenReturn(settings);

        final OvertimeForm overtimeForm = new OvertimeForm(createOvertimeRecord());

        sut.validate(overtimeForm, errors);
        verify(errors).reject("overtime.record.error.deactivated");
    }

    @Test
    void ensureCanNotRecordOvertimeIfMaximumOvertimeIsZero() {
        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setOvertimeActive(true);
        settings.getWorkingTimeSettings().setMaximumOvertime(0);
        when(settingsService.getSettings()).thenReturn(settings);

        final OvertimeForm overtimeForm = new OvertimeForm(createOvertimeRecord());
        // just not important how many number of hours, can not record overtime!
        overtimeForm.setNumberOfHours(BigDecimal.ZERO);

        sut.validate(overtimeForm, errors);

        verify(errors).reject("overtime.record.error.deactivated");

        verify(settingsService).getSettings();
        verifyNoInteractions(overtimeService);
    }

    @Test
    void ensureCanRecordOvertimeIfMaximumOvertimeReachedButNotExceeded() {
        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setOvertimeActive(true);
        settings.getWorkingTimeSettings().setMaximumOvertime(16);
        when(settingsService.getSettings()).thenReturn(settings);

        when(overtimeService.getLeftOvertimeForPerson(any(Person.class))).thenReturn(new BigDecimal("8"));

        final OvertimeForm overtimeForm = new OvertimeForm(createOvertimeRecord());
        overtimeForm.setNumberOfHours(new BigDecimal("8"));

        sut.validate(overtimeForm, errors);
        verifyNoInteractions(errors);
        verify(settingsService).getSettings();
        verify(overtimeService).getLeftOvertimeForPerson(overtimeForm.getPerson());
    }


    @Test
    void ensureCanNotRecordOvertimeIfMaximumOvertimeExceeded() {
        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setOvertimeActive(true);
        settings.getWorkingTimeSettings().setMaximumOvertime(16);
        when(settingsService.getSettings()).thenReturn(settings);

        when(overtimeService.getLeftOvertimeForPerson(any(Person.class))).thenReturn(new BigDecimal("8"));

        final OvertimeForm overtimeForm = new OvertimeForm(createOvertimeRecord());
        overtimeForm.setNumberOfHours(new BigDecimal("8.5"));

        sut.validate(overtimeForm, errors);
        verify(errors)
            .rejectValue("numberOfHours", "overtime.data.numberOfHours.error.maxOvertime",
                new Object[]{new BigDecimal("16")}, null);
        verify(settingsService).getSettings();
        verify(overtimeService).getLeftOvertimeForPerson(overtimeForm.getPerson());
    }


    @Test
    void ensureCanNotRecordOvertimeIfMinimumOvertimeExceeded() {
        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setOvertimeActive(true);
        settings.getWorkingTimeSettings().setMinimumOvertime(10);
        when(settingsService.getSettings()).thenReturn(settings);

        when(overtimeService.getLeftOvertimeForPerson(any(Person.class))).thenReturn(new BigDecimal("-9"));

        final OvertimeForm overtimeForm = new OvertimeForm(createOvertimeRecord());
        overtimeForm.setNumberOfHours(new BigDecimal("-1.5"));

        sut.validate(overtimeForm, errors);
        verify(errors)
            .rejectValue("numberOfHours", "overtime.data.numberOfHours.error.minOvertime",
                new Object[]{new BigDecimal("10")}, null);
        verify(settingsService).getSettings();
        verify(overtimeService).getLeftOvertimeForPerson(overtimeForm.getPerson());
    }

    // Validate changes in existing overtime record --------------------------------------------------------------------
    @Test
    void foo() {
        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setOvertimeActive(true);
        settings.getWorkingTimeSettings().setMaximumOvertime(100);
        when(settingsService.getSettings()).thenReturn(settings);

        when(overtimeService.getLeftOvertimeForPerson(any(Person.class))).thenReturn(new BigDecimal("99.5"));

        final OvertimeForm overtimeForm = new OvertimeForm(createOvertimeRecord());
        overtimeForm.setNumberOfHours(new BigDecimal("2"));
        overtimeForm.setId(42);

        Overtime originalOvertimeRecord = createOvertimeRecord();
        originalOvertimeRecord.setHours(new BigDecimal("3"));

        when(overtimeService.getOvertimeById(anyInt())).thenReturn(Optional.of(originalOvertimeRecord));

        sut.validate(overtimeForm, errors);
        verifyNoInteractions(errors);
        verify(overtimeService).getOvertimeById(overtimeForm.getId());
        verify(settingsService).getSettings();
        verify(overtimeService).getLeftOvertimeForPerson(overtimeForm.getPerson());
    }

    @Test
    void ensureCanEditOvertimeRecordChangingPositiveHours() {
        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setOvertimeActive(true);
        settings.getWorkingTimeSettings().setMaximumOvertime(4);
        when(settingsService.getSettings()).thenReturn(settings);

        when(overtimeService.getLeftOvertimeForPerson(any(Person.class)))
            .thenReturn(new BigDecimal("3.5"));

        final OvertimeForm overtimeForm = new OvertimeForm(createOvertimeRecord());
        overtimeForm.setNumberOfHours(new BigDecimal("3"));
        overtimeForm.setId(42);

        Overtime originalOvertimeRecord = createOvertimeRecord();
        originalOvertimeRecord.setHours(new BigDecimal("2.5"));

        when(overtimeService.getOvertimeById(anyInt())).thenReturn(Optional.of(originalOvertimeRecord));

        sut.validate(overtimeForm, errors);
        verifyNoInteractions(errors);
        verify(overtimeService).getOvertimeById(overtimeForm.getId());
        verify(settingsService).getSettings();
        verify(overtimeService).getLeftOvertimeForPerson(overtimeForm.getPerson());
    }

    @Test
    void ensureCanEditOvertimeRecordChangingNegativeHours() {
        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setOvertimeActive(true);
        settings.getWorkingTimeSettings().setMinimumOvertime(4);
        when(settingsService.getSettings()).thenReturn(settings);

        when(overtimeService.getLeftOvertimeForPerson(any(Person.class)))
            .thenReturn(new BigDecimal("-3.5"));

        final OvertimeForm overtimeForm = new OvertimeForm(createOvertimeRecord());
        overtimeForm.setNumberOfHours(new BigDecimal("-3"));
        overtimeForm.setId(42);

        Overtime originalOvertimeRecord = createOvertimeRecord();
        originalOvertimeRecord.setHours(new BigDecimal("-2.5"));

        when(overtimeService.getOvertimeById(anyInt())).thenReturn(Optional.of(originalOvertimeRecord));

        sut.validate(overtimeForm, errors);
        verifyNoInteractions(errors);
        verify(overtimeService).getOvertimeById(overtimeForm.getId());
        verify(settingsService).getSettings();
        verify(overtimeService).getLeftOvertimeForPerson(overtimeForm.getPerson());
    }


    // Validate comment ------------------------------------------------------------------------------------------------

    @Test
    void ensureCommentInsideMaximumCharacterLength() {

        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setOvertimeActive(true);
        when(settingsService.getSettings()).thenReturn(settings);
        when(overtimeService.getLeftOvertimeForPerson(any(Person.class))).thenReturn(BigDecimal.ZERO);

        final OvertimeForm overtimeForm = new OvertimeForm(createOvertimeRecord());
        overtimeForm.setComment(
            "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore "
                + "et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores");

        sut.validate(overtimeForm, errors);

        verifyNoInteractions(errors);
    }

    @Test
    void ensureCommentIsNotMandatory() {

        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setOvertimeActive(true);
        when(settingsService.getSettings()).thenReturn(settings);
        when(overtimeService.getLeftOvertimeForPerson(any(Person.class))).thenReturn(BigDecimal.ZERO);

        final OvertimeForm overtimeForm = new OvertimeForm(createOvertimeRecord());
        Consumer<String> assertMayBeEmpty = (comment) -> {
            overtimeForm.setComment(comment);

            sut.validate(overtimeForm, errors);

            verifyNoInteractions(errors);
        };

        assertMayBeEmpty.accept(null);
        assertMayBeEmpty.accept("");
    }


    @Test
    void ensureCommentHasMaximumCharacterLength() {

        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setOvertimeActive(true);
        when(settingsService.getSettings()).thenReturn(settings);
        when(overtimeService.getLeftOvertimeForPerson(any(Person.class))).thenReturn(BigDecimal.ZERO);

        final OvertimeForm overtimeForm = new OvertimeForm(createOvertimeRecord());
        overtimeForm.setComment(
            "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore "
                + "et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores e");

        sut.validate(overtimeForm, errors);

        verify(errors).rejectValue("comment", "error.entry.tooManyChars");
    }
}
