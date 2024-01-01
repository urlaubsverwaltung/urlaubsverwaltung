package org.synyx.urlaubsverwaltung.web;

import java.util.List;

class NavigationDto {

    private final boolean visible;
    private final List<NavigationItemDto> elements;

    NavigationDto(List<NavigationItemDto> elements) {
        this(true, elements);
    }

    NavigationDto(boolean visible, List<NavigationItemDto> elements) {
        this.visible = visible;
        this.elements = elements;
    }

    public boolean isVisible() {
        return visible;
    }

    public List<NavigationItemDto> getElements() {
        return elements;
    }
}
