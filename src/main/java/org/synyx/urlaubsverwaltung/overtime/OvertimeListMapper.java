package org.synyx.urlaubsverwaltung.overtime;

import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarSupplier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Collections.reverse;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Stream.concat;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.overtime.OvertimeListRecordDto.OvertimeListRecordType.ABSENCE;
import static org.synyx.urlaubsverwaltung.overtime.OvertimeListRecordDto.OvertimeListRecordType.OVERTIME;

final class OvertimeListMapper {

    private OvertimeListMapper() {
        // ok
    }

    static OvertimeListDto mapToDto(
        List<Application> overtimeAbsences,
        List<Overtime> overtimes,
        Duration totalOvertime,
        Duration totalOvertimeLastYear,
        Duration leftOvertime,
        Person signedInUser,
        Predicate<Overtime> isUserIsAllowedToEditOvertime,
        WorkingTimeCalendarSupplier workingTimeCalendarSupplier,
        int selectedYear
    ) {

        final List<OvertimeListRecordDto> overtimeListRecordDtos = new ArrayList<>();
        Duration sum = totalOvertimeLastYear;

        final List<OvertimeListRecordDto> allOvertimes = orderedOvertimesAndAbsences(overtimeAbsences, overtimes, signedInUser, workingTimeCalendarSupplier, isUserIsAllowedToEditOvertime);
        for (final OvertimeListRecordDto overtimeEntry : allOvertimes) {
            sum = sum.plus(overtimeEntry.getDurationByYear().getOrDefault(selectedYear, Duration.ZERO));
            overtimeListRecordDtos.add(new OvertimeListRecordDto(overtimeEntry, sum, overtimeEntry.getDurationByYear()));
        }

        reverse(overtimeListRecordDtos);

        return new OvertimeListDto(overtimeListRecordDtos, totalOvertime, totalOvertimeLastYear, leftOvertime);
    }

    private static List<OvertimeListRecordDto> orderedOvertimesAndAbsences(
        List<Application> overtimeAbsences,
        List<Overtime> overtimes,
        Person signInUser,
        WorkingTimeCalendarSupplier workingTimeCalendarSupplier,
        Predicate<Overtime> isUserIsAllowedToEditOvertime
    ) {

        final Stream<OvertimeListRecordDto> byOvertimes = byOvertimes(overtimes, isUserIsAllowedToEditOvertime);
        final Stream<OvertimeListRecordDto> byAbsences = byAbsences(overtimeAbsences, signInUser, workingTimeCalendarSupplier);

        return concat(byOvertimes, byAbsences)
            .sorted(comparing(OvertimeListRecordDto::getStartDate))
            .toList();
    }

    private static Stream<OvertimeListRecordDto> byAbsences(
        List<Application> overtimeAbsences,
        Person signInUser,
        WorkingTimeCalendarSupplier workingTimeCalendarSupplier
    ) {
        return overtimeAbsences.stream()
            .map(application -> {
                final Map<Integer, Duration> overtimeDurationByYear = application.getHoursByYear(workingTimeCalendarSupplier)
                    .entrySet().stream()
                    .collect(toMap(
                        Map.Entry::getKey,
                        // negate value because we're calculating overtime duration.
                        // overtime reduction of 8 hours -> overtime duration of -8 hours
                        entry -> entry.getValue().negated()
                    ));

                return new OvertimeListRecordDto(
                    application.getId(),
                    application.getStartDate(),
                    application.getEndDate(),
                    application.getHours().negated(),
                    overtimeDurationByYear,
                    Duration.ZERO,
                    application.getStatus().name(),
                    application.getVacationType().getColor().name(),
                    ABSENCE.name(),
                    false,
                    application.getPerson().equals(signInUser) && application.hasStatus(WAITING));
            });
    }

    private static Stream<OvertimeListRecordDto> byOvertimes(List<Overtime> overtimes, Predicate<Overtime> isUserIsAllowedToEditOvertime) {
        return overtimes.stream()
            .map(overtime ->
                new OvertimeListRecordDto(
                    overtime.id().value(),
                    overtime.startDate(),
                    overtime.endDate(),
                    overtime.duration(),
                    overtime.getDurationByYear(),
                    Duration.ZERO,
                    "",
                    "",
                    OVERTIME.name(),
                    overtime.type().equals(OvertimeType.EXTERNAL),
                    isUserIsAllowedToEditOvertime.test(overtime)
                )
            );
    }
}
