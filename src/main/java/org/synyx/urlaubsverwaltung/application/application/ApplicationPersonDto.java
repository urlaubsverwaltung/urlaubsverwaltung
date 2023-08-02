package org.synyx.urlaubsverwaltung.application.application;

public class ApplicationPersonDto {

    private final String name;
    private final String avatarUrl;
    private final Boolean isInactive;

    private final Long id;

    ApplicationPersonDto(String name, String avatarUrl, Boolean isInactive, Long id) {
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.id = id;
        this.isInactive = isInactive;
    }

    public String getName() {
        return name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public Boolean getIsInactive() {
        return isInactive;
    }

    public Long getId() {
        return id;
    }
}
