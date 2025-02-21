package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentEntity;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static java.util.Objects.requireNonNullElse;

public record SickNoteCommentDTO(
    Instant date,
    String text,
    SickNoteCommentActionDTO sickNoteCommentAction,
    String externalIdOfSickNoteCommentAuthor
) {
    public SickNoteCommentEntity toSickNoteCommentEntity(Person commentator, Long sickNoteId) {
        final SickNoteCommentEntity entity = new SickNoteCommentEntity(Clock.fixed(this.date, ZoneId.systemDefault()));
        entity.setSickNoteId(sickNoteId);
        entity.setAction(this.sickNoteCommentAction.toSickNoteCommentAction());
        entity.setPerson(commentator);
        entity.setText(requireNonNullElse(this.text, ""));
        return entity;
    }
}
