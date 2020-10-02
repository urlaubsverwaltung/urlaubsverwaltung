package org.synyx.urlaubsverwaltung.overtime;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.TestDataCreator;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class OvertimeServiceImplTest {

    private OvertimeService sut;

    private OvertimeRepository overtimeRepository;
    private OvertimeCommentRepository commentDAO;
    private ApplicationService applicationService;
    private OvertimeMailService overtimeMailService;

    private Overtime overtimeMock;
    private Person authorMock;

    @BeforeEach
    void setUp() {

        commentDAO = mock(OvertimeCommentRepository.class);
        overtimeRepository = mock(OvertimeRepository.class);
        applicationService = mock(ApplicationService.class);
        overtimeMailService = mock(OvertimeMailService.class);

        sut = new OvertimeServiceImpl(overtimeRepository, commentDAO, applicationService, overtimeMailService, Clock.systemUTC());

        overtimeMock = mock(Overtime.class);
        authorMock = mock(Person.class);
    }


    // Record overtime -------------------------------------------------------------------------------------------------

    @Test
    void ensurePersistsOvertimeAndComment() {

        sut.record(overtimeMock, Optional.of("Foo Bar"), authorMock);

        verify(overtimeRepository).save(overtimeMock);
        verify(commentDAO).save(any(OvertimeComment.class));
    }


    @Test
    void ensureRecordingUpdatesLastModificationDate() {

        sut.record(overtimeMock, Optional.empty(), authorMock);

        verify(overtimeMock).onUpdate();
    }


    @Test
    void ensureRecordingOvertimeSendsNotification() {

        sut.record(overtimeMock, Optional.of("Foo Bar"), authorMock);

        verify(overtimeMailService).sendOvertimeNotification(eq(overtimeMock), any(OvertimeComment.class));
    }


    @Test
    void ensureCreatesCommentWithCorrectActionForNewOvertime() {

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
    void ensureCreatesCommentWithCorrectActionForExistentOvertime() {

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
    void ensureCreatedCommentWithoutTextHasCorrectProperties() {

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
    void ensureCreatedCommentWithTextHasCorrectProperties() {

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
    void ensureGetForPersonCallsCorrectDAOMethod() {

        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        sut.getOvertimeRecordsForPerson(person);

        verify(overtimeRepository).findByPerson(person);
    }

    @Test
    void ensureThrowsIfTryingToGetOvertimeForNullPerson() {
        assertThatIllegalArgumentException().isThrownBy(() -> sut.getOvertimeRecordsForPerson(null));
    }


    // Get overtime record by ID ---------------------------------------------------------------------------------------

    @Test
    void ensureGetByIDCallsCorrectDAOMethod() {

        sut.getOvertimeById(42);

        verify(overtimeRepository).findById(42);
    }


    @Test
    void ensureThrowsIfTryingToGetOvertimeByEmptyID() {
        assertThatIllegalArgumentException().isThrownBy(() -> sut.getOvertimeById(null));
    }


    @Test
    void ensureReturnsEmptyOptionalIfNoOvertimeFoundForID() {

        when(overtimeRepository.findById(anyInt())).thenReturn(Optional.empty());

        Optional<Overtime> overtimeOptional = sut.getOvertimeById(42);

        Assert.assertNotNull("Should not be null", overtimeOptional);
        Assert.assertEquals("Should be empty", Optional.empty(), overtimeOptional);
    }


    // Get overtime records for person and year ------------------------------------------------------------------------

    @Test
    void ensureThrowsIfTryingToGetRecordsByPersonAndYearWithNullPerson() {
        assertThatIllegalArgumentException().isThrownBy(() -> sut.getOvertimeRecordsForPersonAndYear(null, 2015));
    }


    @Test
    void ensureGetRecordsByPersonAndYearCallsCorrectDAOMethod() {

        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        sut.getOvertimeRecordsForPersonAndYear(person, 2015);

        LocalDate firstDay = LocalDate.of(2015, 1, 1);
        LocalDate lastDay = LocalDate.of(2015, 12, 31);

        verify(overtimeRepository).findByPersonAndPeriod(person, firstDay, lastDay);
    }


    // Get overtime comments -------------------------------------------------------------------------------------------
    @Test
    void ensureGetCommentsCorrectDAOMethod() {

        Overtime overtime = mock(Overtime.class);

        sut.getCommentsForOvertime(overtime);

        verify(commentDAO).findByOvertime(overtime);
    }

    @Test
    void ensureThrowsIfTryingToGetCommentsForNullOvertime() {
        assertThatIllegalArgumentException().isThrownBy(() -> sut.getCommentsForOvertime(null));
    }

    // Get total overtime for year -------------------------------------------------------------------------------------
    @Test
    void ensureThrowsIfTryingToGetYearOvertimeForNullPerson() {
        assertThatIllegalArgumentException().isThrownBy(() -> sut.getTotalOvertimeForPersonAndYear(null, 2016));
    }

    @Test
    void ensureThrowsIfTryingToGetYearOvertimeForNegativeYear() {
        assertThatIllegalArgumentException().isThrownBy(() -> sut.getTotalOvertimeForPersonAndYear(new Person("muster", "Muster", "Marlene", "muster@example.org"), -1));
    }

    @Test
    void ensureThrowsIfTryingToGetYearOvertimeForZeroYear() {
        assertThatIllegalArgumentException().isThrownBy(() -> sut.getTotalOvertimeForPersonAndYear(new Person("muster", "Muster", "Marlene", "muster@example.org"), 0));
    }


    @Test
    void ensureReturnsZeroIfPersonHasNoOvertimeRecordsYetForTheGivenYear() {

        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        when(overtimeRepository.findByPersonAndPeriod(eq(person), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(Collections.emptyList());

        BigDecimal totalHours = sut.getTotalOvertimeForPersonAndYear(person, 2016);

        LocalDate firstDayOfYear = LocalDate.of(2016, 1, 1);
        LocalDate lastDayOfYear = LocalDate.of(2016, 12, 31);

        verify(overtimeRepository).findByPersonAndPeriod(person, firstDayOfYear, lastDayOfYear);

        Assert.assertNotNull("Should not be null", totalHours);
        Assert.assertEquals("Wrong total overtime", BigDecimal.ZERO, totalHours);
    }


    @Test
    void ensureReturnsCorrectYearOvertimeForPerson() {

        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        Overtime overtimeRecord = TestDataCreator.createOvertimeRecord(person);
        overtimeRecord.setHours(BigDecimal.ONE);

        Overtime otherOvertimeRecord = TestDataCreator.createOvertimeRecord(person);
        otherOvertimeRecord.setHours(BigDecimal.TEN);

        when(overtimeRepository.findByPersonAndPeriod(eq(person), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(Arrays.asList(overtimeRecord, otherOvertimeRecord));

        BigDecimal totalHours = sut.getTotalOvertimeForPersonAndYear(person, 2016);

        LocalDate firstDayOfYear = LocalDate.of(2016, 1, 1);
        LocalDate lastDayOfYear = LocalDate.of(2016, 12, 31);

        verify(overtimeRepository).findByPersonAndPeriod(person, firstDayOfYear, lastDayOfYear);

        Assert.assertNotNull("Should not be null", totalHours);
        Assert.assertEquals("Wrong total overtime", new BigDecimal("11"), totalHours);
    }


    // Get left overtime -----------------------------------------------------------------------------------------------
    @Test
    void ensureThrowsIfTryingToGetLeftOvertimeForNullPerson() {
        assertThatIllegalArgumentException().isThrownBy(() -> sut.getLeftOvertimeForPerson(null));
    }

    @Test
    void ensureReturnsZeroAsLeftOvertimeIfPersonHasNoOvertimeRecordsYet() {

        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        when(overtimeRepository.calculateTotalHoursForPerson(person)).thenReturn(null);
        when(applicationService.getTotalOvertimeReductionOfPerson(person)).thenReturn(BigDecimal.ZERO);

        BigDecimal totalHours = sut.getLeftOvertimeForPerson(person);

        verify(overtimeRepository).calculateTotalHoursForPerson(person);
        verify(applicationService).getTotalOvertimeReductionOfPerson(person);

        Assert.assertNotNull("Should not be null", totalHours);
        Assert.assertEquals("Wrong total overtime", BigDecimal.ZERO, totalHours);
    }


    @Test
    void ensureTheLeftOvertimeIsTheDifferenceBetweenTotalOvertimeAndOvertimeReduction() {

        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        when(overtimeRepository.calculateTotalHoursForPerson(person)).thenReturn(BigDecimal.TEN);
        when(applicationService.getTotalOvertimeReductionOfPerson(person)).thenReturn(BigDecimal.ONE);

        BigDecimal leftOvertime = sut.getLeftOvertimeForPerson(person);

        verify(overtimeRepository).calculateTotalHoursForPerson(person);
        verify(applicationService).getTotalOvertimeReductionOfPerson(person);

        Assert.assertNotNull("Should not be null", leftOvertime);
        Assert.assertEquals("Wrong left overtime", new BigDecimal("9"), leftOvertime);
    }


    @Test
    void ensureTheLeftOvertimeIsZeroIfPersonHasNeitherOvertimeRecordsNorOvertimeReduction() {

        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        when(overtimeRepository.calculateTotalHoursForPerson(person)).thenReturn(null);
        when(applicationService.getTotalOvertimeReductionOfPerson(person)).thenReturn(BigDecimal.ZERO);

        BigDecimal leftOvertime = sut.getLeftOvertimeForPerson(person);

        Assert.assertNotNull("Should not be null", leftOvertime);
        Assert.assertEquals("Wrong left overtime", BigDecimal.ZERO, leftOvertime);
    }
}
