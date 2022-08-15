package org.synyx.urlaubsverwaltung.application.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeEntity;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonDeletedEvent;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OVERTIME;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceImplTest {

    private ApplicationServiceImpl sut;

    @Mock
    private ApplicationRepository applicationRepository;

    @BeforeEach
    void setUp() {
        sut = new ApplicationServiceImpl(applicationRepository);
    }

    // Get application by ID -------------------------------------------------------------------------------------------
    @Test
    void ensureGetApplicationByIdCallsCorrectDaoMethod() {
        sut.getApplicationById(1234);
        verify(applicationRepository).findById(1234);
    }

    @Test
    void ensureGetApplicationByIdReturnsAbsentOptionalIfNoOneExists() {
        final Optional<Application> optional = sut.getApplicationById(1234);
        assertThat(optional).isEmpty();
    }

    // Save application ------------------------------------------------------------------------------------------------
    @Test
    void ensureSaveCallsCorrectDaoMethod() {

        final Application application = new Application();
        sut.save(application);
        verify(applicationRepository).save(application);
    }

    // Get total overtime reduction ------------------------------------------------------------------------------------
    @Test
    void ensureReturnsZeroIfPersonHasNoApplicationsForLeaveYet() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        when(applicationRepository.calculateTotalOvertimeReductionOfPerson(person)).thenReturn(null);

        final Duration totalHours = sut.getTotalOvertimeReductionOfPerson(person);

        verify(applicationRepository).calculateTotalOvertimeReductionOfPerson(person);

        assertThat(totalHours).isEqualTo(Duration.ZERO);
    }

    @Test
    void ensureReturnsZeroIfPersonHasNoApplicationsForLeaveBeforeDate() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate date = LocalDate.of(2022, 1, 1);
        when(applicationRepository.calculateTotalOvertimeReductionOfPersonBefore(person, date)).thenReturn(null);

        final Duration totalHours = sut.getTotalOvertimeReductionOfPersonBefore(person, date);

        verify(applicationRepository).calculateTotalOvertimeReductionOfPersonBefore(person, date);

        assertThat(totalHours).isEqualTo(Duration.ZERO);
    }

    @Test
    void getForStates() {

        final Application application = new Application();
        final List<Application> applications = List.of(application);

        when(applicationRepository.findByStatusInAndEndDateGreaterThanEqual(List.of(WAITING), LocalDate.of(2020, 10, 3))).thenReturn(applications);

        final List<Application> result = sut.getForStatesSince(List.of(WAITING), LocalDate.of(2020, 10, 3));
        assertThat(result).isEqualTo(applications);
    }

    @Test
    void getForStatesAndPerson() {

        final Application application = new Application();
        final List<Application> applications = List.of(application);

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        when(applicationRepository.findByStatusInAndPersonIn(List.of(WAITING), List.of(person))).thenReturn(applications);

        final List<Application> result = sut.getForStatesAndPerson(List.of(WAITING), List.of(person));
        assertThat(result).isEqualTo(applications);
    }


    @Test
    void ensureReturnsCorrectTotalOvertimeReductionForPerson() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        when(applicationRepository.calculateTotalOvertimeReductionOfPerson(person)).thenReturn(BigDecimal.ONE);

        final Duration totalHours = sut.getTotalOvertimeReductionOfPerson(person);

        verify(applicationRepository).calculateTotalOvertimeReductionOfPerson(person);

        assertThat(totalHours).isEqualTo(Duration.ofHours(1));
    }

    @Test
    void ensureTotalOvertimeReductionOfPersonIsZeroIfNoApplicationIsFound() {

        final List<ApplicationStatus> waitingAndAllowedStatus = List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED);
        final Person person = new Person();
        final LocalDate start = LocalDate.of(2022, 10, 10);
        final LocalDate end = LocalDate.of(2022, 10, 20);
        when(applicationRepository.findByPersonAndVacationTypeCategoryAndStatusInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(person, OVERTIME, waitingAndAllowedStatus, start, end)).thenReturn(List.of());

        final Duration totalOvertimeReduction = sut.getTotalOvertimeReductionOfPerson(person, start, end);
        assertThat(totalOvertimeReduction).isZero();
    }

    @Test
    void ensureTotalOvertimeReductionOfPersonWithApplicationInRange() {

        final List<ApplicationStatus> waitingAndAllowedStatus = List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED);

        final Person person = new Person();
        final LocalDate start = LocalDate.of(2022, 10, 10);
        final LocalDate end = LocalDate.of(2022, 10, 20);

        final VacationTypeEntity vacationTypeEntity = new VacationTypeEntity();
        vacationTypeEntity.setCategory(HOLIDAY);

        final Application application = new Application();
        application.setStartDate(LocalDate.of(2022, 10, 10));
        application.setEndDate(LocalDate.of(2022, 10, 12));
        application.setStatus(WAITING);
        application.setVacationType(vacationTypeEntity);
        application.setHours(Duration.ofHours(10));
        when(applicationRepository.findByPersonAndVacationTypeCategoryAndStatusInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(person, OVERTIME, waitingAndAllowedStatus, start, end)).thenReturn(List.of(application));

        final Duration totalOvertimeReduction = sut.getTotalOvertimeReductionOfPerson(person, start, end);
        assertThat(totalOvertimeReduction).isEqualTo(Duration.ofHours(10));
    }

    @Test
    void ensureTotalOvertimeReductionOfPersonWithApplicationStartOfRange() {

        final List<ApplicationStatus> waitingAndAllowedStatus = List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED);

        final Person person = new Person();
        final LocalDate start = LocalDate.of(2022, 10, 10);
        final LocalDate end = LocalDate.of(2022, 10, 20);

        final VacationTypeEntity vacationTypeEntity = new VacationTypeEntity();
        vacationTypeEntity.setCategory(HOLIDAY);

        final Application application = new Application();
        application.setStartDate(LocalDate.of(2022, 10, 8));
        application.setEndDate(LocalDate.of(2022, 10, 12));
        application.setStatus(WAITING);
        application.setVacationType(vacationTypeEntity);
        application.setHours(Duration.ofHours(12));
        when(applicationRepository.findByPersonAndVacationTypeCategoryAndStatusInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(person, OVERTIME, waitingAndAllowedStatus, start, end)).thenReturn(List.of(application));

        final Duration totalOvertimeReduction = sut.getTotalOvertimeReductionOfPerson(person, start, end);
        assertThat(totalOvertimeReduction).isEqualTo(Duration.parse("PT7H12M"));
    }

    @Test
    void ensureTotalOvertimeReductionOfPersonWithApplicationEndOfRange() {

        final List<ApplicationStatus> waitingAndAllowedStatus = List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED);

        final Person person = new Person();
        final LocalDate start = LocalDate.of(2022, 10, 10);
        final LocalDate end = LocalDate.of(2022, 10, 20);

        final VacationTypeEntity vacationTypeEntity = new VacationTypeEntity();
        vacationTypeEntity.setCategory(HOLIDAY);

        final Application application = new Application();
        application.setStartDate(LocalDate.of(2022, 10, 20));
        application.setEndDate(LocalDate.of(2022, 10, 22));
        application.setStatus(WAITING);
        application.setVacationType(vacationTypeEntity);
        application.setHours(Duration.ofHours(12));
        when(applicationRepository.findByPersonAndVacationTypeCategoryAndStatusInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(person, OVERTIME, waitingAndAllowedStatus, start, end)).thenReturn(List.of(application));

        final Duration totalOvertimeReduction = sut.getTotalOvertimeReductionOfPerson(person, start, end);
        assertThat(totalOvertimeReduction).isEqualTo(Duration.parse("PT4H"));
    }

    @Test
    void ensureReturnsCorrectTotalOvertimeReductionForPersonBeforeDate() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate date = LocalDate.of(2022, 1, 1);

        when(applicationRepository.calculateTotalOvertimeReductionOfPersonBefore(person, date)).thenReturn(BigDecimal.ONE);

        final Duration totalHours = sut.getTotalOvertimeReductionOfPersonBefore(person, date);

        verify(applicationRepository).calculateTotalOvertimeReductionOfPersonBefore(person, date);

        assertThat(totalHours).isEqualTo(Duration.ofHours(1));
    }

    @Test
    void getForHolidayReplacement() {

        final Person holidayReplacement = new Person();
        final LocalDate localDate = LocalDate.of(2020, 10, 1);

        final Application application = new Application();
        final List<ApplicationStatus> statuses = List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED);

        when(applicationRepository.findByHolidayReplacements_PersonAndEndDateIsGreaterThanEqualAndStatusIn(holidayReplacement, localDate, statuses))
            .thenReturn(List.of(application));

        final List<Application> holidayReplacementApplications = sut.getForHolidayReplacement(holidayReplacement, localDate);
        assertThat(holidayReplacementApplications).hasSize(1).contains(application);
    }

    @Test
    void getApplicationsWithStartDateAndState() {
        final LocalDate from = LocalDate.of(2020, 10, 1);
        final LocalDate to = LocalDate.of(2020, 10, 3);

        final Application application = new Application();
        final List<ApplicationStatus> statuses = List.of(TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED);
        when(applicationRepository.findByStatusInAndStartDateBetweenAndUpcomingApplicationsReminderSendIsNull(statuses, from, to)).thenReturn(List.of(application));

        final List<Application> holidayReplacementApplications = sut.getApplicationsWhereApplicantShouldBeNotifiedAboutUpcomingApplication(from, to, statuses);
        assertThat(holidayReplacementApplications).hasSize(1).contains(application);
    }

    @Test
    void getApplicationsWhereHolidayReplacementShouldBeNotified() {
        final LocalDate from = LocalDate.of(2020, 10, 1);
        final LocalDate to = LocalDate.of(2020, 10, 3);

        final Application application = new Application();
        final List<ApplicationStatus> statuses = List.of(TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED);
        when(applicationRepository.findByStatusInAndStartDateBetweenAndHolidayReplacementsIsNotEmptyAndUpcomingHolidayReplacementNotificationSendIsNull(statuses, from, to)).thenReturn(List.of(application));

        final List<Application> holidayReplacementApplications = sut.getApplicationsWhereHolidayReplacementShouldBeNotified(from, to, statuses);
        assertThat(holidayReplacementApplications).hasSize(1).contains(application);
    }

    @Test
    void getApplicationsStartingInACertainPeriodAndPersonAndVacationCategory() {
        final Person person = new Person();
        final LocalDate from = LocalDate.of(2020, 10, 1);
        final LocalDate to = LocalDate.of(2020, 10, 3);

        final Application application = new Application();
        final List<ApplicationStatus> statuses = List.of(TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED);
        when(applicationRepository.findByStatusInAndPersonAndStartDateBetweenAndVacationTypeCategory(statuses, person, from, to, HOLIDAY)).thenReturn(List.of(application));

        final List<Application> holidayReplacementApplications = sut.getApplicationsStartingInACertainPeriodAndPersonAndVacationCategory(from, to, person, statuses, HOLIDAY);
        assertThat(holidayReplacementApplications).hasSize(1).contains(application);
    }

    @Test
    void getApplicationsForACertainPeriodAndPersonAndVacationCategory() {
        final Person person = new Person();
        final LocalDate from = LocalDate.of(2020, 10, 1);
        final LocalDate to = LocalDate.of(2020, 10, 3);

        final Application application = new Application();
        final List<ApplicationStatus> statuses = List.of(TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED);
        when(applicationRepository.findByStatusInAndPersonAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqualAndVacationTypeCategory(statuses, person, from, to, HOLIDAY)).thenReturn(List.of(application));

        final List<Application> holidayReplacementApplications = sut.getApplicationsForACertainPeriodAndPersonAndVacationCategory(from, to, person, statuses, HOLIDAY);
        assertThat(holidayReplacementApplications).hasSize(1).contains(application);
    }

    @Test
    void ensureGetApplicationsForACertainPeriod() {

        final Person person = new Person();
        person.setId(1);

        final LocalDate startDate = LocalDate.of(2022, 8, 18);
        final LocalDate endDate = LocalDate.of(2022, 8, 18);
        final List<Person> persons = List.of(person);

        final Application application = new Application();
        application.setId(1);

        when(applicationRepository.findByPersonInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(persons, startDate, endDate))
            .thenReturn(List.of(application));

        final List<Application> actual = sut.getApplicationsForACertainPeriod(startDate, endDate, persons);

        assertThat(actual).containsExactly(application);
    }

    @Test
    void deleteOnPersonDeletionEventReturnsDeletedApplication() {
        final Person person = new Person();
        final Application application = new Application();

        when(applicationRepository.deleteByPerson(person)).thenReturn(List.of(application));

        final List<Application> applications = sut.deleteApplicationsByPerson(person);
        assertThat(applications).containsExactly(application);

        verify(applicationRepository).deleteByPerson(person);
    }

    @Test
    void deleteBossInteractionOnPersonDeletionEvent() {
        final Person boss = new Person();
        boss.setId(1);
        final Application application = new Application();
        application.setId(1);
        application.setCanceller(boss);
        final List<Application> applicationsOfBoss = List.of(application);
        when(applicationRepository.findByBoss(boss)).thenReturn(applicationsOfBoss);

        sut.deleteInteractionWithApplications(boss);

        verify(applicationRepository, atLeastOnce()).saveAll(applicationsOfBoss);
        assertThat(applicationsOfBoss).extracting("boss").containsOnlyNulls();
    }

    @Test
    void deleteCancellerInteractionOnPersonDeletionEvent() {
        final Person canceller = new Person();
        canceller.setId(1);
        final Application application = new Application();
        application.setId(1);
        application.setCanceller(canceller);
        final List<Application> applicationsOfCanceller = List.of(application);
        when(applicationRepository.findByCanceller(canceller)).thenReturn(applicationsOfCanceller);

        sut.deleteInteractionWithApplications(canceller);

        verify(applicationRepository, atLeastOnce()).saveAll(applicationsOfCanceller);
        assertThat(applicationsOfCanceller).extracting("canceller").containsOnlyNulls();
    }

    @Test
    void deleteApplierInteractionOnPersonDeletionEvent() {
        final Person applier = new Person();
        applier.setId(1);
        final Application application = new Application();
        application.setId(1);
        application.setApplier(applier);
        final List<Application> applicationsOfApplier = List.of(application);
        when(applicationRepository.findByApplier(applier)).thenReturn(applicationsOfApplier);

        sut.deleteInteractionWithApplications(applier);

        verify(applicationRepository, atLeastOnce()).saveAll(applicationsOfApplier);
        assertThat(applicationsOfApplier).extracting("applier").containsOnlyNulls();
    }

    @Test
    void deleteApplicationReplacement() {

        Application application = new Application();

        final Person person = new Person();
        person.setId(42);
        final HolidayReplacementEntity holidayReplacement = new HolidayReplacementEntity();
        holidayReplacement.setPerson(person);


        final HolidayReplacementEntity otherHolidayReplacement = new HolidayReplacementEntity();
        final Person other = new Person();
        other.setId(21);
        otherHolidayReplacement.setPerson(other);

        application.setHolidayReplacements(List.of(holidayReplacement, otherHolidayReplacement));
        final List<Application> applicationsOfHolidayReplacement = List.of(application);
        when(applicationRepository.findAllByHolidayReplacements_Person(person)).thenReturn(applicationsOfHolidayReplacement);

        sut.deleteHolidayReplacements(new PersonDeletedEvent(person));

        verify(applicationRepository).saveAll(applicationsOfHolidayReplacement);
        assertThat(applicationsOfHolidayReplacement.get(0).getHolidayReplacements()).containsExactly(otherHolidayReplacement);

    }
}
