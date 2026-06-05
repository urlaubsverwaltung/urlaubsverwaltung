package org.synyx.urlaubsverwaltung.search;

import org.synyx.urlaubsverwaltung.person.Person;

class DefaultPersonSuggestionUrlStrategy implements PersonSuggestionUrlStrategy {

    @Override
    public String buildSuggestionMainLink(Person suggestion, SearchContext context) {
        return "/web/person/%s/overview".formatted(suggestion.getId());
    }
}
