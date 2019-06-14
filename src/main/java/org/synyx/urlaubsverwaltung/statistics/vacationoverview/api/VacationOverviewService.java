package org.synyx.urlaubsverwaltung.statistics.vacationoverview.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.synyx.urlaubsverwaltung.absence.api.DayAbsence;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.api.PersonResponse;
import org.synyx.urlaubsverwaltung.settings.FederalState;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.util.DateUtil;
import org.synyx.urlaubsverwaltung.workingtime.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.time.ZoneOffset.UTC;
import static org.synyx.urlaubsverwaltung.statistics.vacationoverview.api.DayOfMonth.TypeOfDay.WEEKEND;
import static org.synyx.urlaubsverwaltung.statistics.vacationoverview.api.DayOfMonth.TypeOfDay.WORKDAY;

@Component
public class VacationOverviewService {

    private final DepartmentService departmentService;
    private final WorkingTimeService workingTimeService;
    private final PublicHolidaysService publicHolidayService;
    private final ApplicationService applicationService;
    private final SickNoteService sickNoteService;

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    @Autowired
    public VacationOverviewService(DepartmentService departmentService,
                                   WorkingTimeService workingTimeService,
                                   PublicHolidaysService publicHolidayService,
                                   ApplicationService applicationService,
                                   SickNoteService sickNoteService) {

        this.departmentService = departmentService;
        this.workingTimeService = workingTimeService;
        this.publicHolidayService = publicHolidayService;
        this.applicationService = applicationService;
        this.sickNoteService = sickNoteService;
    }

    public List<VacationOverview> getVacationOverviews(String selectedDepartment,
                                                       Optional<Integer> selectedYear,
                                                       Optional<Integer> selectedMonth) {
        List<VacationOverview> holidayOverviewList = new ArrayList<>();

        Department department = getDepartmentByName(selectedDepartment);

        if (department != null) {

            for (Person person : department.getMembers()) {

                LocalDate date = LocalDate.now(UTC);
                int year = selectedYear.orElseGet(() -> date.getYear());
                int month = selectedMonth.orElseGet(() -> date.getMonthValue());
                LocalDate lastDay = DateUtil.getLastDayOfMonth(year, month);

                VacationOverview holidayOverview = getVacationOverview(person);

                for (int i = 1; i <= lastDay.getDayOfMonth(); i++) {

                    DayOfMonth dayOfMonth = new DayOfMonth();
                    LocalDate currentDay = LocalDate.of(year, month, i);
                    dayOfMonth.setDayText(currentDay.format(DateTimeFormatter.ofPattern(DATE_FORMAT)));
                    dayOfMonth.setDayNumber(i);

                    dayOfMonth.setTypeOfDay(getTypeOfDay(person, currentDay));
                    holidayOverview.getDays().add(dayOfMonth);
                }

                holidayOverview.setAbsences(personsVacations(person, year, Optional.of(month), Optional.empty()));

                holidayOverviewList.add(holidayOverview);
            }
        }
        return holidayOverviewList;
    }


    public List<DayAbsence> personsVacations(
            Person person,
            Integer year,
            Optional<Integer> month,
            Optional<DayAbsence.Type> typeFilter) {


        List<DayAbsence> absences = new ArrayList<>();

        LocalDate startDate;
        LocalDate endDate;
        try {
            startDate = getStartDate(year, month);
            endDate = getEndDate(year, month);
        } catch (DateTimeException exception) {
            throw new IllegalArgumentException(exception.getMessage());
        }

        if (!typeFilter.isPresent()|| typeFilter.get() == DayAbsence.Type.VACATION) {
            absences.addAll(getVacations(startDate, endDate, person));
        }

        if (!typeFilter.isPresent() || typeFilter.get() == DayAbsence.Type.SICK_NOTE) {
            absences.addAll(getSickNotes(startDate, endDate, person));
        }

        return absences;
    }


    private static LocalDate getStartDate(Integer year, Optional<Integer> optionalMonth) {

        return optionalMonth.map(s -> DateUtil.getFirstDayOfMonth(year, s))
            .orElseGet(() -> DateUtil.getFirstDayOfYear(year));

    }


    private static LocalDate getEndDate(Integer year, Optional<Integer> optionalMonth) {

        return optionalMonth.map(s -> DateUtil.getLastDayOfMonth(year, s))
            .orElseGet(() -> DateUtil.getLastDayOfYear(year));

    }


    private List<DayAbsence> getVacations(LocalDate start, LocalDate end, Person person) {

        List<DayAbsence> absences = new ArrayList<>();

        List<Application> applications = applicationService.getApplicationsForACertainPeriodAndPerson(start, end,
            person)
            .stream()
            .filter(application ->
                application.hasStatus(ApplicationStatus.WAITING)
                    || application.hasStatus(ApplicationStatus.TEMPORARY_ALLOWED)
                    || application.hasStatus(ApplicationStatus.ALLOWED))
            .collect(Collectors.toList());

        for (Application application : applications) {
            LocalDate startDate = application.getStartDate();
            LocalDate endDate = application.getEndDate();

            LocalDate day = startDate;

            while (!day.isAfter(endDate)) {
                if (!day.isBefore(start) && !day.isAfter(end)) {
                    absences.add(new DayAbsence(day, application.getDayLength().getDuration(), application.getDayLength().toString(), DayAbsence.Type.VACATION,
                        application.getStatus().name(), application.getId()));
                }

                day = day.plusDays(1);
            }
        }

        return absences;
    }


    private List<DayAbsence> getSickNotes(LocalDate start, LocalDate end, Person person) {

        List<DayAbsence> absences = new ArrayList<>();

        List<SickNote> sickNotes = sickNoteService.getByPersonAndPeriod(person, start, end)
            .stream()
            .filter(SickNote::isActive)
            .collect(Collectors.toList());

        for (SickNote sickNote : sickNotes) {
            LocalDate startDate = sickNote.getStartDate();
            LocalDate endDate = sickNote.getEndDate();

            LocalDate day = startDate;

            while (!day.isAfter(endDate)) {
                if (!day.isBefore(start) && !day.isAfter(end)) {
                    absences.add(new DayAbsence(day, sickNote.getDayLength().getDuration(),
                        sickNote.getDayLength().toString(), DayAbsence.Type.SICK_NOTE, "ACTIVE",
                        sickNote.getId()));
                }

                day = day.plusDays(1);
            }
        }

        return absences;
    }



    private DayOfMonth.TypeOfDay getTypeOfDay(Person person, LocalDate currentDay) {
        DayOfMonth.TypeOfDay typeOfDay;

        FederalState state = workingTimeService.getFederalStateForPerson(person, currentDay);
        if (DateUtil.isWorkDay(currentDay)
                && (publicHolidayService.getWorkingDurationOfDate(currentDay, state).longValue() > 0)) {

            typeOfDay = WORKDAY;
        } else {
            typeOfDay = WEEKEND;
        }
        return typeOfDay;
    }

    private Department getDepartmentByName(@RequestParam("selectedDepartment") String selectedDepartment) {
        Department department = null;
        for (Department item : departmentService.getAllDepartments()) {
            if (item.getName().equals(selectedDepartment)) {
                department = item;
                break;
            }
        }
        return department;
    }

    private VacationOverview getVacationOverview(Person person) {
        VacationOverview vacationOverview = new VacationOverview();
        vacationOverview.setDays(new ArrayList<>());
        vacationOverview.setPerson(new PersonResponse(person));
        vacationOverview.setPersonID(person.getId());
        return vacationOverview;
    }

}
