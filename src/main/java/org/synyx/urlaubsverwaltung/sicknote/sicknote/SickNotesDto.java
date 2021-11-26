package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import java.util.List;

class SickNotesDto {

    private List<SickNoteDto> sickNotes;

    SickNotesDto(List<SickNoteDto> sickNotes) {
        this.sickNotes = sickNotes;
    }

    public List<SickNoteDto> getSickNotes() {
        return sickNotes;
    }

    public void setSickNotes(List<SickNoteDto> sickNotes) {
        this.sickNotes = sickNotes;
    }
}
