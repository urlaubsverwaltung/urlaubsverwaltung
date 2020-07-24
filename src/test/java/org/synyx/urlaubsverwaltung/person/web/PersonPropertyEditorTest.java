package org.synyx.urlaubsverwaltung.person.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonPropertyEditorTest {

    private PersonPropertyEditor sut;

    @Mock
    private PersonService personService;

    @BeforeEach
    void setUp() {

        sut = new PersonPropertyEditor(personService);
    }

    @Test
    void ensureGetAsTextReturnsEmptyStringForNullValue() {

        sut.setValue(null);

        assertThat(sut.getAsText()).isEmpty();
    }

    @Test
    void ensureGetAsTextReturnsIdOfPerson() {

        final int personId = 10;
        sut.setValue(personWithId(personId));

        assertThat(sut.getAsText()).isEqualTo(Integer.toString(personId));
    }

    @Test
    void ensureSetAsTextSetsValueToPersonForExistingPersonId() {

        final int personId = 5;
        final Person person = personWithId(personId);
        when(personService.getPersonByID(personId)).thenReturn(Optional.of(person));

        sut.setAsText(Integer.toString(personId));

        assertThat(sut.getValue()).isEqualTo(person);
    }

    @Test
    void ensureSetAsTextSetsValueToNullForNotExistingPersonId() {

        when(personService.getPersonByID(anyInt())).thenReturn(Optional.empty());

        sut.setAsText("453");

        assertThat(sut.getValue()).isNull();
    }

    private static Person personWithId(int id) {

        Person person = new Person();
        person.setId(id);

        return person;
    }
}
