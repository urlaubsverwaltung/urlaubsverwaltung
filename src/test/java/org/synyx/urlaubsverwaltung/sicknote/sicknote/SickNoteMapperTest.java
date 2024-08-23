package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation.WorkingTimeCalendarEntryType.NO_WORKDAY;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation.WorkingTimeCalendarEntryType.WORKDAY;

@ExtendWith(MockitoExtension.class)
class SickNoteMapperTest {

    @InjectMocks
    private SickNoteMapper sut;

    @Mock
    private WorkingTimeCalendarService workingTimeCalendarService;

    @Nested
    class ToSickNote {

        @Test
        void ensureMapping() {

            final Person person = new Person();
            person.setId(1L);

            final Person applier = new Person();
            applier.setId(2L);

            final SickNoteType sickNoteType = new SickNoteType();
            final LocalDate startDate = LocalDate.of(2024, 8, 19);
            final LocalDate endDate = LocalDate.of(2024, 8, 23);
            final LocalDate aubStartDate = LocalDate.of(2024, 8, 20);
            final LocalDate aubEndDate = LocalDate.of(2024, 8, 23);
            final LocalDate lastEdited = LocalDate.of(2024, 8, 20);
            final LocalDate endOfSickPauNotificationSend = LocalDate.of(2025, 1, 1);

            final SickNoteEntity entity = new SickNoteEntity();
            entity.setId(1L);
            entity.setPerson(person);
            entity.setApplier(applier);
            entity.setStartDate(startDate);
            entity.setEndDate(endDate);
            entity.setDayLength(DayLength.FULL);
            entity.setSickNoteType(sickNoteType);
            entity.setAubStartDate(aubStartDate);
            entity.setAubEndDate(aubEndDate);
            entity.setLastEdited(lastEdited);
            entity.setEndOfSickPayNotificationSend(endOfSickPauNotificationSend);
            entity.setStatus(SickNoteStatus.ACTIVE);

            final SickNote actual = sut.toSickNote(entity);
            assertThat(actual.getId()).isEqualTo(1L);
            assertThat(actual.getPerson()).isEqualTo(person);
            assertThat(actual.getApplier()).isEqualTo(applier);
            assertThat(actual.getStartDate()).isEqualTo(startDate);
            assertThat(actual.getEndDate()).isEqualTo(endDate);
            assertThat(actual.getDayLength()).isEqualTo(DayLength.FULL);
            assertThat(actual.getSickNoteType()).isEqualTo(sickNoteType);
            assertThat(actual.getAubStartDate()).isEqualTo(aubStartDate);
            assertThat(actual.getAubEndDate()).isEqualTo(aubEndDate);
            assertThat(actual.getLastEdited()).isEqualTo(lastEdited);
            assertThat(actual.getEndOfSickPayNotificationSend()).isEqualTo(endOfSickPauNotificationSend);
            assertThat(actual.getStatus()).isEqualTo(SickNoteStatus.ACTIVE);
        }
    }

    @Nested
    class ToSickNoteWithWorkingTimeCalendar {

        @Test
        void ensureMapping() {

            final Person person = new Person();
            person.setId(1L);

            final Person applier = new Person();
            applier.setId(2L);

            final SickNoteType sickNoteType = new SickNoteType();
            final LocalDate startDate = LocalDate.of(2024, 8, 19);
            final LocalDate endDate = LocalDate.of(2024, 8, 23);
            final LocalDate aubStartDate = LocalDate.of(2024, 8, 20);
            final LocalDate aubEndDate = LocalDate.of(2024, 8, 23);
            final LocalDate lastEdited = LocalDate.of(2024, 8, 20);
            final LocalDate endOfSickPauNotificationSend = LocalDate.of(2025, 1, 1);

            final SickNoteEntity entity = new SickNoteEntity();
            entity.setId(1L);
            entity.setPerson(person);
            entity.setApplier(applier);
            entity.setStartDate(startDate);
            entity.setEndDate(endDate);
            entity.setDayLength(DayLength.FULL);
            entity.setSickNoteType(sickNoteType);
            entity.setAubStartDate(aubStartDate);
            entity.setAubEndDate(aubEndDate);
            entity.setLastEdited(lastEdited);
            entity.setEndOfSickPayNotificationSend(endOfSickPauNotificationSend);
            entity.setStatus(SickNoteStatus.ACTIVE);

            final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(Map.of(
                startDate, new WorkingTimeCalendar.WorkingDayInformation(DayLength.FULL, WORKDAY, WORKDAY),
                startDate.plusDays(1), new WorkingTimeCalendar.WorkingDayInformation(DayLength.FULL, WORKDAY, WORKDAY),
                startDate.plusDays(2), new WorkingTimeCalendar.WorkingDayInformation(DayLength.FULL, WORKDAY, WORKDAY),
                startDate.plusDays(3), new WorkingTimeCalendar.WorkingDayInformation(DayLength.ZERO, NO_WORKDAY, NO_WORKDAY),
                startDate.plusDays(4), new WorkingTimeCalendar.WorkingDayInformation(DayLength.FULL, WORKDAY, WORKDAY),
                startDate.plusDays(5), new WorkingTimeCalendar.WorkingDayInformation(DayLength.FULL, WORKDAY, WORKDAY),
                startDate.plusDays(6), new WorkingTimeCalendar.WorkingDayInformation(DayLength.ZERO, NO_WORKDAY, NO_WORKDAY),
                startDate.plusDays(7), new WorkingTimeCalendar.WorkingDayInformation(DayLength.ZERO, NO_WORKDAY, NO_WORKDAY)
            ));

            final SickNote actual = sut.toSickNote(entity, workingTimeCalendar);
            assertThat(actual.getId()).isEqualTo(1L);
            assertThat(actual.getPerson()).isEqualTo(person);
            assertThat(actual.getApplier()).isEqualTo(applier);
            assertThat(actual.getStartDate()).isEqualTo(startDate);
            assertThat(actual.getEndDate()).isEqualTo(endDate);
            assertThat(actual.getDayLength()).isEqualTo(DayLength.FULL);
            assertThat(actual.getSickNoteType()).isEqualTo(sickNoteType);
            assertThat(actual.getAubStartDate()).isEqualTo(aubStartDate);
            assertThat(actual.getAubEndDate()).isEqualTo(aubEndDate);
            assertThat(actual.getLastEdited()).isEqualTo(lastEdited);
            assertThat(actual.getEndOfSickPayNotificationSend()).isEqualTo(endOfSickPauNotificationSend);
            assertThat(actual.getStatus()).isEqualTo(SickNoteStatus.ACTIVE);

            assertThat(actual.getWorkDays()).isEqualTo(BigDecimal.valueOf(4));
            assertThat(actual.getWorkDaysWithAub(startDate, endDate)).isEqualTo(BigDecimal.valueOf(3));
        }
    }

    @Nested
    class ToSickNoteWithWorkDays {

        @Test
        void ensureWithEmptyEntities() {

            final LocalDate startDate = LocalDate.of(2024, 8, 19);
            final LocalDate endDate = LocalDate.of(2024, 8, 23);

            final List<SickNote> actual = sut.toSickNoteWithWorkDays(List.of(), new DateRange(startDate, endDate));
            assertThat(actual).isEmpty();

            verifyNoInteractions(workingTimeCalendarService);
        }

        @Test
        void ensureMapping() {

            final Person person1 = new Person();
            person1.setId(1L);

            final Person person2 = new Person();
            person2.setId(2L);

            final Person applier = new Person();
            applier.setId(3L);

            final SickNoteType sickNoteType = new SickNoteType();
            final LocalDate startDate = LocalDate.of(2024, 8, 19);
            final LocalDate endDate = LocalDate.of(2024, 8, 23);
            final LocalDate aubStartDate = LocalDate.of(2024, 8, 20);
            final LocalDate aubEndDate = LocalDate.of(2024, 8, 23);
            final LocalDate lastEdited = LocalDate.of(2024, 8, 20);
            final LocalDate endOfSickPauNotificationSend = LocalDate.of(2025, 1, 1);

            final SickNoteEntity entity1 = new SickNoteEntity();
            entity1.setId(1L);
            entity1.setPerson(person1);
            entity1.setApplier(applier);
            entity1.setStartDate(startDate);
            entity1.setEndDate(endDate);
            entity1.setDayLength(DayLength.FULL);
            entity1.setSickNoteType(sickNoteType);
            entity1.setAubStartDate(aubStartDate);
            entity1.setAubEndDate(aubEndDate);
            entity1.setLastEdited(lastEdited);
            entity1.setEndOfSickPayNotificationSend(endOfSickPauNotificationSend);
            entity1.setStatus(SickNoteStatus.ACTIVE);

            final SickNoteEntity entity2 = new SickNoteEntity();
            entity2.setId(2L);
            entity2.setPerson(person2);
            entity2.setApplier(applier);
            entity2.setStartDate(startDate);
            entity2.setEndDate(endDate);
            entity2.setDayLength(DayLength.FULL);
            entity2.setSickNoteType(sickNoteType);
            entity2.setAubStartDate(aubStartDate);
            entity2.setAubEndDate(aubEndDate);
            entity2.setLastEdited(lastEdited);
            entity2.setEndOfSickPayNotificationSend(endOfSickPauNotificationSend);
            entity2.setStatus(SickNoteStatus.SUBMITTED);

            final DateRange dateRange = new DateRange(startDate, endDate);

            when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person1, person2), dateRange))
                .thenReturn(Map.of(
                    person1, new WorkingTimeCalendar(Map.of()),
                    person2, new WorkingTimeCalendar(Map.of())
                ));

            final List<SickNote> actual = sut.toSickNoteWithWorkDays(List.of(entity1, entity2), dateRange);
            assertThat(actual).satisfiesExactly(
                sickNote -> {
                    assertThat(sickNote.getId()).isEqualTo(1L);
                    assertThat(sickNote.getPerson()).isEqualTo(person1);
                    assertThat(sickNote.getApplier()).isEqualTo(applier);
                    assertThat(sickNote.getStartDate()).isEqualTo(startDate);
                    assertThat(sickNote.getEndDate()).isEqualTo(endDate);
                    assertThat(sickNote.getDayLength()).isEqualTo(DayLength.FULL);
                    assertThat(sickNote.getSickNoteType()).isEqualTo(sickNoteType);
                    assertThat(sickNote.getAubStartDate()).isEqualTo(aubStartDate);
                    assertThat(sickNote.getAubEndDate()).isEqualTo(aubEndDate);
                    assertThat(sickNote.getLastEdited()).isEqualTo(lastEdited);
                    assertThat(sickNote.getEndOfSickPayNotificationSend()).isEqualTo(endOfSickPauNotificationSend);
                    assertThat(sickNote.getStatus()).isEqualTo(SickNoteStatus.ACTIVE);
                },
                sickNote -> {
                    assertThat(sickNote.getId()).isEqualTo(2L);
                    assertThat(sickNote.getPerson()).isEqualTo(person2);
                    assertThat(sickNote.getApplier()).isEqualTo(applier);
                    assertThat(sickNote.getStartDate()).isEqualTo(startDate);
                    assertThat(sickNote.getEndDate()).isEqualTo(endDate);
                    assertThat(sickNote.getDayLength()).isEqualTo(DayLength.FULL);
                    assertThat(sickNote.getSickNoteType()).isEqualTo(sickNoteType);
                    assertThat(sickNote.getAubStartDate()).isEqualTo(aubStartDate);
                    assertThat(sickNote.getAubEndDate()).isEqualTo(aubEndDate);
                    assertThat(sickNote.getLastEdited()).isEqualTo(lastEdited);
                    assertThat(sickNote.getEndOfSickPayNotificationSend()).isEqualTo(endOfSickPauNotificationSend);
                    assertThat(sickNote.getStatus()).isEqualTo(SickNoteStatus.SUBMITTED);
                }
            );
        }

        @Test
        void ensureMappingWithDistinctPersons() {

            final Person person = new Person();
            person.setId(1L);

            final Person applier = new Person();
            applier.setId(3L);

            final SickNoteType sickNoteType = new SickNoteType();
            final LocalDate startDate = LocalDate.of(2024, 8, 19);
            final LocalDate endDate = LocalDate.of(2024, 8, 23);
            final LocalDate aubStartDate = LocalDate.of(2024, 8, 20);
            final LocalDate aubEndDate = LocalDate.of(2024, 8, 23);
            final LocalDate lastEdited = LocalDate.of(2024, 8, 20);
            final LocalDate endOfSickPauNotificationSend = LocalDate.of(2025, 1, 1);

            final SickNoteEntity entity1 = new SickNoteEntity();
            entity1.setId(1L);
            entity1.setPerson(person);
            entity1.setApplier(applier);
            entity1.setStartDate(startDate);
            entity1.setEndDate(endDate);
            entity1.setDayLength(DayLength.FULL);
            entity1.setSickNoteType(sickNoteType);
            entity1.setAubStartDate(aubStartDate);
            entity1.setAubEndDate(aubEndDate);
            entity1.setLastEdited(lastEdited);
            entity1.setEndOfSickPayNotificationSend(endOfSickPauNotificationSend);
            entity1.setStatus(SickNoteStatus.ACTIVE);

            final SickNoteEntity entity2 = new SickNoteEntity();
            entity2.setId(2L);
            entity2.setPerson(person);
            entity2.setApplier(applier);
            entity2.setStartDate(startDate);
            entity2.setEndDate(endDate);
            entity2.setDayLength(DayLength.FULL);
            entity2.setSickNoteType(sickNoteType);
            entity2.setAubStartDate(aubStartDate);
            entity2.setAubEndDate(aubEndDate);
            entity2.setLastEdited(lastEdited);
            entity2.setEndOfSickPayNotificationSend(endOfSickPauNotificationSend);
            entity2.setStatus(SickNoteStatus.SUBMITTED);

            final DateRange dateRange = new DateRange(startDate, endDate);

            when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), dateRange))
                .thenReturn(Map.of(
                    person, new WorkingTimeCalendar(Map.of())
                ));

            final List<SickNote> actual = sut.toSickNoteWithWorkDays(List.of(entity1, entity2), dateRange);
            assertThat(actual).satisfiesExactly(
                sickNote -> {
                    assertThat(sickNote.getId()).isEqualTo(1L);
                    assertThat(sickNote.getPerson()).isEqualTo(person);
                    assertThat(sickNote.getApplier()).isEqualTo(applier);
                    assertThat(sickNote.getStartDate()).isEqualTo(startDate);
                    assertThat(sickNote.getEndDate()).isEqualTo(endDate);
                    assertThat(sickNote.getDayLength()).isEqualTo(DayLength.FULL);
                    assertThat(sickNote.getSickNoteType()).isEqualTo(sickNoteType);
                    assertThat(sickNote.getAubStartDate()).isEqualTo(aubStartDate);
                    assertThat(sickNote.getAubEndDate()).isEqualTo(aubEndDate);
                    assertThat(sickNote.getLastEdited()).isEqualTo(lastEdited);
                    assertThat(sickNote.getEndOfSickPayNotificationSend()).isEqualTo(endOfSickPauNotificationSend);
                    assertThat(sickNote.getStatus()).isEqualTo(SickNoteStatus.ACTIVE);
                },
                sickNote -> {
                    assertThat(sickNote.getId()).isEqualTo(2L);
                    assertThat(sickNote.getPerson()).isEqualTo(person);
                    assertThat(sickNote.getApplier()).isEqualTo(applier);
                    assertThat(sickNote.getStartDate()).isEqualTo(startDate);
                    assertThat(sickNote.getEndDate()).isEqualTo(endDate);
                    assertThat(sickNote.getDayLength()).isEqualTo(DayLength.FULL);
                    assertThat(sickNote.getSickNoteType()).isEqualTo(sickNoteType);
                    assertThat(sickNote.getAubStartDate()).isEqualTo(aubStartDate);
                    assertThat(sickNote.getAubEndDate()).isEqualTo(aubEndDate);
                    assertThat(sickNote.getLastEdited()).isEqualTo(lastEdited);
                    assertThat(sickNote.getEndOfSickPayNotificationSend()).isEqualTo(endOfSickPauNotificationSend);
                    assertThat(sickNote.getStatus()).isEqualTo(SickNoteStatus.SUBMITTED);
                }
            );
        }
    }
}
