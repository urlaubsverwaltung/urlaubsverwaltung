package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.synyx.urlaubsverwaltung.api.RestApiDateFormat;
import org.synyx.urlaubsverwaltung.person.api.PersonDto;
import org.synyx.urlaubsverwaltung.person.api.PersonMapper;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class SickNoteDto {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(RestApiDateFormat.DATE_PATTERN);

    private final String from;
    private final String to;
    private final BigDecimal dayLength;
    private final PersonDto person;
    private final String type;
    private final String status;

    SickNoteDto(SickNote sickNote) {
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

    public String getTo() {
        return to;
    }

    public BigDecimal getDayLength() {
        return dayLength;
    }

    public PersonDto getPerson() {
        return person;
    }

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }
}
