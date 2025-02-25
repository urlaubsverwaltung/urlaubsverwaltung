package org.synyx.urlaubsverwaltung.overtime.web;

import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.overtime.OvertimeComment;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Duration;
import java.util.List;

class OvertimeDetailsMapper {

    private OvertimeDetailsMapper() {
        // ok
    }

    static OvertimeDetailsDto mapToDto(Overtime overtime, List<OvertimeComment> comments, Duration totalOvertime, Duration leftOvertime) {

        final Person overtimePerson = overtime.getPerson();
        final OvertimeDetailPersonDto person = new OvertimeDetailPersonDto(overtimePerson.getId(), overtimePerson.getEmail(), overtimePerson.getNiceName(), overtimePerson.getInitials(), overtimePerson.getGravatarURL(), overtimePerson.isInactive());
        final OvertimeDetailRecordDto record = new OvertimeDetailRecordDto(overtime.getId(), person, overtime.getStartDate(), overtime.getEndDate(), overtime.getDuration(), overtime.getDurationByYear(), overtime.getLastModificationDate());

        final List<OvertimeCommentDto> commentDtos = comments.stream()
            .map(OvertimeDetailsMapper::mapComment)
            .toList();

        return new OvertimeDetailsDto(record, commentDtos, totalOvertime, leftOvertime);
    }

    private static OvertimeCommentDto mapComment(OvertimeComment comment) {
        final OvertimeCommentPersonDto personDto = new OvertimeCommentPersonDto(comment.getPerson().getId(), comment.getPerson().getNiceName(), comment.getPerson().getInitials(), comment.getPerson().getGravatarURL());
        return new OvertimeCommentDto(personDto, comment.getAction().toString(), comment.getDate(), comment.getText());
    }
}
