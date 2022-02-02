package org.synyx.urlaubsverwaltung.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.ModelAndView;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserThemeDataProviderTest {

    private UserThemeDataProvider sut;

    @Mock
    private PersonService personService;
    @Mock
    private UserSettingsService userSettingsService;

    @BeforeEach
    void setUp() {
        sut = new UserThemeDataProvider(personService, userSettingsService);
    }

    @Test
    void addTheme() {
        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final UserSettings userSettings = new UserSettings(Theme.LIGHT);
        when(userSettingsService.getUserSettingsForPerson(person)).thenReturn(userSettings);

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("viewName");

        sut.postHandle(null, null, null, modelAndView);
        assertThat(modelAndView.getModelMap().get("theme")).isEqualTo("light");
    }

    @Test
    void addSystemDefaultTheme() {
        when(personService.getSignedInUser()).thenThrow(new IllegalStateException());

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("viewName");

        sut.postHandle(null, null, null, modelAndView);
        assertThat(modelAndView.getModelMap().get("theme")).isEqualTo("system");
    }

    @Test
    void addSystemDefaultThemeForLogin() {
        when(personService.getSignedInUser()).thenThrow(new IllegalStateException());

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");

        sut.postHandle(null, null, null, modelAndView);
        assertThat(modelAndView.getModelMap().get("theme")).isEqualTo("system");
    }

    @ParameterizedTest
    @ValueSource(strings = {"forward:", "redirect:"})
    @NullSource
    void doNotAddTheme(String viewName) {

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName(viewName);

        sut.postHandle(null, null, null, modelAndView);
        assertThat(modelAndView.getModelMap().get("theme")).isNull();
    }

    @Test
    void doNotAddThemeIfModelAndViewIsNull() {
        sut.postHandle(null, null, null, null);
        verifyNoInteractions(personService, userSettingsService);
    }
}
