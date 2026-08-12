package org.synyx.urlaubsverwaltung.branding;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.util.Locale;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Renders the page-title fragment with the very expressions the page templates use, to make sure
 * message expressions with arguments survive being passed as a fragment parameter.
 */
class PageTitleFragmentTest {

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
    void ensureSimpleMessageIsSuffixedWithApplicationName() {

        final Context context = new Context(Locale.GERMAN);
        context.setVariable("applicationName", "Abwesenheiten");

        final String title = render("#{settings.header.title}", context);

        assertThat(title).isEqualTo("Einstellungen – Abwesenheiten");
    }

    @Test
    void ensureMessageWithSeveralArgumentsSurvivesAsFragmentParameter() {

        final Context context = new Context(Locale.GERMAN);
        context.setVariable("applicationName", "Abwesenheiten");
        context.setVariable("name", "Marlene Muster");
        context.setVariable("year", 2022);

        final String title = render("#{overview.header.title(${name}, ${year})}", context);

        assertThat(title).isEqualTo("Übersicht von Marlene Muster für 2022 – Abwesenheiten");
    }

    @Test
    void ensureNestedMessageWithPreprocessingSurvivesAsFragmentParameter() {

        final Context context = new Context(Locale.GERMAN);
        context.setVariable("applicationName", "Abwesenheiten");
        context.setVariable("messageKey", "sicknote.type.SICK_NOTE");
        context.setVariable("name", "Marlene Muster");

        final String title = render("#{sicknote.header.title(#{__${messageKey}__}, ${name})}", context);

        assertThat(title).isEqualTo("Krankmeldung von Marlene Muster – Abwesenheiten");
    }

    @Test
    void ensureConditionalExpressionSurvivesAsFragmentParameter() {

        final Context context = new Context(Locale.GERMAN);
        context.setVariable("applicationName", "Abwesenheiten");
        context.setVariable("editMode", true);

        final String title = render("${editMode} ? #{sicknote.edit.header.title} : #{sicknote.create.header.title}", context);

        assertThat(title).isEqualTo("Krankmeldung bearbeiten – Abwesenheiten");
    }

    @Test
    void ensureApplicationNameIsHtmlEscaped() {

        final Context context = new Context(Locale.GERMAN);
        context.setVariable("applicationName", "Ürlaub & Co <b>");

        final String title = render("#{settings.header.title}", context);

        assertThat(title).isEqualTo("Einstellungen – Ürlaub &amp; Co &lt;b&gt;");
    }

    @Test
    void ensureTitleWithoutSuffixWhenApplicationNameIsMissing() {

        final Context context = new Context(Locale.GERMAN);

        final String title = render("#{settings.header.title}", context);

        assertThat(title).isEqualTo("Einstellungen");
    }

    /**
     * @return the text of the rendered title element
     */
    private static String render(String titleExpression, Context context) {
        final String template = "<title th:replace=\"~{fragments/page-title::page-title(%s)}\"></title>".formatted(titleExpression);
        final String html = templateEngine.process(template, context);
        return html.substring(html.indexOf('>') + 1, html.indexOf("</title>"));
    }
}
