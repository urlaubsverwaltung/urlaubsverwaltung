package org.synyx.urlaubsverwaltung.overtime.statistics;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static java.time.Duration.ZERO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;

/**
 * The company wide balance has to be the same figure every person sees as their own remaining overtime. Both sides are
 * calculated by completely different code, so the identity is verified against a real database instead of mocks.
 */
@SpringBootTest
@Transactional
class OvertimeStatisticsServiceIT extends SingleTenantTestContainersBase {

    @Autowired
    private OvertimeStatisticsService sut;
    @Autowired
    private OvertimeService overtimeService;
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private VacationTypeService vacationTypeService;
    @Autowired
    private PersonService personService;

    @Test
    void ensureBalanceEqualsTheSummedRemainingOvertimeOfEveryPerson() {

        final Person marie = personService.create("marie", "Marie", "Reichenbach", "marie@example.org");
        final Person klaus = personService.create("klaus", "Klaus", "Mustermann", "klaus@example.org");

        overtime(marie, "2024-02-01", Duration.ofHours(12));
        overtime(marie, "2024-06-01", Duration.ofHours(5).negated());
        overtime(klaus, "2025-09-01", Duration.ofHours(8));
        overtimeReductionApplication(marie, "2025-03-03", Duration.ofHours(6));

        final Duration summedPersonalBalances = List.of(marie, klaus).stream()
            .map(overtimeService::getLeftOvertimeForPerson)
            .reduce(ZERO, Duration::plus);

        assertThat(sut.getTotals().balance()).isEqualTo(summedPersonalBalances);
    }

    @Test
    void ensureAccrualAndReductionAddUpToTheBalance() {

        final Person marie = personService.create("marie", "Marie", "Reichenbach", "marie@example.org");

        overtime(marie, "2024-02-01", Duration.ofHours(12));
        overtime(marie, "2024-06-01", Duration.ofHours(5).negated());
        overtimeReductionApplication(marie, "2025-03-03", Duration.ofHours(6));

        final OvertimeTotals totals = sut.getTotals();

        assertThat(totals.accrued()).isEqualTo(Duration.ofHours(12));
        assertThat(totals.reduction()).isEqualTo(Duration.ofHours(11));
        assertThat(totals.balance()).isEqualTo(Duration.ofHours(1));
    }

    @Test
    void ensureAnEmptyCompanyDoesNotFail() {
        assertThat(sut.getTotals().balance()).isEqualTo(ZERO);
    }

    private void overtime(Person person, String date, Duration duration) {
        final LocalDate day = LocalDate.parse(date);
        overtimeService.createOvertime(person.getIdAsPersonId(), new DateRange(day, day), duration, person.getIdAsPersonId(), null);
    }

    private void overtimeReductionApplication(Person person, String date, Duration hours) {

        final VacationType<?> overtimeType = vacationTypeService.getActiveVacationTypes().stream()
            .filter(vacationType -> vacationType.isOfCategory(OVERTIME))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("expected an active vacation type of category overtime"));

        final LocalDate day = LocalDate.parse(date);

        final Application application = new Application();
        application.setPerson(person);
        application.setStartDate(day);
        application.setEndDate(day);
        application.setDayLength(FULL);
        application.setStatus(ALLOWED);
        application.setVacationType(overtimeType);
        application.setHours(hours);

        applicationService.save(application);
    }
}
