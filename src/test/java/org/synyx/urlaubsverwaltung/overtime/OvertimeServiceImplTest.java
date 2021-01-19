package org.synyx.urlaubsverwaltung.overtime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.TestDataCreator;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OvertimeServiceImplTest {

    private OvertimeServiceImpl sut;

    @Mock
    private OvertimeRepository overtimeRepository;
    @Mock
    private OvertimeCommentRepository commentDAO;
    @Mock
    private ApplicationService applicationService;
    @Mock
    private OvertimeMailService overtimeMailService;

    @BeforeEach
    void setUp() {
        sut = new OvertimeServiceImpl(overtimeRepository, commentDAO, applicationService, overtimeMailService, Clock.systemUTC());
    }

    // Record overtime -------------------------------------------------------------------------------------------------
    @Test
    void ensurePersistsOvertimeAndComment() {

        final Overtime overtime = new Overtime();
        final Person author = new Person();

        sut.record(overtime, Optional.of("Foo Bar"), author);

        verify(overtimeRepository).save(overtime);
        verify(commentDAO).save(any(OvertimeComment.class));
    }

    @Test
    void ensureRecordingUpdatesLastModificationDate() {

        final Person author = new Person();

        final Overtime overtime = sut.record(new Overtime(), Optional.empty(), author);
        assertThat(overtime.getLastModificationDate()).isToday();
    }

    @Test
    void ensureRecordingOvertimeSendsNotification() {

        final Overtime overtime = new Overtime();
        final Person author = new Person();

        sut.record(overtime, Optional.of("Foo Bar"), author);

        verify(overtimeMailService).sendOvertimeNotification(eq(overtime), any(OvertimeComment.class));
    }

    @Test
    void ensureCreatesCommentWithCorrectActionForNewOvertime() {

        final Overtime overtime = new Overtime();
        final Person author = new Person();

        sut.record(overtime, Optional.empty(), author);

        final ArgumentCaptor<OvertimeComment> commentCaptor = ArgumentCaptor.forClass(OvertimeComment.class);
        verify(commentDAO).save(commentCaptor.capture());

        final OvertimeComment comment = commentCaptor.getValue();
        assertThat(comment).isNotNull();
        assertThat(comment.getAction()).isEqualTo(OvertimeAction.CREATED);
    }

    @Test
    void ensureCreatesCommentWithCorrectActionForExistentOvertime() {

        final Overtime overtime = new Overtime();
        overtime.setId(1);
        final Person author = new Person();

        sut.record(overtime, Optional.empty(), author);

        final ArgumentCaptor<OvertimeComment> commentCaptor = ArgumentCaptor.forClass(OvertimeComment.class);
        verify(commentDAO).save(commentCaptor.capture());
        final OvertimeComment comment = commentCaptor.getValue();
        assertThat(comment).isNotNull();
        assertThat(comment.getAction()).isEqualTo(OvertimeAction.EDITED);
    }

    @Test
    void ensureCreatedCommentWithoutTextHasCorrectProperties() {

        final Overtime overtime = new Overtime();
        final Person author = new Person();

        sut.record(overtime, Optional.empty(), author);

        final ArgumentCaptor<OvertimeComment> commentCaptor = ArgumentCaptor.forClass(OvertimeComment.class);
        verify(commentDAO).save(commentCaptor.capture());
        final OvertimeComment comment = commentCaptor.getValue();
        assertThat(comment).isNotNull();
        assertThat(comment.getPerson()).isEqualTo(author);
        assertThat(comment.getOvertime()).isEqualTo(overtime);
        assertThat(comment.getText()).isNull();
    }

    @Test
    void ensureCreatedCommentWithTextHasCorrectProperties() {

        final Overtime overtime = new Overtime();
        final Person author = new Person();

        sut.record(overtime, Optional.of("Foo"), author);

        final ArgumentCaptor<OvertimeComment> commentCaptor = ArgumentCaptor.forClass(OvertimeComment.class);
        verify(commentDAO).save(commentCaptor.capture());
        final OvertimeComment comment = commentCaptor.getValue();
        assertThat(comment).isNotNull();
        assertThat(comment.getPerson()).isEqualTo(author);
        assertThat(comment.getOvertime()).isEqualTo(overtime);
        assertThat(comment.getText()).isEqualTo("Foo");
    }

    // Get overtime records for person ---------------------------------------------------------------------------------
    @Test
    void ensureGetForPersonCallsCorrectDAOMethod() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

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

        final Optional<Overtime> maybeOvertime = sut.getOvertimeById(42);
        assertThat(maybeOvertime).isEmpty();
    }

    // Get overtime records for person and year ------------------------------------------------------------------------
    @Test
    void ensureThrowsIfTryingToGetRecordsByPersonAndYearWithNullPerson() {
        assertThatIllegalArgumentException().isThrownBy(() -> sut.getOvertimeRecordsForPersonAndYear(null, 2015));
    }

    @Test
    void ensureGetRecordsByPersonAndYearCallsCorrectDAOMethod() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        sut.getOvertimeRecordsForPersonAndYear(person, 2015);

        final LocalDate firstDay = LocalDate.of(2015, 1, 1);
        final LocalDate lastDay = LocalDate.of(2015, 12, 31);
        verify(overtimeRepository).findByPersonAndPeriod(person, firstDay, lastDay);
    }

    // Get overtime comments -------------------------------------------------------------------------------------------
    @Test
    void ensureGetCommentsCorrectDAOMethod() {

        final Overtime overtime = new Overtime();
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

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        when(overtimeRepository.findByPersonAndPeriod(eq(person), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(Collections.emptyList());

        final BigDecimal totalHours = sut.getTotalOvertimeForPersonAndYear(person, 2016);
        assertThat(totalHours).isEqualTo(BigDecimal.ZERO);

        final LocalDate firstDayOfYear = LocalDate.of(2016, 1, 1);
        final LocalDate lastDayOfYear = LocalDate.of(2016, 12, 31);
        verify(overtimeRepository).findByPersonAndPeriod(person, firstDayOfYear, lastDayOfYear);
    }

    @Test
    void ensureReturnsCorrectYearOvertimeForPerson() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final Overtime overtimeRecord = TestDataCreator.createOvertimeRecord(person);
        overtimeRecord.setHours(BigDecimal.ONE);

        final Overtime otherOvertimeRecord = TestDataCreator.createOvertimeRecord(person);
        otherOvertimeRecord.setHours(BigDecimal.TEN);

        when(overtimeRepository.findByPersonAndPeriod(eq(person), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(List.of(overtimeRecord, otherOvertimeRecord));

        final BigDecimal totalHours = sut.getTotalOvertimeForPersonAndYear(person, 2016);
        assertThat(totalHours).isEqualTo(new BigDecimal("11"));

        final LocalDate firstDayOfYear = LocalDate.of(2016, 1, 1);
        final LocalDate lastDayOfYear = LocalDate.of(2016, 12, 31);
        verify(overtimeRepository).findByPersonAndPeriod(person, firstDayOfYear, lastDayOfYear);
    }

    // Get left overtime -----------------------------------------------------------------------------------------------
    @Test
    void ensureThrowsIfTryingToGetLeftOvertimeForNullPerson() {
        assertThatIllegalArgumentException().isThrownBy(() -> sut.getLeftOvertimeForPerson(null));
    }

    @Test
    void ensureReturnsZeroAsLeftOvertimeIfPersonHasNoOvertimeRecordsYet() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        when(overtimeRepository.calculateTotalHoursForPerson(person)).thenReturn(null);
        when(applicationService.getTotalOvertimeReductionOfPerson(person)).thenReturn(BigDecimal.ZERO);

        final BigDecimal totalHours = sut.getLeftOvertimeForPerson(person);
        assertThat(totalHours).isEqualTo(BigDecimal.ZERO);

        verify(overtimeRepository).calculateTotalHoursForPerson(person);
        verify(applicationService).getTotalOvertimeReductionOfPerson(person);
    }

    @Test
    void ensureTheLeftOvertimeIsTheDifferenceBetweenTotalOvertimeAndOvertimeReduction() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        when(overtimeRepository.calculateTotalHoursForPerson(person)).thenReturn(BigDecimal.TEN);
        when(applicationService.getTotalOvertimeReductionOfPerson(person)).thenReturn(BigDecimal.ONE);

        final BigDecimal leftOvertime = sut.getLeftOvertimeForPerson(person);
        assertThat(leftOvertime).isEqualTo(new BigDecimal("9"));

        verify(overtimeRepository).calculateTotalHoursForPerson(person);
        verify(applicationService).getTotalOvertimeReductionOfPerson(person);
    }

    @Test
    void ensureTheLeftOvertimeIsZeroIfPersonHasNeitherOvertimeRecordsNorOvertimeReduction() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        when(overtimeRepository.calculateTotalHoursForPerson(person)).thenReturn(null);
        when(applicationService.getTotalOvertimeReductionOfPerson(person)).thenReturn(BigDecimal.ZERO);

        final BigDecimal leftOvertime = sut.getLeftOvertimeForPerson(person);
        assertThat(leftOvertime).isEqualTo(BigDecimal.ZERO);
    }
}
