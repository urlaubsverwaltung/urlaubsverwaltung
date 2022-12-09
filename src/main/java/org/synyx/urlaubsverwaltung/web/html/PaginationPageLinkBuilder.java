package org.synyx.urlaubsverwaltung.web.html;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;

public class PaginationPageLinkBuilder {

    private PaginationPageLinkBuilder() {
    }

    /**
     * Builds a pagination link prefix without the `page` query parameter.
     *
     * @param pageable
     * @return
     */
    public static String buildPageLinkPrefix(Pageable pageable) {
        return buildPageLinkPrefix(pageable, emptyMap());
    }

    /**
     * Builds a pagination link prefix without the `page` query parameter.
     *
     * @param pageable
     * @param parameters
     * @return
     */
    public static String buildPageLinkPrefix(Pageable pageable, Map<String, String> parameters) {

        final List<String> linkParameters = new ArrayList<>();

        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            linkParameters.add(entry.getKey() + "=" + entry.getValue());
        }

        for (Sort.Order order : pageable.getSort()) {
            linkParameters.add("sort=" + order.getProperty() + "," + order.getDirection());
        }

        if (pageable.isPaged()) {
            // TODO 'size' name is configurable `spring.data.web.pageable.size-parameter=size`
            linkParameters.add("size=" + pageable.getPageSize());
        }

        return "?" + String.join("&", linkParameters);
    }
}
