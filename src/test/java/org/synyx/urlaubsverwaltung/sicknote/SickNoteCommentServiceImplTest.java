package org.synyx.urlaubsverwaltung.sicknote;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.synyx.urlaubsverwaltung.person.Person;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.sicknote.SickNoteAction.CONVERTED_TO_VACATION;
import static org.synyx.urlaubsverwaltung.sicknote.SickNoteAction.EDITED;
import static org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator.createPerson;
import static org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator.createSickNote;


/**
 * Unit test for {@link SickNoteCommentServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SickNoteCommentServiceImplTest {

    private SickNoteCommentService sut;

    @Mock
    private SickNoteCommentDAO commentDAO;

    @Before
    public void setUp() {
        sut = new SickNoteCommentServiceImpl(commentDAO);
    }

    @Test
    public void ensureCreatesACommentAndPersistsIt() {

        when(commentDAO.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        final Person author = createPerson("author");
        final SickNote sickNote = createSickNote(author);

        final SickNoteComment comment = sut.create(sickNote, EDITED, author);
        assertThat(comment).isNotNull();
        assertThat(comment.getSickNote()).isNotNull();
        assertThat(comment.getDate()).isNotNull();
        assertThat(comment.getAction()).isNotNull();
        assertThat(comment.getPerson()).isNotNull();
        assertThat(comment.getSickNote()).isEqualTo(sickNote);
        assertThat(comment.getAction()).isEqualTo(EDITED);
        assertThat(comment.getPerson()).isEqualTo(author);
        assertThat(comment.getText()).isEmpty();

        verify(commentDAO).save(eq(comment));
    }

    @Test
    public void ensureCreationOfCommentWithTextWorks() {

        when(commentDAO.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        final String givenComment = "Foo";
        final Person givenAuthor = createPerson("author");
        final SickNote givenSickNote = createSickNote(givenAuthor);

        final SickNoteComment sickNoteComment = sut.create(givenSickNote, CONVERTED_TO_VACATION, givenAuthor, givenComment);
        assertThat(sickNoteComment).isNotNull();
        assertThat(sickNoteComment.getDate()).isNotNull();
        assertThat(sickNoteComment.getSickNote()).isEqualTo(givenSickNote);
        assertThat(sickNoteComment.getAction()).isEqualTo(CONVERTED_TO_VACATION);
        assertThat(sickNoteComment.getPerson()).isEqualTo(givenAuthor);
        assertThat(sickNoteComment.getText()).isEqualTo(givenComment);

        verify(commentDAO).save(eq(sickNoteComment));
    }
}
