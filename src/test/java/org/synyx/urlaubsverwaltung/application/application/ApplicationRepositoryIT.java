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

        final Person max = personService.create("muster", "Mustermann", "Max", "mustermann@example.org");
        final Person marlene = personService.create("person2", "Musterfrau", "Marlene", "musterfrau@example.org");
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
    void ensureApplicationForLeaveWithEmoji() {

        final Person marlene = personService.create("marlene", "Musterfrau", "Marlene", "musterfrau@example.org");
        final VacationTypeEntity vacationType = getVacationType(HOLIDAY);

        final LocalDate askedStartDate = LocalDate.now(UTC).with(firstDayOfMonth());
        final LocalDate askedEndDate = LocalDate.now(UTC).with(lastDayOfMonth());

        // application for leave that should be found
        final Application application = createApplication(marlene, vacationType, askedStartDate, askedStartDate.plusDays(2), FULL);
        application.setReason("\uD83C\uDF1E");

        sut.save(application);

        List<ApplicationStatus> statuses = List.of(WAITING);
        List<Person> persons = List.of(marlene);

        final List<Application> actualApplications = sut.findByStatusInAndPersonInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(statuses, persons, askedStartDate, askedEndDate);

        assertThat(actualApplications).contains(application);
    }

    @Test
    void ensureReturnsNullAsTotalOvertimeReductionIfPersonHasNoApplicationsForLeaveYet() {

        final Person savedPerson = personService.create("muster", "Muster", "Marlene", "muster@example.org");

        BigDecimal totalHours = sut.calculateTotalOvertimeReductionOfPerson(savedPerson);
        assertThat(totalHours).isNull();
    }

    @Test
    void findByStatusIn() {

        final Person savedPerson = personService.create("sam", "smith", "sam", "smith@example.org");

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

        final Person savedPerson = personService.create("sam", "smith", "sam", "smith@example.org");

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

        final Person savedPerson = personService.create("sam", "smith", "sam", "smith@example.org");

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

        final Person savedPerson = personService.create("sam", "smith", "sam", "smith@example.org");
        final Person savedOtherPerson = personService.create("other sam", "smith", "sam", "smith@example.org");

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

        final Person savedPerson = personService.create("sam", "smith", "sam", "smith@example.org");
        final Person savedOtherPerson = personService.create("other sam", "smith", "sam", "smith@example.org");

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

        final Person savedPerson = personService.create("sam", "smith", "sam", "smith@example.org");
        final Person savedOtherPerson = personService.create("other sam", "smith", "sam", "smith@example.org");

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

        final Person person = personService.create("sam", "smith", "sam", "smith@example.org");
        final Person otherPerson = personService.create("freddy", "Gwin", "freddy", "gwin@example.org");

        final LocalDate now = LocalDate.now(UTC);

        // Waiting overtime reduction (2.5 hours) ----------------------------------------------------------------------
        final Application halfDayOvertimeReduction = createApplication(person, getVacationType(OVERTIME), now.plusDays(5), now.plusDays(10), MORNING);
        halfDayOvertimeReduction.setHours(Duration.ofMinutes(150));
        halfDayOvertimeReduction.setStatus(WAITING);
        sut.save(halfDayOvertimeReduction);

        // Temporary Allowed overtime reduction (8 hours) ------------------------------------------------------------------------
        Application fullDayTemporaryAllowedOvertimeReduction = createApplication(person, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        fullDayTemporaryAllowedOvertimeReduction.setHours(Duration.ofHours(8));
        fullDayTemporaryAllowedOvertimeReduction.setStatus(TEMPORARY_ALLOWED);
        sut.save(fullDayTemporaryAllowedOvertimeReduction);

        // Allowed overtime reduction (8 hours) ------------------------------------------------------------------------
        Application fullDayAllowedOvertimeReduction = createApplication(person, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        fullDayAllowedOvertimeReduction.setHours(Duration.ofHours(8));
        fullDayAllowedOvertimeReduction.setStatus(ALLOWED);
        sut.save(fullDayAllowedOvertimeReduction);

        // Allowed overtime reduction (8 hours) ------------------------------------------------------------------------
        Application fullDayAllowedCancellationRequestedOvertimeReduction = createApplication(person, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        fullDayAllowedCancellationRequestedOvertimeReduction.setHours(Duration.ofHours(8));
        fullDayAllowedCancellationRequestedOvertimeReduction.setStatus(ALLOWED_CANCELLATION_REQUESTED);
        sut.save(fullDayAllowedCancellationRequestedOvertimeReduction);

        // Cancelled overtime reduction (1 hour) ----------------------------------------------------------------------
        final Application cancelledOvertimeReduction = createApplication(person, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        cancelledOvertimeReduction.setHours(Duration.ofHours(1));
        cancelledOvertimeReduction.setStatus(CANCELLED);
        sut.save(cancelledOvertimeReduction);

        // Rejected overtime reduction (1 hour) -----------------------------------------------------------------------
        final Application rejectedOvertimeReduction = createApplication(person, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        rejectedOvertimeReduction.setHours(Duration.ofHours(1));
        rejectedOvertimeReduction.setStatus(REJECTED);
        sut.save(rejectedOvertimeReduction);

        // Revoked overtime reduction (1 hour) ------------------------------------------------------------------------
        final Application revokedOvertimeReduction = createApplication(person, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        revokedOvertimeReduction.setHours(Duration.ofHours(1));
        revokedOvertimeReduction.setStatus(REVOKED);
        sut.save(revokedOvertimeReduction);

        // Holiday with hours set accidentally (1 hour) ---------------------------------------------------------------
        final Application holiday = createApplication(person, getVacationType(HOLIDAY), now.minusDays(8), now.minusDays(4), FULL);

        // NOTE: Holiday should not have hours set, but who knows....
        // More than once heard: "this should never happen" ;)
        holiday.setHours(Duration.ofHours(1));
        sut.save(holiday);

        // Overtime reduction for other person -------------------------------------------------------------------------
        final Application overtimeReduction = createApplication(otherPerson, getVacationType(OVERTIME), now.plusDays(5), now.plusDays(10), NOON);
        overtimeReduction.setHours(Duration.ofMinutes(150));
        sut.save(overtimeReduction);

        // Let's calculate! --------------------------------------------------------------------------------------------

        BigDecimal totalHours = sut.calculateTotalOvertimeReductionOfPerson(person);
        assertThat(totalHours).isEqualTo(BigDecimal.valueOf(26.5));
    }

    @Test
    void ensureCountsTotalOvertimeReductionBeforeDateCorrectly() {

        final Person person = personService.create("sam", "smith", "sam", "smith@example.org");
        final Person otherPerson = personService.create("freddy", "Gwin", "freddy", "gwin@example.org");

        final LocalDate now = LocalDate.now(UTC);

        // Should be in the calculation
        final Application halfDayWaitingOvertimeReduction = createApplication(person, getVacationType(OVERTIME), LocalDate.of(2021, 12, 31), now.plusDays(10), MORNING);
        halfDayWaitingOvertimeReduction.setHours(Duration.ofMinutes(150));
        halfDayWaitingOvertimeReduction.setStatus(WAITING);
        sut.save(halfDayWaitingOvertimeReduction);

        Application fullDayTemporaryAllowedOvertimeReduction = createApplication(person, getVacationType(OVERTIME), LocalDate.of(2021, 12, 31), now.plusDays(2), FULL);
        fullDayTemporaryAllowedOvertimeReduction.setHours(Duration.ofHours(8));
        fullDayTemporaryAllowedOvertimeReduction.setStatus(TEMPORARY_ALLOWED);
        sut.save(fullDayTemporaryAllowedOvertimeReduction);

        Application fullDayAllowedOvertimeReduction = createApplication(person, getVacationType(OVERTIME), LocalDate.of(2021, 12, 31), now.plusDays(2), FULL);
        fullDayAllowedOvertimeReduction.setHours(Duration.ofHours(8));
        fullDayAllowedOvertimeReduction.setStatus(ALLOWED);
        sut.save(fullDayAllowedOvertimeReduction);

        Application fullDayAllowedCancellationRequestedOvertimeReduction = createApplication(person, getVacationType(OVERTIME), LocalDate.of(2021, 12, 31), now.plusDays(2), FULL);
        fullDayAllowedCancellationRequestedOvertimeReduction.setHours(Duration.ofHours(8));
        fullDayAllowedCancellationRequestedOvertimeReduction.setStatus(ALLOWED_CANCELLATION_REQUESTED);
        sut.save(fullDayAllowedCancellationRequestedOvertimeReduction);

        // Should NOT be in the calculation
        final Application cancelledOvertimeReduction = createApplication(person, getVacationType(OVERTIME), LocalDate.of(2021, 12, 31), now.plusDays(2), FULL);
        cancelledOvertimeReduction.setHours(Duration.ofHours(1));
        cancelledOvertimeReduction.setStatus(CANCELLED);
        sut.save(cancelledOvertimeReduction);

        final Application rejectedOvertimeReduction = createApplication(person, getVacationType(OVERTIME), LocalDate.of(2021, 12, 31), now.plusDays(2), FULL);
        rejectedOvertimeReduction.setHours(Duration.ofHours(1));
        rejectedOvertimeReduction.setStatus(REJECTED);
        sut.save(rejectedOvertimeReduction);

        final Application revokedOvertimeReduction = createApplication(person, getVacationType(OVERTIME), LocalDate.of(2021, 12, 31), now.plusDays(2), FULL);
        revokedOvertimeReduction.setHours(Duration.ofHours(1));
        revokedOvertimeReduction.setStatus(REVOKED);
        sut.save(revokedOvertimeReduction);

        // NOTE: Holiday should not have hours set, but who knows....More than once heard: "this should never happen" ;)
        final Application holiday = createApplication(person, getVacationType(HOLIDAY), LocalDate.of(2021, 12, 31), now.minusDays(4), FULL);
        holiday.setHours(Duration.ofHours(1));
        sut.save(holiday);

        final Application overtimeReduction = createApplication(otherPerson, getVacationType(OVERTIME), LocalDate.of(2021, 12, 31), now.plusDays(10), NOON);
        overtimeReduction.setHours(Duration.ofMinutes(150));
        sut.save(overtimeReduction);

        final Application overtimeReductionAfterDate = createApplication(person, getVacationType(OVERTIME), LocalDate.of(2022, 1, 1), now.plusDays(10), NOON);
        overtimeReductionAfterDate.setHours(Duration.ofMinutes(150));
        sut.save(overtimeReductionAfterDate);

        // Let's calculate! --------------------------------------------------------------------------------------------
        final BigDecimal totalHours = sut.calculateTotalOvertimeReductionOfPersonBefore(person, LocalDate.of(2022, 1, 1));
        assertThat(totalHours).isEqualTo(BigDecimal.valueOf(26.5));
    }

    @Test
    void findByHolidayReplacementAndEndDateIsGreaterThanEqualAndStatusIn() {

        final Person holidayReplacement = personService.create("holly", "holly", "replacement", "holly@example.org");
        final Person person = personService.create("sam", "smith", "sam", "smith@example.org");

        final HolidayReplacementEntity holidayReplacementEntity = new HolidayReplacementEntity();
        holidayReplacementEntity.setPerson(holidayReplacement);

        // correct
        final LocalDate from = LocalDate.of(2020, 5, 3);
        final LocalDate to = LocalDate.of(2020, 5, 10);
        final Application waitingApplication = createApplication(person, getVacationType(OVERTIME), from, to, FULL);
        waitingApplication.setHolidayReplacements(List.of(holidayReplacementEntity));
        waitingApplication.setStatus(WAITING);
        sut.save(waitingApplication);

        // other status
        final Application allowedApplication = createApplication(person, getVacationType(OVERTIME), from, to, FULL);
        allowedApplication.setHolidayReplacements(List.of(holidayReplacementEntity));
        allowedApplication.setStatus(ALLOWED);
        sut.save(allowedApplication);

        // other date
        final LocalDate otherStartDate = LocalDate.of(2020, 5, 3);
        final LocalDate otherEndDate = LocalDate.of(2020, 5, 4);
        final Application wrongDateApplication = createApplication(person, getVacationType(OVERTIME), otherStartDate, otherEndDate, FULL);
        wrongDateApplication.setHolidayReplacements(List.of(holidayReplacementEntity));
        wrongDateApplication.setStatus(WAITING);
        sut.save(wrongDateApplication);

        // other replacement
        final Person savedOtherHolidayReplacement = personService.create("other", "other", "holiday", "other@example.org");
        final HolidayReplacementEntity otherHolidayReplacementEntity = new HolidayReplacementEntity();
        otherHolidayReplacementEntity.setPerson(savedOtherHolidayReplacement);
        final Application otherHolidayReplacementApplication = createApplication(person, getVacationType(OVERTIME), from, to, FULL);
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

        final Person savedPerson = personService.create("sam", "smith", "sam", "smith@example.org");

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

        final Person savedPerson = personService.create("sam", "smith", "sam", "smith@example.org");

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

    @Test
    void ensureFindByStatusInAndPersonAndStartDateBetweenAndVacationTypeCategory() {

        final Person max = personService.create("muster", "Mustermann", "Max", "mustermann@example.org");
        final Person marlene = personService.create("person2", "Musterfrau", "Marlene", "musterfrau@example.org");
        final VacationTypeEntity overtime = getVacationType(OVERTIME);

        final LocalDate askedStartDate = LocalDate.now(UTC).with(firstDayOfMonth());
        final LocalDate askedEndDate = LocalDate.now(UTC).with(lastDayOfMonth());

        // application for leave that should not be found
        final Application appWrongVacationType = createApplication(marlene, getVacationType(HOLIDAY), askedStartDate.plusDays(1), askedEndDate.minusDays(1), FULL);
        sut.save(appWrongVacationType);

        final Application appWrongPerson = createApplication(max, overtime, askedStartDate.plusDays(1), askedEndDate.minusDays(1), FULL);
        sut.save(appWrongPerson);

        final Application appWrongStatus = createApplication(marlene, overtime, askedStartDate.plusDays(1), askedEndDate.plusDays(1), FULL);
        appWrongStatus.setStatus(CANCELLED);
        sut.save(appWrongStatus);

        final Application appStartBeforeAsked = createApplication(marlene, overtime, askedStartDate.minusDays(1), askedEndDate.minusDays(1), FULL);
        sut.save(appStartBeforeAsked);

        // application for leave that should be found
        final Application appEndAfterAsked = createApplication(marlene, overtime, askedStartDate.plusDays(1), askedEndDate.plusDays(1), FULL);
        final Application appInBetween = createApplication(marlene, overtime, askedStartDate.plusDays(10), askedStartDate.plusDays(12), FULL);
        final Application appStartingAtPeriod = createApplication(marlene, overtime, askedStartDate, askedStartDate.plusDays(2), FULL);
        final Application appEndingAtPeriod = createApplication(marlene, overtime, askedEndDate.minusDays(5), askedEndDate, FULL);

        sut.save(appEndAfterAsked);
        sut.save(appInBetween);
        sut.save(appStartingAtPeriod);
        sut.save(appEndingAtPeriod);

        final List<ApplicationStatus> statuses = List.of(WAITING);

        final List<Application> actualApplications = sut.findByStatusInAndPersonAndStartDateBetweenAndVacationTypeCategory(statuses, marlene, askedStartDate, askedEndDate, OVERTIME);
        assertThat(actualApplications).containsOnly(appEndAfterAsked, appInBetween, appStartingAtPeriod, appEndingAtPeriod);
    }

    @Test
    void ensureFindByStatusInAndPersonAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqualAndVacationTypeCategory() {

        final Person max = personService.create("muster", "Mustermann", "Max", "mustermann@example.org");
        final Person marlene = personService.create("person2", "Musterfrau", "Marlene", "musterfrau@example.org");
        final VacationTypeEntity overtime = getVacationType(OVERTIME);

        final LocalDate askedStartDate = LocalDate.now(UTC).with(firstDayOfMonth());
        final LocalDate askedEndDate = LocalDate.now(UTC).with(lastDayOfMonth());

        // application for leave that should not be found
        final Application appWrongVacationType = createApplication(marlene, getVacationType(HOLIDAY), askedStartDate.plusDays(1), askedEndDate.minusDays(1), FULL);
        sut.save(appWrongVacationType);

        final Application appWrongPerson = createApplication(max, overtime, askedStartDate.plusDays(1), askedEndDate.minusDays(1), FULL);
        sut.save(appWrongPerson);

        final Application appWrongStatus = createApplication(marlene, overtime, askedStartDate.plusDays(1), askedEndDate.plusDays(1), FULL);
        appWrongStatus.setStatus(CANCELLED);
        sut.save(appWrongStatus);


        // application for leave that should be found
        final Application appStartBeforeAsked = createApplication(marlene, overtime, askedStartDate.minusDays(1), askedEndDate.minusDays(1), FULL);
        final Application appEndAfterAsked = createApplication(marlene, overtime, askedStartDate.plusDays(1), askedEndDate.plusDays(1), FULL);
        final Application appInBetween = createApplication(marlene, overtime, askedStartDate.plusDays(10), askedStartDate.plusDays(12), FULL);
        final Application appStartingAtPeriod = createApplication(marlene, overtime, askedStartDate, askedStartDate.plusDays(2), FULL);
        final Application appEndingAtPeriod = createApplication(marlene, overtime, askedEndDate.minusDays(5), askedEndDate, FULL);

        sut.save(appStartBeforeAsked);
        sut.save(appEndAfterAsked);
        sut.save(appInBetween);
        sut.save(appStartingAtPeriod);
        sut.save(appEndingAtPeriod);

        final List<ApplicationStatus> statuses = List.of(WAITING);

        final List<Application> actualApplications = sut.findByStatusInAndPersonAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqualAndVacationTypeCategory(statuses, marlene, askedStartDate, askedEndDate, OVERTIME);
        assertThat(actualApplications).containsOnly(appStartBeforeAsked, appEndAfterAsked, appInBetween, appStartingAtPeriod, appEndingAtPeriod);
    }

    @Test
    void ensureFindByPersonInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual() {

        final Person max = personService.create("muster", "Mustermann", "Max", "mustermann@example.org");
        final Person marlene = personService.create("person2", "Musterfrau", "Marlene", "musterfrau@example.org");

        final LocalDate askedStartDate = LocalDate.now(UTC).with(firstDayOfMonth());
        final LocalDate askedEndDate = LocalDate.now(UTC).with(lastDayOfMonth());

        final Application validHolidayStartingBefore = createApplication(marlene, getVacationType(HOLIDAY), askedStartDate.minusDays(1), askedStartDate.plusDays(1), FULL);
        sut.save(validHolidayStartingBefore);

        final Application validOvertimeEndingAfter = createApplication(marlene, getVacationType(OVERTIME), askedEndDate.minusDays(1), askedEndDate.plusDays(1), FULL);
        sut.save(validOvertimeEndingAfter);

        final Application validSpecialLeaveInBetween = createApplication(marlene, getVacationType(SPECIALLEAVE), askedStartDate.plusDays(2), askedEndDate.minusDays(2), FULL);
        sut.save(validSpecialLeaveInBetween);

        final Application invalidApplication = createApplication(max, getVacationType(HOLIDAY), askedStartDate.plusDays(2), askedEndDate.minusDays(2), FULL);
        sut.save(invalidApplication);

        final List<Application> actual = sut.findByPersonInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqualAndStatusIn(List.of(marlene), askedStartDate, askedEndDate, List.of(WAITING));

        assertThat(actual).containsExactlyInAnyOrder(validHolidayStartingBefore, validOvertimeEndingAfter, validSpecialLeaveInBetween);
    }

    @Test
    void findAllByReplacements_Person() {

        final Person person = personService.create("muster", "Mustermann", "Max", "mustermann@example.org");

        Application application = new Application();
        final HolidayReplacementEntity holidayReplacement = new HolidayReplacementEntity();
        holidayReplacement.setPerson(person);
        application.setHolidayReplacements(List.of(holidayReplacement));
        sut.save(application);

        final List<Application> applicationsByHolidayReplacement = sut.findAllByHolidayReplacements_Person(person);
        assertThat(applicationsByHolidayReplacement).contains(application);
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
