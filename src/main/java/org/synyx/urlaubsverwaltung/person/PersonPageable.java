package org.synyx.urlaubsverwaltung.person;

import org.springframework.data.core.TypedPropertyPath;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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

        // e.g. if content should be sorted by firstName, use lastName as second sort criteria
        final Sort implicitSort;

        // actually we would have to check whether lastName is already in requestedSort or not.
        // however, practically only firstName OR lastName is sortable currently.
        if (firstNameOrder != null) {
            final TypedPropertyPath<Person, ?> path = TypedPropertyPath.path(PersonSortProperty.LAST_NAME.propertyExtractor());
            implicitSort = requestedSort.and(Sort.by(firstNameOrder.isAscending() ? Sort.Direction.ASC : Sort.Direction.DESC, path));
        } else if (lastNameOrder != null) {
            final TypedPropertyPath<Person, ?> path = TypedPropertyPath.path(PersonSortProperty.FIRST_NAME.propertyExtractor());
            implicitSort = requestedSort.and(Sort.by(lastNameOrder.isAscending() ? Sort.Direction.ASC : Sort.Direction.DESC, path));
        } else {
            implicitSort = requestedSort;
        }

        return implicitSort;
    }
}
