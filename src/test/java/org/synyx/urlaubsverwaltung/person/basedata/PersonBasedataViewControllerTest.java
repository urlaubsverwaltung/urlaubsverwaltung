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
        person.setId(1);

        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));
        final PersonBasedata personBasedata = new PersonBasedata(1, "1337", "Some additional information");
        when(personBasedataService.getBasedataByPersonId(1)).thenReturn(Optional.of(personBasedata));

        perform(get("/web/person/1/basedata"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("personBasedata", hasProperty("personId", is(1))))
            .andExpect(model().attribute("personBasedata", hasProperty("personnelNumber", is("1337"))))
            .andExpect(model().attribute("personBasedata", hasProperty("additionalInfo", is("Some additional information"))))
            .andExpect(view().name("thymeleaf/person/person-basedata"));
    }

    @Test
    void ensuresFallbackOfPersonBasedata() throws Exception {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1);

        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));
        when(personBasedataService.getBasedataByPersonId(1)).thenReturn(Optional.empty());

        perform(get("/web/person/1/basedata"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("personBasedata", hasProperty("personId", is(1))))
            .andExpect(model().attribute("personBasedata", hasProperty("personnelNumber", is(""))))
            .andExpect(model().attribute("personBasedata", hasProperty("additionalInfo", is(""))))
            .andExpect(view().name("thymeleaf/person/person-basedata"));
    }

    @Test
    void ensuresThrowingExceptionOnUnknownPerson() {

        when(personService.getPersonByID(1)).thenReturn(Optional.empty());

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
        assertThat(personBasedata.getPersonId()).isEqualTo(1);
        assertThat(personBasedata.getPersonnelNumber()).isEqualTo("1337");
        assertThat(personBasedata.getAdditionalInformation()).isEqualTo("Additional Information");
    }

    @Test
    void ensuresUpdateHandlesErrorCorrectlyOfPersonnelNumber() throws Exception {

        final String personnelNumberTooLong256 = "1337133713371337133713371337133713371337133713371337133713371" +
            "337133713371337133713371337133713371337133713371337133713371337133713371337133713371337133713" +
            "371337133713371337133713371337133713371337133713371337133713371337133713371337133713371337133713371337";

        perform(
            post("/web/person/1/basedata")
                .param("personnelNumber", personnelNumberTooLong256)
                .param("additionalInfo", "Additional Information")
        )
            .andExpect(status().isOk())
            .andExpect(model().attributeHasFieldErrors("personBasedata", "personnelNumber"))
            .andExpect(view().name("thymeleaf/person/person-basedata"));
    }

    @Test
    void ensuresUpdateHandlesErrorCorrectlyOfAdditionalInformation() throws Exception {

        final String additionalInformationTooLong256 = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam n" +
            "onumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et" +
            " accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata ";

        perform(
            post("/web/person/1/basedata")
                .param("personnelNumber", "1337")
                .param("additionalInfo", additionalInformationTooLong256)
        )
            .andExpect(status().isOk())
            .andExpect(model().attributeHasFieldErrors("personBasedata", "additionalInfo"))
            .andExpect(view().name("thymeleaf/person/person-basedata"));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
