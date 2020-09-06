package org.synyx.urlaubsverwaltung.sicknote.api;

import org.synyx.urlaubsverwaltung.api.RestApiDateFormat;
import org.synyx.urlaubsverwaltung.person.api.PersonDto;
import org.synyx.urlaubsverwaltung.person.api.PersonMapper;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteType;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class SickNoteDto {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(RestApiDateFormat.DATE_PATTERN);

    private String from;
    private String to;
    private BigDecimal dayLength;
    private PersonDto person;
    private String type;
    private String status;

    public SickNoteDto(SickNote sickNote) {
        this.from = sickNote.getStartDate().format(formatter);
        this.to = Objects.requireNonNull(sickNote.getEndDate()).format(formatter);
        this.dayLength = sickNote.getDayLength().getDuration();
        this.person = PersonMapper.mapToDto(sickNote.getPerson());
        this.status = sickNote.isActive() ? "ACTIVE" : "INACTIVE";

        SickNoteType sickNoteType = sickNote.getSickNoteType();

        this.type = sickNoteType.getCategory().toString();
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public BigDecimal getDayLength() {
        return dayLength;
    }

    public void setDayLength(BigDecimal dayLength) {
        this.dayLength = dayLength;
    }

    public PersonDto getPerson() {
        return person;
    }

    public void setPerson(PersonDto person) {
        this.person = person;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
