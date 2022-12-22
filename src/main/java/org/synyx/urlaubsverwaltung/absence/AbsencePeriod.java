package org.synyx.urlaubsverwaltung.absence;

import org.synyx.urlaubsverwaltung.person.Person;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Defines absence periods of a {@link Person}.
 * <p>
 * e.g. Bruce Wayne is absent on:
 * <ul>
 *     <li>24.March 2021 to 28. March 2021 (vacation full day)</li>
 *     <li>31.March 2021 (vacation morning)</li>
 *     <li>9.June 2021 (vacation noon)</li>
 *     <li>26.August 2021 to 27.August 2021 (sick full day)</li>
 * </ul>
 */
public class AbsencePeriod {

    public enum AbsenceType {
        VACATION,
        SICK,
    }

    public enum AbsenceStatus {
        // vacation
        WAITING,
        TEMPORARY_ALLOWED,
        ALLOWED,
        ALLOWED_CANCELLATION_REQUESTED,
        REVOKED,
        REJECTED,
        // sick note,
        ACTIVE,
        CONVERTED_TO_VACATION,
        // sick notes and vacation
        CANCELLED
    }

    private final List<AbsencePeriod.Record> absenceRecords;

    public AbsencePeriod(List<Record> absenceRecords) {
        this.absenceRecords = absenceRecords;
    }

    public List<AbsencePeriod.Record> getAbsenceRecords() {
        return Collections.unmodifiableList(absenceRecords);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbsencePeriod that = (AbsencePeriod) o;
        return Objects.equals(absenceRecords, that.absenceRecords);
    }

    @Override
    public int hashCode() {
        return Objects.hash(absenceRecords);
    }

    @Override
    public String toString() {
        return "AbsencePeriod{" +
            "absenceRecords=" + absenceRecords +
            '}';
    }

    /**
     * Specifies an absence for one date. The absence consists of `morning` and `evening`.
     * You may have to handle information yourself for "full absence vacation". In This case morning and evening are
     * defined.
     */
    public static class Record {

        private final LocalDate date;
        private final Person person;
        private final RecordMorning morning;
        private final RecordNoon noon;

        public Record(LocalDate date, Person person, RecordMorning morning) {
            this(date, person, morning, null);
        }

        public Record(LocalDate date, Person person, RecordNoon noon) {
            this(date, person, null, noon);
        }

        public Record(LocalDate date, Person person, RecordMorning morning, RecordNoon noon) {
            this.date = date;
            this.person = person;
            this.morning = morning;
            this.noon = noon;
        }

        public LocalDate getDate() {
            return date;
        }

        public Person getPerson() {
            return person;
        }

        public boolean isHalfDayAbsence() {
            return (this.morning == null && this.noon != null) || (this.morning != null && this.noon == null);
        }

        /**
         * Morning RecordInfo is empty when this Record specifies a noon absence only.
         *
         * @return the morning RecordInfo if it exists, empty Optional otherwise.
         */
        public Optional<RecordInfo> getMorning() {
            return Optional.ofNullable(morning);
        }

        /**
         * Noon RecordInfo is empty when this Record specifies a morning absence only.
         *
         * @return the noon RecordInfo if it exists, empty Optional otherwise.
         */
        public Optional<RecordInfo> getNoon() {
            return Optional.ofNullable(noon);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Record absenceRecord = (Record) o;
            return Objects.equals(date, absenceRecord.date) && Objects.equals(person, absenceRecord.person);
        }

        @Override
        public int hashCode() {
            return Objects.hash(date, person);
        }

        @Override
        public String toString() {
            return "Record{" +
                "date=" + date +
                ", person=" + person +
                ", morning=" + morning +
                ", noon=" + noon +
                '}';
        }
    }

    /**
     * Describes an absence record. (e.g. {@link RecordMorning} absence or {@link RecordNoon} absence)
     */
    public interface RecordInfo {
        Person getPerson();

        AbsenceType getType();

        AbsenceStatus getStatus();

        Long getId();

        boolean hasStatusTemporaryAllowed();

        boolean hasStatusWaiting();

        boolean hasStatusAllowed();

        boolean hasStatusAllowedCancellationRequested();

        Optional<Long> getVacationTypeId();

        boolean isVisibleToEveryone();
    }

    /**
     * Describes an absence this morning.
     */
    public interface RecordMorning extends RecordInfo {
    }

    /**
     * Describes an absence this noon.
     */
    public interface RecordNoon extends RecordInfo {
    }

    /**
     * Describes an absence record. (e.g. morning absence or noon absence)
     */
    public abstract static class AbstractRecordInfo implements RecordInfo {

        private final Person person;
        private final AbsenceType type;
        private final Long id;
        private final AbsenceStatus status;
        private final Long vacationTypeId;

        private final boolean visibleToEveryone;

        private AbstractRecordInfo(Person person, AbsenceType type, Long id, AbsenceStatus status) {
            this(person, type, id, status, null, false);
        }

        private AbstractRecordInfo(Person person, AbsenceType type, Long id, AbsenceStatus status, Long vacationTypeId, boolean visibleToEveryone) {
            this.person = person;
            this.type = type;
            this.id = id;
            this.status = status;
            this.vacationTypeId = vacationTypeId;
            this.visibleToEveryone = visibleToEveryone;
        }

        @Override
        public Person getPerson() {
            return person;
        }

        @Override
        public AbsenceType getType() {
            return type;
        }

        @Override
        public Long getId() {
            return id;
        }

        @Override
        public AbsenceStatus getStatus() {
            return status;
        }

        public boolean hasStatusOneOf(AbsenceStatus... status) {
            return List.of(status).contains(this.status);
        }

        public boolean hasStatusTemporaryAllowed() {
            return hasStatusOneOf(AbsenceStatus.TEMPORARY_ALLOWED);
        }

        public boolean hasStatusWaiting() {
            return hasStatusOneOf(AbsenceStatus.WAITING);
        }

        public boolean hasStatusAllowed() {
            return hasStatusOneOf(AbsenceStatus.ALLOWED);
        }

        public boolean hasStatusAllowedCancellationRequested() {
            return hasStatusOneOf(AbsenceStatus.ALLOWED_CANCELLATION_REQUESTED);
        }

        public Optional<Long> getVacationTypeId() {
            return Optional.ofNullable(vacationTypeId);
        }

        public boolean isVisibleToEveryone() {
            return visibleToEveryone;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AbstractRecordInfo that = (AbstractRecordInfo) o;
            return type == that.type && status == that.status;
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, status);
        }

        @Override
        public String toString() {
            return "AbstractRecordInfo{" +
                "id=" + id +
                '}';
        }
    }

    public static class RecordMorningVacation extends AbstractRecordInfo implements RecordMorning {
        public RecordMorningVacation(Person person, Long applicationId, AbsenceStatus status, Long vacationTypeId, boolean visibleToEveryone) {
            super(person, AbsenceType.VACATION, applicationId, status, vacationTypeId, visibleToEveryone);
        }
    }

    public static class RecordMorningSick extends AbstractRecordInfo implements RecordMorning {
        public RecordMorningSick(Person person, Long sickNoteId, AbsenceStatus status) {
            super(person, AbsenceType.SICK, sickNoteId, status);
        }
    }

    public static class RecordNoonVacation extends AbstractRecordInfo implements RecordNoon {
        public RecordNoonVacation(Person person, Long applicationId, AbsenceStatus status, Long vacationTypeId, boolean visibleToEveryone) {
            super(person, AbsenceType.VACATION, applicationId, status, vacationTypeId, visibleToEveryone);
        }
    }

    public static class RecordNoonSick extends AbstractRecordInfo implements RecordNoon {
        public RecordNoonSick(Person person, Long sickNoteId, AbsenceStatus status) {
            super(person, AbsenceType.SICK, sickNoteId, status);
        }
    }
}
