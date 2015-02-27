package org.synyx.urlaubsverwaltung.web.sicknote;

import junit.framework.Assert;

import org.joda.time.DateMidnight;

import org.junit.Test;

import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.web.sicknote.SickNoteConvertForm}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class SickNoteConvertFormTest {

    @Test
    public void ensureCorrectProvidedValuesFromSickNote() {

        Person person = new Person();
        DateMidnight startDate = new DateMidnight(2014, 1, 1);
        DateMidnight endDate = new DateMidnight(2014, 1, 10);

        SickNote sickNote = new SickNote();
        sickNote.setPerson(person);
        sickNote.setStartDate(startDate);
        sickNote.setEndDate(endDate);

        SickNoteConvertForm convertForm = new SickNoteConvertForm(sickNote);

        Assert.assertNotNull("Should not be null", convertForm.getPerson());
        Assert.assertNotNull("Should not be null", convertForm.getStartDate());
        Assert.assertNotNull("Should not be null", convertForm.getEndDate());

        Assert.assertEquals("Wrong person", person, convertForm.getPerson());
        Assert.assertEquals("Wrong start date", startDate, convertForm.getStartDate());
        Assert.assertEquals("Wrong end date", endDate, convertForm.getEndDate());
    }
}
