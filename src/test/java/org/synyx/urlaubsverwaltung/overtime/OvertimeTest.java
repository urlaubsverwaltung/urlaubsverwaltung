package org.synyx.urlaubsverwaltung.overtime;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_USER;
import static org.synyx.urlaubsverwaltung.person.Role.USER;


public class OvertimeTest {

    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsOnNullPerson() {

        Instant now = Instant.now();

        new Overtime(null, now, now, BigDecimal.ONE);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsOnNullStartDate() {

        Person person = TestDataCreator.createPerson();
        Instant now = Instant.now();

        new Overtime(person, null, now, BigDecimal.ONE);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsOnNullEndDate() {

        Person person = TestDataCreator.createPerson();
        Instant now = Instant.now();

        new Overtime(person, now, null, BigDecimal.ONE);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsOnNullNumberOfHours() {

        Person person = TestDataCreator.createPerson();
        Instant now = Instant.now();

        new Overtime(person, now, now, null);
    }


    @Test
    public void ensureReturnsCorrectStartDate() {

        Person person = TestDataCreator.createPerson();
        Instant now = Instant.now();

        Overtime overtime = new Overtime(person, now, now.plus(2, DAYS), BigDecimal.ONE);

        Assert.assertNotNull("Should not be null", overtime.getStartDate());
        Assert.assertEquals("Wrong start date", now, overtime.getStartDate());
    }


    @Test
    public void ensureReturnsCorrectEndDate() {

        Person person = TestDataCreator.createPerson();
        Instant now = Instant.now();

        Overtime overtime = new Overtime(person, now.minus(2, DAYS), now, BigDecimal.ONE);

        Assert.assertNotNull("Should not be null", overtime.getEndDate());
        Assert.assertEquals("Wrong end date", now, overtime.getEndDate());
    }


    @Test
    public void ensureSetLastModificationDateOnInitialization() {

        Person person = TestDataCreator.createPerson();
        Instant now = Instant.now();

        Overtime overtime = new Overtime(person, now.minus(2, DAYS), now.plus(4, DAYS), BigDecimal.ONE);

        Assert.assertNotNull("Should not be null", overtime.getLastModificationDate());
        Assert.assertEquals("Wrong last modification date", now, overtime.getLastModificationDate());
    }


    @Test(expected = IllegalStateException.class)
    public void ensureThrowsIfGettingStartDateOnACorruptedOvertime() throws IllegalAccessException {

        Person person = TestDataCreator.createPerson();
        Instant now = Instant.now();

        Overtime overtime = new Overtime(person, now.minus(2, DAYS), now, BigDecimal.ONE);

        Field startDateField = ReflectionUtils.findField(Overtime.class, "startDate");
        startDateField.setAccessible(true);
        startDateField.set(overtime, null);

        overtime.getStartDate();
    }


    @Test(expected = IllegalStateException.class)
    public void ensureThrowsIfGettingEndDateOnACorruptedOvertime() throws IllegalAccessException {

        Person person = TestDataCreator.createPerson();
        Instant now = Instant.now();

        Overtime overtime = new Overtime(person, now.minus(2, DAYS), now, BigDecimal.ONE);

        Field endDateField = ReflectionUtils.findField(Overtime.class, "endDate");
        endDateField.setAccessible(true);
        endDateField.set(overtime, null);

        overtime.getEndDate();
    }


    @Test(expected = IllegalStateException.class)
    public void ensureThrowsIfGettingLastModificationDateOnACorruptedOvertime() throws IllegalAccessException {

        Person person = TestDataCreator.createPerson();
        Instant now = Instant.now();

        Overtime overtime = new Overtime(person, now.minus(2, DAYS), now, BigDecimal.ONE);

        Field lastModificationDateField = ReflectionUtils.findField(Overtime.class, "lastModificationDate");
        lastModificationDateField.setAccessible(true);
        lastModificationDateField.set(overtime, null);

        overtime.getLastModificationDate();
    }


    @Test
    public void ensureCallingOnUpdateChangesLastModificationDate() throws IllegalAccessException {

        Person person = TestDataCreator.createPerson();
        Instant now = Instant.now();

        Overtime overtime = new Overtime(person, now.minus(2, DAYS), now, BigDecimal.ONE);

        // Simulate that the overtime record has been created to an earlier time
        Field lastModificationDateField = ReflectionUtils.findField(Overtime.class, "lastModificationDate");
        lastModificationDateField.setAccessible(true);
        lastModificationDateField.set(overtime, now.minus(3, DAYS));

        Assert.assertEquals("Wrong initial last modification date", now.minus(3, DAYS),
            overtime.getLastModificationDate());

        overtime.onUpdate();

        Assert.assertEquals("Last modification date should be set to now", now, overtime.getLastModificationDate());
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTryingToSetStartDateToNull() {

        new Overtime().setStartDate(null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTryingToSetEndDateToNull() {

        new Overtime().setEndDate(null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTryingToSetHoursToNull() {

        new Overtime().setHours(null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTryingToSetPersonToNull() {

        new Overtime().setPerson(null);
    }

    @Test
    public void toStringTest() {
        final Person person = new Person("Theo", "Theo", "Theo", "Theo");
        person.setId(10);
        person.setPassword("Theo");
        person.setPermissions(List.of(USER));
        person.setNotifications(List.of(NOTIFICATION_USER));
        final Overtime overtime = new Overtime(person, Instant.MIN, Instant.MAX, BigDecimal.TEN);
        overtime.setId(1);

        final String overtimeToString = overtime.toString();
        assertThat(overtimeToString).isEqualTo("Overtime{id=1, startDate=-999999999-01-01, endDate=+999999999-12-31, hours=10, person=Person{id='10'}}");
    }
}
