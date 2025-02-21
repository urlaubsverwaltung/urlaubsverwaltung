package org.synyx.urlaubsverwaltung.person.basedata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class PersonBasedataViewControllerTest {

    private PersonBasedataViewController sut;

    @Mock
    private PersonService personService;
    @Mock
    private PersonBasedataService personBasedataService;

    @BeforeEach
    void setUp() {
        sut = new PersonBasedataViewController(personBasedataService, personService);
    }

    @Test
    void ensuresCorrectModelOfPersonBasedata() throws Exception {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        final PersonBasedata personBasedata = new PersonBasedata(new PersonId(1L), "1337", "Some additional information");
        when(personBasedataService.getBasedataByPersonId(1)).thenReturn(Optional.of(personBasedata));

        perform(get("/web/person/1/basedata"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("personBasedata", hasProperty("personId", is(1L))))
            .andExpect(model().attribute("personBasedata", hasProperty("personnelNumber", is("1337"))))
            .andExpect(model().attribute("personBasedata", hasProperty("additionalInfo", is("Some additional information"))))
            .andExpect(view().name("person/person-basedata"));
    }

    @Test
    void ensuresFallbackOfPersonBasedata() throws Exception {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(personBasedataService.getBasedataByPersonId(1)).thenReturn(Optional.empty());

        perform(get("/web/person/1/basedata"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("personBasedata", hasProperty("personId", is(1L))))
            .andExpect(model().attribute("personBasedata", hasProperty("personnelNumber", is(""))))
            .andExpect(model().attribute("personBasedata", hasProperty("additionalInfo", is(""))))
            .andExpect(view().name("person/person-basedata"));
    }

    @Test
    void ensuresThrowingExceptionOnUnknownPerson() {

        when(personService.getPersonByID(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            perform(get("/web/person/1/basedata"))
        ).hasCauseInstanceOf(UnknownPersonException.class);
    }

    @Test
    void ensuresUpdateBaseDataCorrectly() throws Exception {

        perform(
            post("/web/person/1/basedata")
                .param("personnelNumber", "1337")
                .param("additionalInfo", "Additional Information")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/person/1"));

        final ArgumentCaptor<PersonBasedata> captor = ArgumentCaptor.forClass(PersonBasedata.class);
        verify(personBasedataService).update(captor.capture());

        final PersonBasedata personBasedata = captor.getValue();
        assertThat(personBasedata.personId()).isEqualTo(new PersonId(1L));
        assertThat(personBasedata.personnelNumber()).isEqualTo("1337");
        assertThat(personBasedata.additionalInformation()).isEqualTo("Additional Information");
    }

    @Test
    void ensuresUpdateHandlesErrorCorrectlyOfPersonnelNumber() throws Exception {

        final String personnelNumberTooLong21 = "133713371337133713371";

        perform(
            post("/web/person/1/basedata")
                .param("personnelNumber", personnelNumberTooLong21)
                .param("additionalInfo", "Additional Information")
        )
            .andExpect(status().isOk())
            .andExpect(model().attributeHasFieldErrors("personBasedata", "personnelNumber"))
            .andExpect(view().name("person/person-basedata"));
    }

    @Test
    void ensuresUpdateHandlesErrorCorrectlyOfAdditionalInformation() throws Exception {

        final String additionalInformationTooLong501 = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. In" +
            " dignissim fringilla neque at molestie. Curabitur rhoncus ipsum et nulla pellentesque dictum sed sed mi." +
            " Donec pharetra justo et massa luctus cursus. Suspendisse nulla ante, scelerisque a ornare in, vehicula" +
            " sit amet eros. Donec et metus euismod diam facilisis condimentum. Quisque ipsum ligula, congue vel" +
            " fermentum ut, sollicitudin nec ante. Nunc ullamcorper et mi et lacinia. Donec euismod sit amet ante" +
            " vitae posuere. Donec in tincidunt..";

        perform(
            post("/web/person/1/basedata")
                .param("personnelNumber", "1337")
                .param("additionalInfo", additionalInformationTooLong501)
        )
            .andExpect(status().isOk())
            .andExpect(model().attributeHasFieldErrors("personBasedata", "additionalInfo"))
            .andExpect(view().name("person/person-basedata"));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
