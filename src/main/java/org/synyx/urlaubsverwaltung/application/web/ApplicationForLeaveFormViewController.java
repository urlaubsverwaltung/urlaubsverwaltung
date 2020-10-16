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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.account.AccountService;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.application.service.ApplicationInteractionService;
import org.synyx.urlaubsverwaltung.application.service.EditApplicationForLeaveNotAllowedException;
import org.synyx.urlaubsverwaltung.application.service.VacationTypeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;
import org.synyx.urlaubsverwaltung.person.web.PersonPropertyEditor;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.web.DecimalNumberPropertyEditor;
import org.synyx.urlaubsverwaltung.web.LocalDatePropertyEditor;
import org.synyx.urlaubsverwaltung.web.TimePropertyEditor;

import java.math.BigDecimal;
import java.sql.Time;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static java.lang.String.format;
import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Optional.ofNullable;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.web.ApplicationMapper.mapToApplication;
import static org.synyx.urlaubsverwaltung.application.web.ApplicationMapper.mapToApplicationForm;
import static org.synyx.urlaubsverwaltung.application.web.ApplicationMapper.merge;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;

/**
 * Controller to apply for leave.
 */
@Controller
@RequestMapping("/web")
public class ApplicationForLeaveFormViewController {

    private static final Logger LOG = getLogger(lookup().lookupClass());
    private static final String PERSONS_ATTRIBUTE = "persons";
    private static final String PERSON_ATTRIBUTE = "person";

    private final PersonService personService;
    private final AccountService accountService;
    private final VacationTypeService vacationTypeService;
    private final ApplicationInteractionService applicationInteractionService;
    private final ApplicationForLeaveFormValidator applicationForLeaveFormValidator;
    private final SettingsService settingsService;
    private final Clock clock;

    @Autowired
    public ApplicationForLeaveFormViewController(PersonService personService, AccountService accountService, VacationTypeService vacationTypeService,
                                                 ApplicationInteractionService applicationInteractionService, ApplicationForLeaveFormValidator applicationForLeaveFormValidator, SettingsService settingsService, Clock clock) {
        this.personService = personService;
        this.accountService = accountService;
        this.vacationTypeService = vacationTypeService;
        this.applicationInteractionService = applicationInteractionService;
        this.applicationForLeaveFormValidator = applicationForLeaveFormValidator;
        this.settingsService = settingsService;
        this.clock = clock;
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

        final Person signedInUser = personService.getSignedInUser();

        Person person;
        if (personId == null) {
            person = signedInUser;
        } else {
            person = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));
        }

        boolean isApplyingForOneSelf = person.equals(signedInUser);

        // only office may apply for leave on behalf of other users
        if (!isApplyingForOneSelf && !signedInUser.hasRole(OFFICE)) {
            throw new AccessDeniedException(format("User '%s' has not the correct permissions to apply for leave for user '%s'", signedInUser.getId(), person.getId()));
        }

        final Optional<Account> holidaysAccount = accountService.getHolidaysAccount(ZonedDateTime.now(clock).getYear(), person);
        if (holidaysAccount.isPresent()) {
            prepareApplicationForLeaveForm(person, new ApplicationForLeaveForm(), model);
        }

        model.addAttribute("noHolidaysAccount", holidaysAccount.isEmpty());

        return "application/app_form";
    }

    @PostMapping("/application")
    public String newApplication(@ModelAttribute("application") ApplicationForLeaveForm appForm, Errors errors,
                                 Model model, RedirectAttributes redirectAttributes) {
        LOG.info("POST new application received: {}", appForm);

        applicationForLeaveFormValidator.validate(appForm, errors);

        if (errors.hasErrors()) {
            prepareApplicationForLeaveForm(appForm.getPerson(), appForm, model);
            if (errors.hasGlobalErrors()) {
                model.addAttribute("errors", errors);
            }
            LOG.info("new application ({}) has errors: {}", appForm, errors);
            return "application/app_form";
        }

        final Application app = mapToApplication(appForm);
        final Person applier = personService.getSignedInUser();
        final Application savedApplicationForLeave = applicationInteractionService.apply(app, applier, ofNullable(appForm.getComment()));

        LOG.info("new application with success applied {}", savedApplicationForLeave);

        redirectAttributes.addFlashAttribute("applySuccess", true);

        return "redirect:/web/application/" + savedApplicationForLeave.getId();
    }

    @GetMapping("/application/{applicationId}/edit")
    public String editApplicationForm(@PathVariable("applicationId") Integer applicationId, Model model) throws UnknownApplicationForLeaveException {

        final Optional<Application> maybeApplication = applicationInteractionService.get(applicationId);
        if (maybeApplication.isEmpty()) {
            throw new UnknownApplicationForLeaveException(applicationId);
        }

        final Application application = maybeApplication.get();
        if (application.getStatus().compareTo(WAITING) != 0) {
            return "application/app_notwaiting";
        }

        final ApplicationForLeaveForm applicationForLeaveForm = mapToApplicationForm(application);
        final Person person = personService.getSignedInUser();

        final Optional<Account> holidaysAccount = accountService.getHolidaysAccount(Year.now(clock).getValue(), person);
        if (holidaysAccount.isPresent()) {
            prepareApplicationForLeaveForm(person, applicationForLeaveForm, model);
        }
        model.addAttribute("noHolidaysAccount", holidaysAccount.isEmpty());

        model.addAttribute("application", applicationForLeaveForm);

        return "application/app_form";
    }

    @PostMapping("/application/{applicationId}")
    public String sendEditApplicationForm(@PathVariable("applicationId") Integer applicationId,
                                          @ModelAttribute("application") ApplicationForLeaveForm appForm, Errors errors,
                                          Model model, RedirectAttributes redirectAttributes) throws UnknownApplicationForLeaveException {

        final Optional<Application> maybeApplication = applicationInteractionService.get(applicationId);
        if (maybeApplication.isEmpty()) {
            throw new UnknownApplicationForLeaveException(applicationId);
        }

        final Application application = maybeApplication.get();
        if (application.getStatus().compareTo(WAITING) != 0) {
            return "application/app_notwaiting";
        }

        appForm.setId(application.getId());
        applicationForLeaveFormValidator.validate(appForm, errors);

        if (errors.hasErrors()) {
            prepareApplicationForLeaveForm(appForm.getPerson(), appForm, model);
            if (errors.hasGlobalErrors()) {
                model.addAttribute("errors", errors);
            }
            LOG.info("edit application ({}) has errors: {}", appForm, errors);
            return "application/app_form";
        }

        final Application editedApplication = merge(application, appForm);
        final Application savedApplicationForLeave;
        final Person signedInUser = personService.getSignedInUser();
        try {
            savedApplicationForLeave = applicationInteractionService.edit(editedApplication, signedInUser, Optional.ofNullable(appForm.getComment()));
        } catch (EditApplicationForLeaveNotAllowedException e) {
            return "application/app_notwaiting";
        }

        LOG.info("Edited application with success applied {}", savedApplicationForLeave);

        redirectAttributes.addFlashAttribute("editSuccess", true);

        return "redirect:/web/application/" + savedApplicationForLeave.getId();
    }

    private void prepareApplicationForLeaveForm(Person person, ApplicationForLeaveForm appForm, Model model) {

        final List<Person> persons = personService.getActivePersons();
        model.addAttribute(PERSON_ATTRIBUTE, person);
        model.addAttribute(PERSONS_ATTRIBUTE, persons);

        final boolean overtimeActive = settingsService.getSettings().getWorkingTimeSettings().isOvertimeActive();
        model.addAttribute("overtimeActive", overtimeActive);

        List<VacationType> vacationTypes = vacationTypeService.getVacationTypes();
        if (!overtimeActive) {
            vacationTypes = vacationTypeService.getVacationTypesFilteredBy(VacationCategory.OVERTIME);
        }
        model.addAttribute("vacationTypes", vacationTypes);

        model.addAttribute("application", appForm);
    }
}
