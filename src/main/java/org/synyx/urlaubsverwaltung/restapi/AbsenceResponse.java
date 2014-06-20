package org.synyx.urlaubsverwaltung.restapi;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
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

    AbsenceResponse(Application application) {

        this.from = application.getStartDate().toString(RestApiDateFormat.PATTERN);
        this.to = application.getEndDate().toString(RestApiDateFormat.PATTERN);
        this.dayLength = application.getHowLong().getDuration();
        this.person = new PersonResponse(application.getPerson());
    }


    AbsenceResponse(SickNote sickNote) {

        this.from = sickNote.getStartDate().toString(RestApiDateFormat.PATTERN);
        this.to = sickNote.getEndDate().toString(RestApiDateFormat.PATTERN);
        this.dayLength = DayLength.FULL.getDuration();
        this.person = new PersonResponse(sickNote.getPerson());
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
}
