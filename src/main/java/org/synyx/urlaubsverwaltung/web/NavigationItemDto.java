package org.synyx.urlaubsverwaltung.web;

import java.util.List;
import java.util.Objects;

class NavigationItemDto {

    private final String id;
    private final String href;
    private final String messageKey;
    private final String iconName;
    private final String dataTestId;
    private final boolean active;
    private final List<NavigationItemDto> subItems;

    NavigationItemDto(String id, String href, String messageKey, String iconName) {
        this(id, href, messageKey, iconName, false, null);
    }

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
        this.subItems = List.of();
    }

    private NavigationItemDto(NavigationItemDto item, List<NavigationItemDto> subItems) {
        this.id = item.id;
        this.href = item.href;
        this.messageKey = item.messageKey;
        this.iconName = item.iconName;
        this.active = item.active;
        this.dataTestId = item.dataTestId;
        this.subItems = subItems;
    }

    public NavigationItemDto withSubItems(List<NavigationItemDto> subItems) {
        return new NavigationItemDto(this, subItems);
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

    public List<NavigationItemDto> getSubItems() {
        return subItems;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NavigationItemDto that = (NavigationItemDto) o;
        return id.equals(that.id)
            && href.equals(that.href)
            && messageKey.equals(that.messageKey)
            && iconName.equals(that.iconName)
            && Objects.equals(active, that.active)
            && Objects.equals(dataTestId, that.dataTestId)
            && Objects.equals(subItems, that.subItems);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, href, messageKey, iconName, active, dataTestId, subItems);
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
            ", subItems='" + subItems + '\'' +
            '}';
    }
}
