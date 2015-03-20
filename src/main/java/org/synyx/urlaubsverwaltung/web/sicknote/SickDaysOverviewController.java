package org.synyx.urlaubsverwaltung.web.sicknote;

import org.joda.time.DateMidnight;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import org.synyx.urlaubsverwaltung.DateFormat;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteType;
import org.synyx.urlaubsverwaltung.security.SessionService;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;
import org.synyx.urlaubsverwaltung.web.FilterRequest;
import org.synyx.urlaubsverwaltung.web.util.GravatarUtil;

import java.math.BigDecimal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Controller for overview about the sick days of all users.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Controller
public class SickDaysOverviewController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private SickNoteService sickNoteService;

    @Autowired
    private PersonService personService;

    @Autowired
    private OwnCalendarService calendarService;

    @RequestMapping(value = "/sicknote", method = RequestMethod.GET)
    public String defaultSickNotes() {

        DateMidnight now = DateMidnight.now();
        DateMidnight from = now.dayOfYear().withMinimumValue();
        DateMidnight to = now.dayOfYear().withMaximumValue();

        return "redirect:/web/sicknote?from=" + from.toString(DateFormat.PATTERN) + "&to="
            + to.toString(DateFormat.PATTERN);
    }


    @RequestMapping(value = "/sicknote/quartal", method = RequestMethod.GET)
    public String quartalSickNotes() {

        if (sessionService.isOffice()) {
            DateMidnight now = DateMidnight.now();

            DateMidnight from = now.dayOfMonth().withMinimumValue().minusMonths(2);
            DateMidnight to = now.dayOfMonth().withMaximumValue();

            return "redirect:/web/sicknote?from=" + from.toString(DateFormat.PATTERN) + "&to="
                + to.toString(DateFormat.PATTERN);
        }

        return ControllerConstants.ERROR_JSP;
    }


    @RequestMapping(value = "/sicknote/filter", method = RequestMethod.POST)
    public String filterSickNotes(@ModelAttribute("filterRequest") FilterRequest filterRequest) {

        if (sessionService.isOffice()) {
            DateMidnight from = filterRequest.getStartDate();
            DateMidnight to = filterRequest.getEndDate();

            return "redirect:/web/sicknote?from=" + from.toString(DateFormat.PATTERN) + "&to="
                + to.toString(DateFormat.PATTERN);
        }

        return ControllerConstants.ERROR_JSP;
    }


    @RequestMapping(value = "/sicknote", method = RequestMethod.GET, params = { "from", "to" })
    public String periodsSickNotes(@RequestParam("from") String from,
        @RequestParam("to") String to, Model model) {

        if (sessionService.isOffice()) {
            DateTimeFormatter formatter = DateTimeFormat.forPattern(DateFormat.PATTERN);
            DateMidnight fromDate = DateMidnight.parse(from, formatter);
            DateMidnight toDate = DateMidnight.parse(to, formatter);

            List<SickNote> sickNoteList = sickNoteService.getByPeriod(fromDate, toDate);

            fillModel(model, sickNoteList, fromDate, toDate);

            return "sicknote/sick_notes";
        }

        return ControllerConstants.ERROR_JSP;
    }


    private void fillModel(Model model, List<SickNote> sickNotes, DateMidnight fromDate, DateMidnight toDate) {

        model.addAttribute("today", DateMidnight.now());
        model.addAttribute("from", fromDate);
        model.addAttribute("to", toDate);
        model.addAttribute("filterRequest", new FilterRequest());

        List<Person> persons = personService.getActivePersons();

        Map<Person, BigDecimal> sickDays = new HashMap<>();
        Map<Person, BigDecimal> sickDaysWithAUB = new HashMap<>();
        Map<Person, BigDecimal> childSickDays = new HashMap<>();
        Map<Person, BigDecimal> childSickDaysWithAUB = new HashMap<>();

        Map<Person, String> gravatars = new HashMap<>();

        for (Person person : persons) {
            sickDays.put(person, BigDecimal.ZERO);
            sickDaysWithAUB.put(person, BigDecimal.ZERO);
            childSickDays.put(person, BigDecimal.ZERO);
            childSickDaysWithAUB.put(person, BigDecimal.ZERO);

            gravatars.put(person, GravatarUtil.createImgURL(person.getEmail()));
        }

        for (SickNote sickNote : sickNotes) {
            if (!sickNote.isActive()) {
                continue;
            }

            Person person = sickNote.getPerson();

            if (sickNote.getType().equals(SickNoteType.SICK_NOTE_CHILD)) {
                BigDecimal currentChildSickDays = childSickDays.get(person);
                childSickDays.put(person,
                    currentChildSickDays.add(
                        calendarService.getWorkDays(DayLength.FULL, sickNote.getStartDate(), sickNote.getEndDate(),
                            person)));

                if (sickNote.isAubPresent()) {
                    BigDecimal workDays = calendarService.getWorkDays(DayLength.FULL, sickNote.getAubStartDate(),
                            sickNote.getAubEndDate(), person);

                    BigDecimal currentChildSickDaysWithAUB = childSickDaysWithAUB.get(person);
                    childSickDaysWithAUB.put(person, currentChildSickDaysWithAUB.add(workDays));
                }
            } else {
                BigDecimal currentSickDays = sickDays.get(person);
                sickDays.put(person,
                    currentSickDays.add(
                        calendarService.getWorkDays(DayLength.FULL, sickNote.getStartDate(), sickNote.getEndDate(),
                            person)));

                if (sickNote.isAubPresent()) {
                    BigDecimal workDays = calendarService.getWorkDays(DayLength.FULL, sickNote.getAubStartDate(),
                            sickNote.getAubEndDate(), person);

                    BigDecimal currentSickDaysWithAUB = sickDaysWithAUB.get(person);
                    sickDaysWithAUB.put(person, currentSickDaysWithAUB.add(workDays));
                }
            }
        }

        model.addAttribute("sickDays", sickDays);
        model.addAttribute("sickDaysWithAUB", sickDaysWithAUB);
        model.addAttribute("childSickDays", childSickDays);
        model.addAttribute("childSickDaysWithAUB", childSickDaysWithAUB);

        model.addAttribute("persons", persons);
        model.addAttribute("gravatars", gravatars);
    }
}
