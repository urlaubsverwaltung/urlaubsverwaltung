package org.synyx.urlaubsverwaltung.person.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verifyNoInteractions;
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

        final long personId = 10;
        sut.setValue(personWithId(personId));

        assertThat(sut.getAsText()).isEqualTo(Long.toString(personId));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    @NullSource
    void ensureSetAsTextDoesNotThrowWhenGivenTextIsEmpty(String givenText) {

        assertThatCode(() -> sut.setAsText(givenText)).doesNotThrowAnyException();

        verifyNoInteractions(personService);
    }

    @Test
    void ensureSetAsTextSetsValueToPersonForExistingPersonId() {

        final long personId = 5;
        final Person person = personWithId(personId);
        when(personService.getPersonByID(personId)).thenReturn(Optional.of(person));

        sut.setAsText(Long.toString(personId));

        assertThat(sut.getValue()).isEqualTo(person);
    }

    @Test
    void ensureSetAsTextSetsValueToNullForNotExistingPersonId() {

        when(personService.getPersonByID(anyLong())).thenReturn(Optional.empty());

        sut.setAsText("453");

        assertThat(sut.getValue()).isNull();
    }

    private static Person personWithId(long id) {

        Person person = new Person();
        person.setId(id);

        return person;
    }
}
