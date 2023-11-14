package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import java.util.List;

public class SickNotesDto {

    private final List<SickNoteDto> sickNotes;

    SickNotesDto(List<SickNoteDto> sickNotes) {
        this.sickNotes = sickNotes;
    }

    public List<SickNoteDto> getSickNotes() {
        return sickNotes;
    }
}
