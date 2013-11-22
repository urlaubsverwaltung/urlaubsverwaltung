package org.synyx.urlaubsverwaltung.sicknote;

import org.joda.time.DateMidnight;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

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

import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.application.web.AppForm;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.security.web.SecurityUtil;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteComment;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteStatus;
import org.synyx.urlaubsverwaltung.sicknote.web.SearchRequest;
import org.synyx.urlaubsverwaltung.util.DateMidnightPropertyEditor;
import org.synyx.urlaubsverwaltung.validator.SickNoteValidator;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;

import java.util.List;
import java.util.Locale;


/**
 * Controller for {@link SickNote} purposes.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Controller
public class SickNoteController {

    private static final String DATE_PATTERN = "dd.MM.yyyy";

    private SickNoteService sickNoteService;
    private PersonService personService;
    private SickNoteValidator validator;
    private SecurityUtil securityUtil;

    public SickNoteController(SickNoteService sickNoteService, PersonService personService, SickNoteValidator validator,
        SecurityUtil securityUtil) {

        this.sickNoteService = sickNoteService;
        this.personService = personService;
        this.validator = validator;
        this.securityUtil = securityUtil;
    }

    @InitBinder
    public void initBinder(DataBinder binder, Locale locale) {

        binder.registerCustomEditor(DateMidnight.class, new DateMidnightPropertyEditor(locale));
        binder.registerCustomEditor(Person.class, new PersonPropertyEditor(personService));
    }


    @RequestMapping(value = "/sicknote/new", method = RequestMethod.GET)
    public String newSickNote(Model model) {

        model.addAttribute("sickNote", new SickNote());
        model.addAttribute("persons", personService.getAllPersons());

        return "sicknote/sick_note_form";
    }


    @RequestMapping(value = "/sicknote", method = RequestMethod.GET)
    public String allSickNotes() {

        DateMidnight now = DateMidnight.now();
        DateMidnight startDate = now.dayOfMonth().withMinimumValue();
        DateMidnight endDate = now.dayOfMonth().withMaximumValue();

        return "redirect:/web/sicknote?from=" + startDate.toString(DATE_PATTERN) + "&to="
            + endDate.toString(DATE_PATTERN);
    }


    @RequestMapping(value = "/sicknote/filter", method = RequestMethod.POST)
    public String filterSickNotes(@ModelAttribute("searchRequest") SearchRequest searchRequest) {

        Person person = personService.getPersonByID(searchRequest.getPersonId());

        if (person != null) {
            return "redirect:/web/sicknote?staff=" + person.getId() + "&from=" + searchRequest.getFrom() + "&to="
                + searchRequest.getTo();
        } else {
            return "redirect:/web/sicknote?from=" + searchRequest.getFrom() + "&to=" + searchRequest.getTo();
        }
    }


    @RequestMapping(value = "/sicknote", method = RequestMethod.GET, params = { "from", "to" })
    public String periodsSickNotes(@RequestParam("from") String from,
        @RequestParam("to") String to, Model model) {

        DateTimeFormatter formatter = DateTimeFormat.forPattern(DATE_PATTERN);
        DateMidnight fromDate = DateMidnight.parse(from, formatter);
        DateMidnight toDate = DateMidnight.parse(to, formatter);

        List<SickNote> sickNoteList = sickNoteService.getByPeriod(fromDate, toDate);

        fillModel(model, sickNoteList, fromDate, toDate);

        return "sicknote/sick_notes";
    }


    @RequestMapping(value = "/sicknote", method = RequestMethod.GET, params = { "staff", "from", "to" })
    public String personsSickNotes(@RequestParam("staff") Integer personId,
        @RequestParam("from") String from,
        @RequestParam("to") String to, Model model) {

        List<SickNote> sickNoteList;

        DateTimeFormatter formatter = DateTimeFormat.forPattern(DATE_PATTERN);
        DateMidnight fromDate = DateMidnight.parse(from, formatter);
        DateMidnight toDate = DateMidnight.parse(to, formatter);

        Person person = personService.getPersonByID(personId);
        sickNoteList = sickNoteService.getByPersonAndPeriod(person, fromDate, toDate);

        model.addAttribute("person", person);
        fillModel(model, sickNoteList, fromDate, toDate);

        return "sicknote/sick_notes";
    }


    private void fillModel(Model model, List<SickNote> sickNoteList, DateMidnight fromDate, DateMidnight toDate) {

        model.addAttribute("sickNotes", sickNoteList);
        model.addAttribute("today", DateMidnight.now());
        model.addAttribute("from", fromDate);
        model.addAttribute("to", toDate);
        model.addAttribute("searchRequest", new SearchRequest());
        model.addAttribute("persons", personService.getAllPersons());
    }


    @RequestMapping(value = "/sicknote/{id}", method = RequestMethod.GET)
    public String sickNoteDetails(@PathVariable("id") Integer id, Model model) {

        model.addAttribute("sickNote", sickNoteService.getById(id));
        model.addAttribute("comment", new SickNoteComment());

        return "sicknote/sick_note";
    }


    @RequestMapping(value = "/sicknote", method = RequestMethod.POST)
    public String newSickNote(@ModelAttribute("sickNote") SickNote sickNote, Errors errors, Model model) {

        validator.validate(sickNote, errors);

        if (errors.hasErrors()) {
            model.addAttribute("sickNote", sickNote);
            model.addAttribute("persons", personService.getAllPersons());

            return "sicknote/sick_note_form";
        }

        sickNoteService.touch(sickNote, SickNoteStatus.CREATED, securityUtil.getLoggedUser());

        return "redirect:/web/sicknote/" + sickNote.getId();
    }


    @RequestMapping(value = "/sicknote/{id}/edit", method = RequestMethod.GET)
    public String editSickNote(@PathVariable("id") Integer id, Model model) {

        SickNote sickNote = sickNoteService.getById(id);

        if (sickNote.isActive()) {
            model.addAttribute("sickNote", sickNote);

            return "sicknote/sick_note_form";
        }

        return ControllerConstants.ERROR_JSP;
    }


    @RequestMapping(value = "/sicknote/{id}/edit", method = RequestMethod.PUT)
    public String editSickNote(@PathVariable("id") Integer id,
        @ModelAttribute("sickNote") SickNote sickNote, Errors errors, Model model) {

        validator.validate(sickNote, errors);

        if (errors.hasErrors()) {
            model.addAttribute("sickNote", sickNote);

            return "sicknote/sick_note_form";
        }

        // this step is necessary because collections can not be binded with form:hidden
        sickNote.setComments(sickNoteService.getById(id).getComments());
        sickNoteService.touch(sickNote, SickNoteStatus.EDITED, securityUtil.getLoggedUser());

        return "redirect:/web/sicknote/" + id;
    }


    @RequestMapping(value = "/sicknote/{id}", method = RequestMethod.POST)
    public String addComment(@PathVariable("id") Integer id,
        @ModelAttribute("comment") SickNoteComment comment, Errors errors, Model model) {

        validator.validateComment(comment, errors);

        if (errors.hasErrors()) {
            model.addAttribute("sickNote", sickNoteService.getById(id));
            model.addAttribute("comment", comment);
            model.addAttribute("error", true);

            return "sicknote/sick_note";
        }

        sickNoteService.addComment(id, comment, SickNoteStatus.COMMENTED, securityUtil.getLoggedUser());

        return "redirect:/web/sicknote/" + id;
    }


    @RequestMapping(value = "/sicknote/{id}/convert", method = RequestMethod.GET)
    public String convertSickNoteToVacation(@PathVariable("id") Integer id, Model model) {

        SickNote sickNote = sickNoteService.getById(id);

        if (sickNote.isActive()) {
            model.addAttribute("sickNote", sickNote);
            model.addAttribute("appForm", new AppForm());
            model.addAttribute("vacTypes", VacationType.values());

            return "sicknote/sick_note_convert";
        }

        return ControllerConstants.ERROR_JSP;
    }


    @RequestMapping(value = "/sicknote/{id}/convert", method = RequestMethod.POST)
    public String convertSickNoteToVacation(@PathVariable("id") Integer id,
        @ModelAttribute("appForm") AppForm appForm, Model model) {

        SickNote sickNote = sickNoteService.getById(id);

        sickNoteService.convertSickNoteToVacation(appForm, sickNote, securityUtil.getLoggedUser());

        return "redirect:/web/sicknote/" + id;
    }
}
