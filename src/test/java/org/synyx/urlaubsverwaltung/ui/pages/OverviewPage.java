package org.synyx.urlaubsverwaltung.ui.pages;

import com.microsoft.playwright.Page;
import org.springframework.context.MessageSource;

import java.util.Locale;

public class OverviewPage {

    private static final String DATEPICKER_SELECTOR = "#datepicker";

    private final Page page;
    private final MessageSource messageSource;
    private final Locale locale;

    public OverviewPage(Page page, MessageSource messageSource, Locale locale) {
        this.page = page;
        this.messageSource = messageSource;
        this.locale = locale;
    }

    public boolean isVisible() {
        return datepickerExists(page);
    }

    private static boolean datepickerExists(Page page) {
        return page.locator(DATEPICKER_SELECTOR).isVisible();
    }

    public boolean isVisibleForPerson(String username, int year) {
        final String titleText = messageSource.getMessage("overview.header.title", new Object[]{username, year}, locale);
        return page.title().contains(titleText);
    }
}
