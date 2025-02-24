package org.synyx.urlaubsverwaltung.web;

import java.util.List;

record NavigationDto(
    List<NavigationItemDto> elements
) {
}
