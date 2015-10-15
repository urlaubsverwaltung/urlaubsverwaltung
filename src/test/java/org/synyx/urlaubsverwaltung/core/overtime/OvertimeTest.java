package org.synyx.urlaubsverwaltung.core.overtime;

import org.joda.time.DateMidnight;

import org.junit.Assert;
import org.junit.Test;

import org.springframework.util.ReflectionUtils;

import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.lang.reflect.Field;

import java.math.BigDecimal;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class OvertimeTest {

    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsOnNullPerson() {

        DateMidnight now = DateMidnight.now();

        new Overtime(null, now, now, BigDecimal.ONE);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsOnNullStartDate() {

        Person person = TestDataCreator.createPerson();
        DateMidnight now = DateMidnight.now();

        new Overtime(person, null, now, BigDecimal.ONE);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsOnNullEndDate() {

        Person person = TestDataCreator.createPerson();
        DateMidnight now = DateMidnight.now();

        new Overtime(person, now, null, BigDecimal.ONE);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsOnNullNumberOfHours() {

        Person person = TestDataCreator.createPerson();
        DateMidnight now = DateMidnight.now();

        new Overtime(person, now, now, null);
    }


    @Test
    public void ensureReturnsCorrectStartDate() {

        Person person = TestDataCreator.createPerson();
        DateMidnight now = DateMidnight.now();

        Overtime overtime = new Overtime(person, now, now.plusDays(2), BigDecimal.ONE);

        Assert.assertNotNull("Should not be null", overtime.getStartDate());
        Assert.assertEquals("Wrong start date", now, overtime.getStartDate());
    }


    @Test
    public void ensureReturnsCorrectEndDate() {

        Person person = TestDataCreator.createPerson();
        DateMidnight now = DateMidnight.now();

        Overtime overtime = new Overtime(person, now.minusDays(2), now, BigDecimal.ONE);

        Assert.assertNotNull("Should not be null", overtime.getEndDate());
        Assert.assertEquals("Wrong end date", now, overtime.getEndDate());
    }


    @Test
    public void ensureSetLastModificationDateOnInitialization() {

        Person person = TestDataCreator.createPerson();
        DateMidnight now = DateMidnight.now();

        Overtime overtime = new Overtime(person, now.minusDays(2), now.plusDays(4), BigDecimal.ONE);

        Assert.assertNotNull("Should not be null", overtime.getLastModificationDate());
        Assert.assertEquals("Wrong last modification date", now, overtime.getLastModificationDate());
    }


    @Test(expected = IllegalStateException.class)
    public void ensureThrowsIfGettingStartDateOnACorruptedOvertime() throws IllegalAccessException {

        Person person = TestDataCreator.createPerson();
        DateMidnight now = DateMidnight.now();

        Overtime overtime = new Overtime(person, now.minusDays(2), now, BigDecimal.ONE);

        Field startDateField = ReflectionUtils.findField(Overtime.class, "startDate");
        startDateField.setAccessible(true);
        startDateField.set(overtime, null);

        overtime.getStartDate();
    }


    @Test(expected = IllegalStateException.class)
    public void ensureThrowsIfGettingEndDateOnACorruptedOvertime() throws IllegalAccessException {

        Person person = TestDataCreator.createPerson();
        DateMidnight now = DateMidnight.now();

        Overtime overtime = new Overtime(person, now.minusDays(2), now, BigDecimal.ONE);

        Field endDateField = ReflectionUtils.findField(Overtime.class, "endDate");
        endDateField.setAccessible(true);
        endDateField.set(overtime, null);

        overtime.getEndDate();
    }


    @Test(expected = IllegalStateException.class)
    public void ensureThrowsIfGettingLastModificationDateOnACorruptedOvertime() throws IllegalAccessException {

        Person person = TestDataCreator.createPerson();
        DateMidnight now = DateMidnight.now();

        Overtime overtime = new Overtime(person, now.minusDays(2), now, BigDecimal.ONE);

        Field endDateField = ReflectionUtils.findField(Overtime.class, "lastModificationDate");
        endDateField.setAccessible(true);
        endDateField.set(overtime, null);

        overtime.getLastModificationDate();
    }
}
