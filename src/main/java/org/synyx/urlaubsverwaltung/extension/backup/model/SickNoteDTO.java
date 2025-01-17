package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteEntity;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;

import java.time.LocalDate;
import java.util.List;

public record SickNoteDTO(Long id, String externalIdOfPerson, String externalIdOfApplier, Long sickNoteTypeId,
                          LocalDate startDate,
                          LocalDate endDate,
                          DayLengthDTO dayLength, LocalDate aubStartDate, LocalDate aubEndDate, LocalDate lastEdited,
                          LocalDate endOfSickPayNotificationSend, SickNoteStatusDTO sickNoteStatus,
                          List<SickNoteCommentDTO> sickNoteComments,
                          List<SickNoteExtensionHistoryDTO> sickNoteExtensionHistoryItems) {

    public SickNoteEntity toSickNoteEntity(SickNoteType sickNoteType, Person person, Person applier) {
        final SickNoteEntity entity = new SickNoteEntity();
        entity.setPerson(person);
        entity.setApplier(applier);
        entity.setSickNoteType(sickNoteType);
        entity.setStartDate(this.startDate);
        entity.setEndDate(this.endDate);
        entity.setDayLength(this.dayLength.toDayLength());
        entity.setAubStartDate(this.aubStartDate);
        entity.setAubEndDate(this.aubEndDate);
        entity.setLastEdited(this.lastEdited);
        entity.setEndOfSickPayNotificationSend(this.endOfSickPayNotificationSend);
        entity.setStatus(this.sickNoteStatus.toSickNoteStatus());
        return entity;
    }
}
