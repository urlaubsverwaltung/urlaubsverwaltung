package org.synyx.urlaubsverwaltung.web.sicknote;

import org.joda.time.DateMidnight;

import org.junit.Assert;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.calendar.WorkDaysService;
import org.synyx.urlaubsverwaltung.core.period.DayLength;
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

        DayLength dayLength = DayLength.MORNING;
        DateMidnight startDate = new DateMidnight(2015, 3, 3);
        DateMidnight endDate = new DateMidnight(2015, 3, 6);

        SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setTypeName(SickNoteType.SICK_NOTE);
        sickNoteType.setTypeDisplayName("Krankmeldung");

        SickNote sickNote = new SickNote();
        sickNote.setDayLength(dayLength);
        sickNote.setStartDate(startDate);
        sickNote.setEndDate(endDate);
        sickNote.setSickNoteType(sickNoteType);

        Mockito.when(calendarService.getWorkDays(Mockito.eq(dayLength), Mockito.eq(startDate), Mockito.eq(endDate),
                    Mockito.any(Person.class)))
            .thenReturn(BigDecimal.TEN);

        ExtendedSickNote extendedSickNote = new ExtendedSickNote(sickNote, calendarService);

        Assert.assertNotNull("Should not be null", extendedSickNote.getDayLength());
        Assert.assertNotNull("Should not be null", extendedSickNote.getStartDate());
        Assert.assertNotNull("Should not be null", extendedSickNote.getEndDate());
        Assert.assertNotNull("Should not be null", extendedSickNote.getSickNoteType());

        Assert.assertEquals("Wrong day length", dayLength, extendedSickNote.getDayLength());
        Assert.assertEquals("Wrong start date", startDate, extendedSickNote.getStartDate());
        Assert.assertEquals("Wrong end date", endDate, extendedSickNote.getEndDate());
        Assert.assertEquals("Wrong type", sickNoteType, extendedSickNote.getSickNoteType());

        Assert.assertNotNull("Should not be null", extendedSickNote.getWorkDays());
        Assert.assertEquals("Wrong number of work days", BigDecimal.TEN, extendedSickNote.getWorkDays());
    }
}
