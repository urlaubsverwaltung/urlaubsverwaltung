package org.synyx.urlaubsverwaltung.companyclosure.web;

import de.focus_shift.launchpad.api.HasLaunchpad;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureEntry;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureService;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureVersion;
import org.synyx.urlaubsverwaltung.search.HasPersonSearch;
import org.synyx.urlaubsverwaltung.search.PersonSearchUiFragmentSupplier;
import org.synyx.urlaubsverwaltung.search.PersonSuggestionUrlStrategy;
import org.synyx.urlaubsverwaltung.settings.SupportedLanguages;

import java.time.Clock;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.Year;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsLast;
import static java.util.Comparator.reverseOrder;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.validation.BindingResult.MODEL_KEY_PREFIX;
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
    private static final String COMPANY_CLOSURE = "companyClosure";
    private static final String MIN_VALID_FROM = "minValidFrom";

    private final CompanyClosureService companyClosureService;
    private final CompanyClosureFormValidator validator;
    private final PersonSuggestionUrlStrategy defaultPersonSuggestionUrlStrategy;
    private final PersonSearchUiFragmentSupplier personSearchUiFragmentSupplier;
    private final MessageSource messageSource;
    private final Clock clock;

    CompanyClosureViewController(CompanyClosureService companyClosureService, CompanyClosureFormValidator validator,
                                 PersonSuggestionUrlStrategy defaultPersonSuggestionUrlStrategy,
                                 PersonSearchUiFragmentSupplier personSearchUiFragmentSupplier,
                                 MessageSource messageSource, Clock clock) {
        this.companyClosureService = companyClosureService;
        this.validator = validator;
        this.defaultPersonSuggestionUrlStrategy = defaultPersonSuggestionUrlStrategy;
        this.personSearchUiFragmentSupplier = personSearchUiFragmentSupplier;
        this.messageSource = messageSource;
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
    public String showCompanyClosures(Model model, Locale locale) {

        final LocalDate today = LocalDate.now(clock);
        final List<CompanyClosureListDto> companyClosures = companyClosureService.getCompanyClosureEntries().stream()
            .map(entry -> new CompanyClosureListDto(toPath(entry.monthDay()), entry.monthDay(), entry.name(locale),
                entry.dayLengthAt(today), latestFirst(entry.versions())))
            .toList();
        model.addAttribute("companyClosures", companyClosures);

        return "companyclosure/company_closure_list";
    }

    @PreAuthorize(IS_BOSS_OR_OFFICE)
    @GetMapping("/new")
    public String newCompanyClosureForm(Model model, Locale locale) {

        // a rejected form comes back with its errors by the redirect
        if (!model.containsAttribute(COMPANY_CLOSURE)) {
            final List<CompanyClosureNameForm> emptyNames = Arrays.stream(SupportedLanguages.values())
                .map(language -> new CompanyClosureNameForm(language.getLocale(), ""))
                .toList();
            final CompanyClosureForm form = new CompanyClosureForm();
            form.setNames(emptyNames);
            form.setDayLength(FULL);
            form.setValidFrom(LocalDate.now(clock));
            model.addAttribute(COMPANY_CLOSURE, form);
        }
        addFormModel(model, locale);

        return FORM_VIEW;
    }

    @PreAuthorize(IS_BOSS_OR_OFFICE)
    @PostMapping("/new")
    public String createCompanyClosure(@ModelAttribute(COMPANY_CLOSURE) CompanyClosureForm form, BindingResult errors,
                                       RedirectAttributes redirectAttributes, Locale locale) {

        validator.validateNew(form, errors);
        if (errors.hasErrors()) {
            keepFormWithErrors(form, errors, redirectAttributes);
            return "redirect:/web/company-closure/new";
        }

        companyClosureService.createCompanyClosure(MonthDay.of(form.getMonth(), form.getDay()), form.getNameByLocale(),
            form.getDayLength(), form.getValidFrom());
        redirectAttributes.addFlashAttribute("createdCompanyClosureName", form.nameFor(locale));

        return REDIRECT_TO_LIST;
    }

    @PreAuthorize(IS_BOSS_OR_OFFICE)
    @GetMapping("/{monthDay}/edit")
    public String editCompanyClosureForm(@PathVariable("monthDay") String monthDayPath, Model model, Locale locale) {

        final CompanyClosureEntry entry = getEntry(monthDayPath);

        // a rejected form comes back with its errors by the redirect
        if (!model.containsAttribute(COMPANY_CLOSURE)) {
            final LocalDate today = LocalDate.now(clock);
            final CompanyClosureForm form = new CompanyClosureForm();
            form.setNames(namesOf(entry));
            form.setDayLength(entry.dayLengthAt(today));
            form.setValidFrom(today);
            model.addAttribute(COMPANY_CLOSURE, form);
        }
        addEditModel(model, entry, locale);

        return FORM_VIEW;
    }

    @PreAuthorize(IS_BOSS_OR_OFFICE)
    @PostMapping("/{monthDay}/edit")
    public String updateCompanyClosure(@PathVariable("monthDay") String monthDayPath,
                                       @ModelAttribute(COMPANY_CLOSURE) CompanyClosureForm form, BindingResult errors,
                                       RedirectAttributes redirectAttributes, Locale locale) {

        final CompanyClosureEntry entry = getEntry(monthDayPath);

        validator.validateEdit(entry, form, errors);
        if (errors.hasErrors()) {
            keepFormWithErrors(form, errors, redirectAttributes);
            return "redirect:/web/company-closure/%s/edit".formatted(toPath(entry.monthDay()));
        }

        companyClosureService.changeCompanyClosure(entry.monthDay(), form.getDayLength(), form.getValidFrom());
        // unchanged names keep the translations of a closure provided by the application
        if (!form.getNameByLocale().equals(nameByLocaleOf(entry))) {
            companyClosureService.renameCompanyClosure(entry.monthDay(), form.getNameByLocale());
        }
        redirectAttributes.addFlashAttribute("updatedCompanyClosureName", form.nameFor(locale));

        return REDIRECT_TO_LIST;
    }

    /**
     * The form is shown again by a redirect to its own url, so reloading the page neither submits the form again
     * nor leads to the url the form was sent to. Form and errors are handed over as flash attributes.
     */
    private static void keepFormWithErrors(CompanyClosureForm form, BindingResult errors, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute(COMPANY_CLOSURE, form);
        redirectAttributes.addFlashAttribute(MODEL_KEY_PREFIX + COMPANY_CLOSURE, errors);
    }

    private void addEditModel(Model model, CompanyClosureEntry entry, Locale locale) {
        model.addAttribute("monthDay", entry.monthDay());
        model.addAttribute("monthDayPath", toPath(entry.monthDay()));
        model.addAttribute("companyClosureName", entry.name(locale));
        addFormModel(model, locale);
    }

    private void addFormModel(Model model, Locale locale) {
        model.addAttribute(MIN_VALID_FROM, minValidFrom());
        model.addAttribute("namePlaceholders", namePlaceholders(locale));
    }

    /**
     * The hint is addressed to the person maintaining the names, so it is in their language. The name it shows is
     * the one readers of the field's language see, so it is in the field's language, e.g. "Leer: „Company closure“".
     *
     * @return per supported language the hint which name is shown if the name is left empty
     */
    private Map<Locale, String> namePlaceholders(Locale locale) {
        return Arrays.stream(SupportedLanguages.values())
            .map(SupportedLanguages::getLocale)
            .collect(toMap(identity(), language -> namePlaceholder(language, locale)));
    }

    private String namePlaceholder(Locale language, Locale locale) {
        final String defaultName = messageSource.getMessage("absences.overview.company-closure", new Object[]{}, language);
        return messageSource.getMessage("company-closure.data.names.placeholder", new Object[]{defaultName}, locale);
    }

    /**
     * @return the versions sorted by {@code validFrom}, the latest first and the initial version without
     * {@code validFrom} last
     */
    private static List<CompanyClosureVersion> latestFirst(List<CompanyClosureVersion> versions) {
        return versions.stream()
            .sorted(comparing(CompanyClosureVersion::validFrom, nullsLast(reverseOrder())))
            .toList();
    }

    /**
     * @return one name per supported language: the names given by a user, the translations of a closure provided
     * by the application that has not been renamed
     */
    private static List<CompanyClosureNameForm> namesOf(CompanyClosureEntry entry) {
        final Map<Locale, String> nameByLocale = nameByLocaleOf(entry);
        return Arrays.stream(SupportedLanguages.values())
            .map(language -> new CompanyClosureNameForm(language.getLocale(), nameByLocale.getOrDefault(language.getLocale(), "")))
            .toList();
    }

    private static Map<Locale, String> nameByLocaleOf(CompanyClosureEntry entry) {
        if (!entry.names().isEmpty()) {
            return entry.names();
        }
        return Arrays.stream(SupportedLanguages.values())
            .map(SupportedLanguages::getLocale)
            .collect(toMap(identity(), entry::name));
    }

    private LocalDate minValidFrom() {
        return Year.now(clock).atDay(1);
    }

    private CompanyClosureEntry getEntry(String monthDayPath) {
        final MonthDay monthDay;
        try {
            monthDay = MonthDay.parse("--" + monthDayPath);
        } catch (DateTimeParseException _) {
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
