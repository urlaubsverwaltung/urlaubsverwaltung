package org.synyx.urlaubsverwaltung.overtime;

import org.synyx.urlaubsverwaltung.person.PersonId;

import java.time.Instant;
import java.util.Optional;

/**
 * Describes a comment for a {@link OvertimeEntity}.
 *
 * @param id         overtime comment identifier
 * @param overtimeId referenced {@link OvertimeEntity}
 * @param action     {@link OvertimeCommentAction}
 * @param personId   person identifier who added this comment, {@link Optional#empty()} when person has been deleted meanwhile but not the comment
 * @param createdAt  creation timestamp of this comment
 * @param text       text of this comment
 */
public record OvertimeComment(
    OvertimeCommentId id,
    Long overtimeId,
    OvertimeCommentAction action,
    Optional<PersonId> personId,
    Instant createdAt,
    String text
) {
}
