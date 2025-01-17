package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.user.Theme;

public enum ThemeDTO {
    SYSTEM,
    DARK,
    LIGHT;

    public Theme toTheme() {
        return Theme.valueOf(this.name());
    }
}
