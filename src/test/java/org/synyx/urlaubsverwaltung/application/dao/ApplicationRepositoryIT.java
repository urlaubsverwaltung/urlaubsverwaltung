package org.synyx.urlaubsverwaltung.application.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.TestContainersBase;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static java.math.BigDecimal.ONE;
import static java.math.RoundingMode.UNNECESSARY;
import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createApplication;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.CANCELLED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.REJECTED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.REVOKED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.SPECIALLEAVE;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.UNPAIDLEAVE;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;


@SpringBootTest
@Transactional
class ApplicationRepositoryIT extends TestContainersBase {

    @Autowired
    private PersonService personService;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private VacationTypeRepository vacationTypeRepository;

    @Test
    void ensureReturnsNullAsTotalOvertimeReductionIfPersonHasNoApplicationsForLeaveYet() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person savedPerson = personService.save(person);

        BigDecimal totalHours = applicationRepository.calculateTotalOvertimeOfPerson(savedPerson);
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
        applicationRepository.save(fullDayOvertimeReduction);

        Application fullDayHoliday = createApplication(savedPerson, getVacationType(HOLIDAY), now, now.plusDays(2), FULL);
        fullDayHoliday.setStatus(ALLOWED);
        applicationRepository.save(fullDayHoliday);

        // Waiting
        final Application halfDayOvertimeReduction = createApplication(savedPerson, getVacationType(SPECIALLEAVE), now.plusDays(5), now.plusDays(10), MORNING);
        halfDayOvertimeReduction.setStatus(WAITING);
        applicationRepository.save(halfDayOvertimeReduction);

        // Cancelled
        final Application cancelledOvertimeReduction = createApplication(savedPerson, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        cancelledOvertimeReduction.setStatus(CANCELLED);
        applicationRepository.save(cancelledOvertimeReduction);

        // Rejected
        final Application rejectedOvertimeReduction = createApplication(savedPerson, getVacationType(SPECIALLEAVE), now, now.plusDays(2), FULL);
        rejectedOvertimeReduction.setStatus(REJECTED);
        applicationRepository.save(rejectedOvertimeReduction);

        // Revoked
        final Application revokedOvertimeReduction = createApplication(savedPerson, getVacationType(UNPAIDLEAVE), now, now.plusDays(2), FULL);
        revokedOvertimeReduction.setStatus(REVOKED);
        applicationRepository.save(revokedOvertimeReduction);

        final List<Application> allowedApplications = applicationRepository.findByStatusInAndEndDateGreaterThanEqual(List.of(ALLOWED), LocalDate.of(2020, 10, 3));
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
        applicationRepository.save(fullDayOvertimeReduction);

        Application fullDayHoliday = createApplication(savedPerson, getVacationType(HOLIDAY), now, now.plusDays(2), FULL);
        fullDayHoliday.setStatus(ALLOWED);
        applicationRepository.save(fullDayHoliday);

        // Waiting
        final Application halfDayOvertimeReduction = createApplication(savedPerson, getVacationType(SPECIALLEAVE), now.plusDays(5), now.plusDays(10), MORNING);
        halfDayOvertimeReduction.setStatus(WAITING);
        applicationRepository.save(halfDayOvertimeReduction);

        // Cancelled
        final Application cancelledOvertimeReduction = createApplication(savedPerson, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        cancelledOvertimeReduction.setStatus(CANCELLED);
        applicationRepository.save(cancelledOvertimeReduction);

        // Rejected
        final Application rejectedOvertimeReduction = createApplication(savedPerson, getVacationType(SPECIALLEAVE), now, now.plusDays(2), FULL);
        rejectedOvertimeReduction.setStatus(REJECTED);
        applicationRepository.save(rejectedOvertimeReduction);

        // Revoked
        final Application revokedOvertimeReduction = createApplication(savedPerson, getVacationType(UNPAIDLEAVE), now, now.plusDays(2), FULL);
        revokedOvertimeReduction.setStatus(REVOKED);
        applicationRepository.save(revokedOvertimeReduction);

        final List<Application> allowedApplications = applicationRepository.findByStatusInAndEndDateGreaterThanEqual(List.of(ALLOWED, REJECTED), LocalDate.of(2020, 10, 3));
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
        applicationRepository.save(revokedOvertimeReduction);

        final List<Application> allowedApplications = applicationRepository.findByStatusInAndEndDateGreaterThanEqual(List.of(), LocalDate.of(2020, 10, 3));
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
        applicationRepository.save(fullDayOvertimeReduction);

        // Waiting
        final Application halfDayOvertimeReduction = createApplication(savedPerson, getVacationType(SPECIALLEAVE), now.plusDays(5), now.plusDays(10), MORNING);
        halfDayOvertimeReduction.setStatus(WAITING);
        applicationRepository.save(halfDayOvertimeReduction);

        // Cancelled
        final Application cancelledOvertimeReduction = createApplication(savedPerson, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        cancelledOvertimeReduction.setStatus(CANCELLED);
        applicationRepository.save(cancelledOvertimeReduction);

        // Rejected
        Application rejectedHoliday = createApplication(savedPerson, getVacationType(HOLIDAY), now, now.plusDays(2), FULL);
        rejectedHoliday.setStatus(REJECTED);
        applicationRepository.save(rejectedHoliday);

        final Application rejectedOvertimeReduction = createApplication(savedOtherPerson, getVacationType(SPECIALLEAVE), now, now.plusDays(2), FULL);
        rejectedOvertimeReduction.setStatus(REJECTED);
        applicationRepository.save(rejectedOvertimeReduction);

        // Revoked
        final Application revokedOvertimeReduction = createApplication(savedOtherPerson, getVacationType(UNPAIDLEAVE), now, now.plusDays(2), FULL);
        revokedOvertimeReduction.setStatus(REVOKED);
        applicationRepository.save(revokedOvertimeReduction);

        final List<Application> allowedApplications = applicationRepository.findByStatusInAndPersonIn(List.of(ALLOWED, REJECTED), List.of(savedPerson, savedOtherPerson));
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
        applicationRepository.save(fullDayOvertimeReduction);

        // Waiting
        final Application halfDayOvertimeReduction = createApplication(savedPerson, getVacationType(SPECIALLEAVE), now.plusDays(5), now.plusDays(10), MORNING);
        halfDayOvertimeReduction.setStatus(WAITING);
        applicationRepository.save(halfDayOvertimeReduction);

        // Cancelled
        final Application cancelledOvertimeReduction = createApplication(savedPerson, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        cancelledOvertimeReduction.setStatus(CANCELLED);
        applicationRepository.save(cancelledOvertimeReduction);

        // Rejected
        Application rejectedHoliday = createApplication(savedPerson, getVacationType(HOLIDAY), now, now.plusDays(2), FULL);
        rejectedHoliday.setStatus(REJECTED);
        applicationRepository.save(rejectedHoliday);

        final Application rejectedOvertimeReduction = createApplication(savedOtherPerson, getVacationType(SPECIALLEAVE), now, now.plusDays(2), FULL);
        rejectedOvertimeReduction.setStatus(REJECTED);
        applicationRepository.save(rejectedOvertimeReduction);

        // Revoked
        final Application revokedOvertimeReduction = createApplication(savedOtherPerson, getVacationType(UNPAIDLEAVE), now, now.plusDays(2), FULL);
        revokedOvertimeReduction.setStatus(REVOKED);
        applicationRepository.save(revokedOvertimeReduction);

        final List<Application> allowedApplications = applicationRepository.findByStatusInAndPersonIn(List.of(ALLOWED, REJECTED), List.of(savedPerson));
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
        applicationRepository.save(fullDayOvertimeReduction);

        // Waiting
        final Application halfDayOvertimeReduction = createApplication(savedPerson, getVacationType(SPECIALLEAVE), now.plusDays(5), now.plusDays(10), MORNING);
        halfDayOvertimeReduction.setStatus(WAITING);
        applicationRepository.save(halfDayOvertimeReduction);

        // Cancelled
        final Application cancelledOvertimeReduction = createApplication(savedPerson, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        cancelledOvertimeReduction.setStatus(CANCELLED);
        applicationRepository.save(cancelledOvertimeReduction);

        // Rejected
        Application rejectedHoliday = createApplication(savedPerson, getVacationType(HOLIDAY), now, now.plusDays(2), FULL);
        rejectedHoliday.setStatus(REJECTED);
        applicationRepository.save(rejectedHoliday);

        final Application rejectedOvertimeReduction = createApplication(savedOtherPerson, getVacationType(SPECIALLEAVE), now, now.plusDays(2), FULL);
        rejectedOvertimeReduction.setStatus(REJECTED);
        applicationRepository.save(rejectedOvertimeReduction);

        final List<Application> allowedApplications = applicationRepository.findByStatusInAndPersonIn(List.of(REVOKED), List.of(savedPerson, savedOtherPerson));
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
        fullDayOvertimeReduction.setHours(new BigDecimal("8"));
        fullDayOvertimeReduction.setStatus(ALLOWED);
        applicationRepository.save(fullDayOvertimeReduction);

        // Waiting overtime reduction (2.5 hours) ----------------------------------------------------------------------
        final Application halfDayOvertimeReduction = createApplication(savedPerson, getVacationType(OVERTIME), now.plusDays(5), now.plusDays(10), MORNING);
        halfDayOvertimeReduction.setHours(new BigDecimal("2.5"));
        halfDayOvertimeReduction.setStatus(WAITING);
        applicationRepository.save(halfDayOvertimeReduction);

        // Cancelled overtime reduction (1 hour) ----------------------------------------------------------------------
        final Application cancelledOvertimeReduction = createApplication(savedPerson, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        cancelledOvertimeReduction.setHours(ONE);
        cancelledOvertimeReduction.setStatus(CANCELLED);
        applicationRepository.save(cancelledOvertimeReduction);

        // Rejected overtime reduction (1 hour) -----------------------------------------------------------------------
        final Application rejectedOvertimeReduction = createApplication(savedPerson, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        rejectedOvertimeReduction.setHours(ONE);
        rejectedOvertimeReduction.setStatus(REJECTED);
        applicationRepository.save(rejectedOvertimeReduction);

        // Revoked overtime reduction (1 hour) ------------------------------------------------------------------------
        final Application revokedOvertimeReduction = createApplication(savedPerson, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        revokedOvertimeReduction.setHours(ONE);
        revokedOvertimeReduction.setStatus(REVOKED);
        applicationRepository.save(revokedOvertimeReduction);

        // Holiday with hours set accidentally (1 hour) ---------------------------------------------------------------
        final Application holiday = createApplication(savedPerson, getVacationType(HOLIDAY), now.minusDays(8), now.minusDays(4), FULL);

        // NOTE: Holiday should not have hours set, but who knows....
        // More than once heard: "this should never happen" ;)
        holiday.setHours(ONE);
        applicationRepository.save(holiday);

        // Overtime reduction for other person -------------------------------------------------------------------------
        final Application overtimeReduction = createApplication(savedOtherPerson, getVacationType(OVERTIME), now.plusDays(5), now.plusDays(10), NOON);
        overtimeReduction.setHours(new BigDecimal("2.5"));
        applicationRepository.save(overtimeReduction);

        // Let's calculate! --------------------------------------------------------------------------------------------

        BigDecimal totalHours = applicationRepository.calculateTotalOvertimeOfPerson(person);
        assertThat(totalHours).isEqualTo(BigDecimal.valueOf(10.50).setScale(2, UNNECESSARY));
    }


    private VacationType getVacationType(VacationCategory category) {

        List<VacationType> vacationTypes = vacationTypeRepository.findAll();

        for (VacationType vacationType : vacationTypes) {
            if (vacationType.isOfCategory(category)) {
                return vacationType;
            }
        }

        throw new IllegalStateException("No type with found with category: " + category.name());
    }
}
