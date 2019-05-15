package org.synyx.urlaubsverwaltung.sicknote.web;

import org.junit.Assert;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;

import java.time.LocalDate;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.sicknote.web.SickNoteConvertForm}.
 */
public class SickNoteConvertFormTest {

    @Test
    public void ensureCorrectProvidedValuesFromSickNote() {

        Person person = TestDataCreator.createPerson();
        LocalDate startDate = LocalDate.of(2014, 1, 1);
        LocalDate endDate = LocalDate.of(2014, 1, 10);

        SickNote sickNote = TestDataCreator.createSickNote(person, startDate, endDate, DayLength.NOON);

        SickNoteConvertForm convertForm = new SickNoteConvertForm(sickNote);

        Assert.assertNotNull("Should not be null", convertForm.getPerson());
        Assert.assertNotNull("Should not be null", convertForm.getDayLength());
        Assert.assertNotNull("Should not be null", convertForm.getStartDate());
        Assert.assertNotNull("Should not be null", convertForm.getEndDate());

        Assert.assertEquals("Wrong person", person, convertForm.getPerson());
        Assert.assertEquals("Wrong day length", DayLength.NOON, convertForm.getDayLength());
        Assert.assertEquals("Wrong start date", startDate, convertForm.getStartDate());
        Assert.assertEquals("Wrong end date", endDate, convertForm.getEndDate());
    }


    @Test
    public void ensureGeneratesCorrectApplicationForLeave() {

        Person person = TestDataCreator.createPerson();
        LocalDate startDate = LocalDate.of(2014, 1, 1);
        LocalDate endDate = LocalDate.of(2014, 1, 10);

        SickNote sickNote = TestDataCreator.createSickNote(person, startDate, endDate, DayLength.FULL);

        String reason = "Foo";
        VacationType vacationType = TestDataCreator.createVacationType(VacationCategory.UNPAIDLEAVE);

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
