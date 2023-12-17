package org.synyx.urlaubsverwaltung.account;

import de.focus_shift.launchpad.api.HasLaunchpad;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeDto;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeViewModelService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;
import org.synyx.urlaubsverwaltung.web.DecimalNumberPropertyEditor;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;

/**
 * Controller to manage {@link Account}s of {@link org.synyx.urlaubsverwaltung.person.Person}s.
 */
@Controller
@RequestMapping("/web")
public class AccountViewController implements HasLaunchpad {

    private final PersonService personService;
    private final AccountService accountService;
    private final AccountInteractionService accountInteractionService;
    private final VacationTypeViewModelService vacationTypeViewModelService;
    private final AccountFormValidator validator;
    private final Clock clock;

    @Autowired
    AccountViewController(PersonService personService, AccountService accountService,
                          AccountInteractionService accountInteractionService,
                          VacationTypeViewModelService vacationTypeViewModelService,
                          AccountFormValidator validator, Clock clock) {
        this.personService = personService;
        this.accountService = accountService;
        this.accountInteractionService = accountInteractionService;
        this.vacationTypeViewModelService = vacationTypeViewModelService;
        this.validator = validator;
        this.clock = clock;
    }

    @InitBinder
    public void initBinder(DataBinder binder, Locale locale) {
        binder.registerCustomEditor(BigDecimal.class, new DecimalNumberPropertyEditor(locale));
    }

    @PreAuthorize(IS_OFFICE)
    @GetMapping("/person/{personId}/account")
    public String editAccount(@PathVariable("personId") Long personId,
                              @RequestParam(value = "year", required = false) Integer year, Model model)
        throws UnknownPersonException {

        final Person person = personService.getPersonByID(personId)
            .orElseThrow(() -> new UnknownPersonException(personId));

        final int currentYear = Year.now(clock).getValue();
        final int selectedYear = year != null ? year : currentYear;

        final Optional<Account> maybeHolidaysAccount = accountService.getHolidaysAccount(selectedYear, person);
        final AccountForm accountForm = maybeHolidaysAccount.map(AccountForm::new).orElseGet(() -> getAccountFormDraft(selectedYear, person));

        model.addAttribute("account", accountForm);
        model.addAttribute("person", person);
        model.addAttribute("selectedYear", selectedYear);
        model.addAttribute("currentYear", currentYear);

        addVacationTypeColorsToModel(model);

        return "account/account_form";
    }

    @PreAuthorize(IS_OFFICE)
    @PostMapping("/person/{personId}/account")
    public String updateAccount(@PathVariable("personId") Long personId,
                                @ModelAttribute("account") AccountForm accountForm, Errors errors, Model model,
                                RedirectAttributes redirectAttributes) throws UnknownPersonException {

        final Person person = personService.getPersonByID(personId)
            .orElseThrow(() -> new UnknownPersonException(personId));

        validator.validate(accountForm, errors);

        if (errors.hasErrors()) {
            model.addAttribute("person", person);
            model.addAttribute("selectedYear", accountForm.getHolidaysAccountYear());
            model.addAttribute("currentYear", Year.now(clock).getValue());

            addVacationTypeColorsToModel(model);

            return "account/account_form";
        }

        final LocalDate validFrom = accountForm.getHolidaysAccountValidFrom();
        final LocalDate validTo = accountForm.getHolidaysAccountValidTo();
        final Boolean doRemainingVacationDaysExpireLocally = accountForm.isOverrideVacationDaysExpire()
            ? accountForm.getDoRemainingVacationDaysExpireLocally()
            : null;
        final LocalDate expiryDateLocally = accountForm.getExpiryDateLocally();
        final BigDecimal annualVacationDays = accountForm.getAnnualVacationDays();
        final BigDecimal actualVacationDays = accountForm.getActualVacationDays();
        final BigDecimal remainingVacationDays = accountForm.getRemainingVacationDays();
        final BigDecimal remainingVacationDaysNotExpiring = accountForm.getRemainingVacationDaysNotExpiring();
        final String comment = accountForm.getComment();

        // check if there is an existing account
        final Optional<Account> account = accountService.getHolidaysAccount(validFrom.getYear(), person);

        if (account.isPresent()) {
            accountInteractionService.editHolidaysAccount(account.get(), validFrom, validTo, doRemainingVacationDaysExpireLocally, expiryDateLocally, annualVacationDays,
                actualVacationDays, remainingVacationDays, remainingVacationDaysNotExpiring, comment);
        } else {
            accountInteractionService.updateOrCreateHolidaysAccount(person, validFrom, validTo, doRemainingVacationDaysExpireLocally,
                expiryDateLocally, annualVacationDays, actualVacationDays, remainingVacationDays, remainingVacationDaysNotExpiring, comment);
        }

        redirectAttributes.addFlashAttribute("updateSuccess", true);

        return "redirect:/web/person/" + personId;
    }

    private AccountForm getAccountFormDraft(int year, Person person) {

        final AccountDraft accountDraft = accountService.createHolidaysAccountDraft(year, person);
        final AccountForm accountForm = new AccountForm(accountDraft.getYear().getValue());

        accountForm.setAnnualVacationDays(accountDraft.getAnnualVacationDays());
        accountForm.setActualVacationDays(accountDraft.getAnnualVacationDays());

        accountForm.setHolidaysAccountValidFrom(accountDraft.getValidFrom());
        accountForm.setHolidaysAccountValidTo(accountDraft.getValidTo());
        accountForm.setExpiryDateLocally(accountDraft.getExpiryDateLocally());
        accountForm.setExpiryDateGlobally(accountDraft.getExpiryDateGlobally());
        accountForm.setRemainingVacationDaysNotExpiring(accountDraft.getRemainingVacationDaysNotExpiring());

        accountForm.setDoRemainingVacationDaysExpireLocally(accountDraft.doRemainingVacationDaysExpireLocally());
        accountForm.setDoRemainingVacationDaysExpireGlobally(accountDraft.doRemainingVacationDaysExpireGlobally());
        accountForm.setOverrideVacationDaysExpire(accountDraft.doRemainingVacationDaysExpireLocally() != null);

        return accountForm;
    }

    private void addVacationTypeColorsToModel(Model model) {
        final List<VacationTypeDto> vacationTypeColors = vacationTypeViewModelService.getVacationTypeColors();
        model.addAttribute("vacationTypeColors", vacationTypeColors);
    }
}
