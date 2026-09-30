package org.synyx.urlaubsverwaltung.account;

import com.opencsv.CSVWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import static java.util.Locale.ENGLISH;
import static java.util.Locale.GERMAN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpiredRemainingVacationDaysCsvExportServiceTest {

    private static final LocalDate APRIL_FIRST = LocalDate.of(2027, 4, 1);
    private static final FilterPeriod RUN_DAY = new FilterPeriod(APRIL_FIRST, APRIL_FIRST);

    private ExpiredRemainingVacationDaysCsvExportService sut;

    @Mock
    private CSVWriter csvWriter;
    @Mock
    private MessageSource messageSource;

    @BeforeEach
    void setUp() {
        sut = new ExpiredRemainingVacationDaysCsvExportService(messageSource);
    }

    @Test
    void writesHeaderAndRowWithAllValues() {
        addHeaderMessages(GERMAN);

        final ExpiredRemainingVacationDaysCsvRow row = new ExpiredRemainingVacationDaysCsvRow("42", "Franka", "Potente",
            List.of("Entwicklung", "Marketing"), APRIL_FIRST, new BigDecimal("2.5"), new BigDecimal("0.5"), new BigDecimal("12"));

        sut.write(RUN_DAY, GERMAN, List.of(row), csvWriter);

        verify(csvWriter).writeNext(new String[]{
            "{person.account.basedata.personnelNumber}", "{person.data.firstName}", "{person.data.lastName}",
            "{applications.export.departments}", "{account.remaining_vacation_days.export.expiryDate}",
            "{account.expired_remaining_vacation_days.export.expiredRemainingVacationDays}",
            "{account.expired_remaining_vacation_days.export.remainingVacationDaysNotExpiring}",
            "{account.expired_remaining_vacation_days.export.vacationDaysLeft}"
        });
        verify(csvWriter).writeNext(new String[]{"42", "Franka", "Potente", "Entwicklung, Marketing", "01.04.2027", "2,5", "0,5", "12"});
    }

    @Test
    void writesNumbersAndDateInTheGivenLocale() {
        addHeaderMessages(ENGLISH);

        final ExpiredRemainingVacationDaysCsvRow row = new ExpiredRemainingVacationDaysCsvRow("42", "Franka", "Potente",
            List.of(), APRIL_FIRST, new BigDecimal("2.5"), new BigDecimal("0.5"), new BigDecimal("12"));

        sut.write(RUN_DAY, ENGLISH, List.of(row), csvWriter);

        verify(csvWriter).writeNext(new String[]{"42", "Franka", "Potente", "", "Apr 1, 2027", "2.5", "0.5", "12"});
    }

    @Test
    void writesEmptyCellsForMissingPersonnelNumberAndDepartments() {
        addHeaderMessages(GERMAN);

        final ExpiredRemainingVacationDaysCsvRow row = new ExpiredRemainingVacationDaysCsvRow("", "Franka", "Potente",
            List.of(), APRIL_FIRST, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO);

        sut.write(RUN_DAY, GERMAN, List.of(row), csvWriter);

        verify(csvWriter).writeNext(new String[]{"", "Franka", "Potente", "", "01.04.2027", "1", "0", "0"});
    }

    @Test
    void fileNameContainsTranslatedNameDateAndLanguage() {
        when(messageSource.getMessage(eq("account.expired_remaining_vacation_days.export.filename"), any(), eq(GERMAN)))
            .thenReturn("Verfallener Resturlaub");

        assertThat(sut.fileName(RUN_DAY, GERMAN)).isEqualTo("Verfallener-Resturlaub_2027-04-01_de.csv");
    }

    private void addHeaderMessages(Locale locale) {
        for (String key : List.of("person.account.basedata.personnelNumber", "person.data.firstName", "person.data.lastName",
            "applications.export.departments", "account.remaining_vacation_days.export.expiryDate",
            "account.expired_remaining_vacation_days.export.expiredRemainingVacationDays",
            "account.expired_remaining_vacation_days.export.remainingVacationDaysNotExpiring",
            "account.expired_remaining_vacation_days.export.vacationDaysLeft")) {
            when(messageSource.getMessage(eq(key), any(), eq(locale))).thenReturn("{%s}".formatted(key));
        }
    }
}
