package org.synyx.urlaubsverwaltung.sicknote;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;

import java.util.Optional;

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

        Assert.assertNotNull("Should not be null", comment);

        Assert.assertNotNull("Sick note should be set", comment.getSickNote());
        Assert.assertNotNull("Date should be set", comment.getDate());
        Assert.assertNotNull("Action should be set", comment.getAction());
        Assert.assertNotNull("Author should be set", comment.getPerson());

        Assert.assertEquals("Wrong sick note", sickNote, comment.getSickNote());
        Assert.assertEquals("Wrong action", SickNoteAction.EDITED, comment.getAction());
        Assert.assertEquals("Wrong author", author, comment.getPerson());

        Assert.assertNull("Text should not be set", comment.getText());

        verify(commentDAO).save(eq(comment));
    }


    @Test
    public void ensureCreationOfCommentWithTextWorks() {

        String comment = "Foo";
        Person author = TestDataCreator.createPerson("author");
        SickNote sickNote = TestDataCreator.createSickNote(author);

        SickNoteComment sickNoteComment = sickNoteCommentService.create(sickNote, SickNoteAction.CONVERTED_TO_VACATION,
            author, comment);

        Assert.assertNotNull("Should not be null", sickNoteComment);

        Assert.assertNotNull("Sick note should be set", sickNoteComment.getSickNote());
        Assert.assertNotNull("Date should be set", sickNoteComment.getDate());
        Assert.assertNotNull("Action should be set", sickNoteComment.getAction());
        Assert.assertNotNull("Author should be set", sickNoteComment.getPerson());
        Assert.assertNotNull("Text should be set", sickNoteComment.getText());

        Assert.assertEquals("Wrong sick note", sickNote, sickNoteComment.getSickNote());
        Assert.assertEquals("Wrong action", SickNoteAction.CONVERTED_TO_VACATION, sickNoteComment.getAction());
        Assert.assertEquals("Wrong author", author, sickNoteComment.getPerson());
        Assert.assertEquals("Wrong text", comment, sickNoteComment.getText());

        verify(commentDAO).save(eq(sickNoteComment));
    }
}
