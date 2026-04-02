package org.synyx.urlaubsverwaltung.web;

import java.util.List;

record NavigationDto(
    List<NavigationItemDto> favorites,
    List<NavigationItemDto> basic,
    List<NavigationItemDto> company,
    List<NavigationItemDto> settings
) {
}
