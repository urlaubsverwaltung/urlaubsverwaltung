package org.synyx.urlaubsverwaltung.application.statistics;

import org.springframework.context.MessageSource;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.util.DurationFormatter;

import java.util.Locale;

final class ApplicationForLeaveStatisticsMapper {

    private ApplicationForLeaveStatisticsMapper() {
        // prevents init
    }

    static ApplicationForLeaveStatisticsDto mapToApplicationForLeaveStatisticsDto(ApplicationForLeaveStatistics statistics, Locale locale, MessageSource messageSource) {
        return new ApplicationForLeaveStatisticsDto(
            statistics.getPerson().getFirstName(),
            statistics.getPerson().getLastName(),
            statistics.getPerson().getNiceName(),
            statistics.getPerson().getGravatarURL(),
            statistics.getPersonBasedata().map(PersonBasedata::getPersonnelNumber).orElse(""),
            statistics.getTotalAllowedVacationDays(),
            statistics.getAllowedVacationDays(),
            statistics.getTotalWaitingVacationDays(),
            statistics.getWaitingVacationDays(),
            statistics.getLeftVacationDaysForPeriod(),
            statistics.getLeftRemainingVacationDaysForPeriod(),
            statistics.getLeftVacationDays(),
            statistics.getLeftRemainingVacationDays(),
            DurationFormatter.toDurationString(statistics.getLeftOvertime(), messageSource, locale)
        );
    }
}
