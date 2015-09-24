package org.synyx.urlaubsverwaltung.web.sicknote;

import lombok.Data;
import org.joda.time.DateMidnight;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;


/**
 * Represents a form to convert a sick note to vacation.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Data
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

    public Application generateApplicationForLeave() {

        Application applicationForLeave = new Application();

        applicationForLeave.setPerson(person);

        applicationForLeave.setVacationType(vacationType);

        applicationForLeave.setDayLength(DayLength.FULL);
        applicationForLeave.setStartDate(startDate);
        applicationForLeave.setEndDate(endDate);

        applicationForLeave.setStatus(ApplicationStatus.ALLOWED);
        applicationForLeave.setApplicationDate(DateMidnight.now());
        applicationForLeave.setEditedDate(DateMidnight.now());

        return applicationForLeave;
    }
}
