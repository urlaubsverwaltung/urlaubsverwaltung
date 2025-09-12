package org.synyx.urlaubsverwaltung.overtime;

import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.function.Function;

class OvertimeDetailsMapper {

    private OvertimeDetailsMapper() {
        // ok
    }

    static OvertimeDetailsDto mapToDto(
        Overtime overtime,
        List<OvertimeComment> comments,
        Duration totalOvertime,
        Duration leftOvertime,
        Function<PersonId, Person> personById
    ) {

        final Person overtimePerson = personById.apply(overtime.personId());

        final OvertimeDetailPersonDto person = new OvertimeDetailPersonDto(
            overtimePerson.getId(),
            overtimePerson.getEmail(),
            overtimePerson.getNiceName(),
            overtimePerson.getInitials(),
            overtimePerson.getGravatarURL(),
            overtimePerson.isInactive()
        );

        final OvertimeDetailRecordDto record = new OvertimeDetailRecordDto(
            overtime.id().value(),
            person,
            overtime.startDate(),
            overtime.endDate(),
            overtime.duration(),
            overtime.getDurationByYear(),
            LocalDate.ofInstant(overtime.lastModification(), ZoneId.of("Europe/Berlin")),
            overtime.type().equals(OvertimeType.EXTERNAL)
        );

        final List<OvertimeCommentDto> commentDtos = comments.stream()
            .map(comment -> mapComment(comment, personById))
            .toList();

        return new OvertimeDetailsDto(record, commentDtos, totalOvertime, leftOvertime);
    }

    private static OvertimeCommentDto mapComment(OvertimeComment comment, Function<PersonId, Person> personById) {

        final OvertimeCommentPersonDto personDto;

        final Person person = comment.personId().map(personById).orElse(null);
        if (person == null) {
            // person has been deleted meanwhile
            personDto = null;
        } else {
            personDto = new OvertimeCommentPersonDto(person.getId(), person.getNiceName(), person.getInitials(), person.getGravatarURL());
        }

        return new OvertimeCommentDto(personDto, comment.action().toString(), comment.createdAt(), comment.text());
    }
}
