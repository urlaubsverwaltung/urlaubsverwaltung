package org.synyx.urlaubsverwaltung.settings;

import de.focus_shift.launchpad.api.HasLaunchpad;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosure;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.search.HasPersonSearch;
import org.synyx.urlaubsverwaltung.search.PersonSearchUiFragmentSupplier;
import org.synyx.urlaubsverwaltung.search.PersonSuggestionUrlStrategy;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;

import java.time.Clock;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.Year;
import java.util.List;

import static java.time.Month.DECEMBER;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;

@Controller
@RequestMapping("/web/settings/public-holidays")
public class SettingsPublicHolidayViewController implements HasLaunchpad, HasPersonSearch {

    private static final MonthDay CHRISTMAS_EVE = MonthDay.of(DECEMBER, 24);
    private static final MonthDay NEW_YEARS_EVE = MonthDay.of(DECEMBER, 31);

    private final SettingsService settingsService;
    private final SettingsPublicHolidayValidator settingsValidator;
    private final CompanyClosureService companyClosureService;
    private final Clock clock;
    private final PersonSuggestionUrlStrategy defaultPersonSuggestionUrlStrategy;
    private final PersonSearchUiFragmentSupplier personSearchUiFragmentSupplier;

    SettingsPublicHolidayViewController(
        SettingsService settingsService,
        SettingsPublicHolidayValidator settingsValidator,
        CompanyClosureService companyClosureService,
        Clock clock,
        PersonSuggestionUrlStrategy defaultPersonSuggestionUrlStrategy,
        PersonSearchUiFragmentSupplier personSearchUiFragmentSupplier
    ) {
        this.settingsService = settingsService;
        this.settingsValidator = settingsValidator;
        this.companyClosureService = companyClosureService;
        this.clock = clock;
        this.defaultPersonSuggestionUrlStrategy = defaultPersonSuggestionUrlStrategy;
        this.personSearchUiFragmentSupplier = personSearchUiFragmentSupplier;
    }

    @Override
    public PersonSuggestionUrlStrategy personSuggestionUrlStrategy() {
        return defaultPersonSuggestionUrlStrategy;
    }

    @Override
    public PersonSearchUiFragmentSupplier personSearchUiFragmentSupplier() {
        return personSearchUiFragmentSupplier;
    }

    @GetMapping
    @PreAuthorize(IS_OFFICE)
    public String settingsDetails(Model model) {

        final Settings settings = settingsService.getSettings();
        final SettingsPublicHolidayDto settingsDto = settingsToDto(settings);

        fillModel(model, settingsDto);

        return "settings/public-holidays/settings_public_holidays";
    }

    @PostMapping
    @PreAuthorize(IS_OFFICE)
    public String settingsSaved(
        @Valid @ModelAttribute("settings") SettingsPublicHolidayDto settingsDto, Errors errors,
        Model model, RedirectAttributes redirectAttributes
    ) {

        settingsValidator.validate(settingsDto, errors);

        if (errors.hasErrors()) {
            fillModel(model, settingsDto);
            model.addAttribute("errors", errors);
            return "settings/public-holidays/settings_public_holidays";
        }

        final Settings settings = settingsDtoToSettings(settingsDto);
        settingsService.save(settings);

        // the service ignores unchanged values
        final LocalDate today = LocalDate.now(clock);
        companyClosureService.changeCompanyClosure(CHRISTMAS_EVE, settingsDto.getWorkingDurationForChristmasEve().getInverse(), today);
        companyClosureService.changeCompanyClosure(NEW_YEARS_EVE, settingsDto.getWorkingDurationForNewYearsEve().getInverse(), today);

        redirectAttributes.addFlashAttribute("success", true);

        return "redirect:/web/settings/public-holidays";
    }

    private void fillModel(Model model, SettingsPublicHolidayDto settingsDto) {
        model.addAttribute("settings", settingsDto);
        model.addAttribute("federalStateTypes", FederalState.federalStatesTypesByCountry());
        model.addAttribute("dayLengthTypes", DayLength.values());
    }

    private SettingsPublicHolidayDto settingsToDto(Settings settings) {
        final SettingsPublicHolidayDto dto = new SettingsPublicHolidayDto();
        dto.setId(settings.getId());
        dto.setPublicHolidaysSettings(settings.getPublicHolidaysSettings());

        final int year = Year.now(clock).getValue();
        final List<CompanyClosure> closures = companyClosureService.getCompanyClosures(CHRISTMAS_EVE.atYear(year), NEW_YEARS_EVE.atYear(year));
        dto.setWorkingDurationForChristmasEve(workingDuration(closures, CHRISTMAS_EVE.atYear(year)));
        dto.setWorkingDurationForNewYearsEve(workingDuration(closures, NEW_YEARS_EVE.atYear(year)));
        return dto;
    }

    private static DayLength workingDuration(List<CompanyClosure> closures, LocalDate date) {
        return closures.stream()
            .filter(closure -> closure.date().equals(date))
            .findFirst()
            .map(closure -> closure.dayLength().getInverse())
            .orElse(DayLength.FULL);
    }

    private Settings settingsDtoToSettings(SettingsPublicHolidayDto settingsDto) {
        final Settings settings = settingsService.getSettings();
        settings.setId(settingsDto.getId());
        // the working durations live in the company closures, keep the (still read) entity values
        settings.getPublicHolidaysSettings().setFederalState(settingsDto.getPublicHolidaysSettings().getFederalState());
        return settings;
    }
}
