package org.synyx.urlaubsverwaltung.web.sicknote;

import org.joda.time.DateMidnight;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import org.synyx.urlaubsverwaltung.DateFormat;
import org.synyx.urlaubsverwaltung.core.calendar.WorkDaysService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteType;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;
import org.synyx.urlaubsverwaltung.security.SecurityRules;
import org.synyx.urlaubsverwaltung.web.FilterRequest;
import org.synyx.urlaubsverwaltung.web.person.PersonConstants;

import java.math.BigDecimal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Controller for overview about the sick days of all users.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Controller
public class SickDaysOverviewController {

    @Autowired
    private SickNoteService sickNoteService;

    @Autowired
    private PersonService personService;

    @Autowired
    private WorkDaysService calendarService;

    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/sicknote/filter", method = RequestMethod.POST)
    public String filterSickNotes(@ModelAttribute("filterRequest") FilterRequest filterRequest) {

        DateMidnight from = filterRequest.getStartDate();
        DateMidnight to = filterRequest.getEndDate();

        return "redirect:/web/sicknote?from=" + from.toString(DateFormat.PATTERN) + "&to="
            + to.toString(DateFormat.PATTERN);
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/sicknote", method = RequestMethod.GET)
    public String periodsSickNotes(@RequestParam(value = "from", required = false) String from,
        @RequestParam(value = "to", required = false) String to, Model model) {

        DateTimeFormatter formatter = DateTimeFormat.forPattern(DateFormat.PATTERN);
        int currentYear = DateMidnight.now().getYear();

        DateMidnight fromDate;
        DateMidnight toDate;

        if (from == null) {
            fromDate = DateUtil.getFirstDayOfYear(currentYear);
        } else {
            fromDate = DateMidnight.parse(from, formatter);
        }

        if (to == null) {
            toDate = DateUtil.getLastDayOfYear(currentYear);
        } else {
            toDate = DateMidnight.parse(to, formatter);
        }

        List<SickNote> sickNoteList = sickNoteService.getByPeriod(fromDate, toDate);

        fillModel(model, sickNoteList, fromDate, toDate);

        return "sicknote/sick_notes";
    }


    private void fillModel(Model model, List<SickNote> sickNotes, DateMidnight fromDate, DateMidnight toDate) {

        model.addAttribute("today", DateMidnight.now());
        model.addAttribute("from", fromDate);
        model.addAttribute("to", toDate);
        model.addAttribute("filterRequest", new FilterRequest());

        List<Person> persons = personService.getActivePersons();

        List<SickNote> sickNotesOfActivePersons = sickNotes.stream().filter(sickNote ->
                    persons.contains(sickNote.getPerson()) && sickNote.isActive()).collect(Collectors.toList());

        Map<Person, BigDecimal> sickDays = new HashMap<>();
        Map<Person, BigDecimal> sickDaysWithAUB = new HashMap<>();
        Map<Person, BigDecimal> childSickDays = new HashMap<>();
        Map<Person, BigDecimal> childSickDaysWithAUB = new HashMap<>();

        for (Person person : persons) {
            sickDays.put(person, BigDecimal.ZERO);
            sickDaysWithAUB.put(person, BigDecimal.ZERO);
            childSickDays.put(person, BigDecimal.ZERO);
            childSickDaysWithAUB.put(person, BigDecimal.ZERO);
        }

        for (SickNote sickNote : sickNotesOfActivePersons) {
            Person person = sickNote.getPerson();

            if (sickNote.getType().equals(SickNoteType.SICK_NOTE_CHILD)) {
                BigDecimal currentChildSickDays = childSickDays.get(person);
                childSickDays.put(person,
                    currentChildSickDays.add(
                        calendarService.getWorkDays(sickNote.getDayLength(), sickNote.getStartDate(),
                            sickNote.getEndDate(), person)));

                if (sickNote.isAubPresent()) {
                    BigDecimal workDays = calendarService.getWorkDays(sickNote.getDayLength(),
                            sickNote.getAubStartDate(), sickNote.getAubEndDate(), person);

                    BigDecimal currentChildSickDaysWithAUB = childSickDaysWithAUB.get(person);
                    childSickDaysWithAUB.put(person, currentChildSickDaysWithAUB.add(workDays));
                }
            } else {
                BigDecimal currentSickDays = sickDays.get(person);
                sickDays.put(person,
                    currentSickDays.add(
                        calendarService.getWorkDays(sickNote.getDayLength(), sickNote.getStartDate(),
                            sickNote.getEndDate(), person)));

                if (sickNote.isAubPresent()) {
                    BigDecimal workDays = calendarService.getWorkDays(sickNote.getDayLength(),
                            sickNote.getAubStartDate(), sickNote.getAubEndDate(), person);

                    BigDecimal currentSickDaysWithAUB = sickDaysWithAUB.get(person);
                    sickDaysWithAUB.put(person, currentSickDaysWithAUB.add(workDays));
                }
            }
        }

        model.addAttribute("sickDays", sickDays);
        model.addAttribute("sickDaysWithAUB", sickDaysWithAUB);
        model.addAttribute("childSickDays", childSickDays);
        model.addAttribute("childSickDaysWithAUB", childSickDaysWithAUB);

        model.addAttribute(PersonConstants.PERSONS_ATTRIBUTE, persons);
    }
}
