package org.synyx.urlaubsverwaltung.web;


import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;

import java.beans.PropertyEditorSupport;
import java.util.Optional;


/**
 * Convert {@link Person}'s id to {@link Person} object.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class PersonPropertyEditor extends PropertyEditorSupport {

    private final PersonService personService;

    public PersonPropertyEditor(PersonService personService) {

        this.personService = personService;
    }

    @Override
    public void setAsText(String text) {

        Integer id = Integer.valueOf(text);

        Optional<Person> person = personService.getPersonByID(id);

        if (person.isPresent()) {
            setValue(person.get());
        } else {
            setValue(null);
        }
    }
}
