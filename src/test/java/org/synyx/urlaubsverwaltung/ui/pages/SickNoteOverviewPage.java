package org.synyx.urlaubsverwaltung.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.springframework.context.MessageSource;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;

public class SickNoteOverviewPage {

    private static final String TABLE_SELECTOR = "[data-test-id=sick-notes-table]";

    private final Page page;
    private final Locale locale;
    private final MessageSource messageSource;

    public SickNoteOverviewPage(Page page, MessageSource messageSource, Locale locale) {
        this.page = page;
        this.locale = locale;
        this.messageSource = messageSource;
    }

    public boolean showsSickNoteStatistic(String firstName, String lastName, int sickDays, int daysWithIncapacityCertificate) {
        final String sickDaysText = messageSource.getMessage("sicknotes.daysOverview.sickDays.number", new Object[]{}, locale);
        final String sickDaysAubText = messageSource.getMessage("overview.sicknotes.sickdays.aub", new Object[]{daysWithIncapacityCertificate}, locale);

        Predicate<String> hasSickDaysText = text -> text.contains(sickDays + " " + sickDaysText);
        Predicate<String> hasSickDaysCertificate = text -> text.contains(sickDaysAubText);

        if (daysWithIncapacityCertificate == 0) {
            hasSickDaysCertificate = Predicate.not(hasSickDaysCertificate);
        }

        return rowWithPerson(firstName, lastName)
            .map(Locator::textContent)
            .map(SickNoteOverviewPage::cleanupTextContent)
            .filter(hasSickDaysText.and(hasSickDaysCertificate))
            .isPresent();
    }

    public boolean showsChildSickNoteStatistic(String firstName, String lastName, int sickDays, int daysWithIncapacityCertificate) {
        final String sickDaysText = messageSource.getMessage("sicknotes.daysOverview.sickDays.child.number", new Object[]{}, locale);
        final String sickDaysAubText = messageSource.getMessage("overview.sicknotes.sickdays.aub", new Object[]{daysWithIncapacityCertificate}, locale);

        Predicate<String> hasSickDaysText = textContent -> textContent.contains(sickDays + " " + sickDaysText);
        Predicate<String> hasSickDaysCertificate = textContent -> textContent.contains(sickDaysAubText);

        if (daysWithIncapacityCertificate == 0) {
            hasSickDaysCertificate = Predicate.not(hasSickDaysCertificate);
        }

        return rowWithPerson(firstName, lastName)
            .map(Locator::textContent)
            .map(SickNoteOverviewPage::cleanupTextContent)
            .filter(hasSickDaysText.and(hasSickDaysCertificate))
            .isPresent();
    }

    private static String cleanupTextContent(String text) {
        // remove new lines from textContent (<br /> are new lines for instance)
        return text.replaceAll("\\n", "")
            // remove tabs from textContent (don't know where tabs come from)
            .replaceAll("\\t", "")
            // and remove multiple whitespace characters
            .replaceAll(" {2,}"," ");
    }

    private Optional<Locator> rowWithPerson(String firstName, String lastName) {

        final Locator table = page.locator(TABLE_SELECTOR);
        final List<Locator> tableRows = table.locator("tr").all();

        for (Locator tableRow : tableRows) {
            if (tableRow.textContent().contains(firstName) && tableRow.textContent().contains(lastName)) {
                return Optional.of(tableRow);
            }
        }

        return Optional.empty();
    }
}
