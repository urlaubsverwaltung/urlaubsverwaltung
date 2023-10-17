package org.synyx.urlaubsverwaltung.comment;

import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Instant;

/**
 * Represents a comment like for an applicationForLeave or a sickNote.
 */
public interface Comment {

    Person person();

    Instant date();

    String text();
}
