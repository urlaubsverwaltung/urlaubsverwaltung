package org.synyx.urlaubsverwaltung.sicknote;

import org.joda.time.DateMidnight;

import org.synyx.urlaubsverwaltung.application.domain.Application;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class SickNoteConversion {

    private DateMidnight sickNoteStartDate;
    private DateMidnight sickNoteEndDate;

    private DateMidnight appStartDate;
    private DateMidnight appEndDate;

    public SickNoteConversion(SickNote sickNote, Application application) {

        this.sickNoteStartDate = sickNote.getStartDate();
        this.sickNoteEndDate = sickNote.getEndDate();
        this.appStartDate = application.getStartDate();
        this.appEndDate = application.getEndDate();
    }

    public boolean onlyStartIsEqual() {

        if (sickNoteStartDate.isEqual(appStartDate) && !sickNoteEndDate.isEqual(appEndDate)) {
            return true;
        }

        return false;
    }


    public boolean onlyEndIsEqual() {

        if (!sickNoteStartDate.isEqual(appStartDate) && sickNoteEndDate.isEqual(appEndDate)) {
            return true;
        }

        return false;
    }


    public boolean identicRange() {

        if (sickNoteStartDate.isEqual(appStartDate) && sickNoteEndDate.isEqual(appEndDate)) {
            return true;
        }

        return false;
    }


    public boolean overlapping() {

        if (!sickNoteStartDate.isEqual(appStartDate) && !sickNoteEndDate.isEqual(appEndDate)) {
            return true;
        }

        return false;
    }
}
