package org.synyx.urlaubsverwaltung.statistics.web;

import org.synyx.urlaubsverwaltung.statistics.ApplicationForLeaveStatistics;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.util.List;

public interface ApplicationForLeaveStatisticsService {
    List<ApplicationForLeaveStatistics> getStatistics(FilterPeriod period);
}
