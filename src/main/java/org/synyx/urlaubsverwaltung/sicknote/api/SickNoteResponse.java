package org.synyx.urlaubsverwaltung.sicknote.api;

import org.synyx.urlaubsverwaltung.api.RestApiDateFormat;
import org.synyx.urlaubsverwaltung.person.api.PersonResponse;
import org.synyx.urlaubsverwaltung.person.api.PersonResponseMapper;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteType;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Objects;


public class SickNoteResponse {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(RestApiDateFormat.DATE_PATTERN);

    private String from;
    private String to;
    private BigDecimal dayLength;
    private PersonResponse person;
    private String type;
    private String status;

    public SickNoteResponse(SickNote sickNote) {

        this.from = formatter.format(sickNote.getStartDate());
        this.to = formatter.format(Objects.requireNonNull(sickNote.getEndDate()));
        this.dayLength = sickNote.getDayLength().getDuration();
        this.person = PersonResponseMapper.mapToResponse(sickNote.getPerson());
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


    public PersonResponse getPerson() {

        return person;
    }


    public void setPerson(PersonResponse person) {

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
