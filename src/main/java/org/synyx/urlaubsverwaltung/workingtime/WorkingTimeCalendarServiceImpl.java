package org.synyx.urlaubsverwaltung.workingtime;

import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHoliday;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation.WorkingTimeCalendarEntryType;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Year;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

@Service
class WorkingTimeCalendarServiceImpl implements WorkingTimeCalendarService {

    private final WorkingTimeRepository workingTimeRepository;
    private final PublicHolidaysService publicHolidaysService;
    private final SettingsService settingsService;

    WorkingTimeCalendarServiceImpl(WorkingTimeRepository workingTimeRepository, PublicHolidaysService publicHolidaysService, SettingsService settingsService) {
        this.workingTimeRepository = workingTimeRepository;
        this.publicHolidaysService = publicHolidaysService;
        this.settingsService = settingsService;
    }

    @Override
    public Map<Person, WorkingTimeCalendar> getWorkingTimesByPersons(Collection<Person> persons, Year year) {
        return getWorkingTimesByPersons(persons, new DateRange(year.atDay(1), year.atDay(1).with(lastDayOfYear())));
    }

    @Override
    public Map<Person, WorkingTimeCalendar> getWorkingTimesByPersons(Collection<Person> persons, DateRange dateRange) {
        final CachedSupplier<FederalState> federalStateCachedSupplier = new CachedSupplier<>(this::getSystemDefaultFederalState);

        final WorkingTimeSettings workingTimeSettings = settingsService.getSettings().getWorkingTimeSettings();

        final Map<Person, List<WorkingTime>> workingTimesByPerson = workingTimeRepository.findByPersonIsInOrderByValidFromDesc(persons)
            .stream()
            .map(entity -> toWorkingTime(entity, federalStateCachedSupplier))
            .collect(groupingBy(WorkingTime::getPerson));

        final LocalDate start = dateRange.getStartDate();
        final LocalDate end = dateRange.getEndDate();

        return persons.stream().map(person -> {
            final List<WorkingTime> workingTimesInDateRange = workingTimesByPerson.getOrDefault(person, List.of())
                .stream()
                .filter(workingTime -> !workingTime.getValidFrom().isAfter(end))
                .toList();

            final Map<LocalDate, WorkingDayInformation> dayLengthByDate = new HashMap<>();

            LocalDate nextEnd = end;

            for (WorkingTime workingTime : workingTimesInDateRange) {

                final DateRange workingTimeDateRange;
                if (workingTime.getValidFrom().isBefore(start)) {
                    workingTimeDateRange = new DateRange(start, nextEnd);
                } else {
                    workingTimeDateRange = new DateRange(workingTime.getValidFrom(), nextEnd);
                }

                for (LocalDate date : workingTimeDateRange) {
                    dayLengthByDate.put(date, getWorkDayLengthForWeekDay(date, workingTime, workingTimeSettings));
                }

                if (workingTimeDateRange.getStartDate().equals(start)) {
                    break;
                }

                nextEnd = workingTime.getValidFrom().minusDays(1);
            }

            return Map.entry(person, new WorkingTimeCalendar(dayLengthByDate));
        }).collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private WorkingDayInformation getWorkDayLengthForWeekDay(LocalDate date, WorkingTime workingTime, WorkingTimeSettings workingTimeSettings) {
        final FederalState federalState = workingTime.getFederalState();

        final DayLength configuredWorkingTimeForDayOfWeek = workingTime.getDayLengthForWeekDay(date.getDayOfWeek());

        DayLength morning = configuredWorkingTimeForDayOfWeek.isFull() || configuredWorkingTimeForDayOfWeek.isMorning() ? DayLength.MORNING : DayLength.ZERO;
        WorkingTimeCalendarEntryType morningType = morning.isMorning() ? WorkingTimeCalendarEntryType.WORKDAY : WorkingTimeCalendarEntryType.NO_WORKDAY;

        DayLength noon = configuredWorkingTimeForDayOfWeek.isFull() || configuredWorkingTimeForDayOfWeek.isNoon() ? DayLength.NOON : DayLength.ZERO;
        WorkingTimeCalendarEntryType noonType = noon.isNoon() ? WorkingTimeCalendarEntryType.WORKDAY : WorkingTimeCalendarEntryType.NO_WORKDAY;

        if (configuredWorkingTimeForDayOfWeek.getDuration().signum() > 0) {
            final Optional<PublicHoliday> maybePublicHoliday = publicHolidaysService.getPublicHoliday(date, federalState, workingTimeSettings);

            if (maybePublicHoliday.isPresent()) {

                final PublicHoliday publicHoliday = maybePublicHoliday.get();

                if (configuredWorkingTimeForDayOfWeek.isFull()) {
                    if (publicHoliday.dayLength().isFull()) {
                        morning = DayLength.ZERO;
                        morningType = WorkingTimeCalendarEntryType.PUBLIC_HOLIDAY;
                        noon = DayLength.ZERO;
                        noonType = WorkingTimeCalendarEntryType.PUBLIC_HOLIDAY;
                    } else if (publicHoliday.dayLength().isMorning()) {
                        morning = DayLength.ZERO;
                        morningType = WorkingTimeCalendarEntryType.PUBLIC_HOLIDAY;
                        noon = DayLength.NOON;
                        noonType = WorkingTimeCalendarEntryType.WORKDAY;
                    } else {
                        morning = DayLength.MORNING;
                        morningType = WorkingTimeCalendarEntryType.WORKDAY;
                        noon = DayLength.ZERO;
                        noonType = WorkingTimeCalendarEntryType.PUBLIC_HOLIDAY;
                    }
                } else {
                    if (configuredWorkingTimeForDayOfWeek.isMorning()) {

                        noon = DayLength.ZERO;
                        noonType = WorkingTimeCalendarEntryType.NO_WORKDAY;

                        if (publicHoliday.isFull() || publicHoliday.isMorning()) {
                            morning = DayLength.ZERO;
                            morningType = WorkingTimeCalendarEntryType.PUBLIC_HOLIDAY;
                        } else {
                            morning = DayLength.MORNING;
                            morningType = WorkingTimeCalendarEntryType.WORKDAY;
                        }
                    } else {

                        morning = DayLength.ZERO;
                        morningType = WorkingTimeCalendarEntryType.NO_WORKDAY;

                        if (publicHoliday.isFull() || publicHoliday.isNoon()) {
                            noon = DayLength.ZERO;
                            noonType = WorkingTimeCalendarEntryType.PUBLIC_HOLIDAY;
                        } else {
                            noon = DayLength.NOON;
                            noonType = WorkingTimeCalendarEntryType.WORKDAY;
                        }
                    }
                }
            }
        }

        DayLength calculatedDayLength = configuredWorkingTimeForDayOfWeek;
        if (morning.isMorning() && noon.isNoon()) {
            calculatedDayLength = DayLength.FULL;
        } else if(morning == DayLength.ZERO && noon == DayLength.ZERO){
            calculatedDayLength = DayLength.ZERO;
        } else if(morning == DayLength.ZERO && noon.isNoon()) {
            calculatedDayLength = DayLength.NOON;
        } else if(morning.isMorning() && noon == DayLength.ZERO){
            calculatedDayLength = DayLength.MORNING;
        }

        return new WorkingDayInformation(calculatedDayLength, morningType, noonType);
    }

    private static WorkingTime toWorkingTime(WorkingTimeEntity workingTimeEntity, Supplier<FederalState> defaultFederalStateProvider) {

        final boolean isDefaultFederalState = workingTimeEntity.getFederalStateOverride() == null;
        final FederalState federalState = isDefaultFederalState ? defaultFederalStateProvider.get() : workingTimeEntity.getFederalStateOverride();

        final WorkingTime workingTime = new WorkingTime(workingTimeEntity.getPerson(), workingTimeEntity.getValidFrom(), federalState, isDefaultFederalState);

        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            final DayLength dayLength = dayLengthForDayOfWeek(workingTimeEntity, dayOfWeek);
            workingTime.setDayLengthForWeekDay(dayOfWeek, dayLength);
        }

        return workingTime;
    }

    private static DayLength dayLengthForDayOfWeek(WorkingTimeEntity workingTimeEntity, DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> workingTimeEntity.getMonday();
            case TUESDAY -> workingTimeEntity.getTuesday();
            case WEDNESDAY -> workingTimeEntity.getWednesday();
            case THURSDAY -> workingTimeEntity.getThursday();
            case FRIDAY -> workingTimeEntity.getFriday();
            case SATURDAY -> workingTimeEntity.getSaturday();
            case SUNDAY -> workingTimeEntity.getSunday();
        };
    }

    private FederalState getSystemDefaultFederalState() {
        return settingsService.getSettings().getWorkingTimeSettings().getFederalState();
    }
}
