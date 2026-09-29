package org.synyx.urlaubsverwaltung.web;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Renders the year selector fragment the overview and statistics pages share, so that all of them mark the selected
 * year and the current year the same way.
 */
class YearSelectorFragmentTest {

    private static final String USAGE = "<div th:replace=\"~{fragments/year-selector::year-selector(id='year-selection', currentYear=${currentYear}, hrefPrefix='/web/statistics?year=', selectedYear=${selectedYear})}\"></div>";

    private static final String CHECK_ICON = "<polyline points=\"20 6 9 17 4 12\">";

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
    void ensureSelectedYearIsMarkedWithCheckIconAndAriaCurrent() {

        final List<String> items = renderItems(Locale.GERMAN, 2026, 2020);

        assertThat(item(items, 2020)).contains("aria-current=\"page\"", "font-semibold", CHECK_ICON);
        assertThat(items).filteredOn(item -> !item.contains("?year=2020\""))
            .hasSize(10)
            .allSatisfy(item -> assertThat(item).doesNotContain("aria-current=\"page\"", "font-semibold", CHECK_ICON));
    }

    @Test
    void ensureCurrentYearIsMarkedWithBadge() {

        final List<String> items = renderItems(Locale.GERMAN, 2026, 2020);

        assertThat(item(items, 2026)).contains(">dieses Jahr<");
        assertThat(items).filteredOn(item -> !item.contains("?year=2026\""))
            .hasSize(10)
            .allSatisfy(item -> assertThat(item).doesNotContain("dieses Jahr"));
    }

    @Test
    void ensureSelectedCurrentYearIsMarkedWithBoth() {

        final List<String> items = renderItems(Locale.GERMAN, 2026, 2026);

        assertThat(item(items, 2026)).contains("aria-current=\"page\"", CHECK_ICON, ">dieses Jahr<");
    }

    @Test
    void ensureCurrentYearBadgeIsTranslated() {

        final List<String> items = renderItems(Locale.ENGLISH, 2026, 2020);

        assertThat(item(items, 2026)).contains(">this year<");
    }

    private static List<String> renderItems(Locale locale, int currentYear, int selectedYear) {

        final Context context = new Context(locale);
        context.setVariable("currentYear", currentYear);
        context.setVariable("selectedYear", selectedYear);

        final String html = templateEngine.process(USAGE, context);
        return Arrays.stream(html.split("<li")).skip(1).toList();
    }

    private static String item(List<String> items, int year) {
        return items.stream()
            .filter(item -> item.contains("?year=" + year + "\""))
            .findFirst()
            .orElseThrow();
    }
}
