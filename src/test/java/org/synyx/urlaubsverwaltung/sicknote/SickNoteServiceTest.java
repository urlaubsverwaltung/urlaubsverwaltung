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
import org.synyx.urlaubsverwaltung.mail.MailService;
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
    private MailService mailService;

    private SickNote sickNote;
    private Application application;

    @Before
    public void setup() {

        sickNoteDAO = Mockito.mock(SickNoteDAO.class);
        commentDAO = Mockito.mock(SickNoteCommentDAO.class);
        calendarService = Mockito.mock(OwnCalendarService.class);
        applicationService = Mockito.mock(ApplicationService.class);
        commentService = Mockito.mock(CommentService.class);
        mailService = Mockito.mock(MailService.class);

        service = new SickNoteService(sickNoteDAO, commentDAO, calendarService, applicationService, commentService,
                mailService);

        sickNote = new SickNote();
        application = new Application();
    }


    @Test
    public void testAdjustSickNoteIdenticalRange() {

        sickNote.setStartDate(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 18));
        sickNote.setEndDate(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 22));

        service.setSickNoteInactive(sickNote);

        Assert.assertEquals(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 18), sickNote.getStartDate());
        Assert.assertEquals(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 22), sickNote.getEndDate());
        Assert.assertEquals(false, sickNote.isActive());
        Assert.assertEquals(BigDecimal.ZERO, sickNote.getWorkDays());
    }
}
