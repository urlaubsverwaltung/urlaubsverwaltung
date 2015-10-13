package org.synyx.urlaubsverwaltung.core.overtime;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.util.Optional;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class OvertimeCommentServiceImplTest {

    private OvertimeCommentService commentService;

    private OvertimeCommentDAO commentDAO;

    private Person author;
    private Overtime overtime;

    @Before
    public void setUp() {

        commentDAO = Mockito.mock(OvertimeCommentDAO.class);
        commentService = new OvertimeCommentServiceImpl(commentDAO);

        author = TestDataCreator.createPerson();
        overtime = TestDataCreator.createOvertimeRecord();
    }


    @Test
    public void ensurePersistsComment() {

        commentService.create(overtime, OvertimeAction.CREATED, Optional.of("Foo"), author);

        Mockito.verify(commentDAO).save(Mockito.any(OvertimeComment.class));
    }


    @Test
    public void ensureCreatesCorrectCommentWithoutText() {

        OvertimeComment comment = commentService.create(overtime, OvertimeAction.CREATED, Optional.empty(), author);

        Assert.assertNotNull("Should not be null", comment);

        Assert.assertEquals("Wrong action", OvertimeAction.CREATED, comment.getAction());
        Assert.assertEquals("Wrong author", author, comment.getPerson());
        Assert.assertEquals("Wrong overtime", overtime, comment.getOvertime());

        Assert.assertNull("Text should not be set", comment.getText());
    }


    @Test
    public void ensureCreatesCorrectCommentWithText() {

        OvertimeComment comment = commentService.create(overtime, OvertimeAction.CREATED, Optional.of("Foo"), author);

        Assert.assertNotNull("Should not be null", comment);

        Assert.assertEquals("Wrong action", OvertimeAction.CREATED, comment.getAction());
        Assert.assertEquals("Wrong author", author, comment.getPerson());
        Assert.assertEquals("Wrong overtime", overtime, comment.getOvertime());
        Assert.assertEquals("Wrong text", "Foo", comment.getText());
    }
}
