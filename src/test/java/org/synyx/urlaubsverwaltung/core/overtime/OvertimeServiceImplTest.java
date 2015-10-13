package org.synyx.urlaubsverwaltung.core.overtime;

import org.joda.time.DateMidnight;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.math.BigDecimal;

import java.util.Optional;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class OvertimeServiceImplTest {

    private OvertimeService overtimeService;

    private OvertimeDAO overtimeDAO;
    private OvertimeCommentDAO commentDAO;

    private Overtime overtime;
    private Person author;

    @Before
    public void setUp() {

        commentDAO = Mockito.mock(OvertimeCommentDAO.class);
        overtimeDAO = Mockito.mock(OvertimeDAO.class);

        overtimeService = new OvertimeServiceImpl(overtimeDAO, commentDAO);

        Person person = TestDataCreator.createPerson();
        DateMidnight startDate = DateMidnight.now();
        DateMidnight endDate = startDate.plusDays(3);

        overtime = new Overtime(person, startDate, endDate, BigDecimal.ONE);
        author = TestDataCreator.createPerson();
    }


    // Record overtime -------------------------------------------------------------------------------------------------

    @Test
    public void ensurePersistsOvertimeAndComment() {

        overtimeService.record(overtime, Optional.of("Foo Bar"), author);

        Mockito.verify(overtimeDAO).save(overtime);
        Mockito.verify(commentDAO).save(Mockito.any(OvertimeComment.class));
    }


    @Test
    public void ensureCreatedCommentWithoutTextHasCorrectProperties() {

        ArgumentCaptor<OvertimeComment> commentCaptor = ArgumentCaptor.forClass(OvertimeComment.class);

        overtimeService.record(overtime, Optional.empty(), author);

        Mockito.verify(commentDAO).save(commentCaptor.capture());

        OvertimeComment comment = commentCaptor.getValue();

        Assert.assertNotNull("Should not be null", comment);

        Assert.assertEquals("Wrong action", OvertimeAction.CREATED, comment.getAction());
        Assert.assertEquals("Wrong author", author, comment.getPerson());
        Assert.assertEquals("Wrong overtime", overtime, comment.getOvertime());

        Assert.assertNull("Text should not be set", comment.getText());
    }


    @Test
    public void ensureCreatedCommentWithTextHasCorrectProperties() {

        ArgumentCaptor<OvertimeComment> commentCaptor = ArgumentCaptor.forClass(OvertimeComment.class);

        overtimeService.record(overtime, Optional.of("Foo"), author);

        Mockito.verify(commentDAO).save(commentCaptor.capture());

        OvertimeComment comment = commentCaptor.getValue();

        Assert.assertNotNull("Should not be null", comment);

        Assert.assertEquals("Wrong action", OvertimeAction.CREATED, comment.getAction());
        Assert.assertEquals("Wrong author", author, comment.getPerson());
        Assert.assertEquals("Wrong overtime", overtime, comment.getOvertime());
        Assert.assertEquals("Wrong text", "Foo", comment.getText());
    }


    // Get overtime records for person ---------------------------------------------------------------------------------

    @Test
    public void ensureGetForPersonCallsCorrectDAOMethod() {

        Person person = TestDataCreator.createPerson();

        overtimeService.getOvertimeRecordsForPerson(person);

        Mockito.verify(overtimeDAO).findByPerson(person);
    }
}
