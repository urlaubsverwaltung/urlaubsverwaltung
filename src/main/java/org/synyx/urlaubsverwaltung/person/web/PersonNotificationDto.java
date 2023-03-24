package org.synyx.urlaubsverwaltung.person.web;

public class PersonNotificationDto {

    private boolean visible;
    private boolean active;

    PersonNotificationDto() {
        // ok
    }

    PersonNotificationDto(boolean visible, boolean active) {
        this.visible = visible;
        this.active = active;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
