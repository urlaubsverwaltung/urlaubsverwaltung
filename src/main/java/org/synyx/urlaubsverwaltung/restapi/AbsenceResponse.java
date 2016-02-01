package org.synyx.urlaubsverwaltung.restapi;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;

import java.math.BigDecimal;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
class AbsenceResponse {

    private String from;
    private String to;
    private BigDecimal dayLength;
    private PersonResponse person;
    private String type;
    private String status;

    AbsenceResponse(Application application) {

        this.from = application.getStartDate().toString(RestApiDateFormat.PATTERN);
        this.to = application.getEndDate().toString(RestApiDateFormat.PATTERN);
        this.dayLength = application.getDayLength().getDuration();
        this.person = new PersonResponse(application.getPerson());
        this.type = application.getVacationType().getTypeName();
        this.status = application.getStatus().name();
    }


    AbsenceResponse(SickNote sickNote) {

        this.from = sickNote.getStartDate().toString(RestApiDateFormat.PATTERN);
        this.to = sickNote.getEndDate().toString(RestApiDateFormat.PATTERN);
        this.dayLength = sickNote.getDayLength().getDuration();
        this.person = new PersonResponse(sickNote.getPerson());
        this.type = sickNote.getSickNoteType().getTypeName();
        this.status = sickNote.isActive() ? "ACTIVE" : "INACTIVE";
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
