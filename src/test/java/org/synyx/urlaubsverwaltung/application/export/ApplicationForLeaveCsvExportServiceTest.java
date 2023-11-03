package org.synyx.urlaubsverwaltung.application.export;

import com.opencsv.CSVWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationForLeave;
import org.synyx.urlaubsverwaltung.application.vacationtype.ProvidedVacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.math.BigDecimal.TEN;
import static java.util.Locale.JAPANESE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;

@ExtendWith(MockitoExtension.class)
class ApplicationForLeaveCsvExportServiceTest {

    private ApplicationForLeaveCsvExportService sut;

    @Mock
    private CSVWriter csvWriter;
    @Mock
    private MessageSource messageSource;
    @Mock
    private WorkDaysCountService workDaysCountService;

    @BeforeEach
    void setUp() {
        sut = new ApplicationForLeaveCsvExportService(messageSource);
    }

    @Test
    void writeApplicationForLeaveExports() {

        final Locale locale = JAPANESE;

        final LocalDate startDate = LocalDate.parse("2018-01-01");
        final LocalDate endDate = LocalDate.parse("2018-12-31");
        final FilterPeriod period = new FilterPeriod(startDate, endDate);

        final Person person = new Person();
        person.setId(1L);
        person.setFirstName("personOneFirstName");
        person.setLastName("personOneLastName");

        final VacationType<?> vacationType = ProvidedVacationType.builder(messageSource)
            .id(1L)
            .category(HOLIDAY)
            .visibleToEveryone(true)
            .messageKey("messagekey.holiday")
            .build();

        final Application application = new Application();
        application.setId(42L);
        application.setPerson(person);
        application.setStartDate(startDate);
        application.setEndDate(endDate);
        application.setDayLength(DayLength.FULL);
        application.setStatus(ALLOWED);
        application.setVacationType(vacationType);

        when(workDaysCountService.getWorkDaysCount(application.getDayLength(), application.getStartDate(), application.getEndDate(), application.getPerson())).thenReturn(TEN);

        final ApplicationForLeave applicationForLeave = new ApplicationForLeave(application, workDaysCountService);

        final List<ApplicationForLeaveExport> applicationForLeaveExports = new ArrayList<>();
        final ApplicationForLeaveExport applicationForLeaveExport = new ApplicationForLeaveExport("1", person.getFirstName(), person.getLastName(), List.of(applicationForLeave), List.of("departmentA"));
        applicationForLeaveExports.add(applicationForLeaveExport);

        addMessageSource("person.account.basedata.personnelNumber", locale);
        addMessageSource("person.data.firstName", locale);
        addMessageSource("person.data.lastName", locale);
        addMessageSource("applications.export.departments", locale);
        addMessageSource("applications.export.from", locale);
        addMessageSource("applications.export.to", locale);
        addMessageSource("applications.export.length", locale);
        addMessageSource("applications.export.type", locale);
        addMessageSource("applications.export.days", locale);
        addMessageSource("FULL", locale);
        addMessageSource("messagekey.holiday", locale);

        sut.write(period, locale, applicationForLeaveExports, csvWriter);
        verify(csvWriter).writeNext(new String[]{"{person.account.basedata.personnelNumber}", "{person.data.firstName}", "{person.data.lastName}", "{applications.export.departments}", "{applications.export.from}", "{applications.export.to}", "{applications.export.length}", "{applications.export.type}", "{applications.export.days}"});
        verify(csvWriter).writeNext(new String[]{"1", "personOneFirstName", "personOneLastName", "departmentA", "2018/01/01", "2018/12/31", "{FULL}", "{messagekey.holiday}", "10"});
    }

    @Test
    void getFileNameWithoutWhitespace() {

        final Locale locale = JAPANESE;

        final LocalDate startDate = LocalDate.parse("2018-01-01");
        final LocalDate endDate = LocalDate.parse("2018-12-31");
        final FilterPeriod period = new FilterPeriod(startDate, endDate);

        when(messageSource.getMessage("applications.export.filename", new String[]{}, locale)).thenReturn("test filename");

        final String fileName = sut.fileName(period, locale);
        assertThat(fileName).startsWith("test-filename_");
    }

    private void addMessageSource(String key, Locale locale) {
        when(messageSource.getMessage(eq(key), any(), eq(locale))).thenReturn(String.format("{%s}", key));
    }
}
