
package org.synyx.urlaubsverwaltung.controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.ModelAttribute;
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

    private static final String REQUEST_ATTRIBUTE_NAME = "requests";
    private static final String REQUESTLIST_VIEW = "antraege/antragsliste";
    private static final String ACTION_COMPLETE_VIEW = "index"; // muss noch geändert werden

    private PersonService personService;
    private AntragService antragService;
    private DateService dateService;

    public AntragController(PersonService personService, AntragService antragService, DateService dateService) {

        this.personService = personService;
        this.antragService = antragService;
        this.dateService = dateService;
    }

    /**
     * show List<Antrag> of one person
     *
     * @param  mitarbeiterId
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = "/antraege/{mitarbeiterId}", method = RequestMethod.GET)
    public String showAntraegeByPerson(@PathVariable("mitarbeiterId") Integer mitarbeiterId, Model model) {

        Person person = personService.getPersonByID(mitarbeiterId);
        List<Antrag> antraege = antragService.getAllRequestsForPerson(person);

        model.addAttribute("person", person);
        model.addAttribute(REQUEST_ATTRIBUTE_NAME, antraege);

        return "antraege/antraege";
    }


    /**
     * used if you want to see all waiting requests
     *
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = "/antraege/wartend", method = RequestMethod.GET)
    public String showWaiting(Model model) {

        List<Antrag> antraege = antragService.getAllRequestsByState(State.WARTEND);
        model.addAttribute(REQUEST_ATTRIBUTE_NAME, antraege);

        return REQUESTLIST_VIEW;
    }


    /**
     * used if you want to see all approved requests
     *
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = "/antraege/genehmigt", method = RequestMethod.GET)
    public String showApproved(Model model) {

        List<Antrag> antraege = antragService.getAllRequestsByState(State.GENEHMIGT);
        model.addAttribute(REQUEST_ATTRIBUTE_NAME, antraege);

        return REQUESTLIST_VIEW;
    }


    /**
     * used if you want to see all cancelled requests
     *
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = "/antraege/storniert", method = RequestMethod.GET)
    public String showStorno(Model model) {

        List<Antrag> antraege = antragService.getAllRequestsByState(State.STORNIERT);
        model.addAttribute(REQUEST_ATTRIBUTE_NAME, antraege);

        return REQUESTLIST_VIEW;
    }


    /**
     * used if you want to see all declined requests
     *
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = "/antraege/abgelehnt", method = RequestMethod.GET)
    public String showDeclined(Model model) {

        List<Antrag> antraege = antragService.getAllRequestsByState(State.STORNIERT);
        model.addAttribute(REQUEST_ATTRIBUTE_NAME, antraege);

        return REQUESTLIST_VIEW;
    }


    /**
     * used if you want to create a new request (shows formular)
     *
     * @param  mitarbeiterId  id of the logged in user
     * @param  model  the datamodel
     *
     * @return  returns the path of the jsp-view
     */
    @RequestMapping(value = "/antrag/{mitarbeiterId}/new", method = RequestMethod.GET)
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


    /**
     * use this to save an request (will be in "waiting" state)
     *
     * @param  mitarbeiterId  the id of the employee who made this request
     * @param  antrag  the request-object created by the form-entries
     * @param  model
     *
     * @return  returns the path to a success-site ("your request is being processed") or the main-page
     */
    @RequestMapping(value = "/antrag/{mitarbeiterId}/new", method = RequestMethod.POST)
    public String saveUrlaubsantrag(@PathVariable("mitarbeiterId") Integer mitarbeiterId,
        @ModelAttribute("antrag") Antrag antrag, Model model) {

        antrag.setPerson(personService.getPersonByID(mitarbeiterId));
        antrag.setState(State.WARTEND);
        antragService.save(antrag);

        return ACTION_COMPLETE_VIEW; // oder vllt auch ine success-seite
    }


    /**
     * view for chef who has to decide if he approves or declines the request
     *
     * @param  antragId
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = "/antrag/{antragId}", method = RequestMethod.GET)
    public String showAntragDetail(@PathVariable("antragId") Integer antragId, Model model) {

        Antrag antrag = antragService.getRequestById(antragId);

        model.addAttribute("antrag", antrag);

        return "antraege/antragdetail";
    }


    /**
     * used if you want to cancel an existing request (owner only/maybe office)
     *
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = "/antrag/{antragId}/stornieren", method = RequestMethod.PUT)
    public String cancelAntrag(@PathVariable("antragId") Integer antragId, Model model) {

        // über die logik sollten wir nochmal nachdenken...
        antragService.storno(antragService.getRequestById(antragId));

        return ACTION_COMPLETE_VIEW; // oder ne successpage oder was ganz anderes
    }


    /**
     * used if you want to approve an existing request (boss only)
     *
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = "/antrag/{antragId}/genehmigen", method = RequestMethod.PUT)
    public String approveAntrag(@PathVariable("antragId") Integer antragId, Model model) {

        // über die logik sollten wir nochmal nachdenken...
        antragService.approve(antragService.getRequestById(antragId));

        // benachrichtigungs-zeugs

        return ACTION_COMPLETE_VIEW; // oder ne successpage oder was ganz anderes
    }


    /**
     * used if you want to decline a request (boss only)
     *
     * @param  antragId  the id of the to declining request
     * @param  reason  the reason of the rejection
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = "/antrag/{antragId}/ablehnen", method = RequestMethod.PUT)
    public String declineAntrag(@PathVariable("antragId") Integer antragId,
        @ModelAttribute("reasonForDeclining") String reasonForDeclining, Model model) {

        // über die logik sollten wir nochmal nachdenken...
        antragService.decline(antragService.getRequestById(antragId), reasonForDeclining);

        // benachrichtigungs-zeugs

        return ACTION_COMPLETE_VIEW; // oder ne successpage oder was ganz anderes
    }
}
