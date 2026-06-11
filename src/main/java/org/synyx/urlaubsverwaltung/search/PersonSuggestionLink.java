package org.synyx.urlaubsverwaltung.search;

record PersonSuggestionLink(String href, String messageKey, Icon icon) {

    enum Icon {
        ACCOUNT,
    }
}
