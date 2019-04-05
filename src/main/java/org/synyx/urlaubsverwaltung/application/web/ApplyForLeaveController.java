package org.synyx.urlaubsverwaltung.application.web;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.synyx.urlaubsverwaltung.account.domain.Account;
import org.synyx.urlaubsverwaltung.account.service.AccountService;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.application.service.ApplicationInteractionService;
import org.synyx.urlaubsverwaltung.application.service.VacationTypeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;
import org.synyx.urlaubsverwaltung.security.SessionService;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;
import org.synyx.urlaubsverwaltung.web.DecimalNumberPropertyEditor;
import org.synyx.urlaubsverwaltung.web.LocalDatePropertyEditor;
import org.synyx.urlaubsverwaltung.web.PersonPropertyEditor;
import org.synyx.urlaubsverwaltung.web.TimePropertyEditor;

import java.math.BigDecimal;
import java.sql.Time;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static java.time.ZoneOffset.UTC;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Controller to apply for leave.
 */
@Controller
@RequestMapping("/web")
public class ApplyForLeaveController {

    private static final Logger LOG = getLogger(lookup().lookupClass());
    private static final String PERSONS_ATTRIBUTE = "persons";
    private static final String PERSON_ATTRIBUTE = "person";

    private final SessionService sessionService;
    private final PersonService personService;
    private final AccountService accountService;
    private final VacationTypeService vacationTypeService;
    private final ApplicationInteractionService applicationInteractionService;
    private final ApplicationValidator applicationValidator;
    private final SettingsService settingsService;

    @Autowired
    public ApplyForLeaveController(SessionService sessionService, PersonService personService, AccountService accountService, VacationTypeService vacationTypeService,
                                   ApplicationInteractionService applicationInteractionService, ApplicationValidator applicationValidator, SettingsService settingsService) {
        this.sessionService = sessionService;
        this.personService = personService;
        this.accountService = accountService;
        this.vacationTypeService = vacationTypeService;
        this.applicationInteractionService = applicationInteractionService;
        this.applicationValidator = applicationValidator;
        this.settingsService = settingsService;
    }

    @InitBinder
    public void initBinder(DataBinder binder, Locale locale) {

        binder.registerCustomEditor(LocalDate.class, new LocalDatePropertyEditor());
        binder.registerCustomEditor(Time.class, new TimePropertyEditor());
        binder.registerCustomEditor(BigDecimal.class, new DecimalNumberPropertyEditor(locale));
        binder.registerCustomEditor(Person.class, new PersonPropertyEditor(personService));
    }


    @GetMapping("/application/new")
    public String newApplicationForm(
        @RequestParam(value = PERSON_ATTRIBUTE, required = false) Integer personId, Model model)
        throws UnknownPersonException {

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

        Optional<Account> holidaysAccount = accountService.getHolidaysAccount(ZonedDateTime.now(UTC).getYear(), person);

        if (holidaysAccount.isPresent()) {
            prepareApplicationForLeaveForm(person, new ApplicationForLeaveForm(), model);
        }

        model.addAttribute("noHolidaysAccount", !holidaysAccount.isPresent());

        return "application/app_form";
    }


    private void prepareApplicationForLeaveForm(Person person, ApplicationForLeaveForm appForm, Model model) {

        List<Person> persons = personService.getActivePersons();
        model.addAttribute(PERSON_ATTRIBUTE, person);
        model.addAttribute(PERSONS_ATTRIBUTE, persons);

        boolean overtimeActive = settingsService.getSettings().getWorkingTimeSettings().isOvertimeActive();
        model.addAttribute("overtimeActive", overtimeActive);

        List<VacationType> vacationTypes = vacationTypeService.getVacationTypes();
        if(!overtimeActive) {
            vacationTypes = vacationTypeService.getVacationTypesFilteredBy(VacationCategory.OVERTIME);
        }
        model.addAttribute("vacationTypes", vacationTypes);

        model.addAttribute("application", appForm);
    }


    @PostMapping("/application")
    public String newApplication(@ModelAttribute("application") ApplicationForLeaveForm appForm, Errors errors,
        Model model, RedirectAttributes redirectAttributes) {

        LOG.info("POST new application received: {}", appForm);

        Person applier = sessionService.getSignedInUser();

        applicationValidator.validate(appForm, errors);

        if (errors.hasErrors()) {
            prepareApplicationForLeaveForm(appForm.getPerson(), appForm, model);
            if (errors.hasGlobalErrors()) {
                model.addAttribute(ControllerConstants.ERRORS_ATTRIBUTE, errors);
            }

            LOG.info("new application ({}) has errors: {}", appForm, errors);

            return "application/app_form";
        }

        Application application = appForm.generateApplicationForLeave();

        Application savedApplicationForLeave = applicationInteractionService.apply(application, applier,
                Optional.ofNullable(appForm.getComment()));

        LOG.info("new application with success applied {}", savedApplicationForLeave);

        redirectAttributes.addFlashAttribute("applySuccess", true);

        return "redirect:/web/application/" + savedApplicationForLeave.getId();
    }
}
