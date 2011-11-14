
package org.synyx.urlaubsverwaltung.controller;

import org.apache.log4j.Logger;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import org.synyx.urlaubsverwaltung.domain.Antrag;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.service.AntragService;
import org.synyx.urlaubsverwaltung.service.KontoService;
import org.synyx.urlaubsverwaltung.service.PersonService;
import org.synyx.urlaubsverwaltung.util.DateService;
import org.synyx.urlaubsverwaltung.util.KeyGenerator;

import java.security.NoSuchAlgorithmException;

import java.util.List;
import java.util.logging.Level;


/**
 * @author  aljona
 */
@Controller
public class PersonController {

    private static final String PERSON_ATTRIBUTE_NAME = "person";
    private static final String MITARBEITER_ATTRIBUTE_NAME = "mitarbeiter";

    private static final String MITARBEITER_ID = "mitarbeiterId";

    private static Logger logger = Logger.getLogger(PersonController.class);
    private static Logger personLogger = Logger.getLogger("personLogger");

    private PersonService personService;
    private AntragService antragService;
    private KontoService kontoService;
    private DateService dateService;
    private KeyGenerator keyGenerator;

    public PersonController(PersonService personService, AntragService antragService, KontoService kontoService,
        DateService dateService, KeyGenerator keyGenerator) {

        this.personService = personService;
        this.antragService = antragService;
        this.kontoService = kontoService;
        this.dateService = dateService;
        this.keyGenerator = keyGenerator;
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

        // nur für Ausprobieren des Loggers
        personLogger.info("Auf Liste geschaut.");

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

        // nur für Ausprobieren des Loggers
        personLogger.info("Auf Details geschaut.");

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
    public String showOverview(@PathVariable(MITARBEITER_ID) Integer mitarbeiterId, Model model) {

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
    public String editPersonForm(@PathVariable(MITARBEITER_ID) Integer mitarbeiterId, Model model) {

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
    public String editPerson(@ModelAttribute(PERSON_ATTRIBUTE_NAME) Person person,
        @PathVariable(MITARBEITER_ID) Integer mitarbeiterId) {

        Person personToUpdate = personService.getPersonByID(mitarbeiterId);

        personToUpdate.setLastName(person.getLastName());
        personToUpdate.setFirstName(person.getFirstName());
        personToUpdate.setEmail(person.getEmail());

        personService.save(personToUpdate);

        Integer year = person.getYearForCurrentUrlaubsanspruch();

        kontoService.newUrlaubsanspruch(person, year, person.getCurrentUrlaubsanspruch());
        kontoService.newUrlaubskonto(person, person.getCurrentUrlaubsanspruch(), 0, year);

        logger.info("Der Mitarbeiter " + person.getFirstName() + " " + person.getLastName() + " wurde editiert.");
        personLogger.info("Der Mitarbeiter " + person.getFirstName() + " " + person.getLastName() + " wurde editiert.");

        return "redirect:/web/mitarbeiter/list";
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
    public String newPerson(@ModelAttribute(PERSON_ATTRIBUTE_NAME) Person person) {

        Integer year = dateService.getYear();

        // neuen Urlaubsanspruch erstellen und speichern
        kontoService.newUrlaubsanspruch(person, year, person.getCurrentUrlaubsanspruch());

        // neues Urlaubskonto erstellen und speichern
        kontoService.newUrlaubskonto(person, person.getCurrentUrlaubsanspruch(), 0, year);

        String fingerprint = null;

        try {
            fingerprint = keyGenerator.getFingerprint();
        } catch (NoSuchAlgorithmException ex) {
            java.util.logging.Logger.getLogger(PersonController.class.getName()).log(Level.SEVERE, null, ex);
        }

        person.setSign(fingerprint);
        personService.save(person);

        logger.info("Neue Person angelegt: " + person.getFirstName() + " " + person.getLastName());
        personLogger.info("Neue Person angelegt: " + person.getFirstName() + " " + person.getLastName());

        return "redirect:/web/mitarbeiter/list";
    }
}
