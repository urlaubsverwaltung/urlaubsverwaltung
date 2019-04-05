package org.synyx.urlaubsverwaltung.sicknote.api;

import java.util.List;


class SickNoteListResponse {

    private List<SickNoteResponse> sickNotes;

    SickNoteListResponse(List<SickNoteResponse> sickNotes) {

        this.sickNotes = sickNotes;
    }

    public List<SickNoteResponse> getSickNotes() {

        return sickNotes;
    }


    public void setSickNotes(List<SickNoteResponse> sickNotes) {

        this.sickNotes = sickNotes;
    }
}
