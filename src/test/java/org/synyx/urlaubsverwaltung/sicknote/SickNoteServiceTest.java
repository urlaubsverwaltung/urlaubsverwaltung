package org.synyx.urlaubsverwaltung.sicknote;

import junit.framework.Assert;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.application.service.CommentService;
import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentDAO;

import java.math.BigDecimal;


/**
 * Unit test for {@link SickNoteService}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class SickNoteServiceTest {

    private SickNoteService service;
    private SickNoteDAO sickNoteDAO;
    private SickNoteCommentDAO commentDAO;
    private OwnCalendarService calendarService;
    private ApplicationService applicationService;
    private CommentService commentService;

    private SickNote sickNote;
    private Application application;

    @Before
    public void setup() {

        sickNoteDAO = Mockito.mock(SickNoteDAO.class);
        commentDAO = Mockito.mock(SickNoteCommentDAO.class);
        calendarService = Mockito.mock(OwnCalendarService.class);
        applicationService = Mockito.mock(ApplicationService.class);
        commentService = Mockito.mock(CommentService.class);

        service = new SickNoteService(sickNoteDAO, commentDAO, calendarService, applicationService, commentService);

        sickNote = new SickNote();
        application = new Application();
    }


    @Test
    public void testAdjustSickNoteIdenticalEnd() {

        sickNote.setStartDate(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 18));
        sickNote.setEndDate(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 22));

        application.setStartDate(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 20));
        application.setEndDate(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 22));

        service.adjustSickNote(sickNote, application);

        Assert.assertEquals(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 18), sickNote.getStartDate());
        Assert.assertEquals(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 19), sickNote.getEndDate());
    }


    @Test
    public void testAdjustSickNoteIdenticalStart() {

        sickNote.setStartDate(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 18));
        sickNote.setEndDate(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 22));

        application.setStartDate(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 18));
        application.setEndDate(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 19));

        service.adjustSickNote(sickNote, application);

        Assert.assertEquals(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 20), sickNote.getStartDate());
        Assert.assertEquals(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 22), sickNote.getEndDate());
    }


    @Test
    public void testAdjustSickNoteIdenticalRange() {

        sickNote.setStartDate(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 18));
        sickNote.setEndDate(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 22));

        application.setStartDate(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 18));
        application.setEndDate(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 22));

        service.adjustSickNote(sickNote, application);

        Assert.assertEquals(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 18), sickNote.getStartDate());
        Assert.assertEquals(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 22), sickNote.getEndDate());
        Assert.assertEquals(BigDecimal.ZERO, sickNote.getWorkDays());
    }
}
