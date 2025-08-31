package org.synyx.urlaubsverwaltung.overtime;

import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

class OvertimeDetailsMapper {

    private OvertimeDetailsMapper() {
        // ok
    }

    static OvertimeDetailsDto mapToDto(
        OvertimeEntity overtime,
        List<OvertimeComment> comments,
        Duration totalOvertime,
        Duration leftOvertime,
        Function<PersonId, Person> personById
    ) {

        final Person overtimePerson = overtime.getPerson();
        final OvertimeDetailPersonDto person = new OvertimeDetailPersonDto(overtimePerson.getId(), overtimePerson.getEmail(), overtimePerson.getNiceName(), overtimePerson.getInitials(), overtimePerson.getGravatarURL(), overtimePerson.isInactive());
        final OvertimeDetailRecordDto record = new OvertimeDetailRecordDto(overtime.getId(), person, overtime.getStartDate(), overtime.getEndDate(), overtime.getDuration(), overtime.getDurationByYear(), overtime.getLastModificationDate());

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
