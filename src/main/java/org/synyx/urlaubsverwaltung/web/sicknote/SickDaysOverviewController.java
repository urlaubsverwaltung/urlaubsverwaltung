package org.synyx.urlaubsverwaltung.web.sicknote;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.validation.DataBinder;

import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import org.synyx.urlaubsverwaltung.core.calendar.WorkDaysService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteCategory;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.security.SecurityRules;
import org.synyx.urlaubsverwaltung.web.DateMidnightPropertyEditor;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;
import org.synyx.urlaubsverwaltung.web.person.PersonConstants;
import org.synyx.urlaubsverwaltung.web.statistics.SickDays;

import java.math.BigDecimal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Controller for overview about the sick days of all users.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Controller
@RequestMapping("/web")
public class SickDaysOverviewController {

    @Autowired
    private SickNoteService sickNoteService;

    @Autowired
    private PersonService personService;

    @Autowired
    private WorkDaysService calendarService;

    @InitBinder
    public void initBinder(DataBinder binder) {

        binder.registerCustomEditor(DateMidnight.class, new DateMidnightPropertyEditor());
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/sicknote/filter", method = RequestMethod.POST)
    public String filterSickNotes(@ModelAttribute("period") FilterPeriod period) {

        return "redirect:/web/sicknote?from=" + period.getStartDateAsString() + "&to=" + period.getEndDateAsString();
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/sicknote", method = RequestMethod.GET)
    public String periodsSickNotes(@RequestParam(value = "from", required = false) String from,
        @RequestParam(value = "to", required = false) String to, Model model) {

        FilterPeriod period = new FilterPeriod(Optional.ofNullable(from), Optional.ofNullable(to));

        List<SickNote> sickNoteList = sickNoteService.getByPeriod(period.getStartDate(), period.getEndDate());

        fillModel(model, sickNoteList, period);

        return "sicknote/sick_notes";
    }


    private void fillModel(Model model, List<SickNote> sickNotes, FilterPeriod period) {

        model.addAttribute("today", DateMidnight.now());
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

        model.addAttribute(PersonConstants.PERSONS_ATTRIBUTE, persons);
    }
}
