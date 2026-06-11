package org.synyx.urlaubsverwaltung.search;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonPageRequest;
import org.synyx.urlaubsverwaltung.person.PersonPageable;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.search.PersonSuggestionLink.Icon;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.springframework.util.StringUtils.hasText;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;

@Component
class PersonSearchSuggestionsProvider {

    public static final String PERSON_SEARCH_QUERY_PARAM = "q";

    private static final int PERSON_RESULT_LIMIT = 6;

    private final PersonService personService;
    private final DepartmentService departmentService;

    PersonSearchSuggestionsProvider(PersonService personService, DepartmentService departmentService) {
        this.personService = personService;
        this.departmentService = departmentService;
    }

    /**
     * Builds the person search suggestions for the given query, scoped to what the logged-in person is allowed to see.
     *
     * <p>
     * Note that this method does not check authorization!
     *
     * @param loggedInPerson the currently logged-in person
     * @param query search query
     * @param mainLinkBuilder builds the main link of a suggestion
     * @return the (possibly empty) list of suggestions, limited to {@value #PERSON_RESULT_LIMIT}
     */
    List<PersonSuggestion> personSuggestions(Person loggedInPerson, String query, Function<Person, String> mainLinkBuilder) {

        final PersonPageRequest pageRequest = PersonPageRequest.of(0, PERSON_RESULT_LIMIT);

        final List<Person> persons = getAllRelevantPersons(loggedInPerson, query, pageRequest).toList();
        return persons.stream()
            .limit(PERSON_RESULT_LIMIT)
            .map(person -> toPersonSearchSuggestion(loggedInPerson, person, query, mainLinkBuilder))
            .toList();
    }

    private Page<Person> getAllRelevantPersons(Person person, String query, PersonPageable pageRequest) {

        if (person.hasRole(OFFICE) || person.hasRole(BOSS)) {
            return personService.getActivePersons(pageRequest, query);
        }

        if (person.isDepartmentPrivileged()) {
            return departmentService.getManagedMembersOfPerson(person, pageRequest, query);
        }

        return new PageImpl<>(List.of(person), pageRequest.toPageable(), 1);
    }

    private static PersonSuggestion toPersonSearchSuggestion(Person loggedInPerson, Person person, String query, Function<Person, String> mainLinkBuilder) {

        final boolean isLoggedInUser = loggedInPerson.getId().equals(person.getId());

        final List<PersonSuggestionLink> links = new ArrayList<>();
        if (isLoggedInUser || loggedInPerson.hasRole(OFFICE)) {
            final String href = "/web/person/%s".formatted(person.getId());
            links.add(new PersonSuggestionLink(withQuery(href, query), "person-search.suggestion.link.account", Icon.ACCOUNT));
        }

        final String mainHref = withQuery(mainLinkBuilder.apply(person), query);

        return new PersonSuggestion(person.getId(), person.getNiceName(), person.getInitials(), person.getEmail(), mainHref, links);
    }

    private static String withQuery(String url, String query) {
        final UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(url);
        if (hasText(query)) {
            uriComponentsBuilder.queryParam(PERSON_SEARCH_QUERY_PARAM, query);
        }
        return uriComponentsBuilder.build().toUriString();
    }
}
