package org.synyx.urlaubsverwaltung.restapi.availability;

import org.joda.time.DateMidnight;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * @author  Timo Eifler - eifler@synyx.de
 */
public class VacationAbsenceProviderTest {

    private VacationAbsenceProvider vacationAbsenceProvider;

    private ApplicationService applicationService;

    private TimedAbsenceSpans emptyTimedAbsenceSpans;
    private Person testPerson;
    private DateMidnight vacationDay;
    private Application application;

    @Before
    public void setUp() {

        applicationService = Mockito.mock(ApplicationService.class);

        emptyTimedAbsenceSpans = new TimedAbsenceSpans(new ArrayList<>());
        testPerson = TestDataCreator.createPerson();
        application = TestDataCreator.createApplication(testPerson, vacationDay, vacationDay, DayLength.FULL);
        vacationDay = new DateMidnight(2016, 1, 4);

        vacationAbsenceProvider = new VacationAbsenceProvider(applicationService);
    }


    @Test
    public void ensurePersonIsNotAvailableOnSickDay() {

        Mockito.when(applicationService.getApplicationsForACertainPeriodAndPerson(vacationDay, vacationDay, testPerson))
            .thenReturn(Collections.singletonList(application));

        TimedAbsenceSpans updatedTimedAbsenceSpans = vacationAbsenceProvider.checkForAbsence(emptyTimedAbsenceSpans,
                testPerson, vacationDay);

        List<TimedAbsence> absencesList = updatedTimedAbsenceSpans.getAbsencesList();

        Assert.assertEquals("wrong number of absences in list", 1, absencesList.size());
        Assert.assertEquals("wrong absence type", TimedAbsence.Type.VACATION, absencesList.get(0).getType());
        Assert.assertEquals("wrong part of day set on absence", DayLength.FULL.name(),
            absencesList.get(0).getPartOfDay());
        Assert.assertTrue("wrong absence ratio", BigDecimal.ONE.compareTo(absencesList.get(0).getRatio()) == 0);
    }


    @Test
    public void ensureReturnsGiveAbsenceSpansIfNoVacationFound() {

        DateMidnight standardWorkingDay = new DateMidnight(2016, 1, 5);

        Mockito.when(applicationService.getApplicationsForACertainPeriodAndPerson(standardWorkingDay,
                    standardWorkingDay, testPerson))
            .thenReturn(Collections.emptyList());

        TimedAbsenceSpans updatedTimedAbsenceSpans = vacationAbsenceProvider.checkForAbsence(emptyTimedAbsenceSpans,
                testPerson, vacationDay);

        Assert.assertEquals("absence spans changed", emptyTimedAbsenceSpans, updatedTimedAbsenceSpans);
    }
}
