package org.synyx.urlaubsverwaltung.web.overtime;

import org.joda.time.DateMidnight;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.util.ReflectionUtils;

import org.springframework.validation.Errors;

import org.synyx.urlaubsverwaltung.core.overtime.Overtime;
import org.synyx.urlaubsverwaltung.core.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.settings.Settings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.lang.reflect.Field;

import java.math.BigDecimal;

import java.util.Optional;
import java.util.function.Consumer;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class OvertimeValidatorTest {

    private OvertimeValidator validator;

    private OvertimeForm overtimeForm;
    private Settings settings;

    private Errors errors;
    private OvertimeService overtimeServiceMock;
    private SettingsService settingsServiceMock;

    @Before
    public void setUp() {

        overtimeServiceMock = Mockito.mock(OvertimeService.class);
        settingsServiceMock = Mockito.mock(SettingsService.class);

        validator = new OvertimeValidator(overtimeServiceMock, settingsServiceMock);
        errors = Mockito.mock(Errors.class);

        Overtime overtimeRecord = TestDataCreator.createOvertimeRecord();
        overtimeForm = new OvertimeForm(overtimeRecord);

        settings = new Settings();
        settings.getWorkingTimeSettings().setOvertimeActive(true);

        Mockito.when(settingsServiceMock.getSettings()).thenReturn(settings);

        Mockito.when(overtimeServiceMock.getLeftOvertimeForPerson(Mockito.any(Person.class)))
            .thenReturn(BigDecimal.ZERO);
    }


    // Support method --------------------------------------------------------------------------------------------------

    @Test
    public void ensureSupportsOvertimeFormClass() {

        Assert.assertTrue("Should support overtime form class", validator.supports(OvertimeForm.class));
    }


    @Test
    public void ensureDoesNotSupportOtherClass() {

        Assert.assertFalse("Should not support other class than overtime form class", validator.supports(Person.class));
    }


    // Validate method -------------------------------------------------------------------------------------------------

    @Test
    public void ensureNoErrorsIfValid() {

        validator.validate(overtimeForm, errors);

        Mockito.verifyZeroInteractions(errors);
    }


    // Validate period -------------------------------------------------------------------------------------------------

    @Test
    public void ensureStartDateIsMandatory() {

        overtimeForm.setStartDate(null);

        validator.validate(overtimeForm, errors);

        Mockito.verify(errors).rejectValue("startDate", "error.entry.mandatory");
    }


    @Test
    public void ensureEndDateIsMandatory() {

        overtimeForm.setEndDate(null);

        validator.validate(overtimeForm, errors);

        Mockito.verify(errors).rejectValue("endDate", "error.entry.mandatory");
    }


    @Test
    public void ensureStartAndEndDateCanBeEquals() {

        DateMidnight now = DateMidnight.now();

        overtimeForm.setStartDate(now);
        overtimeForm.setEndDate(now);

        validator.validate(overtimeForm, errors);

        Mockito.verifyZeroInteractions(errors);
    }


    @Test
    public void ensureStartDateCanNotBeAfterEndDate() {

        overtimeForm.setStartDate(overtimeForm.getEndDate().plusDays(3));

        validator.validate(overtimeForm, errors);

        Mockito.verify(errors).rejectValue("endDate", "error.entry.invalidPeriod");
    }


    @Test
    public void ensureNoErrorMessageForMandatoryIfStartDateIsNullBecauseOfTypeMismatch() {

        Mockito.when(errors.hasFieldErrors("startDate")).thenReturn(true);

        overtimeForm.setStartDate(null);

        validator.validate(overtimeForm, errors);

        Mockito.verify(errors).hasFieldErrors("startDate");
        Mockito.verify(errors, Mockito.never()).rejectValue("startDate", "error.entry.mandatory");
    }


    @Test
    public void ensureNoErrorMessageForMandatoryIfEndDateIsNullBecauseOfTypeMismatch() {

        Mockito.when(errors.hasFieldErrors("endDate")).thenReturn(true);

        overtimeForm.setEndDate(null);

        validator.validate(overtimeForm, errors);

        Mockito.verify(errors).hasFieldErrors("endDate");
        Mockito.verify(errors, Mockito.never()).rejectValue("endDate", "error.entry.mandatory");
    }


    // Validate number of hours ----------------------------------------------------------------------------------------

    @Test
    public void ensureNumberOfHoursIsMandatory() {

        overtimeForm.setNumberOfHours(null);

        validator.validate(overtimeForm, errors);

        Mockito.verify(errors).rejectValue("numberOfHours", "error.entry.mandatory");
    }


    @Test
    public void ensureNumberOfHoursCanBeNegative() {

        overtimeForm.setNumberOfHours(BigDecimal.ONE.negate());

        validator.validate(overtimeForm, errors);

        Mockito.verifyZeroInteractions(errors);
    }


    @Test
    public void ensureNumberOfHoursCanBeZero() {

        overtimeForm.setNumberOfHours(BigDecimal.ZERO);

        validator.validate(overtimeForm, errors);

        Mockito.verifyZeroInteractions(errors);
    }


    @Test
    public void ensureNumberOfHoursCanBeADecimalNumber() {

        overtimeForm.setNumberOfHours(new BigDecimal("0.5"));

        validator.validate(overtimeForm, errors);

        Mockito.verifyZeroInteractions(errors);
    }


    @Test
    public void ensureNoErrorMessageForMandatoryIfNumberOfHoursIsNullBecauseOfTypeMismatch() {

        Mockito.when(errors.hasFieldErrors("numberOfHours")).thenReturn(true);

        overtimeForm.setNumberOfHours(null);

        validator.validate(overtimeForm, errors);

        Mockito.verify(errors).hasFieldErrors("numberOfHours");
        Mockito.verify(errors, Mockito.never()).rejectValue("endDate", "error.entry.mandatory");
    }


    // Validate using overtime settings --------------------------------------------------------------------------------

    @Test
    public void ensureCanNotRecordOvertimeIfOvertimeManagementIsDeactivated() {

        settings.getWorkingTimeSettings().setOvertimeActive(false);

        validator.validate(overtimeForm, errors);

        Mockito.verify(errors).reject("overtime.record.error.deactivated");
    }


    @Test
    public void ensureCanNotRecordOvertimeIfMaximumOvertimeIsZero() {

        settings.getWorkingTimeSettings().setMaximumOvertime(0);

        Mockito.when(overtimeServiceMock.getLeftOvertimeForPerson(Mockito.any(Person.class)))
            .thenReturn(BigDecimal.ZERO);

        // just not important how many number of hours, can not record overtime!
        overtimeForm.setNumberOfHours(BigDecimal.ZERO);

        validator.validate(overtimeForm, errors);

        Mockito.verify(errors).reject("overtime.record.error.deactivated");

        Mockito.verify(settingsServiceMock).getSettings();
        Mockito.verifyZeroInteractions(overtimeServiceMock);
    }


    @Test
    public void ensureCanRecordOvertimeIfMaximumOvertimeReachedButNotExceeded() {

        settings.getWorkingTimeSettings().setMaximumOvertime(16);

        Mockito.when(overtimeServiceMock.getLeftOvertimeForPerson(Mockito.any(Person.class)))
            .thenReturn(new BigDecimal("8"));

        overtimeForm.setNumberOfHours(new BigDecimal("8"));

        validator.validate(overtimeForm, errors);

        Mockito.verifyZeroInteractions(errors);

        Mockito.verify(settingsServiceMock).getSettings();
        Mockito.verify(overtimeServiceMock).getLeftOvertimeForPerson(overtimeForm.getPerson());
    }


    @Test
    public void ensureCanNotRecordOvertimeIfMaximumOvertimeExceeded() {

        settings.getWorkingTimeSettings().setMaximumOvertime(16);

        Mockito.when(overtimeServiceMock.getLeftOvertimeForPerson(Mockito.any(Person.class)))
            .thenReturn(new BigDecimal("8"));

        overtimeForm.setNumberOfHours(new BigDecimal("8.5"));

        validator.validate(overtimeForm, errors);

        Mockito.verify(errors)
            .rejectValue("numberOfHours", "overtime.data.numberOfHours.error.maxOvertime",
                new Object[] { new BigDecimal("8"), new BigDecimal("16") }, null);

        Mockito.verify(settingsServiceMock).getSettings();
        Mockito.verify(overtimeServiceMock).getLeftOvertimeForPerson(overtimeForm.getPerson());
    }


    @Test
    public void ensureCanNotRecordOvertimeIfMinimumOvertimeExceeded() {

        settings.getWorkingTimeSettings().setMinimumOvertime(10);

        Mockito.when(overtimeServiceMock.getLeftOvertimeForPerson(Mockito.any(Person.class)))
            .thenReturn(new BigDecimal("-9"));

        overtimeForm.setNumberOfHours(new BigDecimal("-1.5"));

        validator.validate(overtimeForm, errors);

        Mockito.verify(errors)
            .rejectValue("numberOfHours", "overtime.data.numberOfHours.error.minOvertime",
                new Object[] { new BigDecimal("-9"), new BigDecimal("10") }, null);

        Mockito.verify(settingsServiceMock).getSettings();
        Mockito.verify(overtimeServiceMock).getLeftOvertimeForPerson(overtimeForm.getPerson());
    }


    // Validate changes in existing overtime record --------------------------------------------------------------------

    @Test
    public void foo() throws IllegalAccessException {

        settings.getWorkingTimeSettings().setMaximumOvertime(100);

        Mockito.when(overtimeServiceMock.getLeftOvertimeForPerson(Mockito.any(Person.class)))
            .thenReturn(new BigDecimal("99.5"));

        overtimeForm.setNumberOfHours(new BigDecimal("2"));

        // ensure overtime form has ID
        Field idField = ReflectionUtils.findField(OvertimeForm.class, "id");
        idField.setAccessible(true);
        idField.set(overtimeForm, 42);

        Overtime originalOvertimeRecord = TestDataCreator.createOvertimeRecord();
        originalOvertimeRecord.setHours(new BigDecimal("3"));

        Mockito.when(overtimeServiceMock.getOvertimeById(Mockito.anyInt()))
            .thenReturn(Optional.of(originalOvertimeRecord));

        validator.validate(overtimeForm, errors);

        Mockito.verifyZeroInteractions(errors);

        Mockito.verify(overtimeServiceMock).getOvertimeById(overtimeForm.getId());
        Mockito.verify(settingsServiceMock).getSettings();
        Mockito.verify(overtimeServiceMock).getLeftOvertimeForPerson(overtimeForm.getPerson());
    }


    @Test
    public void ensureCanEditOvertimeRecordChangingPositiveHours() throws IllegalAccessException {

        settings.getWorkingTimeSettings().setMaximumOvertime(4);

        Mockito.when(overtimeServiceMock.getLeftOvertimeForPerson(Mockito.any(Person.class)))
            .thenReturn(new BigDecimal("3.5"));

        overtimeForm.setNumberOfHours(new BigDecimal("3"));

        // ensure overtime form has ID
        Field idField = ReflectionUtils.findField(OvertimeForm.class, "id");
        idField.setAccessible(true);
        idField.set(overtimeForm, 42);

        Overtime originalOvertimeRecord = TestDataCreator.createOvertimeRecord();
        originalOvertimeRecord.setHours(new BigDecimal("2.5"));

        Mockito.when(overtimeServiceMock.getOvertimeById(Mockito.anyInt()))
            .thenReturn(Optional.of(originalOvertimeRecord));

        validator.validate(overtimeForm, errors);

        Mockito.verifyZeroInteractions(errors);

        Mockito.verify(overtimeServiceMock).getOvertimeById(overtimeForm.getId());
        Mockito.verify(settingsServiceMock).getSettings();
        Mockito.verify(overtimeServiceMock).getLeftOvertimeForPerson(overtimeForm.getPerson());
    }


    @Test
    public void ensureCanEditOvertimeRecordChangingNegativeHours() throws IllegalAccessException {

        settings.getWorkingTimeSettings().setMinimumOvertime(4);

        Mockito.when(overtimeServiceMock.getLeftOvertimeForPerson(Mockito.any(Person.class)))
            .thenReturn(new BigDecimal("-3.5"));

        overtimeForm.setNumberOfHours(new BigDecimal("-3"));

        // ensure overtime form has ID
        Field idField = ReflectionUtils.findField(OvertimeForm.class, "id");
        idField.setAccessible(true);
        idField.set(overtimeForm, 42);

        Overtime originalOvertimeRecord = TestDataCreator.createOvertimeRecord();
        originalOvertimeRecord.setHours(new BigDecimal("-2.5"));

        Mockito.when(overtimeServiceMock.getOvertimeById(Mockito.anyInt()))
            .thenReturn(Optional.of(originalOvertimeRecord));

        validator.validate(overtimeForm, errors);

        Mockito.verifyZeroInteractions(errors);

        Mockito.verify(overtimeServiceMock).getOvertimeById(overtimeForm.getId());
        Mockito.verify(settingsServiceMock).getSettings();
        Mockito.verify(overtimeServiceMock).getLeftOvertimeForPerson(overtimeForm.getPerson());
    }


    // Validate comment ------------------------------------------------------------------------------------------------

    @Test
    public void ensureCommentIsNotMandatory() {

        Consumer<String> assertMayBeEmpty = (comment) -> {
            overtimeForm.setComment(comment);

            validator.validate(overtimeForm, errors);

            Mockito.verifyZeroInteractions(errors);
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

        Mockito.verify(errors).rejectValue("comment", "error.entry.tooManyChars");
    }
}
