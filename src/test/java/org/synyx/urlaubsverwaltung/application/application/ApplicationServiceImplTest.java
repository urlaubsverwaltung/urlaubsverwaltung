package org.synyx.urlaubsverwaltung.application.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeEntity;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
    void deleteOnPersonDeletionEvent() {
        final Person person = new Person();
        final List<Application> deletedApplications = List.of(new Application());
        when(applicationRepository.deleteByPerson(person)).thenReturn(deletedApplications);

        final List<Application> actualListOfApplications = sut.deleteApplicationsByPerson(person);

        assertThat(actualListOfApplications).containsExactlyElementsOf(deletedApplications);
        verify(applicationRepository).deleteByPerson(person);
    }

    @Test
    void deleteBossInteractionOnPersonDeletionEvent() {
        final Person boss = new Person();
        boss.setId(1);
        final Application application = new Application();
        application.setId(1);
        application.setCanceller(boss);
        when(applicationRepository.findByBoss(boss)).thenReturn(List.of(application));

        sut.deleteInteractionWithApplications(boss);

        final ArgumentCaptor<Application> argument = ArgumentCaptor.forClass(Application.class);
        verify(applicationRepository).save(argument.capture());

        assertThat(argument.getValue().getBoss()).isNull();
    }

    @Test
    void deleteCancellerInteractionOnPersonDeletionEvent() {
        final Person canceller = new Person();
        canceller.setId(1);
        final Application application = new Application();
        application.setId(1);
        application.setCanceller(canceller);
        when(applicationRepository.findByCanceller(canceller)).thenReturn(List.of(application));

        sut.deleteInteractionWithApplications(canceller);

        final ArgumentCaptor<Application> argument = ArgumentCaptor.forClass(Application.class);
        verify(applicationRepository).save(argument.capture());

        assertThat(argument.getValue().getCanceller()).isNull();
    }
}
