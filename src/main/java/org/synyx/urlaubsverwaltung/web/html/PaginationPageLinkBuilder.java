package org.synyx.urlaubsverwaltung.web.html;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

public class PaginationPageLinkBuilder {

    private PaginationPageLinkBuilder() {
    }

    /**
     * Builds a pagination link prefix without the `page` query parameter.
     *
     * @param pageable the pageable
     * @return query to append to each pagination link
     */
    public static String buildPageLinkPrefix(Pageable pageable) {
        return buildPageLinkPrefix(pageable, List.of());
    }

    /**
     * Builds a pagination link prefix without the `page` query parameter.
     *
     * @param pageable the pageable
     * @param parameters list of parameter tuples
     * @return query to append to each pagination link
     */
    public static String buildPageLinkPrefix(Pageable pageable, List<QueryParam> parameters) {

        final List<String> linkParameters = new ArrayList<>();

        parameters.forEach(tuple -> linkParameters.add(tuple.name + "=" + tuple.value));

        for (Sort.Order order : pageable.getSort()) {
            linkParameters.add("sort=" + order.getProperty() + "," + order.getDirection());
        }

        if (pageable.isPaged()) {
            // TODO 'size' name is configurable `spring.data.web.pageable.size-parameter=size`
            linkParameters.add("size=" + pageable.getPageSize());
        }

        return "?" + String.join("&", linkParameters);
    }

    public record QueryParam(String name, String value) {}
}
