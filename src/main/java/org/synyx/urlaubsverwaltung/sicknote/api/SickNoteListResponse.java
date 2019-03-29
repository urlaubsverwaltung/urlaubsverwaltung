package org.synyx.urlaubsverwaltung.sicknote.api;

import org.synyx.urlaubsverwaltung.absence.api.AbsenceResponse;

import java.util.List;


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
