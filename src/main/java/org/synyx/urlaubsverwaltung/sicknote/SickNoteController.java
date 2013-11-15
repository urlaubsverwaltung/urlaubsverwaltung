package org.synyx.urlaubsverwaltung.sicknote;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import org.synyx.urlaubsverwaltung.person.PersonService;


/**
 * Controller for {@link SickNote} purposes.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Controller
public class SickNoteController {

    private SickNoteService sickNoteService;
    private PersonService personService;

    public SickNoteController(SickNoteService sickNoteService, PersonService personService) {

        this.sickNoteService = sickNoteService;
        this.personService = personService;
    }

    @RequestMapping(value = "/sicknote/new", method = RequestMethod.GET)
    public String newSickNote(Model model) {

        model.addAttribute("sickNote", new SickNote());
        model.addAttribute("persons", personService.getAllPersons());

        return "sicknote/sick_note_form";
    }
}
