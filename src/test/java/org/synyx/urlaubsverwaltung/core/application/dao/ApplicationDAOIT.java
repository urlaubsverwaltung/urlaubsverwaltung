package org.synyx.urlaubsverwaltung.core.application.dao;

import org.joda.time.DateMidnight;

import org.junit.Assert;
import org.junit.Test;

import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.springframework.transaction.annotation.Transactional;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonDAO;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.math.BigDecimal;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:META-INF/applicationContext.xml")
@Transactional
public class ApplicationDAOIT {

    @Autowired
    private PersonDAO personDAO;

    @Autowired
    private ApplicationDAO applicationDAO;

    @Test
    @Rollback
    public void ensureReturnsNullAsTotalOvertimeReductionIfPersonHasNoApplicationsForLeaveYet() {

        Person person = TestDataCreator.createPerson();
        personDAO.save(person);

        BigDecimal totalHours = applicationDAO.calculateTotalOvertimeOfPerson(person);

        Assert.assertNull("Should be null", totalHours);
    }


    @Test
    @Rollback
    public void ensureCountsTotalOvertimeReductionCorrectly() {

        Person person = TestDataCreator.createPerson();
        personDAO.save(person);

        Person otherPerson = TestDataCreator.createPerson();
        personDAO.save(otherPerson);

        DateMidnight now = DateMidnight.now();

        // Allowed overtime reduction (8 hours) ------------------------------------------------------------------------
        Application fullDayOvertimeReduction = TestDataCreator.createApplication(person, VacationType.OVERTIME, now,
                now.plusDays(2), DayLength.FULL);
        fullDayOvertimeReduction.setHours(new BigDecimal("8"));
        fullDayOvertimeReduction.setStatus(ApplicationStatus.ALLOWED);
        applicationDAO.save(fullDayOvertimeReduction);

        // Waiting overtime reduction (2.5 hours) ----------------------------------------------------------------------
        Application halfDayOvertimeReduction = TestDataCreator.createApplication(person, VacationType.OVERTIME,
                now.plusDays(5), now.plusDays(10), DayLength.MORNING);
        halfDayOvertimeReduction.setHours(new BigDecimal("2.5"));
        halfDayOvertimeReduction.setStatus(ApplicationStatus.WAITING);
        applicationDAO.save(halfDayOvertimeReduction);

        // Cancelled overtime reduction (1 hour) ----------------------------------------------------------------------
        Application cancelledOvertimeReduction = TestDataCreator.createApplication(person, VacationType.OVERTIME, now,
                now.plusDays(2), DayLength.FULL);
        cancelledOvertimeReduction.setHours(BigDecimal.ONE);
        cancelledOvertimeReduction.setStatus(ApplicationStatus.CANCELLED);
        applicationDAO.save(cancelledOvertimeReduction);

        // Rejected overtime reduction (1 hour) -----------------------------------------------------------------------
        Application rejectedOvertimeReduction = TestDataCreator.createApplication(person, VacationType.OVERTIME, now,
                now.plusDays(2), DayLength.FULL);
        rejectedOvertimeReduction.setHours(BigDecimal.ONE);
        rejectedOvertimeReduction.setStatus(ApplicationStatus.REJECTED);
        applicationDAO.save(rejectedOvertimeReduction);

        // Revoked overtime reduction (1 hour) ------------------------------------------------------------------------
        Application revokedOvertimeReduction = TestDataCreator.createApplication(person, VacationType.OVERTIME, now,
                now.plusDays(2), DayLength.FULL);
        revokedOvertimeReduction.setHours(BigDecimal.ONE);
        revokedOvertimeReduction.setStatus(ApplicationStatus.REVOKED);
        applicationDAO.save(revokedOvertimeReduction);

        // Holiday with hours set accidentally (1 hour) ---------------------------------------------------------------
        Application holiday = TestDataCreator.createApplication(person, VacationType.HOLIDAY, now.minusDays(8),
                now.minusDays(4), DayLength.FULL);

        // NOTE: Holiday should not have hours set, but who knows....
        // More than once heard: "this should never happen" ;)
        holiday.setHours(BigDecimal.ONE);
        applicationDAO.save(holiday);

        // Overtime reduction for other person -------------------------------------------------------------------------
        Application overtimeReduction = TestDataCreator.createApplication(otherPerson, VacationType.OVERTIME,
                now.plusDays(5), now.plusDays(10), DayLength.NOON);
        overtimeReduction.setHours(new BigDecimal("2.5"));
        applicationDAO.save(overtimeReduction);

        // Let's calculate! --------------------------------------------------------------------------------------------

        BigDecimal totalHours = applicationDAO.calculateTotalOvertimeOfPerson(person);

        Assert.assertNotNull("Should not be null", totalHours);
        Assert.assertEquals("Total overtime reduction calculated wrongly", new BigDecimal("10.5").setScale(1,
                BigDecimal.ROUND_UNNECESSARY), totalHours.setScale(1, BigDecimal.ROUND_UNNECESSARY));
    }
}
