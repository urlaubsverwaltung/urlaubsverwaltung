package org.synyx.urlaubsverwaltung.sicknote.sicknote;

public class SickNoteMapper {

    private SickNoteMapper() {
        // ok
    }

    static SickNote merge(SickNote sickNote, SickNoteForm sickNoteForm) {
        return SickNote.builder(sickNote)
                .person(sickNoteForm.getPerson())
                .sickNoteType(sickNoteForm.getSickNoteType())
                .startDate(sickNoteForm.getStartDate())
                .endDate(sickNoteForm.getEndDate())
                .dayLength(sickNoteForm.getDayLength())
                .aubStartDate(sickNoteForm.getAubStartDate())
                .aubEndDate(sickNoteForm.getAubEndDate())
                .build();
    }
}
