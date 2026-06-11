package org.synyx.urlaubsverwaltung.search;

import jakarta.servlet.http.HttpServletRequest;
import org.synyx.urlaubsverwaltung.person.Person;

public interface SearchContext {

    /**
     * Returns the request URI path without contextPath.
     *
     * @return the request URI path without contextPath
     */
    default String getRequestPath() {
        return getRequestPath(getRequest());
    }

    /**
     * Returns the request URI path without contextPath.
     *
     * @param request the http request
     * @return the request URI path without contextPath
     */
    default String getRequestPath(HttpServletRequest request) {
        return request.getRequestURI().substring(request.getContextPath().length());
    }

    HttpServletRequest getRequest();

    /**
     * Returns the person doing the search (currently logged-in user)
     *
     * @return the person doing the search (currently logged-in user)
     */
    Person getPerson();

    static SearchContext of(HttpServletRequest request, Person person) {
        return new SearchContext() {
            @Override
            public HttpServletRequest getRequest() {
                return request;
            }

            @Override
            public Person getPerson() {
                return person;
            }
        };
    }
}
