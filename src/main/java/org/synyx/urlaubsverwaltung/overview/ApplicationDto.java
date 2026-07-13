package org.synyx.urlaubsverwaltung.overview;

import org.synyx.urlaubsverwaltung.application.application.ApplicationStatus;
import org.synyx.urlaubsverwaltung.period.DayLength;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.SortedMap;

record ApplicationDto(
    Long id,
    Long personId,
    ApplicationStatus status,
    ApplicationVacationTypeDto vacationType,
    LocalDate applicationDate,
    LocalDate startDate,
    LocalDate endDate,
    LocalTime startTime,
    LocalTime endTime,
    ZonedDateTime startDateWithTime,
    ZonedDateTime endDateWithTime,
    DayOfWeek weekDayOfStartDate,
    DayOfWeek weekDayOfEndDate,
    DayLength dayLength,
    BigDecimal workDays,
    SortedMap<Integer, BigDecimal> workDaysByYear,
    Duration hours,
    LocalDate editedDate,
    LocalDate cancelDate,
    List<PersonDto> holidayReplacements,
    boolean allowedToEdit,
    boolean allowedToRevoke,
    boolean allowedToCancel,
    boolean allowedToCancelDirectly,
    boolean allowedToStartCancellationRequest
) {
}
