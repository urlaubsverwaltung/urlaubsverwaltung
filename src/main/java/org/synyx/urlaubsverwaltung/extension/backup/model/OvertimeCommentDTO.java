package org.synyx.urlaubsverwaltung.extension.backup.model;


import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.overtime.OvertimeComment;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static java.util.Objects.requireNonNullElse;

public record OvertimeCommentDTO(Instant date, String text, OvertimeCommentActionDTO action,
                                 String externalIdOfCommentAuthor) {

    public static OvertimeCommentDTO of(OvertimeComment overtimeComment) {

        final String externalIdOfCommentAuthor = Optional.ofNullable(overtimeComment.getPerson())
            .map(Person::getUsername)
            .orElse(null);

        return new OvertimeCommentDTO(overtimeComment.getDate(), overtimeComment.getText(), OvertimeCommentActionDTO.valueOf(overtimeComment.getAction().name()), externalIdOfCommentAuthor);
    }

    public OvertimeComment toOvertimeComment(Overtime overtime, Person commentAuthor) {
        final OvertimeComment overtimeComment = new OvertimeComment(Clock.fixed(this.date(), ZoneId.systemDefault()));
        overtimeComment.setPerson(commentAuthor);
        overtimeComment.setOvertime(overtime);
        overtimeComment.setAction(this.action.toOvertimeCommentAction());
        overtimeComment.setText(requireNonNullElse(this.text(), ""));

        return overtimeComment;
    }
}
