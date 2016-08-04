package org.synyx.urlaubsverwaltung.web.workingtime;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;

import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.synyx.urlaubsverwaltung.core.period.WeekDay;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.settings.FederalState;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkingTime;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.security.SecurityRules;
import org.synyx.urlaubsverwaltung.web.DateMidnightPropertyEditor;
import org.synyx.urlaubsverwaltung.web.DecimalNumberPropertyEditor;
import org.synyx.urlaubsverwaltung.web.person.PersonConstants;
import org.synyx.urlaubsverwaltung.web.person.UnknownPersonException;

import java.math.BigDecimal;

import java.util.Locale;
import java.util.Optional;


/**
 * Controller to manage {@link org.synyx.urlaubsverwaltung.core.workingtime.WorkingTime}s of
 * {@link org.synyx.urlaubsverwaltung.core.person.Person}s.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Controller
@RequestMapping("/web")
public class WorkingTimeController {

    @Autowired
    private PersonService personService;

    @Autowired
    private WorkingTimeService workingTimeService;

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private WorkingTimeValidator validator;

    @InitBinder
    public void initBinder(DataBinder binder, Locale locale) {

        binder.registerCustomEditor(DateMidnight.class, new DateMidnightPropertyEditor());
        binder.registerCustomEditor(BigDecimal.class, new DecimalNumberPropertyEditor(locale));
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/staff/{personId}/workingtime", method = RequestMethod.GET)
    public String editWorkingTime(@PathVariable("personId") Integer personId, Model model)
        throws UnknownPersonException {

        Person person = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));
        Optional<WorkingTime> optionalWorkingTime = workingTimeService.getCurrentOne(person);

        if (optionalWorkingTime.isPresent()) {
            model.addAttribute("workingTime", new WorkingTimeForm(optionalWorkingTime.get()));
        } else {
            model.addAttribute("workingTime", new WorkingTimeForm());
        }

        fillModel(model, person);

        return "workingtime/workingtime_form";
    }


    private void fillModel(Model model, Person person) {

        model.addAttribute(PersonConstants.PERSON_ATTRIBUTE, person);
        model.addAttribute("workingTimes", workingTimeService.getByPerson(person));
        model.addAttribute("weekDays", WeekDay.values());
        model.addAttribute("federalStateTypes", FederalState.values());
        model.addAttribute("defaultFederalState",
            settingsService.getSettings().getWorkingTimeSettings().getFederalState());
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/staff/{personId}/workingtime", method = RequestMethod.POST)
    public String updateWorkingTime(@PathVariable("personId") Integer personId,
                                    @ModelAttribute("workingTime") WorkingTimeForm workingTimeForm,
                                    Model model,
                                    Errors errors,
                                    RedirectAttributes redirectAttributes) throws UnknownPersonException {

        Person person = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));

        validator.validate(workingTimeForm, errors);

        if (errors.hasErrors()) {
            fillModel(model, person);

            return "workingtime/workingtime_form";
        }

        workingTimeService.touch(workingTimeForm.getWorkingDays(),
            Optional.ofNullable(workingTimeForm.getFederalState()), workingTimeForm.getValidFrom(), person);

        redirectAttributes.addFlashAttribute("updateSuccess", true);

        return "redirect:/web/staff/" + personId;
    }
}
