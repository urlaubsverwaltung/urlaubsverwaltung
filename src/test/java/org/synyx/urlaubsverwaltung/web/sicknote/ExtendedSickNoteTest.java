package org.synyx.urlaubsverwaltung.web.sicknote;

import junit.framework.Assert;

import org.joda.time.DateMidnight;

import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.calendar.WorkDaysService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteType;

import java.math.BigDecimal;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.web.sicknote.ExtendedSickNote}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class ExtendedSickNoteTest {

    @Test
    public void ensureCreatesCorrectExtendedSickNote() {

        WorkDaysService calendarService = Mockito.mock(WorkDaysService.class);

        DateMidnight startDate = new DateMidnight(2015, 3, 3);
        DateMidnight endDate = new DateMidnight(2015, 3, 6);

        SickNote sickNote = new SickNote();
        sickNote.setStartDate(startDate);
        sickNote.setEndDate(endDate);
        sickNote.setType(SickNoteType.SICK_NOTE_CHILD);

        Mockito.when(calendarService.getWorkDays(Mockito.eq(DayLength.FULL), Mockito.eq(startDate), Mockito.eq(endDate),
                Mockito.any(Person.class))).thenReturn(BigDecimal.TEN);

        ExtendedSickNote extendedSickNote = new ExtendedSickNote(sickNote, calendarService);

        Assert.assertNotNull("Should not be null", extendedSickNote.getStartDate());
        Assert.assertNotNull("Should not be null", extendedSickNote.getEndDate());
        Assert.assertNotNull("Should not be null", extendedSickNote.getType());

        Assert.assertEquals("Wrong start date", startDate, extendedSickNote.getStartDate());
        Assert.assertEquals("Wrong end date", endDate, extendedSickNote.getEndDate());
        Assert.assertEquals("Wrong day length", SickNoteType.SICK_NOTE_CHILD, extendedSickNote.getType());

        Assert.assertNotNull("Should not be null", extendedSickNote.getWorkDays());
        Assert.assertEquals("Wrong number of work days", BigDecimal.TEN, extendedSickNote.getWorkDays());
    }
}
