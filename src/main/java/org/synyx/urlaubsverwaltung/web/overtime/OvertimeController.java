package org.synyx.urlaubsverwaltung.web.overtime;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;

import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.synyx.urlaubsverwaltung.core.overtime.OvertimeInteractionService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.security.SessionService;
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
    private OvertimeInteractionService overtimeService;

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
    public String showOvertime() {

        return "overtime/overtime_list";
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

        overtimeService.record(overtimeForm.generateOvertime(), Optional.ofNullable(overtimeForm.getComment()),
            sessionService.getSignedInUser());

        redirectAttributes.addFlashAttribute("overtimeRecord", "CREATED");

        return "redirect:/web/overtime";
    }
}
