package org.synyx.urlaubsverwaltung.application.statistics;

import org.springframework.context.MessageSource;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.util.DurationFormatter;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

final class ApplicationForLeaveStatisticsMapper {

    private ApplicationForLeaveStatisticsMapper() {
        // prevents init
    }

    static ApplicationForLeaveStatisticsDto mapToApplicationForLeaveStatisticsDto(ApplicationForLeaveStatistics statistics, Locale locale, MessageSource messageSource) {
        return new ApplicationForLeaveStatisticsDto(
            statistics.getPerson().getId(),
            statistics.getPerson().getFirstName(),
            statistics.getPerson().getLastName(),
            statistics.getPerson().getNiceName(),
            statistics.getPerson().getGravatarURL(),
            statistics.getPersonBasedata().map(PersonBasedata::personnelNumber).orElse(""),
            statistics.getTotalAllowedVacationDays(),
            toVacationDaysDtoMap(statistics.getAllowedVacationDays(), locale),
            statistics.getTotalWaitingVacationDays(),
            toVacationDaysDtoMap(statistics.getWaitingVacationDays(), locale),
            statistics.getLeftVacationDaysForPeriod(),
            statistics.getLeftRemainingVacationDaysForPeriod(),
            statistics.getLeftVacationDaysForYear(),
            statistics.getLeftRemainingVacationDaysForYear(),
            DurationFormatter.toDurationString(statistics.getLeftOvertimeForYear(), messageSource, locale),
            DurationFormatter.toDurationString(statistics.getLeftOvertimeForPeriod(), messageSource, locale));
    }

    private static Map<ApplicationForLeaveStatisticsVacationTypeDto, BigDecimal> toVacationDaysDtoMap(Map<VacationType<?>, BigDecimal> source, Locale locale) {
        return source.entrySet().stream().collect(toMap(
            entry -> vacationTypeDto(entry.getKey(), locale),
            Map.Entry::getValue
        ));
    }

    private static ApplicationForLeaveStatisticsVacationTypeDto vacationTypeDto(VacationType<?> vacationType, Locale locale) {
        return new ApplicationForLeaveStatisticsVacationTypeDto(vacationType.getLabel(locale));
    }
}
