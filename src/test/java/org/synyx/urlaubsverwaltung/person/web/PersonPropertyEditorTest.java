package org.synyx.urlaubsverwaltung.person.web;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PersonPropertyEditorTest {

    private PersonPropertyEditor sut;

    @Mock
    private PersonService personService;

    @Before
    public void setUp() {

        sut = new PersonPropertyEditor(personService);
    }

    @Test
    public void ensureGetAsTextReturnsEmptyStringForNullValue() {

        sut.setValue(null);

        assertThat(sut.getAsText()).isEmpty();
    }

    @Test
    public void ensureGetAsTextReturnsIdOfPerson() {

        final int personId = 10;
        sut.setValue(personWithId(personId));

        assertThat(sut.getAsText()).isEqualTo(Integer.toString(personId));
    }

    @Test
    public void ensureSetAsTextSetsValueToPersonForExistingPersonId() {

        final int personId = 5;
        final Person person = personWithId(personId);
        when(personService.getPersonByID(personId)).thenReturn(Optional.of(person));

        sut.setAsText(Integer.toString(personId));

        assertThat(sut.getValue()).isEqualTo(person);
    }

    @Test
    public void ensureSetAsTextSetsValueToNullForNotExistingPersonId() {

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
