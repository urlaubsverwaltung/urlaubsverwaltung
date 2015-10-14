package org.synyx.urlaubsverwaltung.web.sicknote;

import com.google.common.collect.FluentIterable;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

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

import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.calendar.WorkDaysService;
import org.synyx.urlaubsverwaltung.core.person.GravatarUtil;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.person.Role;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteInteractionService;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteType;
import org.synyx.urlaubsverwaltung.core.sicknote.comment.SickNoteAction;
import org.synyx.urlaubsverwaltung.core.sicknote.comment.SickNoteComment;
import org.synyx.urlaubsverwaltung.core.sicknote.comment.SickNoteCommentService;
import org.synyx.urlaubsverwaltung.security.SecurityRules;
import org.synyx.urlaubsverwaltung.security.SessionService;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;
import org.synyx.urlaubsverwaltung.web.DateMidnightPropertyEditor;
import org.synyx.urlaubsverwaltung.web.PersonPropertyEditor;
import org.synyx.urlaubsverwaltung.web.person.PersonConstants;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


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
    private SickNoteInteractionService sickNoteInteractionService;

    @Autowired
    private SickNoteCommentService sickNoteCommentService;

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
    public String sickNoteDetails(@PathVariable("id") Integer id, Model model) {

        Person signedInUser = sessionService.getSignedInUser();
        Optional<SickNote> sickNote = sickNoteService.getById(id);

        if (sickNote.isPresent()
                && (signedInUser.hasRole(Role.OFFICE) || sickNote.get().getPerson().equals(signedInUser))) {
            model.addAttribute("sickNote", new ExtendedSickNote(sickNote.get(), calendarService));
            model.addAttribute("comment", new SickNoteComment());
            model.addAttribute("gravatar", GravatarUtil.createImgURL(sickNote.get().getPerson().getEmail()));

            List<SickNoteComment> comments = sickNoteCommentService.getCommentsBySickNote(sickNote.get());
            model.addAttribute("comments", comments);

            Map<SickNoteComment, String> gravatarURLs = new HashMap<>();

            for (SickNoteComment comment : comments) {
                String gravatarUrl = GravatarUtil.createImgURL(comment.getPerson().getEmail());

                if (gravatarUrl != null) {
                    gravatarURLs.put(comment, gravatarUrl);
                }
            }

            model.addAttribute(PersonConstants.GRAVATAR_URLS_ATTRIBUTE, gravatarURLs);

            return "sicknote/sick_note";
        }

        return ControllerConstants.ERROR_JSP;
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/sicknote/new", method = RequestMethod.GET)
    public String newSickNote(Model model) {

        model.addAttribute("sickNote", new SickNote());
        model.addAttribute(PersonConstants.PERSONS_ATTRIBUTE, getPersons());
        model.addAttribute("sickNoteTypes", SickNoteType.values());

        return "sicknote/sick_note_form";
    }


    private List<Person> getPersons() {

        return FluentIterable.from(personService.getActivePersons()).toSortedList(new Comparator<Person>() {

                    @Override
                    public int compare(Person p1, Person p2) {

                        String niceName1 = p1.getNiceName();
                        String niceName2 = p2.getNiceName();

                        return niceName1.toLowerCase().compareTo(niceName2.toLowerCase());
                    }
                });
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/sicknote", method = RequestMethod.POST)
    public String newSickNote(@ModelAttribute("sickNote") SickNote sickNote, Errors errors, Model model) {

        validator.validate(sickNote, errors);

        if (errors.hasErrors()) {
            model.addAttribute(ControllerConstants.ERRORS_ATTRIBUTE, errors);
            model.addAttribute("sickNote", sickNote);
            model.addAttribute(PersonConstants.PERSONS_ATTRIBUTE, getPersons());
            model.addAttribute("sickNoteTypes", SickNoteType.values());

            return "sicknote/sick_note_form";
        }

        sickNoteInteractionService.create(sickNote, sessionService.getSignedInUser());

        return "redirect:/web/sicknote/" + sickNote.getId();
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/sicknote/{id}/edit", method = RequestMethod.GET)
    public String editSickNote(@PathVariable("id") Integer id, Model model) {

        Optional<SickNote> sickNote = sickNoteService.getById(id);

        if (sickNote.isPresent() && sickNote.get().isActive()) {
            model.addAttribute("sickNote", sickNote.get());
            model.addAttribute("sickNoteTypes", SickNoteType.values());

            return "sicknote/sick_note_form";
        }

        return ControllerConstants.ERROR_JSP;
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/sicknote/{id}/edit", method = RequestMethod.PUT)
    public String editSickNote(@PathVariable("id") Integer id,
        @ModelAttribute("sickNote") SickNote sickNote, Errors errors, Model model) {

        validator.validate(sickNote, errors);

        if (errors.hasErrors()) {
            model.addAttribute(ControllerConstants.ERRORS_ATTRIBUTE, errors);
            model.addAttribute("sickNote", sickNote);
            model.addAttribute("sickNoteTypes", SickNoteType.values());

            return "sicknote/sick_note_form";
        }

        sickNoteInteractionService.update(sickNote, sessionService.getSignedInUser());

        return "redirect:/web/sicknote/" + id;
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/sicknote/{id}/comment", method = RequestMethod.POST)
    public String addComment(@PathVariable("id") Integer id,
        @ModelAttribute("comment") SickNoteComment comment, RedirectAttributes redirectAttributes, Errors errors) {

        Optional<SickNote> sickNote = sickNoteService.getById(id);

        if (sickNote.isPresent()) {
            validator.validateComment(comment, errors);

            if (errors.hasErrors()) {
                redirectAttributes.addFlashAttribute(ControllerConstants.ERRORS_ATTRIBUTE, errors);
            } else {
                sickNoteCommentService.create(sickNote.get(), SickNoteAction.COMMENTED,
                    Optional.ofNullable(comment.getText()), sessionService.getSignedInUser());
            }

            return "redirect:/web/sicknote/" + id;
        }

        return ControllerConstants.ERROR_JSP;
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/sicknote/{id}/convert", method = RequestMethod.GET)
    public String convertSickNoteToVacation(@PathVariable("id") Integer id, Model model) {

        Optional<SickNote> sickNote = sickNoteService.getById(id);

        if (sickNote.isPresent() && sickNote.get().isActive()) {
            model.addAttribute("sickNote", new ExtendedSickNote(sickNote.get(), calendarService));
            model.addAttribute("sickNoteConvertForm", new SickNoteConvertForm(sickNote.get()));
            model.addAttribute("vacationTypes", VacationType.values());

            return "sicknote/sick_note_convert";
        }

        return ControllerConstants.ERROR_JSP;
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/sicknote/{id}/convert", method = RequestMethod.POST)
    public String convertSickNoteToVacation(@PathVariable("id") Integer id,
        @ModelAttribute("sickNoteConvertForm") SickNoteConvertForm sickNoteConvertForm, Errors errors, Model model) {

        Optional<SickNote> sickNote = sickNoteService.getById(id);

        if (sickNote.isPresent()) {
            sickNoteConvertFormValidator.validate(sickNoteConvertForm, errors);

            if (errors.hasErrors()) {
                model.addAttribute(ControllerConstants.ERRORS_ATTRIBUTE, errors);
                model.addAttribute("sickNote", new ExtendedSickNote(sickNote.get(), calendarService));
                model.addAttribute("sickNoteConvertForm", sickNoteConvertForm);
                model.addAttribute("vacationTypes", VacationType.values());

                return "sicknote/sick_note_convert";
            }

            sickNoteInteractionService.convert(sickNote.get(), sickNoteConvertForm.generateApplicationForLeave(),
                sessionService.getSignedInUser());

            return "redirect:/web/sicknote/" + id;
        }

        return ControllerConstants.ERROR_JSP;
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/sicknote/{id}/cancel", method = RequestMethod.POST)
    public String cancelSickNote(@PathVariable("id") Integer id) {

        Optional<SickNote> sickNote = sickNoteService.getById(id);

        if (sickNote.isPresent()) {
            sickNoteInteractionService.cancel(sickNote.get(), sessionService.getSignedInUser());

            return "redirect:/web/sicknote/" + id;
        }

        return ControllerConstants.ERROR_JSP;
    }
}
