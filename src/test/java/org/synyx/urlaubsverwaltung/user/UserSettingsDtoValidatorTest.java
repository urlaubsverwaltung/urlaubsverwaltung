package org.synyx.urlaubsverwaltung.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.Errors;

import java.util.Locale;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class UserSettingsDtoValidatorTest {

    private UserSettingsDtoValidator sut;

    @Mock
    private Errors errors;

    @BeforeEach
    void setUp() {
        sut = new UserSettingsDtoValidator();
    }

    @Test
    void ensureValidIfLocaleIsNotProvided() {
        final UserSettingsDto userSettingsDto = new UserSettingsDto();
        userSettingsDto.setTheme(Theme.SYSTEM.name());
        sut.validate(userSettingsDto, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureThrowsErrorIfLocaleIsNotSupported() {
        final UserSettingsDto userSettingsDto = new UserSettingsDto();
        userSettingsDto.setTheme(Theme.SYSTEM.name());
        userSettingsDto.setLocale(Locale.ITALIAN);
        sut.validate(userSettingsDto, errors);
        verify(errors).reject("Locale is not available");
    }

    @Test
    void ensureThrowsErrorIfThemeIsNotProvided() {
        final UserSettingsDto userSettingsDto = new UserSettingsDto();
        userSettingsDto.setLocale(Locale.GERMAN);
        sut.validate(userSettingsDto, errors);
        verify(errors).reject("Theme is not available");
    }

    @Test
    void ensureThrowsErrorIfThemeIsNotSupported() {
        final UserSettingsDto userSettingsDto = new UserSettingsDto();
        userSettingsDto.setLocale(Locale.GERMAN);
        userSettingsDto.setTheme("someTheme");
        sut.validate(userSettingsDto, errors);
        verify(errors).reject("Theme is not available");
    }
}
