package org.synyx.urlaubsverwaltung.application.application;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeEntity;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeServiceImpl;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;
import static org.assertj.core.api.Assertions.assertThat;
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
class ApplicationRepositoryIT extends SingleTenantTestContainersBase {

    @Autowired
    private ApplicationRepository sut;

    @Autowired
    private PersonService personService;
    @Autowired
    private VacationTypeService vacationTypeService;

    @Test
    void ensureApplicationForLeaveForStatusAndPersonAndWithinDateRange() {

        final Person max = personService.create("muster", "Max", "Mustermann", "mustermann@example.org");
        final Person marlene = personService.create("person2", "Marlene", "Musterfrau", "musterfrau@example.org");
        final VacationTypeEntity vacationType = getVacationType(HOLIDAY);

        final LocalDate askedStartDate = LocalDate.now(UTC).with(firstDayOfMonth());
        final LocalDate askedEndDate = LocalDate.now(UTC).with(lastDayOfMonth());

        // application for leave that should not be found
        final ApplicationEntity appNotInPeriodBecausePast = applicationEntity(max, vacationType, askedStartDate.minusDays(12), askedStartDate.minusDays(10), FULL);
        final ApplicationEntity appNotInPeriodBecauseFuture = applicationEntity(max, vacationType, askedEndDate.plusDays(10), askedEndDate.plusDays(12), FULL);

        sut.save(appNotInPeriodBecausePast);
        sut.save(appNotInPeriodBecauseFuture);

        // application for leave that should be found
        final ApplicationEntity appStartingBeforePeriod = applicationEntity(max, vacationType, askedStartDate.minusDays(5), askedStartDate.plusDays(1), FULL);
        final ApplicationEntity appEndingAfterPeriod = applicationEntity(max, vacationType, askedEndDate.minusDays(1), askedEndDate.plusDays(1), FULL);
        final ApplicationEntity appInBetween = applicationEntity(max, vacationType, askedStartDate.plusDays(10), askedStartDate.plusDays(12), FULL);
        final ApplicationEntity appStartingAtPeriod = applicationEntity(marlene, vacationType, askedStartDate, askedStartDate.plusDays(2), FULL);
        final ApplicationEntity appEndingAtPeriod = applicationEntity(marlene, vacationType, askedEndDate.minusDays(5), askedEndDate, FULL);

        sut.save(appStartingBeforePeriod);
        sut.save(appEndingAfterPeriod);
        sut.save(appInBetween);
        sut.save(appStartingAtPeriod);
        sut.save(appEndingAtPeriod);

        List<ApplicationStatus> statuses = List.of(WAITING);
        List<Person> persons = List.of(max, marlene);

        final List<ApplicationEntity> actualApplications = sut.findByStatusInAndPersonInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(statuses, persons, askedStartDate, askedEndDate);

        assertThat(actualApplications).contains(appStartingBeforePeriod, appEndingAfterPeriod, appInBetween, appStartingAtPeriod, appEndingAtPeriod);
    }

    @Test
    void ensureApplicationForLeaveWithEmoji() {

        final Person marlene = personService.create("marlene", "Marlene", "Musterfrau", "musterfrau@example.org");
        final VacationTypeEntity vacationType = getVacationType(HOLIDAY);

        final LocalDate askedStartDate = LocalDate.now(UTC).with(firstDayOfMonth());
        final LocalDate askedEndDate = LocalDate.now(UTC).with(lastDayOfMonth());

        // application for leave that should be found
        final ApplicationEntity application = applicationEntity(marlene, vacationType, askedStartDate, askedStartDate.plusDays(2), FULL);
        application.setReason("\uD83C\uDF1E");

        sut.save(application);

        List<ApplicationStatus> statuses = List.of(WAITING);
        List<Person> persons = List.of(marlene);

        final List<ApplicationEntity> actualApplications = sut.findByStatusInAndPersonInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(statuses, persons, askedStartDate, askedEndDate);

        assertThat(actualApplications).contains(application);
    }

    @Test
    void ensureReturnsNullAsTotalOvertimeReductionIfPersonHasNoApplicationsForLeaveYet() {

        final Person savedPerson = personService.create("muster", "Marlene", "Muster", "muster@example.org");

        BigDecimal totalHours = sut.calculateTotalOvertimeReductionOfPerson(savedPerson);
        assertThat(totalHours).isNull();
    }

    @Test
    void findByStatusIn() {

        final Person savedPerson = personService.create("sam", "sam", "smith", "smith@example.org");

        final LocalDate now = LocalDate.now(UTC);

        // Allowed
        final ApplicationEntity fullDayOvertimeReduction = applicationEntity(savedPerson, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        fullDayOvertimeReduction.setStatus(ALLOWED);
        sut.save(fullDayOvertimeReduction);

        final ApplicationEntity fullDayHoliday = applicationEntity(savedPerson, getVacationType(HOLIDAY), now, now.plusDays(2), FULL);
        fullDayHoliday.setStatus(ALLOWED);
        sut.save(fullDayHoliday);

        // Waiting
        final ApplicationEntity halfDayOvertimeReduction = applicationEntity(savedPerson, getVacationType(SPECIALLEAVE), now.plusDays(5), now.plusDays(10), MORNING);
        halfDayOvertimeReduction.setStatus(WAITING);
        sut.save(halfDayOvertimeReduction);

        // Cancelled
        final ApplicationEntity cancelledOvertimeReduction = applicationEntity(savedPerson, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        cancelledOvertimeReduction.setStatus(CANCELLED);
        sut.save(cancelledOvertimeReduction);

        // Rejected
        final ApplicationEntity rejectedOvertimeReduction = applicationEntity(savedPerson, getVacationType(SPECIALLEAVE), now, now.plusDays(2), FULL);
        rejectedOvertimeReduction.setStatus(REJECTED);
        sut.save(rejectedOvertimeReduction);

        // Revoked
        final ApplicationEntity revokedOvertimeReduction = applicationEntity(savedPerson, getVacationType(UNPAIDLEAVE), now, now.plusDays(2), FULL);
        revokedOvertimeReduction.setStatus(REVOKED);
        sut.save(revokedOvertimeReduction);

        final List<ApplicationEntity> allowedApplications = sut.findByStatusInAndEndDateGreaterThanEqual(List.of(ALLOWED), LocalDate.of(2020, 10, 3));
        assertThat(allowedApplications)
            .contains(fullDayOvertimeReduction, fullDayHoliday)
            .hasSize(2);
    }

    @Test
    void findByStatusInMultipleStatus() {

        final Person savedPerson = personService.create("sam", "sam", "smith", "smith@example.org");

        final LocalDate now = LocalDate.now(UTC);

        // Allowed
        final ApplicationEntity fullDayOvertimeReduction = applicationEntity(savedPerson, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        fullDayOvertimeReduction.setStatus(ALLOWED);
        sut.save(fullDayOvertimeReduction);

        final ApplicationEntity fullDayHoliday = applicationEntity(savedPerson, getVacationType(HOLIDAY), now, now.plusDays(2), FULL);
        fullDayHoliday.setStatus(ALLOWED);
        sut.save(fullDayHoliday);

        // Waiting
        final ApplicationEntity halfDayOvertimeReduction = applicationEntity(savedPerson, getVacationType(SPECIALLEAVE), now.plusDays(5), now.plusDays(10), MORNING);
        halfDayOvertimeReduction.setStatus(WAITING);
        sut.save(halfDayOvertimeReduction);

        // Cancelled
        final ApplicationEntity cancelledOvertimeReduction = applicationEntity(savedPerson, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        cancelledOvertimeReduction.setStatus(CANCELLED);
        sut.save(cancelledOvertimeReduction);

        // Rejected
        final ApplicationEntity rejectedOvertimeReduction = applicationEntity(savedPerson, getVacationType(SPECIALLEAVE), now, now.plusDays(2), FULL);
        rejectedOvertimeReduction.setStatus(REJECTED);
        sut.save(rejectedOvertimeReduction);

        // Revoked
        final ApplicationEntity revokedOvertimeReduction = applicationEntity(savedPerson, getVacationType(UNPAIDLEAVE), now, now.plusDays(2), FULL);
        revokedOvertimeReduction.setStatus(REVOKED);
        sut.save(revokedOvertimeReduction);

        final List<ApplicationEntity> allowedApplications = sut.findByStatusInAndEndDateGreaterThanEqual(List.of(ALLOWED, REJECTED), LocalDate.of(2020, 10, 3));
        assertThat(allowedApplications)
            .contains(fullDayOvertimeReduction, fullDayHoliday, rejectedOvertimeReduction)
            .hasSize(3);
    }

    @Test
    void findByStatusInEmptyStatus() {

        final Person savedPerson = personService.create("sam", "sam", "smith", "smith@example.org");

        final LocalDate now = LocalDate.now(UTC);

        // Revoked
        final ApplicationEntity revokedOvertimeReduction = applicationEntity(savedPerson, getVacationType(UNPAIDLEAVE), now, now.plusDays(2), FULL);
        revokedOvertimeReduction.setStatus(REVOKED);
        sut.save(revokedOvertimeReduction);

        final List<ApplicationEntity> allowedApplications = sut.findByStatusInAndEndDateGreaterThanEqual(List.of(), LocalDate.of(2020, 10, 3));
        assertThat(allowedApplications)
            .isEmpty();
    }

    @Test
    void findByStatusInAndPersonIn() {

        final Person savedPerson = personService.create("sam", "sam", "smith", "smith@example.org");
        final Person savedOtherPerson = personService.create("other sam", "sam", "smith", "smith@example.org");

        final LocalDate now = LocalDate.now(UTC);

        // Allowed
        final ApplicationEntity fullDayOvertimeReduction = applicationEntity(savedPerson, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        fullDayOvertimeReduction.setStatus(ALLOWED);
        sut.save(fullDayOvertimeReduction);

        // Waiting
        final ApplicationEntity halfDayOvertimeReduction = applicationEntity(savedPerson, getVacationType(SPECIALLEAVE), now.plusDays(5), now.plusDays(10), MORNING);
        halfDayOvertimeReduction.setStatus(WAITING);
        sut.save(halfDayOvertimeReduction);

        // Cancelled
        final ApplicationEntity cancelledOvertimeReduction = applicationEntity(savedPerson, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        cancelledOvertimeReduction.setStatus(CANCELLED);
        sut.save(cancelledOvertimeReduction);

        // Rejected
        final ApplicationEntity rejectedHoliday = applicationEntity(savedPerson, getVacationType(HOLIDAY), now, now.plusDays(2), FULL);
        rejectedHoliday.setStatus(REJECTED);
        sut.save(rejectedHoliday);

        final ApplicationEntity rejectedOvertimeReduction = applicationEntity(savedOtherPerson, getVacationType(SPECIALLEAVE), now, now.plusDays(2), FULL);
        rejectedOvertimeReduction.setStatus(REJECTED);
        sut.save(rejectedOvertimeReduction);

        // Revoked
        final ApplicationEntity revokedOvertimeReduction = applicationEntity(savedOtherPerson, getVacationType(UNPAIDLEAVE), now, now.plusDays(2), FULL);
        revokedOvertimeReduction.setStatus(REVOKED);
        sut.save(revokedOvertimeReduction);

        final List<ApplicationEntity> allowedApplications = sut.findByStatusInAndPersonIn(List.of(ALLOWED, REJECTED), List.of(savedPerson, savedOtherPerson));
        assertThat(allowedApplications)
            .contains(rejectedHoliday, rejectedOvertimeReduction, fullDayOvertimeReduction)
            .hasSize(3);
    }

    @Test
    void findByStatusInAndPersonInOfOneUser() {

        final Person savedPerson = personService.create("sam", "sam", "smith", "smith@example.org");
        final Person savedOtherPerson = personService.create("other sam", "sam", "smith", "smith@example.org");

        final LocalDate now = LocalDate.now(UTC);

        // Allowed
        final ApplicationEntity fullDayOvertimeReduction = applicationEntity(savedPerson, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        fullDayOvertimeReduction.setStatus(ALLOWED);
        sut.save(fullDayOvertimeReduction);

        // Waiting
        final ApplicationEntity halfDayOvertimeReduction = applicationEntity(savedPerson, getVacationType(SPECIALLEAVE), now.plusDays(5), now.plusDays(10), MORNING);
        halfDayOvertimeReduction.setStatus(WAITING);
        sut.save(halfDayOvertimeReduction);

        // Cancelled
        final ApplicationEntity cancelledOvertimeReduction = applicationEntity(savedPerson, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        cancelledOvertimeReduction.setStatus(CANCELLED);
        sut.save(cancelledOvertimeReduction);

        // Rejected
        final ApplicationEntity rejectedHoliday = applicationEntity(savedPerson, getVacationType(HOLIDAY), now, now.plusDays(2), FULL);
        rejectedHoliday.setStatus(REJECTED);
        sut.save(rejectedHoliday);

        final ApplicationEntity rejectedOvertimeReduction = applicationEntity(savedOtherPerson, getVacationType(SPECIALLEAVE), now, now.plusDays(2), FULL);
        rejectedOvertimeReduction.setStatus(REJECTED);
        sut.save(rejectedOvertimeReduction);

        // Revoked
        final ApplicationEntity revokedOvertimeReduction = applicationEntity(savedOtherPerson, getVacationType(UNPAIDLEAVE), now, now.plusDays(2), FULL);
        revokedOvertimeReduction.setStatus(REVOKED);
        sut.save(revokedOvertimeReduction);

        final List<ApplicationEntity> allowedApplications = sut.findByStatusInAndPersonIn(List.of(ALLOWED, REJECTED), List.of(savedPerson));
        assertThat(allowedApplications)
            .contains(rejectedHoliday, fullDayOvertimeReduction)
            .hasSize(2);
    }

    @Test
    void findByStatusInAndPersonInNoResult() {

        final Person savedPerson = personService.create("sam", "sam", "smith", "smith@example.org");
        final Person savedOtherPerson = personService.create("other sam", "sam", "smith", "smith@example.org");

        final LocalDate now = LocalDate.now(UTC);

        // Allowed
        final ApplicationEntity fullDayOvertimeReduction = applicationEntity(savedPerson, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        fullDayOvertimeReduction.setStatus(ALLOWED);
        sut.save(fullDayOvertimeReduction);

        // Waiting
        final ApplicationEntity halfDayOvertimeReduction = applicationEntity(savedPerson, getVacationType(SPECIALLEAVE), now.plusDays(5), now.plusDays(10), MORNING);
        halfDayOvertimeReduction.setStatus(WAITING);
        sut.save(halfDayOvertimeReduction);

        // Cancelled
        final ApplicationEntity cancelledOvertimeReduction = applicationEntity(savedPerson, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        cancelledOvertimeReduction.setStatus(CANCELLED);
        sut.save(cancelledOvertimeReduction);

        // Rejected
        final ApplicationEntity rejectedHoliday = applicationEntity(savedPerson, getVacationType(HOLIDAY), now, now.plusDays(2), FULL);
        rejectedHoliday.setStatus(REJECTED);
        sut.save(rejectedHoliday);

        final ApplicationEntity rejectedOvertimeReduction = applicationEntity(savedOtherPerson, getVacationType(SPECIALLEAVE), now, now.plusDays(2), FULL);
        rejectedOvertimeReduction.setStatus(REJECTED);
        sut.save(rejectedOvertimeReduction);

        final List<ApplicationEntity> allowedApplications = sut.findByStatusInAndPersonIn(List.of(REVOKED), List.of(savedPerson, savedOtherPerson));
        assertThat(allowedApplications)
            .isEmpty();
    }

    @Test
    void ensureCountsTotalOvertimeReductionCorrectly() {

        final Person person = personService.create("sam", "sam", "smith", "smith@example.org");
        final Person otherPerson = personService.create("freddy", "freddy", "Gwin", "gwin@example.org");

        final LocalDate now = LocalDate.now(UTC);

        // Waiting overtime reduction (2.5 hours) ----------------------------------------------------------------------
        final ApplicationEntity halfDayOvertimeReduction = applicationEntity(person, getVacationType(OVERTIME), now.plusDays(5), now.plusDays(10), MORNING);
        halfDayOvertimeReduction.setHours(Duration.ofMinutes(150));
        halfDayOvertimeReduction.setStatus(WAITING);
        sut.save(halfDayOvertimeReduction);

        // Temporary Allowed overtime reduction (8 hours) ------------------------------------------------------------------------
        final ApplicationEntity fullDayTemporaryAllowedOvertimeReduction = applicationEntity(person, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        fullDayTemporaryAllowedOvertimeReduction.setHours(Duration.ofHours(8));
        fullDayTemporaryAllowedOvertimeReduction.setStatus(TEMPORARY_ALLOWED);
        sut.save(fullDayTemporaryAllowedOvertimeReduction);

        // Allowed overtime reduction (8 hours) ------------------------------------------------------------------------
        final ApplicationEntity fullDayAllowedOvertimeReduction = applicationEntity(person, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        fullDayAllowedOvertimeReduction.setHours(Duration.ofHours(8));
        fullDayAllowedOvertimeReduction.setStatus(ALLOWED);
        sut.save(fullDayAllowedOvertimeReduction);

        // Allowed overtime reduction (8 hours) ------------------------------------------------------------------------
        final ApplicationEntity fullDayAllowedCancellationRequestedOvertimeReduction = applicationEntity(person, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        fullDayAllowedCancellationRequestedOvertimeReduction.setHours(Duration.ofHours(8));
        fullDayAllowedCancellationRequestedOvertimeReduction.setStatus(ALLOWED_CANCELLATION_REQUESTED);
        sut.save(fullDayAllowedCancellationRequestedOvertimeReduction);

        // Cancelled overtime reduction (1 hour) ----------------------------------------------------------------------
        final ApplicationEntity cancelledOvertimeReduction = applicationEntity(person, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        cancelledOvertimeReduction.setHours(Duration.ofHours(1));
        cancelledOvertimeReduction.setStatus(CANCELLED);
        sut.save(cancelledOvertimeReduction);

        // Rejected overtime reduction (1 hour) -----------------------------------------------------------------------
        final ApplicationEntity rejectedOvertimeReduction = applicationEntity(person, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        rejectedOvertimeReduction.setHours(Duration.ofHours(1));
        rejectedOvertimeReduction.setStatus(REJECTED);
        sut.save(rejectedOvertimeReduction);

        // Revoked overtime reduction (1 hour) ------------------------------------------------------------------------
        final ApplicationEntity revokedOvertimeReduction = applicationEntity(person, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        revokedOvertimeReduction.setHours(Duration.ofHours(1));
        revokedOvertimeReduction.setStatus(REVOKED);
        sut.save(revokedOvertimeReduction);

        // Holiday with hours set accidentally (1 hour) ---------------------------------------------------------------
        final ApplicationEntity holiday = applicationEntity(person, getVacationType(HOLIDAY), now.minusDays(8), now.minusDays(4), FULL);

        // NOTE: Holiday should not have hours set, but who knows....
        // More than once heard: "this should never happen" ;)
        holiday.setHours(Duration.ofHours(1));
        sut.save(holiday);

        // Overtime reduction for other person -------------------------------------------------------------------------
        final ApplicationEntity overtimeReduction = applicationEntity(otherPerson, getVacationType(OVERTIME), now.plusDays(5), now.plusDays(10), NOON);
        overtimeReduction.setHours(Duration.ofMinutes(150));
        sut.save(overtimeReduction);

        // Let's calculate! --------------------------------------------------------------------------------------------

        BigDecimal totalHours = sut.calculateTotalOvertimeReductionOfPerson(person);
        assertThat(totalHours).isEqualTo(BigDecimal.valueOf(26.5));
    }

    @Test
    void findByHolidayReplacementAndEndDateIsGreaterThanEqualAndStatusIn() {

        final Person holidayReplacement = personService.create("holly", "replacement", "holly", "holly@example.org");
        final Person person = personService.create("sam", "sam", "smith", "smith@example.org");

        final HolidayReplacementEntity holidayReplacementEntity = new HolidayReplacementEntity();
        holidayReplacementEntity.setPerson(holidayReplacement);

        // correct
        final LocalDate from = LocalDate.of(2020, 5, 3);
        final LocalDate to = LocalDate.of(2020, 5, 10);
        final ApplicationEntity waitingApplication = applicationEntity(person, getVacationType(OVERTIME), from, to, FULL);
        waitingApplication.setHolidayReplacements(List.of(holidayReplacementEntity));
        waitingApplication.setStatus(WAITING);
        sut.save(waitingApplication);

        // other status
        final ApplicationEntity allowedApplication = applicationEntity(person, getVacationType(OVERTIME), from, to, FULL);
        allowedApplication.setHolidayReplacements(List.of(holidayReplacementEntity));
        allowedApplication.setStatus(ALLOWED);
        sut.save(allowedApplication);

        // other date
        final LocalDate otherStartDate = LocalDate.of(2020, 5, 3);
        final LocalDate otherEndDate = LocalDate.of(2020, 5, 4);
        final ApplicationEntity wrongDateApplication = applicationEntity(person, getVacationType(OVERTIME), otherStartDate, otherEndDate, FULL);
        wrongDateApplication.setHolidayReplacements(List.of(holidayReplacementEntity));
        wrongDateApplication.setStatus(WAITING);
        sut.save(wrongDateApplication);

        // other replacement
        final Person savedOtherHolidayReplacement = personService.create("other", "holiday", "other", "other@example.org");
        final HolidayReplacementEntity otherHolidayReplacementEntity = new HolidayReplacementEntity();
        otherHolidayReplacementEntity.setPerson(savedOtherHolidayReplacement);
        final ApplicationEntity otherHolidayReplacementApplication = applicationEntity(person, getVacationType(OVERTIME), from, to, FULL);
        otherHolidayReplacementApplication.setHolidayReplacements(List.of(otherHolidayReplacementEntity));
        otherHolidayReplacementApplication.setStatus(WAITING);
        sut.save(otherHolidayReplacementApplication);

        final LocalDate requestDate = LocalDate.of(2020, 5, 5);
        final List<ApplicationStatus> requestStatus = List.of(WAITING);
        final List<ApplicationEntity> applications = sut.findByHolidayReplacements_PersonAndEndDateIsGreaterThanEqualAndStatusIn(holidayReplacement, requestDate, requestStatus);
        assertThat(applications).hasSize(1).contains(waitingApplication);
    }

    @Test
    void findByStatusInAndStartDateBetweenAndUpcomingApplicationsReminderSendIsNull() {

        final Person savedPerson = personService.create("sam", "sam", "smith", "smith@example.org");

        // yesterday
        final LocalDate yesterdayDates = LocalDate.of(2020, 5, 3);
        final ApplicationEntity yesterdayApplication = applicationEntity(savedPerson, getVacationType(HOLIDAY), yesterdayDates, yesterdayDates, FULL);
        yesterdayApplication.setStatus(ALLOWED);
        sut.save(yesterdayApplication);

        // today
        final LocalDate todayDates = LocalDate.of(2020, 5, 4);
        final ApplicationEntity todayApplication = applicationEntity(savedPerson, getVacationType(HOLIDAY), todayDates, todayDates, FULL);
        todayApplication.setStatus(ALLOWED);
        sut.save(todayApplication);

        // tomorrow
        final LocalDate tomorrowAllowedDates = LocalDate.of(2020, 5, 5);
        final ApplicationEntity tomorrowDateApplication = applicationEntity(savedPerson, getVacationType(HOLIDAY), tomorrowAllowedDates, tomorrowAllowedDates, FULL);
        tomorrowDateApplication.setStatus(ALLOWED);
        sut.save(tomorrowDateApplication);

        final ApplicationEntity tomorrowCancellationRequestDateApplication = applicationEntity(savedPerson, getVacationType(HOLIDAY), tomorrowAllowedDates, tomorrowAllowedDates, FULL);
        tomorrowCancellationRequestDateApplication.setStatus(ALLOWED_CANCELLATION_REQUESTED);
        sut.save(tomorrowCancellationRequestDateApplication);

        final ApplicationEntity tomorrowTemporaryAllowedDateApplication = applicationEntity(savedPerson, getVacationType(HOLIDAY), tomorrowAllowedDates, tomorrowAllowedDates, FULL);
        tomorrowTemporaryAllowedDateApplication.setStatus(TEMPORARY_ALLOWED);
        sut.save(tomorrowTemporaryAllowedDateApplication);

        final ApplicationEntity tomorrowWaitingDateApplication = applicationEntity(savedPerson, getVacationType(HOLIDAY), tomorrowAllowedDates, tomorrowAllowedDates, FULL);
        tomorrowWaitingDateApplication.setStatus(WAITING);
        sut.save(tomorrowWaitingDateApplication);

        // day after tomorrow
        final LocalDate dayAfterTomorrowAllowedDates = LocalDate.of(2020, 5, 6);

        final ApplicationEntity dayAfterTomorrowDateApplication = applicationEntity(savedPerson, getVacationType(HOLIDAY), dayAfterTomorrowAllowedDates, dayAfterTomorrowAllowedDates, FULL);
        dayAfterTomorrowDateApplication.setStatus(ALLOWED);
        sut.save(dayAfterTomorrowDateApplication);

        final LocalDate requestedStartDateFrom = LocalDate.of(2020, 5, 4);
        final LocalDate requestedStartDateTo = LocalDate.of(2020, 5, 5);
        final List<ApplicationStatus> requestStatuses = List.of(ALLOWED, ALLOWED_CANCELLATION_REQUESTED, TEMPORARY_ALLOWED);
        final List<ApplicationEntity> applications = sut.findByStatusInAndStartDateBetweenAndUpcomingApplicationsReminderSendIsNull(requestStatuses, requestedStartDateFrom, requestedStartDateTo);
        assertThat(applications)
            .containsOnly(todayApplication, tomorrowDateApplication, tomorrowCancellationRequestDateApplication, tomorrowTemporaryAllowedDateApplication);
    }

    @Test
    void findByStatusInAndStartDateAndHolidayReplacementsIsNotEmpty() {

        final Person savedPerson = personService.create("sam", "sam", "smith", "smith@example.org");

        final HolidayReplacementEntity holidayReplacement = new HolidayReplacementEntity();
        holidayReplacement.setPerson(savedPerson);
        holidayReplacement.setNote("Note");

        // yesterday
        final LocalDate yesterdayDates = LocalDate.of(2020, 5, 3);
        final ApplicationEntity yesterdayApplication = applicationEntity(savedPerson, getVacationType(HOLIDAY), yesterdayDates, yesterdayDates, FULL);
        yesterdayApplication.setHolidayReplacements(List.of(holidayReplacement));
        yesterdayApplication.setStatus(ALLOWED);
        sut.save(yesterdayApplication);

        // today
        final LocalDate todayDates = LocalDate.of(2020, 5, 4);
        final ApplicationEntity todayApplication = applicationEntity(savedPerson, getVacationType(HOLIDAY), todayDates, todayDates, FULL);
        todayApplication.setStatus(ALLOWED);
        sut.save(todayApplication);

        // tomorrow
        final LocalDate tomorrowAllowedDates = LocalDate.of(2020, 5, 5);

        final ApplicationEntity tomorrowDateApplicationNoHolidayReplacement = applicationEntity(savedPerson, getVacationType(HOLIDAY), tomorrowAllowedDates, tomorrowAllowedDates, FULL);
        tomorrowDateApplicationNoHolidayReplacement.setStatus(ALLOWED);
        sut.save(tomorrowDateApplicationNoHolidayReplacement);

        final ApplicationEntity tomorrowDateApplication = applicationEntity(savedPerson, getVacationType(HOLIDAY), tomorrowAllowedDates, tomorrowAllowedDates, FULL);
        tomorrowDateApplication.setHolidayReplacements(List.of(holidayReplacement));
        tomorrowDateApplication.setStatus(ALLOWED);
        sut.save(tomorrowDateApplication);

        final ApplicationEntity tomorrowCancellationRequestDateApplication = applicationEntity(savedPerson, getVacationType(HOLIDAY), tomorrowAllowedDates, tomorrowAllowedDates, FULL);
        tomorrowCancellationRequestDateApplication.setStatus(ALLOWED_CANCELLATION_REQUESTED);
        tomorrowCancellationRequestDateApplication.setHolidayReplacements(List.of(holidayReplacement));
        sut.save(tomorrowCancellationRequestDateApplication);

        final ApplicationEntity tomorrowTemporaryAllowedDateApplication = applicationEntity(savedPerson, getVacationType(HOLIDAY), tomorrowAllowedDates, tomorrowAllowedDates, FULL);
        tomorrowTemporaryAllowedDateApplication.setStatus(TEMPORARY_ALLOWED);
        tomorrowTemporaryAllowedDateApplication.setHolidayReplacements(List.of(holidayReplacement));
        sut.save(tomorrowTemporaryAllowedDateApplication);

        final ApplicationEntity tomorrowDateApplicationAlreadySend = applicationEntity(savedPerson, getVacationType(HOLIDAY), tomorrowAllowedDates, tomorrowAllowedDates, FULL);
        tomorrowDateApplicationAlreadySend.setStatus(TEMPORARY_ALLOWED);
        tomorrowDateApplicationAlreadySend.setHolidayReplacements(List.of(holidayReplacement));
        tomorrowDateApplicationAlreadySend.setUpcomingHolidayReplacementNotificationSend(tomorrowAllowedDates);
        sut.save(tomorrowDateApplicationAlreadySend);

        final ApplicationEntity tomorrowWaitingDateApplication = applicationEntity(savedPerson, getVacationType(HOLIDAY), tomorrowAllowedDates, tomorrowAllowedDates, FULL);
        tomorrowWaitingDateApplication.setStatus(WAITING);
        sut.save(tomorrowWaitingDateApplication);

        // day after tomorrow
        final LocalDate dayAfterTomorrowAllowedDates = LocalDate.of(2020, 5, 6);

        final ApplicationEntity dayAfterTomorrowDateApplication = applicationEntity(savedPerson, getVacationType(HOLIDAY), dayAfterTomorrowAllowedDates, dayAfterTomorrowAllowedDates, FULL);
        dayAfterTomorrowDateApplication.setHolidayReplacements(List.of(holidayReplacement));
        dayAfterTomorrowDateApplication.setStatus(ALLOWED);
        sut.save(dayAfterTomorrowDateApplication);

        final LocalDate requestedStartDateFrom = LocalDate.of(2020, 5, 4);
        final LocalDate requestedStartDateTo = LocalDate.of(2020, 5, 5);
        final List<ApplicationStatus> requestStatuses = List.of(ALLOWED, ALLOWED_CANCELLATION_REQUESTED, TEMPORARY_ALLOWED);
        final List<ApplicationEntity> applications = sut.findByStatusInAndStartDateBetweenAndHolidayReplacementsIsNotEmptyAndUpcomingHolidayReplacementNotificationSendIsNull(requestStatuses, requestedStartDateFrom, requestedStartDateTo);
        assertThat(applications)
            .containsOnly(tomorrowDateApplication, tomorrowCancellationRequestDateApplication, tomorrowTemporaryAllowedDateApplication);
    }

    @Test
    void ensureFindByStatusInAndPersonAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqualAndVacationTypeCategory() {

        final Person max = personService.create("muster", "Max", "Mustermann", "mustermann@example.org");
        final Person marlene = personService.create("person2", "Marlene", "Musterfrau", "musterfrau@example.org");
        final VacationTypeEntity overtime = getVacationType(OVERTIME);

        final LocalDate askedStartDate = LocalDate.now(UTC).with(firstDayOfMonth());
        final LocalDate askedEndDate = LocalDate.now(UTC).with(lastDayOfMonth());

        // application for leave that should not be found
        final ApplicationEntity appWrongVacationType = applicationEntity(marlene, getVacationType(HOLIDAY), askedStartDate.plusDays(1), askedEndDate.minusDays(1), FULL);
        sut.save(appWrongVacationType);

        final ApplicationEntity appWrongPerson = applicationEntity(max, overtime, askedStartDate.plusDays(1), askedEndDate.minusDays(1), FULL);
        sut.save(appWrongPerson);

        final ApplicationEntity appWrongStatus = applicationEntity(marlene, overtime, askedStartDate.plusDays(1), askedEndDate.plusDays(1), FULL);
        appWrongStatus.setStatus(CANCELLED);
        sut.save(appWrongStatus);


        // application for leave that should be found
        final ApplicationEntity appStartBeforeAsked = applicationEntity(marlene, overtime, askedStartDate.minusDays(1), askedEndDate.minusDays(1), FULL);
        final ApplicationEntity appEndAfterAsked = applicationEntity(marlene, overtime, askedStartDate.plusDays(1), askedEndDate.plusDays(1), FULL);
        final ApplicationEntity appInBetween = applicationEntity(marlene, overtime, askedStartDate.plusDays(10), askedStartDate.plusDays(12), FULL);
        final ApplicationEntity appStartingAtPeriod = applicationEntity(marlene, overtime, askedStartDate, askedStartDate.plusDays(2), FULL);
        final ApplicationEntity appEndingAtPeriod = applicationEntity(marlene, overtime, askedEndDate.minusDays(5), askedEndDate, FULL);

        sut.save(appStartBeforeAsked);
        sut.save(appEndAfterAsked);
        sut.save(appInBetween);
        sut.save(appStartingAtPeriod);
        sut.save(appEndingAtPeriod);

        final List<ApplicationStatus> statuses = List.of(WAITING);

        final List<ApplicationEntity> actualApplications = sut.findByStatusInAndPersonAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqualAndVacationTypeCategory(statuses, marlene, askedStartDate, askedEndDate, OVERTIME);
        assertThat(actualApplications).containsOnly(appStartBeforeAsked, appEndAfterAsked, appInBetween, appStartingAtPeriod, appEndingAtPeriod);
    }

    @Test
    void ensureFindByPersonInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual() {

        final Person max = personService.create("muster", "Max", "Mustermann", "mustermann@example.org");
        final Person marlene = personService.create("person2", "Marlene", "Musterfrau", "musterfrau@example.org");

        final LocalDate askedStartDate = LocalDate.now(UTC).with(firstDayOfMonth());
        final LocalDate askedEndDate = LocalDate.now(UTC).with(lastDayOfMonth());

        final ApplicationEntity validHolidayStartingBefore = applicationEntity(marlene, getVacationType(HOLIDAY), askedStartDate.minusDays(1), askedStartDate.plusDays(1), FULL);
        sut.save(validHolidayStartingBefore);

        final ApplicationEntity validOvertimeEndingAfter = applicationEntity(marlene, getVacationType(OVERTIME), askedEndDate.minusDays(1), askedEndDate.plusDays(1), FULL);
        sut.save(validOvertimeEndingAfter);

        final ApplicationEntity validSpecialLeaveInBetween = applicationEntity(marlene, getVacationType(SPECIALLEAVE), askedStartDate.plusDays(2), askedEndDate.minusDays(2), FULL);
        sut.save(validSpecialLeaveInBetween);

        final ApplicationEntity invalidApplication = applicationEntity(max, getVacationType(HOLIDAY), askedStartDate.plusDays(2), askedEndDate.minusDays(2), FULL);
        sut.save(invalidApplication);

        final List<ApplicationEntity> actual = sut.findByPersonInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqualAndStatusIn(List.of(marlene), askedStartDate, askedEndDate, List.of(WAITING));

        assertThat(actual).containsExactlyInAnyOrder(validHolidayStartingBefore, validOvertimeEndingAfter, validSpecialLeaveInBetween);
    }

    @Test
    void findAllByReplacements_Person() {

        final Person person = personService.create("muster", "Max", "Mustermann", "mustermann@example.org");

        final HolidayReplacementEntity holidayReplacement = new HolidayReplacementEntity();
        holidayReplacement.setPerson(person);

        final ApplicationEntity application = new ApplicationEntity();
        application.setHolidayReplacements(List.of(holidayReplacement));

        sut.save(application);

        final List<ApplicationEntity> applicationsByHolidayReplacement = sut.findAllByHolidayReplacements_Person(person);

        assertThat(applicationsByHolidayReplacement).contains(application);
    }

    private VacationTypeEntity getVacationType(VacationCategory category) {
        final List<VacationTypeEntity> vacationTypeEntities = vacationTypeService.getAllVacationTypes().stream()
            .map(VacationTypeServiceImpl::convert)
            .toList();

        for (VacationTypeEntity vacationType : vacationTypeEntities) {
            if (vacationType.isOfCategory(category)) {
                return vacationType;
            }
        }

        throw new IllegalStateException("No type with found with category: " + category.name());
    }

    public static ApplicationEntity applicationEntity(Person person, VacationTypeEntity vacationType,
                                                      LocalDate startDate, LocalDate endDate, DayLength dayLength) {

        final ApplicationEntity entity = new ApplicationEntity();
        entity.setPerson(person);
        entity.setStartDate(startDate);
        entity.setEndDate(endDate);
        entity.setDayLength(dayLength);
        entity.setVacationType(vacationType);
        entity.setStatus(ApplicationStatus.WAITING);
        return entity;
    }
}
