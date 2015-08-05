package org.synyx.urlaubsverwaltung.core.sync;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;

import java.util.Date;


/**
 * Represents a period of time where a person is not at work.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class Absence {

    private Date startDate;

    private Date endDate;

    private Person person;

    private boolean isAllDay = false;

    public Absence(Application application) {

        this.startDate = application.getStartDate().toDate();
        this.endDate = application.getEndDate().toDate();
        this.person = application.getPerson();

        if (DayLength.FULL.equals(application.getHowLong())) {
            this.isAllDay = true;
        }
    }


    public Absence(SickNote sickNote) {

        this.startDate = sickNote.getStartDate().toDate();
        this.endDate = sickNote.getEndDate().toDate();
        this.person = sickNote.getPerson();

        // TODO: at the moment sick notes have no day length
        this.isAllDay = true;
    }

    public Date getStartDate() {

        return startDate;
    }


    public Date getEndDate() {

        return endDate;
    }


    public Person getPerson() {

        return person;
    }


    public boolean isAllDay() {

        return isAllDay;
    }
}
