
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
import org.springframework.web.bind.annotation.ModelAttribute;


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

    /**
     * used if you want to see all waiting requests
     * @param model
     * @return 
     */
    @RequestMapping(value = "/antraege/wartend", method = RequestMethod.GET)
    public String showWaiting(Model model) {

        List<Antrag> antraege = antragService.getAllRequestsByState(State.WARTEND);
        model.addAttribute("requests", antraege);

        return "antraege/antragsliste";
    }


    /**
     * used if you want to see all approved requests
     * @param model
     * @return 
     */
    @RequestMapping(value = "/antraege/genehmigt", method = RequestMethod.GET)
    public String showApproved(Model model) {

        List<Antrag> antraege = antragService.getAllRequestsByState(State.GENEHMIGT);
        model.addAttribute("requests", antraege);

        return "antraege/antragsliste";
    }


    /**
     * used if you want to see al 'stornierte'requests
     * @param model
     * @return 
     */
    @RequestMapping(value = "/antraege/storniert", method = RequestMethod.GET)
    public String showStorno(Model model) {

        List<Antrag> antraege = antragService.getAllRequestsByState(State.STORNIERT);
        model.addAttribute("requests", antraege);

        return "antraege/antragsliste";
    }
    
    /**
     * used if you want to see al declined requests
     * @param model
     * @return 
     */
    @RequestMapping(value = "/antraege/abgelehnt", method = RequestMethod.GET)
    public String showDeclined(Model model) {

        List<Antrag> antraege = antragService.getAllRequestsByState(State.STORNIERT);
        model.addAttribute("requests", antraege);

        return "antraege/antragsliste";
    }
    
     /**
     * used if you want to cancel an existing request (owner only/maybe office)
     * @param model
     * @return 
     */
    @RequestMapping(value = "/{antragId}/stornieren", method = RequestMethod.GET)
    public String cancelAntrag(@PathVariable("antragId") Integer antragId, Model model) {

        //über die logik sollten wir nochmal nachdenken...
        antragService.storno(antragService.getRequestById(antragId));

        return "index"; //oder ne successpage oder was ganz anderes
    }
    
    /**
     * used if you want to cancel an existing request(boss only)
     * @param model
     * @return 
     */
    @RequestMapping(value = "/{antragId}/genehmigen", method = RequestMethod.GET)
    public String approveAntrag(@PathVariable("antragId") Integer antragId, Model model) {

        //über die logik sollten wir nochmal nachdenken...
        antragService.approve(antragService.getRequestById(antragId));
        
        //benachrichtigungs-zeugs

        return "index"; //oder ne successpage oder was ganz anderes
    }
    
    /**
     * used if you want to decline a request (boss only)
     * @param antragId the id of the to declining request
     * @param reason the reason of the rejection
     * @param model
     * @return 
     */
    @RequestMapping(value = "/{antragId}/ablehnen", method = RequestMethod.GET)
    public String declineAntrag(@PathVariable("antragId") Integer antragId, @ModelAttribute("reason") String reason, Model model) {

        //über die logik sollten wir nochmal nachdenken...
        antragService.decline(antragService.getRequestById(antragId),reason);
        
        //benachrichtigungs-zeugs

        return "index"; //oder ne successpage oder was ganz anderes
    }

    /**
     * used if you want to create a new request
     * @param mitarbeiterId id of the logged in user
     * @param model the datamodel
     * @return returns the path of the jsp-view
     */
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
    
    /**
     * use this to save an request(will be in "waiting" state
     * @param mitarbeiterId the id of the employee who made this request
     * @param antrag the request-object created by the form-entries
     * @param model
     * @return returns the path to a success-site("your request is being processed") or the main-page
     */
    @RequestMapping(value = "/{mitarbeiterId}/antrag", method = RequestMethod.POST)
    public String saveUrlaubsantrag(@PathVariable("mitarbeiterId") Integer mitarbeiterId, @ModelAttribute("antrag") Antrag antrag, Model model) {

        antrag.setPerson(personService.getPersonByID(mitarbeiterId));
        antrag.setState(State.WARTEND);
        antragService.save(antrag);

        return "index"; //oder vllt auch ine success-seite
    }
}
