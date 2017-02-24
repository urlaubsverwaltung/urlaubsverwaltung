package org.synyx.urlaubsverwaltung.restapi.sicknote;

import org.synyx.urlaubsverwaltung.restapi.absence.AbsenceResponse;

import java.util.List;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
class SickNoteListResponse {

    private List<AbsenceResponse> sickNotes;

    SickNoteListResponse(List<AbsenceResponse> sickNotes) {

        this.sickNotes = sickNotes;
    }

    public List<AbsenceResponse> getSickNotes() {

        return sickNotes;
    }


    public void setSickNotes(List<AbsenceResponse> sickNotes) {

        this.sickNotes = sickNotes;
    }
}
