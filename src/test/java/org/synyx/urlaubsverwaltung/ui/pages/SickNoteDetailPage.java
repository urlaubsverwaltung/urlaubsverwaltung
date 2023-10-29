package org.synyx.urlaubsverwaltung.ui.pages;

import com.microsoft.playwright.Page;
import org.springframework.context.MessageSource;

import java.time.LocalDate;
import java.util.Locale;

import static java.time.format.DateTimeFormatter.ofPattern;

public class SickNoteDetailPage {

    private static final String PERSON_SELECTOR = "[data-test-id=sicknote-person]";
    private static final String TYPE_SELECTOR = "[data-test-id=sicknote-type]";
    private static final String DATE_SELECTOR = "[data-test-id=sicknote-date]";
    private static final String AUB_DATE_SELECTOR = "[data-test-id=sicknote-aub-date]";

    private final Page page;
    private final MessageSource messageSource;
    private final Locale locale;

    public SickNoteDetailPage(Page page, MessageSource messageSource, Locale locale) {
        this.page = page;
        this.messageSource = messageSource;
        this.locale = locale;
    }

    public boolean showsSickNoteForPerson(String name) {
        final String typeText = messageSource.getMessage("application.data.sicknotetype.sicknote", new Object[]{}, locale);

        return page.locator(PERSON_SELECTOR).textContent().contains(name)
            && page.locator(TYPE_SELECTOR).textContent().contains(typeText);
    }

    public boolean showsChildSickNoteForPerson(String name) {
        final String typeText = messageSource.getMessage("application.data.sicknotetype.sicknotechild", new Object[]{}, locale);
        return page.locator(PERSON_SELECTOR).textContent().contains(name)
            && page.locator(TYPE_SELECTOR).textContent().contains(typeText);
    }

    public boolean showsSickNoteDateFrom(LocalDate dateFrom) {
        final String expectedDateString = ofPattern("dd.MM.yyyy").format(dateFrom);
        return page.locator(DATE_SELECTOR).textContent().contains(expectedDateString);
    }

    public boolean showsSickNoteDateTo(LocalDate dateTo) {
        final String expectedDateString = ofPattern("dd.MM.yyyy").format(dateTo);
        return page.locator(DATE_SELECTOR).textContent().contains(expectedDateString);
    }

    public boolean showsSickNoteAubDateFrom(LocalDate aubDateFrom) {
        final String expectedDateString = ofPattern("dd.MM.yyyy").format(aubDateFrom);
        return page.locator(AUB_DATE_SELECTOR).textContent().contains(expectedDateString);
    }

    public boolean showsSickNoteAubDateTo(LocalDate aubDateTo) {
        final String expectedDateString = ofPattern("dd.MM.yyyy").format(aubDateTo);
        return page.locator(AUB_DATE_SELECTOR).textContent().contains(expectedDateString);
    }

    public boolean showsNoIncapacityCertificate() {
        final String notPresentText = messageSource.getMessage("sicknote.data.aub.notPresent", new Object[]{}, locale);
        return page.locator(AUB_DATE_SELECTOR).textContent().contains(notPresentText);
    }
}
