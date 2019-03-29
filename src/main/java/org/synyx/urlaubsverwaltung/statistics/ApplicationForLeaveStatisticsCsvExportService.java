package org.synyx.urlaubsverwaltung.statistics;

import liquibase.util.csv.CSVWriter;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;
import org.synyx.urlaubsverwaltung.web.statistics.ApplicationForLeaveStatistics;

import java.util.List;

public interface ApplicationForLeaveStatisticsCsvExportService {

    void writeStatistics(FilterPeriod period, List<ApplicationForLeaveStatistics> statistics, CSVWriter csvWriter);

    String getFileName(FilterPeriod period);
}
