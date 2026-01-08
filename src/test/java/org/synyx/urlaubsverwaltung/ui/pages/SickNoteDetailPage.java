package org.synyx.urlaubsverwaltung.ui.pages;

import com.microsoft.playwright.Page;
import org.springframework.context.MessageSource;

import java.time.LocalDate;
import java.util.Locale;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
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

    public void waitForVisible() {
        page.waitForSelector(PERSON_SELECTOR);
        page.waitForSelector(TYPE_SELECTOR);
    }

    public void showsSickNoteForPerson(String name) {
        final String typeText = messageSource.getMessage("application.data.sicknotetype.sicknote", new Object[]{}, locale);
        assertThat(page.locator(PERSON_SELECTOR)).containsText(name);
        assertThat(page.locator(TYPE_SELECTOR)).hasText(typeText);
    }

    public void showsChildSickNoteForPerson(String name) {
        final String typeText = messageSource.getMessage("application.data.sicknotetype.sicknotechild", new Object[]{}, locale);
        assertThat(page.locator(PERSON_SELECTOR)).containsText(name);
        assertThat(page.locator(TYPE_SELECTOR)).hasText(typeText);
    }

    public void showsSickNoteDateFrom(LocalDate dateFrom) {
        final String expectedDateString = ofPattern("dd.MM.yyyy").format(dateFrom);
        assertThat(page.locator(DATE_SELECTOR)).containsText(expectedDateString);
    }

    public void showsSickNoteDateTo(LocalDate dateTo) {
        final String expectedDateString = ofPattern("dd.MM.yyyy").format(dateTo);
        assertThat(page.locator(DATE_SELECTOR)).containsText(expectedDateString);
    }

    public void showsSickNoteAubDateFrom(LocalDate aubDateFrom) {
        final String expectedDateString = ofPattern("dd.MM.yyyy").format(aubDateFrom);
        assertThat(page.locator(AUB_DATE_SELECTOR)).containsText(expectedDateString);
    }

    public void showsSickNoteAubDateTo(LocalDate aubDateTo) {
        final String expectedDateString = ofPattern("dd.MM.yyyy").format(aubDateTo);
        assertThat(page.locator(AUB_DATE_SELECTOR)).containsText(expectedDateString);
    }

    public void showsNoIncapacityCertificate() {
        final String notPresentText = messageSource.getMessage("sicknote.data.aub.notPresent", new Object[]{}, locale);
        assertThat(page.locator(AUB_DATE_SELECTOR)).hasText(notPresentText);
    }
}
