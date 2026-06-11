package org.synyx.urlaubsverwaltung.search;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.synyx.urlaubsverwaltung.search.PersonSearchSuggestionsProvider.PERSON_SEARCH_QUERY_PARAM;
import static org.synyx.urlaubsverwaltung.web.HotwiredTurboConstants.TURBO_FRAME_HEADER;

/**
 * Cross-cutting mechanism of the global person search.
 *
 * <p>
 * For every page rendered by a {@link HasPersonSearch} controller this enables the search box ({@link #postHandle}). The
 * suggestions themselves are served by a dedicated turbo-frame request that targets the {@value #PERSON_SEARCH_TURBO_FRAME}
 * frame: such a request is short-circuited in {@link #preHandle} and answered with only the person-search fragment, so
 * the feature controller's (potentially expensive) handler is never invoked for a search-only request.
 *
 * <p>
 * Global search is only supported with enabled JavaScript: without Turbo the {@value #PERSON_SEARCH_TURBO_FRAME} frame
 * request is never issued.
 *
 * <p>
 * Authorization of the page is the page's own responsibility: a {@value #PERSON_SEARCH_TURBO_FRAME} request is still
 * routed to the feature controller's handler method and thus guarded by its existing security rules before this
 * interceptor short-circuits it. The visible suggestions are additionally scoped by
 * {@link PersonSearchSuggestionsProvider#personSuggestions} to what the logged-in person may see.
 */
@Component
class PersonSearchInterceptor implements HandlerInterceptor {

    /**
     * Turbo-Frame id of the suggestions frame, see {@code fragments/person-search.html}. A turbo-frame request carrying
     * this id is a search-only request.
     */
    static final String PERSON_SEARCH_TURBO_FRAME = "frame-persons-suggestions";

    private final PersonService personService;
    private final PersonSearchSuggestionsProvider personSearchSuggestionsProvider;
    private final ThymeleafViewResolver thymeleafViewResolver;

    PersonSearchInterceptor(
        PersonService personService,
        PersonSearchSuggestionsProvider personSearchSuggestionsProvider,
        ThymeleafViewResolver thymeleafViewResolver
    ) {
        this.personService = personService;
        this.personSearchSuggestionsProvider = personSearchSuggestionsProvider;
        this.thymeleafViewResolver = thymeleafViewResolver;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {

        if (!(handler instanceof HandlerMethod handlerMethod) || !(handlerMethod.getBean() instanceof HasPersonSearch personSearch)) {
            return true;
        }

        // only a search-only request (suggestions frame) is short-circuited; everything else runs the regular handler
        if (!PERSON_SEARCH_TURBO_FRAME.equals(request.getHeader(TURBO_FRAME_HEADER))) {
            return true;
        }

        final Person signedInUser = personService.getSignedInUser();
        final String query = request.getParameter(PERSON_SEARCH_QUERY_PARAM);
        final boolean allowedToSearch = signedInUser.isPrivileged();

        final Map<String, Object> model = new HashMap<>();
        model.put("personSearchEnabled", allowedToSearch);
        model.put("personSearchQuery", query);

        if (allowedToSearch) {
            final PersonSuggestionUrlStrategy urlStrategy = personSearch.personSuggestionUrlStrategy();
            final SearchContext context = SearchContext.of(request, signedInUser);
            final List<PersonSuggestion> suggestions = personSearchSuggestionsProvider.personSuggestions(signedInUser, query,
                suggestion -> urlStrategy.buildSuggestionMainLink(suggestion, context));
            model.put("personSearchSuggestions", suggestions);
        } else {
            model.put("personSearchSuggestions", List.of());
        }

        final String viewName = personSearch.personSearchUiFragmentSupplier().get();
        renderPersonSearchFragment(viewName, model, request, response);

        // handler (and the rest of the dispatch) is skipped, the response is fully rendered above
        return false;
    }

    @Override
    public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, ModelAndView modelAndView) {

        if (modelAndView == null || !(handler instanceof HandlerMethod handlerMethod)) {
            return;
        }
        if (!(handlerMethod.getBean() instanceof HasPersonSearch personSearch)) {
            return;
        }
        final String viewName = modelAndView.getViewName();
        if (viewName != null && viewName.startsWith("redirect:")) {
            return;
        }

        final Person signedInUser = personService.getSignedInUser();

        // only the cheap search-box attributes; suggestions are served by the short-circuited preHandle frame request
        modelAndView.addObject("personSearchEnabled", signedInUser.isPrivileged());
        modelAndView.addObject("personSearchQuery", request.getParameter(PERSON_SEARCH_QUERY_PARAM));
        modelAndView.addObject("personSearchFragment", personSearch.personSearchUiFragmentSupplier().get());
    }

    private void renderPersonSearchFragment(String viewName, Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        final Locale locale = RequestContextUtils.getLocale(request);
        final View view = thymeleafViewResolver.resolveViewName(viewName, locale);
        if (view == null) {
            throw new IllegalStateException("could not resolve person search view '%s'".formatted(viewName));
        }
        view.render(model, request, response);
    }
}
