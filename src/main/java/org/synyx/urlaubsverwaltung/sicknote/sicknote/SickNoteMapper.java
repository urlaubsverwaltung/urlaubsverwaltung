package org.synyx.urlaubsverwaltung.sicknote.sicknote;

public class SickNoteMapper {

    private SickNoteMapper() {
        // ok
    }

    static SickNote merge(SickNote sickNote, SickNoteFormDto sickNoteFormDto) {
        return SickNote.builder(sickNote)
            .person(sickNoteFormDto.getPerson())
            .sickNoteType(sickNoteFormDto.getSickNoteType())
            .startDate(sickNoteFormDto.getStartDate())
            .endDate(sickNoteFormDto.getEndDate())
            .dayLength(sickNoteFormDto.getDayLength())
            .aubStartDate(sickNoteFormDto.getAubStartDate())
            .aubEndDate(sickNoteFormDto.getAubEndDate())
            .build();
    }
}
