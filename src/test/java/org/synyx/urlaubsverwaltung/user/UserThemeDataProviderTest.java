package org.synyx.urlaubsverwaltung.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserThemeDataProviderTest {

    private UserThemeDataProvider sut;

    @Mock
    private UserSettingsServiceImpl userSettingsService;

    @BeforeEach
    void setUp() {
        sut = new UserThemeDataProvider(userSettingsService);
    }

    @Test
    void addTheme() {
        when(userSettingsService.findThemeForUsername("batman")).thenReturn(Optional.of(Theme.LIGHT));

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("viewName");

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setLocalName(Locale.GERMAN.getLanguage());
        request.setUserPrincipal(anyPrincipal("batman"));

        sut.postHandle(request, null, null, modelAndView);

        assertThat(modelAndView.getModelMap()).containsEntry("theme", "light");
    }

    @Test
    void addSystemDefaultTheme() {
        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("viewName");

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setLocalName(Locale.GERMAN.getLanguage());

        sut.postHandle(request, null, null, modelAndView);
        assertThat(modelAndView.getModelMap()).containsEntry("theme", "system");
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
        verifyNoInteractions(userSettingsService);
    }

    private static Principal anyPrincipal(String username) {
        final UserDetails user = User.withUsername(username).password("secret").authorities(new SimpleGrantedAuthority("ROLE_USER")).build();
        return new UsernamePasswordAuthenticationToken(user, null);
    }
}
