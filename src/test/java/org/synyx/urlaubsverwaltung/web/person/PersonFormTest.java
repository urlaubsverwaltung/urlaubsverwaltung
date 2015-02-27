package org.synyx.urlaubsverwaltung.web.person;

import org.joda.time.DateMidnight;

import org.junit.Assert;
import org.junit.Test;


/**
 * Unit test for.{@link org.synyx.urlaubsverwaltung.web.person.PersonForm}
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class PersonFormTest {

    @Test
    public void ensureHasDefaultValuesForHolidaysAccountPeriod() {

        PersonForm personForm = new PersonForm(2014);

        Assert.assertNotNull("Valid from date for holidays account must not be null",
            personForm.getHolidaysAccountValidFrom());
        Assert.assertNotNull("Valid to date for holidays account must not be null",
            personForm.getHolidaysAccountValidTo());

        Assert.assertEquals("Wrong valid from date for holidays account", new DateMidnight(2014, 1, 1),
            personForm.getHolidaysAccountValidFrom());
        Assert.assertEquals("Wrong valid to date for holidays account", new DateMidnight(2014, 12, 31),
            personForm.getHolidaysAccountValidTo());
    }
}
