package org.synyx.urlaubsverwaltung.application.application;

public class ApplicationPersonDto {

    private final String name;
    private final String avatarUrl;

    private final Long id;

    ApplicationPersonDto(String name, String avatarUrl, Long id) {
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public Long getId() {
        return id;
    }
}
