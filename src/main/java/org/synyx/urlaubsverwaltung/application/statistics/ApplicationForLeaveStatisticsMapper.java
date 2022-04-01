package org.synyx.urlaubsverwaltung.application.statistics;

import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;

final class ApplicationForLeaveStatisticsMapper {

    private ApplicationForLeaveStatisticsMapper() {
        // prevents init
    }

    static ApplicationForLeaveStatisticsDto mapToApplicationForLeaveStatisticsDto(ApplicationForLeaveStatistics applicationForLeaveStatistics) {
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
            applicationForLeaveStatistics.getLeftOvertime()
        );
    }
}
