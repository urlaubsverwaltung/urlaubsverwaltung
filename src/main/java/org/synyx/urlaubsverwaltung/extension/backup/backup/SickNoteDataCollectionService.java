package org.synyx.urlaubsverwaltung.extension.backup.backup;

import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.extension.backup.model.DayLengthDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.SickNoteBackupDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.SickNoteCommentActionDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.SickNoteCommentDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.SickNoteDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.SickNoteExtensionHistoryDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.SickNoteExtensionStatusDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.SickNoteStatusDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.SickNoteTypeCategoryDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.SickNoteTypeDTO;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.extend.SickNoteExtensionHistoryService;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteTypeService;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.synyx.urlaubsverwaltung.extension.backup.backup.PersonHelper.optionalExternalUserId;

@Service
@ConditionalOnBackupCreateEnabled
class SickNoteDataCollectionService {

    private final SickNoteTypeService sickNoteTypeService;
    private final SickNoteService sickNoteService;
    private final SickNoteCommentService sickNoteCommentService;
    private final SickNoteExtensionHistoryService sickNoteExtensionHistoryService;

    SickNoteDataCollectionService(
        SickNoteTypeService sickNoteTypeService,
        SickNoteService sickNoteService,
        SickNoteCommentService sickNoteCommentService,
        SickNoteExtensionHistoryService sickNoteExtensionHistoryService
    ) {
        this.sickNoteService = sickNoteService;
        this.sickNoteTypeService = sickNoteTypeService;
        this.sickNoteCommentService = sickNoteCommentService;
        this.sickNoteExtensionHistoryService = sickNoteExtensionHistoryService;
    }

    SickNoteBackupDTO collectSickNotes(List<Person> allPersons, LocalDate from, LocalDate to) {
        final List<SickNoteTypeDTO> sickNoteTypeDTOS = sickNoteTypeService.getSickNoteTypes().stream()
            .map(sickNoteType -> new SickNoteTypeDTO(sickNoteType.getId(), SickNoteTypeCategoryDTO.valueOf(sickNoteType.getCategory().name()), sickNoteType.getMessageKey()))
            .toList();

        final List<SickNoteDTO> sickNoteDTOs = allPersons.stream()
            .map(person ->
                sickNoteService.getByPersonAndPeriod(person, from, to).stream()
                    .map(sickNote -> {
                        final List<SickNoteExtensionHistoryDTO> sickNoteExtensionHistoryDTOs = sickNoteExtensionHistoryService.getSickNoteExtensionHistory(sickNote.getId())
                            .stream()
                            .map(history -> new SickNoteExtensionHistoryDTO(history.createdAt(), history.newEndDate(), history.isAub(), SickNoteExtensionStatusDTO.valueOf(history.status().name()))).toList();

                        final List<SickNoteCommentDTO> sickNoteCommentDTOs = sickNoteCommentService.getCommentsBySickNote(sickNote).stream()
                            .map(sickNoteComment -> new SickNoteCommentDTO(sickNoteComment.getDate(), sickNoteComment.getText(), SickNoteCommentActionDTO.valueOf(sickNoteComment.getAction().name()), optionalExternalUserId(sickNoteComment.getPerson())))
                            .toList();

                        // is an optional field, so it can be null
                        final String applierUsername = optionalExternalUserId(sickNote.getApplier());

                        return new SickNoteDTO(
                            sickNote.getId(),
                            sickNote.getPerson().getUsername(),
                            applierUsername,
                            sickNote.getSickNoteType().getId(),
                            sickNote.getStartDate(),
                            sickNote.getEndDate(),
                            DayLengthDTO.valueOf(sickNote.getDayLength().name()),
                            sickNote.getAubStartDate(),
                            sickNote.getAubEndDate(),
                            sickNote.getLastEdited(),
                            sickNote.getEndOfSickPayNotificationSend(),
                            SickNoteStatusDTO.valueOf(sickNote.getStatus().name()),
                            sickNoteCommentDTOs, sickNoteExtensionHistoryDTOs
                        );
                    })
                    .toList()
            )
            .flatMap(Collection::stream)
            .toList();

        return new SickNoteBackupDTO(sickNoteTypeDTOS, sickNoteDTOs);
    }
}
