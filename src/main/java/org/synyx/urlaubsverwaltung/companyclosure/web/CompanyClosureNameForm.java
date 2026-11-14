package org.synyx.urlaubsverwaltung.companyclosure.web;

import java.util.Locale;
import java.util.Objects;

/**
 * Name of a company closure in one of the supported languages, blank if the language has no name.
 */
public class CompanyClosureNameForm {

    private Locale locale;
    private String name;

    public CompanyClosureNameForm() {
        // used by data binding
    }

    public CompanyClosureNameForm(Locale locale, String name) {
        this.locale = locale;
        setName(name);
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.strip();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CompanyClosureNameForm that = (CompanyClosureNameForm) o;
        return Objects.equals(locale, that.locale) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(locale, name);
    }

    @Override
    public String toString() {
        return "CompanyClosureNameForm{" +
            "locale=" + locale +
            ", name='" + name + '\'' +
            '}';
    }
}
