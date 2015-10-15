package org.synyx.urlaubsverwaltung.core.overtime;

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

    private Overtime overtimeMock;
    private Person authorMock;

    @Before
    public void setUp() {

        commentDAO = Mockito.mock(OvertimeCommentDAO.class);
        overtimeDAO = Mockito.mock(OvertimeDAO.class);

        overtimeService = new OvertimeServiceImpl(overtimeDAO, commentDAO);

        overtimeMock = Mockito.mock(Overtime.class);
        authorMock = Mockito.mock(Person.class);
    }


    // Record overtime -------------------------------------------------------------------------------------------------

    @Test
    public void ensurePersistsOvertimeAndComment() {

        overtimeService.record(overtimeMock, Optional.of("Foo Bar"), authorMock);

        Mockito.verify(overtimeDAO).save(overtimeMock);
        Mockito.verify(commentDAO).save(Mockito.any(OvertimeComment.class));
    }


    @Test
    public void ensureRecordingUpdatesLastModificationDate() {

        overtimeService.record(overtimeMock, Optional.empty(), authorMock);

        Mockito.verify(overtimeMock).onUpdate();
    }


    @Test
    public void ensureCreatesCommentWithCorrectActionForNewOvertime() {

        Mockito.when(overtimeMock.isNew()).thenReturn(true);

        ArgumentCaptor<OvertimeComment> commentCaptor = ArgumentCaptor.forClass(OvertimeComment.class);

        overtimeService.record(overtimeMock, Optional.empty(), authorMock);

        Mockito.verify(overtimeMock).isNew();
        Mockito.verify(commentDAO).save(commentCaptor.capture());

        OvertimeComment comment = commentCaptor.getValue();

        Assert.assertNotNull("Should not be null", comment);
        Assert.assertEquals("Wrong action", OvertimeAction.CREATED, comment.getAction());
    }


    @Test
    public void ensureCreatesCommentWithCorrectActionForExistentOvertime() {

        Mockito.when(overtimeMock.isNew()).thenReturn(false);

        ArgumentCaptor<OvertimeComment> commentCaptor = ArgumentCaptor.forClass(OvertimeComment.class);

        overtimeService.record(overtimeMock, Optional.empty(), authorMock);

        Mockito.verify(overtimeMock).isNew();
        Mockito.verify(commentDAO).save(commentCaptor.capture());

        OvertimeComment comment = commentCaptor.getValue();

        Assert.assertNotNull("Should not be null", comment);
        Assert.assertEquals("Wrong action", OvertimeAction.EDITED, comment.getAction());
    }


    @Test
    public void ensureCreatedCommentWithoutTextHasCorrectProperties() {

        ArgumentCaptor<OvertimeComment> commentCaptor = ArgumentCaptor.forClass(OvertimeComment.class);

        overtimeService.record(overtimeMock, Optional.empty(), authorMock);

        Mockito.verify(commentDAO).save(commentCaptor.capture());

        OvertimeComment comment = commentCaptor.getValue();

        Assert.assertNotNull("Should not be null", comment);

        Assert.assertEquals("Wrong author", authorMock, comment.getPerson());
        Assert.assertEquals("Wrong overtime", overtimeMock, comment.getOvertime());

        Assert.assertNull("Text should not be set", comment.getText());
    }


    @Test
    public void ensureCreatedCommentWithTextHasCorrectProperties() {

        ArgumentCaptor<OvertimeComment> commentCaptor = ArgumentCaptor.forClass(OvertimeComment.class);

        overtimeService.record(overtimeMock, Optional.of("Foo"), authorMock);

        Mockito.verify(commentDAO).save(commentCaptor.capture());

        OvertimeComment comment = commentCaptor.getValue();

        Assert.assertNotNull("Should not be null", comment);

        Assert.assertEquals("Wrong author", authorMock, comment.getPerson());
        Assert.assertEquals("Wrong overtime", overtimeMock, comment.getOvertime());
        Assert.assertEquals("Wrong text", "Foo", comment.getText());
    }


    // Get overtime records for person ---------------------------------------------------------------------------------

    @Test
    public void ensureGetForPersonCallsCorrectDAOMethod() {

        Person person = TestDataCreator.createPerson();

        overtimeService.getOvertimeRecordsForPerson(person);

        Mockito.verify(overtimeDAO).findByPerson(person);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTryingToGetOvertimeForNullPerson() {

        overtimeService.getOvertimeRecordsForPerson(null);
    }


    // Get overtime record by ID ---------------------------------------------------------------------------------------

    @Test
    public void ensureGetByIDCallsCorrectDAOMethod() {

        overtimeService.getOvertimeById(42);

        Mockito.verify(overtimeDAO).findOne(42);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTryingToGetOvertimeByEmptyID() {

        overtimeService.getOvertimeById(null);
    }


    @Test
    public void ensureReturnsEmptyOptionalIfNoOvertimeFoundForID() {

        Mockito.when(overtimeDAO.findOne(Mockito.anyInt())).thenReturn(null);

        Optional<Overtime> overtimeOptional = overtimeService.getOvertimeById(42);

        Assert.assertNotNull("Should not be null", overtimeOptional);
        Assert.assertEquals("Should be empty", Optional.empty(), overtimeOptional);
    }


    // Get overtime comments -------------------------------------------------------------------------------------------

    @Test
    public void ensureGetCommentsCorrectDAOMethod() {

        Overtime overtime = Mockito.mock(Overtime.class);

        overtimeService.getCommentsForOvertime(overtime);

        Mockito.verify(commentDAO).findByOvertime(overtime);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTryingToGetCommentsForNullOvertime() {

        overtimeService.getCommentsForOvertime(null);
    }


    // Get total overtime ----------------------------------------------------------------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTryingToGetTotalOvertimeForNullPerson() {

        overtimeService.getTotalOvertimeForPerson(null);
    }


    @Test
    public void ensureReturnsZeroIfPersonHasNoOvertimeRecordsYet() {

        Person person = TestDataCreator.createPerson();

        Mockito.when(overtimeDAO.calculateTotalHoursForPerson(person)).thenReturn(null);

        BigDecimal totalHours = overtimeService.getTotalOvertimeForPerson(person);

        Mockito.verify(overtimeDAO).calculateTotalHoursForPerson(person);

        Assert.assertNotNull("Should not be null", totalHours);
        Assert.assertEquals("Wrong total overtime", BigDecimal.ZERO, totalHours);
    }


    @Test
    public void ensureReturnsCorrectTotalOvertimeForPerson() {

        Person person = TestDataCreator.createPerson();

        Mockito.when(overtimeDAO.calculateTotalHoursForPerson(person)).thenReturn(BigDecimal.ONE);

        BigDecimal totalHours = overtimeService.getTotalOvertimeForPerson(person);

        Mockito.verify(overtimeDAO).calculateTotalHoursForPerson(person);

        Assert.assertNotNull("Should not be null", totalHours);
        Assert.assertEquals("Wrong total overtime", BigDecimal.ONE, totalHours);
    }
}
