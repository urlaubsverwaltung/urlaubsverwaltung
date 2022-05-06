package org.synyx.urlaubsverwaltung.person.web;

import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.beans.PropertyEditorSupport;
import java.util.Optional;

import static org.springframework.util.StringUtils.hasText;

/**
 * Convert {@link Person}'s id to {@link Person} object.
 */
public class PersonPropertyEditor extends PropertyEditorSupport {

    private final PersonService personService;

    public PersonPropertyEditor(PersonService personService) {
        this.personService = personService;
    }

    @Override
    public String getAsText() {
        if (this.getValue() == null) {
            return "";
        }
        return ((Person) this.getValue()).getId().toString();
    }

    @Override
    public void setAsText(String text) {
        if (!hasText(text)) {
            return;
        }

        final Integer id = Integer.valueOf(text);
        final Optional<Person> person = personService.getPersonByID(id);
        if (person.isPresent()) {
            setValue(person.get());
        } else {
            setValue(null);
        }
    }
}
