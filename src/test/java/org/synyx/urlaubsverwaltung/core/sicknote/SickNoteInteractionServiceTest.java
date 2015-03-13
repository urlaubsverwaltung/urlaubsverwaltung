package org.synyx.urlaubsverwaltung.core.sicknote;

import com.google.common.base.Optional;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.domain.Comment;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.core.application.service.CommentService;
import org.synyx.urlaubsverwaltung.core.application.service.SignService;
import org.synyx.urlaubsverwaltung.core.mail.MailService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.sicknote.comment.SickNoteCommentService;
import org.synyx.urlaubsverwaltung.core.sicknote.comment.SickNoteStatus;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.core.sicknote.SickNoteInteractionServiceImpl}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class SickNoteInteractionServiceTest {

    private SickNoteInteractionService sickNoteInteractionService;

    private SickNoteService sickNoteService;
    private SickNoteCommentService sickNoteCommentService;
    private ApplicationService applicationService;
    private CommentService applicationCommentService;
    private SignService signService;
    private MailService mailService;

    @Before
    public void setUp() {

        sickNoteService = Mockito.mock(SickNoteService.class);
        sickNoteCommentService = Mockito.mock(SickNoteCommentService.class);
        applicationService = Mockito.mock(ApplicationService.class);
        applicationCommentService = Mockito.mock(CommentService.class);
        signService = Mockito.mock(SignService.class);
        mailService = Mockito.mock(MailService.class);

        sickNoteInteractionService = new SickNoteInteractionServiceImpl(sickNoteService, sickNoteCommentService,
                applicationService, applicationCommentService, signService, mailService);
    }


    @Test
    public void ensureCreatedSickNoteIsPersisted() {

        Person person = new Person();
        SickNote sickNote = new SickNote();

        SickNote createdSickNote = sickNoteInteractionService.create(sickNote, person);

        Mockito.verify(sickNoteService).save(sickNote);
        Mockito.verify(sickNoteCommentService).create(SickNoteStatus.CREATED, Optional.<String>absent(), person);

        Assert.assertNotNull("Should not be null", createdSickNote);

        Assert.assertNotNull("Last edited date should be set", createdSickNote.getLastEdited());
        Assert.assertTrue("Should be active", createdSickNote.isActive());
    }


    @Test
    public void ensureUpdatedSickNoteIsPersisted() {

        Person person = new Person();
        SickNote sickNote = new SickNote();

        SickNote updatedSickNote = sickNoteInteractionService.update(sickNote, person);

        Mockito.verify(sickNoteService).save(sickNote);
        Mockito.verify(sickNoteCommentService).create(SickNoteStatus.EDITED, Optional.<String>absent(), person);

        Assert.assertNotNull("Should not be null", updatedSickNote);

        Assert.assertNotNull("Last edited date should be set", updatedSickNote.getLastEdited());
        Assert.assertTrue("Should be active", updatedSickNote.isActive());
    }


    @Test
    public void ensureCancelledSickNoteIsPersisted() {

        Person person = new Person();
        SickNote sickNote = new SickNote();

        SickNote cancelledSickNote = sickNoteInteractionService.cancel(sickNote, person);

        Mockito.verify(sickNoteService).save(sickNote);
        Mockito.verify(sickNoteCommentService).create(SickNoteStatus.CANCELLED, Optional.<String>absent(), person);

        Assert.assertNotNull("Should not be null", cancelledSickNote);

        Assert.assertNotNull("Last edited date should be set", cancelledSickNote.getLastEdited());
        Assert.assertFalse("Should be inactive", cancelledSickNote.isActive());
    }


    @Test
    public void ensureConvertedSickNoteIsPersisted() {

        Person person = new Person();
        SickNote sickNote = new SickNote();
        Application applicationForLeave = new Application();

        SickNote convertedSickNote = sickNoteInteractionService.convert(sickNote, applicationForLeave, person);

        // assert sick note correctly updated

        Mockito.verify(sickNoteService).save(sickNote);
        Mockito.verify(sickNoteCommentService).create(SickNoteStatus.CONVERTED_TO_VACATION, Optional.<String>absent(),
            person);

        Assert.assertNotNull("Should not be null", convertedSickNote);

        Assert.assertNotNull("Last edited date should be set", convertedSickNote.getLastEdited());
        Assert.assertFalse("Should be inactive", convertedSickNote.isActive());

        // assert application for leave correctly created

        Mockito.verify(applicationService).save(applicationForLeave);
        Mockito.verify(applicationCommentService).saveComment(Mockito.any(Comment.class), Mockito.eq(person),
            Mockito.eq(applicationForLeave));
        Mockito.verify(signService).signApplicationByBoss(Mockito.eq(applicationForLeave), Mockito.eq(person));
        Mockito.verify(mailService).sendSickNoteConvertedToVacationNotification(Mockito.eq(applicationForLeave));

        Assert.assertNotNull("Status should be set", applicationForLeave.getStatus());
        Assert.assertNotNull("Applier should be set", applicationForLeave.getApplier());

        Assert.assertEquals("Wrong status", ApplicationStatus.ALLOWED, applicationForLeave.getStatus());
        Assert.assertEquals("Wrong applier", person, applicationForLeave.getApplier());
    }
}
