package org.synyx.urlaubsverwaltung.application.application;

public class ApplicationPersonDto {

    private final String name;
    private final String avatarUrl;

    ApplicationPersonDto(String name, String avatarUrl) {
        this.name = name;
        this.avatarUrl = avatarUrl;
    }

    public String getName() {
        return name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }
}
