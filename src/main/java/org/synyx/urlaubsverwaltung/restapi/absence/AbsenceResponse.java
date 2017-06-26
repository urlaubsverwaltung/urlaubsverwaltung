package org.synyx.urlaubsverwaltung.restapi.absence;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteType;
import org.synyx.urlaubsverwaltung.restapi.person.PersonResponse;
import org.synyx.urlaubsverwaltung.restapi.RestApiDateFormat;

import java.math.BigDecimal;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class AbsenceResponse {

    private String from;
    private String to;
    private BigDecimal dayLength;
    private PersonResponse person;
    private String type;
    private String typeName;
    private String status;

    public AbsenceResponse(Application application) {

        this.from = application.getStartDate().toString(RestApiDateFormat.DATE_PATTERN);
        this.to = application.getEndDate().toString(RestApiDateFormat.DATE_PATTERN);
        this.dayLength = application.getDayLength().getDuration();
        this.person = new PersonResponse(application.getPerson());
        this.status = application.getStatus().name();

        VacationType vacationType = application.getVacationType();
        this.type = vacationType.getCategory().toString();
        this.typeName = vacationType.getDisplayName();
    }


    public AbsenceResponse(SickNote sickNote) {

        this.from = sickNote.getStartDate().toString(RestApiDateFormat.DATE_PATTERN);
        this.to = sickNote.getEndDate().toString(RestApiDateFormat.DATE_PATTERN);
        this.dayLength = sickNote.getDayLength().getDuration();
        this.person = new PersonResponse(sickNote.getPerson());
        this.status = sickNote.isActive() ? "ACTIVE" : "INACTIVE";

        SickNoteType sickNoteType = sickNote.getSickNoteType();

        this.type = sickNoteType.getCategory().toString();
        this.typeName = sickNoteType.getDisplayName();
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


    public String getTypeName() {

        return typeName;
    }


    public void setTypeName(String typeName) {

        this.typeName = typeName;
    }


    public String getStatus() {

        return status;
    }


    public void setStatus(String status) {

        this.status = status;
    }
}
