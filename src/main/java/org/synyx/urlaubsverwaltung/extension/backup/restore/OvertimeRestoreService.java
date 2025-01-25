package org.synyx.urlaubsverwaltung.extension.backup.restore;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.extension.backup.model.OvertimeCommentDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.OvertimeDTO;
import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.overtime.OvertimeComment;
import org.synyx.urlaubsverwaltung.overtime.OvertimeImportService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.List;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Service
@ConditionalOnBackupRestoreEnabled
class OvertimeRestoreService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final OvertimeImportService overtimeImportService;
    private final PersonService personService;

    OvertimeRestoreService(OvertimeImportService overtimeImportService, PersonService personService) {
        this.overtimeImportService = overtimeImportService;
        this.personService = personService;
    }

    void restore(List<OvertimeDTO> overtimes) {
        overtimes.forEach(overtimeDTO -> personService.getPersonByUsername(overtimeDTO.externalIdOfOwner()).ifPresentOrElse(person -> {
            final Overtime importedOvertime = overtimeImportService.importOvertime(overtimeDTO.toOverTime(person));
            importOvertimeComments(importedOvertime, overtimeDTO.overtimeComments());
        }, () -> LOG.warn("overtime owner with externalId={} not found - skip importing overtime!", overtimeDTO.externalIdOfOwner())));

    }

    private void importOvertimeComments(Overtime importedOvertime, List<OvertimeCommentDTO> overtimeCommentDTOS) {
        overtimeCommentDTOS.forEach(commentDTO -> {
            // it can happen that the comment autor was deleted in the past
            // and so there will no person be found for the given externalId
            final Person commentAutor = findOptionalPerson(commentDTO);
            final OvertimeComment overtimeComment = commentDTO.toOvertimeComment(importedOvertime, commentAutor);
            overtimeImportService.importOvertimeComment(overtimeComment);
        });
    }

    private Person findOptionalPerson(OvertimeCommentDTO commentDTO) {
        return Optional.ofNullable(commentDTO.externalIdOfCommentAuthor())
            .flatMap(personService::getPersonByUsername)
            .orElse(null);
    }
}
