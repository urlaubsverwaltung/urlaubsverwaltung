package org.synyx.urlaubsverwaltung.web.pageable;

import org.synyx.urlaubsverwaltung.person.PersonId;

/**
 * Indicates that a {@linkplain org.synyx.urlaubsverwaltung.person.Person} has updated the default value for {@linkplain org.springframework.data.domain.Pageable} size.
 */
public record PageableDefaultSizeChangedEvent(
    PersonId personId,
    int newPageableDefaultSize
) {
}
