package org.synyx.urlaubsverwaltung.extension.backup.backup;

import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.extension.backup.model.OvertimeCommentDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.OvertimeDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.PersonDTO;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

@Service
@ConditionalOnBackupCreateEnabled
class OvertimeDataCollectionService {

    private final OvertimeService overtimeService;

    OvertimeDataCollectionService(OvertimeService overtimeService) {
        this.overtimeService = overtimeService;
    }

    List<OvertimeDTO> collectOvertimes(List<PersonDTO> persons, Function<PersonId, Person> personById) {

        return persons.stream()
            .map(personDto -> createOvertimeDTOS(personDto, personById))
            .flatMap(Collection::stream)
            .toList();
    }

    private List<OvertimeDTO> createOvertimeDTOS(PersonDTO person, Function<PersonId, Person> personById) {
        return overtimeService.getAllOvertimesByPersonId(person.id()).stream()
            .map(overtime -> {

                final List<OvertimeCommentDTO> overtimeCommentDTOs = overtimeService.getCommentsForOvertime(overtime.id()).stream()
                    .map(comment -> OvertimeCommentDTO.of(comment, personById))
                    .toList();

                return OvertimeDTO.of(overtime, person.externalId(), overtimeCommentDTOs);
            })
            .toList();
    }
}
