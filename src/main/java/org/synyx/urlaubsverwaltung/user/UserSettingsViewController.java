package org.synyx.urlaubsverwaltung.user;

import org.slf4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Controller
@RequestMapping("/web")
class UserSettingsViewController {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final PersonService personService;
    private final UserSettingsService userSettingsService;
    private final MessageSource messageSource;

    UserSettingsViewController(PersonService personService, UserSettingsService userSettingsService, MessageSource messageSource) {
        this.personService = personService;
        this.userSettingsService = userSettingsService;
        this.messageSource = messageSource;
    }

    @GetMapping("/person/{personId}/settings")
    String userSettings(@PathVariable("personId") Integer personId, Model model, Locale locale) {

        final Person signedInUser = personService.getSignedInUser();
        if (!signedInUser.getId().equals(personId)) {
            throw new ResponseStatusException(NOT_FOUND);
        }

        final UserSettings userSettings = userSettingsService.getUserSettingsForPerson(signedInUser);
        final UserSettingsDto userSettingsDto = userSettingsToDto(userSettings, locale);

        model.addAttribute("userSettings", userSettingsDto);

        return "thymeleaf/user/user-settings";
    }

    @PostMapping("/person/{personId}/settings")
    String updateUserSettings(@PathVariable("personId") Integer personId, @ModelAttribute UserSettingsDto userSettingsDto, Model model, Locale locale) {

        final Person signedInUser = personService.getSignedInUser();
        if (!signedInUser.getId().equals(personId)) {
            throw new ResponseStatusException(NOT_FOUND);
        }

        final Theme theme = themeNameToTheme(userSettingsDto.getSelectedTheme());

        userSettingsService.updateUserThemePreference(signedInUser, theme);

        return String.format("redirect:/web/person/%s/settings", personId);
    }

    private UserSettingsDto userSettingsToDto(UserSettings userSettings, Locale locale) {
        final UserSettingsDto userSettingsDto = new UserSettingsDto();

        final List<ThemeDto> availableThemeDtos = List.of(
            themeToThemeDto(Theme.SYSTEM, locale),
            themeToThemeDto(Theme.LIGHT, locale),
            themeToThemeDto(Theme.DARK, locale)
        );

        userSettingsDto.setThemes(availableThemeDtos);
        userSettingsDto.setSelectedTheme(userSettings.theme().name());

        return userSettingsDto;
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
        final String label = messageSource.getMessage("user-settings.theme." + theme.name(), new Object[]{}, locale);

        final ThemeDto themeDto = new ThemeDto();
        themeDto.setValue(theme.name());
        themeDto.setLabel(label);

        return themeDto;
    }
}
