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

import org.synyx.urlaubsverwaltung.core.overtime.Overtime;
import org.synyx.urlaubsverwaltung.core.overtime.OvertimeAction;
import org.synyx.urlaubsverwaltung.core.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.security.SessionService;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;
import org.synyx.urlaubsverwaltung.web.DateMidnightPropertyEditor;
import org.synyx.urlaubsverwaltung.web.DecimalNumberPropertyEditor;
import org.synyx.urlaubsverwaltung.web.PersonPropertyEditor;

import java.math.BigDecimal;

import java.util.Locale;
import java.util.Optional;


/**
 * Manage overtime of persons.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Controller
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
    public String showOvertime(
        @RequestParam(value = ControllerConstants.YEAR_ATTRIBUTE, required = false) Integer requestedYear,
        Model model) {

        Integer year = requestedYear == null ? DateMidnight.now().getYear() : requestedYear;

        Person signedInUser = sessionService.getSignedInUser();

        model.addAttribute("year", year);
        model.addAttribute("person", signedInUser);
        model.addAttribute("records", overtimeService.getOvertimeRecordsForPersonAndYear(signedInUser, year));
        model.addAttribute("overtimeTotal", overtimeService.getTotalOvertimeForPerson(signedInUser));

        // TODO: Subtract hours of applications for leave because of having overtime due from total overtime
        model.addAttribute("overtimeLeft", BigDecimal.ZERO);

        return "overtime/overtime_list";
    }


    @RequestMapping(value = "/overtime/{id}", method = RequestMethod.GET)
    public String showOvertimeDetails(@PathVariable("id") Integer id, Model model) throws UnknownOvertimeException,
        AccessDeniedException {

        Overtime overtime = overtimeService.getOvertimeById(id).orElseThrow(() -> new UnknownOvertimeException(id));
        Person signedInUser = sessionService.getSignedInUser();

        if (!overtime.getPerson().equals(signedInUser)) {
            throw new AccessDeniedException("User " + signedInUser.getLoginName()
                + " has not the correct permissions to access overtime record of user "
                + overtime.getPerson().getLoginName());
        }

        model.addAttribute("record", overtime);
        model.addAttribute("comments", overtimeService.getCommentsForOvertime(overtime));
        model.addAttribute("overtimeTotal", overtimeService.getTotalOvertimeForPerson(signedInUser));

        return "overtime/overtime_details";
    }


    @RequestMapping(value = "/overtime/new", method = RequestMethod.GET)
    public String recordOvertime(Model model) {

        model.addAttribute("overtime", new OvertimeForm(sessionService.getSignedInUser()));

        return "overtime/overtime_form";
    }


    @RequestMapping(value = "/overtime", method = RequestMethod.POST)
    public String recordOvertime(@ModelAttribute("overtime") OvertimeForm overtimeForm, Errors errors, Model model,
        RedirectAttributes redirectAttributes) {

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

        model.addAttribute("overtime", new OvertimeForm(overtime));

        return "overtime/overtime_form";
    }


    @RequestMapping(value = "/overtime/{id}", method = RequestMethod.PUT)
    public String updateOvertime(@PathVariable("id") Integer id,
        @ModelAttribute("overtime") OvertimeForm overtimeForm, Errors errors, Model model,
        RedirectAttributes redirectAttributes) throws UnknownOvertimeException {

        Overtime overtime = overtimeService.getOvertimeById(id).orElseThrow(() -> new UnknownOvertimeException(id));

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
