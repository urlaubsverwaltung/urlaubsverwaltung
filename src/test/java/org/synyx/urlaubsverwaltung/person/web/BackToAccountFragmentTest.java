package org.synyx.urlaubsverwaltung.person.web;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.StringTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.util.Locale;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Renders the link back to the account that the account subpages share, so that all of them lead back the same way.
 */
class BackToAccountFragmentTest {

    private static final String USAGE = "<div th:replace=\"~{person/back-to-account::back-to-account(personId=${personId}, personNiceName=${personNiceName}, year=${year})}\"></div>";

    private static final String CHEVRON_LEFT_ICON = "<polyline points=\"15 18 9 12 15 6\">";

    private static SpringTemplateEngine templateEngine;

    @BeforeAll
    static void setUpTemplateEngine() {

        final StringTemplateResolver inlineResolver = new StringTemplateResolver();
        inlineResolver.setTemplateMode(TemplateMode.HTML);
        inlineResolver.setResolvablePatterns(Set.of("<*"));
        inlineResolver.setOrder(1);

        final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setOrder(2);

        final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(false);

        templateEngine = new SpringTemplateEngine();
        templateEngine.addTemplateResolver(inlineResolver);
        templateEngine.addTemplateResolver(templateResolver);
        templateEngine.setTemplateEngineMessageSource(messageSource);
    }

    @Test
    void ensureLinksToTheAccountOfThePerson() {

        final String html = render(Locale.GERMAN, 1L, 42L, "Marlene Muster", null);

        assertThat(html).contains("href=\"/web/person/42\"", ">Konto von Marlene Muster<", CHEVRON_LEFT_ICON);
    }

    @Test
    void ensureLinksToTheOwnAccount() {

        final String html = render(Locale.GERMAN, 42L, 42L, "Marlene Muster", null);

        assertThat(html)
            .contains("href=\"/web/person/42\"", ">Mein Konto<")
            .doesNotContain("Marlene Muster");
    }

    @Test
    void ensureLinksToTheAccountOfTheGivenYear() {

        final String html = render(Locale.GERMAN, 1L, 42L, "Marlene Muster", 2025);

        assertThat(html).contains("href=\"/web/person/42?year=2025\"");
    }

    @Test
    void ensureLinkTextIsTranslated() {

        final String html = render(Locale.ENGLISH, 1L, 42L, "Marlene Muster", null);

        assertThat(html).contains(">Account of Marlene Muster<");
    }

    private static String render(Locale locale, Long userId, Long personId, String personNiceName, Integer year) {

        final MockServletContext servletContext = new MockServletContext();
        final JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(servletContext);
        final MockHttpServletRequest request = new MockHttpServletRequest(servletContext);
        final MockHttpServletResponse response = new MockHttpServletResponse();

        final WebContext context = new WebContext(application.buildExchange(request, response), locale);
        context.setVariable("userId", userId);
        context.setVariable("personId", personId);
        context.setVariable("personNiceName", personNiceName);
        context.setVariable("year", year);

        return templateEngine.process(USAGE, context);
    }
}
