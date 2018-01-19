package org.synyx.urlaubsverwaltung.core.statistics;

import org.synyx.urlaubsverwaltung.web.FilterPeriod;
import org.synyx.urlaubsverwaltung.web.statistics.ApplicationForLeaveStatistics;

import java.util.List;

public interface ApplicationForLeaveStatisticsService {
    List<ApplicationForLeaveStatistics> getStatistics(FilterPeriod period);
}
