package org.synyx.urlaubsverwaltung.availability.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.CANCELLED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.REJECTED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.REVOKED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.availability.api.TimedAbsence.Type.VACATION;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;
import static org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator.createApplication;
import static org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator.createPerson;

@RunWith(MockitoJUnitRunner.class)
public class VacationAbsenceProviderTest {

    private VacationAbsenceProvider sut;

    @Mock
    private ApplicationService applicationService;

    @Before
    public void setUp() {
        sut = new VacationAbsenceProvider(applicationService);
    }

    @Test
    public void ensurePersonIsNotAvailableOnVacationFullDayWaiting() {

        final Person person = createPerson();
        final Instant fullDayVacationDate = Instant.from(LocalDate.of(2016, 1, 4));
        final Application fullDayApplication = createApplication(person, fullDayVacationDate, fullDayVacationDate, FULL);

        when(applicationService.getApplicationsForACertainPeriodAndPerson(fullDayVacationDate, fullDayVacationDate, person))
            .thenReturn(List.of(fullDayApplication));

        final TimedAbsenceSpans updatedTimedAbsenceSpans = sut.checkForAbsence(new TimedAbsenceSpans(new ArrayList<>()),
            person, fullDayVacationDate);

        List<TimedAbsence> absencesList = updatedTimedAbsenceSpans.getAbsencesList();
        assertThat(absencesList).hasSize(1);
        assertThat(absencesList.get(0).getType()).isEqualTo(VACATION);
        assertThat(absencesList.get(0).getPartOfDay()).isEqualTo(FULL.name());
        assertThat(absencesList.get(0).getRatio()).isEqualTo(BigDecimal.valueOf(1.0));
    }

    @Test
    public void ensurePersonIsNotAvailableOnTemporaryAllowed() {

        final Person person = createPerson();
        final Instant fullDayVacationDate = Instant.from(LocalDate.of(2016, 1, 4));
        final Application fullDayApplication = createApplication(person, fullDayVacationDate, fullDayVacationDate, FULL);
        fullDayApplication.setStatus(TEMPORARY_ALLOWED);

        when(applicationService.getApplicationsForACertainPeriodAndPerson(fullDayVacationDate, fullDayVacationDate, person))
            .thenReturn(List.of(fullDayApplication));

        final TimedAbsenceSpans updatedTimedAbsenceSpans = sut.checkForAbsence(new TimedAbsenceSpans(new ArrayList<>()),
            person, fullDayVacationDate);

        List<TimedAbsence> absencesList = updatedTimedAbsenceSpans.getAbsencesList();
        assertThat(absencesList).hasSize(1);
        assertThat(absencesList.get(0).getType()).isEqualTo(VACATION);
        assertThat(absencesList.get(0).getPartOfDay()).isEqualTo(FULL.name());
        assertThat(absencesList.get(0).getRatio()).isEqualTo(BigDecimal.valueOf(1.0));
    }

    @Test
    public void ensurePersonIsNotAvailableOnAllowed() {

        final Person person = createPerson();
        final Instant fullDayVacationDate = Instant.from(LocalDate.of(2016, 1, 4));
        final Application fullDayApplication = createApplication(person, fullDayVacationDate, fullDayVacationDate, FULL);
        fullDayApplication.setStatus(ALLOWED);

        when(applicationService.getApplicationsForACertainPeriodAndPerson(fullDayVacationDate, fullDayVacationDate, person))
            .thenReturn(List.of(fullDayApplication));

        final TimedAbsenceSpans updatedTimedAbsenceSpans = sut.checkForAbsence(new TimedAbsenceSpans(new ArrayList<>()),
            person, fullDayVacationDate);

        List<TimedAbsence> absencesList = updatedTimedAbsenceSpans.getAbsencesList();
        assertThat(absencesList).hasSize(1);
        assertThat(absencesList.get(0).getType()).isEqualTo(VACATION);
        assertThat(absencesList.get(0).getPartOfDay()).isEqualTo(FULL.name());
        assertThat(absencesList.get(0).getRatio()).isEqualTo(BigDecimal.valueOf(1.0));
    }

    @Test
    public void ensurePersonIsAvailableOnRejected() {

        final Person person = createPerson();
        final Instant fullDayVacationDate = Instant.from(LocalDate.of(2016, 1, 4));
        final Application fullDayApplication = createApplication(person, fullDayVacationDate, fullDayVacationDate, FULL);
        fullDayApplication.setStatus(REJECTED);

        when(applicationService.getApplicationsForACertainPeriodAndPerson(fullDayVacationDate, fullDayVacationDate, person))
            .thenReturn(List.of(fullDayApplication));

        final TimedAbsenceSpans updatedTimedAbsenceSpans = sut.checkForAbsence(new TimedAbsenceSpans(new ArrayList<>()),
            person, fullDayVacationDate);

        List<TimedAbsence> absencesList = updatedTimedAbsenceSpans.getAbsencesList();
        assertThat(absencesList).hasSize(0);
    }

    @Test
    public void ensurePersonIsAvailableOnCancelled() {

        final Person person = createPerson();
        final Instant fullDayVacationDate = Instant.from(LocalDate.of(2016, 1, 4));
        final Application fullDayApplication = createApplication(person, fullDayVacationDate, fullDayVacationDate, FULL);
        fullDayApplication.setStatus(CANCELLED);

        when(applicationService.getApplicationsForACertainPeriodAndPerson(fullDayVacationDate, fullDayVacationDate, person))
            .thenReturn(List.of(fullDayApplication));

        final TimedAbsenceSpans updatedTimedAbsenceSpans = sut.checkForAbsence(new TimedAbsenceSpans(new ArrayList<>()),
            person, fullDayVacationDate);

        List<TimedAbsence> absencesList = updatedTimedAbsenceSpans.getAbsencesList();
        assertThat(absencesList).hasSize(0);
    }

    @Test
    public void ensurePersonIsAvailableOnRevoked() {

        final Person person = createPerson();
        final Instant fullDayVacationDate = Instant.from(LocalDate.of(2016, 1, 4));
        final Application fullDayApplication = createApplication(person, fullDayVacationDate, fullDayVacationDate, FULL);
        fullDayApplication.setStatus(REVOKED);

        when(applicationService.getApplicationsForACertainPeriodAndPerson(fullDayVacationDate, fullDayVacationDate, person))
            .thenReturn(List.of(fullDayApplication));

        final TimedAbsenceSpans updatedTimedAbsenceSpans = sut.checkForAbsence(new TimedAbsenceSpans(new ArrayList<>()),
            person, fullDayVacationDate);

        List<TimedAbsence> absencesList = updatedTimedAbsenceSpans.getAbsencesList();
        assertThat(absencesList).hasSize(0);
    }

    @Test
    public void ensurePersonIsNotAvailableOnVacationOneHalfDay() {

        final Person testPerson = createPerson();
        final Instant twoHalfDayVacationsDate = Instant.from(LocalDate.of(2016, 1, 5));
        final Application halfMorningDayApplication = createApplication(testPerson, twoHalfDayVacationsDate, twoHalfDayVacationsDate, MORNING);

        when(applicationService.getApplicationsForACertainPeriodAndPerson(twoHalfDayVacationsDate, twoHalfDayVacationsDate, testPerson))
            .thenReturn(List.of(halfMorningDayApplication));

        final TimedAbsenceSpans updatedTimedAbsenceSpans = sut.checkForAbsence(new TimedAbsenceSpans(new ArrayList<>()),
            testPerson, twoHalfDayVacationsDate);

        List<TimedAbsence> absencesList = updatedTimedAbsenceSpans.getAbsencesList();
        assertThat(absencesList).hasSize(1);
        assertThat(absencesList.get(0).getType()).isEqualTo(VACATION);
        assertThat(absencesList.get(0).getPartOfDay()).isEqualTo(MORNING.name());
        assertThat(absencesList.get(0).getRatio()).isEqualTo(BigDecimal.valueOf(0.5));
    }

    @Test
    public void ensurePersonIsNotAvailableOnVacationTwoHalfDays() {

        final Person testPerson = createPerson();
        final Instant twoHalfDayVacationsDate = Instant.from(LocalDate.of(2016, 1, 5));
        final Application halfMorningDayApplication = createApplication(testPerson, twoHalfDayVacationsDate, twoHalfDayVacationsDate, MORNING);
        final Application halfNoonDayApplication = createApplication(testPerson, twoHalfDayVacationsDate, twoHalfDayVacationsDate, NOON);

        when(applicationService.getApplicationsForACertainPeriodAndPerson(twoHalfDayVacationsDate, twoHalfDayVacationsDate, testPerson))
            .thenReturn(List.of(halfMorningDayApplication, halfNoonDayApplication));

        final TimedAbsenceSpans updatedTimedAbsenceSpans = sut.checkForAbsence(new TimedAbsenceSpans(new ArrayList<>()),
            testPerson, twoHalfDayVacationsDate);

        List<TimedAbsence> absencesList = updatedTimedAbsenceSpans.getAbsencesList();
        assertThat(absencesList).hasSize(2);
        assertThat(absencesList.get(0).getType()).isEqualTo(VACATION);
        assertThat(absencesList.get(0).getPartOfDay()).isEqualTo(MORNING.name());
        assertThat(absencesList.get(0).getRatio()).isEqualTo(BigDecimal.valueOf(0.5));
        assertThat(absencesList.get(1).getType()).isEqualTo(VACATION);
        assertThat(absencesList.get(1).getPartOfDay()).isEqualTo(NOON.name());
        assertThat(absencesList.get(1).getRatio()).isEqualTo(BigDecimal.valueOf(0.5));
    }

    @Test
    public void ensureReturnsGiveAbsenceSpansIfNoVacationFound() {

        final Person person = createPerson();
        final Instant vacationDay = Instant.from(LocalDate.of(2016, 1, 4));
        final TimedAbsenceSpans emptyTimedAbsenceSpans = new TimedAbsenceSpans(new ArrayList<>());

        final TimedAbsenceSpans updatedTimedAbsenceSpans = sut.checkForAbsence(emptyTimedAbsenceSpans,
            person, vacationDay);
        assertThat(updatedTimedAbsenceSpans).isEqualTo(emptyTimedAbsenceSpans);
    }
}
