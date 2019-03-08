package org.synyx.urlaubsverwaltung.core.sicknote;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.util.Optional;


/**
 * Unit test for {@link SickNoteCommentServiceImpl}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class SickNoteCommentServiceImplTest {

    private SickNoteCommentService sickNoteCommentService;

    private SickNoteCommentDAO commentDAO;

    @Before
    public void setUp() {

        commentDAO = Mockito.mock(SickNoteCommentDAO.class);

        sickNoteCommentService = new SickNoteCommentServiceImpl(commentDAO);
    }


    @Test
    public void ensureCreatesACommentAndPersistsIt() {

        Person author = TestDataCreator.createPerson("author");
        SickNote sickNote = TestDataCreator.createSickNote(author);

        SickNoteComment comment = sickNoteCommentService.create(sickNote, SickNoteAction.EDITED,
                Optional.<String>empty(), author);

        Assert.assertNotNull("Should not be null", comment);

        Assert.assertNotNull("Sick note should be set", comment.getSickNote());
        Assert.assertNotNull("Date should be set", comment.getDate());
        Assert.assertNotNull("Action should be set", comment.getAction());
        Assert.assertNotNull("Author should be set", comment.getPerson());

        Assert.assertEquals("Wrong sick note", sickNote, comment.getSickNote());
        Assert.assertEquals("Wrong action", SickNoteAction.EDITED, comment.getAction());
        Assert.assertEquals("Wrong author", author, comment.getPerson());

        Assert.assertNull("Text should not be set", comment.getText());

        Mockito.verify(commentDAO).save(Mockito.eq(comment));
    }


    @Test
    public void ensureCreationOfCommentWithTextWorks() {

        Person author = TestDataCreator.createPerson("author");
        SickNote sickNote = TestDataCreator.createSickNote(author);

        SickNoteComment comment = sickNoteCommentService.create(sickNote, SickNoteAction.CONVERTED_TO_VACATION,
                Optional.of("Foo"), author);

        Assert.assertNotNull("Should not be null", comment);

        Assert.assertNotNull("Sick note should be set", comment.getSickNote());
        Assert.assertNotNull("Date should be set", comment.getDate());
        Assert.assertNotNull("Action should be set", comment.getAction());
        Assert.assertNotNull("Author should be set", comment.getPerson());
        Assert.assertNotNull("Text should be set", comment.getText());

        Assert.assertEquals("Wrong sick note", sickNote, comment.getSickNote());
        Assert.assertEquals("Wrong action", SickNoteAction.CONVERTED_TO_VACATION, comment.getAction());
        Assert.assertEquals("Wrong author", author, comment.getPerson());
        Assert.assertEquals("Wrong text", "Foo", comment.getText());

        Mockito.verify(commentDAO).save(Mockito.eq(comment));
    }
}
