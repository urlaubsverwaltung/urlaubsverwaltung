package org.synyx.urlaubsverwaltung.web;

import java.util.Objects;

class NavigationItemDto {

    private final String id;
    private final String href;
    private final String messageKey;
    private final String iconName;
    private final String dataTestId;

    NavigationItemDto(String id, String href, String messageKey, String iconName) {
        this(id, href, messageKey, iconName, null);
    }

    NavigationItemDto(String id, String href, String messageKey, String iconName, String dataTestId) {
        this.id = id;
        this.href = href;
        this.messageKey = messageKey;
        this.iconName = iconName;
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

    public String getDataTestId() {
        return dataTestId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NavigationItemDto that = (NavigationItemDto) o;
        return id.equals(that.id)
            && href.equals(that.href)
            && messageKey.equals(that.messageKey)
            && iconName.equals(that.iconName)
            && Objects.equals(dataTestId, that.dataTestId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, href, messageKey, iconName, dataTestId);
    }

    @Override
    public String toString() {
        return "NavigationItemDto{" +
            "id='" + id + '\'' +
            ", href='" + href + '\'' +
            ", messageKey='" + messageKey + '\'' +
            ", iconName='" + iconName + '\'' +
            ", dataTestId='" + dataTestId + '\'' +
            '}';
    }
}
