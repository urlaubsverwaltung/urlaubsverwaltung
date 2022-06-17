package org.synyx.urlaubsverwaltung.application.statistics;

import org.springframework.context.MessageSource;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.util.DurationFormatter;

import java.util.Locale;

import java.math.BigDecimal;

final class ApplicationForLeaveStatisticsMapper {

    private ApplicationForLeaveStatisticsMapper() {
        // prevents init
    }

    static ApplicationForLeaveStatisticsDto mapToApplicationForLeaveStatisticsDto(ApplicationForLeaveStatistics statistics, Locale locale, MessageSource messageSource) {

        final BigDecimal totalAllowedVacationDays = statistics.getTotalAllowedVacationDays();
        final BigDecimal totalWaitingVacationDays = statistics.getTotalWaitingVacationDays();

        return new ApplicationForLeaveStatisticsDto(
            statistics.getPerson().getFirstName(),
            statistics.getPerson().getLastName(),
            statistics.getPerson().getNiceName(),
            statistics.getPerson().getGravatarURL(),
            statistics.getPersonBasedata().map(PersonBasedata::getPersonnelNumber).orElse(""),
            totalAllowedVacationDays,
            statistics.getAllowedVacationDays(),
            totalWaitingVacationDays,
            statistics.getWaitingVacationDays(),
            statistics.getLeftPeriodVacationDays(),
            statistics.getLeftVacationDays(),
            DurationFormatter.toDurationString(statistics.getLeftOvertime(), messageSource, locale)
        );
    }
}
