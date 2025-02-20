package org.synyx.urlaubsverwaltung.user;

import de.focus_shift.launchpad.api.HasLaunchpad;
import org.slf4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.List;
import java.util.Locale;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Comparator.comparing;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Controller
@RequestMapping("/web")
class UserSettingsViewController implements HasLaunchpad {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final PersonService personService;
    private final UserSettingsServiceImpl userSettingsService;
    private final SupportedLocaleService supportedLocaleService;
    private final MessageSource messageSource;
    private final UserSettingsDtoValidator userSettingsDtoValidator;

    UserSettingsViewController(PersonService personService, UserSettingsServiceImpl userSettingsService, SupportedLocaleService supportedLocaleService, MessageSource messageSource, UserSettingsDtoValidator userSettingsDtoValidator) {
        this.personService = personService;
        this.userSettingsService = userSettingsService;
        this.supportedLocaleService = supportedLocaleService;
        this.messageSource = messageSource;
        this.userSettingsDtoValidator = userSettingsDtoValidator;
    }

    @GetMapping("/person/{personId}/settings")
    String userSettings(@PathVariable("personId") Long personId, Model model, Locale locale) {

        final Person signedInUser = personService.getSignedInUser();
        if (!signedInUser.getId().equals(personId)) {
            throw new ResponseStatusException(NOT_FOUND);
        }

        final UserSettings userSettings = userSettingsService.getUserSettingsForPerson(signedInUser);
        model.addAttribute("userSettings", userSettingsToDto(userSettings));
        model.addAttribute("supportedLocales", getSupportedLocales());
        model.addAttribute("supportedThemes", getAvailableThemeDtos(locale));

        return "user/user-settings";
    }

    @PostMapping("/person/{personId}/settings")
    String updateUserSettings(@PathVariable("personId") Long personId, Model model, @ModelAttribute UserSettingsDto userSettingsDto,
                              Errors errors, Locale locale) {

        final Person signedInUser = personService.getSignedInUser();
        if (!signedInUser.getId().equals(personId)) {
            throw new ResponseStatusException(NOT_FOUND);
        }

        userSettingsDtoValidator.validate(userSettingsDto, errors);
        if (errors.hasErrors()) {
            model.addAttribute("userSettings", userSettingsDto);
            model.addAttribute("supportedThemes", getAvailableThemeDtos(locale));
            model.addAttribute("supportedLocales", getSupportedLocales());
            return "user/user-settings";
        }

        final Theme theme = themeNameToTheme(userSettingsDto.getTheme());
        final Locale userLocale = userSettingsDto.getLocale();
        userSettingsService.updateUserPreference(signedInUser, theme, userLocale);

        return String.format("redirect:/web/person/%s/settings", personId);
    }

    private LocaleDto toLocaleDto(Locale locale) {
        final boolean displayNameOverflow = SupportedLocale.GERMAN_AUSTRIA.getLocale().equals(locale);
        final String displayName = i18n(locale, "locale");
        return new LocaleDto(locale, displayName, displayNameOverflow);
    }

    private UserSettingsDto userSettingsToDto(UserSettings userSettings) {
        final UserSettingsDto userSettingsDto = new UserSettingsDto();
        userSettingsDto.setTheme(userSettings.theme().name());
        userSettings.locale().ifPresent(userSettingsDto::setLocale);

        return userSettingsDto;
    }

    private List<ThemeDto> getAvailableThemeDtos(Locale locale) {
        return List.of(
            themeToThemeDto(Theme.SYSTEM, locale),
            themeToThemeDto(Theme.LIGHT, locale),
            themeToThemeDto(Theme.DARK, locale)
        );
    }

    private Theme themeNameToTheme(String themeName) {
        try {
            return Theme.valueOf(themeName.toUpperCase());
        } catch (IllegalArgumentException e) {
            LOG.error("tried to map unknown name={} to Theme.", themeName, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "theme does not exist.");
        }
    }

    private ThemeDto themeToThemeDto(Theme theme, Locale locale) {
        final String label = i18n(locale, "user-settings.theme." + theme.name());

        final ThemeDto themeDto = new ThemeDto();
        themeDto.setValue(theme.name());
        themeDto.setLabel(label);

        return themeDto;
    }

    private List<LocaleDto> getSupportedLocales() {
        return supportedLocaleService.getSupportedLocales().stream()
            .map(this::toLocaleDto)
            .sorted(comparing(LocaleDto::getDisplayName))
            .toList();
    }

    private String i18n(Locale locale, String messageKey) {
        return messageSource.getMessage(messageKey, new Object[]{}, locale);
    }
}
