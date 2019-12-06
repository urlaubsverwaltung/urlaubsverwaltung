package org.synyx.urlaubsverwaltung.application.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static java.math.BigDecimal.ONE;
import static java.math.RoundingMode.UNNECESSARY;
import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.CANCELLED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.REJECTED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.REVOKED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;
import static org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator.createApplication;


@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class ApplicationDAOIT {

    @Autowired
    private PersonService personService;
    @Autowired
    private ApplicationDAO applicationDAO;
    @Autowired
    private VacationTypeDAO vacationTypeDAO;

    @Test
    public void ensureReturnsNullAsTotalOvertimeReductionIfPersonHasNoApplicationsForLeaveYet() {

        final Person person = TestDataCreator.createPerson();
        final Person savedPerson = personService.save(person);

        BigDecimal totalHours = applicationDAO.calculateTotalOvertimeOfPerson(savedPerson);
        assertThat(totalHours).isNull();
    }


    @Test
    public void ensureCountsTotalOvertimeReductionCorrectly() {

        final Person person = TestDataCreator.createPerson("sam", "sam", "smith", "smith@test.de");
        final Person savedPerson = personService.save(person);

        final Person otherPerson = TestDataCreator.createPerson("freddy", "freddy", "Gwin", "gwin@test.de");
        final Person savedOtherPerson = personService.save(otherPerson);

        final LocalDate now = LocalDate.now(UTC);

        // Allowed overtime reduction (8 hours) ------------------------------------------------------------------------
        Application fullDayOvertimeReduction = createApplication(savedPerson, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        fullDayOvertimeReduction.setHours(new BigDecimal("8"));
        fullDayOvertimeReduction.setStatus(ALLOWED);
        applicationDAO.save(fullDayOvertimeReduction);

        // Waiting overtime reduction (2.5 hours) ----------------------------------------------------------------------
        final Application halfDayOvertimeReduction = createApplication(person, getVacationType(OVERTIME), now.plusDays(5), now.plusDays(10), MORNING);
        halfDayOvertimeReduction.setHours(new BigDecimal("2.5"));
        halfDayOvertimeReduction.setStatus(WAITING);
        applicationDAO.save(halfDayOvertimeReduction);

        // Cancelled overtime reduction (1 hour) ----------------------------------------------------------------------
        final Application cancelledOvertimeReduction = createApplication(person, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        cancelledOvertimeReduction.setHours(ONE);
        cancelledOvertimeReduction.setStatus(CANCELLED);
        applicationDAO.save(cancelledOvertimeReduction);

        // Rejected overtime reduction (1 hour) -----------------------------------------------------------------------
        final Application rejectedOvertimeReduction = createApplication(person, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        rejectedOvertimeReduction.setHours(ONE);
        rejectedOvertimeReduction.setStatus(REJECTED);
        applicationDAO.save(rejectedOvertimeReduction);

        // Revoked overtime reduction (1 hour) ------------------------------------------------------------------------
        final Application revokedOvertimeReduction = createApplication(person, getVacationType(OVERTIME), now, now.plusDays(2), FULL);
        revokedOvertimeReduction.setHours(ONE);
        revokedOvertimeReduction.setStatus(REVOKED);
        applicationDAO.save(revokedOvertimeReduction);

        // Holiday with hours set accidentally (1 hour) ---------------------------------------------------------------
        final Application holiday = createApplication(person, getVacationType(HOLIDAY), now.minusDays(8), now.minusDays(4), FULL);

        // NOTE: Holiday should not have hours set, but who knows....
        // More than once heard: "this should never happen" ;)
        holiday.setHours(ONE);
        applicationDAO.save(holiday);

        // Overtime reduction for other person -------------------------------------------------------------------------
        final Application overtimeReduction = createApplication(savedOtherPerson, getVacationType(OVERTIME), now.plusDays(5), now.plusDays(10), NOON);
        overtimeReduction.setHours(new BigDecimal("2.5"));
        applicationDAO.save(overtimeReduction);

        // Let's calculate! --------------------------------------------------------------------------------------------

        BigDecimal totalHours = applicationDAO.calculateTotalOvertimeOfPerson(person);
        assertThat(totalHours).isEqualTo(BigDecimal.valueOf(10.50).setScale(2, UNNECESSARY));
    }


    private VacationType getVacationType(VacationCategory category) {

        List<VacationType> vacationTypes = vacationTypeDAO.findAll();

        for (VacationType vacationType : vacationTypes) {
            if (vacationType.isOfCategory(category)) {
                return vacationType;
            }
        }

        throw new IllegalStateException("No type with found with category: " + category.name());
    }
}
