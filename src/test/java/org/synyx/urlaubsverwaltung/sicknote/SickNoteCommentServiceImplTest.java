package org.synyx.urlaubsverwaltung.sicknote;

import org.junit.Before;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Unit test for {@link SickNoteCommentServiceImpl}.
 */
public class SickNoteCommentServiceImplTest {

    private SickNoteCommentService sickNoteCommentService;

    private SickNoteCommentDAO commentDAO;

    @Before
    public void setUp() {

        commentDAO = mock(SickNoteCommentDAO.class);
        when(commentDAO.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        sickNoteCommentService = new SickNoteCommentServiceImpl(commentDAO);
    }


    @Test
    public void ensureCreatesACommentAndPersistsIt() {

        final Person author = TestDataCreator.createPerson("author");
        final SickNote sickNote = TestDataCreator.createSickNote(author);

        SickNoteComment comment = sickNoteCommentService.create(sickNote, SickNoteAction.EDITED, author);

        assertThat(comment).isNotNull();
        assertThat(comment.getSickNote()).isNotNull();
        assertThat(comment.getDate()).isNotNull();
        assertThat(comment.getAction()).isNotNull();
        assertThat(comment.getPerson()).isNotNull();
        assertThat(comment.getSickNote()).isEqualTo(sickNote);
        assertThat(comment.getAction()).isEqualTo(SickNoteAction.EDITED);
        assertThat(comment.getPerson()).isEqualTo(author);
        assertThat(comment.getText()).isEqualTo("");

        verify(commentDAO).save(eq(comment));
    }


    @Test
    public void ensureCreationOfCommentWithTextWorks() {

        final String givenComment = "Foo";
        final Person givenAuthor = TestDataCreator.createPerson("author");
        final SickNote givenSickNote = TestDataCreator.createSickNote(givenAuthor);

        SickNoteComment sickNoteComment = sickNoteCommentService.create(givenSickNote, SickNoteAction.CONVERTED_TO_VACATION,
            givenAuthor, givenComment);

        assertThat(sickNoteComment).isNotNull();
        assertThat(sickNoteComment.getDate()).isNotNull();

        assertThat(sickNoteComment.getSickNote()).isEqualTo(givenSickNote);
        assertThat(sickNoteComment.getAction()).isEqualTo(SickNoteAction.CONVERTED_TO_VACATION);
        assertThat(sickNoteComment.getPerson()).isEqualTo(givenAuthor);
        assertThat(sickNoteComment.getText()).isEqualTo(givenComment);

        verify(commentDAO).save(eq(sickNoteComment));
    }
}
