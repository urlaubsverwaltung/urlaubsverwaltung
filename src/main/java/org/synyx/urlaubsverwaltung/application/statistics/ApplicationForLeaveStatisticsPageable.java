package org.synyx.urlaubsverwaltung.application.statistics;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface ApplicationForLeaveStatisticsPageable {

    default Pageable toPageable() {
        if (this instanceof ApplicationForLeaveStatisticsPageRequest.ApplicationForLeaveStatisticsPageRequestUnpaged) {
            return Pageable.unpaged();
        } else {
            return PageRequest.of(getPageNumber(), getPageSize(), mapToImplicitSort(getSort()));
        }
    }

    int getPageNumber();

    int getPageSize();

    Sort getSort();

    private static Sort mapToImplicitSort(Sort requestedSort) {
        return requestedSort;
    }
}
