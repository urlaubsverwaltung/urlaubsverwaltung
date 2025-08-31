package org.synyx.urlaubsverwaltung.overtime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.StaticMessageSource;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.application.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.vacationtype.ProvidedVacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonDeletedEvent;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.time.Month.AUGUST;
import static java.time.Month.JANUARY;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class OvertimeServiceImplTest {

    private OvertimeServiceImpl sut;

    @Mock
    private OvertimeRepository overtimeRepository;
    @Mock
    private OvertimeCommentRepository overtimeCommentRepository;
    @Mock
    private ApplicationService applicationService;
    @Mock
    private PersonService personService;
    @Mock
    private OvertimeMailService overtimeMailService;
    @Mock
    private SettingsService settingsService;

    private final Clock clock = Clock.systemUTC();

    @Captor
    private ArgumentCaptor<OvertimeEntity> overtimeEntityCaptor;
    @Captor
    private ArgumentCaptor<OvertimeCommentEntity> commentEntityCaptor;

    @BeforeEach
    void setUp() {
        sut = new OvertimeServiceImpl(overtimeRepository, overtimeCommentRepository, applicationService, personService, overtimeMailService, settingsService, clock);
    }

    @Nested
    class CreateOvertime {

        @Test
        void ensureCreateOvertime() {

            final PersonId authorId = new PersonId(1L);
            final Person author = new Person();
            author.setId(authorId.value());

            final PersonId ownerId = new PersonId(2L);
            final Person owner = new Person();
            owner.setId(ownerId.value());

            final OvertimeEntity savedOvertimeEntity = new OvertimeEntity();
            savedOvertimeEntity.setId(1L);
            savedOvertimeEntity.setPerson(owner);

            final OvertimeCommentEntity savedCommentEntity = new OvertimeCommentEntity(clock);
            savedCommentEntity.setId(1L);
            savedCommentEntity.setPerson(author);

            when(personService.getAllPersonsByIds(List.of(ownerId, authorId))).thenReturn(List.of(author, owner));
            when(overtimeRepository.save(any(OvertimeEntity.class))).thenReturn(savedOvertimeEntity);
            when(overtimeCommentRepository.save(any(OvertimeCommentEntity.class))).thenReturn(savedCommentEntity);

            final LocalDate startDate = LocalDate.now();
            final LocalDate endDate = LocalDate.now();
            final DateRange dateRange = new DateRange(startDate, endDate);
            final Duration duration = Duration.ofHours(8);

            final OvertimeEntity actual = sut.createOvertime(ownerId, dateRange, duration, authorId, "Foo Bar");

            assertThat(actual.getId()).isEqualTo(1L);

            verify(overtimeRepository).save(overtimeEntityCaptor.capture());
            assertThat(overtimeEntityCaptor.getValue()).satisfies(actualEntity -> {
                assertThat(actualEntity.getId()).isNull();
                assertThat(actualEntity.getPerson()).isSameAs(owner);
                assertThat(actualEntity.getStartDate()).isEqualTo(startDate);
                assertThat(actualEntity.getEndDate()).isEqualTo(endDate);
                assertThat(actualEntity.getDuration()).isEqualTo(duration);
            });
        }

        @Test
        void ensureCreateOvertimeWithComment() {

            final PersonId authorId = new PersonId(1L);
            final Person author = new Person();
            author.setId(authorId.value());

            final PersonId ownerId = new PersonId(2L);
            final Person owner = new Person();
            owner.setId(ownerId.value());

            final OvertimeEntity savedOvertimeEntity = new OvertimeEntity();
            savedOvertimeEntity.setId(1L);
            savedOvertimeEntity.setPerson(owner);

            final OvertimeCommentEntity savedCommentEntity = new OvertimeCommentEntity(clock);
            savedCommentEntity.setId(1L);
            savedCommentEntity.setPerson(author);

            when(personService.getAllPersonsByIds(List.of(ownerId, authorId))).thenReturn(List.of(author, owner));
            when(overtimeRepository.save(any(OvertimeEntity.class))).thenReturn(savedOvertimeEntity);
            when(overtimeCommentRepository.save(any(OvertimeCommentEntity.class))).thenReturn(savedCommentEntity);

            final DateRange dateRange = new DateRange(LocalDate.now(), LocalDate.now());
            final Duration duration = Duration.ofHours(8);

            sut.createOvertime(ownerId, dateRange, duration, authorId, "Foo Bar");

            verify(overtimeCommentRepository).save(commentEntityCaptor.capture());
            assertThat(commentEntityCaptor.getValue()).satisfies(actualComment -> {
                assertThat(actualComment.getId()).isNull();
                assertThat(actualComment.getPerson()).isEqualTo(author);
                assertThat(actualComment.getOvertime()).isSameAs(savedOvertimeEntity);
                assertThat(actualComment.getText()).isEqualTo("Foo Bar");
                assertThat(actualComment.getAction()).isEqualTo(OvertimeCommentAction.CREATED);
            });
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   "})
        @NullSource
        void ensureCreateOvertimeWithoutComment(String givenComment) {

            final PersonId authorId = new PersonId(1L);
            final Person author = new Person();
            author.setId(authorId.value());

            final PersonId ownerId = new PersonId(2L);
            final Person owner = new Person();
            owner.setId(ownerId.value());

            final OvertimeEntity savedOvertimeEntity = new OvertimeEntity();
            savedOvertimeEntity.setId(1L);
            savedOvertimeEntity.setPerson(owner);

            final OvertimeCommentEntity savedCommentEntity = new OvertimeCommentEntity(clock);
            savedCommentEntity.setId(1L);
            savedCommentEntity.setPerson(author);

            when(personService.getAllPersonsByIds(List.of(ownerId, authorId))).thenReturn(List.of(author, owner));
            when(overtimeRepository.save(any(OvertimeEntity.class))).thenReturn(savedOvertimeEntity);
            when(overtimeCommentRepository.save(any(OvertimeCommentEntity.class))).thenReturn(savedCommentEntity);

            final DateRange dateRange = new DateRange(LocalDate.now(), LocalDate.now());
            final Duration duration = Duration.ofHours(8);

            sut.createOvertime(ownerId, dateRange, duration, authorId, givenComment);

            verify(overtimeCommentRepository).save(commentEntityCaptor.capture());
            assertThat(commentEntityCaptor.getValue()).satisfies(actualComment -> {
                assertThat(actualComment.getId()).isNull();
                assertThat(actualComment.getPerson()).isEqualTo(author);
                assertThat(actualComment.getOvertime()).isSameAs(savedOvertimeEntity);
                assertThat(actualComment.getText()).isEqualTo("");
                assertThat(actualComment.getAction()).isEqualTo(OvertimeCommentAction.CREATED);
            });
        }

        @Test
        void ensureRecordingOvertimeSendsNotification() {

            final PersonId authorId = new PersonId(1L);
            final Person author = new Person();
            author.setId(authorId.value());

            when(personService.getAllPersonsByIds(List.of(authorId, authorId))).thenReturn(List.of(author));

            final OvertimeEntity overtime = new OvertimeEntity();
            overtime.setPerson(author);
            when(overtimeRepository.save(any(OvertimeEntity.class))).thenReturn(overtime);

            final OvertimeCommentEntity overtimeComment = new OvertimeCommentEntity(clock);
            when(overtimeCommentRepository.save(any())).thenReturn(overtimeComment);

            final DateRange dateRange = new DateRange(LocalDate.now(), LocalDate.now());
            final Duration duration = Duration.ofHours(8);
            sut.createOvertime(authorId, dateRange, duration, authorId, "Foo Bar");

            verify(overtimeMailService, never()).sendOvertimeNotificationToApplicantFromManagement(overtime, overtimeComment, author);
            verify(overtimeMailService).sendOvertimeNotificationToApplicantFromApplicant(overtime, overtimeComment);
            verify(overtimeMailService).sendOvertimeNotificationToManagement(overtime, overtimeComment);
        }

        @Test
        void ensureRecordingOvertimeSendsNotificationFromManagement() {

            final PersonId authorId = new PersonId(1L);
            final Person author = new Person();
            author.setId(authorId.value());

            final PersonId personId = new PersonId(2L);
            final Person person = new Person();
            person.setId(personId.value());

            final OvertimeEntity overtime = new OvertimeEntity();
            overtime.setId(1L);
            overtime.setPerson(person);

            final OvertimeCommentEntity overtimeComment = new OvertimeCommentEntity(clock);
            overtimeComment.setId(1L);
            overtimeComment.setPerson(author);

            when(personService.getAllPersonsByIds(List.of(personId, authorId))).thenReturn(List.of(person, author));
            when(overtimeRepository.save(any(OvertimeEntity.class))).thenReturn(overtime);
            when(overtimeCommentRepository.save(any(OvertimeCommentEntity.class))).thenReturn(overtimeComment);

            final DateRange dateRange = new DateRange(LocalDate.now(), LocalDate.now());
            final Duration duration = Duration.ofHours(8);
            sut.createOvertime(personId, dateRange, duration, authorId, "Foo Bar");

            verify(overtimeMailService, never()).sendOvertimeNotificationToApplicantFromApplicant(overtime, overtimeComment);
            verify(overtimeMailService).sendOvertimeNotificationToApplicantFromManagement(overtime, overtimeComment, author);
            verify(overtimeMailService).sendOvertimeNotificationToManagement(overtime, overtimeComment);
        }
    }

    @Nested
    class UpdateOvertime {

        @Test
        void ensureUpdatesOvertime() {

            final PersonId authorId = new PersonId(1L);
            final Person author = new Person();
            author.setId(authorId.value());

            final PersonId ownerId = new PersonId(2L);
            final Person owner = new Person();
            owner.setId(ownerId.value());

            final OvertimeEntity savedOvertimeEntity = new OvertimeEntity();
            savedOvertimeEntity.setId(1L);
            savedOvertimeEntity.setPerson(owner);

            final OvertimeCommentEntity savedCommentEntity = new OvertimeCommentEntity(clock);
            savedCommentEntity.setId(1L);
            savedCommentEntity.setPerson(author);

            when(personService.getAllPersonsByIds(List.of(ownerId, authorId))).thenReturn(List.of(author, owner));
            when(overtimeRepository.findById(anyLong())).thenReturn(Optional.of(savedOvertimeEntity));
            when(overtimeRepository.save(any(OvertimeEntity.class))).thenReturn(savedOvertimeEntity);
            when(overtimeCommentRepository.save(any(OvertimeCommentEntity.class))).thenReturn(savedCommentEntity);

            final LocalDate startDate = LocalDate.now();
            final LocalDate endDate = LocalDate.now();
            final DateRange dateRange = new DateRange(startDate, endDate);
            final Duration duration = Duration.ofHours(8);

            final OvertimeEntity actual = sut.updateOvertime(1L, dateRange, duration, authorId, "Foo Bar");

            assertThat(actual.getId()).isEqualTo(1L);

            verify(overtimeRepository).save(overtimeEntityCaptor.capture());
            assertThat(overtimeEntityCaptor.getValue()).satisfies(actualEntity -> {
                assertThat(actualEntity.getId()).isEqualTo(1L);
                assertThat(actualEntity.getPerson()).isSameAs(owner);
                assertThat(actualEntity.getStartDate()).isEqualTo(startDate);
                assertThat(actualEntity.getEndDate()).isEqualTo(endDate);
                assertThat(actualEntity.getDuration()).isEqualTo(duration);
            });
        }

        @Test
        void ensureUpdatesOvertimeWithComment() {

            final PersonId authorId = new PersonId(1L);
            final Person author = new Person();
            author.setId(authorId.value());

            final PersonId ownerId = new PersonId(2L);
            final Person owner = new Person();
            owner.setId(ownerId.value());

            final OvertimeEntity savedOvertimeEntity = new OvertimeEntity();
            savedOvertimeEntity.setId(1L);
            savedOvertimeEntity.setPerson(owner);

            final OvertimeCommentEntity savedCommentEntity = new OvertimeCommentEntity(clock);
            savedCommentEntity.setId(1L);
            savedCommentEntity.setPerson(author);

            when(personService.getAllPersonsByIds(List.of(ownerId, authorId))).thenReturn(List.of(author, owner));
            when(overtimeRepository.findById(anyLong())).thenReturn(Optional.of(savedOvertimeEntity));
            when(overtimeRepository.save(any(OvertimeEntity.class))).thenReturn(savedOvertimeEntity);
            when(overtimeCommentRepository.save(any(OvertimeCommentEntity.class))).thenReturn(savedCommentEntity);

            final DateRange dateRange = new DateRange(LocalDate.now(), LocalDate.now());
            final Duration duration = Duration.ofHours(8);
            sut.updateOvertime(1L, dateRange, duration, authorId, "Foo Bar");

            verify(overtimeCommentRepository).save(commentEntityCaptor.capture());
            assertThat(commentEntityCaptor.getValue()).satisfies(actualComment -> {
                assertThat(actualComment.getId()).isNull();
                assertThat(actualComment.getPerson()).isEqualTo(author);
                assertThat(actualComment.getOvertime()).isSameAs(savedOvertimeEntity);
                assertThat(actualComment.getText()).isEqualTo("Foo Bar");
                assertThat(actualComment.getAction()).isEqualTo(OvertimeCommentAction.EDITED);
            });
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   "})
        @NullSource
        void ensureUpdatesOvertimeWithoutComment(String givenComment) {

            final PersonId authorId = new PersonId(1L);
            final Person author = new Person();
            author.setId(authorId.value());

            final PersonId ownerId = new PersonId(2L);
            final Person owner = new Person();
            owner.setId(ownerId.value());

            final OvertimeEntity savedOvertimeEntity = new OvertimeEntity();
            savedOvertimeEntity.setId(1L);
            savedOvertimeEntity.setPerson(owner);

            final OvertimeCommentEntity savedCommentEntity = new OvertimeCommentEntity(clock);
            savedCommentEntity.setId(1L);
            savedCommentEntity.setPerson(author);

            when(personService.getAllPersonsByIds(List.of(ownerId, authorId))).thenReturn(List.of(author, owner));
            when(overtimeRepository.findById(anyLong())).thenReturn(Optional.of(savedOvertimeEntity));
            when(overtimeRepository.save(any(OvertimeEntity.class))).thenReturn(savedOvertimeEntity);
            when(overtimeCommentRepository.save(any(OvertimeCommentEntity.class))).thenReturn(savedCommentEntity);

            final DateRange dateRange = new DateRange(LocalDate.now(), LocalDate.now());
            final Duration duration = Duration.ofHours(8);

            sut.updateOvertime(1L, dateRange, duration, authorId, givenComment);

            verify(overtimeCommentRepository).save(commentEntityCaptor.capture());
            assertThat(commentEntityCaptor.getValue()).satisfies(actualComment -> {
                assertThat(actualComment.getId()).isNull();
                assertThat(actualComment.getPerson()).isEqualTo(author);
                assertThat(actualComment.getOvertime()).isSameAs(savedOvertimeEntity);
                assertThat(actualComment.getText()).isEqualTo("");
                assertThat(actualComment.getAction()).isEqualTo(OvertimeCommentAction.EDITED);
            });
        }

        @Test
        void ensureUpdatingOvertimeSendsNotification() {

            final PersonId authorId = new PersonId(1L);
            final Person author = new Person();
            author.setId(authorId.value());

            final OvertimeEntity overtime = new OvertimeEntity();
            overtime.setId(1L);
            overtime.setPerson(author);

            final OvertimeCommentEntity overtimeComment = new OvertimeCommentEntity(clock);
            overtimeComment.setId(1L);
            overtimeComment.setPerson(author);

            when(personService.getAllPersonsByIds(List.of(authorId, authorId))).thenReturn(List.of(author));
            when(overtimeRepository.findById(anyLong())).thenReturn(Optional.of(overtime));
            when(overtimeRepository.save(overtime)).thenReturn(overtime);
            when(overtimeCommentRepository.save(any())).thenReturn(overtimeComment);

            final DateRange dateRange = new DateRange(LocalDate.now(), LocalDate.now());
            final Duration duration = Duration.ofHours(8);
            sut.updateOvertime(1L, dateRange, duration, authorId, "Foo Bar");

            verify(overtimeMailService, never()).sendOvertimeNotificationToApplicantFromManagement(overtime, overtimeComment, author);
            verify(overtimeMailService).sendOvertimeNotificationToApplicantFromApplicant(overtime, overtimeComment);
            verify(overtimeMailService).sendOvertimeNotificationToManagement(overtime, overtimeComment);
        }

        @Test
        void ensureUpdatingOvertimeSendsNotificationFromManagement() {

            final PersonId authorId = new PersonId(1L);
            final Person author = new Person();
            author.setId(authorId.value());

            final PersonId personId = new PersonId(2L);
            final Person person = new Person();
            person.setId(personId.value());

            final OvertimeEntity overtime = new OvertimeEntity();
            overtime.setId(1L);
            overtime.setPerson(person);

            final OvertimeCommentEntity overtimeComment = new OvertimeCommentEntity(clock);
            overtimeComment.setId(1L);
            overtimeComment.setPerson(author);

            when(personService.getAllPersonsByIds(List.of(personId, authorId))).thenReturn(List.of(author, person));
            when(overtimeRepository.findById(1L)).thenReturn(Optional.of(overtime));
            when(overtimeRepository.save(any(OvertimeEntity.class))).thenReturn(overtime);
            when(overtimeCommentRepository.save(any())).thenReturn(overtimeComment);

            final DateRange dateRange = new DateRange(LocalDate.now(), LocalDate.now());
            final Duration duration = Duration.ofHours(8);
            sut.updateOvertime(1L, dateRange, duration, authorId, "Foo Bar");

            verify(overtimeMailService, never()).sendOvertimeNotificationToApplicantFromApplicant(overtime, overtimeComment);
            verify(overtimeMailService).sendOvertimeNotificationToApplicantFromManagement(overtime, overtimeComment, author);
            verify(overtimeMailService).sendOvertimeNotificationToManagement(overtime, overtimeComment);
        }
    }

    // Get overtime record by ID ---------------------------------------------------------------------------------------
    @Test
    void ensureGetByIDCallsCorrectDAOMethod() {

        sut.getOvertimeById(42L);

        verify(overtimeRepository).findById(42L);
    }

    @Test
    void ensureReturnsEmptyOptionalIfNoOvertimeFoundForID() {

        when(overtimeRepository.findById(anyLong())).thenReturn(Optional.empty());

        final Optional<OvertimeEntity> maybeOvertime = sut.getOvertimeById(42L);
        assertThat(maybeOvertime).isEmpty();
    }

    // Get overtime comments -------------------------------------------------------------------------------------------
    @Test
    void ensureGetCommentsCorrectDAOMethod() {

        final OvertimeEntity overtime = new OvertimeEntity();
        sut.getCommentsForOvertime(overtime);

        verify(overtimeCommentRepository).findByOvertimeOrderByIdDesc(overtime);
    }

    // Get total overtime for year -------------------------------------------------------------------------------------
    @Test
    void ensureReturnsZeroIfPersonHasNoOvertimeRecordsYetForTheGivenYear() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate firstDayOfYear = LocalDate.of(2016, 1, 1);
        final LocalDate lastDayOfYear = LocalDate.of(2016, 12, 31);
        when(overtimeRepository.findByPersonAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(person, firstDayOfYear, lastDayOfYear)).thenReturn(List.of());

        final Duration totalHours = sut.getTotalOvertimeForPersonAndYear(person, 2016);
        assertThat(totalHours).isEqualTo(Duration.ZERO);
    }

    @Test
    void ensureReturnsCorrectYearOvertimeForPerson() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final OvertimeEntity overtimeRecord = new OvertimeEntity(person, LocalDate.of(2016, 1, 5), LocalDate.of(2016, 1, 5), Duration.ofHours(1));
        final OvertimeEntity otherOvertimeRecord = new OvertimeEntity(person, LocalDate.of(2016, 2, 5), LocalDate.of(2016, 2, 5), Duration.ofHours(10));

        final LocalDate firstDayOfYear = LocalDate.of(2016, 1, 1);
        final LocalDate lastDayOfYear = LocalDate.of(2016, 12, 31);
        when(overtimeRepository.findByPersonAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(person, firstDayOfYear, lastDayOfYear)).thenReturn(List.of(overtimeRecord, otherOvertimeRecord));

        final Duration totalHours = sut.getTotalOvertimeForPersonAndYear(person, 2016);
        assertThat(totalHours).isEqualTo(Duration.ofHours(11));
    }

    @Test
    void ensureGetTotalOvertimeForPersonBeforeYear() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final OvertimeEntity overtime = new OvertimeEntity(person, LocalDate.of(2016, 1, 5), LocalDate.of(2016, 1, 5), Duration.ofHours(10));
        final OvertimeEntity overtime2 = new OvertimeEntity(person, LocalDate.of(2016, 2, 5), LocalDate.of(2016, 2, 5), Duration.ofHours(4));

        final LocalDate firstDayOfYear = LocalDate.of(2017, 1, 1);
        final LocalDate lastDayOfBeforeYear = firstDayOfYear.minusYears(1).with(lastDayOfYear());
        when(overtimeRepository.findByPersonAndStartDateIsBefore(person, firstDayOfYear)).thenReturn(List.of(overtime, overtime2));
        when(applicationService.getTotalOvertimeReductionOfPersonUntil(person, lastDayOfBeforeYear)).thenReturn(Duration.ofHours(1));

        final Duration totalHours = sut.getTotalOvertimeForPersonBeforeYear(person, 2017);
        assertThat(totalHours).isEqualTo(Duration.ofHours(13));
    }

    // Get left overtime -----------------------------------------------------------------------------------------------
    @Test
    void ensureReturnsZeroAsLeftOvertimeIfPersonHasNoOvertimeRecordsYet() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        when(overtimeRepository.calculateTotalHoursForPerson(person)).thenReturn(Optional.empty());
        when(applicationService.getTotalOvertimeReductionOfPerson(person)).thenReturn(Duration.ZERO);

        final Duration totalHours = sut.getLeftOvertimeForPerson(person);
        assertThat(totalHours).isEqualTo(Duration.ZERO);

        verify(overtimeRepository).calculateTotalHoursForPerson(person);
        verify(applicationService).getTotalOvertimeReductionOfPerson(person);
    }

    @Test
    void ensureTheLeftOvertimeIsTheDifferenceBetweenTotalOvertimeAndOvertimeReduction() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        when(overtimeRepository.calculateTotalHoursForPerson(person)).thenReturn(Optional.of((double) Duration.ofHours(10L).toMinutes() / 60));
        when(applicationService.getTotalOvertimeReductionOfPerson(person)).thenReturn(Duration.ofHours(1));

        final Duration leftOvertime = sut.getLeftOvertimeForPerson(person);
        assertThat(leftOvertime).isEqualTo(Duration.ofHours(9));

        verify(overtimeRepository).calculateTotalHoursForPerson(person);
        verify(applicationService).getTotalOvertimeReductionOfPerson(person);
    }

    @Test
    void ensureTheLeftOvertimeIsZeroIfPersonHasNeitherOvertimeRecordsNorOvertimeReduction() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        when(overtimeRepository.calculateTotalHoursForPerson(person)).thenReturn(Optional.empty());
        when(applicationService.getTotalOvertimeReductionOfPerson(person)).thenReturn(Duration.ZERO);

        final Duration leftOvertime = sut.getLeftOvertimeForPerson(person);
        assertThat(leftOvertime).isEqualTo(Duration.ZERO);
    }

    @Test
    void ensureToExcludeApplicationsIdsForLeftOvertimeForPerson() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        when(overtimeRepository.calculateTotalHoursForPerson(person)).thenReturn(Optional.of((double) Duration.ofHours(10L).toMinutes() / 60));
        when(applicationService.getTotalOvertimeReductionOfPerson(person)).thenReturn(Duration.ofHours(5));

        final VacationType<?> overtimeVacationType = ProvidedVacationType.builder(new StaticMessageSource()).id(1L).category(OVERTIME).build();
        final Application applicationToEdit = new Application();
        applicationToEdit.setId(1L);
        applicationToEdit.setPerson(person);
        applicationToEdit.setStatus(ApplicationStatus.ALLOWED);
        applicationToEdit.setVacationType(overtimeVacationType);
        applicationToEdit.setHours(Duration.ofHours(1L));
        applicationToEdit.setStartDate(LocalDate.now(clock).withMonth(JANUARY.getValue()));
        applicationToEdit.setEndDate(LocalDate.now(clock).withMonth(JANUARY.getValue()));

        when(applicationService.findApplicationsByIds(List.of(applicationToEdit.getId()))).thenReturn(List.of(applicationToEdit));

        final Duration totalHours = sut.getLeftOvertimeForPerson(person, List.of(overtimeVacationType.getId()));
        assertThat(totalHours).isEqualTo(Duration.ofHours(6));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void ensureCannotCreateOvertimeIfOvertimeSyncIsActive(boolean overtimeWritePrivilegedOnly) {

        final Person signedInUser = new Person();
        signedInUser.setPermissions(List.of(USER, OFFICE));
        final Person personOfOvertime = new Person();
        when(settingsService.getSettings()).thenReturn(overtimeSettings(overtimeWritePrivilegedOnly, false, true));

        assertThat(sut.isUserIsAllowedToCreateOvertime(signedInUser, personOfOvertime)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void ensureCannotCreateOvertimeIfOvertimeIsNotActive(boolean overtimeWritePrivilegedOnly) {

        final Person signedInUser = new Person();
        signedInUser.setPermissions(List.of(USER, OFFICE));
        final Person personOfOvertime = new Person();
        when(settingsService.getSettings()).thenReturn(overtimeSettings(overtimeWritePrivilegedOnly, false, false));

        assertThat(sut.isUserIsAllowedToCreateOvertime(signedInUser, personOfOvertime)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void ensureOfficeIsAllowedToCreateOthersOvertime(boolean overtimeWritePrivilegedOnly) {

        final Person signedInUser = new Person();
        signedInUser.setPermissions(List.of(USER, OFFICE));
        final Person personOfOvertime = new Person();
        when(settingsService.getSettings()).thenReturn(overtimeSettings(overtimeWritePrivilegedOnly, true, false));

        assertThat(sut.isUserIsAllowedToCreateOvertime(signedInUser, personOfOvertime)).isTrue();
    }

    @Test
    void ensureUserIsNotAllowedToCreateOwnOvertimeWithPrivilegedRestriction() {

        final Person person = new Person();
        person.setPermissions(List.of(USER));

        when(settingsService.getSettings()).thenReturn(overtimeSettings(true, true, false));

        assertThat(sut.isUserIsAllowedToCreateOvertime(person, person)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"OFFICE", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY", "BOSS"})
    void ensurePrivilegedPersonIsAllowedToCreateOwnOvertimeWithPrivilegedRestriction(Role role) {

        final Person person = new Person();
        person.setPermissions(List.of(USER, role));

        when(settingsService.getSettings()).thenReturn(overtimeSettings(true, true, false));

        assertThat(sut.isUserIsAllowedToCreateOvertime(person, person)).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"OFFICE", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY", "BOSS", "USER"})
    void ensurePersonIsAllowedToCreateOwnOvertimeWithoutPrivilegedRestriction(Role role) {

        final Person person = new Person();
        person.setPermissions(List.of(USER, role));

        when(settingsService.getSettings()).thenReturn(overtimeSettings(false, true, false));

        assertThat(sut.isUserIsAllowedToCreateOvertime(person, person)).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY", "BOSS", "USER"})
    void ensurePersonIsNotAllowedToCreateOthersOvertimeWithNoPrivilegedRestriction(Role role) {

        final Person person = new Person();
        person.setPermissions(List.of(USER, role));
        final Person other = new Person();

        when(settingsService.getSettings()).thenReturn(overtimeSettings(false, true, false));

        assertThat(sut.isUserIsAllowedToCreateOvertime(person, other)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY", "BOSS", "OFFICE"})
    void ensurePersonIsAllowedToCreateOthersOvertimeWithPrivilegedRestriction(Role role) {

        final Person signedInUser = new Person();
        signedInUser.setPermissions(List.of(USER, role));
        final Person personOfOvertime = new Person();

        when(settingsService.getSettings()).thenReturn(overtimeSettings(true, true, false));

        assertThat(sut.isUserIsAllowedToCreateOvertime(signedInUser, personOfOvertime)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void ensureCannotUpdateOvertimeIfOvertimeIsFromExternal(boolean overtimeWritePrivilegedOnly) {

        final Person signedInUser = new Person();
        signedInUser.setPermissions(List.of(USER, OFFICE));
        final Person personOfOvertime = new Person();
        when(settingsService.getSettings()).thenReturn(overtimeSettings(overtimeWritePrivilegedOnly, false, true));

        final OvertimeEntity overtime = new OvertimeEntity(personOfOvertime, LocalDate.now(clock), LocalDate.now(clock), Duration.ofHours(1), true);

        assertThat(sut.isUserIsAllowedToUpdateOvertime(signedInUser, personOfOvertime, overtime)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void ensureCannotUpdateOvertimeIfOvertimeIsNotActive(boolean overtimeWritePrivilegedOnly) {

        final Person signedInUser = new Person();
        signedInUser.setPermissions(List.of(USER, OFFICE));
        final Person personOfOvertime = new Person();
        when(settingsService.getSettings()).thenReturn(overtimeSettings(overtimeWritePrivilegedOnly, false, false));

        final OvertimeEntity overtime = new OvertimeEntity(personOfOvertime, LocalDate.now(clock), LocalDate.now(clock), Duration.ofHours(1), false);

        assertThat(sut.isUserIsAllowedToUpdateOvertime(signedInUser, personOfOvertime, overtime)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void ensureOfficeIsAllowedToUpdateOthersOvertime(boolean overtimeWritePrivilegedOnly) {

        final Person signedInUser = new Person();
        signedInUser.setPermissions(List.of(USER, OFFICE));
        final Person personOfOvertime = new Person();
        when(settingsService.getSettings()).thenReturn(overtimeSettings(overtimeWritePrivilegedOnly, true, false));

        final OvertimeEntity overtime = new OvertimeEntity(personOfOvertime, LocalDate.now(clock), LocalDate.now(clock), Duration.ofHours(1), false);

        assertThat(sut.isUserIsAllowedToUpdateOvertime(signedInUser, personOfOvertime, overtime)).isTrue();
    }

    @Test
    void ensureUserIsNotAllowedToUpdateOwnOvertimeWithPrivilegedRestriction() {

        final Person person = new Person();
        person.setPermissions(List.of(USER));

        when(settingsService.getSettings()).thenReturn(overtimeSettings(true, true, false));

        final OvertimeEntity overtime = new OvertimeEntity(person, LocalDate.now(clock), LocalDate.now(clock), Duration.ofHours(1), false);

        assertThat(sut.isUserIsAllowedToUpdateOvertime(person, person, overtime)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"OFFICE", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY", "BOSS"})
    void ensurePrivilegedPersonIsAllowedToUpdateOwnOvertimeWithPrivilegedRestriction(Role role) {

        final Person person = new Person();
        person.setPermissions(List.of(USER, role));

        when(settingsService.getSettings()).thenReturn(overtimeSettings(true, true, false));

        final OvertimeEntity overtime = new OvertimeEntity(person, LocalDate.now(clock), LocalDate.now(clock), Duration.ofHours(1), false);

        assertThat(sut.isUserIsAllowedToUpdateOvertime(person, person, overtime)).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"OFFICE", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY", "BOSS", "USER"})
    void ensurePersonIsAllowedToUpdateOwnOvertimeWithoutPrivilegedRestriction(Role role) {

        final Person person = new Person();
        person.setPermissions(List.of(USER, role));

        when(settingsService.getSettings()).thenReturn(overtimeSettings(false, true, false));

        final OvertimeEntity overtime = new OvertimeEntity(person, LocalDate.now(clock), LocalDate.now(clock), Duration.ofHours(1), false);

        assertThat(sut.isUserIsAllowedToUpdateOvertime(person, person, overtime)).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY", "BOSS", "USER"})
    void ensurePersonIsNotAllowedToUpdateOthersOvertimeWithNoPrivilegedRestriction(Role role) {

        final Person person = new Person();
        person.setPermissions(List.of(USER, role));
        final Person other = new Person();

        when(settingsService.getSettings()).thenReturn(overtimeSettings(false, true, false));

        final OvertimeEntity overtime = new OvertimeEntity(other, LocalDate.now(clock), LocalDate.now(clock), Duration.ofHours(1), false);

        assertThat(sut.isUserIsAllowedToUpdateOvertime(person, other, overtime)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY", "BOSS", "OFFICE"})
    void ensurePersonIsAllowedToUpdateOthersOvertimeWithPrivilegedRestriction(Role role) {

        final Person signedInUser = new Person();
        signedInUser.setPermissions(List.of(USER, role));
        final Person personOfOvertime = new Person();

        when(settingsService.getSettings()).thenReturn(overtimeSettings(true, true, false));

        final OvertimeEntity overtime = new OvertimeEntity(personOfOvertime, LocalDate.now(clock), LocalDate.now(clock), Duration.ofHours(1), false);

        assertThat(sut.isUserIsAllowedToUpdateOvertime(signedInUser, personOfOvertime, overtime)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void ensureCannotAddCommentOvertimeIfOvertimeIsNotActive(boolean overtimeWritePrivilegedOnly) {

        final Person signedInUser = new Person();
        signedInUser.setPermissions(List.of(USER, OFFICE));
        final Person personOfOvertime = new Person();
        when(settingsService.getSettings()).thenReturn(overtimeSettings(overtimeWritePrivilegedOnly, false, false));

        assertThat(sut.isUserIsAllowedToAddOvertimeComment(signedInUser, personOfOvertime)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void ensureOfficeIsAllowedToAddCommentOthersOvertime(boolean overtimeWritePrivilegedOnly) {

        final Person signedInUser = new Person();
        signedInUser.setPermissions(List.of(USER, OFFICE));
        final Person personOfOvertime = new Person();
        when(settingsService.getSettings()).thenReturn(overtimeSettings(overtimeWritePrivilegedOnly, true, false));

        assertThat(sut.isUserIsAllowedToAddOvertimeComment(signedInUser, personOfOvertime)).isTrue();
    }

    @Test
    void ensureUserIsNotAllowedToAddCommentOwnOvertimeWithPrivilegedRestriction() {

        final Person person = new Person();
        person.setPermissions(List.of(USER));

        when(settingsService.getSettings()).thenReturn(overtimeSettings(true, true, false));

        assertThat(sut.isUserIsAllowedToAddOvertimeComment(person, person)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"OFFICE", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY", "BOSS"})
    void ensurePrivilegedPersonIsAllowedToAddCommentOwnOvertimeWithPrivilegedRestriction(Role role) {

        final Person person = new Person();
        person.setPermissions(List.of(USER, role));

        when(settingsService.getSettings()).thenReturn(overtimeSettings(true, true, false));

        assertThat(sut.isUserIsAllowedToAddOvertimeComment(person, person)).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"OFFICE", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY", "BOSS", "USER"})
    void ensurePersonIsAllowedToAddCommentOwnOvertimeWithoutPrivilegedRestriction(Role role) {

        final Person person = new Person();
        person.setPermissions(List.of(USER, role));

        when(settingsService.getSettings()).thenReturn(overtimeSettings(false, true, false));

        assertThat(sut.isUserIsAllowedToAddOvertimeComment(person, person)).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY", "BOSS", "USER"})
    void ensurePersonIsNotAllowedToAddCommentOthersOvertimeWithNoPrivilegedRestriction(Role role) {

        final Person person = new Person();
        person.setPermissions(List.of(USER, role));
        final Person other = new Person();

        when(settingsService.getSettings()).thenReturn(overtimeSettings(false, true, false));

        assertThat(sut.isUserIsAllowedToAddOvertimeComment(person, other)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY", "BOSS", "OFFICE"})
    void ensurePersonIsAllowedToAddCommentOthersOvertimeWithPrivilegedRestriction(Role role) {

        final Person signedInUser = new Person();
        signedInUser.setPermissions(List.of(USER, role));
        final Person personOfOvertime = new Person();

        when(settingsService.getSettings()).thenReturn(overtimeSettings(true, true, false));

        assertThat(sut.isUserIsAllowedToAddOvertimeComment(signedInUser, personOfOvertime)).isTrue();
    }

    @Test
    void ensureGetLeftOvertimeTotalAndDateRangeForPersons() {
        final LocalDate from = LocalDate.now(clock).withMonth(AUGUST.getValue()).with(firstDayOfMonth());
        final LocalDate to = LocalDate.now(clock).withMonth(AUGUST.getValue()).with(lastDayOfMonth());

        final Person person = new Person();
        person.setId(1L);

        final Person person2 = new Person();
        person2.setId(2L);

        final List<Person> persons = List.of(person, person2);

        final OvertimeEntity overtimeOne = new OvertimeEntity();
        overtimeOne.setPerson(person);
        overtimeOne.setStartDate(from);
        overtimeOne.setEndDate(from.plusDays(1));
        overtimeOne.setDuration(Duration.ofHours(1));

        final OvertimeEntity overtimeOneOne = new OvertimeEntity();
        overtimeOneOne.setPerson(person);
        overtimeOneOne.setStartDate(to.plusDays(1));
        overtimeOneOne.setEndDate(to.plusDays(1));
        overtimeOneOne.setDuration(Duration.ofHours(1));

        final OvertimeEntity overtimeTwo = new OvertimeEntity();
        overtimeTwo.setPerson(person2);
        overtimeTwo.setStartDate(from.plusDays(4));
        overtimeTwo.setEndDate(from.plusDays(4));
        overtimeTwo.setDuration(Duration.ofHours(10));

        final OvertimeEntity overtimeThree = new OvertimeEntity();
        overtimeThree.setPerson(person2);
        overtimeThree.setStartDate(to.plusDays(4));
        overtimeThree.setEndDate(to.plusDays(4));
        overtimeThree.setDuration(Duration.ofHours(10));

        when(overtimeRepository.findByPersonIsInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(persons, from.with(firstDayOfYear()), to.with(lastDayOfYear())))
            .thenReturn(List.of(overtimeOne, overtimeOneOne, overtimeTwo, overtimeThree));
        when(overtimeRepository.findByPersonIsInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(persons, from, to))
            .thenReturn(List.of(overtimeOne, overtimeTwo));

        final List<Application> applications = List.of();

        final Map<Person, LeftOvertime> actual = sut.getLeftOvertimeTotalAndDateRangeForPersons(persons, applications, from, to);
        assertThat(actual)
            .hasSize(2)
            .containsKey(person)
            .containsKey(person2);

        final LeftOvertime leftOvertime = actual.get(person);
        assertThat(leftOvertime.leftOvertimeOverall()).isEqualTo(Duration.ofHours(2));
        assertThat(leftOvertime.leftOvertimeDateRange()).isEqualTo(Duration.ofHours(1));

        final LeftOvertime leftOvertime2 = actual.get(person2);
        assertThat(leftOvertime2.leftOvertimeOverall()).isEqualTo(Duration.ofHours(20));
        assertThat(leftOvertime2.leftOvertimeDateRange()).isEqualTo(Duration.ofHours(10));
    }

    @Test
    void ensureGetLeftOvertimeTotalAndDateRangeForPersonsWithOvertimeReduction() {
        final LocalDate from = LocalDate.now(clock).withMonth(AUGUST.getValue()).with(firstDayOfMonth());
        final LocalDate to = LocalDate.now(clock).withMonth(AUGUST.getValue()).with(lastDayOfMonth());

        final Person person = new Person();
        person.setId(1L);

        final Person person2 = new Person();
        person2.setId(2L);

        final List<Person> persons = List.of(person, person2);

        final OvertimeEntity overtimeOne = new OvertimeEntity();
        overtimeOne.setPerson(person);
        overtimeOne.setStartDate(from);
        overtimeOne.setEndDate(from.plusDays(1));
        overtimeOne.setDuration(Duration.ofHours(1));

        final OvertimeEntity overtimeOneOne = new OvertimeEntity();
        overtimeOneOne.setPerson(person);
        overtimeOneOne.setStartDate(to.plusDays(1));
        overtimeOneOne.setEndDate(to.plusDays(1));
        overtimeOneOne.setDuration(Duration.ofHours(1));

        final OvertimeEntity overtimeTwo = new OvertimeEntity();
        overtimeTwo.setPerson(person2);
        overtimeTwo.setStartDate(from.plusDays(4));
        overtimeTwo.setEndDate(from.plusDays(4));
        overtimeTwo.setDuration(Duration.ofHours(10));

        final OvertimeEntity overtimeTwoTwo = new OvertimeEntity();
        overtimeTwoTwo.setPerson(person2);
        overtimeTwoTwo.setStartDate(to.plusDays(4));
        overtimeTwoTwo.setEndDate(to.plusDays(4));
        overtimeTwoTwo.setDuration(Duration.ofHours(10));

        when(overtimeRepository.findByPersonIsInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(persons, from.with(firstDayOfYear()), to.with(lastDayOfYear())))
            .thenReturn(List.of(overtimeOne, overtimeOneOne, overtimeTwo, overtimeTwoTwo));
        when(overtimeRepository.findByPersonIsInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(persons, from, to))
            .thenReturn(List.of(overtimeOne, overtimeTwo));

        final VacationType<?> overtimeVacationType = ProvidedVacationType.builder(new StaticMessageSource())
            .id(1L)
            .category(OVERTIME)
            .build();

        final Application personOvertimeReduction = new Application();
        personOvertimeReduction.setId(1L);
        personOvertimeReduction.setPerson(person);
        personOvertimeReduction.setStatus(ApplicationStatus.ALLOWED);
        personOvertimeReduction.setVacationType(overtimeVacationType);
        personOvertimeReduction.setHours(Duration.ofMinutes(90));
        // overtime reduction should result in `overall`. NOT in `date range`.
        personOvertimeReduction.setStartDate(LocalDate.now(clock).withMonth(JANUARY.getValue()));
        personOvertimeReduction.setEndDate(LocalDate.now(clock).withMonth(JANUARY.getValue()));

        final List<Application> applications = List.of(personOvertimeReduction);

        final Map<Person, LeftOvertime> actual = sut.getLeftOvertimeTotalAndDateRangeForPersons(persons, applications, from, to);
        assertThat(actual)
            .hasSize(2)
            .containsKey(person)
            .containsKey(person2);

        final LeftOvertime leftOvertime = actual.get(person);
        assertThat(leftOvertime.leftOvertimeOverall()).isEqualTo(Duration.ofMinutes(30));
        assertThat(leftOvertime.leftOvertimeDateRange()).isEqualTo(Duration.ofHours(1));

        final LeftOvertime leftOvertime2 = actual.get(person2);
        assertThat(leftOvertime2.leftOvertimeOverall()).isEqualTo(Duration.ofHours(20));
        assertThat(leftOvertime2.leftOvertimeDateRange()).isEqualTo(Duration.ofHours(10));
    }

    @Test
    void ensureGetLeftOvertimeTotalAndDateRangeForPersonsIncludesEntriesForPersonsWithoutOvertimeReduction() {
        final LocalDate from = LocalDate.now(clock).withMonth(AUGUST.getValue()).with(firstDayOfMonth());
        final LocalDate to = LocalDate.now(clock).withMonth(AUGUST.getValue()).with(lastDayOfMonth());

        final Person personWithoutOvertime = new Person();
        personWithoutOvertime.setId(1L);

        final List<Person> persons = List.of(personWithoutOvertime);

        when(overtimeRepository.findByPersonIsInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(persons, from.with(firstDayOfYear()), to.with(lastDayOfYear()))).thenReturn(List.of());
        when(overtimeRepository.findByPersonIsInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(persons, from, to)).thenReturn(List.of());

        final List<Application> applications = List.of();

        final Map<Person, LeftOvertime> actual = sut.getLeftOvertimeTotalAndDateRangeForPersons(persons, applications, from, to);
        assertThat(actual)
            .hasSize(1)
            .containsKey(personWithoutOvertime);

        final LeftOvertime leftOvertime = actual.get(personWithoutOvertime);
        assertThat(leftOvertime).isNotNull();
        assertThat(leftOvertime.leftOvertimeOverall()).isEqualTo(Duration.ZERO);
        assertThat(leftOvertime.leftOvertimeDateRange()).isEqualTo(Duration.ZERO);
    }

    @Test
    void ensureDeletionOnPersonDeletionEvent() {
        final Person person = new Person();

        sut.deleteAll(new PersonDeletedEvent(person));

        final InOrder inOrder = inOrder(overtimeCommentRepository, overtimeRepository);
        inOrder.verify(overtimeCommentRepository).deleteByOvertimePerson(person);
        inOrder.verify(overtimeRepository).deleteByPerson(person);
    }

    @Test
    void ensuresToSaveComment() {

        final LocalDate from = LocalDate.now(clock).withMonth(AUGUST.getValue()).with(firstDayOfMonth());
        final LocalDate to = LocalDate.now(clock).withMonth(AUGUST.getValue()).with(lastDayOfMonth());

        final Person person = new Person();
        person.setId(1L);

        final OvertimeEntity overtimeEntity = new OvertimeEntity();
        overtimeEntity.setPerson(person);
        overtimeEntity.setStartDate(from);
        overtimeEntity.setEndDate(to);
        overtimeEntity.setDuration(Duration.ofHours(1));

        final OvertimeCommentEntity overtimeCommentEntity = new OvertimeCommentEntity(clock);
        overtimeCommentEntity.setOvertime(overtimeEntity);
        overtimeCommentEntity.setAction(OvertimeCommentAction.CREATED);
        overtimeCommentEntity.setPerson(person);
        overtimeCommentEntity.setText("Foo Bar");

        when(overtimeCommentRepository.save(any())).thenAnswer(args -> {
            final OvertimeCommentEntity toBeSaved = args.getArgument(0);
            toBeSaved.setId(1L);
            return toBeSaved;
        });

        sut.saveComment(overtimeEntity, OvertimeCommentAction.COMMENTED, "Foo Bar", person);

        final ArgumentCaptor<OvertimeCommentEntity> commentCaptor = ArgumentCaptor.forClass(OvertimeCommentEntity.class);
        verify(overtimeCommentRepository).save(commentCaptor.capture());

        final OvertimeCommentEntity comment = commentCaptor.getValue();
        assertThat(comment.getText()).isEqualTo("Foo Bar");
        assertThat(comment.getAction()).isEqualTo(OvertimeCommentAction.COMMENTED);
        assertThat(comment.getPerson()).isEqualTo(person);
        assertThat(comment.getOvertime()).isEqualTo(overtimeEntity);
    }

    @Test
    void ensureToRetrieveExternalOvertimeByDate() {

        final LocalDate date = LocalDate.now(clock).withMonth(AUGUST.getValue()).with(firstDayOfMonth());

        final Person person = new Person();
        person.setId(1L);

        final OvertimeEntity overtime = new OvertimeEntity(person, date, date, Duration.ofHours(1), true);
        when(overtimeRepository.findByPersonIdAndStartDateAndEndDateAndExternalIsTrue(person.getId(), date, date)).thenReturn(Optional.of(overtime));

        sut.getExternalOvertimeByDate(date, person.getId());

        verify(overtimeRepository).findByPersonIdAndStartDateAndEndDateAndExternalIsTrue(person.getId(), date, date);
    }

    private Settings overtimeSettings(boolean overtimeWritePrivilegedOnly, boolean overtimeActive, boolean overtimeSyncActive) {

        final Settings settings = new Settings();
        final OvertimeSettings overtimeSettings = settings.getOvertimeSettings();
        overtimeSettings.setOvertimeWritePrivilegedOnly(overtimeWritePrivilegedOnly);
        overtimeSettings.setOvertimeActive(overtimeActive);
        overtimeSettings.setOvertimeSyncActive(overtimeSyncActive);

        return settings;
    }
}
