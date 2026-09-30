package org.synyx.urlaubsverwaltung.vacationcertificate;

import de.focus_shift.launchpad.api.HasLaunchpad;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.account.AccountService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataService;
import org.synyx.urlaubsverwaltung.web.DecimalNumberPropertyEditor;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Year;
import java.util.Locale;
import java.util.Optional;

import static java.math.BigDecimal.ZERO;
import static java.util.Comparator.naturalOrder;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;

/**
 * Shows the vacation certificate (§ 6 Abs. 2 BUrlG) of a person, ready to be printed.
 */
@Controller
@RequestMapping("/web")
public class VacationCertificateViewController implements HasLaunchpad {

    private static final String VIEW = "vacationcertificate/vacation_certificate";

    private final PersonService personService;
    private final PersonBasedataService personBasedataService;
    private final AccountService accountService;
    private final VacationCertificateService vacationCertificateService;
    private final VacationCertificateFormValidator validator;
    private final Clock clock;

    VacationCertificateViewController(
        PersonService personService,
        PersonBasedataService personBasedataService,
        AccountService accountService,
        VacationCertificateService vacationCertificateService,
        VacationCertificateFormValidator validator,
        Clock clock
    ) {
        this.personService = personService;
        this.personBasedataService = personBasedataService;
        this.accountService = accountService;
        this.vacationCertificateService = vacationCertificateService;
        this.validator = validator;
        this.clock = clock;
    }

    @InitBinder
    public void initBinder(DataBinder binder, Locale locale) {
        binder.registerCustomEditor(BigDecimal.class, new DecimalNumberPropertyEditor(locale));
    }

    @PreAuthorize(IS_OFFICE)
    @GetMapping("/person/{personId}/vacation-certificate")
    public String vacationCertificate(@PathVariable("personId") Long personId,
                                      @ModelAttribute("certificateForm") VacationCertificateFormDto form, Errors errors,
                                      Model model) throws UnknownPersonException {

        final Person person = personService.getPersonByID(personId)
            .orElseThrow(() -> new UnknownPersonException(personId));

        final int currentYear = Year.now(clock).getValue();
        if (form.getYear() == null) {
            form.setYear(currentYear);
        }
        final int selectedYear = form.getYear();

        model.addAttribute("person", person);
        model.addAttribute("personnelNumber", personnelNumber(person));
        model.addAttribute("selectedYear", selectedYear);
        model.addAttribute("currentYear", currentYear);

        final Optional<Account> maybeAccount = accountService.getHolidaysAccount(selectedYear, person);
        if (maybeAccount.isEmpty()) {
            model.addAttribute("noAccount", true);
            return VIEW;
        }

        final Account account = maybeAccount.get();
        applyDefaults(form, errors, person, account);

        validator.validate(form, errors);
        if (!errors.hasErrors()) {
            model.addAttribute("certificate", vacationCertificateService.getVacationCertificate(account, form.getEmploymentTo()));
        }

        return VIEW;
    }

    /**
     * Prefills inputs that were not submitted. An input that was submitted but could not be bound keeps its
     * binding error instead of being replaced silently.
     */
    private void applyDefaults(VacationCertificateFormDto form, Errors errors, Person person, Account account) {
        if (form.getEmploymentFrom() == null && !errors.hasFieldErrors("employmentFrom")) {
            form.setEmploymentFrom(accountService.getHolidaysAccountsByPerson(person).stream()
                .map(Account::getValidFrom)
                .min(naturalOrder())
                .orElse(account.getValidFrom()));
        }
        if (form.getEmploymentTo() == null && !errors.hasFieldErrors("employmentTo")) {
            form.setEmploymentTo(account.getValidTo());
        }
        if (form.getCompensatedDays() == null && !errors.hasFieldErrors("compensatedDays")) {
            form.setCompensatedDays(ZERO);
        }
    }

    private String personnelNumber(Person person) {
        return personBasedataService.getBasedataByPersonId(person.getId())
            .map(PersonBasedata::personnelNumber)
            .filter(StringUtils::hasText)
            .orElse(null);
    }
}
