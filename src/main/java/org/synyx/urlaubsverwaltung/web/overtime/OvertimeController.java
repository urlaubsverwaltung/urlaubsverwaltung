package org.synyx.urlaubsverwaltung.web.overtime;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;

import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.synyx.urlaubsverwaltung.core.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.core.overtime.Overtime;
import org.synyx.urlaubsverwaltung.core.overtime.OvertimeAction;
import org.synyx.urlaubsverwaltung.core.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.person.Role;
import org.synyx.urlaubsverwaltung.security.SessionService;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;
import org.synyx.urlaubsverwaltung.web.DateMidnightPropertyEditor;
import org.synyx.urlaubsverwaltung.web.DecimalNumberPropertyEditor;
import org.synyx.urlaubsverwaltung.web.PersonPropertyEditor;
import org.synyx.urlaubsverwaltung.web.person.PersonConstants;
import org.synyx.urlaubsverwaltung.web.person.UnknownPersonException;

import java.math.BigDecimal;

import java.util.Locale;
import java.util.Optional;


/**
 * Manage overtime of persons.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Controller
@RequestMapping("/web")
public class OvertimeController {

    @Autowired
    private OvertimeService overtimeService;

    @Autowired
    private PersonService personService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private OvertimeValidator validator;

    @InitBinder
    public void initBinder(DataBinder binder, Locale locale) {

        binder.registerCustomEditor(DateMidnight.class, new DateMidnightPropertyEditor());
        binder.registerCustomEditor(BigDecimal.class, new DecimalNumberPropertyEditor(locale));
        binder.registerCustomEditor(Person.class, new PersonPropertyEditor(personService));
    }


    @RequestMapping(value = "/overtime", method = RequestMethod.GET)
    public String showPersonalOvertime() {

        Person signedInUser = sessionService.getSignedInUser();

        return "redirect:/web/overtime?person=" + signedInUser.getId();
    }


    @RequestMapping(value = "/overtime", method = RequestMethod.GET, params = PersonConstants.PERSON_ATTRIBUTE)
    public String showOvertime(
        @RequestParam(value = PersonConstants.PERSON_ATTRIBUTE, required = true) Integer personId,
        @RequestParam(value = ControllerConstants.YEAR_ATTRIBUTE, required = false) Integer requestedYear, Model model)
        throws UnknownPersonException {

        Integer year = requestedYear == null ? DateMidnight.now().getYear() : requestedYear;
        Person person = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));
        Person signedInUser = sessionService.getSignedInUser();

        if (!sessionService.isSignedInUserAllowedToAccessPersonData(signedInUser, person)) {
            throw new AccessDeniedException(String.format(
                    "User '%s' has not the correct permissions to see overtime records of user '%s'",
                    signedInUser.getLoginName(), person.getLoginName()));
        }

        model.addAttribute("year", year);
        model.addAttribute("person", person);
        model.addAttribute("records", overtimeService.getOvertimeRecordsForPersonAndYear(person, year));

        model.addAttribute("overtimeTotal", overtimeService.getTotalOvertimeForPerson(person));
        model.addAttribute("overtimeLeft", overtimeService.getLeftOvertimeForPerson(person));

        return "overtime/overtime_list";
    }


    @RequestMapping(value = "/overtime/{id}", method = RequestMethod.GET)
    public String showOvertimeDetails(@PathVariable("id") Integer id, Model model) throws UnknownOvertimeException,
        AccessDeniedException {

        Overtime overtime = overtimeService.getOvertimeById(id).orElseThrow(() -> new UnknownOvertimeException(id));

        Person person = overtime.getPerson();
        Person signedInUser = sessionService.getSignedInUser();

        if (!sessionService.isSignedInUserAllowedToAccessPersonData(signedInUser, person)) {
            throw new AccessDeniedException(String.format(
                    "User '%s' has not the correct permissions to see overtime records of user '%s'",
                    signedInUser.getLoginName(), person.getLoginName()));
        }

        model.addAttribute("record", overtime);
        model.addAttribute("comments", overtimeService.getCommentsForOvertime(overtime));

        model.addAttribute("overtimeTotal", overtimeService.getTotalOvertimeForPerson(person));
        model.addAttribute("overtimeLeft", overtimeService.getLeftOvertimeForPerson(person));

        return "overtime/overtime_details";
    }


    @RequestMapping(value = "/overtime/new", method = RequestMethod.GET)
    public String recordOvertime(
        @RequestParam(value = PersonConstants.PERSON_ATTRIBUTE, required = false) Integer personId, Model model)
        throws UnknownPersonException {

        Person signedInUser = sessionService.getSignedInUser();
        Person person;

        if (personId != null) {
            person = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));
        } else {
            person = signedInUser;
        }

        if (!signedInUser.equals(person) && !signedInUser.hasRole(Role.OFFICE)) {
            throw new AccessDeniedException(String.format(
                    "User '%s' has not the correct permissions to record overtime for user '%s'",
                    signedInUser.getLoginName(), person.getLoginName()));
        }

        model.addAttribute("overtime", new OvertimeForm(person));

        return "overtime/overtime_form";
    }


    @RequestMapping(value = "/overtime", method = RequestMethod.POST)
    public String recordOvertime(@ModelAttribute("overtime") OvertimeForm overtimeForm, Errors errors, Model model,
        RedirectAttributes redirectAttributes) {

        Person signedInUser = sessionService.getSignedInUser();
        Person person = overtimeForm.getPerson();

        if (!signedInUser.equals(person) && !signedInUser.hasRole(Role.OFFICE)) {
            throw new AccessDeniedException(String.format(
                    "User '%s' has not the correct permissions to record overtime for user '%s'",
                    signedInUser.getLoginName(), person.getLoginName()));
        }

        validator.validate(overtimeForm, errors);

        if (errors.hasErrors()) {
            model.addAttribute("overtime", overtimeForm);

            return "overtime/overtime_form";
        }

        Overtime recordedOvertime = overtimeService.record(overtimeForm.generateOvertime(),
                Optional.ofNullable(overtimeForm.getComment()), sessionService.getSignedInUser());

        redirectAttributes.addFlashAttribute("overtimeRecord", OvertimeAction.CREATED.name());

        return "redirect:/web/overtime/" + recordedOvertime.getId();
    }


    @RequestMapping(value = "/overtime/{id}/edit", method = RequestMethod.GET)
    public String editOvertime(@PathVariable("id") Integer id, Model model) throws UnknownOvertimeException {

        Overtime overtime = overtimeService.getOvertimeById(id).orElseThrow(() -> new UnknownOvertimeException(id));

        Person signedInUser = sessionService.getSignedInUser();
        Person person = overtime.getPerson();

        if (!signedInUser.equals(person) && !signedInUser.hasRole(Role.OFFICE)) {
            throw new AccessDeniedException(String.format(
                    "User '%s' has not the correct permissions to edit overtime record of user '%s'",
                    signedInUser.getLoginName(), person.getLoginName()));
        }

        model.addAttribute("overtime", new OvertimeForm(overtime));

        return "overtime/overtime_form";
    }


    @RequestMapping(value = "/overtime/{id}", method = RequestMethod.POST)
    public String updateOvertime(@PathVariable("id") Integer id,
        @ModelAttribute("overtime") OvertimeForm overtimeForm, Errors errors, Model model,
        RedirectAttributes redirectAttributes) throws UnknownOvertimeException {

        Overtime overtime = overtimeService.getOvertimeById(id).orElseThrow(() -> new UnknownOvertimeException(id));

        Person signedInUser = sessionService.getSignedInUser();
        Person person = overtime.getPerson();

        if (!signedInUser.equals(person) && !signedInUser.hasRole(Role.OFFICE)) {
            throw new AccessDeniedException(String.format(
                    "User '%s' has not the correct permissions to edit overtime record of user '%s'",
                    signedInUser.getLoginName(), person.getLoginName()));
        }

        validator.validate(overtimeForm, errors);

        if (errors.hasErrors()) {
            model.addAttribute("overtime", overtimeForm);

            return "overtime/overtime_form";
        }

        overtimeForm.updateOvertime(overtime);

        overtimeService.record(overtime, Optional.ofNullable(overtimeForm.getComment()),
            sessionService.getSignedInUser());

        redirectAttributes.addFlashAttribute("overtimeRecord", OvertimeAction.EDITED.name());

        return "redirect:/web/overtime/" + id;
    }
}
