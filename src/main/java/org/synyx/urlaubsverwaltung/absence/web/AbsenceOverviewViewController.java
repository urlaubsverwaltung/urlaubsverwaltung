package org.synyx.urlaubsverwaltung.absence.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomCollectionEditor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.synyx.urlaubsverwaltung.absence.AbsencePeriod;
import org.synyx.urlaubsverwaltung.absence.AbsenceService;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHoliday;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.Integer.parseInt;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.springframework.util.StringUtils.hasText;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;

@RequestMapping("/web/absences")
@Controller
public class AbsenceOverviewViewController {

    private final PersonService personService;
    private final DepartmentService departmentService;
    private final MessageSource messageSource;
    private final Clock clock;
    private final PublicHolidaysService publicHolidaysService;
    private final SettingsService settingsService;
    private final AbsenceService absenceService;
    private final WorkingTimeService workingTimeService;

    @Autowired
    public AbsenceOverviewViewController(PersonService personService, DepartmentService departmentService,
                                         MessageSource messageSource, Clock clock,
                                         PublicHolidaysService publicHolidaysService, SettingsService settingsService,
                                         AbsenceService absenceService, WorkingTimeService workingTimeService) {
        this.personService = personService;
        this.departmentService = departmentService;
        this.messageSource = messageSource;
        this.clock = clock;
        this.publicHolidaysService = publicHolidaysService;
        this.settingsService = settingsService;
        this.absenceService = absenceService;
        this.workingTimeService = workingTimeService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(List.class, new CustomCollectionEditor(List.class));
    }

    @GetMapping
    public String absenceOverview(
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) String month,
        @RequestParam(name = "department", required = false, defaultValue = "") List<String> rawSelectedDepartments, Model model, Locale locale) {

        final Person signedInUser = personService.getSignedInUser();

        final List<Person> overviewPersons;
        if (departmentService.getNumberOfDepartments() > 0) {

            final List<Department> visibleDepartments;
            if (signedInUser.hasRole(BOSS) || signedInUser.hasRole(OFFICE)) {
                visibleDepartments = departmentService.getAllDepartments();
            } else {
                visibleDepartments = departmentService.getAllowedDepartmentsOfPerson(signedInUser);
            }
            model.addAttribute("visibleDepartments", visibleDepartments);

            if (visibleDepartments.isEmpty()) {
                overviewPersons = List.of(signedInUser);
            } else {
                final List<String> selectedDepartmentNames = getSelectedDepartmentNames(rawSelectedDepartments, visibleDepartments);
                model.addAttribute("selectedDepartments", selectedDepartmentNames);

                overviewPersons = visibleDepartments.stream()
                    .filter(department -> selectedDepartmentNames.contains(department.getName()))
                    .map(Department::getMembers)
                    .flatMap(List::stream)
                    .filter(member -> !member.hasRole(INACTIVE))
                    .distinct()
                    .sorted(comparing(Person::getFirstName))
                    .collect(toList());
            }
        } else {
            overviewPersons = personService.getActivePersons();
        }

        final LocalDate startDate = getStartDate(year, month);
        final LocalDate endDate = getEndDate(year, month);

        model.addAttribute("currentYear", Year.now(clock).getValue());
        model.addAttribute("selectedYear", startDate.getYear());

        final String selectedMonth = getSelectedMonth(month, startDate);
        model.addAttribute("selectedMonth", selectedMonth);

        final boolean isPrivilegedUser = signedInUser.isPrivileged();
        model.addAttribute("isPrivileged", isPrivilegedUser);

        final DateRange dateRange = new DateRange(startDate, endDate);
        final List<AbsenceOverviewMonthDto> months = getAbsenceOverViewMonthModels(dateRange, overviewPersons, locale, isPrivilegedUser);
        final AbsenceOverviewDto absenceOverview = new AbsenceOverviewDto(months);
        model.addAttribute("absenceOverview", absenceOverview);

        return "absences/absences_overview";
    }

    private List<String> getSelectedDepartmentNames(List<String> rawSelectedDepartments, List<Department> departments) {
        final List<String> preparedSelectedDepartments = rawSelectedDepartments.stream().filter(StringUtils::hasText).collect(toList());
        return preparedSelectedDepartments.isEmpty() ? List.of(departments.get(0).getName()) : preparedSelectedDepartments;
    }

    private List<AbsenceOverviewMonthDto> getAbsenceOverViewMonthModels(DateRange dateRange, List<Person> personList, Locale locale, boolean isPrivilegedUser) {

        final LocalDate today = LocalDate.now(clock);

        final List<AbsencePeriod> openAbsences = absenceService.getOpenAbsences(personList, dateRange.getStartDate(), dateRange.getEndDate());

        final HashMap<Integer, AbsenceOverviewMonthDto> monthsByNr = new HashMap<>();
        final FederalState defaultFederalState = settingsService.getSettings().getWorkingTimeSettings().getFederalState();

        final Map<Person, List<AbsencePeriod.Record>> absencePeriodRecordsByPerson = openAbsences.stream()
            .map(AbsencePeriod::getAbsenceRecords)
            .flatMap(List::stream)
            .collect(groupingBy(AbsencePeriod.Record::getPerson));

        final Map<Person, Map<LocalDate, PublicHoliday>> publicHolidaysOfAllPersons = new HashMap<>();
        for (Person person : personList) {
            publicHolidaysOfAllPersons.put(person, getPublicHolidaysOfPerson(dateRange, person));
        }

        for (LocalDate date : dateRange) {
            final AbsenceOverviewMonthDto monthView = monthsByNr.computeIfAbsent(date.getMonthValue(),
                monthValue -> this.initializeAbsenceOverviewMonthDto(date, personList, locale));

            final AbsenceOverviewMonthDayDto tableHeadDay = tableHeadDay(date, defaultFederalState, today);
            monthView.getDays().add(tableHeadDay);

            final Map<AbsenceOverviewMonthPersonDto, Person> personByView = personList.stream()
                .collect(
                    toMap(person -> monthView.getPersons().stream()
                        .filter(view -> view.getEmail().equals(person.getEmail()) &&
                            view.getFirstName().equals(person.getFirstName()) &&
                            view.getLastName().equals(person.getLastName())
                        )
                        .findFirst()
                        .orElse(null), Function.identity()
                    )
                );

            // create an absence day dto for every person of the department
            for (AbsenceOverviewMonthPersonDto personView : monthView.getPersons()) {

                final Person person = personByView.get(personView);

                final List<AbsencePeriod.Record> personAbsenceRecordsForDate = Optional.ofNullable(absencePeriodRecordsByPerson.get(person))
                    .stream()
                    .flatMap(List::stream)
                    .filter(absenceRecord -> absenceRecord.getDate().isEqual(date))
                    .collect(toUnmodifiableList());

                final AbsenceOverviewDayType personViewDayType = Optional.ofNullable(publicHolidaysOfAllPersons.get(person).get(date))
                    .map(publicHoliday -> getAbsenceOverviewDayType(personAbsenceRecordsForDate, isPrivilegedUser, publicHoliday))
                    .orElseGet(() -> getAbsenceOverviewDayType(personAbsenceRecordsForDate, isPrivilegedUser))
                    .build();

                personView.getDays().add(new AbsenceOverviewPersonDayDto(personViewDayType, isWeekend(date)));
            }
        }

        return new ArrayList<>(monthsByNr.values());
    }

    private Map<LocalDate, PublicHoliday> getPublicHolidaysOfPerson(DateRange dateRange, Person person) {
        return workingTimeService.getFederalStatesByPersonAndDateRange(person, dateRange)
            .entrySet().stream()
            .map(entry -> publicHolidaysService.getPublicHolidays(entry.getKey().getStartDate(), entry.getKey().getEndDate(), entry.getValue()))
            .flatMap(List::stream)
            .collect(toMap(PublicHoliday::getDate, Function.identity()));
    }

    private AbsenceOverviewMonthDto initializeAbsenceOverviewMonthDto(LocalDate date, List<Person> personList, Locale locale) {

        final List<AbsenceOverviewMonthPersonDto> monthViewPersons = personList.stream()
            .map(AbsenceOverviewViewController::initializeAbsenceOverviewMonthPersonDto)
            .collect(toList());

        return new AbsenceOverviewMonthDto(getMonthText(date, locale), new ArrayList<>(), monthViewPersons);
    }

    private static AbsenceOverviewMonthPersonDto initializeAbsenceOverviewMonthPersonDto(Person person) {

        final String firstName = person.getFirstName();
        final String lastName = person.getLastName();
        final String email = person.getEmail();
        final String gravatarUrl = person.getGravatarURL();

        return new AbsenceOverviewMonthPersonDto(firstName, lastName, email, gravatarUrl, new ArrayList<>());
    }

    private AbsenceOverviewDayType.Builder getAbsenceOverviewDayType(List<AbsencePeriod.Record> absenceRecords, boolean isPrivileged, PublicHoliday publicHoliday) {
        AbsenceOverviewDayType.Builder builder = getAbsenceOverviewDayType(absenceRecords, isPrivileged);
        if (publicHoliday.getDayLength().equals(DayLength.MORNING)) {
            builder = builder.publicHolidayMorning();
        }
        if (publicHoliday.getDayLength().equals(DayLength.NOON)) {
            builder = builder.publicHolidayNoon();
        }
        if (publicHoliday.getDayLength().equals(DayLength.FULL)) {
            builder = builder.publicHolidayFull();
        }
        return builder;
    }

    private AbsenceOverviewDayType.Builder getAbsenceOverviewDayType(List<AbsencePeriod.Record> absenceRecords, boolean isPrivileged) {
        if (absenceRecords.isEmpty()) {
            return AbsenceOverviewDayType.builder();
        }

        AbsenceOverviewDayType.Builder builder = AbsenceOverviewDayType.builder();

        for (AbsencePeriod.Record absenceRecord : absenceRecords) {
            if (absenceRecord.isHalfDayAbsence()) {
                builder = getAbsenceOverviewDayTypeForHalfDay(builder, absenceRecord, isPrivileged);
            } else {
                builder = getAbsenceOverviewDayTypeForFullDay(builder, absenceRecord, isPrivileged);
            }
        }

        return builder;
    }

    private AbsenceOverviewDayType.Builder getAbsenceOverviewDayTypeForHalfDay(AbsenceOverviewDayType.Builder builder, AbsencePeriod.Record absenceRecord, boolean isPrivileged) {
        final AbsencePeriod.AbsenceType morningAbsenceType = absenceRecord.getMorning().map(AbsencePeriod.RecordInfo::getType).orElse(null);
        final AbsencePeriod.AbsenceType noonAbsenceType = absenceRecord.getNoon().map(AbsencePeriod.RecordInfo::getType).orElse(null);

        if (AbsencePeriod.AbsenceType.SICK.equals(morningAbsenceType)) {
            return isPrivileged ? builder.sickNoteMorning() : builder.absenceMorning();
        }
        if (AbsencePeriod.AbsenceType.SICK.equals(noonAbsenceType)) {
            return isPrivileged ? builder.sickNoteNoon() : builder.absenceNoon();
        }

        final boolean morningWaiting = absenceRecord.getMorning().map(AbsencePeriod.RecordInfo::hasStatusWaiting).orElse(false);
        if (morningWaiting) {
            return isPrivileged ? builder.waitingVacationMorning() : builder.absenceMorning();
        }
        final boolean morningAllowed = absenceRecord.getMorning().map(AbsencePeriod.RecordInfo::hasStatusAllowed).orElse(false);
        if (morningAllowed) {
            return isPrivileged ? builder.allowedVacationMorning() : builder.absenceMorning();
        }

        final boolean noonWaiting = absenceRecord.getNoon().map(AbsencePeriod.RecordInfo::hasStatusWaiting).orElse(false);
        if (noonWaiting) {
            return isPrivileged ? builder.waitingVacationNoon() : builder.absenceNoon();
        }

        return isPrivileged ? builder.allowedVacationNoon() : builder.absenceNoon();
    }

    private AbsenceOverviewDayType.Builder getAbsenceOverviewDayTypeForFullDay(AbsenceOverviewDayType.Builder builder, AbsencePeriod.Record absenceRecord, boolean isPrivileged) {
        final Optional<AbsencePeriod.RecordInfo> morning = absenceRecord.getMorning();
        final Optional<AbsencePeriod.RecordInfo> noon = absenceRecord.getNoon();
        final Optional<AbsencePeriod.AbsenceType> morningType = morning.map(AbsencePeriod.RecordInfo::getType);
        final Optional<AbsencePeriod.AbsenceType> noonType = noon.map(AbsencePeriod.RecordInfo::getType);

        final boolean sickMorning = morningType.map(AbsencePeriod.AbsenceType.SICK::equals).orElse(false);
        final boolean sickNoon = noonType.map(AbsencePeriod.AbsenceType.SICK::equals).orElse(false);
        final boolean sickFull = sickMorning && sickNoon;

        if (sickFull) {
            return isPrivileged ? builder.sickNoteFull() : builder.absenceFull();
        }

        final boolean morningWaiting = morning.map(AbsencePeriod.RecordInfo::hasStatusWaiting).orElse(false);
        final boolean noonWaiting = noon.map(AbsencePeriod.RecordInfo::hasStatusWaiting).orElse(false);

        if (morningWaiting && noonWaiting) {
            return isPrivileged ? builder.waitingVacationFull() : builder.absenceFull();
        } else if (!morningWaiting && !noonWaiting) {
            return isPrivileged ? builder.allowedVacationFull() : builder.absenceFull();
        }

        return builder;
    }

    private AbsenceOverviewDayType.Builder getPublicHolidayType(DayLength dayLength) {
        final AbsenceOverviewDayType.Builder builder = AbsenceOverviewDayType.builder();
        switch (dayLength) {
            case MORNING:
                return builder.publicHolidayMorning();
            case NOON:
                return builder.publicHolidayNoon();
            case FULL:
            default:
                return builder.publicHolidayFull();
        }
    }

    private String getSelectedMonth(String month, LocalDate startDate) {
        String selectedMonth = "";
        if (month == null) {
            selectedMonth = String.valueOf(startDate.getMonthValue());
        } else if (hasText(month)) {
            selectedMonth = month;
        }
        return selectedMonth;
    }

    private AbsenceOverviewMonthDayDto tableHeadDay(LocalDate date, FederalState defaultFederalState, LocalDate today) {
        final Optional<PublicHoliday> maybePublicHoliday = publicHolidaysService.getPublicHoliday(date, defaultFederalState);
        final DayLength publicHolidayDayLength = maybePublicHoliday.isPresent() ? maybePublicHoliday.get().getDayLength() : DayLength.ZERO;

        AbsenceOverviewDayType publicHolidayType = null;
        if (DayLength.ZERO.compareTo(publicHolidayDayLength) != 0) {
            publicHolidayType = getPublicHolidayType(publicHolidayDayLength).build();
        }

        final String tableHeadDayText = String.format("%02d", date.getDayOfMonth());
        final boolean isToday = date.isEqual(today);

        return new AbsenceOverviewMonthDayDto(publicHolidayType, tableHeadDayText, isWeekend(date), isToday);
    }

    private String getMonthText(LocalDate date, Locale locale) {
        return messageSource.getMessage(getMonthMessageCode(date), new Object[]{}, locale);
    }

    private String getMonthMessageCode(LocalDate localDate) {
        switch (localDate.getMonthValue()) {
            case 1:
                return "month.january";
            case 2:
                return "month.february";
            case 3:
                return "month.march";
            case 4:
                return "month.april";
            case 5:
                return "month.may";
            case 6:
                return "month.june";
            case 7:
                return "month.july";
            case 8:
                return "month.august";
            case 9:
                return "month.september";
            case 10:
                return "month.october";
            case 11:
                return "month.november";
            case 12:
                return "month.december";
            default:
                throw new IllegalStateException("month value not in range of 1 to 12 cannot be mapped to a message key.");
        }
    }

    private LocalDate getStartDate(Integer year, String month) {
        return getStartOrEndDate(year, month, TemporalAdjusters::firstDayOfYear, TemporalAdjusters::firstDayOfMonth);
    }

    private LocalDate getEndDate(Integer year, String month) {
        return getStartOrEndDate(year, month, TemporalAdjusters::lastDayOfYear, TemporalAdjusters::lastDayOfMonth);
    }

    private LocalDate getStartOrEndDate(Integer year, String month, Supplier<TemporalAdjuster> firstOrLastOfYearSupplier,
                                        Supplier<TemporalAdjuster> firstOrLastOfMonthSupplier) {

        final LocalDate now = LocalDate.now(clock);

        if (year != null) {
            if (hasText(month)) {
                return now.withYear(year).withMonth(parseInt(month)).with(firstOrLastOfMonthSupplier.get());
            }
            if ("".equals(month)) {
                return now.withYear(year).with(firstOrLastOfYearSupplier.get());
            }
            return now.withYear(year).with(firstOrLastOfMonthSupplier.get());
        }

        if (hasText(month)) {
            return now.withMonth(parseInt(month)).with(firstOrLastOfMonthSupplier.get());
        }
        return now.with(firstOrLastOfMonthSupplier.get());
    }

    private static boolean isWeekend(LocalDate date) {
        final DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == SATURDAY || dayOfWeek == SUNDAY;
    }
}
