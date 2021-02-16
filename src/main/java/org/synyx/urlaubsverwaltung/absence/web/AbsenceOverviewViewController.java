package org.synyx.urlaubsverwaltung.absence.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTime;
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
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.Integer.parseInt;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.springframework.util.StringUtils.hasText;
import static org.synyx.urlaubsverwaltung.absence.web.AbsenceOverviewDayType.ACTIVE_SICKNOTE_FULL;
import static org.synyx.urlaubsverwaltung.absence.web.AbsenceOverviewDayType.ACTIVE_SICKNOTE_MORNING;
import static org.synyx.urlaubsverwaltung.absence.web.AbsenceOverviewDayType.ACTIVE_SICKNOTE_NOON;
import static org.synyx.urlaubsverwaltung.absence.web.AbsenceOverviewDayType.ALLOWED_VACATION_FULL;
import static org.synyx.urlaubsverwaltung.absence.web.AbsenceOverviewDayType.ALLOWED_VACATION_MORNING;
import static org.synyx.urlaubsverwaltung.absence.web.AbsenceOverviewDayType.ALLOWED_VACATION_NOON;
import static org.synyx.urlaubsverwaltung.absence.web.AbsenceOverviewDayType.PUBLIC_HOLIDAY_FULL;
import static org.synyx.urlaubsverwaltung.absence.web.AbsenceOverviewDayType.PUBLIC_HOLIDAY_MORNING;
import static org.synyx.urlaubsverwaltung.absence.web.AbsenceOverviewDayType.PUBLIC_HOLIDAY_NOON;
import static org.synyx.urlaubsverwaltung.absence.web.AbsenceOverviewDayType.WAITING_VACATION_FULL;
import static org.synyx.urlaubsverwaltung.absence.web.AbsenceOverviewDayType.WAITING_VACATION_MORNING;
import static org.synyx.urlaubsverwaltung.absence.web.AbsenceOverviewDayType.WAITING_VACATION_NOON;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_PRIVILEGED_USER;

@RequestMapping("/web/absences")
@Controller
public class AbsenceOverviewViewController {

    private final PersonService personService;
    private final DepartmentService departmentService;
    private final ApplicationService applicationService;
    private final SickNoteService sickNoteService;
    private final MessageSource messageSource;
    private final Clock clock;
    private final PublicHolidaysService publicHolidayService;
    private final SettingsService settingsService;
    private final WorkingTimeService workingTimeService;

    @Autowired
    public AbsenceOverviewViewController(PersonService personService, DepartmentService departmentService,
                                         ApplicationService applicationService, SickNoteService sickNoteService,
                                         MessageSource messageSource, Clock clock, PublicHolidaysService publicHolidayService,
                                         SettingsService settingsService, WorkingTimeService workingTimeService) {
        this.personService = personService;
        this.departmentService = departmentService;
        this.applicationService = applicationService;
        this.sickNoteService = sickNoteService;
        this.messageSource = messageSource;
        this.clock = clock;
        this.publicHolidayService = publicHolidayService;
        this.settingsService = settingsService;
        this.workingTimeService = workingTimeService;
    }

    @PreAuthorize(IS_PRIVILEGED_USER)
    @GetMapping
    public String absenceOverview(
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) String month,
        @RequestParam(name = "department", required = false, defaultValue = "") List<String> rawSelectedDepartments,
        Model model, Locale locale) {
        final Person signedInUser = personService.getSignedInUser();
        final List<Department> departmentsOfUser = departmentService.getAllowedDepartmentsOfPerson(signedInUser);
        model.addAttribute("departments", departmentsOfUser);

        final String fallbackDepartment = departmentsOfUser.isEmpty() ? "" : departmentsOfUser.get(0).getName();
        final List<String> preparedSelectedDepartments = rawSelectedDepartments.stream().filter(StringUtils::hasText).collect(toList());
        final List<String> selectedDepartmentNames = preparedSelectedDepartments.isEmpty() ?
            List.of(fallbackDepartment) : preparedSelectedDepartments;
        model.addAttribute("selectedDepartments", selectedDepartmentNames);

        final LocalDate startDate = getStartDate(year, month);
        final LocalDate endDate = getEndDate(year, month);

        model.addAttribute("currentYear", Year.now(clock).getValue());
        model.addAttribute("selectedYear", startDate.getYear());

        final String selectedMonth = getSelectedMonth(month, startDate);
        model.addAttribute("selectedMonth", selectedMonth);

        final DateRange dateRange = new DateRange(startDate, endDate);
        final List<Person> overviewPersons = getOverviewPersonsForUser(signedInUser, departmentsOfUser, selectedDepartmentNames);

        final List<AbsenceOverviewMonthDto> months = getAbsenceOverViewMonthModels(year, dateRange, overviewPersons, locale);
        final AbsenceOverviewDto absenceOverview = new AbsenceOverviewDto(months);
        model.addAttribute("absenceOverview", absenceOverview);

        return "absences/absences_overview";
    }

    private List<AbsenceOverviewMonthDto> getAbsenceOverViewMonthModels(Integer year, DateRange dateRange, List<Person> personList, Locale locale) {

        final HashMap<Integer, AbsenceOverviewMonthDto> monthsByNr = new HashMap<>();
        final HashMap<String, List<Application>> applicationsForLeaveByEmail = getApplicationForLeavesByEmail(dateRange, personList);
        final List<SickNote> sickNotes = sickNoteService.getAllActiveByYear(year == null ? Year.now(clock).getValue() : year);
        final List<WorkingTime> workingTimes = workingTimeService.getByPersonsAndDateInterval(personList, dateRange.getStartDate(), dateRange.getEndDate());
        final FederalState defaultFederalState = settingsService.getSettings().getWorkingTimeSettings().getFederalState();

        for (LocalDate date : dateRange) {

            final AbsenceOverviewMonthDto monthView = monthsByNr.computeIfAbsent(date.getMonthValue(),
                monthValue -> this.initializeAbsenceOverviewMonthDto(date, personList, locale));

            final AbsenceOverviewMonthDayDto tableHeadDay = tableHeadDay(date, defaultFederalState);
            monthView.getDays().add(tableHeadDay);

            final Map<String, SickNote> sickNotesOnThisDayByEmail = sickNotesForDate(date, sickNotes,
                sickNote -> sickNote.getPerson().getEmail());

            final Map<AbsenceOverviewMonthPersonDto, Person> personByView = personList.stream().collect(toMap(
                person -> monthView.getPersons().stream()
                    .filter(view ->
                        view.getEmail().equals(person.getEmail()) &&
                            view.getFirstName().equals(person.getFirstName()) &&
                            view.getLastName().equals(person.getLastName())
                    )
                    .findFirst().orElse(null),
                Function.identity()
            ));

            // create an absence day dto for every person of the department
            for (AbsenceOverviewMonthPersonDto personView : monthView.getPersons()) {

                final SickNote sickNote = sickNotesOnThisDayByEmail.get(personView.getEmail());
                final List<Application> applications = applicationsForLeaveByEmail.get(personView.getEmail());
                final Person person = personByView.get(personView);

                final FederalState personDateFederalStateOverride = workingTimes.stream()
                    .filter(workingTime ->
                        workingTime.getPerson().equals(person) && (workingTime.getValidFrom().isBefore(date) || workingTime.getValidFrom().equals(date)))
                    .min(comparing(WorkingTime::getValidFrom))
                    .flatMap(WorkingTime::getFederalStateOverride).orElse(defaultFederalState);

                final AbsenceOverviewDayType personViewDayType = getAbsenceOverviewDayType(date, sickNote, applications, personDateFederalStateOverride);

                personView
                    .getDays()
                    .add(new AbsenceOverviewPersonDayDto(personViewDayType, isWeekend(date)));
            }
        }

        return new ArrayList<>(monthsByNr.values());
    }

    private HashMap<String, List<Application>> getApplicationForLeavesByEmail(DateRange dateRange, List<Person> personList) {

        final LocalDate endDate = dateRange.getEndDate();
        final LocalDate startDate = dateRange.getStartDate();
        final HashMap<String, List<Application>> byEmail = new HashMap<>();

        for (Person person : personList) {
            List<Application> apps = applicationService.getApplicationsForACertainPeriodAndPerson(startDate, endDate, person);
            byEmail.put(person.getEmail(), apps);
        }

        return byEmail;
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

        return new AbsenceOverviewMonthPersonDto(firstName, lastName, email, new ArrayList<>());
    }

    private static Map<String, SickNote> sickNotesForDate(LocalDate date, List<SickNote> sickNotes, Function<SickNote, String> keySupplier) {
        return sickNotes.stream()
            .filter(sickNote -> isDateInPeriod(date, sickNote.getPeriod()))
            .collect(toMap(keySupplier, Function.identity()));
    }

    private AbsenceOverviewDayType getAbsenceOverviewDayType(LocalDate date, SickNote sickNote, List<Application> applications, FederalState personDateFederalStateOverride) {

        final DayLength publicHolidayDayLength = publicHolidayService.getAbsenceTypeOfDate(date, personDateFederalStateOverride);
        if (DayLength.ZERO.compareTo(publicHolidayDayLength) != 0) {
            return getPublicHolidayType(publicHolidayDayLength);
        } else if (sickNote == null) {
            return applications.stream()
                .filter(application -> isDateInPeriod(date, application.getPeriod()))
                .findFirst()
                .map(this::getAbsenceOverviewDayType)
                .orElse(null);
        }

        return getAbsenceOverviewDayType(sickNote);
    }

    private AbsenceOverviewDayType getPublicHolidayType(DayLength dayLength) {
        switch (dayLength) {
            case MORNING:
                return PUBLIC_HOLIDAY_MORNING;
            case NOON:
                return PUBLIC_HOLIDAY_NOON;
            case FULL:
            default:
                return PUBLIC_HOLIDAY_FULL;
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

    private List<Person> getOverviewPersonsForUser(Person signedInUser, List<Department> departments, List<String> selectedDepartmentNames) {

        if (departments.isEmpty() && (signedInUser.hasRole(BOSS) || signedInUser.hasRole(OFFICE))) {
            return personService.getActivePersons();
        }

        return departments.stream()
            .filter(department -> selectedDepartmentNames.contains(department.getName()))
            .map(Department::getMembers)
            .flatMap(List::stream)
            .distinct()
            .sorted(comparing(Person::getFirstName))
            .collect(toList());
    }

    private AbsenceOverviewMonthDayDto tableHeadDay(LocalDate date, FederalState defaultFederalState) {
        final String tableHeadDayText = String.format("%02d", date.getDayOfMonth());
        final DayLength publicHolidayDayLength = publicHolidayService.getAbsenceTypeOfDate(date, defaultFederalState);

        AbsenceOverviewDayType publicHolidayType = null;
        if (DayLength.ZERO.compareTo(publicHolidayDayLength) != 0) {
            publicHolidayType = getPublicHolidayType(publicHolidayDayLength);
        }

        return new AbsenceOverviewMonthDayDto(publicHolidayType, tableHeadDayText, isWeekend(date));
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

    private AbsenceOverviewDayType getAbsenceOverviewDayType(SickNote sickNote) {
        switch (sickNote.getDayLength()) {
            case MORNING:
                return ACTIVE_SICKNOTE_MORNING;
            case NOON:
                return ACTIVE_SICKNOTE_NOON;
            default:
                return ACTIVE_SICKNOTE_FULL;
        }
    }

    private AbsenceOverviewDayType getAbsenceOverviewDayType(Application application) {
        final ApplicationStatus status = application.getStatus();
        if (status == WAITING) {
            switch (application.getDayLength()) {
                case MORNING:
                    return WAITING_VACATION_MORNING;
                case NOON:
                    return WAITING_VACATION_NOON;
                default:
                    return WAITING_VACATION_FULL;
            }
        } else if (status == ALLOWED || status == ALLOWED_CANCELLATION_REQUESTED) {
            switch (application.getDayLength()) {
                case MORNING:
                    return ALLOWED_VACATION_MORNING;
                case NOON:
                    return ALLOWED_VACATION_NOON;
                default:
                    return ALLOWED_VACATION_FULL;
            }
        }
        return null;
    }

    private static boolean isDateInPeriod(LocalDate date, Period period) {
        final LocalDate startDate = period.getStartDate();
        if (startDate.isEqual(date)) {
            return true;
        }

        final LocalDate endDate = period.getEndDate();
        if (endDate.isEqual(date)) {
            return true;
        }

        return startDate.isBefore(date) && date.isBefore(endDate);
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
