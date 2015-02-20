package org.synyx.urlaubsverwaltung.web.sicknote;

import org.joda.time.DateMidnight;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;

import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.synyx.urlaubsverwaltung.DateFormat;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteType;
import org.synyx.urlaubsverwaltung.core.sicknote.comment.SickNoteComment;
import org.synyx.urlaubsverwaltung.core.sicknote.comment.SickNoteStatus;
import org.synyx.urlaubsverwaltung.core.sicknote.statistics.SickNoteStatistics;
import org.synyx.urlaubsverwaltung.core.sicknote.statistics.SickNoteStatisticsService;
import org.synyx.urlaubsverwaltung.security.Role;
import org.synyx.urlaubsverwaltung.security.SessionService;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;
import org.synyx.urlaubsverwaltung.web.application.AppForm;
import org.synyx.urlaubsverwaltung.web.person.PersonConstants;
import org.synyx.urlaubsverwaltung.web.util.DateMidnightPropertyEditor;
import org.synyx.urlaubsverwaltung.web.util.GravatarUtil;
import org.synyx.urlaubsverwaltung.web.validator.ApplicationValidator;
import org.synyx.urlaubsverwaltung.web.validator.SickNoteValidator;

import java.math.BigDecimal;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.security.RolesAllowed;


/**
 * Controller for {@link org.synyx.urlaubsverwaltung.core.sicknote.SickNote} purposes.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Controller
public class SickNoteController {

    @Autowired
    private SickNoteService sickNoteService;

    @Autowired
    private PersonService personService;

    @Autowired
    private OwnCalendarService calendarService;

    @Autowired
    private SickNoteValidator validator;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private ApplicationValidator applicationValidator;

    @Autowired
    private SickNoteStatisticsService statisticsService;

    @InitBinder
    public void initBinder(DataBinder binder, Locale locale) {

        binder.registerCustomEditor(DateMidnight.class, new DateMidnightPropertyEditor(locale));
        binder.registerCustomEditor(Person.class, new PersonPropertyEditor(personService));
    }


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
            DateMidnight now = DateMidnight.now();
            DateMidnight from = now;
            DateMidnight to = now;

            if (filterRequest.getPeriod().equals(FilterRequest.Period.YEAR)) {
                from = now.dayOfYear().withMinimumValue();
                to = now.dayOfYear().withMaximumValue();
            } else if (filterRequest.getPeriod().equals(FilterRequest.Period.QUARTAL)) {
                from = now.dayOfMonth().withMinimumValue().minusMonths(2);
                to = now.dayOfMonth().withMaximumValue();
            } else if (filterRequest.getPeriod().equals(FilterRequest.Period.MONTH)) {
                from = now.dayOfMonth().withMinimumValue();
                to = now.dayOfMonth().withMaximumValue();
            }

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
                childSickDays.put(person, currentChildSickDays.add(sickNote.getWorkDays()));

                if (sickNote.isAubPresent()) {
                    BigDecimal workDays = calendarService.getWorkDays(DayLength.FULL, sickNote.getAubStartDate(),
                            sickNote.getAubEndDate(), person);

                    BigDecimal currentChildSickDaysWithAUB = childSickDaysWithAUB.get(person);
                    childSickDaysWithAUB.put(person, currentChildSickDaysWithAUB.add(workDays));
                }
            } else {
                BigDecimal currentSickDays = sickDays.get(person);
                sickDays.put(person, currentSickDays.add(sickNote.getWorkDays()));

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


    @RequestMapping(value = "/sicknote/{id}", method = RequestMethod.GET)
    @RolesAllowed({ "USER", "OFFICE" })
    public String sickNoteDetails(@PathVariable("id") Integer id, Model model) {

        Person loggedUser = sessionService.getLoggedUser();
        SickNote sickNote = sickNoteService.getById(id);

        if (loggedUser.hasRole(Role.OFFICE) || sickNote.getPerson().equals(loggedUser)) {
            model.addAttribute("sickNote", sickNoteService.getById(id));
            model.addAttribute("comment", new SickNoteComment());
            model.addAttribute("gravatar", GravatarUtil.createImgURL(sickNote.getPerson().getEmail()));

            Map<SickNoteComment, String> gravatarUrls = new HashMap<>();

            for (SickNoteComment comment : sickNote.getComments()) {
                String gravatarUrl = GravatarUtil.createImgURL(comment.getPerson().getEmail());

                if (gravatarUrl != null) {
                    gravatarUrls.put(comment, gravatarUrl);
                }
            }

            model.addAttribute(PersonConstants.GRAVATAR_URLS, gravatarUrls);

            return "sicknote/sick_note";
        }

        return ControllerConstants.ERROR_JSP;
    }


    @RequestMapping(value = "/sicknote/new", method = RequestMethod.GET)
    public String newSickNote(Model model) {

        if (sessionService.isOffice()) {
            model.addAttribute("sickNote", new SickNote());
            model.addAttribute("persons", personService.getActivePersons());
            model.addAttribute("sickNoteTypes", SickNoteType.values());

            return "sicknote/sick_note_form";
        }

        return ControllerConstants.ERROR_JSP;
    }


    @RequestMapping(value = "/sicknote", method = RequestMethod.POST)
    public String newSickNote(@ModelAttribute("sickNote") SickNote sickNote, Errors errors, Model model) {

        if (sessionService.isOffice()) {
            validator.validate(sickNote, errors);

            if (errors.hasErrors()) {
                model.addAttribute("sickNote", sickNote);
                model.addAttribute("persons", personService.getActivePersons());
                model.addAttribute("sickNoteTypes", SickNoteType.values());

                return "sicknote/sick_note_form";
            }

            sickNoteService.touch(sickNote, SickNoteStatus.CREATED, sessionService.getLoggedUser());

            return "redirect:/web/sicknote/" + sickNote.getId();
        }

        return ControllerConstants.ERROR_JSP;
    }


    @RequestMapping(value = "/sicknote/{id}/edit", method = RequestMethod.GET)
    public String editSickNote(@PathVariable("id") Integer id, Model model) {

        SickNote sickNote = sickNoteService.getById(id);

        if (sickNote.isActive() && sessionService.isOffice()) {
            model.addAttribute("sickNote", sickNote);
            model.addAttribute("sickNoteTypes", SickNoteType.values());

            return "sicknote/sick_note_form";
        }

        return ControllerConstants.ERROR_JSP;
    }


    @RequestMapping(value = "/sicknote/{id}/edit", method = RequestMethod.PUT)
    public String editSickNote(@PathVariable("id") Integer id,
        @ModelAttribute("sickNote") SickNote sickNote, Errors errors, Model model) {

        if (sessionService.isOffice()) {
            validator.validate(sickNote, errors);

            if (errors.hasErrors()) {
                model.addAttribute("sickNote", sickNote);
                model.addAttribute("sickNoteTypes", SickNoteType.values());

                return "sicknote/sick_note_form";
            }

            // this step is necessary because collections can not be binded with form:hidden
            sickNote.setComments(sickNoteService.getById(id).getComments());
            sickNoteService.touch(sickNote, SickNoteStatus.EDITED, sessionService.getLoggedUser());

            return "redirect:/web/sicknote/" + id;
        }

        return ControllerConstants.ERROR_JSP;
    }


    @RequestMapping(value = "/sicknote/{id}/comment", method = RequestMethod.POST)
    public String addComment(@PathVariable("id") Integer id,
        @ModelAttribute("comment") SickNoteComment comment, RedirectAttributes redirectAttributes, Errors errors) {

        if (sessionService.isOffice()) {
            validator.validateComment(comment, errors);

            if (errors.hasErrors()) {
                redirectAttributes.addFlashAttribute("errors", errors);
            } else {
                sickNoteService.addComment(id, comment, SickNoteStatus.COMMENTED, sessionService.getLoggedUser());
            }

            return "redirect:/web/sicknote/" + id;
        }

        return ControllerConstants.ERROR_JSP;
    }


    @RequestMapping(value = "/sicknote/{id}/convert", method = RequestMethod.GET)
    public String convertSickNoteToVacation(@PathVariable("id") Integer id, Model model) {

        SickNote sickNote = sickNoteService.getById(id);

        if (sickNote.isActive() && sessionService.isOffice()) {
            model.addAttribute("sickNote", sickNote);
            model.addAttribute("appForm", new AppForm());
            model.addAttribute("vacTypes", VacationType.values());

            return "sicknote/sick_note_convert";
        }

        return ControllerConstants.ERROR_JSP;
    }


    @RequestMapping(value = "/sicknote/{id}/convert", method = RequestMethod.POST)
    public String convertSickNoteToVacation(@PathVariable("id") Integer id,
        @ModelAttribute("appForm") AppForm appForm, Errors errors, Model model) {

        if (sessionService.isOffice()) {
            SickNote sickNote = sickNoteService.getById(id);

            applicationValidator.validatedShortenedAppForm(appForm, errors);

            if (errors.hasErrors()) {
                model.addAttribute("sickNote", sickNote);
                model.addAttribute("appForm", appForm);
                model.addAttribute("vacTypes", VacationType.values());

                return "sicknote/sick_note_convert";
            }

            sickNoteService.convertSickNoteToVacation(appForm, sickNote, sessionService.getLoggedUser());

            return "redirect:/web/sicknote/" + id;
        }

        return ControllerConstants.ERROR_JSP;
    }


    @RequestMapping(value = "/sicknote/{id}/cancel", method = RequestMethod.POST)
    public String cancelSickNote(@PathVariable("id") Integer id, Model model) {

        if (sessionService.isOffice()) {
            SickNote sickNote = sickNoteService.getById(id);

            sickNoteService.cancel(sickNote, sessionService.getLoggedUser());

            return "redirect:/web/sicknote/" + id;
        }

        return ControllerConstants.ERROR_JSP;
    }


    @RequestMapping(value = "/sicknote/statistics", method = RequestMethod.GET, params = "year")
    public String sickNotesStatistics(@RequestParam("year") Integer year, Model model) {

        if (sessionService.isOffice()) {
            SickNoteStatistics statistics = statisticsService.createStatistics(year);

            model.addAttribute("statistics", statistics);

            return "sicknote/sick_notes_statistics";
        }

        return ControllerConstants.ERROR_JSP;
    }
}
