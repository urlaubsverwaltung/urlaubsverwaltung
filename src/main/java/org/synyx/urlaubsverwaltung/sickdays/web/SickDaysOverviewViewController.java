package org.synyx.urlaubsverwaltung.sickdays.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.web.DateFormatAware;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;
import org.synyx.urlaubsverwaltung.web.LocalDatePropertyEditor;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;
import static org.synyx.urlaubsverwaltung.sickdays.web.SickDays.SickDayType.TOTAL;
import static org.synyx.urlaubsverwaltung.sickdays.web.SickDays.SickDayType.WITH_AUB;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE_CHILD;

/**
 * Controller for overview about the sick days of all users.
 */
@Controller
@RequestMapping("/web")
public class SickDaysOverviewViewController {

    private final SickNoteService sickNoteService;
    private final PersonService personService;
    private final WorkDaysCountService workDaysCountService;
    private final DateFormatAware dateFormatAware;
    private final Clock clock;

    @Autowired
    public SickDaysOverviewViewController(SickNoteService sickNoteService, PersonService personService,
                                          WorkDaysCountService workDaysCountService, DateFormatAware dateFormatAware, Clock clock) {
        this.sickNoteService = sickNoteService;
        this.personService = personService;
        this.workDaysCountService = workDaysCountService;
        this.dateFormatAware = dateFormatAware;
        this.clock = clock;
    }

    @InitBinder
    public void initBinder(DataBinder binder) {
        binder.registerCustomEditor(LocalDate.class, new LocalDatePropertyEditor());
    }

    @PreAuthorize(IS_OFFICE)
    @PostMapping("/sicknote/filter")
    public String filterSickNotes(@ModelAttribute("period") FilterPeriod period) {

        final String startDateIsoString = dateFormatAware.formatISO(period.getStartDate());
        final String endDateISoString = dateFormatAware.formatISO(period.getEndDate());

        return "redirect:/web/sicknote?from=" + startDateIsoString + "&to=" + endDateISoString;
    }

    @PreAuthorize(IS_OFFICE)
    @GetMapping("/sicknote")
    public String periodsSickNotes(@RequestParam(value = "from", defaultValue = "") String from,
                                   @RequestParam(value = "to", defaultValue = "") String to,
                                   Model model) {

        final LocalDate startDate = dateFormatAware.parse(from).orElse(null);
        final LocalDate endDate = dateFormatAware.parse(to).orElse(null);
        final FilterPeriod period = new FilterPeriod(startDate, endDate);

        final List<SickNote> sickNoteList = sickNoteService.getByPeriod(period.getStartDate(), period.getEndDate());
        fillModel(model, sickNoteList, period);

        return "sicknote/sick_notes";
    }

    private void fillModel(Model model, List<SickNote> sickNotes, FilterPeriod period) {

        model.addAttribute("today", LocalDate.now(clock));
        model.addAttribute("from", period.getStartDate());
        model.addAttribute("to", period.getEndDate());
        model.addAttribute("period", period);

        final List<Person> persons = personService.getActivePersons();

        final List<SickNote> sickNotesOfActivePersons = sickNotes.stream()
            .filter(sickNote -> persons.contains(sickNote.getPerson()) && sickNote.isActive())
            .collect(toList());

        final Map<Person, SickDays> sickDays = new HashMap<>();
        final Map<Person, SickDays> childSickDays = new HashMap<>();

        for (Person person : persons) {
            sickDays.put(person, new SickDays());
            childSickDays.put(person, new SickDays());
        }

        for (SickNote sickNote : sickNotesOfActivePersons) {

            final Person person = sickNote.getPerson();

            if (sickNote.getSickNoteType().isOfCategory(SICK_NOTE_CHILD)) {
                calculateSickDays(period, childSickDays, sickNote, person);
            } else {
                calculateSickDays(period, sickDays, sickNote, person);
            }
        }

        model.addAttribute("sickDays", sickDays);
        model.addAttribute("childSickDays", childSickDays);
        model.addAttribute("persons", persons);
    }

    private void calculateSickDays(FilterPeriod period, Map<Person, SickDays> sickDays, SickNote sickNote, Person person) {

        final DayLength dayLength = sickNote.getDayLength();

        final LocalDate startDate = sickNote.getStartDate().isBefore(period.getStartDate()) ? period.getStartDate() : sickNote.getStartDate();
        final LocalDate endDate = sickNote.getEndDate().isAfter(period.getEndDate()) ? period.getEndDate() : sickNote.getEndDate();
        final BigDecimal workDays = workDaysCountService.getWorkDaysCount(dayLength, startDate, endDate, person);
        sickDays.get(person).addDays(TOTAL, workDays);

        if (sickNote.isAubPresent()) {
            final LocalDate startDateAub = sickNote.getAubStartDate().isBefore(period.getStartDate()) ? period.getStartDate() : sickNote.getAubStartDate();
            final LocalDate endDateAub = sickNote.getAubEndDate().isAfter(period.getEndDate()) ? period.getEndDate() : sickNote.getAubEndDate();

            final BigDecimal workDaysWithAUB = workDaysCountService.getWorkDaysCount(dayLength, startDateAub, endDateAub, person);
            sickDays.get(person).addDays(WITH_AUB, workDaysWithAUB);
        }
    }
}
