package org.synyx.urlaubsverwaltung.overtime.web;

import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.overtime.OvertimeComment;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

class OvertimeDetailsMapper {

    private OvertimeDetailsMapper() {
    }

    static OvertimeDetailsDto mapToDto(Overtime overtime, List<OvertimeComment> comments, Duration totalOvertime, Duration leftOvertime) {

        final Person overtimePerson = overtime.getPerson();
        final OvertimeDetailPersonDto person = new OvertimeDetailPersonDto(overtimePerson.getId(), overtimePerson.getEmail(), overtimePerson.getNiceName(), overtimePerson.getGravatarURL());
        final OvertimeDetailRecordDto record = new OvertimeDetailRecordDto(overtime.getId(), person, overtime.getStartDate(), overtime.getEndDate(), overtime.getDuration(), overtime.getLastModificationDate());

        final List<OvertimeCommentDto> commentDtos = comments.stream()
            .map(OvertimeDetailsMapper::mapComment)
            .collect(Collectors.toList());

        return new OvertimeDetailsDto(record, commentDtos,
            BigDecimal.valueOf(totalOvertime.toMinutes() / 60.0),
            BigDecimal.valueOf(leftOvertime.toMinutes() / 60.0));
    }

    private static OvertimeCommentDto mapComment(OvertimeComment comment) {
        final OvertimeCommentPersonDto personDto = new OvertimeCommentPersonDto(comment.getPerson().getNiceName(), comment.getPerson().getGravatarURL());
        return new OvertimeCommentDto(personDto, comment.getAction().toString(), comment.getDate(), comment.getText());
    }
}
