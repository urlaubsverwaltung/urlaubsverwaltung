package org.synyx.urlaubsverwaltung.absence.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;

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
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_PRIVILEGED_USER;

/**
 * Controller to generate applications for leave vacation overview.
 */
@RequestMapping("/web/application")
@Controller
public class ApplicationForLeaveVacationOverviewViewController {

    private final PersonService personService;
    private final DepartmentService departmentService;
    private final ApplicationService applicationService;
    private final SickNoteService sickNoteService;
    private final MessageSource messageSource;
    private final Clock clock;

    @Autowired
    public ApplicationForLeaveVacationOverviewViewController(PersonService personService,
                                                             DepartmentService departmentService,
                                                             ApplicationService applicationService,
                                                             SickNoteService sickNoteService,
                                                             MessageSource messageSource, Clock clock) {
        this.personService = personService;
        this.departmentService = departmentService;
        this.applicationService = applicationService;
        this.sickNoteService = sickNoteService;
        this.messageSource = messageSource;
        this.clock = clock;
    }

    @PreAuthorize(IS_PRIVILEGED_USER)
    @GetMapping("/vacationoverview")
    public String applicationForLeaveVacationOverview(
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) String month,
        @RequestParam(required = false) String department,
        Model model, Locale locale)
    {
        final Person signedInUser = personService.getSignedInUser();
        final List<Department> departments = departmentService.getAllowedDepartmentsOfPerson(signedInUser);
        model.addAttribute("departments", departments);

        final String fallbackDepartment = departments.isEmpty() ? "" : departments.get(0).getName();
        final String selectedDepartmentName = hasText(department) ? department : fallbackDepartment;
        model.addAttribute("selectedDepartment", selectedDepartmentName);

        final LocalDate startDate = getStartDate(year, month);
        final LocalDate endDate = getEndDate(year, month);

        model.addAttribute("currentYear", Year.now(clock).getValue());
        model.addAttribute("selectedYear", startDate.getYear());

        final String selectedMonth = getSelectedMonth(month, startDate);
        model.addAttribute("selectedMonth", selectedMonth);

        final List<Person> overviewPersons = getOverviewPersonsForUser(signedInUser, departments, selectedDepartmentName);
        final List<SickNote> sickNotes = sickNoteService.getAllActiveByYear(year == null ? Year.now(clock).getValue() : year);

        final HashMap<String, List<Application>> vacationsByEmail = new HashMap<>();
        for (Person person : overviewPersons) {
            List<Application> apps = applicationService.getApplicationsForACertainPeriodAndPerson(startDate, endDate, person);
            vacationsByEmail.put(person.getEmail(), apps);
        }

        HashMap<Integer, AbsenceOverviewMonthDto> monthsByNr = new HashMap<>();

        // `date` is increased by one month at the end of this outer loop
        // the outer loop builds the absence overview view model for every user desired month (specific one or all 12 🐵)
        LocalDate date = startDate;
        while (date.isBefore(endDate) || date.isEqual(endDate)) {

            // `monthDate` is increased by one day at the end of this inner loop
            // this inner loop builds the monthly absence items for every person of the given department
            LocalDate monthDate = date;
            while (monthDate.getMonthValue() == date.getMonthValue()) {

                // since `monthDate` is increased by one day at the end of the loop we have to check
                // if we have to create the month view dto in the current loop iteration.
                if (!monthsByNr.containsKey(date.getMonthValue())) {
                    ArrayList<AbsenceOverviewMonthPersonDto> monthViewPersons = new ArrayList<>(overviewPersons.size());
                    for (Person person : overviewPersons) {
                        AbsenceOverviewMonthPersonDto p = new AbsenceOverviewMonthPersonDto(
                            person.getFirstName(), person.getLastName(), person.getEmail(), new ArrayList<>());

                        monthViewPersons.add(p);
                    }

                    AbsenceOverviewMonthDto monthView = new AbsenceOverviewMonthDto(
                        getMonthText(monthDate, locale), new ArrayList<>(), monthViewPersons);

                    monthsByNr.put(date.getMonthValue(), monthView);
                }

                final AbsenceOverviewMonthDto monthView = monthsByNr.get(date.getMonthValue());
                final AbsenceOverviewMonthDayDto tableHeadDay = tableHeadDay(monthDate);
                monthView.getDays().add(tableHeadDay);

                final LocalDate thisDate = monthDate;
                final Map<String, SickNote> sickNotesOnThisDayByEmail = sickNotes.stream()
                    .filter(sickNote -> isDateInPeriod(thisDate, sickNote.getPeriod()))
                    .collect(toMap(sickNote -> sickNote.getPerson().getEmail(), Function.identity()));

                // create an absence day dto for every person of the department
                for (AbsenceOverviewMonthPersonDto personView : monthView.getPersons()) {
                    AbsenceOverviewDayType personViewDayType;

                    SickNote sickNote = sickNotesOnThisDayByEmail.get(personView.getEmail());
                    if (sickNote != null) {
                        personViewDayType = getAbsenceOverviewDayType(sickNote);
                    } else {
                        personViewDayType = vacationsByEmail.get(personView.getEmail()).stream()
                            .filter(application -> isDateInPeriod(thisDate, application.getPeriod()))
                            .findFirst()
                            .map(this::getAbsenceOverviewDayType).orElse(null);
                    }

                    final AbsenceOverviewPersonDayDto personDay = new AbsenceOverviewPersonDayDto(personViewDayType, isWeekend(monthDate));
                    personView.getDays().add(personDay);
                }

                monthDate = monthDate.plusDays(1);
            }

            date = monthDate;
        }

        AbsenceOverviewDto absenceOverview = new AbsenceOverviewDto(new ArrayList<>(monthsByNr.values()));
        model.addAttribute("absenceOverview", absenceOverview);

        return "application/vacation_overview";
    }

    private String getSelectedMonth(String month, LocalDate startDate) {
        if (month == null) {
            return String.valueOf(startDate.getMonthValue());
        } else if (hasText(month)) {
            return month;
        }
        return "";
    }

    private List<Person> getOverviewPersonsForUser(Person signedInUser, List<Department> departments, String selectedDepartmentName) {

        if (departments.isEmpty() && (signedInUser.hasRole(BOSS) || signedInUser.hasRole(OFFICE))) {
            return personService.getActivePersons();
        }

        return departments.stream()
            .filter(department -> department.getName().equals(selectedDepartmentName))
            .map(Department::getMembers)
            .flatMap(List::stream)
            .sorted(comparing(Person::getFirstName))
            .collect(toList());
    }

    private AbsenceOverviewMonthDayDto tableHeadDay(LocalDate localDate) {
        String tableHeadDayText = String.format("%02d", localDate.getDayOfMonth());
        boolean weekend = isWeekend(localDate);

        return new AbsenceOverviewMonthDayDto(tableHeadDayText, weekend);
    }

    private String getMonthText(LocalDate date, Locale locale) {
        return messageSource.getMessage("month." + date.getMonthValue(), new Object[]{}, locale);
    }


    private AbsenceOverviewDayType getAbsenceOverviewDayType(SickNote sickNote) {
        switch (sickNote.getDayLength()) {
            case MORNING:
                return AbsenceOverviewDayType.ACTIVE_SICKNOTE_MORNING;
            case NOON:
                return AbsenceOverviewDayType.ACTIVE_SICKNOTE_NOON;
            default:
                return AbsenceOverviewDayType.ACTIVE_SICKNOTE_FULL;
        }
    }

    private AbsenceOverviewDayType getAbsenceOverviewDayType(Application application) {
        ApplicationStatus status = application.getStatus();
        if (status == ApplicationStatus.WAITING) {
            switch (application.getDayLength()) {
                case MORNING:
                    return AbsenceOverviewDayType.WAITING_VACATION_MORNING;
                case NOON:
                    return AbsenceOverviewDayType.WAITING_VACATION_NOON;
                default:
                    return AbsenceOverviewDayType.WAITING_VACATION_FULL;
            }
        } else if (status == ApplicationStatus.ALLOWED) {
            switch (application.getDayLength()) {
                case MORNING:
                    return AbsenceOverviewDayType.ALLOWED_VACATION_MORNING;
                case NOON:
                    return AbsenceOverviewDayType.ALLOWED_VACATION_NOON;
                default:
                    return AbsenceOverviewDayType.ALLOWED_VACATION_FULL;
            }
        }
        return null;
    }

    private static boolean isDateInPeriod(LocalDate date, Period period) {
        LocalDate startDate = period.getStartDate();
        if (startDate.isEqual(date)) {
            return true;
        }

        LocalDate endDate = period.getEndDate();
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

    private LocalDate getStartOrEndDate(Integer year, String month, Supplier<TemporalAdjuster> firstOrLastOfYearSupplier, Supplier<TemporalAdjuster> firstOrLastOfMonthSupplier) {
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
