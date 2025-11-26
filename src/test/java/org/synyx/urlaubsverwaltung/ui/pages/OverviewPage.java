package org.synyx.urlaubsverwaltung.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.springframework.context.MessageSource;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class OverviewPage {

    private final Page page;
    private final MessageSource messageSource;
    private final Locale locale;

    public OverviewPage(Page page, MessageSource messageSource, Locale locale) {
        this.page = page;
        this.messageSource = messageSource;
        this.locale = locale;
    }

    public boolean isVisibleForPerson(String username, int year) {
        final String titleText = messageSource.getMessage("overview.header.title", new Object[]{username, year}, locale);
        return page.title().contains(titleText);
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

        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM", locale);
        final String formatted = date.format(dateTimeFormatter);

        return page.getByRole(AriaRole.BUTTON).filter(new Locator.FilterOptions().setHasText(formatted));
    }
}
