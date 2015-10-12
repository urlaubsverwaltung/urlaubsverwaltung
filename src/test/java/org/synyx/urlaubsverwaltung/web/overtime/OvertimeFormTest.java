package org.synyx.urlaubsverwaltung.web.overtime;

import org.junit.Assert;
import org.junit.Test;

import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;


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
}
