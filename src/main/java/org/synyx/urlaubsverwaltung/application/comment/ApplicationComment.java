package org.synyx.urlaubsverwaltung.application.comment;

import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Instant;
import java.util.Objects;

/**
 * This class describes the information that every step of an {@link Application}'s lifecycle contains.
 */
public record ApplicationComment(
    Long id,
    Instant date,
    Application application,
    ApplicationCommentAction action,
    Person person,
    String text
) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationComment that = (ApplicationComment) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
