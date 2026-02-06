package org.synyx.urlaubsverwaltung.application.statistics;

import org.springframework.data.domain.AbstractPageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.ObjectUtils;

import static java.util.Objects.requireNonNullElseGet;

public class ApplicationForLeaveStatisticsPageRequest extends AbstractPageRequest implements ApplicationForLeaveStatisticsPageable {

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
