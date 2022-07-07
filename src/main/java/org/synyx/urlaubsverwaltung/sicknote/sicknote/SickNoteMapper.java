package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.springframework.beans.BeanUtils;

public class SickNoteMapper {

    private SickNoteMapper() {
        // ok
    }

    static SickNote merge(SickNote sickNote, SickNoteForm sickNoteForm) {

        final SickNote newSickNote = new SickNote();
        BeanUtils.copyProperties(sickNote, newSickNote);

        newSickNote.setPerson(sickNoteForm.getPerson());
        newSickNote.setSickNoteType(sickNoteForm.getSickNoteType());
        newSickNote.setStartDate(sickNoteForm.getStartDate());
        newSickNote.setEndDate(sickNoteForm.getEndDate());
        newSickNote.setDayLength(sickNoteForm.getDayLength());
        newSickNote.setAubStartDate(sickNoteForm.getAubStartDate());
        newSickNote.setAubEndDate(sickNoteForm.getAubEndDate());
        return newSickNote;
    }
}
