package org.synyx.urlaubsverwaltung.sicknote;

import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.beans.PropertyEditorSupport;


/**
 * Convert {@link Person}'s id to {@link Person} object.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class PersonPropertyEditor extends PropertyEditorSupport {

    private PersonService personService;

    public PersonPropertyEditor(PersonService personService) {

        this.personService = personService;
    }

    @Override
    public void setAsText(String text) {

        Integer id = Integer.valueOf(text);

        Person person = personService.getPersonByID(id);

        if (person != null) {
            setValue(person);
        } else {
            setValue(null);
        }
    }
}
