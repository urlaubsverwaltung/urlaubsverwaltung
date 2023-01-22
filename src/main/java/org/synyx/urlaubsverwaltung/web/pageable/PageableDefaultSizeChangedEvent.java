package org.synyx.urlaubsverwaltung.web.pageable;

import org.synyx.urlaubsverwaltung.person.PersonId;

import java.util.Objects;

/**
 * Indicates that a {@linkplain org.synyx.urlaubsverwaltung.person.Person} has updated the default value for {@linkplain org.springframework.data.domain.Pageable} size.
 */
public final class PageableDefaultSizeChangedEvent {

    private final PersonId personId;
    private final int newPageableDefaultSize;

    public PageableDefaultSizeChangedEvent(PersonId personId, int newPageableDefaultSize) {
        this.personId = personId;
        this.newPageableDefaultSize = newPageableDefaultSize;
    }

    public PersonId getPersonId() {
        return personId;
    }

    public int getNewPageableDefaultSize() {
        return newPageableDefaultSize;
    }

    @Override
    public String toString() {
        return "PageableDefaultSizeChangedEvent{" +
            "personId=" + personId +
            ", newPageableDefaultSize=" + newPageableDefaultSize +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PageableDefaultSizeChangedEvent that = (PageableDefaultSizeChangedEvent) o;
        return newPageableDefaultSize == that.newPageableDefaultSize && Objects.equals(personId, that.personId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(personId, newPageableDefaultSize);
    }
}
