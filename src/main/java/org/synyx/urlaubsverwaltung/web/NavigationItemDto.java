package org.synyx.urlaubsverwaltung.web;

import java.util.Objects;

class NavigationItemDto {

    private final String id;
    private final String href;
    private final String messageKey;
    private final String iconName;
    private final boolean active;
    private final String dataTestId;

    NavigationItemDto(String id, String href, String messageKey, String iconName, boolean active) {
        this(id, href, messageKey, iconName, active, null);
    }

    NavigationItemDto(String id, String href, String messageKey, String iconName, boolean active, String dataTestId) {
        this.id = id;
        this.href = href;
        this.messageKey = messageKey;
        this.iconName = iconName;
        this.active = active;
        this.dataTestId = dataTestId;
    }

    public String getId() {
        return id;
    }

    public String getHref() {
        return href;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public String getIconName() {
        return iconName;
    }

    public boolean isActive() {
        return active;
    }

    public String getDataTestId() {
        return dataTestId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NavigationItemDto that = (NavigationItemDto) o;
        return active == that.active
            && Objects.equals(id, that.id)
            && Objects.equals(href, that.href)
            && Objects.equals(messageKey, that.messageKey)
            && Objects.equals(iconName, that.iconName)
            && Objects.equals(dataTestId, that.dataTestId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, href, messageKey, iconName, active, dataTestId);
    }

    @Override
    public String toString() {
        return "NavigationItemDto{" +
            "id='" + id + '\'' +
            ", href='" + href + '\'' +
            ", messageKey='" + messageKey + '\'' +
            ", iconName='" + iconName + '\'' +
            ", active='" + active + '\'' +
            ", dataTestId='" + dataTestId + '\'' +
            '}';
    }
}
