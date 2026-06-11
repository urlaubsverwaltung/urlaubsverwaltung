package org.synyx.urlaubsverwaltung.search;

import org.springframework.stereotype.Controller;

/**
 * Interface that View-{@linkplain Controller}s implement to opt into the global person search.
 *
 * <p>
 * Implementing this interface is the single opt-in: the {@link PersonSearchInterceptor} then enables the search box and
 * populates the suggestions for every page rendered by the controller. In return the controller must provide a
 * {@link PersonSuggestionUrlStrategy} describing where a suggestion links to on its pages.
 */
public interface HasPersonSearch {

    PersonSuggestionUrlStrategy personSuggestionUrlStrategy();

    PersonSearchUiFragmentSupplier personSearchUiFragmentSupplier();
}
