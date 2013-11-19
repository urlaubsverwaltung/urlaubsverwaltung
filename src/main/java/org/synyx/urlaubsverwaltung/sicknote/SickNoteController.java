package org.synyx.urlaubsverwaltung.sicknote;

import org.joda.time.DateMidnight;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;

import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.util.DateMidnightPropertyEditor;
import org.synyx.urlaubsverwaltung.validator.SickNoteValidator;

import java.util.Locale;


/**
 * Controller for {@link SickNote} purposes.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Controller
public class SickNoteController {

    private SickNoteService sickNoteService;
    private PersonService personService;
    private SickNoteValidator validator;

    public SickNoteController(SickNoteService sickNoteService, PersonService personService,
        SickNoteValidator validator) {

        this.sickNoteService = sickNoteService;
        this.personService = personService;
        this.validator = validator;
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
    public String allSickNotes(Model model) {

        model.addAttribute("sickNotes", sickNoteService.getAll());

        return "sicknote/sick_notes";
    }


    @RequestMapping(value = "/sicknote", method = RequestMethod.POST)
    public String newSickNote(@ModelAttribute("sickNote") SickNote sickNote, Errors errors, Model model) {

        validator.validate(sickNote, errors);

        if (errors.hasErrors()) {
            model.addAttribute("sickNote", sickNote);
            model.addAttribute("persons", personService.getAllPersons());

            return "sicknote/sick_note_form";
        }

        sickNoteService.save(sickNote);

        return "redirect:/web/sicknote";
    }
}
