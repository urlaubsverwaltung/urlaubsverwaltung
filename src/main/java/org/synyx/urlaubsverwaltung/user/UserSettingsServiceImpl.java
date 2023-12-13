package org.synyx.urlaubsverwaltung.user;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.springframework.context.event.EventListener;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.LocaleResolver;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonDeletedEvent;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.slf4j.LoggerFactory.getLogger;

@Service
class UserSettingsServiceImpl implements UserSettingsService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final UserSettingsRepository userSettingsRepository;
    private final LocaleResolver localeResolver;
    private final PersonService personService;

    UserSettingsServiceImpl(UserSettingsRepository userSettingsRepository,
                            LocaleResolver localeResolver, PersonService personService) {
        this.userSettingsRepository = userSettingsRepository;
        this.localeResolver = localeResolver;
        this.personService = personService;
    }

    @EventListener
    void delete(PersonDeletedEvent event) {
        userSettingsRepository.deleteByPerson(event.person());
    }

    @EventListener
    void handleAuthenticationSuccess(AuthenticationSuccessEvent event) {
        final String userName = event.getAuthentication().getName();
        final Optional<Person> maybePerson = personService.getPersonByUsername(userName);

        getRequest()
            .map(ServletRequest::getLocale)
            .ifPresent(locale -> maybePerson.ifPresent(person -> updateLocaleBrowserSpecific(person, locale)));
        maybePerson.flatMap(this::getLocale).ifPresent(this::setLocale);
    }

    UserSettings getUserSettingsForPerson(Person person) {
        final UserSettingsEntity entity = findForPersonOrGetDefault(person);
        return toUserSettings(entity);
    }

    Optional<Theme> findThemeForUsername(String username) {
        return userSettingsRepository.findByPersonUsername(username).map(UserSettingsEntity::getTheme);
    }

    Optional<Locale> getLocale(Person person) {
        return userSettingsRepository.findByPerson(person).map(UserSettingsEntity::getLocale);
    }

    /**
     * Updates the user settings of the person with the given attributes.
     * <p>
     * Also update the browser specific locale based on the given locale
     * and if false with the locale from the request.
     *
     * @param person the person to update the {@link UserSettings} for.
     * @param theme  the {@link Theme} for the person.
     * @param locale the locale to set for the person. must be a {@link SupportedLocale} or {@code null} to use the user-agent as fallback.
     * @return the updated {@link UserSettings}
     */
    UserSettings updateUserPreference(Person person, Theme theme, @Nullable Locale locale) {
        final UserSettingsEntity entity = findForPersonOrGetDefault(person);
        entity.setPersonId(person.getId());
        entity.setTheme(theme);
        entity.setLocale(locale);

        final Locale localeFromRequest = locale == null ? getRequest().map(ServletRequest::getLocale).orElse(null) : null;
        entity.setLocaleBrowserSpecific(localeFromRequest);

        final UserSettingsEntity persistedEntity = userSettingsRepository.save(entity);
        setLocale(persistedEntity.getLocale());

        return toUserSettings(persistedEntity);
    }

    /**
     * Sets the browser specific locale from the request.
     * <p>
     * Only saves the browser specific locale if the saved 'locale' is null.
     * If the saved 'locale' is null, that means, that the localization is based on the browser,
     * and therefore we save it to use it in e-mail templates e.g.
     *
     * @param person                to save the browser specific locale
     * @param localeBrowserSpecific
     */
    void updateLocaleBrowserSpecific(Person person, Locale localeBrowserSpecific) {
        userSettingsRepository.findByPerson(person)
            .ifPresentOrElse(userSettingsEntity -> {
                if (userSettingsEntity.getLocale() == null) {
                    userSettingsEntity.setLocaleBrowserSpecific(localeBrowserSpecific);
                    userSettingsRepository.save(userSettingsEntity);
                }
            }, () -> {
                final UserSettingsEntity defaultUserSettingsEntity = defaultUserSettingsEntity(person);
                defaultUserSettingsEntity.setLocaleBrowserSpecific(localeBrowserSpecific);
                userSettingsRepository.save(defaultUserSettingsEntity);
            });
    }

    @Override
    public Map<Person, Locale> getEffectiveLocale(List<Person> persons) {
        final List<Long> personIds = persons.stream().map(Person::getId).collect(toList());
        final Map<Person, Locale> personLocale = userSettingsRepository.findByPersonIdIn(personIds).stream()
            .collect(toMap(
                UserSettingsEntity::getPerson,
                UserSettingsServiceImpl::getEffectiveLocale
            ));
        persons.forEach(person -> personLocale.computeIfAbsent(person, unused -> Locale.GERMAN));
        return personLocale;
    }

    private static Locale getEffectiveLocale(UserSettingsEntity userSettingsEntity) {
        return Optional.ofNullable(userSettingsEntity.getLocale())
            .or(() -> Optional.ofNullable(userSettingsEntity.getLocaleBrowserSpecific()))
            .orElse(Locale.GERMAN);
    }

    private UserSettingsEntity findForPersonOrGetDefault(Person person) {
        return userSettingsRepository.findById(person.getId()).orElseGet(() -> defaultUserSettingsEntity(person));
    }

    private UserSettingsEntity defaultUserSettingsEntity(Person person) {
        final UserSettingsEntity userSettingsEntity = new UserSettingsEntity();
        userSettingsEntity.setTheme(Theme.SYSTEM);
        userSettingsEntity.setPersonId(person.getId());

        LOG.debug("created (not persisted) default userSettingsEntity={}", userSettingsEntity);

        return userSettingsEntity;
    }

    private static UserSettings toUserSettings(UserSettingsEntity userSettingsEntity) {
        return new UserSettings(userSettingsEntity.getTheme(), userSettingsEntity.getLocale(), userSettingsEntity.getLocaleBrowserSpecific());
    }

    private void setLocale(Locale locale) {
        getRequest().ifPresent(request -> localeResolver.setLocale(request, null, locale));
    }

    private Optional<HttpServletRequest> getRequest() {
        HttpServletRequest request = null;

        final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            request = ((ServletRequestAttributes) requestAttributes).getRequest();
        }

        return Optional.ofNullable(request);
    }
}
