package org.synyx.urlaubsverwaltung.sicknote.sicknote.extend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.extend.SickNoteExtensionStatus.ACCEPTED;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.extend.SickNoteExtensionStatus.SUBMITTED;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.extend.SickNoteExtensionStatus.SUPERSEDED;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation.WorkingTimeCalendarEntryType.WORKDAY;

@ExtendWith(MockitoExtension.class)
class SickNoteExtensionServiceImplTest {

    private SickNoteExtensionServiceImpl sut;

    @Mock
    private SickNoteExtensionRepository repository;
    @Mock
    private SickNoteService sickNoteService;
    @Mock
    private WorkingTimeCalendarService workingTimeCalendarService;

    private final Clock clock = Clock.systemUTC();

    @BeforeEach
    void setUp() {
        sut = new SickNoteExtensionServiceImpl(repository, sickNoteService, workingTimeCalendarService, clock);
    }

    @Nested
    class FindSubmittedExtensionOfSickNote {

        @Test
        void ensureRecentSickNoteExtensionIsEmptyBecauseNoExtensions() {

            when(repository.findAllBySickNoteIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());

            final SickNote sickNote = SickNote.builder().id(1L).build();

            final Optional<SickNoteExtension> actual = sut.findSubmittedExtensionOfSickNote(sickNote);
            assertThat(actual).isEmpty();
        }

        @Test
        void ensureRecentSickNoteExtensionIsEmptyBecauseNoExtensionsSubmitted() {

            final SickNoteExtensionEntity entity = new SickNoteExtensionEntity();
            entity.setId(1L);
            entity.setSickNoteId(1L);
            entity.setStatus(ACCEPTED);

            when(repository.findAllBySickNoteIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(entity));

            final SickNote sickNote = SickNote.builder().id(1L).build();

            final Optional<SickNoteExtension> actual = sut.findSubmittedExtensionOfSickNote(sickNote);
            assertThat(actual).isEmpty();
        }

        @Test
        void ensureRecentSickNoteExtensionThrowsWhenWorkingTimeCalendarCannotBeFound() {

            final Person person = new Person();
            person.setId(1L);

            final SickNoteExtensionEntity entity = new SickNoteExtensionEntity();
            entity.setId(1L);
            entity.setSickNoteId(1L);
            entity.setNewEndDate(LocalDate.of(2024, 8, 23));
            entity.setStatus(SUBMITTED);

            when(repository.findAllBySickNoteIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(entity));

            final SickNote sickNote = SickNote.builder()
                .id(1L)
                .person(person)
                .startDate(LocalDate.of(2024, 8, 21))
                .endDate(LocalDate.of(2024, 8, 22))
                .build();

            final DateRange dateRange = new DateRange(sickNote.getStartDate(), entity.getNewEndDate());
            when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), dateRange)).thenReturn(Map.of());

            assertThatThrownBy(() -> sut.findSubmittedExtensionOfSickNote(sickNote))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("expected workingTimeCalender of person=1 to exist for dateRange=DateRange[startDate=2024-08-21, endDate=2024-08-23]");
        }

        @Test
        void ensureRecentSickNoteExtension() {

            final Person person = new Person();
            person.setId(1L);

            final SickNoteExtensionEntity entity = new SickNoteExtensionEntity();
            entity.setId(1L);
            entity.setSickNoteId(1L);
            entity.setNewEndDate(LocalDate.of(2024, 8, 23));
            entity.setStatus(SUBMITTED);

            when(repository.findAllBySickNoteIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(entity));

            final SickNote sickNote = SickNote.builder()
                .id(1L)
                .person(person)
                .startDate(LocalDate.of(2024, 8, 21))
                .endDate(LocalDate.of(2024, 8, 22))
                .build();

            final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(Map.of(
                LocalDate.of(2024, 8, 21), new WorkingTimeCalendar.WorkingDayInformation(DayLength.FULL, WORKDAY, WORKDAY),
                LocalDate.of(2024, 8, 22), new WorkingTimeCalendar.WorkingDayInformation(DayLength.FULL, WORKDAY, WORKDAY),
                LocalDate.of(2024, 8, 23), new WorkingTimeCalendar.WorkingDayInformation(DayLength.FULL, WORKDAY, WORKDAY)
            ));

            final DateRange dateRange = new DateRange(sickNote.getStartDate(), entity.getNewEndDate());
            when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), dateRange))
                .thenReturn(Map.of(person, workingTimeCalendar));

            final Optional<SickNoteExtension> actual = sut.findSubmittedExtensionOfSickNote(sickNote);
            assertThat(actual).hasValueSatisfying(extension -> {
                assertThat(extension.id()).isEqualTo(1L);
                assertThat(extension.nextEndDate()).isEqualTo(LocalDate.of(2024, 8, 23));
                assertThat(extension.status()).isEqualTo(SUBMITTED);
                assertThat(extension.additionalWorkdays()).isEqualTo(BigDecimal.ONE);
            });
        }
    }

    @Nested
    class UpdateExtensionsForConvertedSickNote {

        @ParameterizedTest
        @EnumSource(value = SickNoteStatus.class, names = {"CONVERTED_TO_VACATION", "CANCELLED"}, mode = EnumSource.Mode.EXCLUDE)
        void ensureUpdateForStatusDoesNothing(SickNoteStatus sickNoteStatus) {

            final SickNote sickNote = SickNote.builder()
                .status(sickNoteStatus)
                .build();

            sut.updateExtensionsForConvertedSickNote(sickNote);
            verifyNoInteractions(repository);
        }

        @ParameterizedTest
        @EnumSource(value = SickNoteStatus.class, names = {"CONVERTED_TO_VACATION", "CANCELLED"}, mode = EnumSource.Mode.INCLUDE)
        void ensureUpdateForStatusDoesNothingBecauseNotExtensionAvailable(SickNoteStatus sickNoteStatus) {

            final Person person = new Person();
            person.setId(1L);

            final SickNote sickNote = SickNote.builder()
                .id(1L)
                .person(person)
                .status(sickNoteStatus)
                .build();

            when(repository.findAllBySickNoteIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());

            sut.updateExtensionsForConvertedSickNote(sickNote);
            verifyNoMoreInteractions(repository);
        }

        @ParameterizedTest
        @EnumSource(value = SickNoteStatus.class, names = {"CONVERTED_TO_VACATION", "CANCELLED"}, mode = EnumSource.Mode.INCLUDE)
        void ensureUpdateForStatusDoesNothingBecauseNotExtensionSUBMITTED(SickNoteStatus sickNoteStatus) {

            final Person person = new Person();
            person.setId(1L);

            final SickNote sickNote = SickNote.builder()
                .id(1L)
                .person(person)
                .status(sickNoteStatus)
                .build();

            final SickNoteExtensionEntity extensionEntity = new SickNoteExtensionEntity();
            extensionEntity.setId(1L);
            extensionEntity.setStatus(ACCEPTED);

            when(repository.findAllBySickNoteIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(extensionEntity));

            sut.updateExtensionsForConvertedSickNote(sickNote);
            verifyNoMoreInteractions(repository);
        }

        @ParameterizedTest
        @EnumSource(value = SickNoteStatus.class, names = {"CONVERTED_TO_VACATION", "CANCELLED"}, mode = EnumSource.Mode.INCLUDE)
        void ensureUpdateForStatus(SickNoteStatus sickNoteStatus) {

            final Person person = new Person();
            person.setId(1L);

            final SickNote sickNote = SickNote.builder()
                .id(1L)
                .person(person)
                .status(sickNoteStatus)
                .build();

            final SickNoteExtensionEntity extensionEntity = new SickNoteExtensionEntity();
            extensionEntity.setId(1L);
            extensionEntity.setStatus(SUBMITTED);

            when(repository.findAllBySickNoteIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(extensionEntity));

            sut.updateExtensionsForConvertedSickNote(sickNote);

            final ArgumentCaptor<List<SickNoteExtensionEntity>> captor = ArgumentCaptor.forClass(List.class);
            verify(repository).saveAll(captor.capture());

            assertThat(captor.getValue()).satisfiesExactly(
                entity -> {
                    assertThat(entity.getId()).isEqualTo(1L);
                    assertThat(entity.getStatus()).isEqualTo(SUPERSEDED);
                }
            );
        }
    }

    @Nested
    class CreateSickNoteExtension {

        @Test
        void ensureUpdatesExistingSubmittedExtension() {

            final Person person = new Person();
            person.setId(1L);

            final LocalDate startDate = LocalDate.of(2024, 8, 21);
            final LocalDate endDate = LocalDate.of(2024, 8, 22);
            final LocalDate nextEndDate = LocalDate.of(2024, 8, 23);

            final SickNoteExtensionEntity existingEntity = new SickNoteExtensionEntity();
            existingEntity.setId(1L);
            existingEntity.setSickNoteId(1L);
            existingEntity.setStatus(SUBMITTED);

            when(repository.findAllBySickNoteIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(existingEntity));

            final WorkingTimeCalendar workingTimeCalendar = mock(WorkingTimeCalendar.class);
            when(workingTimeCalendar.workingTime(endDate, nextEndDate)).thenReturn(BigDecimal.valueOf(3));

            when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), new DateRange(endDate, nextEndDate)))
                .thenReturn(Map.of(person, workingTimeCalendar));

            when(repository.save(any(SickNoteExtensionEntity.class))).thenAnswer(returnsFirstArg());

            final SickNote sickNote = SickNote.builder()
                .id(1L)
                .person(person)
                .startDate(startDate)
                .endDate(endDate)
                .build();

            final SickNoteExtension actual = sut.createSickNoteExtension(sickNote, nextEndDate);
            assertThat(actual.id()).isEqualTo(1L);
            assertThat(actual.sickNoteId()).isEqualTo(1L);
            assertThat(actual.nextEndDate()).isEqualTo(nextEndDate);
            assertThat(actual.status()).isEqualTo(SUBMITTED);
            assertThat(actual.additionalWorkdays()).isEqualTo(BigDecimal.valueOf(3));
        }

        @Test
        void ensureCreatesNewEntityWhenRecentIsAccepted() {

            final Person person = new Person();
            person.setId(1L);

            final LocalDate startDate = LocalDate.of(2024, 8, 21);
            final LocalDate endDate = LocalDate.of(2024, 8, 22);
            final LocalDate nextEndDate = LocalDate.of(2024, 8, 23);

            final SickNoteExtensionEntity existingEntity = new SickNoteExtensionEntity();
            existingEntity.setId(1L);
            existingEntity.setSickNoteId(1L);
            existingEntity.setStatus(ACCEPTED);

            when(repository.findAllBySickNoteIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(existingEntity));

            final WorkingTimeCalendar workingTimeCalendar = mock(WorkingTimeCalendar.class);
            when(workingTimeCalendar.workingTime(endDate, nextEndDate)).thenReturn(BigDecimal.valueOf(3));

            when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), new DateRange(endDate, nextEndDate)))
                .thenReturn(Map.of(person, workingTimeCalendar));

            when(repository.save(any(SickNoteExtensionEntity.class))).thenAnswer(returnsFirstArg());

            final SickNote sickNote = SickNote.builder()
                .id(1L)
                .person(person)
                .startDate(startDate)
                .endDate(endDate)
                .build();

            final SickNoteExtension actual = sut.createSickNoteExtension(sickNote, nextEndDate);
            // save is mocked to return first arg which has no id. in reality, it would be a new value.
            assertThat(actual.id()).isNull();
        }

        @Test
        void ensureCreatesNewEntityWhenNothingExistsYet() {

            final Person submitter = new Person();
            submitter.setId(1L);

            final LocalDate startDate = LocalDate.of(2024, 8, 21);
            final LocalDate endDate = LocalDate.of(2024, 8, 22);
            final LocalDate nextEndDate = LocalDate.of(2024, 8, 23);

            final SickNote sickNote = SickNote.builder()
                .id(1L)
                .person(submitter)
                .startDate(startDate)
                .endDate(endDate)
                .build();

            when(repository.findAllBySickNoteIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());

            when(repository.save(any(SickNoteExtensionEntity.class))).thenAnswer(returnsFirstArg());

            final WorkingTimeCalendar workingTimeCalendar = mock(WorkingTimeCalendar.class);
            when(workingTimeCalendar.workingTime(endDate, nextEndDate)).thenReturn(BigDecimal.valueOf(3));

            when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(submitter), new DateRange(endDate, nextEndDate)))
                .thenReturn(Map.of(submitter, workingTimeCalendar));

            final SickNoteExtension actual = sut.createSickNoteExtension(sickNote, nextEndDate);
            // save is mocked to return first arg which has no id. in reality, it would be a new value.
            assertThat(actual.id()).isNull();
            assertThat(actual.sickNoteId()).isEqualTo(1L);
            assertThat(actual.nextEndDate()).isEqualTo(nextEndDate);
            assertThat(actual.status()).isEqualTo(SUBMITTED);
            assertThat(actual.additionalWorkdays()).isEqualTo(BigDecimal.valueOf(3));
        }
    }

    @Nested
    class AcceptSubmittedExtension {

        @Test
        void ensureThrowsWhenSickNoteDoesNotExist() {

            when(sickNoteService.getById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.acceptSubmittedExtension(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Could not find sickNote with id=1");
        }

        @Test
        void ensureThrowsWhenNoExtensionExist() {

            when(sickNoteService.getById(1L)).thenReturn(Optional.of(SickNote.builder().id(1L).build()));
            when(repository.findAllBySickNoteIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());

            assertThatThrownBy(() -> sut.acceptSubmittedExtension(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot accept submitted extension. No extensions could be found for sickNote id=1");
        }

        @ParameterizedTest
        @EnumSource(value = SickNoteExtensionStatus.class, names = {"SUBMITTED"}, mode = EnumSource.Mode.EXCLUDE)
        void ensureThrowsWhenNoSubmittedExtensionExist(SickNoteExtensionStatus status) {

            when(sickNoteService.getById(1L)).thenReturn(Optional.of(SickNote.builder().id(1L).build()));

            final SickNoteExtensionEntity extensionEntity = new SickNoteExtensionEntity();
            extensionEntity.setId(1L);
            extensionEntity.setSickNoteId(1L);
            extensionEntity.setStatus(status);

            when(repository.findAllBySickNoteIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(extensionEntity));

            assertThatThrownBy(() -> sut.acceptSubmittedExtension(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot accept submitted extension. No extension with status SUBMITTED could be found for sickNote id=1");
        }

        @Test
        void ensureAccepted() {

            final Person person = new Person();
            person.setId(1L);

            final LocalDate startDate = LocalDate.of(2024, 8, 21);
            final LocalDate endDate = LocalDate.of(2024, 8, 22);
            final LocalDate nextEndDate = LocalDate.of(2024, 8, 23);

            final SickNote sickNote = SickNote.builder()
                .id(1L)
                .person(person)
                .startDate(startDate)
                .endDate(endDate)
                .build();

            when(sickNoteService.getById(1L)).thenReturn(Optional.of(sickNote));
            when(sickNoteService.save(any(SickNote.class))).thenAnswer(returnsFirstArg());

            final SickNoteExtensionEntity extensionEntity = new SickNoteExtensionEntity();
            extensionEntity.setId(1L);
            extensionEntity.setSickNoteId(1L);
            extensionEntity.setNewEndDate(nextEndDate);
            extensionEntity.setStatus(SUBMITTED);

            when(repository.findAllBySickNoteIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(extensionEntity));

            sut.acceptSubmittedExtension(1L);

            final ArgumentCaptor<List<SickNoteExtensionEntity>> captor = ArgumentCaptor.forClass(List.class);
            verify(repository).saveAll(captor.capture());

            assertThat(captor.getValue()).satisfiesExactly(
                entity -> {
                    assertThat(entity.getId()).isEqualTo(1L);
                    assertThat(entity.getSickNoteId()).isEqualTo(1L);
                    assertThat(entity.getNewEndDate()).isEqualTo(nextEndDate);
                    assertThat(entity.getStatus()).isEqualTo(ACCEPTED);
                }
            );
        }
    }
}
