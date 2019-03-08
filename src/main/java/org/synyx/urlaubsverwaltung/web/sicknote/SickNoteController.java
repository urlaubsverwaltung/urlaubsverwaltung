package org.synyx.urlaubsverwaltung.web.sicknote;

import org.joda.time.DateMidnight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.synyx.urlaubsverwaltung.core.application.service.VacationTypeService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.person.Role;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteAction;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteComment;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteCommentService;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteInteractionService;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteTypeService;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkDaysService;
import org.synyx.urlaubsverwaltung.security.SecurityRules;
import org.synyx.urlaubsverwaltung.security.SessionService;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;
import org.synyx.urlaubsverwaltung.web.DateMidnightPropertyEditor;
import org.synyx.urlaubsverwaltung.web.PersonPropertyEditor;
import org.synyx.urlaubsverwaltung.web.person.PersonConstants;

import java.util.List;
import java.util.Optional;


/**
 * Controller for {@link org.synyx.urlaubsverwaltung.core.sicknote.SickNote} purposes.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Controller
@RequestMapping("/web")
public class SickNoteController {

    @Autowired
    private SickNoteService sickNoteService;

    @Autowired
    private SickNoteInteractionService sickNoteInteractionService;

    @Autowired
    private SickNoteCommentService sickNoteCommentService;

    @Autowired
    private SickNoteTypeService sickNoteTypeService;

    @Autowired
    private VacationTypeService vacationTypeService;

    @Autowired
    private PersonService personService;

    @Autowired
    private WorkDaysService calendarService;

    @Autowired
    private SickNoteValidator validator;

    @Autowired
    private SickNoteConvertFormValidator sickNoteConvertFormValidator;

    @Autowired
    private SessionService sessionService;

    @InitBinder
    public void initBinder(DataBinder binder) {

        binder.registerCustomEditor(DateMidnight.class, new DateMidnightPropertyEditor());
        binder.registerCustomEditor(Person.class, new PersonPropertyEditor(personService));
    }


    @RequestMapping(value = "/sicknote/{id}", method = RequestMethod.GET)
    public String sickNoteDetails(@PathVariable("id") Integer id, Model model) throws UnknownSickNoteException {

        Person signedInUser = sessionService.getSignedInUser();

        SickNote sickNote = sickNoteService.getById(id).orElseThrow(() -> new UnknownSickNoteException(id));

        if (signedInUser.hasRole(Role.OFFICE) || sickNote.getPerson().equals(signedInUser)) {
            model.addAttribute("sickNote", new ExtendedSickNote(sickNote, calendarService));
            model.addAttribute("comment", new SickNoteComment());

            List<SickNoteComment> comments = sickNoteCommentService.getCommentsBySickNote(sickNote);
            model.addAttribute("comments", comments);

            return "sicknote/sick_note";
        }

        throw new AccessDeniedException(String.format(
                "User '%s' has not the correct permissions to see the sick note of user '%s'",
                signedInUser.getLoginName(), sickNote.getPerson().getLoginName()));
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/sicknote/new", method = RequestMethod.GET)
    public String newSickNote(Model model) {

        model.addAttribute("sickNote", new SickNote());
        model.addAttribute(PersonConstants.PERSONS_ATTRIBUTE, personService.getActivePersons());
        model.addAttribute("sickNoteTypes", sickNoteTypeService.getSickNoteTypes());

        return "sicknote/sick_note_form";
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/sicknote", method = RequestMethod.POST)
    public String newSickNote(@ModelAttribute("sickNote") SickNote sickNote, Errors errors, Model model) {

        validator.validate(sickNote, errors);

        if (errors.hasErrors()) {
            model.addAttribute(ControllerConstants.ERRORS_ATTRIBUTE, errors);
            model.addAttribute("sickNote", sickNote);
            model.addAttribute(PersonConstants.PERSONS_ATTRIBUTE, personService.getActivePersons());
            model.addAttribute("sickNoteTypes", sickNoteTypeService.getSickNoteTypes());

            return "sicknote/sick_note_form";
        }

        sickNoteInteractionService.create(sickNote, sessionService.getSignedInUser());

        return "redirect:/web/sicknote/" + sickNote.getId();
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/sicknote/{id}/edit", method = RequestMethod.GET)
    public String editSickNote(@PathVariable("id") Integer id, Model model) throws UnknownSickNoteException,
        SickNoteAlreadyInactiveException {

        SickNote sickNote = sickNoteService.getById(id).orElseThrow(() -> new UnknownSickNoteException(id));

        if (!sickNote.isActive()) {
            throw new SickNoteAlreadyInactiveException(id);
        }

        model.addAttribute("sickNote", sickNote);
        model.addAttribute("sickNoteTypes", sickNoteTypeService.getSickNoteTypes());

        return "sicknote/sick_note_form";
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/sicknote/{id}/edit", method = RequestMethod.POST)
    public String editSickNote(@PathVariable("id") Integer id,
        @ModelAttribute("sickNote") SickNote sickNote, Errors errors, Model model) {

        validator.validate(sickNote, errors);

        if (errors.hasErrors()) {
            model.addAttribute(ControllerConstants.ERRORS_ATTRIBUTE, errors);
            model.addAttribute("sickNote", sickNote);
            model.addAttribute("sickNoteTypes", sickNoteTypeService.getSickNoteTypes());

            return "sicknote/sick_note_form";
        }

        sickNoteInteractionService.update(sickNote, sessionService.getSignedInUser());

        return "redirect:/web/sicknote/" + id;
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/sicknote/{id}/comment", method = RequestMethod.POST)
    public String addComment(@PathVariable("id") Integer id,
        @ModelAttribute("comment") SickNoteComment comment, RedirectAttributes redirectAttributes, Errors errors)
        throws UnknownSickNoteException {

        SickNote sickNote = sickNoteService.getById(id).orElseThrow(() -> new UnknownSickNoteException(id));

        validator.validateComment(comment, errors);

        if (errors.hasErrors()) {
            redirectAttributes.addFlashAttribute(ControllerConstants.ERRORS_ATTRIBUTE, errors);
        } else {
            sickNoteCommentService.create(sickNote, SickNoteAction.COMMENTED, Optional.ofNullable(comment.getText()),
                sessionService.getSignedInUser());
        }

        return "redirect:/web/sicknote/" + id;
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/sicknote/{id}/convert", method = RequestMethod.GET)
    public String convertSickNoteToVacation(@PathVariable("id") Integer id, Model model)
        throws UnknownSickNoteException, SickNoteAlreadyInactiveException {

        SickNote sickNote = sickNoteService.getById(id).orElseThrow(() -> new UnknownSickNoteException(id));

        if (!sickNote.isActive()) {
            throw new SickNoteAlreadyInactiveException(id);
        }

        model.addAttribute("sickNote", new ExtendedSickNote(sickNote, calendarService));
        model.addAttribute("sickNoteConvertForm", new SickNoteConvertForm(sickNote));
        model.addAttribute("vacationTypes", vacationTypeService.getVacationTypes());

        return "sicknote/sick_note_convert";
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/sicknote/{id}/convert", method = RequestMethod.POST)
    public String convertSickNoteToVacation(@PathVariable("id") Integer id,
        @ModelAttribute("sickNoteConvertForm") SickNoteConvertForm sickNoteConvertForm, Errors errors, Model model)
        throws UnknownSickNoteException {

        SickNote sickNote = sickNoteService.getById(id).orElseThrow(() -> new UnknownSickNoteException(id));

        sickNoteConvertFormValidator.validate(sickNoteConvertForm, errors);

        if (errors.hasErrors()) {
            model.addAttribute(ControllerConstants.ERRORS_ATTRIBUTE, errors);
            model.addAttribute("sickNote", new ExtendedSickNote(sickNote, calendarService));
            model.addAttribute("sickNoteConvertForm", sickNoteConvertForm);
            model.addAttribute("vacationTypes", vacationTypeService.getVacationTypes());

            return "sicknote/sick_note_convert";
        }

        sickNoteInteractionService.convert(sickNote, sickNoteConvertForm.generateApplicationForLeave(),
            sessionService.getSignedInUser());

        return "redirect:/web/sicknote/" + id;
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/sicknote/{id}/cancel", method = RequestMethod.POST)
    public String cancelSickNote(@PathVariable("id") Integer id) throws UnknownSickNoteException {

        SickNote sickNote = sickNoteService.getById(id).orElseThrow(() -> new UnknownSickNoteException(id));

        sickNoteInteractionService.cancel(sickNote, sessionService.getSignedInUser());

        return "redirect:/web/sicknote/" + id;
    }
}
