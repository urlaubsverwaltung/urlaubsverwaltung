package org.synyx.urlaubsverwaltung.user;

import java.util.List;
import java.util.Locale;

public class UserSettingsDto {

    private List<ThemeDto> themes;

    private String selectedTheme;

    private Locale locale;

    public List<ThemeDto> getThemes() {
        return themes;
    }

    public UserSettingsDto setThemes(List<ThemeDto> themes) {
        this.themes = themes;
        return this;
    }

    public String getSelectedTheme() {
        return selectedTheme;
    }

    public UserSettingsDto setSelectedTheme(String selectedTheme) {
        this.selectedTheme = selectedTheme;
        return this;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }
}
