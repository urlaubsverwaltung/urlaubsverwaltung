package org.synyx.urlaubsverwaltung.application.application;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.TestContainersBase;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeEntity;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeServiceImpl;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createApplication;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.CANCELLED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.REJECTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.REVOKED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.SPECIALLEAVE;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.UNPAIDLEAVE;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;


@SpringBootTest
@Transactional
class ApplicationRepositoryIT extends TestContainersBase {

    @Autowired
    private ApplicationRepository sut;

    @Autowired
    private PersonService personService;
    @Autowired
    private VacationTypeService vacationTypeService;

    @Test
    void ensureApplicationForLeaveForStatusAndPersonAndWithinDateRange() {

        final Person max = personService.save(new Person("muster", "Mustermann", "Max", "mustermann@example.org"));
        final Person marlene = personService.save(new Person("person2", "Musterfrau", "Marlene", "musterfrau@example.org"));
        final VacationTypeEntity vacationType = getVacationType(HOLIDAY);

        final LocalDate askedStartDate = LocalDate.now(UTC).with(firstDayOfMonth());
        final LocalDate askedEndDate = LocalDate.now(UTC).with(lastDayOfMonth());

        // application for leave that should not be found
        final Application appNotInPeriodBecausePast = createApplication(max, vacationType, askedStartDate.minusDays(12), askedStartDate.minusDays(10), FULL);
        final Application appNotInPeriodBecauseFuture = createApplication(max, vacationType, askedEndDate.plusDays(10), askedEndDate.plusDays(12), FULL);

        sut.save(appNotInPeriodBecausePast);
        sut.save(appNotInPeriodBecauseFuture);

        // application for leave that should be found
        final Application appStartingBeforePeriod = createApplication(max, vacationType, askedStartDate.minusDays(5), askedStartDate.plusDays(1), FULL);
        final Application appEndingAfterPeriod = createApplication(max, vacationType, askedEndDate.minusDays(1), askedEndDate.plusDays(1), FULL);
        final Application appInBetween = createApplication(max, vacationType, askedStartDate.plusDays(10), askedStartDate.plusDays(12), FULL);
        final Application appStartingAtPeriod = createApplication(marlene, vacationType, askedStartDate, askedStartDate.plusDays(2), FULL);
        final Application appEndingAtPeriod = createApplication(marlene, vacationType, askedEndDate.minusDays(5), askedEndDate, FULL);

        sut.save(appStartingBeforePeriod);
        sut.save(appEndingAfterPeriod);
        sut.save(appInBetween);
        sut.save(appStartingAtPeriod);
        sut.save(appEndingAtPeriod);

        List<ApplicationStatus> statuses = List.of(WAITING);
        List<Person> persons = List.of(max, marlene);

        final List<Application> actualApplications = sut.findByStatusInAndPersonInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(statuses, persons, askedStartDate, askedEndDate);

        assertThat(actualApplications).contains(appStartingBeforePeriod, appEndingAfterPeriod, appInBetween, appStartingAtPeriod, appEndingAtPeriod);
    }

    @Test
    void ensureReturnsNullAsTotalOvertimeReductionIfPersonHasNoApplicationsForLeaveYet() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person savedPerson = personService.save(person);

        BigDecimal totalHours = sut.calculateTotalOvertimeReductionOfPerson(savedPerson);
        assertThat(totalHours).isNull();
    }

    @Test
    void findByStatusIn() {

        final Person person = new Person("sam", "smith", "sam", "smith@example.org");
        final Person savedPerson = personService.save(person);

        final LocalDate now = LocalDate.now(UTC);

        // Allowed
        Application fullDayOvertimeReduction = createApplication(savedPerson, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        fullDayOvertimeReduction.setStatus(ALLOWED);
        sut.save(fullDayOvertimeReduction);

        Application fullDayHoliday = createApplication(savedPerson, getVacationType(HOLIDAY), now, now.plusDays(2), FULL);
        fullDayHoliday.setStatus(ALLOWED);
        sut.save(fullDayHoliday);

        // Waiting
        final Application halfDayOvertimeReduction = createApplication(savedPerson, getVacationType(SPECIALLEAVE), now.plusDays(5), now.plusDays(10), MORNING);
        halfDayOvertimeReduction.setStatus(WAITING);
        sut.save(halfDayOvertimeReduction);

        // Cancelled
        final Application cancelledOvertimeReduction = createApplication(savedPerson, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        cancelledOvertimeReduction.setStatus(CANCELLED);
        sut.save(cancelledOvertimeReduction);

        // Rejected
        final Application rejectedOvertimeReduction = createApplication(savedPerson, getVacationType(SPECIALLEAVE), now, now.plusDays(2), FULL);
        rejectedOvertimeReduction.setStatus(REJECTED);
        sut.save(rejectedOvertimeReduction);

        // Revoked
        final Application revokedOvertimeReduction = createApplication(savedPerson, getVacationType(UNPAIDLEAVE), now, now.plusDays(2), FULL);
        revokedOvertimeReduction.setStatus(REVOKED);
        sut.save(revokedOvertimeReduction);

        final List<Application> allowedApplications = sut.findByStatusInAndEndDateGreaterThanEqual(List.of(ALLOWED), LocalDate.of(2020, 10, 3));
        assertThat(allowedApplications)
            .contains(fullDayOvertimeReduction, fullDayHoliday)
            .hasSize(2);
    }

    @Test
    void findByStatusInMultipleStatus() {

        final Person person = new Person("sam", "smith", "sam", "smith@example.org");
        final Person savedPerson = personService.save(person);

        final LocalDate now = LocalDate.now(UTC);

        // Allowed
        Application fullDayOvertimeReduction = createApplication(savedPerson, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        fullDayOvertimeReduction.setStatus(ALLOWED);
        sut.save(fullDayOvertimeReduction);

        Application fullDayHoliday = createApplication(savedPerson, getVacationType(HOLIDAY), now, now.plusDays(2), FULL);
        fullDayHoliday.setStatus(ALLOWED);
        sut.save(fullDayHoliday);

        // Waiting
        final Application halfDayOvertimeReduction = createApplication(savedPerson, getVacationType(SPECIALLEAVE), now.plusDays(5), now.plusDays(10), MORNING);
        halfDayOvertimeReduction.setStatus(WAITING);
        sut.save(halfDayOvertimeReduction);

        // Cancelled
        final Application cancelledOvertimeReduction = createApplication(savedPerson, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        cancelledOvertimeReduction.setStatus(CANCELLED);
        sut.save(cancelledOvertimeReduction);

        // Rejected
        final Application rejectedOvertimeReduction = createApplication(savedPerson, getVacationType(SPECIALLEAVE), now, now.plusDays(2), FULL);
        rejectedOvertimeReduction.setStatus(REJECTED);
        sut.save(rejectedOvertimeReduction);

        // Revoked
        final Application revokedOvertimeReduction = createApplication(savedPerson, getVacationType(UNPAIDLEAVE), now, now.plusDays(2), FULL);
        revokedOvertimeReduction.setStatus(REVOKED);
        sut.save(revokedOvertimeReduction);

        final List<Application> allowedApplications = sut.findByStatusInAndEndDateGreaterThanEqual(List.of(ALLOWED, REJECTED), LocalDate.of(2020, 10, 3));
        assertThat(allowedApplications)
            .contains(fullDayOvertimeReduction, fullDayHoliday, rejectedOvertimeReduction)
            .hasSize(3);
    }

    @Test
    void findByStatusInEmptyStatus() {

        final Person person = new Person("sam", "smith", "sam", "smith@example.org");
        final Person savedPerson = personService.save(person);

        final LocalDate now = LocalDate.now(UTC);

        // Revoked
        final Application revokedOvertimeReduction = createApplication(savedPerson, getVacationType(UNPAIDLEAVE), now, now.plusDays(2), FULL);
        revokedOvertimeReduction.setStatus(REVOKED);
        sut.save(revokedOvertimeReduction);

        final List<Application> allowedApplications = sut.findByStatusInAndEndDateGreaterThanEqual(List.of(), LocalDate.of(2020, 10, 3));
        assertThat(allowedApplications)
            .isEmpty();
    }

    @Test
    void findByStatusInAndPersonIn() {

        final Person person = new Person("sam", "smith", "sam", "smith@example.org");
        final Person savedPerson = personService.save(person);

        final Person otherPerson = new Person("other sam", "smith", "sam", "smith@example.org");
        final Person savedOtherPerson = personService.save(otherPerson);

        final LocalDate now = LocalDate.now(UTC);

        // Allowed
        Application fullDayOvertimeReduction = createApplication(savedPerson, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        fullDayOvertimeReduction.setStatus(ALLOWED);
        sut.save(fullDayOvertimeReduction);

        // Waiting
        final Application halfDayOvertimeReduction = createApplication(savedPerson, getVacationType(SPECIALLEAVE), now.plusDays(5), now.plusDays(10), MORNING);
        halfDayOvertimeReduction.setStatus(WAITING);
        sut.save(halfDayOvertimeReduction);

        // Cancelled
        final Application cancelledOvertimeReduction = createApplication(savedPerson, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        cancelledOvertimeReduction.setStatus(CANCELLED);
        sut.save(cancelledOvertimeReduction);

        // Rejected
        Application rejectedHoliday = createApplication(savedPerson, getVacationType(HOLIDAY), now, now.plusDays(2), FULL);
        rejectedHoliday.setStatus(REJECTED);
        sut.save(rejectedHoliday);

        final Application rejectedOvertimeReduction = createApplication(savedOtherPerson, getVacationType(SPECIALLEAVE), now, now.plusDays(2), FULL);
        rejectedOvertimeReduction.setStatus(REJECTED);
        sut.save(rejectedOvertimeReduction);

        // Revoked
        final Application revokedOvertimeReduction = createApplication(savedOtherPerson, getVacationType(UNPAIDLEAVE), now, now.plusDays(2), FULL);
        revokedOvertimeReduction.setStatus(REVOKED);
        sut.save(revokedOvertimeReduction);

        final List<Application> allowedApplications = sut.findByStatusInAndPersonIn(List.of(ALLOWED, REJECTED), List.of(savedPerson, savedOtherPerson));
        assertThat(allowedApplications)
            .contains(rejectedHoliday, rejectedOvertimeReduction, fullDayOvertimeReduction)
            .hasSize(3);
    }

    @Test
    void findByStatusInAndPersonInOfOneUser() {

        final Person person = new Person("sam", "smith", "sam", "smith@example.org");
        final Person savedPerson = personService.save(person);

        final Person otherPerson = new Person("other sam", "smith", "sam", "smith@example.org");
        final Person savedOtherPerson = personService.save(otherPerson);

        final LocalDate now = LocalDate.now(UTC);

        // Allowed
        Application fullDayOvertimeReduction = createApplication(savedPerson, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        fullDayOvertimeReduction.setStatus(ALLOWED);
        sut.save(fullDayOvertimeReduction);

        // Waiting
        final Application halfDayOvertimeReduction = createApplication(savedPerson, getVacationType(SPECIALLEAVE), now.plusDays(5), now.plusDays(10), MORNING);
        halfDayOvertimeReduction.setStatus(WAITING);
        sut.save(halfDayOvertimeReduction);

        // Cancelled
        final Application cancelledOvertimeReduction = createApplication(savedPerson, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        cancelledOvertimeReduction.setStatus(CANCELLED);
        sut.save(cancelledOvertimeReduction);

        // Rejected
        Application rejectedHoliday = createApplication(savedPerson, getVacationType(HOLIDAY), now, now.plusDays(2), FULL);
        rejectedHoliday.setStatus(REJECTED);
        sut.save(rejectedHoliday);

        final Application rejectedOvertimeReduction = createApplication(savedOtherPerson, getVacationType(SPECIALLEAVE), now, now.plusDays(2), FULL);
        rejectedOvertimeReduction.setStatus(REJECTED);
        sut.save(rejectedOvertimeReduction);

        // Revoked
        final Application revokedOvertimeReduction = createApplication(savedOtherPerson, getVacationType(UNPAIDLEAVE), now, now.plusDays(2), FULL);
        revokedOvertimeReduction.setStatus(REVOKED);
        sut.save(revokedOvertimeReduction);

        final List<Application> allowedApplications = sut.findByStatusInAndPersonIn(List.of(ALLOWED, REJECTED), List.of(savedPerson));
        assertThat(allowedApplications)
            .contains(rejectedHoliday, fullDayOvertimeReduction)
            .hasSize(2);
    }

    @Test
    void findByStatusInAndPersonInNoResult() {

        final Person person = new Person("sam", "smith", "sam", "smith@example.org");
        final Person savedPerson = personService.save(person);

        final Person otherPerson = new Person("other sam", "smith", "sam", "smith@example.org");
        final Person savedOtherPerson = personService.save(otherPerson);

        final LocalDate now = LocalDate.now(UTC);

        // Allowed
        Application fullDayOvertimeReduction = createApplication(savedPerson, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        fullDayOvertimeReduction.setStatus(ALLOWED);
        sut.save(fullDayOvertimeReduction);

        // Waiting
        final Application halfDayOvertimeReduction = createApplication(savedPerson, getVacationType(SPECIALLEAVE), now.plusDays(5), now.plusDays(10), MORNING);
        halfDayOvertimeReduction.setStatus(WAITING);
        sut.save(halfDayOvertimeReduction);

        // Cancelled
        final Application cancelledOvertimeReduction = createApplication(savedPerson, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        cancelledOvertimeReduction.setStatus(CANCELLED);
        sut.save(cancelledOvertimeReduction);

        // Rejected
        Application rejectedHoliday = createApplication(savedPerson, getVacationType(HOLIDAY), now, now.plusDays(2), FULL);
        rejectedHoliday.setStatus(REJECTED);
        sut.save(rejectedHoliday);

        final Application rejectedOvertimeReduction = createApplication(savedOtherPerson, getVacationType(SPECIALLEAVE), now, now.plusDays(2), FULL);
        rejectedOvertimeReduction.setStatus(REJECTED);
        sut.save(rejectedOvertimeReduction);

        final List<Application> allowedApplications = sut.findByStatusInAndPersonIn(List.of(REVOKED), List.of(savedPerson, savedOtherPerson));
        assertThat(allowedApplications)
            .isEmpty();
    }

    @Test
    void ensureCountsTotalOvertimeReductionCorrectly() {

        final Person person = new Person("sam", "smith", "sam", "smith@example.org");
        final Person savedPerson = personService.save(person);

        final Person otherPerson = new Person("freddy", "Gwin", "freddy", "gwin@example.org");
        final Person savedOtherPerson = personService.save(otherPerson);

        final LocalDate now = LocalDate.now(UTC);

        // Allowed overtime reduction (8 hours) ------------------------------------------------------------------------
        Application fullDayOvertimeReduction = createApplication(savedPerson, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        fullDayOvertimeReduction.setHours(Duration.ofHours(8));
        fullDayOvertimeReduction.setStatus(ALLOWED);
        sut.save(fullDayOvertimeReduction);

        // Waiting overtime reduction (2.5 hours) ----------------------------------------------------------------------
        final Application halfDayOvertimeReduction = createApplication(savedPerson, getVacationType(OVERTIME), now.plusDays(5), now.plusDays(10), MORNING);
        halfDayOvertimeReduction.setHours(Duration.ofMinutes(150));
        halfDayOvertimeReduction.setStatus(WAITING);
        sut.save(halfDayOvertimeReduction);

        // Cancelled overtime reduction (1 hour) ----------------------------------------------------------------------
        final Application cancelledOvertimeReduction = createApplication(savedPerson, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        cancelledOvertimeReduction.setHours(Duration.ofHours(1));
        cancelledOvertimeReduction.setStatus(CANCELLED);
        sut.save(cancelledOvertimeReduction);

        // Rejected overtime reduction (1 hour) -----------------------------------------------------------------------
        final Application rejectedOvertimeReduction = createApplication(savedPerson, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        rejectedOvertimeReduction.setHours(Duration.ofHours(1));
        rejectedOvertimeReduction.setStatus(REJECTED);
        sut.save(rejectedOvertimeReduction);

        // Revoked overtime reduction (1 hour) ------------------------------------------------------------------------
        final Application revokedOvertimeReduction = createApplication(savedPerson, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        revokedOvertimeReduction.setHours(Duration.ofHours(1));
        revokedOvertimeReduction.setStatus(REVOKED);
        sut.save(revokedOvertimeReduction);

        // Holiday with hours set accidentally (1 hour) ---------------------------------------------------------------
        final Application holiday = createApplication(savedPerson, getVacationType(HOLIDAY), now.minusDays(8), now.minusDays(4), FULL);

        // NOTE: Holiday should not have hours set, but who knows....
        // More than once heard: "this should never happen" ;)
        holiday.setHours(Duration.ofHours(1));
        sut.save(holiday);

        // Overtime reduction for other person -------------------------------------------------------------------------
        final Application overtimeReduction = createApplication(savedOtherPerson, getVacationType(OVERTIME), now.plusDays(5), now.plusDays(10), NOON);
        overtimeReduction.setHours(Duration.ofMinutes(150));
        sut.save(overtimeReduction);

        // Let's calculate! --------------------------------------------------------------------------------------------

        BigDecimal totalHours = sut.calculateTotalOvertimeReductionOfPerson(person);
        assertThat(totalHours).isEqualTo(BigDecimal.valueOf(10.5));
    }

    @Test
    void findByHolidayReplacementAndEndDateIsGreaterThanEqualAndStatusIn() {

        final Person holidayReplacement = new Person("holly", "holly", "replacement", "holly@example.org");
        final Person savedHolidayReplacement = personService.save(holidayReplacement);

        final Person person = new Person("sam", "smith", "sam", "smith@example.org");
        final Person savedPerson = personService.save(person);

        final HolidayReplacementEntity holidayReplacementEntity = new HolidayReplacementEntity();
        holidayReplacementEntity.setPerson(savedHolidayReplacement);

        // correct
        final LocalDate from = LocalDate.of(2020, 5, 3);
        final LocalDate to = LocalDate.of(2020, 5, 10);
        final Application waitingApplication = createApplication(savedPerson, getVacationType(OVERTIME), from, to, FULL);
        waitingApplication.setHolidayReplacements(List.of(holidayReplacementEntity));
        waitingApplication.setStatus(WAITING);
        sut.save(waitingApplication);

        // other status
        final Application allowedApplication = createApplication(savedPerson, getVacationType(OVERTIME), from, to, FULL);
        allowedApplication.setHolidayReplacements(List.of(holidayReplacementEntity));
        allowedApplication.setStatus(ALLOWED);
        sut.save(allowedApplication);

        // other date
        final LocalDate otherStartDate = LocalDate.of(2020, 5, 3);
        final LocalDate otherEndDate = LocalDate.of(2020, 5, 4);
        final Application wrongDateApplication = createApplication(savedPerson, getVacationType(OVERTIME), otherStartDate, otherEndDate, FULL);
        wrongDateApplication.setHolidayReplacements(List.of(holidayReplacementEntity));
        wrongDateApplication.setStatus(WAITING);
        sut.save(wrongDateApplication);

        // other holiday replacement
        final Person otherHolidayReplacement = new Person("other", "other", "holiday", "other@example.org");
        final Person savedOtherHolidayReplacement = personService.save(otherHolidayReplacement);
        final HolidayReplacementEntity otherHolidayReplacementEntity = new HolidayReplacementEntity();
        otherHolidayReplacementEntity.setPerson(savedOtherHolidayReplacement);
        final Application otherHolidayReplacementApplication = createApplication(savedPerson, getVacationType(OVERTIME), from, to, FULL);
        otherHolidayReplacementApplication.setHolidayReplacements(List.of(otherHolidayReplacementEntity));
        otherHolidayReplacementApplication.setStatus(WAITING);
        sut.save(otherHolidayReplacementApplication);

        final LocalDate requestDate = LocalDate.of(2020, 5, 5);
        final List<ApplicationStatus> requestStatus = List.of(WAITING);
        final List<Application> applications = sut.findByHolidayReplacements_PersonAndEndDateIsGreaterThanEqualAndStatusIn(holidayReplacement, requestDate, requestStatus);
        assertThat(applications).hasSize(1).contains(waitingApplication);
    }

    @Test
    void findByStatusInAndStartDateBetweenAndUpcomingApplicationsReminderSendIsNull() {

        final Person person = new Person("sam", "smith", "sam", "smith@example.org");
        final Person savedPerson = personService.save(person);

        // yesterday
        final LocalDate yesterdayDates = LocalDate.of(2020, 5, 3);
        final Application yesterdayApplication = createApplication(savedPerson, getVacationType(HOLIDAY), yesterdayDates, yesterdayDates, FULL);
        yesterdayApplication.setStatus(ALLOWED);
        sut.save(yesterdayApplication);

        // today
        final LocalDate todayDates = LocalDate.of(2020, 5, 4);
        final Application todayApplication = createApplication(savedPerson, getVacationType(HOLIDAY), todayDates, todayDates, FULL);
        todayApplication.setStatus(ALLOWED);
        sut.save(todayApplication);

        // tomorrow
        final LocalDate tomorrowAllowedDates = LocalDate.of(2020, 5, 5);
        final Application tomorrowDateApplication = createApplication(savedPerson, getVacationType(HOLIDAY), tomorrowAllowedDates, tomorrowAllowedDates, FULL);
        tomorrowDateApplication.setStatus(ALLOWED);
        sut.save(tomorrowDateApplication);

        final Application tomorrowCancellationRequestDateApplication = createApplication(savedPerson, getVacationType(HOLIDAY), tomorrowAllowedDates, tomorrowAllowedDates, FULL);
        tomorrowCancellationRequestDateApplication.setStatus(ALLOWED_CANCELLATION_REQUESTED);
        sut.save(tomorrowCancellationRequestDateApplication);

        final Application tomorrowTemporaryAllowedDateApplication = createApplication(savedPerson, getVacationType(HOLIDAY), tomorrowAllowedDates, tomorrowAllowedDates, FULL);
        tomorrowTemporaryAllowedDateApplication.setStatus(TEMPORARY_ALLOWED);
        sut.save(tomorrowTemporaryAllowedDateApplication);

        final Application tomorrowWaitingDateApplication = createApplication(savedPerson, getVacationType(HOLIDAY), tomorrowAllowedDates, tomorrowAllowedDates, FULL);
        tomorrowWaitingDateApplication.setStatus(WAITING);
        sut.save(tomorrowWaitingDateApplication);

        // day after tomorrow
        final LocalDate dayAfterTomorrowAllowedDates = LocalDate.of(2020, 5, 6);

        final Application dayAfterTomorrowDateApplication = createApplication(savedPerson, getVacationType(HOLIDAY), dayAfterTomorrowAllowedDates, dayAfterTomorrowAllowedDates, FULL);
        dayAfterTomorrowDateApplication.setStatus(ALLOWED);
        sut.save(dayAfterTomorrowDateApplication);

        final LocalDate requestedStartDateFrom = LocalDate.of(2020, 5, 4);
        final LocalDate requestedStartDateTo = LocalDate.of(2020, 5, 5);
        final List<ApplicationStatus> requestStatuses = List.of(ALLOWED, ALLOWED_CANCELLATION_REQUESTED, TEMPORARY_ALLOWED);
        final List<Application> applications = sut.findByStatusInAndStartDateBetweenAndUpcomingApplicationsReminderSendIsNull(requestStatuses, requestedStartDateFrom, requestedStartDateTo);
        assertThat(applications)
            .containsOnly(todayApplication, tomorrowDateApplication, tomorrowCancellationRequestDateApplication, tomorrowTemporaryAllowedDateApplication);
    }

    @Test
    void findByStatusInAndStartDateAndHolidayReplacementsIsNotEmpty() {

        final Person person = new Person("sam", "smith", "sam", "smith@example.org");
        final Person savedPerson = personService.save(person);

        final HolidayReplacementEntity holidayReplacement = new HolidayReplacementEntity();
        holidayReplacement.setPerson(savedPerson);
        holidayReplacement.setNote("Note");

        // yesterday
        final LocalDate yesterdayDates = LocalDate.of(2020, 5, 3);
        final Application yesterdayApplication = createApplication(savedPerson, getVacationType(HOLIDAY), yesterdayDates, yesterdayDates, FULL);
        yesterdayApplication.setHolidayReplacements(List.of(holidayReplacement));
        yesterdayApplication.setStatus(ALLOWED);
        sut.save(yesterdayApplication);

        // today
        final LocalDate todayDates = LocalDate.of(2020, 5, 4);
        final Application todayApplication = createApplication(savedPerson, getVacationType(HOLIDAY), todayDates, todayDates, FULL);
        todayApplication.setStatus(ALLOWED);
        sut.save(todayApplication);

        // tomorrow
        final LocalDate tomorrowAllowedDates = LocalDate.of(2020, 5, 5);

        final Application tomorrowDateApplicationNoHolidayReplacement = createApplication(savedPerson, getVacationType(HOLIDAY), tomorrowAllowedDates, tomorrowAllowedDates, FULL);
        tomorrowDateApplicationNoHolidayReplacement.setStatus(ALLOWED);
        sut.save(tomorrowDateApplicationNoHolidayReplacement);

        final Application tomorrowDateApplication = createApplication(savedPerson, getVacationType(HOLIDAY), tomorrowAllowedDates, tomorrowAllowedDates, FULL);
        tomorrowDateApplication.setHolidayReplacements(List.of(holidayReplacement));
        tomorrowDateApplication.setStatus(ALLOWED);
        sut.save(tomorrowDateApplication);

        final Application tomorrowCancellationRequestDateApplication = createApplication(savedPerson, getVacationType(HOLIDAY), tomorrowAllowedDates, tomorrowAllowedDates, FULL);
        tomorrowCancellationRequestDateApplication.setStatus(ALLOWED_CANCELLATION_REQUESTED);
        tomorrowCancellationRequestDateApplication.setHolidayReplacements(List.of(holidayReplacement));
        sut.save(tomorrowCancellationRequestDateApplication);

        final Application tomorrowTemporaryAllowedDateApplication = createApplication(savedPerson, getVacationType(HOLIDAY), tomorrowAllowedDates, tomorrowAllowedDates, FULL);
        tomorrowTemporaryAllowedDateApplication.setStatus(TEMPORARY_ALLOWED);
        tomorrowTemporaryAllowedDateApplication.setHolidayReplacements(List.of(holidayReplacement));
        sut.save(tomorrowTemporaryAllowedDateApplication);

        final Application tomorrowDateApplicationAlreadySend = createApplication(savedPerson, getVacationType(HOLIDAY), tomorrowAllowedDates, tomorrowAllowedDates, FULL);
        tomorrowDateApplicationAlreadySend.setStatus(TEMPORARY_ALLOWED);
        tomorrowDateApplicationAlreadySend.setHolidayReplacements(List.of(holidayReplacement));
        tomorrowDateApplicationAlreadySend.setUpcomingHolidayReplacementNotificationSend(tomorrowAllowedDates);
        sut.save(tomorrowDateApplicationAlreadySend);

        final Application tomorrowWaitingDateApplication = createApplication(savedPerson, getVacationType(HOLIDAY), tomorrowAllowedDates, tomorrowAllowedDates, FULL);
        tomorrowWaitingDateApplication.setStatus(WAITING);
        sut.save(tomorrowWaitingDateApplication);

        // day after tomorrow
        final LocalDate dayAfterTomorrowAllowedDates = LocalDate.of(2020, 5, 6);

        final Application dayAfterTomorrowDateApplication = createApplication(savedPerson, getVacationType(HOLIDAY), dayAfterTomorrowAllowedDates, dayAfterTomorrowAllowedDates, FULL);
        dayAfterTomorrowDateApplication.setHolidayReplacements(List.of(holidayReplacement));
        dayAfterTomorrowDateApplication.setStatus(ALLOWED);
        sut.save(dayAfterTomorrowDateApplication);

        final LocalDate requestedStartDateFrom = LocalDate.of(2020, 5, 4);
        final LocalDate requestedStartDateTo = LocalDate.of(2020, 5, 5);
        final List<ApplicationStatus> requestStatuses = List.of(ALLOWED, ALLOWED_CANCELLATION_REQUESTED, TEMPORARY_ALLOWED);
        final List<Application> applications = sut.findByStatusInAndStartDateBetweenAndHolidayReplacementsIsNotEmptyAndUpcomingHolidayReplacementNotificationSendIsNull(requestStatuses, requestedStartDateFrom, requestedStartDateTo);
        assertThat(applications)
            .containsOnly(tomorrowDateApplication, tomorrowCancellationRequestDateApplication, tomorrowTemporaryAllowedDateApplication);
    }

    private VacationTypeEntity getVacationType(VacationCategory category) {
        final List<VacationTypeEntity> vacationTypeEntities = vacationTypeService.getAllVacationTypes().stream()
            .map(VacationTypeServiceImpl::convert)
            .collect(toList());

        for (VacationTypeEntity vacationType : vacationTypeEntities) {
            if (vacationType.isOfCategory(category)) {
                return vacationType;
            }
        }

        throw new IllegalStateException("No type with found with category: " + category.name());
    }
}
