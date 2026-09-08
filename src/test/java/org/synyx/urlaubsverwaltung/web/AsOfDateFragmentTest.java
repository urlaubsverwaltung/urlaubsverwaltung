package org.synyx.urlaubsverwaltung.web;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;

import static java.time.Month.SEPTEMBER;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Renders the as-of-date fragment the statistics pages share, so that its label and its date format stay the same
 * on all of them.
 */
class AsOfDateFragmentTest {

    private static final String USAGE = "<div th:replace=\"~{fragments/as-of-date::as-of-date(${asOfDate})}\"></div>";

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
    void ensureLabelAndDateAreRendered() {

        final Context context = new Context(Locale.GERMAN);
        context.setVariable("asOfDate", LocalDate.of(2026, SEPTEMBER, 1));

        assertThat(templateEngine.process(USAGE, context)).contains("Stand", "01.09.2026");
    }

    @Test
    void ensureDateFormatDoesNotFollowTheLocale() {

        // the day-first format is part of the agreed output, an english page shows the same date as a german one

        final Context context = new Context(Locale.ENGLISH);
        context.setVariable("asOfDate", LocalDate.of(2026, SEPTEMBER, 1));

        assertThat(templateEngine.process(USAGE, context)).contains("Status", "01.09.2026");
    }
}
