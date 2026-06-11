package org.synyx.urlaubsverwaltung.search;

import jakarta.servlet.http.HttpServletRequest;
import org.synyx.urlaubsverwaltung.person.Person;

/**
 * Builds the main link of a person search suggestion for a concrete feature page.
 *
 * <p>
 * Each {@linkplain HasPersonSearch} controller provides its own strategy. The current {@linkplain HttpServletRequest}
 * is passed so the link can be derived from the requested path and its query parameters (e.g. preserving {@code year}).
 */
@FunctionalInterface
public interface PersonSuggestionUrlStrategy {

    String buildSuggestionMainLink(Person suggestion, SearchContext context);
}
