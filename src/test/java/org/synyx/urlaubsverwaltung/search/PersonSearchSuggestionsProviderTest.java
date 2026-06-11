package org.synyx.urlaubsverwaltung.search;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonPageable;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;

import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;
import static org.synyx.urlaubsverwaltung.search.PersonSuggestionLink.Icon.ACCOUNT;

@ExtendWith(MockitoExtension.class)
class PersonSearchSuggestionsProviderTest {

    private PersonSearchSuggestionsProvider sut;

    @Mock
    private PersonService personService;
    @Mock
    private DepartmentService departmentService;

    private static final Function<Person, String> MAIN_LINK = person -> "/web/person/%s/overview".formatted(person.getId());

    @BeforeEach
    void setUp() {
        sut = new PersonSearchSuggestionsProvider(personService, departmentService);
    }

    @Test
    void ensureOfficeUsesActivePersons() {

        final Person office = person(1L, "Office", "Manager", OFFICE);
        final Person hit = person(2L, "Marlene", "Muster");

        when(personService.getActivePersons(any(PersonPageable.class), eq("mar")))
            .thenReturn(new PageImpl<>(List.of(hit)));

        final List<PersonSuggestion> suggestions = sut.personSuggestions(office, "mar", MAIN_LINK);

        assertThat(suggestions).hasSize(1);
        assertThat(suggestions.getFirst().id()).isEqualTo(2L);
        assertThat(suggestions.getFirst().name()).isEqualTo("Marlene Muster");
        verifyNoInteractions(departmentService);
    }

    @Test
    void ensureBossUsesActivePersons() {

        final Person boss = person(1L, "Boss", "Person", BOSS);
        final Person hit = person(2L, "Marlene", "Muster");

        when(personService.getActivePersons(any(PersonPageable.class), eq("mar")))
            .thenReturn(new PageImpl<>(List.of(hit)));

        final List<PersonSuggestion> suggestions = sut.personSuggestions(boss, "mar", MAIN_LINK);

        assertThat(suggestions).hasSize(1);
        verifyNoInteractions(departmentService);
    }

    @Test
    void ensureDepartmentPrivilegedUsesManagedMembers() {

        final Person departmentHead = person(1L, "Department", "Head", DEPARTMENT_HEAD);
        final Person hit = person(2L, "Marlene", "Muster");

        when(departmentService.getManagedMembersOfPerson(eq(departmentHead), any(PersonPageable.class), eq("mar")))
            .thenReturn(new PageImpl<>(List.of(hit)));

        final List<PersonSuggestion> suggestions = sut.personSuggestions(departmentHead, "mar", MAIN_LINK);

        assertThat(suggestions).hasSize(1);
        assertThat(suggestions.getFirst().id()).isEqualTo(2L);
        verifyNoInteractions(personService);
    }

    @Test
    void ensurePlainUserGetsOnlyHimself() {

        final Person user = person(1L, "Plain", "User", USER);

        final List<PersonSuggestion> suggestions = sut.personSuggestions(user, "mar", MAIN_LINK);

        assertThat(suggestions).hasSize(1);
        assertThat(suggestions.getFirst().id()).isEqualTo(1L);
        verifyNoInteractions(personService);
        verifyNoInteractions(departmentService);
    }

    @Test
    void ensureRequestsFirstPageWithResultLimit() {

        final Person office = person(1L, "Office", "Manager", OFFICE);
        when(personService.getActivePersons(any(PersonPageable.class), eq("mar")))
            .thenReturn(new PageImpl<>(List.of()));

        sut.personSuggestions(office, "mar", MAIN_LINK);

        final ArgumentCaptor<PersonPageable> captor = ArgumentCaptor.forClass(PersonPageable.class);
        verify(personService).getActivePersons(captor.capture(), eq("mar"));
        assertThat(captor.getValue().getPageNumber()).isZero();
        assertThat(captor.getValue().getPageSize()).isEqualTo(6);
    }

    @Test
    void ensureResultsAreLimitedToSix() {

        final Person office = person(1L, "Office", "Manager", OFFICE);
        final List<Person> hits = IntStream.rangeClosed(10, 20)
            .mapToObj(i -> person(i, "First" + i, "Last" + i))
            .toList();

        when(personService.getActivePersons(any(PersonPageable.class), eq("")))
            .thenReturn(new PageImpl<>(hits));

        final List<PersonSuggestion> suggestions = sut.personSuggestions(office, "", MAIN_LINK);

        assertThat(suggestions).hasSize(6);
    }

    @Test
    void ensureMainLinkAppendsQueryParam() {

        final Person office = person(1L, "Office", "Manager", OFFICE);
        final Person hit = person(2L, "Marlene", "Muster");

        when(personService.getActivePersons(any(PersonPageable.class), eq("mar")))
            .thenReturn(new PageImpl<>(List.of(hit)));

        final List<PersonSuggestion> suggestions = sut.personSuggestions(office, "mar", MAIN_LINK);

        assertThat(suggestions.getFirst().href()).isEqualTo("/web/person/2/overview?q=mar");
    }

    @Test
    void ensureMainLinkHasNoQueryParamWhenQueryBlank() {

        final Person office = person(1L, "Office", "Manager", OFFICE);
        final Person hit = person(2L, "Marlene", "Muster");

        when(personService.getActivePersons(any(PersonPageable.class), eq("")))
            .thenReturn(new PageImpl<>(List.of(hit)));

        final List<PersonSuggestion> suggestions = sut.personSuggestions(office, "", MAIN_LINK);

        assertThat(suggestions.getFirst().href()).isEqualTo("/web/person/2/overview");
    }

    @Test
    void ensureOfficeGetsAccountLinkForOtherPerson() {

        final Person office = person(1L, "Office", "Manager", OFFICE);
        final Person hit = person(2L, "Marlene", "Muster");

        when(personService.getActivePersons(any(PersonPageable.class), eq("mar")))
            .thenReturn(new PageImpl<>(List.of(hit)));

        final List<PersonSuggestion> suggestions = sut.personSuggestions(office, "mar", MAIN_LINK);

        assertThat(suggestions.getFirst().links())
            .extracting(PersonSuggestionLink::href, PersonSuggestionLink::messageKey, PersonSuggestionLink::icon)
            .containsExactly(tuple("/web/person/2?q=mar", "person-search.suggestion.link.account", ACCOUNT));
    }

    @Test
    void ensureLoggedInUserGetsAccountLinkToHimself() {

        final Person departmentHead = person(1L, "Department", "Head", DEPARTMENT_HEAD);

        when(departmentService.getManagedMembersOfPerson(eq(departmentHead), any(PersonPageable.class), eq("dep")))
            .thenReturn(new PageImpl<>(List.of(departmentHead)));

        final List<PersonSuggestion> suggestions = sut.personSuggestions(departmentHead, "dep", MAIN_LINK);

        assertThat(suggestions.getFirst().links())
            .extracting(PersonSuggestionLink::href)
            .containsExactly("/web/person/1?q=dep");
    }

    @Test
    void ensureNonOfficeGetsNoAccountLinkForOtherPerson() {

        final Person departmentHead = person(1L, "Department", "Head", DEPARTMENT_HEAD);
        final Person hit = person(2L, "Marlene", "Muster");

        when(departmentService.getManagedMembersOfPerson(eq(departmentHead), any(PersonPageable.class), eq("mar")))
            .thenReturn(new PageImpl<>(List.of(hit)));

        final List<PersonSuggestion> suggestions = sut.personSuggestions(departmentHead, "mar", MAIN_LINK);

        assertThat(suggestions.getFirst().links()).isEmpty();
    }

    @Test
    void ensureSuggestionCarriesPersonData() {

        final Person office = person(1L, "Office", "Manager", OFFICE);
        final Person hit = person(2L, "Marlene", "Muster");
        hit.setEmail("marlene@example.org");

        when(personService.getActivePersons(any(PersonPageable.class), eq("mar")))
            .thenReturn(new PageImpl<>(List.of(hit)));

        final PersonSuggestion suggestion = sut.personSuggestions(office, "mar", MAIN_LINK).getFirst();

        assertThat(suggestion.id()).isEqualTo(2L);
        assertThat(suggestion.name()).isEqualTo("Marlene Muster");
        assertThat(suggestion.initials()).isEqualTo("MM");
        assertThat(suggestion.email()).isEqualTo("marlene@example.org");
    }

    private static Person person(long id, String firstName, String lastName, Role... roles) {
        final Person person = new Person();
        person.setId(id);
        person.setFirstName(firstName);
        person.setLastName(lastName);
        person.setPermissions(List.of(roles));
        return person;
    }
}
