package org.synyx.urlaubsverwaltung.person;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.TypedSort;

/**
 * Abstract interface for {@link Person} pagination information.
 * Based on spring {@link org.springframework.data.domain.Pageable}.
 */
public interface PersonPageable {

    default Pageable toPageable() {
        if (this instanceof PersonPageRequest.PersonPageRequestUnpaged) {
            return Pageable.unpaged();
        } else {
            return PageRequest.of(getPageNumber(), getPageSize(), mapToImplicitPersonSort(getSort()));
        }
    }

    int getPageNumber();

    int getPageSize();

    Sort getSort();

    private static Sort mapToImplicitPersonSort(Sort requestedSort) {
        final Sort.Order firstNameOrder = requestedSort.getOrderFor(PersonSortProperty.FIRST_NAME_KEY);
        final Sort.Order lastNameOrder = requestedSort.getOrderFor(PersonSortProperty.LAST_NAME_KEY);

        final TypedSort<Person> personSort = TypedSort.sort(Person.class);

        // e.g. if content should be sorted by firstName, use lastName as second sort criteria
        final Sort implicitSort;

        // actually we would have to check whether lastName is already in requestedSort or not.
        // however, practically only firstName OR lastName is sortable currently.
        if (firstNameOrder != null) {
            final TypedSort<?> sort = personSort.by(PersonSortProperty.LAST_NAME.propertyExtractor());
            implicitSort = requestedSort.and(firstNameOrder.isAscending() ? sort.ascending() : sort.descending());
        } else if (lastNameOrder != null) {
            final TypedSort<?> sort = personSort.by(PersonSortProperty.FIRST_NAME.propertyExtractor());
            implicitSort = requestedSort.and(lastNameOrder.isAscending() ? sort.ascending() : sort.descending());
        } else {
            implicitSort = requestedSort;
        }

        return implicitSort;
    }
}
