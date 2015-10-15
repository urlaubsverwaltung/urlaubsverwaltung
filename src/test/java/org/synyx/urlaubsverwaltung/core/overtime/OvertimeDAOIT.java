package org.synyx.urlaubsverwaltung.core.overtime;

import org.joda.time.DateMidnight;

import org.junit.Assert;
import org.junit.Test;

import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.springframework.transaction.annotation.Transactional;

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
public class OvertimeDAOIT {

    @Autowired
    private PersonDAO personDAO;

    @Autowired
    private OvertimeDAO overtimeDAO;

    @Test
    @Rollback
    public void ensureCanPersistOvertime() {

        Person person = TestDataCreator.createPerson();
        personDAO.save(person);

        DateMidnight now = DateMidnight.now();
        Overtime overtime = new Overtime(person, now, now.plusDays(2), BigDecimal.ONE);

        Assert.assertNull("Must not have ID", overtime.getId());

        overtimeDAO.save(overtime);

        Assert.assertNotNull("Missing ID", overtime.getId());
    }


    @Test
    @Rollback
    public void ensureCountsTotalHoursCorrectly() {

        Person person = TestDataCreator.createPerson();
        personDAO.save(person);

        Person otherPerson = TestDataCreator.createPerson();
        personDAO.save(otherPerson);

        DateMidnight now = DateMidnight.now();

        // Overtime for person
        overtimeDAO.save(new Overtime(person, now, now.plusDays(2), new BigDecimal("2")));
        overtimeDAO.save(new Overtime(person, now.plusDays(5), now.plusDays(10), new BigDecimal("0.5")));

        // Overtime for other person
        overtimeDAO.save(new Overtime(otherPerson, now.plusDays(5), now.plusDays(10), new BigDecimal("5")));

        BigDecimal totalHours = overtimeDAO.calculateTotalHoursForPerson(person);

        Assert.assertNotNull("Should not be null", totalHours);
        Assert.assertEquals("Total hours calculated wrongly", new BigDecimal("2.5").setScale(1,
                BigDecimal.ROUND_UNNECESSARY), totalHours.setScale(1, BigDecimal.ROUND_UNNECESSARY));
    }


    @Test
    @Rollback
    public void ensureReturnsNullAsTotalOvertimeIfPersonHasNoOvertimeRecords() {

        Person person = TestDataCreator.createPerson();
        personDAO.save(person);

        BigDecimal totalHours = overtimeDAO.calculateTotalHoursForPerson(person);

        Assert.assertNull("Should be null", totalHours);
    }
}
