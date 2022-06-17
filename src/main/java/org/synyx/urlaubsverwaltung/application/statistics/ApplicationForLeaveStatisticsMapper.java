package org.synyx.urlaubsverwaltung.application.statistics;

import org.springframework.context.MessageSource;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.util.DurationUtil;

import java.util.Locale;

final class ApplicationForLeaveStatisticsMapper {

    private ApplicationForLeaveStatisticsMapper() {
        // prevents init
    }

    static ApplicationForLeaveStatisticsDto mapToApplicationForLeaveStatisticsDto(ApplicationForLeaveStatistics applicationForLeaveStatistics, Locale locale, MessageSource messageSource) {
        return new ApplicationForLeaveStatisticsDto(
            applicationForLeaveStatistics.getPerson().getFirstName(),
            applicationForLeaveStatistics.getPerson().getLastName(),
            applicationForLeaveStatistics.getPerson().getNiceName(),
            applicationForLeaveStatistics.getPerson().getGravatarURL(),
            applicationForLeaveStatistics.getPersonBasedata().map(PersonBasedata::getPersonnelNumber).orElse(""),
            applicationForLeaveStatistics.getTotalAllowedVacationDays(),
            applicationForLeaveStatistics.getAllowedVacationDays(),
            applicationForLeaveStatistics.getTotalWaitingVacationDays(),
            applicationForLeaveStatistics.getWaitingVacationDays(),
            applicationForLeaveStatistics.getLeftVacationDays(),
            DurationUtil.toDurationString(applicationForLeaveStatistics.getLeftOvertime(), messageSource, locale)
        );
    }
}
