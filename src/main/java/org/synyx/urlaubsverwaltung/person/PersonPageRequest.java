package org.synyx.urlaubsverwaltung.person;

import org.slf4j.Logger;
import org.springframework.data.domain.AbstractPageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.ObjectUtils;

import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Objects.requireNonNullElseGet;
import static org.slf4j.LoggerFactory.getLogger;

public class PersonPageRequest extends AbstractPageRequest implements PersonPageable {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    public static final String PERSON_PREFIX = "person.";
    public static final String DEFAULT_PERSON_SORT = PERSON_PREFIX + PersonSortProperty.FIRST_NAME_KEY;

    private final Sort personSort;

    private PersonPageRequest(int pageNumber, int pageSize) {
        this(pageNumber, pageSize, null);
    }

    private PersonPageRequest(int pageNumber, int pageSize, Sort sort) {
        super(pageNumber, pageSize);
        this.personSort = requireNonNullElseGet(sort, Sort::unsorted);
    }

    /**
     * {@link PersonPageRequest} implementation to represent the absence of pagination information.
     */
    public static class PersonPageRequestUnpaged extends PersonPageRequest {
        private PersonPageRequestUnpaged() {
            super(0, 1);
        }

        @Override
        public boolean isPaged() {
            return false;
        }
    }

    public static PersonPageRequest unpaged() {
        return new PersonPageRequestUnpaged();
    }

    /**
     * This method simply maps the {@link Pageable} to a {@link PersonPageRequest}. Nothing else considered here.
     *
     * <p>
     * If you are interested in mapping a custom api exposed {@link Pageable} please see
     * {@link PersonPageRequest#ofApiPageable(Pageable)}.
     *
     * @param pageable {@link Pageable} to map
     * @return the {@link PersonPageRequest}
     */
    public static PersonPageRequest of(Pageable pageable) {
        return new PersonPageRequest(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
    }

    /**
     * Unsorted {@link PersonPageRequest} of the given pageNumber and size.
     *
     * @param pageNumber zero-based page number, must not be negative.
     * @param pageSize the size of the page to be returned, must be greater than 0.
     * @return {@link PersonPageRequest}
     */
    public static PersonPageRequest of(int pageNumber, int pageSize) {
        return new PersonPageRequest(pageNumber, pageSize);
    }

    /**
     * Constructor for test purpose only (since not typed...).
     *
     * @param pageNumber zero-based page number, must not be negative.
     * @param pageSize the size of the page to be returned, must be greater than 0.
     * @param sort must not be {@literal null}, use {@link Sort#unsorted()} instead.
     * @return {@link PersonPageRequest}
     */
    public static PersonPageRequest of(int pageNumber, int pageSize, Sort sort) {
        return new PersonPageRequest(pageNumber, pageSize, sort);
    }

    /**
     * This method maps the api exposed {@link Pageable} to a typed {@link PersonPageRequest}.
     *
     * <p>
     * API exposed means, that the {@link Pageable} {@link Sort} has been constructed with property names
     * prefixed with {@link PersonPageRequest#PERSON_PREFIX} followed by {@link PersonSortProperty} values.
     *
     * @param pageable a generic {@link Pageable}
     * @return the mapped {@link PersonPageRequest}, {@link PersonPageRequestUnpaged} when there is no person sort criteria
     */
    public static PersonPageRequest ofApiPageable(Pageable pageable) {

        Sort sort = Sort.unsorted();

        final Sort.TypedSort<Person> personSort = Sort.TypedSort.sort(Person.class);

        for (Sort.Order order : pageable.getSort()) {
            if (order.getProperty().startsWith(PERSON_PREFIX)) {
                final String property = order.getProperty().replace(PERSON_PREFIX, "");
                final Optional<PersonSortProperty> maybeSort = PersonSortProperty.byKey(property);
                if (maybeSort.isPresent()) {
                    final Sort.TypedSort<?> by = personSort.by(maybeSort.get().propertyExtractor());
                    sort = sort.and(order.isAscending() ? by.ascending() : by.descending());
                } else {
                    // error: this should not happen by our program flow (only when client is "experimenting")
                    LOG.error("Could not map person sort property '{}' to domain. Ignoring it.", property);
                }
            }
        }

        if (sort.isUnsorted()) {
            return PersonPageRequest.unpaged();
        }

        return new PersonPageRequest(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    @Override
    public Sort getSort() {
        return personSort;
    }

    @Override
    public Pageable next() {
        return new PersonPageRequest(getPageNumber() + 1, getPageSize(), personSort);
    }

    @Override
    public Pageable previous() {
        return getPageNumber() == 0 ? this : new PersonPageRequest(getPageNumber() - 1, getPageSize(), personSort);
    }

    @Override
    public Pageable first() {
        return new PersonPageRequest(0, getPageSize(), personSort);
    }

    @Override
    public Pageable withPage(int pageNumber) {
        return new PersonPageRequest(pageNumber, getPageSize(), personSort);
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (!(o instanceof PersonPageRequest that)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        return ObjectUtils.nullSafeEquals(personSort, that.personSort);
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 31 + ObjectUtils.nullSafeHash(personSort);
    }

    @Override
    public String toString() {
        return String.format("Page request [number: %d, size %d, sort: %s]", getPageNumber(), getPageSize(), getSort());
    }
}
