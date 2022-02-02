package org.synyx.urlaubsverwaltung.user;

class UserSettings {

    private final Theme theme;

    UserSettings(Theme theme) {
        this.theme = theme;
    }

    Theme theme() {
        return theme;
    }
}
