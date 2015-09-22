package org.synyx.urlaubsverwaltung.web.sicknote;

import org.junit.Assert;

import org.joda.time.DateMidnight;

import org.junit.Test;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
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


    @Test
    public void ensureGeneratesCorrectApplicationForLeave() {

        Person person = new Person();
        DateMidnight startDate = new DateMidnight(2014, 1, 1);
        DateMidnight endDate = new DateMidnight(2014, 1, 10);

        SickNote sickNote = new SickNote();
        sickNote.setPerson(person);
        sickNote.setStartDate(startDate);
        sickNote.setEndDate(endDate);

        String reason = "Foo";
        VacationType vacationType = VacationType.UNPAIDLEAVE;

        SickNoteConvertForm convertForm = new SickNoteConvertForm(sickNote);

        convertForm.setReason(reason);
        convertForm.setVacationType(vacationType);

        Application applicationForLeave = convertForm.generateApplicationForLeave();

        Assert.assertNotNull("Should not be null", applicationForLeave.getPerson());
        Assert.assertNotNull("Should not be null", applicationForLeave.getStartDate());
        Assert.assertNotNull("Should not be null", applicationForLeave.getEndDate());
        Assert.assertNotNull("Should not be null", applicationForLeave.getVacationType());
        Assert.assertNotNull("Should not be null", applicationForLeave.getDayLength());
        Assert.assertNotNull("Should not be null", applicationForLeave.getApplicationDate());
        Assert.assertNotNull("Should not be null", applicationForLeave.getEditedDate());

        Assert.assertEquals("Wrong person", person, applicationForLeave.getPerson());
        Assert.assertEquals("Wrong start date", startDate, applicationForLeave.getStartDate());
        Assert.assertEquals("Wrong end date", endDate, applicationForLeave.getEndDate());
        Assert.assertEquals("Wrong day length", DayLength.FULL, applicationForLeave.getDayLength());
        Assert.assertEquals("Wrong vacation type", vacationType, applicationForLeave.getVacationType());
        Assert.assertEquals("Wrong status", ApplicationStatus.ALLOWED, applicationForLeave.getStatus());
    }
}
