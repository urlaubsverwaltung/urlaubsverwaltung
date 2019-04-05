package org.synyx.urlaubsverwaltung.sicknote.web;

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
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.security.SecurityRules;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteCategory;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.statistics.web.SickDays;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;
import org.synyx.urlaubsverwaltung.web.LocalDatePropertyEditor;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.time.ZoneOffset.UTC;


/**
 * Controller for overview about the sick days of all users.
 */
@Controller
@RequestMapping("/web")
public class SickDaysOverviewController {

    private static final String PERSONS_ATTRIBUTE = "persons";

    private final SickNoteService sickNoteService;
    private final PersonService personService;
    private final WorkDaysService calendarService;

    @Autowired
    public SickDaysOverviewController(SickNoteService sickNoteService, PersonService personService, WorkDaysService calendarService) {
        this.sickNoteService = sickNoteService;
        this.personService = personService;
        this.calendarService = calendarService;
    }

    @InitBinder
    public void initBinder(DataBinder binder) {

        binder.registerCustomEditor(LocalDate.class, new LocalDatePropertyEditor());
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @PostMapping("/sicknote/filter")
    public String filterSickNotes(@ModelAttribute("period") FilterPeriod period) {

        return "redirect:/web/sicknote?from=" + period.getStartDateAsString() + "&to=" + period.getEndDateAsString();
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @GetMapping("/sicknote")
    public String periodsSickNotes(@RequestParam(value = "from", required = false) String from,
        @RequestParam(value = "to", required = false) String to, Model model) {

        FilterPeriod period = new FilterPeriod(Optional.ofNullable(from), Optional.ofNullable(to));

        List<SickNote> sickNoteList = sickNoteService.getByPeriod(period.getStartDate(), period.getEndDate());

        fillModel(model, sickNoteList, period);

        return "sicknote/sick_notes";
    }


    private void fillModel(Model model, List<SickNote> sickNotes, FilterPeriod period) {

        model.addAttribute("today", LocalDate.now(UTC));
        model.addAttribute("from", period.getStartDate());
        model.addAttribute("to", period.getEndDate());
        model.addAttribute("period", period);

        List<Person> persons = personService.getActivePersons();

        List<SickNote> sickNotesOfActivePersons = sickNotes.stream().filter(sickNote ->
                    persons.contains(sickNote.getPerson()) && sickNote.isActive()).collect(Collectors.toList());

        Map<Person, SickDays> sickDays = new HashMap<>();
        Map<Person, SickDays> childSickDays = new HashMap<>();

        for (Person person : persons) {
            sickDays.put(person, new SickDays());
            childSickDays.put(person, new SickDays());
        }

        for (SickNote sickNote : sickNotesOfActivePersons) {
            Person person = sickNote.getPerson();
            BigDecimal workDays = calendarService.getWorkDays(sickNote.getDayLength(), sickNote.getStartDate(),
                    sickNote.getEndDate(), person);

            if (sickNote.getSickNoteType().isOfCategory(SickNoteCategory.SICK_NOTE_CHILD)) {
                childSickDays.get(person).addDays(SickDays.SickDayType.TOTAL, workDays);

                if (sickNote.isAubPresent()) {
                    BigDecimal workDaysWithAUB = calendarService.getWorkDays(sickNote.getDayLength(),
                            sickNote.getAubStartDate(), sickNote.getAubEndDate(), person);

                    childSickDays.get(person).addDays(SickDays.SickDayType.WITH_AUB, workDaysWithAUB);
                }
            } else {
                sickDays.get(person).addDays(SickDays.SickDayType.TOTAL, workDays);

                if (sickNote.isAubPresent()) {
                    BigDecimal workDaysWithAUB = calendarService.getWorkDays(sickNote.getDayLength(),
                            sickNote.getAubStartDate(), sickNote.getAubEndDate(), person);

                    sickDays.get(person).addDays(SickDays.SickDayType.WITH_AUB, workDaysWithAUB);
                }
            }
        }

        model.addAttribute("sickDays", sickDays);
        model.addAttribute("childSickDays", childSickDays);
        model.addAttribute(PERSONS_ATTRIBUTE, persons);
    }
}
