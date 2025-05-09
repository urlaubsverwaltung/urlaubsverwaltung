package org.synyx.urlaubsverwaltung.overtime.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.overtime.OvertimeEntity;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Consumer;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createOvertimeRecord;

@ExtendWith(MockitoExtension.class)
class OvertimeFormDtoValidatorTest {

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
        assertThat(sut.supports(OvertimeFormDto.class)).isTrue();
    }

    @Test
    void ensureDoesNotSupportOtherClass() {
        assertThat(sut.supports(Person.class)).isFalse();
    }

    // Validate method -------------------------------------------------------------------------------------------------
    @Test
    void ensureNoErrorsIfValid() {

        final Settings settings = new Settings();
        settings.getOvertimeSettings().setOvertimeActive(true);
        when(settingsService.getSettings()).thenReturn(settings);

        when(overtimeService.getLeftOvertimeForPerson(any(Person.class))).thenReturn(Duration.ZERO);

        final OvertimeFormDto overtimeFormDto = new OvertimeFormDto(createOvertimeRecord());
        sut.validate(overtimeFormDto, errors);

        verifyNoInteractions(errors);
    }

    // Validate period -------------------------------------------------------------------------------------------------
    @Test
    void ensureStartDateIsMandatory() {

        final Settings settings = new Settings();
        settings.getOvertimeSettings().setOvertimeActive(true);
        when(settingsService.getSettings()).thenReturn(settings);
        when(overtimeService.getLeftOvertimeForPerson(any(Person.class))).thenReturn(Duration.ZERO);

        final OvertimeFormDto overtimeFormDto = new OvertimeFormDto(createOvertimeRecord());
        overtimeFormDto.setStartDate(null);

        sut.validate(overtimeFormDto, errors);

        verify(errors).rejectValue("startDate", "error.entry.mandatory");
    }


    @Test
    void ensureEndDateIsMandatory() {

        final Settings settings = new Settings();
        settings.getOvertimeSettings().setOvertimeActive(true);
        when(settingsService.getSettings()).thenReturn(settings);
        when(overtimeService.getLeftOvertimeForPerson(any(Person.class))).thenReturn(Duration.ZERO);

        final OvertimeFormDto overtimeFormDto = new OvertimeFormDto(createOvertimeRecord());
        overtimeFormDto.setEndDate(null);

        sut.validate(overtimeFormDto, errors);

        verify(errors).rejectValue("endDate", "error.entry.mandatory");
    }

    @Test
    void ensureStartAndEndDateCanBeEquals() {

        final Settings settings = new Settings();
        settings.getOvertimeSettings().setOvertimeActive(true);
        when(settingsService.getSettings()).thenReturn(settings);

        when(overtimeService.getLeftOvertimeForPerson(any(Person.class))).thenReturn(Duration.ZERO);

        LocalDate now = LocalDate.now(UTC);

        final OvertimeFormDto overtimeFormDto = new OvertimeFormDto(createOvertimeRecord());
        overtimeFormDto.setStartDate(now);
        overtimeFormDto.setEndDate(now);

        sut.validate(overtimeFormDto, errors);

        verifyNoInteractions(errors);
    }

    @Test
    void ensureStartDateCanNotBeAfterEndDate() {

        final Settings settings = new Settings();
        settings.getOvertimeSettings().setOvertimeActive(true);
        when(settingsService.getSettings()).thenReturn(settings);

        when(overtimeService.getLeftOvertimeForPerson(any(Person.class))).thenReturn(Duration.ZERO);

        final OvertimeFormDto overtimeFormDto = new OvertimeFormDto(createOvertimeRecord());
        overtimeFormDto.setStartDate(overtimeFormDto.getEndDate().plusDays(3));

        sut.validate(overtimeFormDto, errors);

        verify(errors).rejectValue("endDate", "error.entry.invalidPeriod");
    }

    @Test
    void ensureNoErrorMessageForMandatoryIfStartDateIsNullBecauseOfTypeMismatch() {

        final Settings settings = new Settings();
        settings.getOvertimeSettings().setOvertimeActive(true);
        when(settingsService.getSettings()).thenReturn(settings);
        when(errors.hasFieldErrors("startDate")).thenReturn(true);
        when(overtimeService.getLeftOvertimeForPerson(any(Person.class))).thenReturn(Duration.ZERO);

        final OvertimeFormDto overtimeFormDto = new OvertimeFormDto(createOvertimeRecord());
        overtimeFormDto.setStartDate(null);

        sut.validate(overtimeFormDto, errors);

        verify(errors).hasFieldErrors("startDate");
        verify(errors, never()).rejectValue("startDate", "error.entry.mandatory");
    }

    @Test
    void ensureNoErrorMessageForMandatoryIfEndDateIsNullBecauseOfTypeMismatch() {

        final Settings settings = new Settings();
        settings.getOvertimeSettings().setOvertimeActive(true);
        when(settingsService.getSettings()).thenReturn(settings);
        when(overtimeService.getLeftOvertimeForPerson(any(Person.class))).thenReturn(Duration.ZERO);
        when(errors.hasFieldErrors("endDate")).thenReturn(true);

        final OvertimeFormDto overtimeFormDto = new OvertimeFormDto(createOvertimeRecord());
        overtimeFormDto.setEndDate(null);

        sut.validate(overtimeFormDto, errors);

        verify(errors).hasFieldErrors("endDate");
        verify(errors, never()).rejectValue("endDate", "error.entry.mandatory");
    }

    // Validate number of hours ----------------------------------------------------------------------------------------
    @Test
    void ensureHoursOrMinutesIsMandatory() {

        final Settings settings = new Settings();
        settings.getOvertimeSettings().setOvertimeActive(true);
        when(settingsService.getSettings()).thenReturn(settings);

        final OvertimeFormDto overtimeFormDto = new OvertimeFormDto(createOvertimeRecord());
        overtimeFormDto.setHours(null);
        overtimeFormDto.setMinutes(null);

        sut.validate(overtimeFormDto, errors);

        verify(errors).rejectValue("hours", "overtime.error.hoursOrMinutesRequired");
        verify(errors).rejectValue("minutes", "overtime.error.hoursOrMinutesRequired");
    }

    @Test
    void ensureNumberOfHoursCanBeNegative() {

        final Settings settings = new Settings();
        settings.getOvertimeSettings().setOvertimeActive(true);
        when(settingsService.getSettings()).thenReturn(settings);

        when(overtimeService.getLeftOvertimeForPerson(any(Person.class))).thenReturn(Duration.ZERO);

        final OvertimeFormDto overtimeFormDto = new OvertimeFormDto(createOvertimeRecord());
        overtimeFormDto.setHours(BigDecimal.ONE.negate());
        overtimeFormDto.setMinutes(0);

        sut.validate(overtimeFormDto, errors);

        verifyNoInteractions(errors);
    }

    @Test
    void ensureNumberOfHoursCanBeZero() {

        final Settings settings = new Settings();
        settings.getOvertimeSettings().setOvertimeActive(true);
        when(settingsService.getSettings()).thenReturn(settings);

        when(overtimeService.getLeftOvertimeForPerson(any(Person.class))).thenReturn(Duration.ZERO);

        final OvertimeFormDto overtimeFormDto = new OvertimeFormDto(createOvertimeRecord());
        overtimeFormDto.setHours(BigDecimal.ZERO);
        overtimeFormDto.setMinutes(0);

        sut.validate(overtimeFormDto, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureMinutes() {

        final Settings settings = new Settings();
        settings.getOvertimeSettings().setOvertimeActive(true);
        when(settingsService.getSettings()).thenReturn(settings);

        when(overtimeService.getLeftOvertimeForPerson(any(Person.class))).thenReturn(Duration.ZERO);

        final OvertimeFormDto overtimeFormDto = new OvertimeFormDto(createOvertimeRecord());
        overtimeFormDto.setHours(BigDecimal.ZERO);
        overtimeFormDto.setMinutes(30);

        sut.validate(overtimeFormDto, errors);
        verifyNoInteractions(errors);
    }

    @ParameterizedTest
    @CsvSource({"1, 30, true", "-1, -30, false", "0, -30, false", "-1, 0, false"})
    void ensureOvertimeReductionIsAllowedWhenFeatureIsEnabled(long hour, int minutes, boolean isReduce) {

        final Settings settings = new Settings();
        settings.getOvertimeSettings().setOvertimeActive(true);
        settings.getOvertimeSettings().setOvertimeReductionWithoutApplicationActive(true);
        when(settingsService.getSettings()).thenReturn(settings);

        when(overtimeService.getLeftOvertimeForPerson(any())).thenReturn(Duration.ofHours(42));

        final OvertimeFormDto overtimeFormDto = new OvertimeFormDto(createOvertimeRecord());
        overtimeFormDto.setHours(BigDecimal.valueOf(hour));
        overtimeFormDto.setMinutes(minutes);
        overtimeFormDto.setReduce(isReduce);

        sut.validate(overtimeFormDto, errors);

        verifyNoInteractions(errors);
    }

    @ParameterizedTest
    @CsvSource({"1, 30, true", "-1, -30, false", "0, -30, false", "-1, 0, false"})
    void ensureOvertimeReductionIsNotAllowedWhenFeatureIsDisabled(long hour, int minutes, boolean isReduce) {

        final Settings settings = new Settings();
        settings.getOvertimeSettings().setOvertimeActive(true);
        settings.getOvertimeSettings().setOvertimeReductionWithoutApplicationActive(false);
        when(settingsService.getSettings()).thenReturn(settings);

        final OvertimeFormDto overtimeFormDto = new OvertimeFormDto(createOvertimeRecord());
        overtimeFormDto.setHours(BigDecimal.valueOf(hour));
        overtimeFormDto.setMinutes(minutes);
        overtimeFormDto.setReduce(isReduce);

        sut.validate(overtimeFormDto, errors);

        verify(errors).rejectValue("reduce", "overtime.error.overtimeReductionNotAllowed");
    }

    // Validate using overtime settings --------------------------------------------------------------------------------
    @Test
    void ensureCanNotRecordOvertimeIfOvertimeManagementIsDeactivated() {

        final Settings settings = new Settings();
        settings.getOvertimeSettings().setOvertimeActive(false);
        when(settingsService.getSettings()).thenReturn(settings);

        final OvertimeFormDto overtimeFormDto = new OvertimeFormDto(createOvertimeRecord());

        sut.validate(overtimeFormDto, errors);
        verify(errors).reject("overtime.record.error.deactivated");
    }

    @Test
    void ensureCanNotRecordOvertimeIfMaximumOvertimeIsZero() {
        final Settings settings = new Settings();
        settings.getOvertimeSettings().setOvertimeActive(true);
        settings.getOvertimeSettings().setMaximumOvertime(0);
        when(settingsService.getSettings()).thenReturn(settings);

        final OvertimeFormDto overtimeFormDto = new OvertimeFormDto(createOvertimeRecord());
        // just not important how many number of hours, can not record overtime!
        overtimeFormDto.setHours(BigDecimal.ZERO);
        overtimeFormDto.setMinutes(0);

        sut.validate(overtimeFormDto, errors);

        verify(errors).reject("overtime.record.error.deactivated");

        verify(settingsService).getSettings();
        verifyNoInteractions(overtimeService);
    }

    @Test
    void ensureCanRecordOvertimeIfMaximumOvertimeReachedButNotExceeded() {
        final Settings settings = new Settings();
        settings.getOvertimeSettings().setOvertimeActive(true);
        settings.getOvertimeSettings().setMaximumOvertime(16);
        when(settingsService.getSettings()).thenReturn(settings);

        when(overtimeService.getLeftOvertimeForPerson(any(Person.class))).thenReturn(Duration.ofHours(8));

        final OvertimeFormDto overtimeFormDto = new OvertimeFormDto(createOvertimeRecord());
        overtimeFormDto.setHours(new BigDecimal(8));
        overtimeFormDto.setMinutes(0);

        sut.validate(overtimeFormDto, errors);
        verifyNoInteractions(errors);
        verify(settingsService).getSettings();
        verify(overtimeService).getLeftOvertimeForPerson(overtimeFormDto.getPerson());
    }


    @Test
    void ensureCanNotRecordOvertimeIfMaximumOvertimeExceeded() {
        final Settings settings = new Settings();
        settings.getOvertimeSettings().setOvertimeActive(true);
        settings.getOvertimeSettings().setMaximumOvertime(16);
        when(settingsService.getSettings()).thenReturn(settings);

        when(overtimeService.getLeftOvertimeForPerson(any(Person.class))).thenReturn(Duration.ofDays(8));

        final OvertimeFormDto overtimeFormDto = new OvertimeFormDto(createOvertimeRecord());
        overtimeFormDto.setHours(new BigDecimal(8));
        overtimeFormDto.setMinutes(30);

        sut.validate(overtimeFormDto, errors);
        verify(errors).reject("overtime.data.numberOfHours.error.maxOvertime", new Object[]{16L}, null);
        verify(settingsService).getSettings();
        verify(overtimeService).getLeftOvertimeForPerson(overtimeFormDto.getPerson());
    }


    @Test
    void ensureCanNotRecordOvertimeIfMinimumOvertimeExceeded() {
        final Settings settings = new Settings();
        settings.getOvertimeSettings().setOvertimeActive(true);
        settings.getOvertimeSettings().setMinimumOvertime(10);
        when(settingsService.getSettings()).thenReturn(settings);

        when(overtimeService.getLeftOvertimeForPerson(any(Person.class))).thenReturn(Duration.ofHours(-9));

        final OvertimeFormDto overtimeFormDto = new OvertimeFormDto(createOvertimeRecord());
        overtimeFormDto.setReduce(true);
        overtimeFormDto.setHours(BigDecimal.ONE);
        overtimeFormDto.setMinutes(30);

        sut.validate(overtimeFormDto, errors);
        verify(errors).reject("overtime.data.numberOfHours.error.minOvertime", new Object[]{10L}, null);
        verify(settingsService).getSettings();
        verify(overtimeService).getLeftOvertimeForPerson(overtimeFormDto.getPerson());
    }

    // Validate changes in existing overtime record --------------------------------------------------------------------
    @Test
    void foo() {
        final Settings settings = new Settings();
        settings.getOvertimeSettings().setOvertimeActive(true);
        settings.getOvertimeSettings().setMaximumOvertime(100);
        when(settingsService.getSettings()).thenReturn(settings);

        when(overtimeService.getLeftOvertimeForPerson(any(Person.class))).thenReturn(Duration.ofMinutes(5970));

        final OvertimeFormDto overtimeFormDto = new OvertimeFormDto(createOvertimeRecord());
        overtimeFormDto.setHours(new BigDecimal(2));
        overtimeFormDto.setMinutes(0);
        overtimeFormDto.setId(42L);

        OvertimeEntity originalOvertimeRecord = createOvertimeRecord();
        originalOvertimeRecord.setDuration(Duration.ofHours(3));

        when(overtimeService.getOvertimeById(anyLong())).thenReturn(Optional.of(originalOvertimeRecord));

        sut.validate(overtimeFormDto, errors);
        verifyNoInteractions(errors);
        verify(overtimeService).getOvertimeById(overtimeFormDto.getId());
        verify(settingsService).getSettings();
        verify(overtimeService).getLeftOvertimeForPerson(overtimeFormDto.getPerson());
    }

    @Test
    void ensureCanEditOvertimeRecordChangingPositiveHours() {
        final Settings settings = new Settings();
        settings.getOvertimeSettings().setOvertimeActive(true);
        settings.getOvertimeSettings().setMaximumOvertime(4);
        when(settingsService.getSettings()).thenReturn(settings);

        when(overtimeService.getLeftOvertimeForPerson(any(Person.class)))
            .thenReturn(Duration.ofMinutes(210));

        final OvertimeFormDto overtimeFormDto = new OvertimeFormDto(createOvertimeRecord());
        overtimeFormDto.setHours(new BigDecimal(3));
        overtimeFormDto.setMinutes(0);
        overtimeFormDto.setId(42L);

        OvertimeEntity originalOvertimeRecord = createOvertimeRecord();
        originalOvertimeRecord.setDuration(Duration.ofMinutes(150));

        when(overtimeService.getOvertimeById(anyLong())).thenReturn(Optional.of(originalOvertimeRecord));

        sut.validate(overtimeFormDto, errors);
        verifyNoInteractions(errors);
        verify(overtimeService).getOvertimeById(overtimeFormDto.getId());
        verify(settingsService).getSettings();
        verify(overtimeService).getLeftOvertimeForPerson(overtimeFormDto.getPerson());
    }

    @Test
    void ensureCanEditOvertimeRecordChangingNegativeHours() {
        final Settings settings = new Settings();
        settings.getOvertimeSettings().setOvertimeActive(true);
        settings.getOvertimeSettings().setMinimumOvertime(4);
        when(settingsService.getSettings()).thenReturn(settings);

        when(overtimeService.getLeftOvertimeForPerson(any(Person.class)))
            .thenReturn(Duration.ofMinutes(210));

        final OvertimeFormDto overtimeFormDto = new OvertimeFormDto(createOvertimeRecord());
        overtimeFormDto.setReduce(true);
        overtimeFormDto.setHours(new BigDecimal(3));
        overtimeFormDto.setId(42L);

        OvertimeEntity originalOvertimeRecord = createOvertimeRecord();
        originalOvertimeRecord.setDuration(Duration.ofMinutes(-150));

        when(overtimeService.getOvertimeById(anyLong())).thenReturn(Optional.of(originalOvertimeRecord));

        sut.validate(overtimeFormDto, errors);
        verifyNoInteractions(errors);
        verify(overtimeService).getOvertimeById(overtimeFormDto.getId());
        verify(settingsService).getSettings();
        verify(overtimeService).getLeftOvertimeForPerson(overtimeFormDto.getPerson());
    }


    // Validate comment ------------------------------------------------------------------------------------------------

    @Test
    void ensureCommentInsideMaximumCharacterLength() {

        final Settings settings = new Settings();
        settings.getOvertimeSettings().setOvertimeActive(true);
        when(settingsService.getSettings()).thenReturn(settings);
        when(overtimeService.getLeftOvertimeForPerson(any(Person.class))).thenReturn(Duration.ZERO);

        final OvertimeFormDto overtimeFormDto = new OvertimeFormDto(createOvertimeRecord());
        overtimeFormDto.setComment(
            "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore "
                + "et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores");

        sut.validate(overtimeFormDto, errors);

        verifyNoInteractions(errors);
    }

    @Test
    void ensureCommentIsNotMandatory() {

        final Settings settings = new Settings();
        settings.getOvertimeSettings().setOvertimeActive(true);
        when(settingsService.getSettings()).thenReturn(settings);
        when(overtimeService.getLeftOvertimeForPerson(any(Person.class))).thenReturn(Duration.ZERO);

        final OvertimeFormDto overtimeFormDto = new OvertimeFormDto(createOvertimeRecord());
        final Consumer<String> assertMayBeEmpty = comment -> {
            overtimeFormDto.setComment(comment);

            sut.validate(overtimeFormDto, errors);

            verifyNoInteractions(errors);
        };

        assertMayBeEmpty.accept(null);
        assertMayBeEmpty.accept("");
    }
}
