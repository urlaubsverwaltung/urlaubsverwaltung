package org.synyx.urlaubsverwaltung.search;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.synyx.urlaubsverwaltung.search.PersonSuggestionLink.Icon;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Renders the person search suggestions, whose links carry request text like the search query.
 */
class PersonSearchFragmentTest {

    private static SpringTemplateEngine templateEngine;

    @BeforeAll
    static void setUpTemplateEngine() {

        final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding("UTF-8");

        final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(false);

        templateEngine = new SpringTemplateEngine();
        templateEngine.addTemplateResolver(templateResolver);
        templateEngine.setTemplateEngineMessageSource(messageSource);
    }

    @Test
    void ensureSuggestionLinksAreNotEvaluated() {

        final String href = "/web/person/2?year=__${7*7}__&q=x(a=${7*7})";

        final String html = render("", href);

        assertThat(html)
            .contains("href=\"/web/person/2?year=__${7*7}__&amp;q=x(a=${7*7})\"")
            .doesNotContain("49");
    }

    @Test
    void ensureSuggestionLinksContainContextPath() {

        final String html = render("/uv", "/web/person/2?q=M%C3%A4x+M%C3%BCller");

        assertThat(html).contains("href=\"/uv/web/person/2?q=M%C3%A4x+M%C3%BCller\"");
    }

    private static String render(String contextPath, String href) {

        final MockServletContext servletContext = new MockServletContext();
        servletContext.setContextPath(contextPath);
        final MockHttpServletRequest request = new MockHttpServletRequest(servletContext);
        request.setContextPath(contextPath);

        final PersonSuggestion suggestion = new PersonSuggestion(2L, "Marlene Muster", "MM", "marlene@example.org", href,
            List.of(new PersonSuggestionLink(href, "person-search.suggestion.link.account", Icon.ACCOUNT)));

        final WebContext context = new WebContext(
            JakartaServletWebApplication.buildApplication(servletContext).buildExchange(request, new MockHttpServletResponse()),
            Locale.GERMAN,
            Map.of("personSearchEnabled", true, "personSearchQuery", "x", "personSearchSuggestions", List.of(suggestion))
        );

        return templateEngine.process("fragments/person-search", Set.of("person-search"), context);
    }
}
