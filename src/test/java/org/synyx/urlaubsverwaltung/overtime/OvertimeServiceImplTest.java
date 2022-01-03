package org.synyx.urlaubsverwaltung.overtime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.TestDataCreator;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class OvertimeServiceImplTest {

    private OvertimeServiceImpl sut;

    @Mock
    private OvertimeRepository overtimeRepository;
    @Mock
    private OvertimeCommentRepository commentDAO;
    @Mock
    private ApplicationService applicationService;
    @Mock
    private OvertimeMailService overtimeMailService;
    @Mock
    private SettingsService settingsService;

    private final Clock clock = Clock.systemUTC();

    @BeforeEach
    void setUp() {
        sut = new OvertimeServiceImpl(overtimeRepository, commentDAO, applicationService, overtimeMailService, settingsService, clock);
    }

    // Record overtime -------------------------------------------------------------------------------------------------
    @Test
    void ensurePersistsOvertimeAndComment() {

        final Overtime overtime = new Overtime();
        final Person author = new Person();

        sut.record(overtime, Optional.of("Foo Bar"), author);

        verify(overtimeRepository).save(overtime);
        verify(commentDAO).save(any(OvertimeComment.class));
    }

    @Test
    void ensureRecordingUpdatesLastModificationDate() {

        final Person author = new Person();

        final Overtime overtime = sut.record(new Overtime(), Optional.empty(), author);
        assertThat(overtime.getLastModificationDate()).isEqualTo(LocalDate.now(clock));
    }

    @Test
    void ensureRecordingOvertimeSendsNotification() {

        final Overtime overtime = new Overtime();
        final Person author = new Person();

        sut.record(overtime, Optional.of("Foo Bar"), author);

        verify(overtimeMailService).sendOvertimeNotification(eq(overtime), any(OvertimeComment.class));
    }

    @Test
    void ensureCreatesCommentWithCorrectActionForNewOvertime() {

        final Overtime overtime = new Overtime();
        final Person author = new Person();

        sut.record(overtime, Optional.empty(), author);

        final ArgumentCaptor<OvertimeComment> commentCaptor = ArgumentCaptor.forClass(OvertimeComment.class);
        verify(commentDAO).save(commentCaptor.capture());

        final OvertimeComment comment = commentCaptor.getValue();
        assertThat(comment).isNotNull();
        assertThat(comment.getAction()).isEqualTo(OvertimeCommentAction.CREATED);
    }

    @Test
    void ensureCreatesCommentWithCorrectActionForExistentOvertime() {

        final Overtime overtime = new Overtime();
        overtime.setId(1);
        final Person author = new Person();

        sut.record(overtime, Optional.empty(), author);

        final ArgumentCaptor<OvertimeComment> commentCaptor = ArgumentCaptor.forClass(OvertimeComment.class);
        verify(commentDAO).save(commentCaptor.capture());
        final OvertimeComment comment = commentCaptor.getValue();
        assertThat(comment).isNotNull();
        assertThat(comment.getAction()).isEqualTo(OvertimeCommentAction.EDITED);
    }

    @Test
    void ensureCreatedCommentWithoutTextHasCorrectProperties() {

        final Overtime overtime = new Overtime();
        final Person author = new Person();

        sut.record(overtime, Optional.empty(), author);

        final ArgumentCaptor<OvertimeComment> commentCaptor = ArgumentCaptor.forClass(OvertimeComment.class);
        verify(commentDAO).save(commentCaptor.capture());
        final OvertimeComment comment = commentCaptor.getValue();
        assertThat(comment).isNotNull();
        assertThat(comment.getPerson()).isEqualTo(author);
        assertThat(comment.getOvertime()).isEqualTo(overtime);
        assertThat(comment.getText()).isNull();
    }

    @Test
    void ensureCreatedCommentWithTextHasCorrectProperties() {

        final Overtime overtime = new Overtime();
        final Person author = new Person();

        sut.record(overtime, Optional.of("Foo"), author);

        final ArgumentCaptor<OvertimeComment> commentCaptor = ArgumentCaptor.forClass(OvertimeComment.class);
        verify(commentDAO).save(commentCaptor.capture());
        final OvertimeComment comment = commentCaptor.getValue();
        assertThat(comment).isNotNull();
        assertThat(comment.getPerson()).isEqualTo(author);
        assertThat(comment.getOvertime()).isEqualTo(overtime);
        assertThat(comment.getText()).isEqualTo("Foo");
    }

    // Get overtime records for person ---------------------------------------------------------------------------------
    @Test
    void ensureGetForPersonCallsCorrectDAOMethod() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        sut.getOvertimeRecordsForPerson(person);

        verify(overtimeRepository).findByPerson(person);
    }

    // Get overtime record by ID ---------------------------------------------------------------------------------------
    @Test
    void ensureGetByIDCallsCorrectDAOMethod() {

        sut.getOvertimeById(42);

        verify(overtimeRepository).findById(42);
    }

    @Test
    void ensureReturnsEmptyOptionalIfNoOvertimeFoundForID() {

        when(overtimeRepository.findById(anyInt())).thenReturn(Optional.empty());

        final Optional<Overtime> maybeOvertime = sut.getOvertimeById(42);
        assertThat(maybeOvertime).isEmpty();
    }

    // Get overtime records for person and year ------------------------------------------------------------------------
    @Test
    void ensureGetRecordsByPersonAndYearCallsCorrectDAOMethod() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        sut.getOvertimeRecordsForPersonAndYear(person, 2015);

        final LocalDate firstDay = LocalDate.of(2015, 1, 1);
        final LocalDate lastDay = LocalDate.of(2015, 12, 31);
        verify(overtimeRepository).findByPersonAndPeriod(person, firstDay, lastDay);
    }

    // Get overtime comments -------------------------------------------------------------------------------------------
    @Test
    void ensureGetCommentsCorrectDAOMethod() {

        final Overtime overtime = new Overtime();
        sut.getCommentsForOvertime(overtime);

        verify(commentDAO).findByOvertime(overtime);
    }

    // Get total overtime for year -------------------------------------------------------------------------------------
    @Test
    void ensureThrowsIfTryingToGetYearOvertimeForNegativeYear() {
        assertThatIllegalArgumentException().isThrownBy(() -> sut.getTotalOvertimeForPersonAndYear(new Person("muster", "Muster", "Marlene", "muster@example.org"), -1));
    }

    @Test
    void ensureThrowsIfTryingToGetYearOvertimeForZeroYear() {
        assertThatIllegalArgumentException().isThrownBy(() -> sut.getTotalOvertimeForPersonAndYear(new Person("muster", "Muster", "Marlene", "muster@example.org"), 0));
    }

    @Test
    void ensureReturnsZeroIfPersonHasNoOvertimeRecordsYetForTheGivenYear() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        when(overtimeRepository.findByPersonAndPeriod(eq(person), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(Collections.emptyList());

        final Duration totalHours = sut.getTotalOvertimeForPersonAndYear(person, 2016);
        assertThat(totalHours).isEqualTo(Duration.ZERO);

        final LocalDate firstDayOfYear = LocalDate.of(2016, 1, 1);
        final LocalDate lastDayOfYear = LocalDate.of(2016, 12, 31);
        verify(overtimeRepository).findByPersonAndPeriod(person, firstDayOfYear, lastDayOfYear);
    }

    @Test
    void ensureReturnsCorrectYearOvertimeForPerson() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final Overtime overtimeRecord = TestDataCreator.createOvertimeRecord(person);
        overtimeRecord.setDuration(Duration.ofHours(1));

        final Overtime otherOvertimeRecord = TestDataCreator.createOvertimeRecord(person);
        otherOvertimeRecord.setDuration(Duration.ofHours(10));

        when(overtimeRepository.findByPersonAndPeriod(eq(person), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(List.of(overtimeRecord, otherOvertimeRecord));

        final Duration totalHours = sut.getTotalOvertimeForPersonAndYear(person, 2016);
        assertThat(totalHours).isEqualTo(Duration.ofHours(11));

        final LocalDate firstDayOfYear = LocalDate.of(2016, 1, 1);
        final LocalDate lastDayOfYear = LocalDate.of(2016, 12, 31);
        verify(overtimeRepository).findByPersonAndPeriod(person, firstDayOfYear, lastDayOfYear);
    }

    // Get left overtime -----------------------------------------------------------------------------------------------
    @Test
    void ensureReturnsZeroAsLeftOvertimeIfPersonHasNoOvertimeRecordsYet() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        when(overtimeRepository.calculateTotalHoursForPerson(person)).thenReturn(null);
        when(applicationService.getTotalOvertimeReductionOfPerson(person)).thenReturn(Duration.ZERO);

        final Duration totalHours = sut.getLeftOvertimeForPerson(person);
        assertThat(totalHours).isEqualTo(Duration.ZERO);

        verify(overtimeRepository).calculateTotalHoursForPerson(person);
        verify(applicationService).getTotalOvertimeReductionOfPerson(person);
    }

    @Test
    void ensureTheLeftOvertimeIsTheDifferenceBetweenTotalOvertimeAndOvertimeReduction() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        when(overtimeRepository.calculateTotalHoursForPerson(person)).thenReturn((double) Duration.ofHours(10L).toMinutes() / 60);
        when(applicationService.getTotalOvertimeReductionOfPerson(person)).thenReturn(Duration.ofHours(1));

        final Duration leftOvertime = sut.getLeftOvertimeForPerson(person);
        assertThat(leftOvertime).isEqualTo(Duration.ofHours(9));

        verify(overtimeRepository).calculateTotalHoursForPerson(person);
        verify(applicationService).getTotalOvertimeReductionOfPerson(person);
    }

    @Test
    void ensureTheLeftOvertimeIsZeroIfPersonHasNeitherOvertimeRecordsNorOvertimeReduction() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        when(overtimeRepository.calculateTotalHoursForPerson(person)).thenReturn(null);
        when(applicationService.getTotalOvertimeReductionOfPerson(person)).thenReturn(Duration.ZERO);

        final Duration leftOvertime = sut.getLeftOvertimeForPerson(person);
        assertThat(leftOvertime).isEqualTo(Duration.ZERO);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void ensureOfficeIsAllowedToWriteOthersOvertime(boolean overtimeWritePrivilegedOnly) {

        final Person signedInUser = new Person();
        signedInUser.setPermissions(List.of(OFFICE));
        final Person personOfOvertime = new Person();
        when(settingsService.getSettings()).thenReturn(overtimeSettings(overtimeWritePrivilegedOnly));

        assertThat(sut.isUserIsAllowedToWriteOvertime(signedInUser, personOfOvertime)).isTrue();
    }

    @Test
    void ensureUserIsNotAllowedToWriteOwnOvertimeWithPrivilegedRestriction() {

        final Person person = new Person();
        person.setPermissions(List.of(USER));

        when(settingsService.getSettings()).thenReturn(overtimeSettings(true));

        assertThat(sut.isUserIsAllowedToWriteOvertime(person, person)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"OFFICE", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY", "BOSS"})
    void ensurePrivilegedPersonIsAllowedToWriteOwnOvertimeWithPrivilegedRestriction(Role role) {

        final Person person = new Person();
        person.setPermissions(List.of(role));

        when(settingsService.getSettings()).thenReturn(overtimeSettings(true));

        assertThat(sut.isUserIsAllowedToWriteOvertime(person, person)).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"OFFICE", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY", "BOSS", "USER"})
    void ensurePersonIsAllowedToWriteOwnOvertimeWithoutPrivilegedRestriction(Role role) {

        final Person person = new Person();
        person.setPermissions(List.of(role));

        when(settingsService.getSettings()).thenReturn(overtimeSettings(false));

        assertThat(sut.isUserIsAllowedToWriteOvertime(person, person)).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY", "BOSS", "USER"})
    void ensurePersonIsNotAllowedToWriteOthersOvertimeWithNoPrivilegedRestriction(Role role) {

        final Person person = new Person();
        person.setPermissions(List.of(role));
        final Person other = new Person();

        when(settingsService.getSettings()).thenReturn(overtimeSettings(false));

        assertThat(sut.isUserIsAllowedToWriteOvertime(person, other)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY", "BOSS", "USER"})
    void ensurePersonIsNotAllowedToWriteOthersOvertimeWithPrivilegedRestriction(Role role) {

        final Person person = new Person();
        person.setPermissions(List.of(role));
        final Person other = new Person();

        when(settingsService.getSettings()).thenReturn(overtimeSettings(true));

        assertThat(sut.isUserIsAllowedToWriteOvertime(person, other)).isFalse();
    }

    private Settings overtimeSettings(boolean overtimeWritePrivilegedOnly) {

        final Settings settings = new Settings();
        settings.getOvertimeSettings().setOvertimeWritePrivilegedOnly(overtimeWritePrivilegedOnly);

        return settings;
    }
}
