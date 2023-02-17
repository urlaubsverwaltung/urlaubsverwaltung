package org.synyx.urlaubsverwaltung.web;

import java.util.List;

class NavigationDto {

    private final List<NavigationItemDto> elements;

    NavigationDto(List<NavigationItemDto> elements) {
        this.elements = elements;
    }

    public List<NavigationItemDto> getElements() {
        return elements;
    }
}
