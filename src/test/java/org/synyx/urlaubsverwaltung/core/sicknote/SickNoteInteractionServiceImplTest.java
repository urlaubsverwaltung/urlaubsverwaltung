package org.synyx.urlaubsverwaltung.core.sicknote;

import org.joda.time.DateMidnight;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationInteractionService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.settings.Settings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.core.sicknote.comment.SickNoteCommentService;
import org.synyx.urlaubsverwaltung.core.sicknote.comment.SickNoteCommentStatus;
import org.synyx.urlaubsverwaltung.core.sync.CalendarSyncService;
import org.synyx.urlaubsverwaltung.core.sync.absence.Absence;
import org.synyx.urlaubsverwaltung.core.sync.absence.AbsenceMapping;
import org.synyx.urlaubsverwaltung.core.sync.absence.AbsenceMappingService;
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
    private SickNoteCommentService commentService;
    private ApplicationInteractionService applicationInteractionService;
    private CalendarSyncService calendarSyncService;
    private AbsenceMappingService absenceMappingService;
    private SettingsService settingsService;

    private SickNote sickNote;
    private Person person;

    @Before
    public void setUp() {

        sickNoteService = Mockito.mock(SickNoteService.class);
        commentService = Mockito.mock(SickNoteCommentService.class);
        applicationInteractionService = Mockito.mock(ApplicationInteractionService.class);
        calendarSyncService = Mockito.mock(CalendarSyncService.class);
        absenceMappingService = Mockito.mock(AbsenceMappingService.class);
        settingsService = Mockito.mock(SettingsService.class);

        Mockito.when(calendarSyncService.addAbsence(any(Absence.class))).thenReturn(Optional.of("42"));
        Mockito.when(absenceMappingService.getAbsenceByIdAndType(anyInt(), eq(AbsenceType.SICKNOTE)))
            .thenReturn(Optional.of(new AbsenceMapping(1, AbsenceType.VACATION, "42")));
        Mockito.when(settingsService.getSettings()).thenReturn(new Settings());

        sickNoteInteractionService = new SickNoteInteractionServiceImpl(sickNoteService, commentService,
                applicationInteractionService, calendarSyncService, absenceMappingService, settingsService);

        sickNote = new SickNote();
        sickNote.setStatus(SickNoteStatus.ACTIVE);
        sickNote.setStartDate(DateMidnight.now());
        sickNote.setEndDate(DateMidnight.now());
        sickNote.setDayLength(DayLength.FULL);

        person = new Person();
    }


    @Test
    public void ensureCreatedSickNoteIsPersisted() {

        SickNote createdSickNote = sickNoteInteractionService.create(sickNote, person);

        Mockito.verify(sickNoteService).save(sickNote);
        Mockito.verify(commentService)
            .create(sickNote, SickNoteCommentStatus.CREATED, Optional.<String>empty(), person);

        Assert.assertNotNull("Should not be null", createdSickNote);

        Assert.assertNotNull("Last edited date should be set", createdSickNote.getLastEdited());
        Assert.assertEquals("Wrong status", SickNoteStatus.ACTIVE, createdSickNote.getStatus());
    }


    @Test
    public void ensureCreatingSickNoteAddsEventToCalendar() throws Exception {

        SickNote createdSickNote = sickNoteInteractionService.create(sickNote, person);

        Mockito.verify(calendarSyncService).addAbsence(Mockito.any(Absence.class));
        Mockito.verify(absenceMappingService).create(eq(createdSickNote), Mockito.anyString());
    }


    @Test
    public void ensureUpdatedSickNoteIsPersisted() {

        SickNote updatedSickNote = sickNoteInteractionService.update(sickNote, person);

        Mockito.verify(sickNoteService).save(sickNote);
        Mockito.verify(commentService).create(sickNote, SickNoteCommentStatus.EDITED, Optional.<String>empty(), person);

        Assert.assertNotNull("Should not be null", updatedSickNote);

        Assert.assertNotNull("Last edited date should be set", updatedSickNote.getLastEdited());
        Assert.assertEquals("Status should not be changed", sickNote.getStatus(), updatedSickNote.getStatus());
    }


    @Test
    public void ensureUpdatingSickNoteUpdatesCalendarEvent() throws Exception {

        sickNoteInteractionService.update(sickNote, person);

        Mockito.verify(calendarSyncService).update(Mockito.any(Absence.class), Mockito.anyString());
        Mockito.verify(absenceMappingService).getAbsenceByIdAndType(anyInt(), Mockito.eq(AbsenceType.SICKNOTE));
    }


    @Test
    public void ensureCancelledSickNoteIsPersisted() {

        SickNote cancelledSickNote = sickNoteInteractionService.cancel(sickNote, person);

        Mockito.verify(sickNoteService).save(sickNote);
        Mockito.verify(commentService)
            .create(sickNote, SickNoteCommentStatus.CANCELLED, Optional.<String>empty(), person);

        Assert.assertNotNull("Should not be null", cancelledSickNote);

        Assert.assertNotNull("Last edited date should be set", cancelledSickNote.getLastEdited());
        Assert.assertEquals("Wrong status", SickNoteStatus.CANCELLED, cancelledSickNote.getStatus());
    }


    @Test
    public void ensureCancellingSickNoteDeletesCalendarEvent() throws Exception {

        sickNoteInteractionService.cancel(sickNote, person);

        Mockito.verify(absenceMappingService).getAbsenceByIdAndType(Mockito.anyInt(), Mockito.eq(AbsenceType.SICKNOTE));
        Mockito.verify(calendarSyncService).deleteAbsence(Mockito.anyString());
        Mockito.verify(absenceMappingService).delete(Mockito.any(AbsenceMapping.class));
    }


    @Test
    public void ensureConvertedSickNoteIsPersisted() {

        Application applicationForLeave = new Application();
        applicationForLeave.setStartDate(DateMidnight.now());
        applicationForLeave.setEndDate(DateMidnight.now());
        applicationForLeave.setStatus(ApplicationStatus.ALLOWED);
        applicationForLeave.setDayLength(DayLength.FULL);

        SickNote convertedSickNote = sickNoteInteractionService.convert(sickNote, applicationForLeave, person);

        // assert sick note correctly updated

        Mockito.verify(sickNoteService).save(sickNote);
        Mockito.verify(commentService)
            .create(sickNote, SickNoteCommentStatus.CONVERTED_TO_VACATION, Optional.<String>empty(), person);

        Assert.assertNotNull("Should not be null", convertedSickNote);

        Assert.assertNotNull("Last edited date should be set", convertedSickNote.getLastEdited());
        Assert.assertEquals("Wrong status", SickNoteStatus.CONVERTED_TO_VACATION, convertedSickNote.getStatus());

        // assert application for leave correctly created
        Mockito.verify(applicationInteractionService).createFromConvertedSickNote(applicationForLeave, person);
    }


    @Test
    public void ensureConvertingSickNoteToVacationUpdatesCalendarEvent() throws Exception {

        Application applicationForLeave = new Application();
        applicationForLeave.setStartDate(DateMidnight.now());
        applicationForLeave.setEndDate(DateMidnight.now());
        applicationForLeave.setStatus(ApplicationStatus.ALLOWED);
        applicationForLeave.setDayLength(DayLength.FULL);

        AbsenceMapping absenceMapping = new AbsenceMapping();
        Mockito.when(absenceMappingService.getAbsenceByIdAndType(Mockito.anyInt(), Mockito.eq(AbsenceType.SICKNOTE)))
            .thenReturn(Optional.of(absenceMapping));

        sickNoteInteractionService.convert(sickNote, applicationForLeave, person);

        Mockito.verify(absenceMappingService).getAbsenceByIdAndType(Mockito.anyInt(), Mockito.eq(AbsenceType.SICKNOTE));
        Mockito.verify(calendarSyncService).update(Mockito.any(Absence.class), Mockito.anyString());
        Mockito.verify(absenceMappingService).delete(Mockito.eq(absenceMapping));
        Mockito.verify(absenceMappingService).create(Mockito.eq(applicationForLeave), Mockito.anyString());
    }
}
