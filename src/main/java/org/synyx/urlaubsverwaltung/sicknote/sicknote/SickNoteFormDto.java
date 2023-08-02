package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.springframework.format.annotation.DateTimeFormat;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.DD_MM_YYYY;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.D_M_YY;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.D_M_YYYY;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.ISO_DATE;

public class SickNoteFormDto {

    private Long id;
    private Person person;
    private SickNoteType sickNoteType;
    @DateTimeFormat(pattern = DD_MM_YYYY, fallbackPatterns = {D_M_YY, D_M_YYYY, ISO_DATE})
    private LocalDate startDate;
    @DateTimeFormat(pattern = DD_MM_YYYY, fallbackPatterns = {D_M_YY, D_M_YYYY, ISO_DATE})
    private LocalDate endDate;
    private DayLength dayLength = FULL;
    @DateTimeFormat(pattern = DD_MM_YYYY, fallbackPatterns = {D_M_YY, D_M_YYYY, ISO_DATE})
    private LocalDate aubStartDate;
    @DateTimeFormat(pattern = DD_MM_YYYY, fallbackPatterns = {D_M_YY, D_M_YYYY, ISO_DATE})
    private LocalDate aubEndDate;
    private String comment;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public SickNoteType getSickNoteType() {
        return sickNoteType;
    }

    public void setSickNoteType(SickNoteType sickNoteType) {
        this.sickNoteType = sickNoteType;
    }

    public String getStartDateIsoValue() {
        return startDate == null ? "" : startDate.format(DateTimeFormatter.ISO_DATE);
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public String getEndDateIsoValue() {
        return endDate == null ? "" : endDate.format(DateTimeFormatter.ISO_DATE);
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public DayLength getDayLength() {
        return dayLength;
    }

    public void setDayLength(DayLength dayLength) {
        this.dayLength = dayLength;
    }

    public LocalDate getAubStartDate() {
        return aubStartDate;
    }

    public String getAubStartDateIsoValue() {
        return aubStartDate == null ? "" : aubStartDate.format(DateTimeFormatter.ISO_DATE);
    }

    public void setAubStartDate(LocalDate aubStartDate) {
        this.aubStartDate = aubStartDate;
    }

    public LocalDate getAubEndDate() {
        return aubEndDate;
    }

    public String getAubEndDateIsoValue() {
        return aubEndDate == null ? "" : aubEndDate.format(DateTimeFormatter.ISO_DATE);
    }

    public void setAubEndDate(LocalDate aubEndDate) {
        this.aubEndDate = aubEndDate;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "SickNoteForm{" +
            "id=" + id +
            ", person=" + person +
            ", sickNoteType=" + sickNoteType +
            ", startDate=" + startDate +
            ", endDate=" + endDate +
            ", dayLength=" + dayLength +
            ", aubStartDate=" + aubStartDate +
            ", aubEndDate=" + aubEndDate + '\'' +
            '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Person person;
        private SickNoteType sickNoteType;
        private LocalDate startDate;
        private LocalDate endDate;
        private DayLength dayLength = FULL;
        private LocalDate aubStartDate;
        private LocalDate aubEndDate;
        private String comment;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder person(Person person) {
            this.person = person;
            return this;
        }

        public Builder sickNoteType(SickNoteType sickNoteType) {
            this.sickNoteType = sickNoteType;
            return this;
        }

        public Builder startDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder endDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder dayLength(DayLength dayLength) {
            this.dayLength = dayLength;
            return this;
        }

        public Builder aubStartDate(LocalDate aubStartDate) {
            this.aubStartDate = aubStartDate;
            return this;
        }

        public Builder aubEndDate(LocalDate aubEndDate) {
            this.aubEndDate = aubEndDate;
            return this;
        }

        public Builder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public SickNoteFormDto build() {
            final SickNoteFormDto sickNoteFormDto = new SickNoteFormDto();
            sickNoteFormDto.setId(id);
            sickNoteFormDto.setPerson(person);
            sickNoteFormDto.setSickNoteType(sickNoteType);
            sickNoteFormDto.setStartDate(startDate);
            sickNoteFormDto.setEndDate(endDate);
            sickNoteFormDto.setDayLength(dayLength);
            sickNoteFormDto.setAubStartDate(aubStartDate);
            sickNoteFormDto.setAubEndDate(aubEndDate);
            sickNoteFormDto.setComment(comment);
            return sickNoteFormDto;
        }
    }
}
