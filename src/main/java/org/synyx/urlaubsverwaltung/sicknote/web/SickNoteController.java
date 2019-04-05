package org.synyx.urlaubsverwaltung.sicknote.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.synyx.urlaubsverwaltung.application.service.VacationTypeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteAction;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteComment;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteCommentService;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteInteractionService;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteTypeService;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysService;
import org.synyx.urlaubsverwaltung.security.SecurityRules;
import org.synyx.urlaubsverwaltung.security.SessionService;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;
import org.synyx.urlaubsverwaltung.web.LocalDatePropertyEditor;
import org.synyx.urlaubsverwaltung.web.PersonPropertyEditor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


/**
 * Controller for {@link org.synyx.urlaubsverwaltung.sicknote.SickNote} purposes.
 */
@Controller
@RequestMapping("/web")
public class SickNoteController {

    private static final String PERSONS_ATTRIBUTE = "persons";

    private final SickNoteService sickNoteService;
    private final SickNoteInteractionService sickNoteInteractionService;
    private final SickNoteCommentService sickNoteCommentService;
    private final SickNoteTypeService sickNoteTypeService;
    private final VacationTypeService vacationTypeService;
    private final PersonService personService;
    private final WorkDaysService calendarService;
    private final SickNoteValidator validator;
    private final SickNoteConvertFormValidator sickNoteConvertFormValidator;
    private final SessionService sessionService;

    @Autowired
    public SickNoteController(SickNoteService sickNoteService, SickNoteInteractionService sickNoteInteractionService, SickNoteCommentService sickNoteCommentService, SickNoteTypeService sickNoteTypeService, VacationTypeService vacationTypeService, PersonService personService, WorkDaysService calendarService, SickNoteValidator validator, SickNoteConvertFormValidator sickNoteConvertFormValidator, SessionService sessionService) {
        this.sickNoteService = sickNoteService;
        this.sickNoteInteractionService = sickNoteInteractionService;
        this.sickNoteCommentService = sickNoteCommentService;
        this.sickNoteTypeService = sickNoteTypeService;
        this.vacationTypeService = vacationTypeService;
        this.personService = personService;
        this.calendarService = calendarService;
        this.validator = validator;
        this.sickNoteConvertFormValidator = sickNoteConvertFormValidator;
        this.sessionService = sessionService;
    }

    @InitBinder
    public void initBinder(DataBinder binder) {

        binder.registerCustomEditor(LocalDate.class, new LocalDatePropertyEditor());
        binder.registerCustomEditor(Person.class, new PersonPropertyEditor(personService));
    }


    @GetMapping("/sicknote/{id}")
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
    @GetMapping("/sicknote/new")
    public String newSickNote(Model model) {

        model.addAttribute("sickNote", new SickNote());
        model.addAttribute(PERSONS_ATTRIBUTE, personService.getActivePersons());
        model.addAttribute("sickNoteTypes", sickNoteTypeService.getSickNoteTypes());

        return "sicknote/sick_note_form";
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @PostMapping("/sicknote")
    public String newSickNote(@ModelAttribute("sickNote") SickNote sickNote, Errors errors, Model model) {

        validator.validate(sickNote, errors);

        if (errors.hasErrors()) {
            model.addAttribute(ControllerConstants.ERRORS_ATTRIBUTE, errors);
            model.addAttribute("sickNote", sickNote);
            model.addAttribute(PERSONS_ATTRIBUTE, personService.getActivePersons());
            model.addAttribute("sickNoteTypes", sickNoteTypeService.getSickNoteTypes());

            return "sicknote/sick_note_form";
        }

        sickNoteInteractionService.create(sickNote, sessionService.getSignedInUser());

        return "redirect:/web/sicknote/" + sickNote.getId();
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @GetMapping("/sicknote/{id}/edit")
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
    @PostMapping("/sicknote/{id}/edit")
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
    @PostMapping("/sicknote/{id}/comment")
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
    @GetMapping("/sicknote/{id}/convert")
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
    @PostMapping("/sicknote/{id}/convert")
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
    @PostMapping("/sicknote/{id}/cancel")
    public String cancelSickNote(@PathVariable("id") Integer id) throws UnknownSickNoteException {

        SickNote sickNote = sickNoteService.getById(id).orElseThrow(() -> new UnknownSickNoteException(id));

        sickNoteInteractionService.cancel(sickNote, sessionService.getSignedInUser());

        return "redirect:/web/sicknote/" + id;
    }
}
