package org.synyx.urlaubsverwaltung.overtime.web;

import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.reverse;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.overtime.web.OvertimeListRecordDto.OvertimeListRecordType.ABSENCE;
import static org.synyx.urlaubsverwaltung.overtime.web.OvertimeListRecordDto.OvertimeListRecordType.OVERTIME;

final class OvertimeListMapper {

    private OvertimeListMapper() {
        // ok
    }

    static OvertimeListDto mapToDto(List<Application> overtimeAbsences, List<Overtime> overtimes, Duration totalOvertime, Duration totalOvertimeLastYear, Duration leftOvertime, Person signedInUser, boolean isUserIsAllowedToEditOvertime) {

        final List<OvertimeListRecordDto> overtimeListRecordDtos = new ArrayList<>();
        Duration sum = totalOvertimeLastYear;

        final List<OvertimeListRecordDto> allOvertimes = orderedOvertimesAndAbsences(overtimeAbsences, overtimes, signedInUser, isUserIsAllowedToEditOvertime);
        for (OvertimeListRecordDto overtimeEntry : allOvertimes) {
            sum = sum.plus(overtimeEntry.getDuration());
            overtimeListRecordDtos.add(new OvertimeListRecordDto(overtimeEntry, sum));
        }

        reverse(overtimeListRecordDtos);

        return new OvertimeListDto(overtimeListRecordDtos, totalOvertime, totalOvertimeLastYear, leftOvertime);
    }

    private static List<OvertimeListRecordDto> orderedOvertimesAndAbsences(List<Application> overtimeAbsences, List<Overtime> overtimes, Person signInUser, boolean isUserIsAllowedToEditOvertime) {
        return concat(byOvertimes(overtimes, isUserIsAllowedToEditOvertime), byAbsences(overtimeAbsences, signInUser))
            .sorted(comparing(OvertimeListRecordDto::getStartDate))
            .collect(toList());
    }

    private static Stream<OvertimeListRecordDto> byAbsences(List<Application> overtimeAbsences, Person signInUser) {
        return overtimeAbsences.stream()
            .map(application -> new OvertimeListRecordDto(
                application.getId(),
                application.getStartDate(),
                application.getEndDate(),
                application.getHours().negated(),
                Duration.ZERO,
                application.getStatus().name(),
                ABSENCE.name(),
                application.getPerson().equals(signInUser) && application.hasStatus(WAITING))
            );
    }

    private static Stream<OvertimeListRecordDto> byOvertimes(List<Overtime> overtimes, boolean isUserIsAllowedToEditOvertime) {
        return overtimes.stream()
            .map(overtime -> new OvertimeListRecordDto(
                overtime.getId(),
                overtime.getStartDate(),
                overtime.getEndDate(),
                overtime.getDuration(),
                Duration.ZERO,
                "",
                OVERTIME.name(),
                isUserIsAllowedToEditOvertime)
            );
    }
}
