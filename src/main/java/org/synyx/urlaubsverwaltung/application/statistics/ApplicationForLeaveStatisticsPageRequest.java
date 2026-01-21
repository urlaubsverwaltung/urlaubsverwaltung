package org.synyx.urlaubsverwaltung.application.statistics;

import org.slf4j.Logger;
import org.springframework.data.domain.AbstractPageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.ObjectUtils;

import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Objects.requireNonNullElseGet;
import static org.slf4j.LoggerFactory.getLogger;

public class ApplicationForLeaveStatisticsPageRequest extends AbstractPageRequest implements ApplicationForLeaveStatisticsPageable {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    public static final String STATISTICS_PREFIX = "";

    private final Sort statisticsSort;

    private ApplicationForLeaveStatisticsPageRequest(int pageNumber, int pageSize) {
        this(pageNumber, pageSize, null);
    }

    private ApplicationForLeaveStatisticsPageRequest(int pageNumber, int pageSize, Sort sort) {
        super(pageNumber, pageSize);
        this.statisticsSort = requireNonNullElseGet(sort, Sort::unsorted);
    }

    /**
     * {@link ApplicationForLeaveStatisticsPageRequest} implementation to represent the absence of pagination information.
     */
    public static class ApplicationForLeaveStatisticsPageRequestUnpaged extends ApplicationForLeaveStatisticsPageRequest {
        private ApplicationForLeaveStatisticsPageRequestUnpaged() {
            super(0, 1);
        }

        @Override
        public boolean isPaged() {
            return false;
        }
    }

    public static ApplicationForLeaveStatisticsPageRequest unpaged() {
        return new ApplicationForLeaveStatisticsPageRequest.ApplicationForLeaveStatisticsPageRequestUnpaged();
    }

    /**
     * This method simply maps the {@link Pageable} to a {@link ApplicationForLeaveStatisticsPageRequest}. Nothing else considered here.
     *
     * <p>
     * If you are interested in mapping a custom api exposed {@link Pageable} please see
     * {@link ApplicationForLeaveStatisticsPageRequest#ofApiPageable(Pageable)}.
     *
     * @param pageable {@link Pageable} to map
     * @return the {@link ApplicationForLeaveStatisticsPageRequest}
     */
    public static ApplicationForLeaveStatisticsPageRequest of(Pageable pageable) {
        return new ApplicationForLeaveStatisticsPageRequest(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
    }

    /**
     * Unsorted {@link ApplicationForLeaveStatisticsPageRequest} of the given pageNumber and size.
     *
     * @param pageNumber zero-based page number, must not be negative.
     * @param pageSize the size of the page to be returned, must be greater than 0.
     * @return {@link ApplicationForLeaveStatisticsPageRequest}
     */
    public static ApplicationForLeaveStatisticsPageRequest of(int pageNumber, int pageSize) {
        return new ApplicationForLeaveStatisticsPageRequest(pageNumber, pageSize);
    }

    /**
     * Constructor for test purpose only (since not typed...).
     *
     * @param pageNumber zero-based page number, must not be negative.
     * @param pageSize the size of the page to be returned, must be greater than 0.
     * @param sort must not be {@literal null}, use {@link Sort#unsorted()} instead.
     * @return {@link ApplicationForLeaveStatisticsPageRequest}
     */
    public static ApplicationForLeaveStatisticsPageRequest of(int pageNumber, int pageSize, Sort sort) {
        return new ApplicationForLeaveStatisticsPageRequest(pageNumber, pageSize, sort);
    }

    /**
     * This method maps the api exposed {@link Pageable} to a typed {@link ApplicationForLeaveStatisticsPageRequest}.
     *
     * <p>
     * API exposed means, that the {@link Pageable} {@link Sort} has been constructed with property names
     * prefixed with {@link ApplicationForLeaveStatisticsPageRequest#STATISTICS_PREFIX} followed by {@link ApplicationForLeaveStatisticsSortProperty} values.
     *
     * @param pageable a generic {@link Pageable}
     * @return the mapped {@link ApplicationForLeaveStatisticsPageRequest}, {@link ApplicationForLeaveStatisticsPageRequest.ApplicationForLeaveStatisticsPageRequestUnpaged} when there is no sort criteria
     */
    public static ApplicationForLeaveStatisticsPageRequest ofApiPageable(Pageable pageable) {

        Sort sort = Sort.unsorted();

        final Sort.TypedSort<ApplicationForLeaveStatistics> statisticsSort = Sort.TypedSort.sort(ApplicationForLeaveStatistics.class);

        for (Sort.Order order : pageable.getSort()) {
            // different to PersonPageable, statistics has no prefix. we're simply ignoring values that cannot be mapped.
            final String property = order.getProperty();
            final Optional<ApplicationForLeaveStatisticsSortProperty> maybeSort = ApplicationForLeaveStatisticsSortProperty.byKey(property);
            if (maybeSort.isPresent()) {
                final Sort.TypedSort<?> by = statisticsSort.by(maybeSort.get().propertyExtractor());
                sort = sort.and(order.isAscending() ? by.ascending() : by.descending());
            } else {
                LOG.info("Could not map statistics sort property '{}' to domain. Ignoring it.", property);
            }
        }

        if (sort.isUnsorted()) {
            return ApplicationForLeaveStatisticsPageRequest.unpaged();
        }

        return new ApplicationForLeaveStatisticsPageRequest(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    @Override
    public Sort getSort() {
        return statisticsSort;
    }

    @Override
    public Pageable next() {
        return new ApplicationForLeaveStatisticsPageRequest(getPageNumber() + 1, getPageSize(), statisticsSort);
    }

    @Override
    public Pageable previous() {
        return getPageNumber() == 0 ? this : new ApplicationForLeaveStatisticsPageRequest(getPageNumber() - 1, getPageSize(), statisticsSort);
    }

    @Override
    public Pageable first() {
        return new ApplicationForLeaveStatisticsPageRequest(0, getPageSize(), statisticsSort);
    }

    @Override
    public Pageable withPage(int pageNumber) {
        return new ApplicationForLeaveStatisticsPageRequest(pageNumber, getPageSize(), statisticsSort);
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (!(o instanceof ApplicationForLeaveStatisticsPageRequest that)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        return ObjectUtils.nullSafeEquals(statisticsSort, that.statisticsSort);
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 31 + ObjectUtils.nullSafeHash(statisticsSort);
    }

    @Override
    public String toString() {
        return String.format("Page request [number: %d, size %d, sort: %s]", getPageNumber(), getPageSize(), getSort());
    }
}
