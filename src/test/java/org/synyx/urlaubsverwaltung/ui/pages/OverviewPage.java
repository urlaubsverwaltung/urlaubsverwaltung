package org.synyx.urlaubsverwaltung.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.springframework.context.MessageSource;

import java.time.LocalDate;
import java.util.Locale;
import java.util.regex.Pattern;

public class OverviewPage {

    public static final Pattern URL_PATTERN = Pattern.compile("/web/person/\\d+/overview");

    private static final Pattern REGEX_METACHARACTER = Pattern.compile("[.\\[\\]{}()*+?^$|\\\\]");

    private final Page page;
    private final MessageSource messageSource;
    private final Locale locale;

    public OverviewPage(Page page, MessageSource messageSource, Locale locale) {
        this.page = page;
        this.messageSource = messageSource;
        this.locale = locale;
    }

    /**
     * The page title is suffixed with the configurable application name. Therefore the returned pattern matches a
     * substring of the title only.
     *
     * <p>Note that the pattern is evaluated by the browser. Therefore {@link Pattern#quote(String)} cannot be used to
     * escape the text, `\Q...\E` is java syntax which is unknown to javascript regular expressions.
     */
    public Pattern getExpectedPageTitlePattern(String username, int year) {
        final String title = messageSource.getMessage("overview.header.title", new Object[]{username, year}, locale);
        return Pattern.compile(REGEX_METACHARACTER.matcher(title).replaceAll("\\\\$0"));
    }

    public void selectDateRange(LocalDate startDate, LocalDate endDate) {
        dayLocator(startDate).hover();
        page.mouse().down();
        dayLocator(endDate).hover();
        page.mouse().up();
    }

    public void clickDay(LocalDate date) {
        dayLocator(date).click();
    }

    private Locator dayLocator(LocalDate date) {
        final String dayName = "%s%02d".formatted(date.getMonth().getDisplayName(java.time.format.TextStyle.SHORT, locale), date.getDayOfMonth());
        return page.getByRole(AriaRole.LISTITEM).filter(new Locator.FilterOptions().setHasText(dayName)).locator("div");
    }
}
