package org.synyx.urlaubsverwaltung.web.application;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;

import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.synyx.urlaubsverwaltung.core.account.domain.Account;
import org.synyx.urlaubsverwaltung.core.account.service.AccountService;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationInteractionService;
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

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Controller to apply for leave.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Controller
@RequestMapping("/web")
public class ApplyForLeaveController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private PersonService personService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private ApplicationInteractionService applicationInteractionService;

    @Autowired
    private ApplicationValidator applicationValidator;

    @InitBinder
    public void initBinder(DataBinder binder, Locale locale) {

        binder.registerCustomEditor(DateMidnight.class, new DateMidnightPropertyEditor());
        binder.registerCustomEditor(BigDecimal.class, new DecimalNumberPropertyEditor(locale));
        binder.registerCustomEditor(Person.class, new PersonPropertyEditor(personService));
    }


    /**
     * Show form to apply for leave.
     *
     * @param  personId  of the person that applies for leave
     * @param  model  to be filled
     *
     * @return  form to apply for leave
     */
    @RequestMapping(value = "/application/new", method = RequestMethod.GET)
    public String newApplicationForm(
        @RequestParam(value = PersonConstants.PERSON_ATTRIBUTE, required = false) Integer personId, Model model)
        throws UnknownPersonException, AccessDeniedException {

        Person signedInUser = sessionService.getSignedInUser();

        Person person;

        if (personId == null) {
            person = signedInUser;
        } else {
            person = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));
        }

        boolean isApplyingForOneSelf = person.equals(signedInUser);

        // only office may apply for leave on behalf of other users
        if (!isApplyingForOneSelf && !signedInUser.hasRole(Role.OFFICE)) {
            throw new AccessDeniedException(String.format(
                    "User '%s' has not the correct permissions to apply for leave for user '%s'",
                    signedInUser.getLoginName(), person.getLoginName()));
        }

        Optional<Account> holidaysAccount = accountService.getHolidaysAccount(DateMidnight.now().getYear(), person);

        if (holidaysAccount.isPresent()) {
            prepareApplicationForLeaveForm(person, new ApplicationForLeaveForm(), model);
        }

        model.addAttribute("noHolidaysAccount", !holidaysAccount.isPresent());

        return "application/app_form";
    }


    private void prepareApplicationForLeaveForm(Person person, ApplicationForLeaveForm appForm, Model model) {

        List<Person> persons = personService.getActivePersons();

        model.addAttribute(PersonConstants.PERSON_ATTRIBUTE, person);
        model.addAttribute(PersonConstants.PERSONS_ATTRIBUTE, persons);
        model.addAttribute("application", appForm);
        model.addAttribute("vacationTypes", VacationType.values());
    }


    @RequestMapping(value = "/application", method = RequestMethod.POST)
    public String newApplication(@ModelAttribute("application") ApplicationForLeaveForm appForm, Errors errors,
        Model model, RedirectAttributes redirectAttributes) throws UnknownPersonException {

        Person applier = sessionService.getSignedInUser();

        applicationValidator.validate(appForm, errors);

        if (errors.hasErrors()) {
            prepareApplicationForLeaveForm(appForm.getPerson(), appForm, model);

            if (errors.hasGlobalErrors()) {
                model.addAttribute(ControllerConstants.ERRORS_ATTRIBUTE, errors);
            }

            return "application/app_form";
        }

        Application application = appForm.generateApplicationForLeave();

        Application savedApplicationForLeave = applicationInteractionService.apply(application, applier,
                Optional.ofNullable(appForm.getComment()));

        redirectAttributes.addFlashAttribute("applySuccess", true);

        return "redirect:/web/application/" + savedApplicationForLeave.getId();
    }
}
