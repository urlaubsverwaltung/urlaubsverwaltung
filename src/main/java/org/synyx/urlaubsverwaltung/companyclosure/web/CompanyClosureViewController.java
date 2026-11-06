package org.synyx.urlaubsverwaltung.companyclosure.web;

import de.focus_shift.launchpad.api.HasLaunchpad;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureEntry;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureService;
import org.synyx.urlaubsverwaltung.search.HasPersonSearch;
import org.synyx.urlaubsverwaltung.search.PersonSearchUiFragmentSupplier;
import org.synyx.urlaubsverwaltung.search.PersonSuggestionUrlStrategy;

import java.time.Clock;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.Year;
import java.time.format.DateTimeParseException;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_BOSS_OR_OFFICE;

/**
 * Controller to manage company closures ("Betriebsruhe") - yearly recurring days on which the company is closed
 * for everybody.
 */
@Controller
@RequestMapping("/web/company-closure")
class CompanyClosureViewController implements HasLaunchpad, HasPersonSearch {

    private static final String FORM_VIEW = "companyclosure/company_closure_form";
    private static final String REDIRECT_TO_LIST = "redirect:/web/company-closure";

    private final CompanyClosureService companyClosureService;
    private final CompanyClosureFormValidator validator;
    private final PersonSuggestionUrlStrategy defaultPersonSuggestionUrlStrategy;
    private final PersonSearchUiFragmentSupplier personSearchUiFragmentSupplier;
    private final Clock clock;

    CompanyClosureViewController(CompanyClosureService companyClosureService, CompanyClosureFormValidator validator,
                                 PersonSuggestionUrlStrategy defaultPersonSuggestionUrlStrategy,
                                 PersonSearchUiFragmentSupplier personSearchUiFragmentSupplier, Clock clock) {
        this.companyClosureService = companyClosureService;
        this.validator = validator;
        this.defaultPersonSuggestionUrlStrategy = defaultPersonSuggestionUrlStrategy;
        this.personSearchUiFragmentSupplier = personSearchUiFragmentSupplier;
        this.clock = clock;
    }

    @Override
    public PersonSuggestionUrlStrategy personSuggestionUrlStrategy() {
        return defaultPersonSuggestionUrlStrategy;
    }

    @Override
    public PersonSearchUiFragmentSupplier personSearchUiFragmentSupplier() {
        return personSearchUiFragmentSupplier;
    }

    @PreAuthorize(IS_BOSS_OR_OFFICE)
    @GetMapping
    public String showCompanyClosures(Model model) {

        final LocalDate today = LocalDate.now(clock);
        model.addAttribute("companyClosures", companyClosureService.getCompanyClosureEntries().stream()
            .map(entry -> new CompanyClosureListDto(toPath(entry.monthDay()), entry.monthDay(), entry.name(),
                entry.dayLengthAt(today), entry.versions()))
            .toList());

        return "companyclosure/company_closure_list";
    }

    @PreAuthorize(IS_BOSS_OR_OFFICE)
    @GetMapping("/new")
    public String newCompanyClosureForm(Model model) {

        final CompanyClosureForm form = new CompanyClosureForm();
        form.setDayLength(FULL);
        form.setValidFrom(LocalDate.now(clock));
        model.addAttribute("companyClosure", form);
        model.addAttribute("minValidFrom", minValidFrom());

        return FORM_VIEW;
    }

    @PreAuthorize(IS_BOSS_OR_OFFICE)
    @PostMapping
    public String createCompanyClosure(@ModelAttribute("companyClosure") CompanyClosureForm form, Errors errors,
                                       Model model, RedirectAttributes redirectAttributes) {

        validator.validateNew(form, errors);
        if (errors.hasErrors()) {
            model.addAttribute("minValidFrom", minValidFrom());
            return FORM_VIEW;
        }

        companyClosureService.createCompanyClosure(MonthDay.of(form.getMonth(), form.getDay()), form.getName(),
            form.getDayLength(), form.getValidFrom());
        redirectAttributes.addFlashAttribute("createdCompanyClosureName", form.getName());

        return REDIRECT_TO_LIST;
    }

    @PreAuthorize(IS_BOSS_OR_OFFICE)
    @GetMapping("/{monthDay}/edit")
    public String editCompanyClosureForm(@PathVariable("monthDay") String monthDayPath, Model model) {

        final CompanyClosureEntry entry = getEntry(monthDayPath);
        final LocalDate today = LocalDate.now(clock);

        final CompanyClosureForm form = new CompanyClosureForm();
        form.setName(entry.name());
        form.setDayLength(entry.dayLengthAt(today));
        form.setValidFrom(today);
        model.addAttribute("companyClosure", form);
        addEditModel(model, entry);

        return FORM_VIEW;
    }

    @PreAuthorize(IS_BOSS_OR_OFFICE)
    @PostMapping("/{monthDay}")
    public String updateCompanyClosure(@PathVariable("monthDay") String monthDayPath,
                                       @ModelAttribute("companyClosure") CompanyClosureForm form, Errors errors,
                                       Model model, RedirectAttributes redirectAttributes) {

        final CompanyClosureEntry entry = getEntry(monthDayPath);

        validator.validateEdit(entry, form, errors);
        if (errors.hasErrors()) {
            addEditModel(model, entry);
            return FORM_VIEW;
        }

        companyClosureService.changeCompanyClosure(entry.monthDay(), form.getDayLength(), form.getValidFrom());
        if (!entry.name().equals(form.getName())) {
            companyClosureService.renameCompanyClosure(entry.monthDay(), form.getName());
        }
        redirectAttributes.addFlashAttribute("updatedCompanyClosureName", form.getName());

        return REDIRECT_TO_LIST;
    }

    private void addEditModel(Model model, CompanyClosureEntry entry) {
        model.addAttribute("monthDay", entry.monthDay());
        model.addAttribute("monthDayPath", toPath(entry.monthDay()));
        model.addAttribute("companyClosureName", entry.name());
        model.addAttribute("minValidFrom", minValidFrom());
    }

    private LocalDate minValidFrom() {
        return Year.now(clock).atDay(1);
    }

    private CompanyClosureEntry getEntry(String monthDayPath) {
        final MonthDay monthDay;
        try {
            monthDay = MonthDay.parse("--" + monthDayPath);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(NOT_FOUND);
        }
        return companyClosureService.getCompanyClosureEntries().stream()
            .filter(entry -> entry.monthDay().equals(monthDay))
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
    }

    /**
     * @return the month day as used in urls, e.g. {@code 12-24}
     */
    private static String toPath(MonthDay monthDay) {
        // MonthDay#toString is "--12-24"
        return monthDay.toString().substring(2);
    }
}
