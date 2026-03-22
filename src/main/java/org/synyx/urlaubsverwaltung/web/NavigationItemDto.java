package org.synyx.urlaubsverwaltung.web;

import java.util.Objects;

class NavigationItemDto {

    private final String id;
    private final String href;
    private final String messageKey;
    private final String dataTestId;

    NavigationItemDto(String id, String href, String messageKey) {
        this(id, href, messageKey, null);
    }

    NavigationItemDto(String id, String href, String messageKey, String dataTestId) {
        this.id = id;
        this.href = href;
        this.messageKey = messageKey;
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

    public String getDataTestId() {
        return dataTestId;
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
            && Objects.equals(dataTestId, that.dataTestId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, href, messageKey, dataTestId);
    }

    @Override
    public String toString() {
        return "NavigationItemDto{" +
            "id='" + id + '\'' +
            ", href='" + href + '\'' +
            ", messageKey='" + messageKey + '\'' +
            ", dataTestId='" + dataTestId + '\'' +
            '}';
    }
}
