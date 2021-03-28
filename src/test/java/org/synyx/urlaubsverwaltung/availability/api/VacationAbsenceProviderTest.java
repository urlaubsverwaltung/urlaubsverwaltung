package org.synyx.urlaubsverwaltung.availability.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createApplication;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.CANCELLED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.REJECTED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.REVOKED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;

@ExtendWith(MockitoExtension.class)
class VacationAbsenceProviderTest {

    private VacationAbsenceProvider sut;

    @Mock
    private ApplicationService applicationService;

    @BeforeEach
    void setUp() {
        sut = new VacationAbsenceProvider(applicationService);
    }

    @Test
    void ensurePersonIsNotAvailableOnVacationFullDayWaiting() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate fullDayVacationDate = LocalDate.of(2016, 1, 4);
        final Application fullDayApplication = createApplication(person, fullDayVacationDate, fullDayVacationDate, FULL);

        when(applicationService.getApplicationsForACertainPeriodAndPerson(fullDayVacationDate, fullDayVacationDate, person))
            .thenReturn(List.of(fullDayApplication));

        final TimedAbsenceSpans updatedTimedAbsenceSpans = sut.checkForAbsence(new TimedAbsenceSpans(new ArrayList<>()), person, fullDayVacationDate);
        assertThat(updatedTimedAbsenceSpans.getAbsencesList()).hasSize(1);
        assertThat(updatedTimedAbsenceSpans.getAbsencesList().get(0).getPartOfDay()).isEqualTo(FULL.name());
        assertThat(updatedTimedAbsenceSpans.getAbsencesList().get(0).getRatio()).isEqualTo(BigDecimal.ONE);
    }

    @Test
    void ensurePersonIsNotAvailableOnTemporaryAllowed() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate fullDayVacationDate = LocalDate.of(2016, 1, 4);
        final Application fullDayApplication = createApplication(person, fullDayVacationDate, fullDayVacationDate, FULL);
        fullDayApplication.setStatus(TEMPORARY_ALLOWED);

        when(applicationService.getApplicationsForACertainPeriodAndPerson(fullDayVacationDate, fullDayVacationDate, person))
            .thenReturn(List.of(fullDayApplication));

        final TimedAbsenceSpans updatedTimedAbsenceSpans = sut.checkForAbsence(new TimedAbsenceSpans(new ArrayList<>()), person, fullDayVacationDate);
        assertThat(updatedTimedAbsenceSpans.getAbsencesList()).hasSize(1);
        assertThat(updatedTimedAbsenceSpans.getAbsencesList().get(0).getPartOfDay()).isEqualTo(FULL.name());
        assertThat(updatedTimedAbsenceSpans.getAbsencesList().get(0).getRatio()).isEqualTo(BigDecimal.ONE);
    }

    @Test
    void ensurePersonIsNotAvailableOnCancellationRequest() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate fullDayVacationDate = LocalDate.of(2016, 1, 4);
        final Application fullDayApplication = createApplication(person, fullDayVacationDate, fullDayVacationDate, FULL);
        fullDayApplication.setStatus(ALLOWED_CANCELLATION_REQUESTED);

        when(applicationService.getApplicationsForACertainPeriodAndPerson(fullDayVacationDate, fullDayVacationDate, person))
            .thenReturn(List.of(fullDayApplication));

        final TimedAbsenceSpans updatedTimedAbsenceSpans = sut.checkForAbsence(new TimedAbsenceSpans(new ArrayList<>()), person, fullDayVacationDate);
        assertThat(updatedTimedAbsenceSpans.getAbsencesList()).hasSize(1);
        assertThat(updatedTimedAbsenceSpans.getAbsencesList().get(0).getPartOfDay()).isEqualTo(FULL.name());
        assertThat(updatedTimedAbsenceSpans.getAbsencesList().get(0).getRatio()).isEqualTo(BigDecimal.ONE);
    }

    @Test
    void ensurePersonIsNotAvailableOnAllowed() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate fullDayVacationDate = LocalDate.of(2016, 1, 4);
        final Application fullDayApplication = createApplication(person, fullDayVacationDate, fullDayVacationDate, FULL);
        fullDayApplication.setStatus(ALLOWED);

        when(applicationService.getApplicationsForACertainPeriodAndPerson(fullDayVacationDate, fullDayVacationDate, person))
            .thenReturn(List.of(fullDayApplication));

        final TimedAbsenceSpans updatedTimedAbsenceSpans = sut.checkForAbsence(new TimedAbsenceSpans(new ArrayList<>()),
            person, fullDayVacationDate);

        List<TimedAbsence> absencesList = updatedTimedAbsenceSpans.getAbsencesList();
        assertThat(absencesList).hasSize(1);
        assertThat(absencesList.get(0).getPartOfDay()).isEqualTo(FULL.name());
        assertThat(absencesList.get(0).getRatio()).isEqualTo(BigDecimal.ONE);
    }

    @Test
    void ensurePersonIsAvailableOnRejected() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate fullDayVacationDate = LocalDate.of(2016, 1, 4);
        final Application fullDayApplication = createApplication(person, fullDayVacationDate, fullDayVacationDate, FULL);
        fullDayApplication.setStatus(REJECTED);

        when(applicationService.getApplicationsForACertainPeriodAndPerson(fullDayVacationDate, fullDayVacationDate, person))
            .thenReturn(List.of(fullDayApplication));

        final TimedAbsenceSpans updatedTimedAbsenceSpans = sut.checkForAbsence(new TimedAbsenceSpans(new ArrayList<>()), person, fullDayVacationDate);
        assertThat(updatedTimedAbsenceSpans.getAbsencesList()).isEmpty();
    }

    @Test
    void ensurePersonIsAvailableOnCancelled() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate fullDayVacationDate = LocalDate.of(2016, 1, 4);
        final Application fullDayApplication = createApplication(person, fullDayVacationDate, fullDayVacationDate, FULL);
        fullDayApplication.setStatus(CANCELLED);

        when(applicationService.getApplicationsForACertainPeriodAndPerson(fullDayVacationDate, fullDayVacationDate, person))
            .thenReturn(List.of(fullDayApplication));

        final TimedAbsenceSpans updatedTimedAbsenceSpans = sut.checkForAbsence(new TimedAbsenceSpans(new ArrayList<>()), person, fullDayVacationDate);
        assertThat(updatedTimedAbsenceSpans.getAbsencesList()).isEmpty();
    }

    @Test
    void ensurePersonIsAvailableOnRevoked() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate fullDayVacationDate = LocalDate.of(2016, 1, 4);
        final Application fullDayApplication = createApplication(person, fullDayVacationDate, fullDayVacationDate, FULL);
        fullDayApplication.setStatus(REVOKED);

        when(applicationService.getApplicationsForACertainPeriodAndPerson(fullDayVacationDate, fullDayVacationDate, person))
            .thenReturn(List.of(fullDayApplication));

        final TimedAbsenceSpans updatedTimedAbsenceSpans = sut.checkForAbsence(new TimedAbsenceSpans(new ArrayList<>()), person, fullDayVacationDate);
        assertThat(updatedTimedAbsenceSpans.getAbsencesList()).isEmpty();
    }

    @Test
    void ensurePersonIsNotAvailableOnVacationOneHalfDay() {

        final Person testPerson = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate twoHalfDayVacationsDate = LocalDate.of(2016, 1, 5);
        final Application halfMorningDayApplication = createApplication(testPerson, twoHalfDayVacationsDate, twoHalfDayVacationsDate, MORNING);

        when(applicationService.getApplicationsForACertainPeriodAndPerson(twoHalfDayVacationsDate, twoHalfDayVacationsDate, testPerson))
            .thenReturn(List.of(halfMorningDayApplication));

        final TimedAbsenceSpans updatedTimedAbsenceSpans = sut.checkForAbsence(new TimedAbsenceSpans(new ArrayList<>()), testPerson, twoHalfDayVacationsDate);
        assertThat(updatedTimedAbsenceSpans.getAbsencesList()).hasSize(1);
        assertThat(updatedTimedAbsenceSpans.getAbsencesList().get(0).getPartOfDay()).isEqualTo(MORNING.name());
        assertThat(updatedTimedAbsenceSpans.getAbsencesList().get(0).getRatio()).isEqualTo(BigDecimal.valueOf(0.5));
    }

    @Test
    void ensurePersonIsNotAvailableOnVacationTwoHalfDays() {

        final Person testPerson = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate twoHalfDayVacationsDate = LocalDate.of(2016, 1, 5);
        final Application halfMorningDayApplication = createApplication(testPerson, twoHalfDayVacationsDate, twoHalfDayVacationsDate, MORNING);
        final Application halfNoonDayApplication = createApplication(testPerson, twoHalfDayVacationsDate, twoHalfDayVacationsDate, NOON);

        when(applicationService.getApplicationsForACertainPeriodAndPerson(twoHalfDayVacationsDate, twoHalfDayVacationsDate, testPerson))
            .thenReturn(List.of(halfMorningDayApplication, halfNoonDayApplication));

        final TimedAbsenceSpans updatedTimedAbsenceSpans = sut.checkForAbsence(new TimedAbsenceSpans(new ArrayList<>()), testPerson, twoHalfDayVacationsDate);
        assertThat(updatedTimedAbsenceSpans.getAbsencesList()).hasSize(2);
        assertThat(updatedTimedAbsenceSpans.getAbsencesList().get(0).getPartOfDay()).isEqualTo(MORNING.name());
        assertThat(updatedTimedAbsenceSpans.getAbsencesList().get(0).getRatio()).isEqualTo(BigDecimal.valueOf(0.5));
        assertThat(updatedTimedAbsenceSpans.getAbsencesList().get(1).getPartOfDay()).isEqualTo(NOON.name());
        assertThat(updatedTimedAbsenceSpans.getAbsencesList().get(1).getRatio()).isEqualTo(BigDecimal.valueOf(0.5));
    }

    @Test
    void ensureReturnsGiveAbsenceSpansIfNoVacationFound() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate vacationDay = LocalDate.of(2016, 1, 4);
        final TimedAbsenceSpans emptyTimedAbsenceSpans = new TimedAbsenceSpans(new ArrayList<>());

        final TimedAbsenceSpans updatedTimedAbsenceSpans = sut.checkForAbsence(emptyTimedAbsenceSpans, person, vacationDay);
        assertThat(updatedTimedAbsenceSpans).isEqualTo(emptyTimedAbsenceSpans);
    }
}
