package org.synyx.urlaubsverwaltung.overtime.web;

import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.math.RoundingMode.HALF_UP;
import static java.util.Collections.reverse;
import static java.util.Comparator.comparing;
import static java.util.HashMap.newHashMap;
import static java.util.stream.Stream.concat;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.overtime.web.OvertimeListRecordDto.OvertimeListRecordType.ABSENCE;
import static org.synyx.urlaubsverwaltung.overtime.web.OvertimeListRecordDto.OvertimeListRecordType.OVERTIME;

public final class OvertimeListMapper {

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
        WorkingTimeCalendar workingTimeCalendar,
        Predicate<Overtime> isUserIsAllowedToEditOvertime,
        int selectedYear
    ) {

        final List<OvertimeListRecordDto> overtimeListRecordDtos = new ArrayList<>();
        Duration sum = totalOvertimeLastYear;

        final List<OvertimeListRecordDto> allOvertimes = orderedOvertimesAndAbsences(overtimeAbsences, overtimes, signedInUser, workingTimeCalendar, isUserIsAllowedToEditOvertime);
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
        WorkingTimeCalendar workingTimeCalendar,
        Predicate<Overtime> isUserIsAllowedToEditOvertime
    ) {
        return concat(byOvertimes(overtimes, isUserIsAllowedToEditOvertime), byAbsences(overtimeAbsences, signInUser, workingTimeCalendar))
            .sorted(comparing(OvertimeListRecordDto::getStartDate))
            .toList();
    }

    private static Stream<OvertimeListRecordDto> byAbsences(
        List<Application> overtimeAbsences,
        Person signInUser,
        WorkingTimeCalendar workingTimeCalendar
    ) {
        return overtimeAbsences.stream()
            .map(application -> {

                final LocalDate startDate = application.getStartDate();
                final LocalDate endDate = application.getEndDate();

                final Year startYear = Year.from(startDate);
                final Year endYear = Year.from(endDate);

                final Map<Integer, Duration> overtimeReductionDurationByYear =
                    newHashMap(endYear.getValue() - startYear.getValue() + 1);

                final BigDecimal arbeitstageDesAntrags = workingTimeCalendar.workingTime(application);
                // SOLL: 4.5

                final BigDecimal ueberstundenDesAntrags = durationToBigDecimalInHours(application.getHours());
                // SOLL: 32.0

                final BigDecimal ueberstundenAbbauTagesAnteil = ueberstundenDesAntrags.divide(arbeitstageDesAntrags, HALF_UP);
                // SOLL: 7.11

                Year yearPivot = startYear;
                while (yearPivot.equals(endYear) || yearPivot.isBefore(endYear)) {
                    // erster tag des jahres oder startDate
                    final LocalDate from = yearPivot.equals(startYear) ? startDate : yearPivot.atDay(1);
                    final LocalDate to = yearPivot.equals(endYear) ? endDate : yearPivot.atDay(yearPivot.length());

                    // überstundenabbau auf mehrere tage (application)
                    // application 27.12.2024 bis 3.1.2025
                    // -> anhand der Arbeitstage die Duration des überstundenabbaus

                    // wenn arbeitstag: 8h oder 4h für halben arbeitstag
                    // wenn kein arbeitstag: 0h

                    // 0, 0.5, 1, 1.5, 2, 2.5, ... Anzahl Tage die gearbeitet wird. Wochenenden und Feiertage sind hier z. B. raus.
                    final BigDecimal arbeitstageImBerechnetenJahr = workingTimeCalendar.workingTime(from, to);
                    // SOLL: 2.5

                    final BigDecimal ueberstundenAbbauImBerechnetenJahr = ueberstundenAbbauTagesAnteil.multiply(arbeitstageImBerechnetenJahr);
                    // SOLL: 17.78

                    final Duration anteiligeUeberstundenDuration = hoursBigDecimalToDuration(ueberstundenAbbauImBerechnetenJahr);
                    // SOLL: 17h 48min

                    overtimeReductionDurationByYear.put(yearPivot.getValue(), anteiligeUeberstundenDuration);
                    yearPivot = yearPivot.plusYears(1);
                }

                return new OvertimeListRecordDto(
                    application.getId(),
                    startDate,
                    endDate,
                    application.getHours().negated(),
                    overtimeReductionDurationByYear,
                    Duration.ZERO,
                    application.getStatus().name(),
                    application.getVacationType().getColor().name(),
                    ABSENCE.name(),
                    application.getPerson().equals(signInUser) && application.hasStatus(WAITING)
                );
            });
    }

    public static BigDecimal durationToBigDecimalInHours(Duration duration) {

        final BigDecimal minutes = BigDecimal.valueOf(duration.toMinutesPart());

        final BigDecimal hoursPart = BigDecimal.valueOf(duration.toHours());
        final BigDecimal minutesPart = minutes.equals(BigDecimal.ZERO) ? BigDecimal.ZERO : BigDecimal.valueOf(60).divide(minutes, HALF_UP).multiply(minutes);

        return hoursPart.add(minutesPart).setScale(2, HALF_UP);
    }

    public static Duration hoursBigDecimalToDuration(BigDecimal hours) {

        final BigDecimal seconds = hours.multiply(BigDecimal.valueOf(3600));
        final long secondsValue = seconds.setScale(0, HALF_UP).longValueExact();

        return Duration.ofSeconds(secondsValue);
    }

    private static Stream<OvertimeListRecordDto> byOvertimes(List<Overtime> overtimes, Predicate<Overtime> isUserIsAllowedToEditOvertime) {
        return overtimes.stream()
            .map(overtime ->
                new OvertimeListRecordDto(
                    overtime.getId(),
                    overtime.getStartDate(),
                    overtime.getEndDate(),
                    overtime.getDuration(),
                    overtime.getDurationByYear(),
                    Duration.ZERO,
                    "",
                    "",
                    OVERTIME.name(),
                    isUserIsAllowedToEditOvertime.test(overtime)
                )
            );
    }
}
