package org.synyx.urlaubsverwaltung.sicknote.comment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createSickNote;
import static org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentAction.CONVERTED_TO_VACATION;
import static org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentAction.EDITED;

/**
 * Unit test for {@link SickNoteCommentServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class SickNoteCommentServiceImplTest {

    private SickNoteCommentServiceImpl sut;

    @Mock
    private SickNoteCommentEntityRepository sickNoteCommentEntityRepository;

    @BeforeEach
    void setUp() {
        sut = new SickNoteCommentServiceImpl(sickNoteCommentEntityRepository, Clock.systemUTC());
    }

    @Test
    void ensureCreatesACommentAndPersistsIt() {

        when(sickNoteCommentEntityRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        final Person author = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final SickNote sickNote = createSickNote(author);

        final SickNoteCommentEntity comment = sut.create(sickNote, EDITED, author);
        assertThat(comment).isNotNull();
        assertThat(comment.getSickNote()).isNotNull();
        assertThat(comment.getDate()).isNotNull();
        assertThat(comment.getAction()).isNotNull();
        assertThat(comment.getPerson()).isNotNull();
        assertThat(comment.getSickNote()).isEqualTo(sickNote);
        assertThat(comment.getAction()).isEqualTo(EDITED);
        assertThat(comment.getPerson()).isEqualTo(author);
        assertThat(comment.getText()).isEmpty();

        verify(sickNoteCommentEntityRepository).save(comment);
    }

    @Test
    void ensureCreationOfCommentWithTextWorks() {

        when(sickNoteCommentEntityRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        final String givenComment = "Foo";
        final Person givenAuthor = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final SickNote givenSickNote = createSickNote(givenAuthor);

        final SickNoteCommentEntity sickNoteCommentEntity = sut.create(givenSickNote, CONVERTED_TO_VACATION, givenAuthor, givenComment);
        assertThat(sickNoteCommentEntity).isNotNull();
        assertThat(sickNoteCommentEntity.getDate()).isNotNull();
        assertThat(sickNoteCommentEntity.getSickNote()).isEqualTo(givenSickNote);
        assertThat(sickNoteCommentEntity.getAction()).isEqualTo(CONVERTED_TO_VACATION);
        assertThat(sickNoteCommentEntity.getPerson()).isEqualTo(givenAuthor);
        assertThat(sickNoteCommentEntity.getText()).isEqualTo(givenComment);

        verify(sickNoteCommentEntityRepository).save(sickNoteCommentEntity);
    }
}
