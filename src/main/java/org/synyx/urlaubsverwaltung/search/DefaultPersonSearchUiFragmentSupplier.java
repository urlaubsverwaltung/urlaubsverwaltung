package org.synyx.urlaubsverwaltung.search;

class DefaultPersonSearchUiFragmentSupplier implements PersonSearchUiFragmentSupplier {

    @Override
    public String get() {
        return "fragments/person-search::person-search";
    }
}
