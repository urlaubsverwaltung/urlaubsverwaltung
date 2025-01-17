package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.application.comment.ApplicationCommentEntity;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static java.util.Objects.requireNonNullElse;

/**
 * @param action     Executed action on an application for leave.
 * @param externalId The external Id of the comment author.
 * @param date       The date of the comment.
 * @param text       The text of the comment.
 */
public record ApplicationCommentDTO(ApplicationCommentActionDTO action, String externalId, Instant date, String text) {

    public ApplicationCommentEntity toApplicationCommentEntity(Person author, Long applicationId) {
        final ApplicationCommentEntity entity = new ApplicationCommentEntity(Clock.fixed(this.date, ZoneId.systemDefault()));
        entity.setApplicationId(applicationId);
        entity.setAction(this.action.toApplicationCommentAction());
        entity.setPerson(author);
        entity.setText(requireNonNullElse(this.text, ""));
        return entity;
    }
}
