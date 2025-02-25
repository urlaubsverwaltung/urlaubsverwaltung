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
public record AbsencePeriod(
    List<Record> absenceRecords
) {

    public enum AbsenceType {
        VACATION,
        SICK,
        PUBLIC_HOLIDAY,
        NO_WORKDAY
    }

    public enum AbsenceStatus {
        // vacation
        TEMPORARY_ALLOWED,
        ALLOWED,
        ALLOWED_CANCELLATION_REQUESTED,
        REVOKED,
        REJECTED,
        // sick note,
        ACTIVE,
        CONVERTED_TO_VACATION,
        // sick notes and vacation
        WAITING,
        CANCELLED
    }

    @Override
    public List<Record> absenceRecords() {
        return Collections.unmodifiableList(absenceRecords);
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

        AbsenceType getAbsenceType();

        AbsenceStatus getStatus();

        Optional<Long> getId();

        boolean hasStatusTemporaryAllowed();

        boolean hasStatusWaiting();

        boolean hasStatusAllowed();

        boolean hasStatusAllowedCancellationRequested();

        Optional<String> getCategory();

        Optional<Long> getTypeId();

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
        private final AbsenceType absenceType;
        private final Long id;
        private final AbsenceStatus status;
        private final String category;
        private final Long typeId;
        private final boolean visibleToEveryone;

        private AbstractRecordInfo(Person person, AbsenceType absenceType, AbsenceStatus status) {
            this(person, absenceType, null, status, null, null, false);
        }

        private AbstractRecordInfo(Person person, AbsenceType absenceType, Long id, AbsenceStatus status, String category, Long typeId) {
            this(person, absenceType, id, status, category, typeId, false);
        }

        private AbstractRecordInfo(Person person, AbsenceType absenceType, Long id, AbsenceStatus status, String category, Long typeId, boolean visibleToEveryone) {
            this.person = person;
            this.absenceType = absenceType;
            this.id = id;
            this.status = status;
            this.category = category;
            this.typeId = typeId;
            this.visibleToEveryone = visibleToEveryone;
        }

        @Override
        public Person getPerson() {
            return person;
        }

        @Override
        public AbsenceType getAbsenceType() {
            return absenceType;
        }

        @Override
        public Optional<Long> getId() {
            return Optional.ofNullable(id);
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

        public Optional<String> getCategory() {
            return Optional.ofNullable(category);
        }

        public Optional<Long> getTypeId() {
            return Optional.ofNullable(typeId);
        }

        public boolean isVisibleToEveryone() {
            return visibleToEveryone;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AbstractRecordInfo that = (AbstractRecordInfo) o;
            return absenceType == that.absenceType && status == that.status;
        }

        @Override
        public int hashCode() {
            return Objects.hash(absenceType, status);
        }

        @Override
        public String toString() {
            return "AbstractRecordInfo{" +
                "id=" + id +
                '}';
        }
    }

    public static class RecordMorningVacation extends AbstractRecordInfo implements RecordMorning {
        public RecordMorningVacation(Person person, Long applicationId, AbsenceStatus status, String category, Long typeId, boolean visibleToEveryone) {
            super(person, AbsenceType.VACATION, applicationId, status, category, typeId, visibleToEveryone);
        }
    }

    public static class RecordMorningSick extends AbstractRecordInfo implements RecordMorning {
        public RecordMorningSick(Person person, Long sickNoteId, AbsenceStatus status, String category, Long typeId) {
            super(person, AbsenceType.SICK, sickNoteId, status, category, typeId);
        }
    }

    public static class RecordMorningNoWorkday extends AbstractRecordInfo implements RecordMorning {
        public RecordMorningNoWorkday(Person person) {
            super(person, AbsenceType.NO_WORKDAY, AbsenceStatus.ACTIVE);
        }
    }

    public static class RecordMorningPublicHoliday extends AbstractRecordInfo implements RecordMorning {
        public RecordMorningPublicHoliday(Person person) {
            super(person, AbsenceType.PUBLIC_HOLIDAY, AbsenceStatus.ACTIVE);
        }
    }

    public static class RecordNoonVacation extends AbstractRecordInfo implements RecordNoon {
        public RecordNoonVacation(Person person, Long applicationId, AbsenceStatus status, String category, Long typeId, boolean visibleToEveryone) {
            super(person, AbsenceType.VACATION, applicationId, status, category, typeId, visibleToEveryone);
        }
    }

    public static class RecordNoonSick extends AbstractRecordInfo implements RecordNoon {
        public RecordNoonSick(Person person, Long sickNoteId, AbsenceStatus status, String category, Long typeId) {
            super(person, AbsenceType.SICK, sickNoteId, status, category, typeId);
        }
    }

    public static class RecordNoonNoWorkday extends AbstractRecordInfo implements RecordNoon {
        public RecordNoonNoWorkday(Person person) {
            super(person, AbsenceType.NO_WORKDAY, AbsenceStatus.ACTIVE);
        }
    }

    public static class RecordNoonPublicHoliday extends AbstractRecordInfo implements RecordNoon {
        public RecordNoonPublicHoliday(Person person) {
            super(person, AbsenceType.PUBLIC_HOLIDAY, AbsenceStatus.ACTIVE);
        }
    }
}
