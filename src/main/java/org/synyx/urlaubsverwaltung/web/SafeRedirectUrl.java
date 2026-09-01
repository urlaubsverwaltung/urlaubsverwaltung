package org.synyx.urlaubsverwaltung.web;

import org.jspecify.annotations.Nullable;
import org.springframework.http.server.PathContainer;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Guards redirect targets that originate from a request parameter.
 *
 * <p>Pages hand the page they were started from to an action - "cancel this sick note, then take me back to
 * {@code /web/person/42/overview}" - so the value is under the control of whoever crafts the link. Feeding it into
 * {@code "redirect:" + url} unchecked would let a crafted link drop the user on an arbitrary page after the action,
 * so only the pages that actually offer such an action are accepted.
 */
public final class SafeRedirectUrl {

    private static final PathPatternParser PARSER = new PathPatternParser();

    /**
     * The pages that offer accept / cancel / allow / reject shortcuts and may therefore ask to be returned to.
     */
    private static final List<PathPattern> ALLOWED_ORIGINS = Stream.of(
            "/web/application",
            "/web/application/replacement",
            "/web/sicknote/submitted",
            "/web/persons/{personId}/applications",
            "/web/persons/{personId}/sicknotes",
            "/web/person/{personId}/overview"
        )
        .map(PARSER::parse)
        .toList();

    private SafeRedirectUrl() {
        // static helper
    }

    /**
     * Accepts the given url if it is one of the pages an action can be started from.
     *
     * <p>The url has to be a path of this application - it must start with a single {@code /}, since
     * {@code //example.org} is a protocol relative url that would leave the application, must not contain a
     * backslash, since browsers normalise those to {@code /} and {@code /\example.org} would leave the application
     * after all, and must not contain control characters, which would allow injecting headers into the response.
     * Its path - the query string is not part of the comparison - then has to match one of {@link #ALLOWED_ORIGINS}.
     *
     * @param url url to check, may be {@code null}
     * @return the url if an action may return to it, otherwise {@link Optional#empty()}
     */
    public static Optional<String> ofKnownOrigin(@Nullable String url) {

        if (url == null || !url.startsWith("/") || url.startsWith("//")) {
            return Optional.empty();
        }

        if (url.contains("\\") || url.chars().anyMatch(Character::isISOControl)) {
            return Optional.empty();
        }

        final int queryStart = url.indexOf('?');
        final String path = queryStart == -1 ? url : url.substring(0, queryStart);

        final PathContainer pathContainer = PathContainer.parsePath(path);
        return ALLOWED_ORIGINS.stream().anyMatch(pattern -> pattern.matches(pathContainer))
            ? Optional.of(url)
            : Optional.empty();
    }
}
