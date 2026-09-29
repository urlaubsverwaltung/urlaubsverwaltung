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

import static java.math.BigDecimal.TEN;
import static java.util.Locale.ENGLISH;
import static java.util.Locale.GERMAN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RemainingVacationDaysCsvExportServiceTest {

    private static final FilterPeriod YEAR_2027 = new FilterPeriod(LocalDate.of(2027, 1, 1), LocalDate.of(2027, 12, 31));

    private RemainingVacationDaysCsvExportService sut;

    @Mock
    private CSVWriter csvWriter;
    @Mock
    private MessageSource messageSource;

    @BeforeEach
    void setUp() {
        sut = new RemainingVacationDaysCsvExportService(messageSource);
    }

    @Test
    void writesHeaderAndRowWithAllValues() {
        addHeaderMessages(GERMAN);

        final RemainingVacationDaysCsvRow row = new RemainingVacationDaysCsvRow("42", "Franka", "Potente", List.of("Entwicklung", "Marketing"), new BigDecimal("2.5"), new BigDecimal("0.5"), LocalDate.of(2027, 4, 1));

        sut.write(YEAR_2027, GERMAN, List.of(row), csvWriter);

        verify(csvWriter).writeNext(new String[]{
            "{person.account.basedata.personnelNumber}", "{person.data.firstName}", "{person.data.lastName}",
            "{applications.export.departments}", "{account.remaining_vacation_days.export.remainingVacationDays}",
            "{account.remaining_vacation_days.export.remainingVacationDaysNotExpiring}", "{account.remaining_vacation_days.export.expiryDate}"
        });
        verify(csvWriter).writeNext(new String[]{"42", "Franka", "Potente", "Entwicklung, Marketing", "2,5", "0,5", "01.04.2027"});
    }

    @Test
    void writesNumbersAndDateInTheGivenLocale() {
        addHeaderMessages(ENGLISH);

        final RemainingVacationDaysCsvRow row = new RemainingVacationDaysCsvRow("42", "Franka", "Potente", List.of(), new BigDecimal("2.5"), new BigDecimal("0.5"), LocalDate.of(2027, 4, 1));

        sut.write(YEAR_2027, ENGLISH, List.of(row), csvWriter);

        verify(csvWriter).writeNext(new String[]{"42", "Franka", "Potente", "", "2.5", "0.5", "Apr 1, 2027"});
    }

    @Test
    void writesEmptyCellsForMissingPersonnelNumberAndDepartments() {
        addHeaderMessages(GERMAN);

        final RemainingVacationDaysCsvRow row = new RemainingVacationDaysCsvRow("", "Franka", "Potente", List.of(), TEN, BigDecimal.ZERO, LocalDate.of(2027, 4, 1));

        sut.write(YEAR_2027, GERMAN, List.of(row), csvWriter);

        verify(csvWriter).writeNext(new String[]{"", "Franka", "Potente", "", "10", "0", "01.04.2027"});
    }

    @Test
    void writesAllRemainingDaysAsNotExpiringWhenRemainingDaysDoNotExpire() {
        addHeaderMessages(GERMAN);

        final RemainingVacationDaysCsvRow row = new RemainingVacationDaysCsvRow("42", "Franka", "Potente", List.of(), TEN, TEN, null);

        sut.write(YEAR_2027, GERMAN, List.of(row), csvWriter);

        verify(csvWriter).writeNext(new String[]{"42", "Franka", "Potente", "", "10", "10", ""});
    }

    @Test
    void fileNameContainsTranslatedNameYearAndLanguage() {
        when(messageSource.getMessage(eq("account.remaining_vacation_days.export.filename"), any(), eq(GERMAN))).thenReturn("Resturlaub Export");

        assertThat(sut.fileName(YEAR_2027, GERMAN)).isEqualTo("Resturlaub-Export_2027_de.csv");
    }

    private void addHeaderMessages(Locale locale) {
        for (String key : List.of("person.account.basedata.personnelNumber", "person.data.firstName", "person.data.lastName",
            "applications.export.departments", "account.remaining_vacation_days.export.remainingVacationDays",
            "account.remaining_vacation_days.export.remainingVacationDaysNotExpiring", "account.remaining_vacation_days.export.expiryDate")) {
            when(messageSource.getMessage(eq(key), any(), eq(locale))).thenReturn("{%s}".formatted(key));
        }
    }
}
