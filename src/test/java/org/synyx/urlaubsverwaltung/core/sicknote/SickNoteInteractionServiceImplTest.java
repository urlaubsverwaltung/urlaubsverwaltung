package org.synyx.urlaubsverwaltung.core.sicknote;

import junit.framework.Assert;

import org.joda.time.DateMidnight;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.core.application.service.CommentService;
import org.synyx.urlaubsverwaltung.core.application.service.SignService;
import org.synyx.urlaubsverwaltung.core.mail.MailService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.sicknote.comment.SickNoteCommentService;
import org.synyx.urlaubsverwaltung.core.sicknote.comment.SickNoteStatus;
import org.synyx.urlaubsverwaltung.core.sync.CalendarProviderService;
import org.synyx.urlaubsverwaltung.core.sync.absence.Absence;
import org.synyx.urlaubsverwaltung.core.sync.absence.AbsenceMapping;
import org.synyx.urlaubsverwaltung.core.sync.absence.AbsenceMappingService;
import org.synyx.urlaubsverwaltung.core.sync.absence.AbsenceTimeConfiguration;
import org.synyx.urlaubsverwaltung.core.sync.absence.AbsenceType;

import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.core.sicknote.SickNoteInteractionServiceImpl}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class SickNoteInteractionServiceImplTest {

    private SickNoteInteractionService sickNoteInteractionService;

    private SickNoteService sickNoteService;
    private SickNoteCommentService sickNoteCommentService;
    private ApplicationService applicationService;
    private CommentService applicationCommentService;
    private SignService signService;
    private MailService mailService;
    private CalendarProviderService calendarProviderService;
    private AbsenceMappingService absenceMappingService;

    private SickNote sickNote;
    private Person person;

    @Before
    public void setUp() {

        sickNoteService = Mockito.mock(SickNoteService.class);
        sickNoteCommentService = Mockito.mock(SickNoteCommentService.class);
        applicationService = Mockito.mock(ApplicationService.class);
        applicationCommentService = Mockito.mock(CommentService.class);
        signService = Mockito.mock(SignService.class);
        mailService = Mockito.mock(MailService.class);
        calendarProviderService = Mockito.mock(CalendarProviderService.class);
        absenceMappingService = Mockito.mock(AbsenceMappingService.class);

        Mockito.when(calendarProviderService.addAbsence(any(Absence.class))).thenReturn(Optional.of("42"));
        Mockito.when(absenceMappingService.getAbsenceByIdAndType(anyInt(), eq(AbsenceType.SICKNOTE)))
            .thenReturn(Optional.of(new AbsenceMapping(1, AbsenceType.VACATION, "42")));

        sickNoteInteractionService = new SickNoteInteractionServiceImpl(sickNoteService, sickNoteCommentService,
                applicationService, applicationCommentService, signService, mailService, calendarProviderService,
                absenceMappingService, new AbsenceTimeConfiguration(8, 12, 13, 17));

        sickNote = new SickNote();
        sickNote.setStartDate(DateMidnight.now());
        sickNote.setEndDate(DateMidnight.now());

        person = new Person();
    }


    @Test
    public void ensureCreatedSickNoteIsPersisted() {

        SickNote createdSickNote = sickNoteInteractionService.create(sickNote, person);

        Mockito.verify(sickNoteService).save(sickNote);
        Mockito.verify(sickNoteCommentService)
            .create(sickNote, SickNoteStatus.CREATED, Optional.<String>empty(), person);

        Assert.assertNotNull("Should not be null", createdSickNote);

        Assert.assertNotNull("Last edited date should be set", createdSickNote.getLastEdited());
        Assert.assertTrue("Should be active", createdSickNote.isActive());
    }


    @Test
    public void ensureCreatingSickNoteAddsEventToCalendar() throws Exception {

        SickNote createdSickNote = sickNoteInteractionService.create(sickNote, person);

        Mockito.verify(calendarProviderService).addAbsence(Mockito.any(Absence.class));
        Mockito.verify(absenceMappingService).create(eq(createdSickNote), Mockito.anyString());
    }


    @Test
    public void ensureUpdatedSickNoteIsPersisted() {

        SickNote updatedSickNote = sickNoteInteractionService.update(sickNote, person);

        Mockito.verify(sickNoteService).save(sickNote);
        Mockito.verify(sickNoteCommentService)
            .create(sickNote, SickNoteStatus.EDITED, Optional.<String>empty(), person);

        Assert.assertNotNull("Should not be null", updatedSickNote);

        Assert.assertNotNull("Last edited date should be set", updatedSickNote.getLastEdited());
        Assert.assertTrue("Should be active", updatedSickNote.isActive());
    }


    @Test
    public void ensureUpdatingSickNoteUpdatesCalendarEvent() throws Exception {

        sickNoteInteractionService.update(sickNote, person);

        Mockito.verify(calendarProviderService).update(Mockito.any(Absence.class), Mockito.anyString());
        Mockito.verify(absenceMappingService).getAbsenceByIdAndType(anyInt(), Mockito.eq(AbsenceType.SICKNOTE));
    }


    @Test
    public void ensureCancelledSickNoteIsPersisted() {

        SickNote cancelledSickNote = sickNoteInteractionService.cancel(sickNote, person);

        Mockito.verify(sickNoteService).save(sickNote);
        Mockito.verify(sickNoteCommentService)
            .create(sickNote, SickNoteStatus.CANCELLED, Optional.<String>empty(), person);

        Assert.assertNotNull("Should not be null", cancelledSickNote);

        Assert.assertNotNull("Last edited date should be set", cancelledSickNote.getLastEdited());
        Assert.assertFalse("Should be inactive", cancelledSickNote.isActive());
    }


    @Test
    public void ensureCancellingSickNoteDeletesCalendarEvent() throws Exception {

        sickNoteInteractionService.cancel(sickNote, person);

        Mockito.verify(absenceMappingService).getAbsenceByIdAndType(Mockito.anyInt(), Mockito.eq(AbsenceType.SICKNOTE));
        Mockito.verify(calendarProviderService).deleteAbsence(Mockito.anyString());
        Mockito.verify(absenceMappingService).delete(Mockito.any(AbsenceMapping.class));
    }


    @Test
    public void ensureConvertedSickNoteIsPersisted() {

        Application applicationForLeave = new Application();
        applicationForLeave.setStartDate(DateMidnight.now());
        applicationForLeave.setEndDate(DateMidnight.now());
        applicationForLeave.setStatus(ApplicationStatus.ALLOWED);
        applicationForLeave.setHowLong(DayLength.FULL);

        SickNote convertedSickNote = sickNoteInteractionService.convert(sickNote, applicationForLeave, person);

        // assert sick note correctly updated

        Mockito.verify(sickNoteService).save(sickNote);
        Mockito.verify(sickNoteCommentService)
            .create(sickNote, SickNoteStatus.CONVERTED_TO_VACATION, Optional.<String>empty(), person);

        Assert.assertNotNull("Should not be null", convertedSickNote);

        Assert.assertNotNull("Last edited date should be set", convertedSickNote.getLastEdited());
        Assert.assertFalse("Should be inactive", convertedSickNote.isActive());

        // assert application for leave correctly created

        Mockito.verify(applicationService).save(applicationForLeave);
        Mockito.verify(applicationCommentService)
            .create(eq(applicationForLeave), eq(ApplicationStatus.ALLOWED), eq(Optional.<String>empty()), eq(person));
        Mockito.verify(signService).signApplicationByBoss(eq(applicationForLeave), eq(person));
        Mockito.verify(mailService).sendSickNoteConvertedToVacationNotification(eq(applicationForLeave));

        Assert.assertNotNull("Status should be set", applicationForLeave.getStatus());
        Assert.assertNotNull("Applier should be set", applicationForLeave.getApplier());

        Assert.assertEquals("Wrong status", ApplicationStatus.ALLOWED, applicationForLeave.getStatus());
        Assert.assertEquals("Wrong applier", person, applicationForLeave.getApplier());
    }


    @Test
    public void ensureConvertingSickNoteToVacationUpdatesCalendarEvent() throws Exception {

        Application applicationForLeave = new Application();
        applicationForLeave.setStartDate(DateMidnight.now());
        applicationForLeave.setEndDate(DateMidnight.now());
        applicationForLeave.setStatus(ApplicationStatus.ALLOWED);
        applicationForLeave.setHowLong(DayLength.FULL);

        AbsenceMapping absenceMapping = new AbsenceMapping();
        Mockito.when(absenceMappingService.getAbsenceByIdAndType(Mockito.anyInt(), Mockito.eq(AbsenceType.SICKNOTE)))
            .thenReturn(Optional.of(absenceMapping));

        sickNoteInteractionService.convert(sickNote, applicationForLeave, person);

        Mockito.verify(absenceMappingService).getAbsenceByIdAndType(Mockito.anyInt(), Mockito.eq(AbsenceType.SICKNOTE));
        Mockito.verify(calendarProviderService).update(Mockito.any(Absence.class), Mockito.anyString());
        Mockito.verify(absenceMappingService).delete(Mockito.eq(absenceMapping));
        Mockito.verify(absenceMappingService).create(Mockito.eq(applicationForLeave), Mockito.anyString());
    }
}
