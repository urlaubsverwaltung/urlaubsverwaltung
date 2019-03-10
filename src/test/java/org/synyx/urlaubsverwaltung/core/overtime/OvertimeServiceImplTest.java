package org.synyx.urlaubsverwaltung.core.overtime;

import org.joda.time.DateMidnight;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.core.mail.MailService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class OvertimeServiceImplTest {

    private OvertimeService overtimeService;

    private OvertimeDAO overtimeDAO;
    private OvertimeCommentDAO commentDAO;
    private ApplicationService applicationService;
    private MailService mailService;

    private Overtime overtimeMock;
    private Person authorMock;

    @Before
    public void setUp() {

        commentDAO = Mockito.mock(OvertimeCommentDAO.class);
        overtimeDAO = Mockito.mock(OvertimeDAO.class);
        applicationService = Mockito.mock(ApplicationService.class);
        mailService = Mockito.mock(MailService.class);

        overtimeService = new OvertimeServiceImpl(overtimeDAO, commentDAO, applicationService, mailService);

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
    public void ensureRecordingOvertimeSendsNotification() {

        overtimeService.record(overtimeMock, Optional.of("Foo Bar"), authorMock);

        Mockito.verify(mailService)
            .sendOvertimeNotification(Mockito.eq(overtimeMock), Mockito.any(OvertimeComment.class));
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


    // Get overtime records for person and year ------------------------------------------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTryingToGetRecordsByPersonAndYearWithNullPerson() {

        overtimeService.getOvertimeRecordsForPersonAndYear(null, 2015);
    }


    @Test
    public void ensureGetRecordsByPersonAndYearCallsCorrectDAOMethod() {

        Person person = TestDataCreator.createPerson();

        overtimeService.getOvertimeRecordsForPersonAndYear(person, 2015);

        DateMidnight firstDay = new DateMidnight(2015, 1, 1);
        DateMidnight lastDay = new DateMidnight(2015, 12, 31);

        Mockito.verify(overtimeDAO).findByPersonAndPeriod(person, firstDay.toDate(), lastDay.toDate());
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


    // Get total overtime for year -------------------------------------------------------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTryingToGetYearOvertimeForNullPerson() {

        overtimeService.getTotalOvertimeForPersonAndYear(null, 2016);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTryingToGetYearOvertimeForNegativeYear() {

        overtimeService.getTotalOvertimeForPersonAndYear(TestDataCreator.createPerson(), -1);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTryingToGetYearOvertimeForZeroYear() {

        overtimeService.getTotalOvertimeForPersonAndYear(TestDataCreator.createPerson(), 0);
    }


    @Test
    public void ensureReturnsZeroIfPersonHasNoOvertimeRecordsYetForTheGivenYear() {

        Person person = TestDataCreator.createPerson();

        Mockito.when(overtimeDAO.findByPersonAndPeriod(Mockito.eq(person), Mockito.any(Date.class),
                    Mockito.any(Date.class)))
            .thenReturn(Collections.emptyList());

        BigDecimal totalHours = overtimeService.getTotalOvertimeForPersonAndYear(person, 2016);

        DateMidnight firstDayOfYear = new DateMidnight(2016, 1, 1);
        DateMidnight lastDayOfYear = new DateMidnight(2016, 12, 31);

        Mockito.verify(overtimeDAO).findByPersonAndPeriod(person, firstDayOfYear.toDate(), lastDayOfYear.toDate());

        Assert.assertNotNull("Should not be null", totalHours);
        Assert.assertEquals("Wrong total overtime", BigDecimal.ZERO, totalHours);
    }


    @Test
    public void ensureReturnsCorrectYearOvertimeForPerson() {

        Person person = TestDataCreator.createPerson();

        Overtime overtimeRecord = TestDataCreator.createOvertimeRecord(person);
        overtimeRecord.setHours(BigDecimal.ONE);

        Overtime otherOvertimeRecord = TestDataCreator.createOvertimeRecord(person);
        otherOvertimeRecord.setHours(BigDecimal.TEN);

        Mockito.when(overtimeDAO.findByPersonAndPeriod(Mockito.eq(person), Mockito.any(Date.class),
                    Mockito.any(Date.class)))
            .thenReturn(Arrays.asList(overtimeRecord, otherOvertimeRecord));

        BigDecimal totalHours = overtimeService.getTotalOvertimeForPersonAndYear(person, 2016);

        DateMidnight firstDayOfYear = new DateMidnight(2016, 1, 1);
        DateMidnight lastDayOfYear = new DateMidnight(2016, 12, 31);

        Mockito.verify(overtimeDAO).findByPersonAndPeriod(person, firstDayOfYear.toDate(), lastDayOfYear.toDate());

        Assert.assertNotNull("Should not be null", totalHours);
        Assert.assertEquals("Wrong total overtime", new BigDecimal("11"), totalHours);
    }


    // Get left overtime -----------------------------------------------------------------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTryingToGetLeftOvertimeForNullPerson() {

        overtimeService.getLeftOvertimeForPerson(null);
    }


    @Test
    public void ensureReturnsZeroAsLeftOvertimeIfPersonHasNoOvertimeRecordsYet() {

        Person person = TestDataCreator.createPerson();

        Mockito.when(overtimeDAO.calculateTotalHoursForPerson(person)).thenReturn(null);
        Mockito.when(applicationService.getTotalOvertimeReductionOfPerson(person)).thenReturn(BigDecimal.ZERO);

        BigDecimal totalHours = overtimeService.getLeftOvertimeForPerson(person);

        Mockito.verify(overtimeDAO).calculateTotalHoursForPerson(person);
        Mockito.verify(applicationService).getTotalOvertimeReductionOfPerson(person);

        Assert.assertNotNull("Should not be null", totalHours);
        Assert.assertEquals("Wrong total overtime", BigDecimal.ZERO, totalHours);
    }


    @Test
    public void ensureTheLeftOvertimeIsTheDifferenceBetweenTotalOvertimeAndOvertimeReduction() {

        Person person = TestDataCreator.createPerson();

        Mockito.when(overtimeDAO.calculateTotalHoursForPerson(person)).thenReturn(BigDecimal.TEN);
        Mockito.when(applicationService.getTotalOvertimeReductionOfPerson(person)).thenReturn(BigDecimal.ONE);

        BigDecimal leftOvertime = overtimeService.getLeftOvertimeForPerson(person);

        Mockito.verify(overtimeDAO).calculateTotalHoursForPerson(person);
        Mockito.verify(applicationService).getTotalOvertimeReductionOfPerson(person);

        Assert.assertNotNull("Should not be null", leftOvertime);
        Assert.assertEquals("Wrong left overtime", new BigDecimal("9"), leftOvertime);
    }


    @Test
    public void ensureTheLeftOvertimeIsZeroIfPersonHasNeitherOvertimeRecordsNorOvertimeReduction() {

        Person person = TestDataCreator.createPerson();

        Mockito.when(overtimeDAO.calculateTotalHoursForPerson(person)).thenReturn(null);
        Mockito.when(applicationService.getTotalOvertimeReductionOfPerson(person)).thenReturn(BigDecimal.ZERO);

        BigDecimal leftOvertime = overtimeService.getLeftOvertimeForPerson(person);

        Assert.assertNotNull("Should not be null", leftOvertime);
        Assert.assertEquals("Wrong left overtime", BigDecimal.ZERO, leftOvertime);
    }
}
