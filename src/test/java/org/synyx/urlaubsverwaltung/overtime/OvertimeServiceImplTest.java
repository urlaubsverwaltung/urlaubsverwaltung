package org.synyx.urlaubsverwaltung.overtime;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class OvertimeServiceImplTest {

    private OvertimeService sut;

    private OvertimeDAO overtimeDAO;
    private OvertimeCommentDAO commentDAO;
    private ApplicationService applicationService;
    private OvertimeMailService overtimeMailService;

    private Overtime overtimeMock;
    private Person authorMock;

    @Before
    public void setUp() {

        commentDAO = mock(OvertimeCommentDAO.class);
        overtimeDAO = mock(OvertimeDAO.class);
        applicationService = mock(ApplicationService.class);
        overtimeMailService = mock(OvertimeMailService.class);

        sut = new OvertimeServiceImpl(overtimeDAO, commentDAO, applicationService, overtimeMailService);

        overtimeMock = mock(Overtime.class);
        authorMock = mock(Person.class);
    }


    // Record overtime -------------------------------------------------------------------------------------------------

    @Test
    public void ensurePersistsOvertimeAndComment() {

        sut.record(overtimeMock, Optional.of("Foo Bar"), authorMock);

        verify(overtimeDAO).save(overtimeMock);
        verify(commentDAO).save(any(OvertimeComment.class));
    }


    @Test
    public void ensureRecordingUpdatesLastModificationDate() {

        sut.record(overtimeMock, Optional.empty(), authorMock);

        verify(overtimeMock).onUpdate();
    }


    @Test
    public void ensureRecordingOvertimeSendsNotification() {

        sut.record(overtimeMock, Optional.of("Foo Bar"), authorMock);

        verify(overtimeMailService).sendOvertimeNotification(eq(overtimeMock), any(OvertimeComment.class));
    }


    @Test
    public void ensureCreatesCommentWithCorrectActionForNewOvertime() {

        when(overtimeMock.isNew()).thenReturn(true);

        ArgumentCaptor<OvertimeComment> commentCaptor = ArgumentCaptor.forClass(OvertimeComment.class);

        sut.record(overtimeMock, Optional.empty(), authorMock);

        verify(overtimeMock).isNew();
        verify(commentDAO).save(commentCaptor.capture());

        OvertimeComment comment = commentCaptor.getValue();

        Assert.assertNotNull("Should not be null", comment);
        Assert.assertEquals("Wrong action", OvertimeAction.CREATED, comment.getAction());
    }


    @Test
    public void ensureCreatesCommentWithCorrectActionForExistentOvertime() {

        when(overtimeMock.isNew()).thenReturn(false);

        ArgumentCaptor<OvertimeComment> commentCaptor = ArgumentCaptor.forClass(OvertimeComment.class);

        sut.record(overtimeMock, Optional.empty(), authorMock);

        verify(overtimeMock).isNew();
        verify(commentDAO).save(commentCaptor.capture());

        OvertimeComment comment = commentCaptor.getValue();

        Assert.assertNotNull("Should not be null", comment);
        Assert.assertEquals("Wrong action", OvertimeAction.EDITED, comment.getAction());
    }


    @Test
    public void ensureCreatedCommentWithoutTextHasCorrectProperties() {

        ArgumentCaptor<OvertimeComment> commentCaptor = ArgumentCaptor.forClass(OvertimeComment.class);

        sut.record(overtimeMock, Optional.empty(), authorMock);

        verify(commentDAO).save(commentCaptor.capture());

        OvertimeComment comment = commentCaptor.getValue();

        Assert.assertNotNull("Should not be null", comment);

        Assert.assertEquals("Wrong author", authorMock, comment.getPerson());
        Assert.assertEquals("Wrong overtime", overtimeMock, comment.getOvertime());

        Assert.assertNull("Text should not be set", comment.getText());
    }


    @Test
    public void ensureCreatedCommentWithTextHasCorrectProperties() {

        ArgumentCaptor<OvertimeComment> commentCaptor = ArgumentCaptor.forClass(OvertimeComment.class);

        sut.record(overtimeMock, Optional.of("Foo"), authorMock);

        verify(commentDAO).save(commentCaptor.capture());

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

        sut.getOvertimeRecordsForPerson(person);

        verify(overtimeDAO).findByPerson(person);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTryingToGetOvertimeForNullPerson() {

        sut.getOvertimeRecordsForPerson(null);
    }


    // Get overtime record by ID ---------------------------------------------------------------------------------------

    @Test
    public void ensureGetByIDCallsCorrectDAOMethod() {

        sut.getOvertimeById(42);

        verify(overtimeDAO).findById(42);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTryingToGetOvertimeByEmptyID() {

        sut.getOvertimeById(null);
    }


    @Test
    public void ensureReturnsEmptyOptionalIfNoOvertimeFoundForID() {

        when(overtimeDAO.findById(anyInt())).thenReturn(Optional.empty());

        Optional<Overtime> overtimeOptional = sut.getOvertimeById(42);

        Assert.assertNotNull("Should not be null", overtimeOptional);
        Assert.assertEquals("Should be empty", Optional.empty(), overtimeOptional);
    }


    // Get overtime records for person and year ------------------------------------------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTryingToGetRecordsByPersonAndYearWithNullPerson() {

        sut.getOvertimeRecordsForPersonAndYear(null, 2015);
    }


    @Test
    public void ensureGetRecordsByPersonAndYearCallsCorrectDAOMethod() {

        Person person = TestDataCreator.createPerson();

        sut.getOvertimeRecordsForPersonAndYear(person, 2015);

        Instant firstDay = Instant.from(LocalDate.of(2015, 1, 1));
        Instant lastDay = Instant.from(LocalDate.of(2015, 12, 31));

        verify(overtimeDAO).findByPersonAndPeriod(person, firstDay, lastDay);
    }


    // Get overtime comments -------------------------------------------------------------------------------------------

    @Test
    public void ensureGetCommentsCorrectDAOMethod() {

        Overtime overtime = mock(Overtime.class);

        sut.getCommentsForOvertime(overtime);

        verify(commentDAO).findByOvertime(overtime);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTryingToGetCommentsForNullOvertime() {

        sut.getCommentsForOvertime(null);
    }


    // Get total overtime for year -------------------------------------------------------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTryingToGetYearOvertimeForNullPerson() {

        sut.getTotalOvertimeForPersonAndYear(null, 2016);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTryingToGetYearOvertimeForNegativeYear() {

        sut.getTotalOvertimeForPersonAndYear(TestDataCreator.createPerson(), -1);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTryingToGetYearOvertimeForZeroYear() {

        sut.getTotalOvertimeForPersonAndYear(TestDataCreator.createPerson(), 0);
    }


    @Test
    public void ensureReturnsZeroIfPersonHasNoOvertimeRecordsYetForTheGivenYear() {

        Person person = TestDataCreator.createPerson();

        when(overtimeDAO.findByPersonAndPeriod(eq(person), any(Instant.class), any(Instant.class)))
            .thenReturn(Collections.emptyList());

        BigDecimal totalHours = sut.getTotalOvertimeForPersonAndYear(person, 2016);

        Instant firstDayOfYear = Instant.from(LocalDate.of(2016, 1, 1));
        Instant lastDayOfYear = Instant.from(LocalDate.of(2016, 12, 31));

        verify(overtimeDAO).findByPersonAndPeriod(person, firstDayOfYear, lastDayOfYear);

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

        when(overtimeDAO.findByPersonAndPeriod(eq(person), any(Instant.class), any(Instant.class)))
            .thenReturn(Arrays.asList(overtimeRecord, otherOvertimeRecord));

        BigDecimal totalHours = sut.getTotalOvertimeForPersonAndYear(person, 2016);

        Instant firstDayOfYear = Instant.from(LocalDate.of(2016, 1, 1));
        Instant lastDayOfYear = Instant.from(LocalDate.of(2016, 12, 31));

        verify(overtimeDAO).findByPersonAndPeriod(person, firstDayOfYear, lastDayOfYear);

        Assert.assertNotNull("Should not be null", totalHours);
        Assert.assertEquals("Wrong total overtime", new BigDecimal("11"), totalHours);
    }


    // Get left overtime -----------------------------------------------------------------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTryingToGetLeftOvertimeForNullPerson() {

        sut.getLeftOvertimeForPerson(null);
    }


    @Test
    public void ensureReturnsZeroAsLeftOvertimeIfPersonHasNoOvertimeRecordsYet() {

        Person person = TestDataCreator.createPerson();

        when(overtimeDAO.calculateTotalHoursForPerson(person)).thenReturn(null);
        when(applicationService.getTotalOvertimeReductionOfPerson(person)).thenReturn(BigDecimal.ZERO);

        BigDecimal totalHours = sut.getLeftOvertimeForPerson(person);

        verify(overtimeDAO).calculateTotalHoursForPerson(person);
        verify(applicationService).getTotalOvertimeReductionOfPerson(person);

        Assert.assertNotNull("Should not be null", totalHours);
        Assert.assertEquals("Wrong total overtime", BigDecimal.ZERO, totalHours);
    }


    @Test
    public void ensureTheLeftOvertimeIsTheDifferenceBetweenTotalOvertimeAndOvertimeReduction() {

        Person person = TestDataCreator.createPerson();

        when(overtimeDAO.calculateTotalHoursForPerson(person)).thenReturn(BigDecimal.TEN);
        when(applicationService.getTotalOvertimeReductionOfPerson(person)).thenReturn(BigDecimal.ONE);

        BigDecimal leftOvertime = sut.getLeftOvertimeForPerson(person);

        verify(overtimeDAO).calculateTotalHoursForPerson(person);
        verify(applicationService).getTotalOvertimeReductionOfPerson(person);

        Assert.assertNotNull("Should not be null", leftOvertime);
        Assert.assertEquals("Wrong left overtime", new BigDecimal("9"), leftOvertime);
    }


    @Test
    public void ensureTheLeftOvertimeIsZeroIfPersonHasNeitherOvertimeRecordsNorOvertimeReduction() {

        Person person = TestDataCreator.createPerson();

        when(overtimeDAO.calculateTotalHoursForPerson(person)).thenReturn(null);
        when(applicationService.getTotalOvertimeReductionOfPerson(person)).thenReturn(BigDecimal.ZERO);

        BigDecimal leftOvertime = sut.getLeftOvertimeForPerson(person);

        Assert.assertNotNull("Should not be null", leftOvertime);
        Assert.assertEquals("Wrong left overtime", BigDecimal.ZERO, leftOvertime);
    }
}
