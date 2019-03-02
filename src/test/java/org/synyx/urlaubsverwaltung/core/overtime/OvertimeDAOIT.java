package org.synyx.urlaubsverwaltung.core.overtime;

import org.joda.time.DateMidnight;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonDAO;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.math.BigDecimal;

import java.util.List;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */

@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:META-INF/applicationContext.xml")
@Transactional
public class OvertimeDAOIT {

    @Autowired
    private PersonDAO personDAO;

    @Autowired
    private OvertimeDAO overtimeDAO;

    @Test
    @Rollback
    @Ignore("Currently disabled as integration tests broke during the migration to Spring Boot.")
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
    @Ignore("Currently disabled as integration tests broke during the migration to Spring Boot.")
    public void ensureCountsTotalHoursCorrectly() {

        Person person = TestDataCreator.createPerson();
        personDAO.save(person);

        Person otherPerson = TestDataCreator.createPerson();
        personDAO.save(otherPerson);

        DateMidnight now = DateMidnight.now();

        // Overtime for person
        overtimeDAO.save(new Overtime(person, now, now.plusDays(2), new BigDecimal("3")));
        overtimeDAO.save(new Overtime(person, now.plusDays(5), now.plusDays(10), new BigDecimal("0.5")));
        overtimeDAO.save(new Overtime(person, now.minusDays(8), now.minusDays(4), new BigDecimal("-1")));

        // Overtime for other person
        overtimeDAO.save(new Overtime(otherPerson, now.plusDays(5), now.plusDays(10), new BigDecimal("5")));

        BigDecimal totalHours = overtimeDAO.calculateTotalHoursForPerson(person);

        Assert.assertNotNull("Should not be null", totalHours);
        Assert.assertEquals("Total hours calculated wrongly", new BigDecimal("2.5").setScale(1,
                BigDecimal.ROUND_UNNECESSARY), totalHours.setScale(1, BigDecimal.ROUND_UNNECESSARY));
    }


    @Test
    @Rollback
    @Ignore("Currently disabled as integration tests broke during the migration to Spring Boot.")
    public void ensureReturnsNullAsTotalOvertimeIfPersonHasNoOvertimeRecords() {

        Person person = TestDataCreator.createPerson();
        personDAO.save(person);

        BigDecimal totalHours = overtimeDAO.calculateTotalHoursForPerson(person);

        Assert.assertNull("Should be null", totalHours);
    }


    @Test
    @Ignore("Currently disabled as integration tests broke during the migration to Spring Boot.")
    public void ensureReturnsAllRecordsWithStartOrEndDateInTheGivenYear() {

        Person person = TestDataCreator.createPerson();
        personDAO.save(person);

        // records for 2015
        overtimeDAO.save(new Overtime(person, new DateMidnight(2014, 12, 30), new DateMidnight(2015, 1, 3),
                new BigDecimal("1")));
        overtimeDAO.save(new Overtime(person, new DateMidnight(2015, 10, 5), new DateMidnight(2015, 10, 20),
                new BigDecimal("2")));
        overtimeDAO.save(new Overtime(person, new DateMidnight(2015, 12, 28), new DateMidnight(2016, 1, 6),
                new BigDecimal("3")));

        // record for 2014
        overtimeDAO.save(new Overtime(person, new DateMidnight(2014, 12, 5), new DateMidnight(2014, 12, 31),
                new BigDecimal("4")));

        List<Overtime> records = overtimeDAO.findByPersonAndPeriod(person, new DateMidnight(2015, 1, 1).toDate(),
                new DateMidnight(2015, 12, 31).toDate());

        Assert.assertNotNull("Should not be null", records);
        Assert.assertEquals("Wrong number of records", 3, records.size());
        Assert.assertEquals("Wrong record", new BigDecimal("1"), records.get(0).getHours());
        Assert.assertEquals("Wrong record", new BigDecimal("2"), records.get(1).getHours());
        Assert.assertEquals("Wrong record", new BigDecimal("3"), records.get(2).getHours());
    }
}
