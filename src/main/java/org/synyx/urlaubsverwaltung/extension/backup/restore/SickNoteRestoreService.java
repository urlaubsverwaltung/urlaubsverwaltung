package org.synyx.urlaubsverwaltung.extension.backup.restore;


import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.extension.backup.model.SickNoteBackupDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.SickNoteDTO;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentImportService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteEntity;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteImportService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.extend.SickNoteExtensionEntity;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.extend.SickNoteExtensionImportService;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteTypeImportService;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteTypeService;

import java.util.List;

@Service
@ConditionalOnBackupRestoreEnabled
class SickNoteRestoreService {

    private final SickNoteImportService sickNoteImportService;
    private final SickNoteTypeImportService sickNoteTypeImportService;
    private final SickNoteCommentImportService sickNoteCommentImportService;
    private final SickNoteExtensionImportService sickNoteExtensionImportService;
    private final SickNoteTypeService sickNoteTypeService;
    private final PersonService personService;

    SickNoteRestoreService(SickNoteImportService sickNoteImportService, SickNoteTypeImportService sickNoteTypeImportService, SickNoteCommentImportService sickNoteCommentImportService, SickNoteExtensionImportService sickNoteExtensionImportService, SickNoteTypeService sickNoteTypeService, PersonService personService) {
        this.sickNoteImportService = sickNoteImportService;
        this.sickNoteTypeImportService = sickNoteTypeImportService;
        this.sickNoteCommentImportService = sickNoteCommentImportService;
        this.sickNoteExtensionImportService = sickNoteExtensionImportService;
        this.sickNoteTypeService = sickNoteTypeService;
        this.personService = personService;
    }

    List<ImportedIdTuple> restore(SickNoteBackupDTO sickNotes) {

        List<ImportedIdTuple> importedSickNoteTypes = importSickNoteTypes(sickNotes);

        return sickNotes.sickNotes().stream().map(sickNoteDTO -> {
            final SickNoteType sickNoteType = findSickNoteType(importedSickNoteTypes, sickNoteDTO.sickNoteTypeId());
            final SickNoteEntity importedSickNote = importSickNote(sickNoteDTO, sickNoteType);
            importSickNoteComments(sickNoteDTO, importedSickNote);
            importSickNoteExtensionHistory(sickNoteDTO, importedSickNote);
            return new ImportedIdTuple(sickNoteDTO.id(), importedSickNote.getId());
        }).toList();
    }

    private List<ImportedIdTuple> importSickNoteTypes(SickNoteBackupDTO sickNotes) {
        return sickNotes.sickNoteTypes().stream().map(sickNoteTypeDTO -> {
            final SickNoteType importedSickNoteType = sickNoteTypeImportService.importSickNoteType(sickNoteTypeDTO.toSickNoteEntity());
            return new ImportedIdTuple(sickNoteTypeDTO.id(), importedSickNoteType.getId());
        }).toList();
    }

    public SickNoteEntity importSickNote(SickNoteDTO sickNoteDTO, SickNoteType sickNoteType) {
        final Person person = findPerson(sickNoteDTO.externalIdOfPerson());
        final Person applier = findPerson(sickNoteDTO.externalIdOfApplier());
        return sickNoteImportService.importSickNote(sickNoteDTO.toSickNoteEntity(sickNoteType, person, applier));
    }

    private void importSickNoteComments(SickNoteDTO sickNoteDTO, SickNoteEntity importedSickNote) {
        sickNoteDTO.sickNoteComments().forEach(sickNoteCommentDTO -> {
            final Person commentator = findPerson(sickNoteCommentDTO.externalIdOfSickNoteCommentAuthor());
            sickNoteCommentImportService.importSickNoteComment(sickNoteCommentDTO.toSickNoteCommentEntity(commentator, importedSickNote.getId()));
        });
    }

    private void importSickNoteExtensionHistory(SickNoteDTO sickNoteDTO, SickNoteEntity importedSickNote) {
        List<SickNoteExtensionEntity> entities = sickNoteDTO.sickNoteExtensionHistoryItems().stream().map(sickNoteExtensionHistoryDTO -> sickNoteExtensionHistoryDTO.toSickNoteExtensionEntity(importedSickNote.getId())).toList();
        sickNoteExtensionImportService.importSickNoteExtension(entities);
    }

    private SickNoteType findSickNoteType(List<ImportedIdTuple> sickNoteTypes, Long sickNoteTypeIdOfBackup) {
        final Long idOfRestoredSickNoteType = sickNoteTypes.stream().filter(sickNoteType -> sickNoteType.idOfBackup().equals(sickNoteTypeIdOfBackup)).findFirst().orElseThrow().idOfRestore();
        return sickNoteTypeService.getSickNoteTypes().stream().filter(sickNoteType -> sickNoteType.getId().equals(idOfRestoredSickNoteType)).findFirst().orElseThrow();
    }

    private Person findPerson(String sickNoteDTO) {
        return personService.getPersonByUsername(sickNoteDTO).orElseThrow();
    }

}
