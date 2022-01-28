package org.synyx.urlaubsverwaltung.user;

import org.slf4j.Logger;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@ControllerAdvice
public class UserThemeControllerAdvice {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final PersonService personService;
    private final UserSettingsService userSettingsService;

    UserThemeControllerAdvice(PersonService personService, UserSettingsService userSettingsService) {
        this.personService = personService;
        this.userSettingsService = userSettingsService;
    }

    @ModelAttribute
    public void addUserThemeAttribute(Model model) {

        final Theme theme = getTheme().orElse(Theme.SYSTEM);
        final String themeValueLowerCase = theme.name().toLowerCase();

        model.addAttribute("theme", themeValueLowerCase);
    }

    private Optional<Theme> getTheme() {
        try {
            final Person signedInUser = personService.getSignedInUser();
            return Optional.of(userSettingsService.getUserSettingsForPerson(signedInUser).theme());
        } catch (IllegalStateException e) {
            LOG.info("could not get theme for unknown user (login page called for instance).", e);
            return Optional.empty();
        }
    }
}
