package org.synyx.urlaubsverwaltung.workingtime;

import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;
import static org.synyx.urlaubsverwaltung.period.DayLength.ZERO;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation.WorkingTimeCalendarEntryType.NO_WORKDAY;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation.WorkingTimeCalendarEntryType.WORKDAY;

public class WorkingTimeCalendarFactory {

    /**
     * Creates a {@link WorkingTimeCalendar} with custom working days defined by the provided function.
     * The function should return a {@link WorkingDayInformation} for each date in the range.
     *
     * @param dateRange date range of the calendar
     * @param provider  a function that provides {@link WorkingDayInformation} for each date
     * @return a {@link WorkingTimeCalendar} with custom working days
     */
    public static WorkingTimeCalendar workingTimeCalendar(DateRange dateRange, Function<LocalDate, WorkingDayInformation> provider) {
        return new WorkingTimeCalendar(buildWorkingTimeByDate(dateRange.startDate(), dateRange.endDate(), provider));
    }

    /**
     * Creates a {@link WorkingTimeCalendar} with custom working days defined by the provided function.
     * The function should return a {@link WorkingDayInformation} for each date in the range.
     *
     * @param startDate the start date of the calendar
     * @param endDate   the end date of the calendar
     * @param provider  a function that provides {@link WorkingDayInformation} for each date
     * @return a {@link WorkingTimeCalendar} with custom working days
     */
    public static WorkingTimeCalendar workingTimeCalendar(LocalDate startDate, LocalDate endDate, Function<LocalDate, WorkingDayInformation> provider) {
        return new WorkingTimeCalendar(buildWorkingTimeByDate(startDate, endDate, provider));
    }

    /**
     * Creates a {@link WorkingTimeCalendar} with full working days from Monday to Friday.
     * Weekends (Saturday and Sunday) and possible public holidays are not considered as working days.
     *
     * @param startDate the start date of the calendar
     * @param endDate   the end date of the calendar
     * @return a {@link WorkingTimeCalendar} with full working days from Monday to Friday
     */
    public static WorkingTimeCalendar workingTimeCalendarMondayToFriday(LocalDate startDate, LocalDate endDate) {
        return new WorkingTimeCalendar(buildWorkingTimeByDate(startDate, endDate, WorkingTimeCalendarFactory::fullDayMondayToFriday));
    }

    /**
     * Creates a {@link WorkingTimeCalendar} with full working days from Monday to Sunday.
     * This calendar does not consider weekends or public holidays.
     *
     * @param startDate the start date of the calendar
     * @param endDate   the end date of the calendar
     * @return a {@link WorkingTimeCalendar} with full working days from Monday to Sunday
     */
    public static WorkingTimeCalendar workingTimeCalendarMondayToSunday(LocalDate startDate, LocalDate endDate) {
        return new WorkingTimeCalendar(buildWorkingTimeByDate(startDate, endDate, WorkingTimeCalendarFactory::fullWorkday));
    }

    public static WorkingTimeCalendar workingTimeCalendarMondayToSunday(LocalDate startDate, LocalDate endDate, Function<LocalDate, Boolean> isFullWorkday) {
        return new WorkingTimeCalendar(buildWorkingTimeByDate(startDate, endDate, date -> {
            if (isFullWorkday.apply(date)) {
                return fullWorkday();
            } else {
                return noWorkday();
            }
        }));
    }

    public static WorkingDayInformation noWorkday() {
        return noWorkday(null);
    }

    public static WorkingDayInformation noWorkday(LocalDate date) {
        return new WorkingDayInformation(ZERO, NO_WORKDAY, NO_WORKDAY);
    }

    public static WorkingDayInformation fullWorkday() {
        return fullWorkday(null);
    }

    public static WorkingDayInformation fullWorkday(LocalDate date) {
        return new WorkingDayInformation(FULL, WORKDAY, WORKDAY);
    }

    public static WorkingDayInformation halfWorkdayMorning() {
        return new WorkingDayInformation(MORNING, WORKDAY, NO_WORKDAY);
    }

    public static WorkingDayInformation halfWorkdayNoon() {
        return new WorkingDayInformation(NOON, NO_WORKDAY, WORKDAY);
    }

    private static WorkingDayInformation fullDayMondayToFriday(LocalDate date) {
        if (isWeekend(date)) {
            return noWorkday();
        } else {
            return fullWorkday();
        }
    }

    private static Map<LocalDate, WorkingDayInformation> buildWorkingTimeByDate(
        LocalDate from,
        LocalDate to,
        Function<LocalDate, WorkingDayInformation> dayLengthProvider
    ) {
        Map<LocalDate, WorkingDayInformation> map = new HashMap<>();
        for (LocalDate date : new DateRange(from, to)) {
            map.put(date, dayLengthProvider.apply(date));
        }
        return map;
    }

    private static boolean isWeekend(LocalDate date) {
        final DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == SATURDAY || dayOfWeek == SUNDAY;
    }
}
