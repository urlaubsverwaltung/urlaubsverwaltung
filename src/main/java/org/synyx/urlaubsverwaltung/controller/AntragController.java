
package org.synyx.urlaubsverwaltung.controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import org.synyx.urlaubsverwaltung.domain.Antrag;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.State;
import org.synyx.urlaubsverwaltung.domain.VacationType;
import org.synyx.urlaubsverwaltung.service.AntragService;
import org.synyx.urlaubsverwaltung.service.PersonService;
import org.synyx.urlaubsverwaltung.util.DateService;

import java.util.List;


/**
 * @author  aljona
 */
public class AntragController {

    private PersonService personService;
    private AntragService antragService;
    private DateService dateService;

    public AntragController(PersonService personService, AntragService antragService, DateService dateService) {

        this.personService = personService;
        this.antragService = antragService;
        this.dateService = dateService;
    }

    @RequestMapping(value = "/antraege/wartend", method = RequestMethod.GET)
    public String showWaiting(Model model) {

        List<Antrag> antraege = antragService.getAllRequestsByState(State.WARTEND);
        model.addAttribute("requests", antraege);

        return "antraege/antragsliste";
    }


    @RequestMapping(value = "/antraege/genehmigt", method = RequestMethod.GET)
    public String showApproved(Model model) {

        List<Antrag> antraege = antragService.getAllRequestsByState(State.GENEHMIGT);
        model.addAttribute("requests", antraege);

        return "antraege/antragsliste";
    }


    @RequestMapping(value = "/antraege/storniert", method = RequestMethod.GET)
    public String showStorno(Model model) {

        List<Antrag> antraege = antragService.getAllRequestsByState(State.STORNIERT);
        model.addAttribute("requests", antraege);

        return "antraege/antragsliste";
    }


    @RequestMapping(value = "/{mitarbeiterId}/antrag", method = RequestMethod.GET)
    public String showUrlaubsantrag(@PathVariable("mitarbeiterId") Integer mitarbeiterId, Model model) {

        Person person = personService.getPersonByID(mitarbeiterId);
        List<Person> mitarbeiter = personService.getAllPersons();
        String date = dateService.getDate();
        Integer year = dateService.getYear();

        model.addAttribute("person", person);
        model.addAttribute("mitarbeiter", mitarbeiter);
        model.addAttribute("date", date);
        model.addAttribute("year", year);
        model.addAttribute("vacTypes", VacationType.values());

        return "antraege/antragform";
    }
}
