package org.synyx.urlaubsverwaltung.sicknote;

import org.junit.Before;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


/**
 * Unit test for {@link SickNoteCommentServiceImpl}.
 */
public class SickNoteCommentServiceImplTest {

    private SickNoteCommentService sickNoteCommentService;

    private SickNoteCommentDAO commentDAO;

    @Before
    public void setUp() {

        commentDAO = mock(SickNoteCommentDAO.class);

        sickNoteCommentService = new SickNoteCommentServiceImpl(commentDAO);
    }


    @Test
    public void ensureCreatesACommentAndPersistsIt() {

        Person author = TestDataCreator.createPerson("author");
        SickNote sickNote = TestDataCreator.createSickNote(author);

        SickNoteComment comment = sickNoteCommentService.create(sickNote, SickNoteAction.EDITED, author);

        assertThat(comment).isNotNull();
        assertThat(comment.getSickNote()).isNotNull();
        assertThat(comment.getDate()).isNotNull();
        assertThat(comment.getAction()).isNotNull();
        assertThat(comment.getPerson()).isNotNull();
        assertThat(comment.getSickNote()).isEqualTo(sickNote);
        assertThat(comment.getAction()).isEqualTo(SickNoteAction.EDITED);
        assertThat(comment.getPerson()).isEqualTo(author);
        assertThat(comment.getText()).isNull();


        verify(commentDAO).save(eq(comment));
    }


    @Test
    public void ensureCreationOfCommentWithTextWorks() {

        String comment = "Foo";
        Person author = TestDataCreator.createPerson("author");
        SickNote sickNote = TestDataCreator.createSickNote(author);

        SickNoteComment sickNoteComment = sickNoteCommentService.create(sickNote, SickNoteAction.CONVERTED_TO_VACATION,
            author, comment);

        assertThat(sickNoteComment).isNotNull();
        assertThat(sickNoteComment.getSickNote()).isNotNull();
        assertThat(sickNoteComment.getDate()).isNotNull();
        assertThat(sickNoteComment.getAction()).isNotNull();
        assertThat(sickNoteComment.getPerson()).isNotNull();
        assertThat(sickNoteComment.getText()).isNotNull();

        assertThat(sickNoteComment.getSickNote()).isEqualTo(sickNote);
        assertThat(sickNoteComment.getAction()).isEqualTo(SickNoteAction.CONVERTED_TO_VACATION);
        assertThat(sickNoteComment.getPerson()).isEqualTo(author);
        assertThat(sickNoteComment.getText()).isEqualTo(comment);

        verify(commentDAO).save(eq(sickNoteComment));
    }
}
