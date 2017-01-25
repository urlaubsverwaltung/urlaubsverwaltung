package org.synyx.urlaubsverwaltung.web.account;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.synyx.urlaubsverwaltung.core.account.domain.Account;
import org.synyx.urlaubsverwaltung.core.account.service.AccountInteractionService;
import org.synyx.urlaubsverwaltung.core.account.service.AccountService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.security.SecurityRules;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;
import org.synyx.urlaubsverwaltung.web.DateMidnightPropertyEditor;
import org.synyx.urlaubsverwaltung.web.DecimalNumberPropertyEditor;
import org.synyx.urlaubsverwaltung.web.person.UnknownPersonException;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Optional;

/**
 * Controller to manage {@link org.synyx.urlaubsverwaltung.core.account.domain.Account}s of {@link org.synyx.urlaubsverwaltung.core.person.Person}s.
 *
 * @author Aljona Murygina - murygina@synyx.de
 */
@Controller
@RequestMapping("/web")
public class AccountController {

    @Autowired
    private PersonService personService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountInteractionService accountInteractionService;

    @Autowired
    private AccountValidator validator;

    @InitBinder
    public void initBinder(DataBinder binder, Locale locale) {

        binder.registerCustomEditor(DateMidnight.class, new DateMidnightPropertyEditor());
        binder.registerCustomEditor(BigDecimal.class, new DecimalNumberPropertyEditor(locale));
    }

    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/staff/{personId}/account", method = RequestMethod.GET)
    public String editAccount(@PathVariable("personId") Integer personId,
        @RequestParam(value = ControllerConstants.YEAR_ATTRIBUTE, required = false) Integer year, Model model)
        throws UnknownPersonException {

        Person person = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));

        int yearOfHolidaysAccount = year != null ? year : DateMidnight.now().getYear();
        AccountForm accountForm = new AccountForm(yearOfHolidaysAccount, accountService.getHolidaysAccount(
            yearOfHolidaysAccount, person));

        model.addAttribute("person", person);
        model.addAttribute("account", accountForm);
        model.addAttribute("year", yearOfHolidaysAccount);

        return "account/account_form";
    }

    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/staff/{personId}/account", method = RequestMethod.POST)
    public String updateAccount(@PathVariable("personId") Integer personId,
        @ModelAttribute("account") AccountForm accountForm, Model model, Errors errors,
        RedirectAttributes redirectAttributes) throws UnknownPersonException {

        Person person = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));

        validator.validate(accountForm, errors);

        if (errors.hasErrors()) {
            model.addAttribute("person", person);
            model.addAttribute("year", accountForm.getHolidaysAccountYear());

            return "account/account_form";
        }

        DateMidnight validFrom = accountForm.getHolidaysAccountValidFrom();
        DateMidnight validTo = accountForm.getHolidaysAccountValidTo();

        BigDecimal annualVacationDays = accountForm.getAnnualVacationDays();
        BigDecimal actualVacationDays = accountForm.getActualVacationDays();
        BigDecimal remainingVacationDays = accountForm.getRemainingVacationDays();
        BigDecimal remainingVacationDaysNotExpiring = accountForm.getRemainingVacationDaysNotExpiring();
        String comment = accountForm.getComment();

        // check if there is an existing account
        Optional<Account> account = accountService.getHolidaysAccount(validFrom.getYear(), person);

        if (account.isPresent()) {
            accountInteractionService.editHolidaysAccount(account.get(), validFrom, validTo, annualVacationDays,
                actualVacationDays, remainingVacationDays, remainingVacationDaysNotExpiring, comment);
        } else {
            accountInteractionService.createHolidaysAccount(person, validFrom, validTo, annualVacationDays,
                actualVacationDays, remainingVacationDays, remainingVacationDaysNotExpiring, comment);
        }

        redirectAttributes.addFlashAttribute("updateSuccess", true);

        return "redirect:/web/staff/" + personId;
    }
}
