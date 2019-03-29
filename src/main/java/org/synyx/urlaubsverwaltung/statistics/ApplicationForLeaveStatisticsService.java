package org.synyx.urlaubsverwaltung.statistics;

import org.synyx.urlaubsverwaltung.web.FilterPeriod;
import org.synyx.urlaubsverwaltung.statistics.web.ApplicationForLeaveStatistics;

import java.util.List;

public interface ApplicationForLeaveStatisticsService {
    List<ApplicationForLeaveStatistics> getStatistics(FilterPeriod period);
}
