package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationInteractionService;
import org.synyx.urlaubsverwaltung.application.application.ApplicationStatus;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentAction;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentService;

import java.time.Clock;
import java.time.LocalDate;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * Unit test for {@link SickNoteInteractionServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class SickNoteInteractionServiceImplTest {

    private SickNoteInteractionServiceImpl sut;

    @Mock
    private SickNoteService sickNoteService;
    @Mock
    private SickNoteCommentService commentService;
    @Mock
    private ApplicationInteractionService applicationInteractionService;

    @BeforeEach
    void setUp() {
        sut = new SickNoteInteractionServiceImpl(sickNoteService, commentService, applicationInteractionService, Clock.systemUTC());
    }

    @Test
    void ensureCreatedSickNoteIsPersisted() {

        final Person creator = new Person("creator", "Senior", "Creator", "creator@example.org");

        final SickNote sickNote = getSickNote();

        final SickNote createdSickNote = sut.create(sickNote, creator);
        assertThat(createdSickNote).isNotNull();
        assertThat(createdSickNote.getLastEdited()).isNotNull();
        assertThat(createdSickNote.getStatus()).isEqualTo(SickNoteStatus.ACTIVE);

        verify(sickNoteService).save(sickNote);
        verify(commentService).create(sickNote, SickNoteCommentAction.CREATED, creator, null);
    }

    @Test
    void ensureCreatedSickNoteHasComment() {

        final String comment = "test comment";
        final Person creator = new Person("creator", "Senior", "Creator", "creator@example.org");

        final SickNote sickNote = getSickNote();

        sut.create(sickNote, creator, comment);

        verify(sickNoteService).save(sickNote);
        verify(commentService).create(sickNote, SickNoteCommentAction.CREATED, creator, comment);
    }

    @Test
    void ensureUpdatedSickNoteIsPersisted() {

        final Person creator = new Person("creator", "Senior", "Creator", "creator@example.org");

        final SickNote sickNote = getSickNote();

        final SickNote updatedSickNote = sut.update(sickNote, creator);
        assertThat(updatedSickNote).isNotNull();
        assertThat(updatedSickNote.getLastEdited()).isNotNull();
        assertThat(updatedSickNote.getStatus()).isEqualTo(SickNoteStatus.ACTIVE);

        verify(sickNoteService).save(sickNote);
        verify(commentService).create(sickNote, SickNoteCommentAction.EDITED, creator, null);
    }

    @Test
    void ensureUpdatedSickHasComment() {

        final String comment = "test comment";
        final Person creator = new Person("creator", "Senior", "Creator", "creator@example.org");

        final SickNote sickNote = getSickNote();

        sut.update(sickNote, creator, comment);

        verify(sickNoteService).save(sickNote);
        verify(commentService).create(sickNote, SickNoteCommentAction.EDITED, creator, comment);
    }

    @Test
    void ensureCancelledSickNoteIsPersisted() {
        final Person creator = new Person("creator", "Senior", "Creator", "creator@example.org");

        final SickNote sickNote = getSickNote();

        final SickNote cancelledSickNote = sut.cancel(sickNote, creator);
        assertThat(cancelledSickNote).isNotNull();
        assertThat(cancelledSickNote.getLastEdited()).isNotNull();
        assertThat(cancelledSickNote.getStatus()).isEqualTo(SickNoteStatus.CANCELLED);

        verify(commentService).create(sickNote, SickNoteCommentAction.CANCELLED, creator);
        verify(sickNoteService).save(sickNote);
    }

    @Test
    void ensureConvertedSickNoteIsPersisted() {

        final Person creator = new Person("creator", "Senior", "Creator", "creator@example.org");

        final Application applicationForLeave = new Application();
        applicationForLeave.setStartDate(LocalDate.now(UTC));
        applicationForLeave.setEndDate(LocalDate.now(UTC));
        applicationForLeave.setStatus(ApplicationStatus.ALLOWED);
        applicationForLeave.setDayLength(DayLength.FULL);
        applicationForLeave.setPerson(new Person("muster", "Muster", "Marlene", "muster@example.org"));

        final SickNote sickNote = getSickNote();

        final SickNote convertedSickNote = sut.convert(sickNote, applicationForLeave, creator);
        assertThat(convertedSickNote).isNotNull();
        assertThat(convertedSickNote.getLastEdited()).isNotNull();
        assertThat(convertedSickNote.getStatus()).isEqualTo(SickNoteStatus.CONVERTED_TO_VACATION);

        // assert sick note correctly updated
        verify(sickNoteService).save(sickNote);
        verify(commentService).create(sickNote, SickNoteCommentAction.CONVERTED_TO_VACATION, creator);

        // assert application for leave correctly created
        verify(applicationInteractionService).createFromConvertedSickNote(applicationForLeave, creator);
    }

    private SickNote getSickNote() {
        final SickNote sickNote = new SickNote();
        sickNote.setId(42);
        sickNote.setStatus(SickNoteStatus.ACTIVE);
        sickNote.setStartDate(LocalDate.now(UTC));
        sickNote.setEndDate(LocalDate.now(UTC));
        sickNote.setDayLength(DayLength.FULL);
        sickNote.setPerson(new Person("muster", "Muster", "Marlene", "muster@example.org"));
        return sickNote;
    }
}
