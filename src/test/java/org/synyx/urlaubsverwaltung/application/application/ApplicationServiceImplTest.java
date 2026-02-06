package org.synyx.urlaubsverwaltung.application.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.application.vacationtype.ProvidedVacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeEntity;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonDeletedEvent;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarService;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.time.Duration.ZERO;
import static java.util.Locale.JAPANESE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.activeStatuses;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.CYAN;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarFactory.fullWorkday;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarFactory.workingTimeCalendar;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceImplTest {

    private ApplicationServiceImpl sut;

    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private WorkingTimeCalendarService workingTimeCalendarService;
    @Mock
    private MessageSource messageSource;

    @BeforeEach
    void setUp() {
        sut = new ApplicationServiceImpl(applicationRepository, workingTimeCalendarService, messageSource);
    }

    @Nested
    class GetApplicationById {

        @Test
        void ensureGetApplicationByIdCallsCorrectDaoMethod() {
            sut.getApplicationById(1234L);
            verify(applicationRepository).findById(1234L);
        }

        @Test
        void ensureGetApplicationByIdReturnsAbsentOptionalIfNoOneExists() {
            final Optional<Application> optional = sut.getApplicationById(1234L);
            assertThat(optional).isEmpty();
        }
    }

    @Nested
    class FindApplicationsByIds {

        @Test
        void ensureFindApplicationsByIds() {

            when(messageSource.getMessage("vacation-type-message-key", new Object[]{}, JAPANESE)).thenReturn("vacation type label");

            final LocalDate applicationDate = LocalDate.now();
            final LocalDate cancelDate = LocalDate.now();
            final LocalDate editedDate = LocalDate.now();
            final Person applier = new Person();
            final Person boss = new Person();
            final Person canceller = new Person();
            final LocalDate endDate = LocalDate.now();
            final LocalTime startTime = LocalTime.now();
            final LocalTime endTime = LocalTime.now();
            final Person person = new Person();
            final LocalDate startDate = LocalDate.now();
            final VacationTypeEntity vacationTypeEntity = new VacationTypeEntity();
            vacationTypeEntity.setId(2L);
            vacationTypeEntity.setActive(true);
            vacationTypeEntity.setColor(CYAN);
            vacationTypeEntity.setMessageKey("vacation-type-message-key");
            vacationTypeEntity.setRequiresApprovalToApply(true);
            vacationTypeEntity.setVisibleToEveryone(true);
            vacationTypeEntity.setCategory(OVERTIME);
            final LocalDate remindDate = LocalDate.now();
            final LocalDate upcomingHolidayReplacementNotificationSend = LocalDate.now();
            final LocalDate upcomingApplicationsReminderSend = LocalDate.now();
            final HolidayReplacementEntity holidayReplacement = new HolidayReplacementEntity();

            final ApplicationEntity entity = new ApplicationEntity();
            entity.setId(1337L);
            entity.setAddress("address");
            entity.setApplicationDate(applicationDate);
            entity.setCancelDate(cancelDate);
            entity.setEditedDate(editedDate);
            entity.setApplier(applier);
            entity.setBoss(boss);
            entity.setCanceller(canceller);
            entity.setTwoStageApproval(true);
            entity.setEndDate(endDate);
            entity.setStartTime(startTime);
            entity.setEndTime(endTime);
            entity.setDayLength(DayLength.NOON);
            entity.setPerson(person);
            entity.setReason("reason");
            entity.setStartDate(startDate);
            entity.setStatus(WAITING);
            entity.setVacationType(vacationTypeEntity);
            entity.setRemindDate(remindDate);
            entity.setTeamInformed(true);
            entity.setHours(Duration.ofHours(2));
            entity.setUpcomingHolidayReplacementNotificationSend(upcomingHolidayReplacementNotificationSend);
            entity.setUpcomingApplicationsReminderSend(upcomingApplicationsReminderSend);
            entity.setHolidayReplacements(List.of(holidayReplacement));

            when(applicationRepository.findAllById(List.of(1337L))).thenReturn(List.of(entity));

            final List<Application> actual = sut.findApplicationsByIds(List.of(1337L));

            assertThat(actual).hasSize(1);
            assertThat(actual.get(0)).satisfies(application -> {
                assertThat(application.getId()).isEqualTo(1337L);
                assertThat(application.getAddress()).isEqualTo("address");
                assertThat(application.getApplicationDate()).isSameAs(applicationDate);
                assertThat(application.getCancelDate()).isSameAs(cancelDate);
                assertThat(application.getEditedDate()).isSameAs(editedDate);
                assertThat(application.getApplier()).isSameAs(applier);
                assertThat(application.getBoss()).isSameAs(boss);
                assertThat(application.getCanceller()).isSameAs(canceller);
                assertThat(application.isTwoStageApproval()).isTrue();
                assertThat(application.getEndDate()).isSameAs(endDate);
                assertThat(application.getStartTime()).isSameAs(startTime);
                assertThat(application.getEndTime()).isSameAs(endTime);
                assertThat(application.getDayLength()).isEqualTo(DayLength.NOON);
                assertThat(application.getPerson()).isSameAs(person);
                assertThat(application.getReason()).isEqualTo("reason");
                assertThat(application.getStartDate()).isSameAs(startDate);
                assertThat(application.getStatus()).isEqualTo(WAITING);
                assertThat(application.getVacationType()).satisfies(vacationType -> {
                    assertThat(vacationType.getId()).isEqualTo(2L);
                    assertThat(vacationType.isActive()).isTrue();
                    assertThat(vacationType.getColor()).isEqualTo(CYAN);
                    assertThat(vacationType.getLabel(JAPANESE)).isEqualTo("vacation type label");
                    assertThat(vacationType.isRequiresApprovalToApply()).isTrue();
                    assertThat(vacationType.isVisibleToEveryone()).isTrue();
                    assertThat(vacationType.getCategory()).isEqualTo(OVERTIME);
                });
                assertThat(application.getRemindDate()).isSameAs(remindDate);
                assertThat(application.isTeamInformed()).isTrue();
                assertThat(application.getHours()).isEqualTo(Duration.ofHours(2));
                assertThat(application.getUpcomingHolidayReplacementNotificationSend()).isSameAs(upcomingHolidayReplacementNotificationSend);
                assertThat(application.getUpcomingApplicationsReminderSend()).isSameAs(upcomingApplicationsReminderSend);
                assertThat(application.getHolidayReplacements()).containsExactly(holidayReplacement);
            });
        }
    }

    @Nested
    class Save {

        @Test
        void ensureSaveCallsCorrectDaoMethod() {

            final LocalDate applicationDate = LocalDate.of(2023, 10, 14);
            final LocalDate cancelDate = LocalDate.of(2023, 10, 15);
            final LocalDate editedDate = LocalDate.of(2023, 10, 16);
            final LocalDate startDate = LocalDate.of(2023, 10, 17);
            final LocalTime startTime = LocalTime.of(11, 0);
            final LocalDate endDate = LocalDate.of(2023, 10, 18);
            final LocalTime endTime = LocalTime.of(15, 30);
            final LocalDate remindDate = LocalDate.of(2023, 10, 19);
            final LocalDate upcomingApplicationReminderSend = LocalDate.of(2023, 10, 20);
            final LocalDate upcomingApplicationsReminderSend = LocalDate.of(2023, 10, 21);

            final Person applier = new Person();
            applier.setId(1L);

            final Person boss = new Person();
            boss.setId(2L);

            final Person canceller = new Person();
            canceller.setId(3L);

            final Person person = new Person();
            person.setId(4L);

            final Person holidayReplacementPerson = new Person();
            person.setId(5L);

            final VacationType<?> vacationType = ProvidedVacationType.builder(messageSource).id(1L).build();

            final HolidayReplacementEntity holidayReplacementEntity = new HolidayReplacementEntity();
            holidayReplacementEntity.setPerson(holidayReplacementPerson);

            final Application application = new Application();
            application.setAddress("address");
            application.setApplicationDate(applicationDate);
            application.setCancelDate(cancelDate);
            application.setEditedDate(editedDate);
            application.setApplier(applier);
            application.setBoss(boss);
            application.setCanceller(canceller);
            application.setTwoStageApproval(true);
            application.setEndDate(endDate);
            application.setStartTime(startTime);
            application.setEndTime(endTime);
            application.setDayLength(DayLength.FULL);
            application.setPerson(person);
            application.setReason("reason");
            application.setStartDate(startDate);
            application.setStatus(WAITING);
            application.setVacationType(vacationType);
            application.setRemindDate(remindDate);
            application.setTeamInformed(true);
            application.setHours(Duration.ofHours(5));
            application.setUpcomingHolidayReplacementNotificationSend(upcomingApplicationReminderSend);
            application.setUpcomingApplicationsReminderSend(upcomingApplicationsReminderSend);
            application.setHolidayReplacements(List.of(holidayReplacementEntity));

            final ApplicationEntity entity = new ApplicationEntity();
            entity.setId(1L);
            entity.setVacationType(new VacationTypeEntity());
            when(applicationRepository.save(any(ApplicationEntity.class))).thenReturn(entity);

            final Application savedApplication = sut.save(application);
            assertThat(savedApplication).isNotSameAs(application);
            assertThat(savedApplication.getId()).isEqualTo(1L);

            final ArgumentCaptor<ApplicationEntity> captor = ArgumentCaptor.forClass(ApplicationEntity.class);
            verify(applicationRepository).save(captor.capture());

            assertThat(captor.getValue()).satisfies(persistedEntity -> {
                assertThat(persistedEntity.getId()).isNull();
                assertThat(persistedEntity.getAddress()).isEqualTo("address");
                assertThat(persistedEntity.getApplicationDate()).isEqualTo(applicationDate);
                assertThat(persistedEntity.getCancelDate()).isEqualTo(cancelDate);
                assertThat(persistedEntity.getEditedDate()).isEqualTo(editedDate);
                assertThat(persistedEntity.getApplier()).isEqualTo(applier);
                assertThat(persistedEntity.getBoss()).isEqualTo(boss);
                assertThat(persistedEntity.getCanceller()).isEqualTo(canceller);
                assertThat(persistedEntity.isTwoStageApproval()).isTrue();
                assertThat(persistedEntity.getEndDate()).isEqualTo(endDate);
                assertThat(persistedEntity.getStartTime()).isEqualTo(startTime);
                assertThat(persistedEntity.getEndTime()).isEqualTo(endTime);
                assertThat(persistedEntity.getDayLength()).isEqualTo(DayLength.FULL);
                assertThat(persistedEntity.getPerson()).isEqualTo(person);
                assertThat(persistedEntity.getReason()).isEqualTo("reason");
                assertThat(persistedEntity.getStartDate()).isEqualTo(startDate);
                assertThat(persistedEntity.getStatus()).isEqualTo(WAITING);
                assertThat(persistedEntity.getVacationType()).isNotNull();
                assertThat(persistedEntity.getRemindDate()).isEqualTo(remindDate);
                assertThat(persistedEntity.isTeamInformed()).isTrue();
                assertThat(persistedEntity.getHours()).isEqualTo(Duration.ofHours(5));
                assertThat(persistedEntity.getUpcomingHolidayReplacementNotificationSend()).isEqualTo(upcomingApplicationReminderSend);
                assertThat(persistedEntity.getUpcomingApplicationsReminderSend()).isEqualTo(upcomingApplicationsReminderSend);
                assertThat(persistedEntity.getHolidayReplacements()).isEqualTo(List.of(holidayReplacementEntity));
            });
        }
    }

    @Nested
    class GetTotalOvertimeReductionOfPerson {

        @Test
        void ensureReturnsZeroIfPersonHasNoApplicationsForLeaveYet() {

            final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
            when(applicationRepository.calculateTotalOvertimeReductionOfPerson(person)).thenReturn(null);

            final Duration totalHours = sut.getTotalOvertimeReductionOfPerson(person);
            assertThat(totalHours).isEqualTo(ZERO);
            verify(applicationRepository).calculateTotalOvertimeReductionOfPerson(person);
        }

        @Test
        void ensureReturnsCorrectTotalOvertimeReductionForPerson() {

            final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
            when(applicationRepository.calculateTotalOvertimeReductionOfPerson(person)).thenReturn(BigDecimal.ONE);

            final Duration totalHours = sut.getTotalOvertimeReductionOfPerson(person);
            assertThat(totalHours).isEqualTo(Duration.ofHours(1));
            verify(applicationRepository).calculateTotalOvertimeReductionOfPerson(person);
        }
    }

    @Nested
    class GetForStatesSince {

        @Test
        void getForStates() {

            final Application application = new Application();
            application.setId(1L);

            final ApplicationEntity applicationEntity = new ApplicationEntity();
            applicationEntity.setId(1L);
            applicationEntity.setVacationType(new VacationTypeEntity());

            when(applicationRepository.findByStatusInAndEndDateGreaterThanEqual(List.of(WAITING), LocalDate.of(2020, 10, 3)))
                .thenReturn(List.of(applicationEntity));

            final List<Application> result = sut.getForStatesSince(List.of(WAITING), LocalDate.of(2020, 10, 3));
            assertThat(result).isEqualTo(List.of(application));
        }
    }

    @Nested
    class GetForStatesAndPerson {

        @Test
        void getForStatesAndPerson() {

            final Application application = new Application();
            application.setId(1L);

            final ApplicationEntity applicationEntity = new ApplicationEntity();
            applicationEntity.setId(1L);
            applicationEntity.setVacationType(new VacationTypeEntity());

            final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

            when(applicationRepository.findByStatusInAndPersonIn(List.of(WAITING), List.of(person)))
                .thenReturn(List.of(applicationEntity));

            final List<Application> result = sut.getForStatesAndPerson(List.of(WAITING), List.of(person));
            assertThat(result).isEqualTo(List.of(application));
        }
    }

    @Nested
    class GetForHolidayReplacement {

        @Test
        void getForHolidayReplacement() {

            final Person holidayReplacement = new Person();
            final LocalDate localDate = LocalDate.of(2020, 10, 1);

            final Application application = new Application();
            application.setId(1L);

            final ApplicationEntity applicationEntity = new ApplicationEntity();
            applicationEntity.setId(1L);
            applicationEntity.setVacationType(new VacationTypeEntity());

            when(applicationRepository.findByHolidayReplacements_PersonAndEndDateIsGreaterThanEqualAndStatusIn(holidayReplacement, localDate, activeStatuses()))
                .thenReturn(List.of(applicationEntity));

            final List<Application> holidayReplacementApplications = sut.getForHolidayReplacement(holidayReplacement, localDate);
            assertThat(holidayReplacementApplications).hasSize(1).contains(application);
        }
    }

    @Nested
    class GetApplicationsWhereApplicantShouldBeNotifiedAboutUpcomingApplication {

        @Test
        void getApplicationsWithStartDateAndState() {
            final LocalDate from = LocalDate.of(2020, 10, 1);
            final LocalDate to = LocalDate.of(2020, 10, 3);

            final Application application = new Application();
            application.setId(1L);

            final ApplicationEntity applicationEntity = new ApplicationEntity();
            applicationEntity.setId(1L);
            applicationEntity.setVacationType(new VacationTypeEntity());

            final List<ApplicationStatus> statuses = List.of(TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED);
            when(applicationRepository.findByStatusInAndStartDateBetweenAndUpcomingApplicationsReminderSendIsNull(statuses, from, to))
                .thenReturn(List.of(applicationEntity));

            final List<Application> holidayReplacementApplications = sut.getApplicationsWhereApplicantShouldBeNotifiedAboutUpcomingApplication(from, to, statuses);
            assertThat(holidayReplacementApplications).hasSize(1).contains(application);
        }
    }

    @Nested
    class GetApplicationsWhereHolidayReplacementShouldBeNotified {

        @Test
        void getApplicationsWhereHolidayReplacementShouldBeNotified() {
            final LocalDate from = LocalDate.of(2020, 10, 1);
            final LocalDate to = LocalDate.of(2020, 10, 3);

            final Application application = new Application();
            application.setId(1L);

            final ApplicationEntity applicationEntity = new ApplicationEntity();
            applicationEntity.setId(1L);
            applicationEntity.setVacationType(new VacationTypeEntity());

            final List<ApplicationStatus> statuses = List.of(TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED);
            when(applicationRepository.findByStatusInAndStartDateBetweenAndHolidayReplacementsIsNotEmptyAndUpcomingHolidayReplacementNotificationSendIsNull(statuses, from, to))
                .thenReturn(List.of(applicationEntity));

            final List<Application> holidayReplacementApplications = sut.getApplicationsWhereHolidayReplacementShouldBeNotified(from, to, statuses);
            assertThat(holidayReplacementApplications).hasSize(1).contains(application);
        }
    }

    @Nested
    class GetApplicationsForACertainPeriodAndPersonAndVacationCategory {

        @Test
        void getApplicationsForACertainPeriodAndPersonAndVacationCategory() {
            final Person person = new Person();
            final LocalDate from = LocalDate.of(2020, 10, 1);
            final LocalDate to = LocalDate.of(2020, 10, 3);

            final Application application = new Application();
            application.setId(1L);

            final ApplicationEntity applicationEntity = new ApplicationEntity();
            applicationEntity.setId(1L);
            applicationEntity.setVacationType(new VacationTypeEntity());

            final List<ApplicationStatus> statuses = List.of(TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED);
            when(applicationRepository.findByStatusInAndPersonAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqualAndVacationTypeCategory(statuses, person, from, to, HOLIDAY))
                .thenReturn(List.of(applicationEntity));

            final List<Application> holidayReplacementApplications = sut.getApplicationsForACertainPeriodAndPersonAndVacationCategory(from, to, person, statuses, HOLIDAY);
            assertThat(holidayReplacementApplications).hasSize(1).contains(application);
        }
    }

    @Nested
    class GetApplicationsForACertainPeriodAndStatus {

        @Test
        void ensureGetApplicationsForACertainPeriod() {

            final Person person = new Person();
            person.setId(1L);

            final LocalDate startDate = LocalDate.of(2022, 8, 18);
            final LocalDate endDate = LocalDate.of(2022, 8, 18);
            final List<Person> persons = List.of(person);

            final Application application = new Application();
            application.setId(1L);

            final ApplicationEntity applicationEntity = new ApplicationEntity();
            applicationEntity.setId(1L);
            applicationEntity.setVacationType(new VacationTypeEntity());

            when(applicationRepository.findByPersonInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqualAndStatusIn(persons, startDate, endDate, List.of(WAITING)))
                .thenReturn(List.of(applicationEntity));

            final List<Application> actual = sut.getApplicationsForACertainPeriodAndStatus(startDate, endDate, persons, List.of(WAITING));

            assertThat(actual).containsExactly(application);
        }

        @Test
        void ensureGetApplicationsForACertainPeriodWithNullPersonQuery() {
            final LocalDate startDate = LocalDate.parse("2025-01-01");
            final LocalDate endDate = LocalDate.parse("2025-01-31");
            final List<ApplicationStatus> statuses = List.of(WAITING, ALLOWED);

            final VacationType<?> vacationType1 = ProvidedVacationType.builder(messageSource).id(1L).build();
            final VacationType<?> vacationType2 = ProvidedVacationType.builder(messageSource).id(2L).build();
            final List<VacationType<?>> vacationTypes = List.of(vacationType1, vacationType2);

            final ApplicationEntity applicationEntity = new ApplicationEntity();
            applicationEntity.setId(1L);
            applicationEntity.setVacationType(new VacationTypeEntity());

            when(applicationRepository.findByEndDateIsGreaterThanEqualAndStartDateIsLessThanEqualAndStatusInAndPersonNiceNameContainingIgnoreCase(
                startDate, endDate, statuses, List.of(1L, 2L), ""))
                .thenReturn(List.of(applicationEntity));

            final List<Application> actual = sut.getApplicationsForACertainPeriodAndStatus(startDate, endDate, statuses, vacationTypes, null);

            final Application expectedApplication = new Application();
            expectedApplication.setId(1L);
            assertThat(actual).containsExactly(expectedApplication);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "      "})
        void ensureGetApplicationsForACertainPeriodWithEmptyPersonQuery(String givenQuery) {
            final LocalDate startDate = LocalDate.parse("2025-01-01");
            final LocalDate endDate = LocalDate.parse("2025-01-31");
            final List<ApplicationStatus> statuses = List.of(WAITING, ALLOWED);

            final VacationType<?> vacationType = ProvidedVacationType.builder(messageSource).id(1L).build();
            final List<VacationType<?>> vacationTypes = List.of(vacationType);

            final ApplicationEntity applicationEntity = new ApplicationEntity();
            applicationEntity.setId(1L);
            applicationEntity.setVacationType(new VacationTypeEntity());

            when(applicationRepository.findByEndDateIsGreaterThanEqualAndStartDateIsLessThanEqualAndStatusInAndPersonNiceNameContainingIgnoreCase(
                startDate, endDate, statuses, List.of(1L), ""))
                .thenReturn(List.of(applicationEntity));

            final List<Application> actual = sut.getApplicationsForACertainPeriodAndStatus(startDate, endDate, statuses, vacationTypes, givenQuery);

            final Application expectedApplication = new Application();
            expectedApplication.setId(1L);
            assertThat(actual).containsExactly(expectedApplication);
        }

        @Test
        void ensureGetApplicationsForACertainPeriodWithValidPersonQuery() {
            final LocalDate startDate = LocalDate.parse("2025-01-01");
            final LocalDate endDate = LocalDate.parse("2025-01-31");
            final List<ApplicationStatus> statuses = List.of(WAITING, ALLOWED);

            final VacationType<?> vacationType = ProvidedVacationType.builder(messageSource).id(1L).build();
            final List<VacationType<?>> vacationTypes = List.of(vacationType);

            final ApplicationEntity applicationEntity = new ApplicationEntity();
            applicationEntity.setId(1L);
            applicationEntity.setVacationType(new VacationTypeEntity());

            when(applicationRepository.findByEndDateIsGreaterThanEqualAndStartDateIsLessThanEqualAndStatusInAndPersonNiceNameContainingIgnoreCase(
                startDate, endDate, statuses, List.of(1L), "John"))
                .thenReturn(List.of(applicationEntity));

            final List<Application> actual = sut.getApplicationsForACertainPeriodAndStatus(startDate, endDate, statuses, vacationTypes, "John");

            final Application expectedApplication = new Application();
            expectedApplication.setId(1L);
            assertThat(actual).containsExactly(expectedApplication);
        }

        @Test
        void ensureGetApplicationsForACertainPeriodWithMultipleVacationTypes() {
            final LocalDate startDate = LocalDate.parse("2025-01-01");
            final LocalDate endDate = LocalDate.parse("2025-01-31");
            final List<ApplicationStatus> statuses = List.of(WAITING, ALLOWED);

            final VacationType<?> vacationType1 = ProvidedVacationType.builder(messageSource).id(5L).build();
            final VacationType<?> vacationType2 = ProvidedVacationType.builder(messageSource).id(10L).build();
            final VacationType<?> vacationType3 = ProvidedVacationType.builder(messageSource).id(15L).build();
            final List<VacationType<?>> vacationTypes = List.of(vacationType1, vacationType2, vacationType3);

            final ApplicationEntity applicationEntity = new ApplicationEntity();
            applicationEntity.setId(1L);
            applicationEntity.setVacationType(new VacationTypeEntity());

            when(applicationRepository.findByEndDateIsGreaterThanEqualAndStartDateIsLessThanEqualAndStatusInAndPersonNiceNameContainingIgnoreCase(
                startDate, endDate, statuses, List.of(5L, 10L, 15L), "Smith"))
                .thenReturn(List.of(applicationEntity));

            final List<Application> actual = sut.getApplicationsForACertainPeriodAndStatus(startDate, endDate, statuses, vacationTypes, "Smith");

            final Application expectedApplication = new Application();
            expectedApplication.setId(1L);
            assertThat(actual).containsExactly(expectedApplication);
        }

        @Test
        void ensureGetApplicationsForACertainPeriodWithEmptyVacationTypesList() {
            final LocalDate startDate = LocalDate.parse("2025-01-01");
            final LocalDate endDate = LocalDate.parse("2025-01-31");
            final List<ApplicationStatus> statuses = List.of(WAITING, ALLOWED);
            final List<VacationType<?>> vacationTypes = List.of();

            when(applicationRepository.findByEndDateIsGreaterThanEqualAndStartDateIsLessThanEqualAndStatusInAndPersonNiceNameContainingIgnoreCase(
                startDate, endDate, statuses, List.of(), ""))
                .thenReturn(List.of());

            final List<Application> actual = sut.getApplicationsForACertainPeriodAndStatus(startDate, endDate, statuses, vacationTypes, null);
            assertThat(actual).isEmpty();
        }

        @Test
        void ensureGetApplicationsForACertainPeriodReturnsCorrectlyMappedApplications() {
            when(messageSource.getMessage("vacation-type-message-key", new Object[]{}, JAPANESE)).thenReturn("vacation type label");

            final LocalDate startDate = LocalDate.parse("2025-01-01");
            final LocalDate endDate = LocalDate.parse("2025-01-31");
            final List<ApplicationStatus> statuses = List.of(WAITING);

            final VacationType<?> vacationType = ProvidedVacationType.builder(messageSource).id(1L).build();
            final List<VacationType<?>> vacationTypes = List.of(vacationType);

            final Person person = new Person();
            person.setId(1L);

            final VacationTypeEntity vacationTypeEntity = new VacationTypeEntity();
            vacationTypeEntity.setId(2L);
            vacationTypeEntity.setActive(true);
            vacationTypeEntity.setColor(CYAN);
            vacationTypeEntity.setMessageKey("vacation-type-message-key");
            vacationTypeEntity.setRequiresApprovalToApply(true);
            vacationTypeEntity.setVisibleToEveryone(true);
            vacationTypeEntity.setCategory(OVERTIME);

            final ApplicationEntity applicationEntity = new ApplicationEntity();
            applicationEntity.setId(42L);
            applicationEntity.setPerson(person);
            applicationEntity.setStartDate(startDate);
            applicationEntity.setEndDate(endDate);
            applicationEntity.setStatus(WAITING);
            applicationEntity.setVacationType(vacationTypeEntity);
            applicationEntity.setDayLength(DayLength.FULL);

            when(applicationRepository.findByEndDateIsGreaterThanEqualAndStartDateIsLessThanEqualAndStatusInAndPersonNiceNameContainingIgnoreCase(
                startDate, endDate, statuses, List.of(1L), "test"))
                .thenReturn(List.of(applicationEntity));

            final List<Application> actual = sut.getApplicationsForACertainPeriodAndStatus(startDate, endDate, statuses, vacationTypes, "test");

            assertThat(actual).hasSize(1);
            assertThat(actual.get(0)).satisfies(application -> {
                assertThat(application.getId()).isEqualTo(42L);
                assertThat(application.getPerson()).isSameAs(person);
                assertThat(application.getStartDate()).isEqualTo(startDate);
                assertThat(application.getEndDate()).isEqualTo(endDate);
                assertThat(application.getStatus()).isEqualTo(WAITING);
                assertThat(application.getDayLength()).isEqualTo(DayLength.FULL);
                assertThat(application.getVacationType()).satisfies(vt -> {
                    assertThat(vt.getId()).isEqualTo(2L);
                    assertThat(vt.isActive()).isTrue();
                    assertThat(vt.getColor()).isEqualTo(CYAN);
                    assertThat(vt.getLabel(JAPANESE)).isEqualTo("vacation type label");
                    assertThat(vt.isRequiresApprovalToApply()).isTrue();
                    assertThat(vt.isVisibleToEveryone()).isTrue();
                    assertThat(vt.getCategory()).isEqualTo(OVERTIME);
                });
            });
        }
    }

    @Nested
    class DeleteApplicationsByPerson {

        @Test
        void deleteOnPersonDeletionEventReturnsDeletedApplication() {

            final Person person = new Person();

            final Application application = new Application();
            application.setId(1L);

            final ApplicationEntity applicationEntity = new ApplicationEntity();
            applicationEntity.setId(1L);
            applicationEntity.setVacationType(new VacationTypeEntity());

            when(applicationRepository.deleteByPerson(person)).thenReturn(List.of(applicationEntity));

            final List<Application> applications = sut.deleteApplicationsByPerson(person);
            assertThat(applications).containsExactly(application);

            verify(applicationRepository).deleteByPerson(person);
        }
    }

    @Nested
    class DeleteInteractionWithApplications {

        @Test
        void deleteBossInteractionOnPersonDeletionEvent() {

            final Person boss = new Person();
            boss.setId(1L);

            final Application application = new Application();
            application.setId(1L);
            application.setCanceller(boss);

            final ApplicationEntity applicationEntity = new ApplicationEntity();
            applicationEntity.setId(1L);
            applicationEntity.setCanceller(boss);

            when(applicationRepository.findByBoss(boss)).thenReturn(List.of(applicationEntity));

            sut.deleteInteractionWithApplications(boss);

            verify(applicationRepository, atLeastOnce()).saveAll(List.of(applicationEntity));
        }

        @Test
        void deleteCancellerInteractionOnPersonDeletionEvent() {

            final Person canceller = new Person();
            canceller.setId(1L);

            final Application application = new Application();
            application.setId(1L);
            application.setCanceller(canceller);

            final ApplicationEntity applicationEntity = new ApplicationEntity();
            applicationEntity.setId(1L);
            applicationEntity.setCanceller(canceller);

            when(applicationRepository.findByCanceller(canceller)).thenReturn(List.of(applicationEntity));

            sut.deleteInteractionWithApplications(canceller);

            verify(applicationRepository, atLeastOnce()).saveAll(List.of(applicationEntity));
        }

        @Test
        void deleteApplierInteractionOnPersonDeletionEvent() {

            final Person applier = new Person();
            applier.setId(1L);

            final Application application = new Application();
            application.setId(1L);
            application.setApplier(applier);

            final ApplicationEntity applicationEntity = new ApplicationEntity();
            applicationEntity.setId(1L);
            applicationEntity.setApplier(applier);

            when(applicationRepository.findByApplier(applier)).thenReturn(List.of(applicationEntity));

            sut.deleteInteractionWithApplications(applier);

            verify(applicationRepository, atLeastOnce()).saveAll(List.of(applicationEntity));
        }
    }

    @Nested
    class DeleteHolidayReplacements {

        @Test
        void deleteApplicationReplacement() {

            final Person person = new Person();
            person.setId(42L);

            final Person other = new Person();
            other.setId(21L);

            final HolidayReplacementEntity holidayReplacement = new HolidayReplacementEntity();
            holidayReplacement.setPerson(person);

            final HolidayReplacementEntity otherHolidayReplacement = new HolidayReplacementEntity();
            otherHolidayReplacement.setPerson(other);

            final Application application = new Application();
            application.setId(1L);
            application.setHolidayReplacements(List.of(holidayReplacement, otherHolidayReplacement));

            final ApplicationEntity applicationEntity = new ApplicationEntity();
            applicationEntity.setId(1L);
            applicationEntity.setHolidayReplacements(List.of(holidayReplacement, otherHolidayReplacement));

            when(applicationRepository.findAllByHolidayReplacements_Person(person)).thenReturn(List.of(applicationEntity));

            sut.deleteHolidayReplacements(new PersonDeletedEvent(person));

            verify(applicationRepository).saveAll(List.of(applicationEntity));
        }
    }

    @Nested
    class GetTotalOvertimeReductionOfPersonUntil {

        @Test
        void ensureToGetAllPersonsWithZeroDurationIfNoApplicationWasFound() {

            final Person batman = new Person();
            batman.setId(1L);
            final Person robin = new Person();
            robin.setId(2L);
            final Person alfred = new Person();
            alfred.setId(3L);

            final Set<Person> persons = Set.of(batman, robin, alfred);
            final LocalDate until = LocalDate.of(2022, 8, 30);

            when(applicationRepository.findByPersonInAndVacationTypeCategoryAndStatusInAndStartDateIsLessThanEqual(persons, OVERTIME, activeStatuses(), until))
                .thenReturn(List.of());

            assertThat(sut.getTotalOvertimeReductionOfPersonUntil(persons, until))
                .containsEntry(batman, ZERO)
                .containsEntry(robin, ZERO)
                .containsEntry(alfred, ZERO);
        }

        @Test
        void ensureToGetCompleteDurationUntilASpecificDate() {

            final Person batman = new Person();
            batman.setId(1L);

            final VacationTypeEntity vacationTypeOvertime = new VacationTypeEntity();
            vacationTypeOvertime.setCategory(OVERTIME);

            final LocalDate startDate = LocalDate.of(2022, 8, 10);
            final LocalDate endDate = LocalDate.of(2022, 8, 12);

            final ApplicationEntity applicationEntity = new ApplicationEntity();
            applicationEntity.setPerson(batman);
            applicationEntity.setStartDate(startDate);
            applicationEntity.setEndDate(endDate);
            applicationEntity.setDayLength(DayLength.FULL);
            applicationEntity.setStatus(WAITING);
            applicationEntity.setVacationType(vacationTypeOvertime);
            applicationEntity.setHours(Duration.ofHours(10));

            final Set<Person> persons = Set.of(batman);
            final LocalDate until = LocalDate.of(2022, 8, 30);

            when(applicationRepository.findByPersonInAndVacationTypeCategoryAndStatusInAndStartDateIsLessThanEqual(
                persons,
                OVERTIME,
                activeStatuses(),
                until
            )).thenReturn(List.of(applicationEntity));

            when(workingTimeCalendarService.getWorkingTimesByPersons(persons, new DateRange(startDate, endDate)))
                .thenReturn(Map.of(batman, workingTimeCalendar(startDate, endDate, (date) -> fullWorkday())));

            assertThat(sut.getTotalOvertimeReductionOfPersonUntil(persons, until))
                .containsEntry(batman, Duration.ofHours(10));
        }

        @Test
        void ensureToGetPartialDurationUntilASpecificDate() {

            final Person batman = new Person();
            batman.setId(1L);

            final VacationTypeEntity vacationTypeOvertime = new VacationTypeEntity();
            vacationTypeOvertime.setCategory(OVERTIME);

            final LocalDate startDate = LocalDate.of(2022, 8, 10);
            final LocalDate endDate = LocalDate.of(2022, 8, 19);

            final ApplicationEntity applicationEntity = new ApplicationEntity();
            applicationEntity.setPerson(batman);
            applicationEntity.setStartDate(startDate);
            applicationEntity.setEndDate(endDate);
            applicationEntity.setDayLength(DayLength.FULL);
            applicationEntity.setStatus(WAITING);
            applicationEntity.setVacationType(vacationTypeOvertime);
            applicationEntity.setHours(Duration.ofHours(10));

            final Set<Person> persons = Set.of(batman);
            final LocalDate until = LocalDate.of(2022, 8, 14);

            when(applicationRepository.findByPersonInAndVacationTypeCategoryAndStatusInAndStartDateIsLessThanEqual(
                persons,
                OVERTIME,
                activeStatuses(),
                until
            )).thenReturn(List.of(applicationEntity));

            when(workingTimeCalendarService.getWorkingTimesByPersons(persons, new DateRange(startDate, endDate)))
                .thenReturn(Map.of(batman, workingTimeCalendar(startDate, endDate, (date) -> fullWorkday())));

            assertThat(sut.getTotalOvertimeReductionOfPersonUntil(persons, until))
                .containsEntry(batman, Duration.ofHours(5));
        }
    }
}
