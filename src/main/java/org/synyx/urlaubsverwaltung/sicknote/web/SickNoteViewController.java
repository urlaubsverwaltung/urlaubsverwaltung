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
import org.synyx.urlaubsverwaltung.person.web.PersonPropertyEditor;
import org.synyx.urlaubsverwaltung.security.SecurityRules;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteAction;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteComment;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteCommentService;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteInteractionService;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteTypeService;
import org.synyx.urlaubsverwaltung.web.InstantPropertyEditor;
import org.synyx.urlaubsverwaltung.web.LocalDatePropertyEditor;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;


/**
 * Controller for {@link org.synyx.urlaubsverwaltung.sicknote.SickNote} purposes.
 */
@Controller
@RequestMapping("/web")
public class SickNoteViewController {

    private static final String PERSONS_ATTRIBUTE = "persons";
    private static final String SICKNOTE_SICK_NOTE_FORM = "sicknote/sick_note_form";
    private static final String SICK_NOTE = "sickNote";
    private static final String SICK_NOTE_TYPES = "sickNoteTypes";
    private static final String REDIRECT_WEB_SICKNOTE = "redirect:/web/sicknote/";
    private static final String ATTRIBUTE_ERRORS = "errors";

    private final SickNoteService sickNoteService;
    private final SickNoteInteractionService sickNoteInteractionService;
    private final SickNoteCommentService sickNoteCommentService;
    private final SickNoteTypeService sickNoteTypeService;
    private final VacationTypeService vacationTypeService;
    private final PersonService personService;
    private final WorkDaysCountService workDaysCountService;
    private final SickNoteValidator sickNoteValidator;
    private final SickNoteConvertFormValidator sickNoteConvertFormValidator;
    private final SettingsService settingsService;
    private final Clock clock;

    @Autowired
    public SickNoteViewController(SickNoteService sickNoteService, SickNoteInteractionService sickNoteInteractionService,
                                  SickNoteCommentService sickNoteCommentService, SickNoteTypeService sickNoteTypeService,
                                  VacationTypeService vacationTypeService, PersonService personService,
                                  WorkDaysCountService workDaysCountService, SickNoteValidator sickNoteValidator,
                                  SickNoteConvertFormValidator sickNoteConvertFormValidator, SettingsService settingsService, Clock clock) {

        this.sickNoteService = sickNoteService;
        this.sickNoteInteractionService = sickNoteInteractionService;
        this.sickNoteCommentService = sickNoteCommentService;
        this.sickNoteTypeService = sickNoteTypeService;
        this.vacationTypeService = vacationTypeService;
        this.personService = personService;
        this.workDaysCountService = workDaysCountService;
        this.sickNoteValidator = sickNoteValidator;
        this.sickNoteConvertFormValidator = sickNoteConvertFormValidator;
        this.settingsService = settingsService;
        this.clock = clock;
    }

    @InitBinder
    public void initBinder(DataBinder binder) {

        binder.registerCustomEditor(Instant.class, new InstantPropertyEditor(clock, settingsService));
        binder.registerCustomEditor(LocalDate.class, new LocalDatePropertyEditor());
        binder.registerCustomEditor(Person.class, new PersonPropertyEditor(personService));
    }


    @GetMapping("/sicknote/{id}")
    public String sickNoteDetails(@PathVariable("id") Integer id, Model model) throws UnknownSickNoteException {

        Person signedInUser = personService.getSignedInUser();

        SickNote sickNote = sickNoteService.getById(id).orElseThrow(() -> new UnknownSickNoteException(id));

        if (signedInUser.hasRole(Role.OFFICE) || sickNote.getPerson().equals(signedInUser)) {
            model.addAttribute(SICK_NOTE, new ExtendedSickNote(sickNote, workDaysCountService));
            model.addAttribute("comment", new SickNoteComment(clock));

            List<SickNoteComment> comments = sickNoteCommentService.getCommentsBySickNote(sickNote);
            model.addAttribute("comments", comments);

            return "sicknote/sick_note";
        }

        throw new AccessDeniedException(String.format(
            "User '%s' has not the correct permissions to see the sick note of user '%s'",
            signedInUser.getId(), sickNote.getPerson().getId()));
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @GetMapping("/sicknote/new")
    public String newSickNote(Model model) {

        model.addAttribute(SICK_NOTE, new SickNoteForm());
        model.addAttribute(PERSONS_ATTRIBUTE, personService.getActivePersons());
        model.addAttribute(SICK_NOTE_TYPES, sickNoteTypeService.getSickNoteTypes());

        return SICKNOTE_SICK_NOTE_FORM;
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @PostMapping("/sicknote")
    public String newSickNote(@ModelAttribute(SICK_NOTE) SickNoteForm sickNoteForm, Errors errors, Model model) {

        final SickNote sickNote = sickNoteForm.generateSickNote();

        sickNoteValidator.validate(sickNote, errors);
        if (errors.hasErrors()) {
            model.addAttribute(ATTRIBUTE_ERRORS, errors);
            model.addAttribute(SICK_NOTE, sickNoteForm);
            model.addAttribute(PERSONS_ATTRIBUTE, personService.getActivePersons());
            model.addAttribute(SICK_NOTE_TYPES, sickNoteTypeService.getSickNoteTypes());

            return SICKNOTE_SICK_NOTE_FORM;
        }

        sickNoteInteractionService.create(sickNote, personService.getSignedInUser(), sickNoteForm.getComment());

        return REDIRECT_WEB_SICKNOTE + sickNote.getId();
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @GetMapping("/sicknote/{id}/edit")
    public String editSickNote(@PathVariable("id") Integer id, Model model) throws UnknownSickNoteException,
        SickNoteAlreadyInactiveException {

        SickNote sickNote = sickNoteService.getById(id).orElseThrow(() -> new UnknownSickNoteException(id));

        SickNoteForm sickNoteForm = new SickNoteForm(sickNote);

        if (!sickNote.isActive()) {
            throw new SickNoteAlreadyInactiveException(id);
        }

        model.addAttribute(SICK_NOTE, sickNoteForm);
        model.addAttribute(SICK_NOTE_TYPES, sickNoteTypeService.getSickNoteTypes());

        return SICKNOTE_SICK_NOTE_FORM;
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @PostMapping("/sicknote/{id}/edit")
    public String editSickNote(@PathVariable("id") Integer id,
                               @ModelAttribute(SICK_NOTE) SickNoteForm sickNoteForm, Errors errors, Model model) {

        SickNote sickNote = sickNoteForm.generateSickNote();

        sickNoteValidator.validate(sickNote, errors);

        if (errors.hasErrors()) {
            model.addAttribute(ATTRIBUTE_ERRORS, errors);
            model.addAttribute(SICK_NOTE, sickNoteForm);
            model.addAttribute(SICK_NOTE_TYPES, sickNoteTypeService.getSickNoteTypes());

            return SICKNOTE_SICK_NOTE_FORM;
        }

        sickNoteInteractionService.update(sickNote, personService.getSignedInUser(), sickNoteForm.getComment());

        return REDIRECT_WEB_SICKNOTE + id;
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @PostMapping("/sicknote/{id}/comment")
    public String addComment(@PathVariable("id") Integer id,
                             @ModelAttribute("comment") SickNoteComment comment, RedirectAttributes redirectAttributes, Errors errors)
        throws UnknownSickNoteException {

        SickNote sickNote = sickNoteService.getById(id).orElseThrow(() -> new UnknownSickNoteException(id));

        sickNoteValidator.validateComment(comment, errors);

        if (errors.hasErrors()) {
            redirectAttributes.addFlashAttribute(ATTRIBUTE_ERRORS, errors);
        } else {
            sickNoteCommentService.create(sickNote, SickNoteAction.COMMENTED, personService.getSignedInUser(), comment.getText());
        }

        return REDIRECT_WEB_SICKNOTE + id;
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @GetMapping("/sicknote/{id}/convert")
    public String convertSickNoteToVacation(@PathVariable("id") Integer id, Model model)
        throws UnknownSickNoteException, SickNoteAlreadyInactiveException {

        SickNote sickNote = sickNoteService.getById(id).orElseThrow(() -> new UnknownSickNoteException(id));

        if (!sickNote.isActive()) {
            throw new SickNoteAlreadyInactiveException(id);
        }

        model.addAttribute(SICK_NOTE, new ExtendedSickNote(sickNote, workDaysCountService));
        model.addAttribute("sickNoteConvertForm", new SickNoteConvertForm(sickNote));
        model.addAttribute("vacationTypes", vacationTypeService.getVacationTypes());

        return "sicknote/sick_note_convert";
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @PostMapping("/sicknote/{id}/convert")
    public String convertSickNoteToVacation(@PathVariable("id") Integer id,
                                            @ModelAttribute("sickNoteConvertForm") SickNoteConvertForm sickNoteConvertForm, Errors errors, Model model)
        throws UnknownSickNoteException {

        final SickNote sickNote = sickNoteService.getById(id).orElseThrow(() -> new UnknownSickNoteException(id));

        sickNoteConvertFormValidator.validate(sickNoteConvertForm, errors);

        if (errors.hasErrors()) {
            model.addAttribute(ATTRIBUTE_ERRORS, errors);
            model.addAttribute(SICK_NOTE, new ExtendedSickNote(sickNote, workDaysCountService));
            model.addAttribute("sickNoteConvertForm", sickNoteConvertForm);
            model.addAttribute("vacationTypes", vacationTypeService.getVacationTypes());

            return "sicknote/sick_note_convert";
        }

        sickNoteInteractionService.convert(sickNote, sickNoteConvertForm.generateApplicationForLeave(clock), personService.getSignedInUser());

        return REDIRECT_WEB_SICKNOTE + id;
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @PostMapping("/sicknote/{id}/cancel")
    public String cancelSickNote(@PathVariable("id") Integer id) throws UnknownSickNoteException {

        SickNote sickNote = sickNoteService.getById(id).orElseThrow(() -> new UnknownSickNoteException(id));

        sickNoteInteractionService.cancel(sickNote, personService.getSignedInUser());

        return REDIRECT_WEB_SICKNOTE + id;
    }
}
