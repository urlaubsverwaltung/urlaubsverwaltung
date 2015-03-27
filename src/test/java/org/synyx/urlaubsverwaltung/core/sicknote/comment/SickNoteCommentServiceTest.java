package org.synyx.urlaubsverwaltung.core.sicknote.comment;

import com.google.common.base.Optional;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.core.sicknote.comment.SickNoteCommentServiceImpl}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class SickNoteCommentServiceTest {

    private SickNoteCommentService sickNoteCommentService;

    private SickNoteCommentDAO commentDAO;

    @Before
    public void setUp() {

        commentDAO = Mockito.mock(SickNoteCommentDAO.class);

        sickNoteCommentService = new SickNoteCommentServiceImpl(commentDAO);
    }


    @Test
    public void ensureCreatesACommentAndPersistsIt() {

        Person author = new Person();
        SickNote sickNote = new SickNote();

        SickNoteComment comment = sickNoteCommentService.create(sickNote, SickNoteStatus.EDITED,
                Optional.<String>absent(), author);

        Assert.assertNotNull("Should not be null", comment);

        Assert.assertNotNull("Sick note should be set", comment.getSickNote());
        Assert.assertNotNull("Date should be set", comment.getDate());
        Assert.assertNotNull("Status should be set", comment.getStatus());
        Assert.assertNotNull("Author should be set", comment.getPerson());

        Assert.assertEquals("Wrong sick note", sickNote, comment.getSickNote());
        Assert.assertEquals("Wrong status", SickNoteStatus.EDITED, comment.getStatus());
        Assert.assertEquals("Wrong author", author, comment.getPerson());

        Assert.assertNull("Text should not be set", comment.getText());

        Mockito.verify(commentDAO).save(Mockito.eq(comment));
    }


    @Test
    public void ensureCreationOfCommentWithTextWorks() {

        Person author = new Person();
        SickNote sickNote = new SickNote();

        SickNoteComment comment = sickNoteCommentService.create(sickNote, SickNoteStatus.CONVERTED_TO_VACATION,
                Optional.of("Foo"), author);

        Assert.assertNotNull("Should not be null", comment);

        Assert.assertNotNull("Sick note should be set", comment.getSickNote());
        Assert.assertNotNull("Date should be set", comment.getDate());
        Assert.assertNotNull("Status should be set", comment.getStatus());
        Assert.assertNotNull("Author should be set", comment.getPerson());
        Assert.assertNotNull("Text should be set", comment.getText());

        Assert.assertEquals("Wrong sick note", sickNote, comment.getSickNote());
        Assert.assertEquals("Wrong status", SickNoteStatus.CONVERTED_TO_VACATION, comment.getStatus());
        Assert.assertEquals("Wrong author", author, comment.getPerson());
        Assert.assertEquals("Wrong text", "Foo", comment.getText());

        Mockito.verify(commentDAO).save(Mockito.eq(comment));
    }
}
