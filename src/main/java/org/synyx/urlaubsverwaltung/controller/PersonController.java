
package org.synyx.urlaubsverwaltung.controller;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import org.synyx.urlaubsverwaltung.domain.Antrag;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.Urlaubsanspruch;
import org.synyx.urlaubsverwaltung.domain.Urlaubskonto;
import org.synyx.urlaubsverwaltung.service.AntragService;
import org.synyx.urlaubsverwaltung.service.KontoService;
import org.synyx.urlaubsverwaltung.service.PersonService;
import org.synyx.urlaubsverwaltung.util.DateService;

import java.util.List;


/**
 * @author  aljona
 */
@Controller
public class PersonController {

    private static final String PERSON_ATTRIBUTE_NAME = "person";
    private static final String MITARBEITER_ATTRIBUTE_NAME = "mitarbeiter";

    private PersonService personService;
    private AntragService antragService;
    private KontoService kontoService;
    private DateService dateService;

    public PersonController(PersonService personService, AntragService antragService, KontoService kontoService,
        DateService dateService) {

        this.personService = personService;
        this.antragService = antragService;
        this.kontoService = kontoService;
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

        model.addAttribute(MITARBEITER_ATTRIBUTE_NAME, mitarbeiter);

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

        model.addAttribute(MITARBEITER_ATTRIBUTE_NAME, mitarbeiter);

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
        model.addAttribute(PERSON_ATTRIBUTE_NAME, person);

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

        model.addAttribute(PERSON_ATTRIBUTE_NAME, person);

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

        personService.save(personToUpdate);

        Integer year = person.getYearForCurrentUrlaubsanspruch();

        kontoService.newUrlaubsanspruch(person, year, person.getCurrentUrlaubsanspruch());
        kontoService.newUrlaubskonto(person, person.getCurrentUrlaubsanspruch(), 0, year);

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

        model.addAttribute(PERSON_ATTRIBUTE_NAME, person);

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

        Integer year = dateService.getYear();
        Urlaubsanspruch urlaubsanspruch = new Urlaubsanspruch();
        Urlaubskonto urlaubskonto = new Urlaubskonto();

        urlaubsanspruch.setYear(year);
        urlaubsanspruch.setPerson(person);
        urlaubsanspruch.setVacationDays(person.getCurrentUrlaubsanspruch());
        kontoService.saveUrlaubsanspruch(null);

        urlaubskonto.setYear(year);
        urlaubskonto.setPerson(person);
        urlaubskonto.setVacationDays(person.getCurrentUrlaubsanspruch());
        kontoService.saveUrlaubskonto(urlaubskonto);

        personService.save(person);

        // braucht noch richtigen Verweis
        return "redirect:/web/mitarbeiter/list";
    }
}
