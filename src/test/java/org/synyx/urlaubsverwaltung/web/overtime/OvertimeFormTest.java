package org.synyx.urlaubsverwaltung.web.overtime;

import org.joda.time.DateMidnight;

import org.junit.Assert;
import org.junit.Test;

import org.synyx.urlaubsverwaltung.core.overtime.Overtime;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.math.BigDecimal;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class OvertimeFormTest {

    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfInitializedWithNull() {

        new OvertimeForm(null);
    }


    @Test
    public void ensureCanBeInitializedWithPerson() {

        Person person = TestDataCreator.createPerson();

        OvertimeForm overtimeForm = new OvertimeForm(person);

        Assert.assertNotNull("Person should be set", overtimeForm.getPerson());
        Assert.assertEquals("Wrong person", person, overtimeForm.getPerson());
    }


    @Test
    public void ensureCanConstructAnOvertimeObject() {

        Person person = TestDataCreator.createPerson();

        OvertimeForm overtimeForm = new OvertimeForm(person);
        overtimeForm.setStartDate(DateMidnight.now());
        overtimeForm.setEndDate(DateMidnight.now().plusDays(1));
        overtimeForm.setNumberOfHours(BigDecimal.ONE);
        overtimeForm.setComment("Lorem ipsum");

        Overtime overtime = overtimeForm.generateOvertime();

        Assert.assertNotNull("Object should have been constructed successfully", overtime);

        Assert.assertEquals("Wrong person", overtimeForm.getPerson(), overtime.getPerson());
        Assert.assertEquals("Wrong start date", overtimeForm.getStartDate(), overtime.getStartDate());
        Assert.assertEquals("Wrong end date", overtimeForm.getEndDate(), overtime.getEndDate());
        Assert.assertEquals("Wrong number of hours", overtimeForm.getNumberOfHours(), overtime.getNumberOfHours());
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfGeneratingOvertimeWithoutCheckingFormAttributes() {

        Person person = TestDataCreator.createPerson();

        new OvertimeForm(person).generateOvertime();
    }
}
