package org.synyx.urlaubsverwaltung.mail;

import org.junit.Assert;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;

import java.util.List;


public class RecipientUtilTest {

    @Test
    public void ensureFiltersOutPersonsWithoutMailAddress() {

        Person person = TestDataCreator.createPerson("muster", "Max", "Mustermann", "max@firma.test");
        Person anotherPerson = TestDataCreator.createPerson("mmuster", "Marlene", "Muster", "marlene@firma.test");
        Person personWithoutMailAddress = TestDataCreator.createPerson("nomail", "No", "Mail", null);

        List<String> recipients = RecipientUtil.getMailAddresses(person, anotherPerson, personWithoutMailAddress);

        Assert.assertEquals("Wrong number of recipients", 2, recipients.size());
        Assert.assertTrue("Missing recipient", recipients.contains("max@firma.test"));
        Assert.assertTrue("Missing recipient", recipients.contains("marlene@firma.test"));
    }
}
