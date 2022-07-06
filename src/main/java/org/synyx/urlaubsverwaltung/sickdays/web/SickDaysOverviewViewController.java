package org.synyx.urlaubsverwaltung.sickdays.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.web.DateFormatAware;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;
import static org.springframework.util.StringUtils.hasText;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;
import static org.synyx.urlaubsverwaltung.sickdays.web.SickDays.SickDayType.TOTAL;
import static org.synyx.urlaubsverwaltung.sickdays.web.SickDays.SickDayType.WITH_AUB;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE_CHILD;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.ACTIVE;

/**
 * Controller for overview about the sick days of all users.
 */
@Controller
@RequestMapping("/web")
public class SickDaysOverviewViewController {

    private final SickNoteService sickNoteService;
    private final PersonBasedataService personBasedataService;
    private final WorkDaysCountService workDaysCountService;
    private final DepartmentService departmentService;
    private final PersonService personService;
    private final DateFormatAware dateFormatAware;
    private final Clock clock;

    @Autowired
    public SickDaysOverviewViewController(SickNoteService sickNoteService, PersonBasedataService personBasedataService,
                                          WorkDaysCountService workDaysCountService, DepartmentService departmentService,
                                          PersonService personService, DateFormatAware dateFormatAware, Clock clock) {
        this.sickNoteService = sickNoteService;
        this.personBasedataService = personBasedataService;
        this.workDaysCountService = workDaysCountService;
        this.departmentService = departmentService;
        this.personService = personService;
        this.dateFormatAware = dateFormatAware;
        this.clock = clock;
    }

    @PreAuthorize("hasAnyAuthority('OFFICE', 'DEPARTMENT_HEAD')")
    @PostMapping("/sicknote/filter")
    public String filterSickNotes(@ModelAttribute("period") FilterPeriod period, Errors errors, RedirectAttributes redirectAttributes) {

        if (errors.hasErrors()) {
            redirectAttributes.addFlashAttribute("filterPeriodIncorrect", true);
        }

        final String startDateIsoString = dateFormatAware.formatISO(period.getStartDate());
        final String endDateISoString = dateFormatAware.formatISO(period.getEndDate());

        return "redirect:/web/sicknote?from=" + startDateIsoString + "&to=" + endDateISoString;
    }

    @PreAuthorize("hasAnyAuthority('OFFICE', 'DEPARTMENT_HEAD')")
    @GetMapping("/sicknote")
    public String periodsSickNotes(@RequestParam(value = "from", defaultValue = "") String from,
                                   @RequestParam(value = "to", defaultValue = "") String to,
                                   Model model) {

        final LocalDate startDate = dateFormatAware.parse(from).orElse(null);
        final LocalDate endDate = dateFormatAware.parse(to).orElse(null);
        final FilterPeriod period = new FilterPeriod(startDate, endDate);

        final Person signedInUser = personService.getSignedInUser();

        final List<SickNote> sickNotes;
        final List<Person> persons;
        if (signedInUser.hasRole(DEPARTMENT_HEAD)) {
            persons = departmentService.getMembersForDepartmentHead(signedInUser);
            sickNotes = sickNoteService.getForStatesAndPersonAndPersonHasRoles(List.of(ACTIVE), persons, List.of(USER), period.getStartDate(), period.getEndDate());
        } else if (signedInUser.hasRole(OFFICE)) {
            persons = personService.getActivePersons();
            sickNotes = sickNoteService.getForStatesAndPersonAndPersonHasRoles(List.of(ACTIVE), persons, List.of(USER), period.getStartDate(), period.getEndDate());
        } else {
            persons = List.of();
            sickNotes = List.of();
        }

        fillModel(model, sickNotes, period, persons);

        return "sicknote/sick_notes";
    }

    private void fillModel(Model model, List<SickNote> sickNotes, FilterPeriod period, List<Person> persons) {

        model.addAttribute("today", LocalDate.now(clock));
        model.addAttribute("from", period.getStartDate());
        model.addAttribute("to", period.getEndDate());
        model.addAttribute("period", period);

        final Map<Person, SickDays> sickDays = new HashMap<>();
        final Map<Person, SickDays> childSickDays = new HashMap<>();
        sickNotes.forEach(sickNote -> {
            if (sickNote.getSickNoteType().isOfCategory(SICK_NOTE_CHILD)) {
                calculateSickDays(period, childSickDays, sickNote);
            } else {
                calculateSickDays(period, sickDays, sickNote);
            }
        });
        model.addAttribute("sickDays", sickDays);
        model.addAttribute("childSickDays", childSickDays);

        persons.forEach(person -> {
            sickDays.putIfAbsent(person, new SickDays());
            childSickDays.putIfAbsent(person, new SickDays());
        });
        model.addAttribute("persons", persons);

        final Map<Integer, String> personnelNumberOfPersons = getPersonnelNumbersOfPersons(persons);
        model.addAttribute("showPersonnelNumberColumn", !personnelNumberOfPersons.isEmpty());
        model.addAttribute("personnelNumberOfPersons", personnelNumberOfPersons);
    }

    private Map<Integer, String> getPersonnelNumbersOfPersons(List<Person> persons) {
        return persons.stream()
            .map(person -> personBasedataService.getBasedataByPersonId(person.getId()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .filter(personBasedata -> hasText(personBasedata.getPersonnelNumber()))
            .collect(toMap(PersonBasedata::getPersonId, PersonBasedata::getPersonnelNumber));
    }

    private void calculateSickDays(FilterPeriod period, Map<Person, SickDays> sickDays, SickNote sickNote) {

        final DayLength dayLength = sickNote.getDayLength();
        final Person person = sickNote.getPerson();

        final LocalDate startDate = sickNote.getStartDate().isBefore(period.getStartDate()) ? period.getStartDate() : sickNote.getStartDate();
        final LocalDate endDate = sickNote.getEndDate().isAfter(period.getEndDate()) ? period.getEndDate() : sickNote.getEndDate();
        final BigDecimal workDays = workDaysCountService.getWorkDaysCount(dayLength, startDate, endDate, person);
        sickDays.computeIfAbsent(person, unused -> new SickDays()).addDays(TOTAL, workDays);

        if (sickNote.isAubPresent()) {
            final LocalDate startDateAub = sickNote.getAubStartDate().isBefore(period.getStartDate()) ? period.getStartDate() : sickNote.getAubStartDate();
            final LocalDate endDateAub = sickNote.getAubEndDate().isAfter(period.getEndDate()) ? period.getEndDate() : sickNote.getAubEndDate();

            final BigDecimal workDaysWithAUB = workDaysCountService.getWorkDaysCount(dayLength, startDateAub, endDateAub, person);
            sickDays.computeIfAbsent(person, unused -> new SickDays()).addDays(WITH_AUB, workDaysWithAUB);
        }
    }
}
