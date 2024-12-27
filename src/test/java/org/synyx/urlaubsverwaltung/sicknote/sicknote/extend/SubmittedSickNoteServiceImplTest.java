package org.synyx.urlaubsverwaltung.sicknote.sicknote.extend;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteEntity;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteMapper;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SubmittedSickNote;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation.WorkingTimeCalendarEntryType.WORKDAY;

@ExtendWith(MockitoExtension.class)
class SubmittedSickNoteServiceImplTest {

    @InjectMocks
    private SubmittedSickNoteServiceImpl sut;

    @Mock
    private SickNoteExtensionRepository extensionRepository;
    @Mock
    private SickNoteService sickNoteService;
    @Mock
    private WorkingTimeCalendarService workingTimeCalendarService;
    @Mock
    private SickNoteMapper sickNoteMapper;

    @Nested
    class FindSubmittedSickNotes {

        @Test
        void ensureEmpty() {

            final Person person = new Person();
            person.setId(1L);

            when(sickNoteService.getForStatesAndPerson(List.of(SickNoteStatus.SUBMITTED), List.of(person)))
                .thenReturn(List.of());

            when(extensionRepository.findAllByStatusAndPersonIsIn(SickNoteExtensionStatus.SUBMITTED, List.of(1L)))
                .thenReturn(List.of());

            final List<SubmittedSickNote> actual = sut.findSubmittedSickNotes(List.of(person));
            assertThat(actual).isEmpty();
        }

        @Test
        void ensureSickNoteWithStatusSUBMITTED() {

            final Person person = new Person();
            person.setId(1L);

            final SickNote sickNote = SickNote.builder().id(1L).build();

            when(sickNoteService.getForStatesAndPerson(List.of(SickNoteStatus.SUBMITTED), List.of(person)))
                .thenReturn(List.of(sickNote));

            when(extensionRepository.findAllByStatusAndPersonIsIn(SickNoteExtensionStatus.SUBMITTED, List.of(1L)))
                .thenReturn(List.of());

            final List<SubmittedSickNote> actual = sut.findSubmittedSickNotes(List.of(person));
            assertThat(actual).satisfiesExactly(
                submittedSickNote -> {
                    assertThat(submittedSickNote.sickNote()).isSameAs(sickNote);
                    assertThat(submittedSickNote.extension()).isEmpty();
                }
            );
        }

        @Test
        void ensureSickNoteWithExtensions() {

            final Person person = new Person();
            person.setId(1L);

            final LocalDate startDate = LocalDate.of(2024, 8, 21);
            final LocalDate endDate = LocalDate.of(2024, 8, 22);
            final LocalDate nextEndDate = LocalDate.of(2024, 8, 23);

            when(sickNoteService.getForStatesAndPerson(List.of(SickNoteStatus.SUBMITTED), List.of(person)))
                .thenReturn(List.of());

            final SickNoteEntity sickNoteEntity = mock(SickNoteEntity.class);
            when(sickNoteEntity.getPerson()).thenReturn(person);
            when(sickNoteEntity.getStartDate()).thenReturn(startDate);
            when(sickNoteEntity.getEndDate()).thenReturn(endDate);

            final SickNoteExtensionEntity extensionEntity = new SickNoteExtensionEntity();
            extensionEntity.setId(1L);
            extensionEntity.setSickNoteId(1L);
            extensionEntity.setStatus(SickNoteExtensionStatus.SUBMITTED);
            extensionEntity.setNewEndDate(nextEndDate);

            when(extensionRepository.findAllByStatusAndPersonIsIn(SickNoteExtensionStatus.SUBMITTED, List.of(1L)))
                .thenReturn(List.of(projection(sickNoteEntity, extensionEntity)));

            final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(Map.of());
            when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), new DateRange(startDate, nextEndDate)))
                .thenReturn(Map.of(person, workingTimeCalendar));

            final SickNote sickNote = SickNote.builder()
                .id(1L)
                .startDate(startDate)
                .endDate(endDate)
                .build();
            when(sickNoteMapper.toSickNote(sickNoteEntity, workingTimeCalendar)).thenReturn(sickNote);

            final List<SubmittedSickNote> actual = sut.findSubmittedSickNotes(List.of(person));
            assertThat(actual).satisfiesExactly(
                submittedSickNote -> {
                    assertThat(submittedSickNote.sickNote()).isSameAs(sickNote);
                    assertThat(submittedSickNote.extension()).hasValueSatisfying(extension -> {
                        assertThat(extension.id()).isEqualTo(1L);
                        assertThat(extension.sickNoteId()).isEqualTo(1L);
                        assertThat(extension.nextEndDate()).isEqualTo(nextEndDate);
                        // additionalWorkingDays are handled in extra test case
                    });
                }
            );
        }

        @Test
        void ensureCorrectAdditionalWorkingDays() {

            final Person person = new Person();
            person.setId(1L);

            final LocalDate startDate = LocalDate.of(2024, 8, 21);
            final LocalDate endDate = LocalDate.of(2024, 8, 22);
            final LocalDate nextEndDate = LocalDate.of(2024, 8, 23);

            when(sickNoteService.getForStatesAndPerson(List.of(SickNoteStatus.SUBMITTED), List.of(person)))
                .thenReturn(List.of());

            final SickNoteEntity sickNoteEntity = mock(SickNoteEntity.class);
            when(sickNoteEntity.getPerson()).thenReturn(person);
            when(sickNoteEntity.getStartDate()).thenReturn(startDate);
            when(sickNoteEntity.getEndDate()).thenReturn(endDate);

            final SickNoteExtensionEntity extensionEntity = new SickNoteExtensionEntity();
            extensionEntity.setId(1L);
            extensionEntity.setSickNoteId(1L);
            extensionEntity.setStatus(SickNoteExtensionStatus.SUBMITTED);
            extensionEntity.setNewEndDate(nextEndDate);

            when(extensionRepository.findAllByStatusAndPersonIsIn(SickNoteExtensionStatus.SUBMITTED, List.of(1L)))
                .thenReturn(List.of(projection(sickNoteEntity, extensionEntity)));

            final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(Map.of(
                startDate, new WorkingTimeCalendar.WorkingDayInformation(DayLength.FULL, WORKDAY, WORKDAY),
                endDate, new WorkingTimeCalendar.WorkingDayInformation(DayLength.FULL, WORKDAY, WORKDAY),
                nextEndDate, new WorkingTimeCalendar.WorkingDayInformation(DayLength.FULL, WORKDAY, WORKDAY)
            ));
            when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), new DateRange(startDate, nextEndDate)))
                .thenReturn(Map.of(person, workingTimeCalendar));

            final SickNote sickNote = SickNote.builder()
                .id(1L)
                .startDate(startDate)
                .endDate(endDate)
                .build();
            when(sickNoteMapper.toSickNote(sickNoteEntity, workingTimeCalendar)).thenReturn(sickNote);

            final List<SubmittedSickNote> actual = sut.findSubmittedSickNotes(List.of(person));
            assertThat(actual.getFirst().extension()).hasValueSatisfying(extension -> {
                // sickNote: 2 workingdays
                // extension: +1
                assertThat(extension.additionalWorkdays()).isEqualTo(BigDecimal.valueOf(1));
            });
        }

        @Test
        void ensureDistinctWhenSickNoteAndExtension() {
            // this actually cannot happen when everythings correct.
            // a sickNote with status SUBMITTED cannot have extensions.
            // the sickNote itself is updated when an extension is submitted.
            // but... who knows...

            final Person person = new Person();
            person.setId(1L);

            final LocalDate startDate = LocalDate.of(2024, 8, 21);
            final LocalDate endDate = LocalDate.of(2024, 8, 22);
            final LocalDate nextEndDate = LocalDate.of(2024, 8, 23);

            final SickNote sickNote = SickNote.builder()
                .id(1L)
                .startDate(startDate)
                .endDate(endDate)
                .build();

            when(sickNoteService.getForStatesAndPerson(List.of(SickNoteStatus.SUBMITTED), List.of(person)))
                .thenReturn(List.of(sickNote));

            final SickNoteEntity sickNoteEntity = mock(SickNoteEntity.class);
            when(sickNoteEntity.getPerson()).thenReturn(person);
            when(sickNoteEntity.getStartDate()).thenReturn(startDate);
            when(sickNoteEntity.getEndDate()).thenReturn(endDate);

            final SickNoteExtensionEntity extensionEntity = new SickNoteExtensionEntity();
            extensionEntity.setId(1L);
            extensionEntity.setSickNoteId(1L);
            extensionEntity.setStatus(SickNoteExtensionStatus.SUBMITTED);
            extensionEntity.setNewEndDate(nextEndDate);

            when(extensionRepository.findAllByStatusAndPersonIsIn(SickNoteExtensionStatus.SUBMITTED, List.of(1L)))
                .thenReturn(List.of(projection(sickNoteEntity, extensionEntity)));

            final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(Map.of());
            when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), new DateRange(startDate, nextEndDate)))
                .thenReturn(Map.of(person, workingTimeCalendar));
            when(sickNoteMapper.toSickNote(sickNoteEntity, workingTimeCalendar)).thenReturn(sickNote);

            final List<SubmittedSickNote> actual = sut.findSubmittedSickNotes(List.of(person));
            assertThat(actual).hasSize(1);
            assertThat(actual.getFirst().sickNote()).isSameAs(sickNote);
        }
    }

    private static SickNoteExtensionProjection projection(final SickNoteEntity sickNoteEntity, final SickNoteExtensionEntity sickNoteExtensionEntity) {
        return new SickNoteExtensionProjection() {
            @Override
            public SickNoteEntity getSickNote() {
                return sickNoteEntity;
            }

            @Override
            public SickNoteExtensionEntity getSickNoteExtension() {
                return sickNoteExtensionEntity;
            }
        };
    }
}

