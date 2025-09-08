package org.synyx.urlaubsverwaltung.extension.backup.model;


import org.synyx.urlaubsverwaltung.overtime.OvertimeComment;
import org.synyx.urlaubsverwaltung.overtime.OvertimeCommentEntity;
import org.synyx.urlaubsverwaltung.overtime.OvertimeEntity;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.function.Function;

import static java.util.Objects.requireNonNullElse;

public record OvertimeCommentDTO(Instant date, String text, OvertimeCommentActionDTO action,
                                 String externalIdOfCommentAuthor) {

    public static OvertimeCommentDTO of(OvertimeComment overtimeComment, Function<PersonId, Person> personById) {

        final String externalIdOfCommentAuthor = overtimeComment.personId()
            .map(personById)
            .map(Person::getUsername)
            .orElse(null);

        return new OvertimeCommentDTO(overtimeComment.createdAt(), overtimeComment.text(), OvertimeCommentActionDTO.valueOf(overtimeComment.action().name()), externalIdOfCommentAuthor);
    }

    public OvertimeCommentEntity toOvertimeComment(OvertimeEntity overtime, Person commentAuthor) {
        final OvertimeCommentEntity overtimeComment = new OvertimeCommentEntity(Clock.fixed(this.date(), ZoneId.systemDefault()));
        overtimeComment.setPerson(commentAuthor);
        overtimeComment.setOvertime(overtime);
        overtimeComment.setAction(this.action.toOvertimeCommentAction());
        overtimeComment.setText(requireNonNullElse(this.text(), ""));

        return overtimeComment;
    }
}
