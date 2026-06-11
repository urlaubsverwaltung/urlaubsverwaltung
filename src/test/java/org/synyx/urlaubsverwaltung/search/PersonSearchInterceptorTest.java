package org.synyx.urlaubsverwaltung.search;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;
import static org.synyx.urlaubsverwaltung.search.PersonSearchInterceptor.PERSON_SEARCH_TURBO_FRAME;
import static org.synyx.urlaubsverwaltung.search.PersonSearchSuggestionsProvider.PERSON_SEARCH_QUERY_PARAM;
import static org.synyx.urlaubsverwaltung.web.HotwiredTurboConstants.TURBO_FRAME_HEADER;

@ExtendWith(MockitoExtension.class)
class PersonSearchInterceptorTest {

    private PersonSearchInterceptor sut;

    @Mock
    private PersonService personService;
    @Mock
    private PersonSearchSuggestionsProvider personSearchViewHelper;
    @Mock
    private ThymeleafViewResolver thymeleafViewResolver;

    @BeforeEach
    void setUp() {
        sut = new PersonSearchInterceptor(personService, personSearchViewHelper, thymeleafViewResolver);
    }


    @Nested
    class PreHandle {
        // --- preHandle: search-only frame request is short-circuited, the feature handler is never invoked ---

        @Test
        void ensurePreHandleReturnsTrueWhenHandlerIsNotHandlerMethod() throws Exception {

            final boolean proceed = sut.preHandle(searchFrameRequest(), response(), new Object());

            assertThat(proceed).isTrue();
            verifyNoInteractions(personService, personSearchViewHelper, thymeleafViewResolver);
        }

        @Test
        void ensurePreHandleReturnsTrueWhenBeanDoesNotImplementHasPersonSearch() throws Exception {

            final HandlerMethod handlerMethod = mock(HandlerMethod.class);
            when(handlerMethod.getBean()).thenReturn(new Object());

            final boolean proceed = sut.preHandle(searchFrameRequest(), response(), handlerMethod);

            assertThat(proceed).isTrue();
            verifyNoInteractions(personService, personSearchViewHelper, thymeleafViewResolver);
        }

        @Test
        void ensurePreHandleReturnsTrueWhenNoTurboFrameHeader() throws Exception {

            final HandlerMethod handlerMethod = mock(HandlerMethod.class);
            when(handlerMethod.getBean()).thenReturn(new PersonSearchController());

            final boolean proceed = sut.preHandle(request(), response(), handlerMethod);

            assertThat(proceed).isTrue();
            verifyNoInteractions(personService, personSearchViewHelper, thymeleafViewResolver);
        }

        @Test
        void ensurePreHandleReturnsTrueForDifferentTurboFrame() throws Exception {

            final HandlerMethod handlerMethod = mock(HandlerMethod.class);
            when(handlerMethod.getBean()).thenReturn(new PersonSearchController());

            final MockHttpServletRequest request = request();
            request.addHeader(TURBO_FRAME_HEADER, "frame-statistics");

            final boolean proceed = sut.preHandle(request, response(), handlerMethod);

            assertThat(proceed).isTrue();
            verifyNoInteractions(personService, personSearchViewHelper, thymeleafViewResolver);
        }

        @Test
        void ensurePreHandleRendersSuggestionsAndReturnsFalseForPrivilegedUser() throws Exception {

            final Person signedInUser = person(1L, OFFICE);
            when(personService.getSignedInUser()).thenReturn(signedInUser);

            final PersonSuggestion suggestion = new PersonSuggestion(2L, "Marlene Muster", "MM", "marlene@example.org", "/web/person/2/overview", emptyList());
            when(personSearchViewHelper.personSuggestions(eq(signedInUser), eq("mar"), any()))
                .thenReturn(List.of(suggestion));

            final View view = mock(View.class);
            when(thymeleafViewResolver.resolveViewName(eq("fragments/person-search"), any(Locale.class))).thenReturn(view);

            final MockHttpServletRequest request = searchFrameRequest();
            request.setParameter(PERSON_SEARCH_QUERY_PARAM, "mar");
            final MockHttpServletResponse response = response();

            final HandlerMethod handlerMethod = mock(HandlerMethod.class);
            when(handlerMethod.getBean()).thenReturn(new PersonSearchController());

            final boolean proceed = sut.preHandle(request, response, handlerMethod);

            assertThat(proceed).isFalse();

            @SuppressWarnings("unchecked")
            final ArgumentCaptor<Map<String, Object>> modelCaptor = ArgumentCaptor.forClass(Map.class);
            verify(view).render(modelCaptor.capture(), eq(request), eq(response));
            assertThat(modelCaptor.getValue())
                .containsEntry("personSearchEnabled", true)
                .containsEntry("personSearchQuery", "mar")
                .containsEntry("personSearchSuggestions", List.of(suggestion));
        }

        @Test
        void ensurePreHandlePassesUrlStrategyToSuggestionsProvider() throws Exception {

            final Person signedInUser = person(1L, OFFICE);
            when(personService.getSignedInUser()).thenReturn(signedInUser);

            final Person suggestedPerson = person(2L, USER);

            final View view = mock(View.class);
            when(thymeleafViewResolver.resolveViewName(eq("fragments/person-search"), any(Locale.class))).thenReturn(view);

            final HandlerMethod handlerMethod = mock(HandlerMethod.class);
            when(handlerMethod.getBean()).thenReturn(new PersonSearchController());

            final MockHttpServletRequest request = searchFrameRequest();
            request.setParameter(PERSON_SEARCH_QUERY_PARAM, "mar");

            sut.preHandle(request, response(), handlerMethod);

            @SuppressWarnings("unchecked")
            final ArgumentCaptor<Function<Person, String>> linkBuilderCaptor = ArgumentCaptor.forClass(Function.class);
            verify(personSearchViewHelper).personSuggestions(eq(signedInUser), eq("mar"), linkBuilderCaptor.capture());

            assertThat(linkBuilderCaptor.getValue().apply(suggestedPerson)).isEqualTo("strategy-link-2");
        }

        @Test
        void ensurePreHandleRendersWithoutSuggestionsForUnprivilegedUserAndReturnsFalse() throws Exception {

            final Person signedInUser = person(1L, USER);
            when(personService.getSignedInUser()).thenReturn(signedInUser);

            final View view = mock(View.class);
            when(thymeleafViewResolver.resolveViewName(eq("fragments/person-search"), any(Locale.class))).thenReturn(view);

            final MockHttpServletRequest request = searchFrameRequest();
            request.setParameter(PERSON_SEARCH_QUERY_PARAM, "mar");
            final MockHttpServletResponse response = response();

            final HandlerMethod handlerMethod = mock(HandlerMethod.class);
            when(handlerMethod.getBean()).thenReturn(new PersonSearchController());

            final boolean proceed = sut.preHandle(request, response, handlerMethod);

            assertThat(proceed).isFalse();

            @SuppressWarnings("unchecked")
            final ArgumentCaptor<Map<String, Object>> modelCaptor = ArgumentCaptor.forClass(Map.class);
            verify(view).render(modelCaptor.capture(), eq(request), eq(response));
            assertThat(modelCaptor.getValue())
                .containsEntry("personSearchEnabled", false)
                .containsEntry("personSearchQuery", "mar")
                .containsEntry("personSearchSuggestions", List.of());
            verify(personSearchViewHelper, never()).personSuggestions(any(), any(), any());
        }
    }

    @Nested
    class PostHandle {
        // --- postHandle: enables the search box only, suggestions are served by the preHandle frame request ---

        @Test
        void ensurePostHandleDoesNothingWhenModelAndViewIsNull() {

            final HandlerMethod handlerMethod = mock(HandlerMethod.class);

            sut.postHandle(request(), response(), handlerMethod, null);

            verifyNoInteractions(personService, personSearchViewHelper);
        }

        @Test
        void ensurePostHandleDoesNothingWhenHandlerIsNotHandlerMethod() {

            final ModelAndView modelAndView = anyView();

            sut.postHandle(request(), response(), new Object(), modelAndView);

            assertThat(modelAndView.getModel()).isEmpty();
            verifyNoInteractions(personService, personSearchViewHelper);
        }

        @Test
        void ensurePostHandleDoesNothingWhenBeanDoesNotImplementHasPersonSearch() {

            final HandlerMethod handlerMethod = mock(HandlerMethod.class);
            when(handlerMethod.getBean()).thenReturn(new Object());

            final ModelAndView modelAndView = anyView();

            sut.postHandle(request(), response(), handlerMethod, modelAndView);

            assertThat(modelAndView.getModel()).isEmpty();
            verifyNoInteractions(personService, personSearchViewHelper);
        }

        @Test
        void ensurePostHandleDoesNothingForRedirectView() {

            final HandlerMethod handlerMethod = mock(HandlerMethod.class);
            when(handlerMethod.getBean()).thenReturn(new PersonSearchController());

            final ModelAndView modelAndView = new ModelAndView("redirect:/web/somewhere");

            sut.postHandle(request(), response(), handlerMethod, modelAndView);

            assertThat(modelAndView.getModel()).isEmpty();
            verifyNoInteractions(personService, personSearchViewHelper);
        }

        @Test
        void ensurePostHandleEnablesSearchBoxForPrivilegedUserWithoutSuggestions() {

            final Person signedInUser = person(1L, OFFICE);
            when(personService.getSignedInUser()).thenReturn(signedInUser);

            final MockHttpServletRequest request = request();
            request.setParameter(PERSON_SEARCH_QUERY_PARAM, "mar");

            final HandlerMethod handlerMethod = mock(HandlerMethod.class);
            when(handlerMethod.getBean()).thenReturn(new PersonSearchController());

            final ModelAndView modelAndView = anyView();

            sut.postHandle(request, response(), handlerMethod, modelAndView);

            assertThat(modelAndView.getModel())
                .containsEntry("personSearchEnabled", true)
                .containsEntry("personSearchQuery", "mar")
                .containsEntry("personSearchFragment", "fragments/person-search")
                .doesNotContainKey("personSearchSuggestions");
            verifyNoInteractions(personSearchViewHelper);
        }

        @Test
        void ensurePostHandleDisablesSearchBoxForUnprivilegedUser() {

            final Person signedInUser = person(1L, USER);
            when(personService.getSignedInUser()).thenReturn(signedInUser);

            final MockHttpServletRequest request = request();
            request.setParameter(PERSON_SEARCH_QUERY_PARAM, "mar");

            final HandlerMethod handlerMethod = mock(HandlerMethod.class);
            when(handlerMethod.getBean()).thenReturn(new PersonSearchController());

            final ModelAndView modelAndView = anyView();

            sut.postHandle(request, response(), handlerMethod, modelAndView);

            assertThat(modelAndView.getModel())
                .containsEntry("personSearchEnabled", false)
                .containsEntry("personSearchQuery", "mar")
                .containsEntry("personSearchFragment", "fragments/person-search");
            verifyNoInteractions(personSearchViewHelper);
        }
    }

    private static @NonNull ModelAndView anyView() {
        return new ModelAndView("any-view");
    }

    private static @NonNull MockHttpServletResponse response() {
        return new MockHttpServletResponse();
    }

    private static @NonNull MockHttpServletRequest request() {
        return new MockHttpServletRequest();
    }

    private static @NonNull MockHttpServletRequest searchFrameRequest() {
        final MockHttpServletRequest request = request();
        request.addHeader(TURBO_FRAME_HEADER, PERSON_SEARCH_TURBO_FRAME);
        return request;
    }

    private static Person person(long id, Role... roles) {
        final Person person = new Person();
        person.setId(id);
        person.setPermissions(List.of(roles));
        return person;
    }

    private static class PersonSearchController implements HasPersonSearch {
        @Override
        public PersonSuggestionUrlStrategy personSuggestionUrlStrategy() {
            return (suggestion, request) -> "strategy-link-" + suggestion.getId();
        }

        @Override
        public PersonSearchUiFragmentSupplier personSearchUiFragmentSupplier() {
            return () -> "fragments/person-search";
        }
    }
}
