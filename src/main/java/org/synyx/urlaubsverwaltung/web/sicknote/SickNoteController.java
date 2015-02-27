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
@RequestMapping("/sicknote")
@Controller
public class SickNoteController {

    @Autowired
    private SickNoteService sickNoteService;

    @Autowired
    private PersonService personService;

    @Autowired
    private SickNoteValidator validator;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private ApplicationValidator applicationValidator;

    @InitBinder
    public void initBinder(DataBinder binder, Locale locale) {

        binder.registerCustomEditor(DateMidnight.class, new DateMidnightPropertyEditor(locale));
        binder.registerCustomEditor(Person.class, new PersonPropertyEditor(personService));
    }


    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
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


    @RequestMapping(value = "/new", method = RequestMethod.GET)
    public String newSickNote(Model model) {

        if (sessionService.isOffice()) {
            model.addAttribute("sickNote", new SickNote());
            model.addAttribute("persons", personService.getActivePersons());
            model.addAttribute("sickNoteTypes", SickNoteType.values());

            return "sicknote/sick_note_form";
        }

        return ControllerConstants.ERROR_JSP;
    }


    @RequestMapping(value = "/", method = RequestMethod.POST)
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


    @RequestMapping(value = "/{id}/edit", method = RequestMethod.GET)
    public String editSickNote(@PathVariable("id") Integer id, Model model) {

        SickNote sickNote = sickNoteService.getById(id);

        if (sickNote.isActive() && sessionService.isOffice()) {
            model.addAttribute("sickNote", sickNote);
            model.addAttribute("sickNoteTypes", SickNoteType.values());

            return "sicknote/sick_note_form";
        }

        return ControllerConstants.ERROR_JSP;
    }


    @RequestMapping(value = "/{id}/edit", method = RequestMethod.PUT)
    public String editSickNote(@PathVariable("id") Integer id,
        @ModelAttribute("sickNote") SickNote sickNote, Errors errors, Model model) {

        if (sessionService.isOffice()) {
            validator.validate(sickNote, errors);

            if (errors.hasErrors()) {
                model.addAttribute("sickNote", sickNote);
                model.addAttribute("sickNoteTypes", SickNoteType.values());

                return "sicknote/sick_note_form";
            }

            // this step is necessary because collections can not be bind with form:hidden
            sickNote.setComments(sickNoteService.getById(id).getComments());
            sickNoteService.touch(sickNote, SickNoteStatus.EDITED, sessionService.getLoggedUser());

            return "redirect:/web/" + id;
        }

        return ControllerConstants.ERROR_JSP;
    }


    @RequestMapping(value = "/{id}/comment", method = RequestMethod.POST)
    public String addComment(@PathVariable("id") Integer id,
        @ModelAttribute("comment") SickNoteComment comment, RedirectAttributes redirectAttributes, Errors errors) {

        if (sessionService.isOffice()) {
            validator.validateComment(comment, errors);

            if (errors.hasErrors()) {
                redirectAttributes.addFlashAttribute("errors", errors);
            } else {
                sickNoteService.addComment(id, comment, SickNoteStatus.COMMENTED, sessionService.getLoggedUser());
            }

            return "redirect:/web/" + id;
        }

        return ControllerConstants.ERROR_JSP;
    }


    @RequestMapping(value = "/{id}/convert", method = RequestMethod.GET)
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


    @RequestMapping(value = "/{id}/convert", method = RequestMethod.POST)
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


    @RequestMapping(value = "/{id}/cancel", method = RequestMethod.POST)
    public String cancelSickNote(@PathVariable("id") Integer id) {

        if (sessionService.isOffice()) {
            SickNote sickNote = sickNoteService.getById(id);

            sickNoteService.cancel(sickNote, sessionService.getLoggedUser());

            return "redirect:/web/sicknote/" + id;
        }

        return ControllerConstants.ERROR_JSP;
    }
}
