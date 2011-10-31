
package org.synyx.urlaubsverwaltung.controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import org.synyx.urlaubsverwaltung.domain.Antrag;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.service.AntragService;
import org.synyx.urlaubsverwaltung.service.PersonService;
import org.synyx.urlaubsverwaltung.util.DateService;

import java.util.List;


/**
 * @author  aljona
 */
public class PersonController {

    private PersonService personService;
    private AntragService antragService;
    private DateService dateService;

    public PersonController(PersonService personService, AntragService antragService, DateService dateService) {

        this.personService = personService;
        this.antragService = antragService;
        this.dateService = dateService;
    }

    /**
     * Listenansicht aller Mitarbeiter und ihrer Urlaubstage
     *
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = "/mitarbeiter/list", method = RequestMethod.GET)
    public String showMitarbeiterList(Model model) {

        List<Person> mitarbeiter = personService.getAllPersons();

        model.addAttribute("mitarbeiter", mitarbeiter);

        return "personen/mitarbeiterliste";
    }


    /**
     * Detailansicht aller Mitarbeiter und ihrer Urlaubstage
     *
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = "/mitarbeiter/detail", method = RequestMethod.GET)
    public String showMitarbeiterDetail(Model model) {

        List<Person> mitarbeiter = personService.getAllPersons();

        model.addAttribute("mitarbeiter", mitarbeiter);

        return "personen/mitarbeiterdetails";
    }


    /**
     * Uebersicht fuer User: Infos zu einzelnem Mitarbeiter
     *
     * @param  mitarbeiterId
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = "/mitarbeiter/{mitarbeiterId}/overview", method = RequestMethod.GET)
    public String showOverview(@PathVariable("mitarbeiterId") Integer mitarbeiterId, Model model) {

        Person person = personService.getPersonByID(mitarbeiterId);

        List<Antrag> requests = antragService.getAllRequestsForPerson(person);

        model.addAttribute("year", dateService.getYear());
        model.addAttribute("requests", requests);
        model.addAttribute("person", person);

        return "personen/overview";
    }


    /**
     * Liefert Formular, um einen Mitarbeiter zu editieren
     *
     * @param  mitarbeiterId
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = "/mitarbeiter/{mitarbeiterId}/edit", method = RequestMethod.GET)
    public String editPersonForm(@PathVariable("mitarbeiterId") Integer mitarbeiterId, Model model) {

        Person person = personService.getPersonByID(mitarbeiterId);

        model.addAttribute("person", person);

        return "personen/personform";
    }


    /**
     * Speichert Datenaenderungen eines Mitarbeiters ab.
     *
     * @param  person
     * @param  mitarbeiterId
     *
     * @return
     */
    @RequestMapping(value = "/mitarbeiter/{mitarbeiterId}/edit", method = RequestMethod.PUT)
    public String editPerson(@ModelAttribute("person") Person person,
        @PathVariable("mitarbeiterId") Integer mitarbeiterId) {

        Person personToUpdate = personService.getPersonByID(mitarbeiterId);

        personToUpdate.setLastName(person.getLastName());
        personToUpdate.setFirstName(person.getFirstName());
        personToUpdate.setEmail(person.getEmail());
        personToUpdate.setVacationDays(person.getVacationDays());

        personService.save(personToUpdate);

        // braucht noch richtigen Verweis
        return "redirect:irgendwohin";
    }


    /**
     * Liefert Formular, um eine neue Person anzulegen.
     *
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = "/mitarbeiter/new", method = RequestMethod.GET)
    public String newPersonForm(Model model) {

        Person person = new Person();

        model.addAttribute("person", person);

        return "personen/personform";
    }


    /**
     * Speichert eine neu angelegte Person ab.
     *
     * @param  person
     *
     * @return
     */
    @RequestMapping(value = "/mitarbeiter/new", method = RequestMethod.POST)
    public String newPerson(@ModelAttribute("person") Person person) {

        personService.save(person);

        // braucht noch richtigen Verweis
        return "redirect:irgendwohin";
    }
}
