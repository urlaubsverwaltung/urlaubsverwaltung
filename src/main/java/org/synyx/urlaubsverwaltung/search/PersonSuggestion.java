package org.synyx.urlaubsverwaltung.search;

import java.util.List;

record PersonSuggestion(
    long id,
    String name,
    String initials,
    String email,
    String href,
    List<PersonSuggestionLink> links
) {
}
