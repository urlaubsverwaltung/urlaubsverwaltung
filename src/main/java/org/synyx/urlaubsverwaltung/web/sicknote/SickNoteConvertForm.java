package org.synyx.urlaubsverwaltung.web.sicknote;

import org.joda.time.DateMidnight;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;

import java.math.BigDecimal;


/**
 * Represents a form to convert a sick note to vacation.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class SickNoteConvertForm {

    private Person person;

    private DateMidnight startDate;

    private DateMidnight endDate;

    private VacationType vacationType;

    private String reason;

    SickNoteConvertForm() {

        // needed for Spring magic
    }


    public SickNoteConvertForm(SickNote sickNote) {

        this.person = sickNote.getPerson();
        this.startDate = sickNote.getStartDate();
        this.endDate = sickNote.getEndDate();
    }

    public Person getPerson() {

        return person;
    }


    public void setPerson(Person person) {

        this.person = person;
    }


    public DateMidnight getStartDate() {

        return startDate;
    }


    public void setStartDate(DateMidnight startDate) {

        this.startDate = startDate;
    }


    public DateMidnight getEndDate() {

        return endDate;
    }


    public void setEndDate(DateMidnight endDate) {

        this.endDate = endDate;
    }


    public VacationType getVacationType() {

        return vacationType;
    }


    public void setVacationType(VacationType vacationType) {

        this.vacationType = vacationType;
    }


    public String getReason() {

        return reason;
    }


    public void setReason(String reason) {

        this.reason = reason;
    }


    public Application generateApplicationForLeave() {

        Application applicationForLeave = new Application();

        applicationForLeave.setPerson(person);

        applicationForLeave.setVacationType(vacationType);

        applicationForLeave.setHowLong(DayLength.FULL);
        applicationForLeave.setStartDate(startDate);
        applicationForLeave.setEndDate(endDate);

        applicationForLeave.setStatus(ApplicationStatus.ALLOWED);
        applicationForLeave.setApplicationDate(DateMidnight.now());
        applicationForLeave.setEditedDate(DateMidnight.now());

        return applicationForLeave;
    }
}
