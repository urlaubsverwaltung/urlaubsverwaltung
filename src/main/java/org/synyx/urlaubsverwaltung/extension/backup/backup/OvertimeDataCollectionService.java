package org.synyx.urlaubsverwaltung.extension.backup.backup;

import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.extension.backup.model.OvertimeCommentDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.OvertimeDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.PersonDTO;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;

import java.util.Collection;
import java.util.List;

@Service
@ConditionalOnBackupCreateEnabled
class OvertimeDataCollectionService {

    private final OvertimeService overtimeService;

    OvertimeDataCollectionService(OvertimeService overtimeService) {
        this.overtimeService = overtimeService;
    }

    List<OvertimeDTO> collectOvertimes(List<PersonDTO> persons) {
        return persons.stream()
            .map(this::createOvertimeDTOS)
            .flatMap(Collection::stream)
            .toList();
    }

    private List<OvertimeDTO> createOvertimeDTOS(PersonDTO person) {
        return overtimeService.getAllOvertimesByPersonId(person.id()).stream()
            .map(overtime -> {
                final List<OvertimeCommentDTO> overtimeCommentDTOs = overtimeService.getCommentsForOvertime(overtime).stream().map(OvertimeCommentDTO::of).toList();
                return OvertimeDTO.of(overtime, person.externalId(), overtimeCommentDTOs);
            })
            .toList();
    }
}
