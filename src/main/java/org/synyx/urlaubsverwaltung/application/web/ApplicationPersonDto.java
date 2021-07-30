package org.synyx.urlaubsverwaltung.application.web;

public class ApplicationPersonDto {

    private final String name;
    private final String avatarUrl;

    public ApplicationPersonDto(String name, String avatarUrl) {
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
