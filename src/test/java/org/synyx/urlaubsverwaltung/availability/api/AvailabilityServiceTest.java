package org.synyx.urlaubsverwaltung.availability.api;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.demodatacreator.DemoDataCreator;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class AvailabilityServiceTest {

    private static final int DAYS_IN_TEST_DATE_RANGE = 8;

    private AvailabilityService availabilityService;

    private FreeTimeAbsenceProvider freeTimeAbsenceProvider;

    private Person testPerson;
    private LocalDate testDateRangeStart;
    private LocalDate testDateRangeEnd;
    private TimedAbsenceSpans timedAbsenceSpansMock;

    @BeforeEach
    void setUp() {

        freeTimeAbsenceProvider = mock(FreeTimeAbsenceProvider.class);
        timedAbsenceSpansMock = mock(TimedAbsenceSpans.class);

        when(freeTimeAbsenceProvider.checkForAbsence(
            any(Person.class),
            any(LocalDate.class)))
            .thenReturn(timedAbsenceSpansMock);

        availabilityService = new AvailabilityService(freeTimeAbsenceProvider);

        testPerson = DemoDataCreator.createPerson();
        testDateRangeStart = LocalDate.of(2016, 1, 1);
        testDateRangeEnd = LocalDate.of(2016, 1, DAYS_IN_TEST_DATE_RANGE);
    }


    @Test
    void ensureFetchesAvailabilityListForEachDayInDateRange() {

        availabilityService.getPersonsAvailabilities(testDateRangeStart, testDateRangeEnd, testPerson);

        verify(freeTimeAbsenceProvider, times(DAYS_IN_TEST_DATE_RANGE))
            .checkForAbsence(eq(testPerson), any(LocalDate.class));
    }


    @Test
    void ensureReturnsDayAvailabilityWithCalculatedPresenceRatio() {

        LocalDate dayToTest = testDateRangeStart;

        BigDecimal expectedAvailabilityRatio = BigDecimal.ONE;

        when(timedAbsenceSpansMock.calculatePresenceRatio()).thenReturn(expectedAvailabilityRatio);

        AvailabilityListDto personsAvailabilities = availabilityService.getPersonsAvailabilities(dayToTest, dayToTest,
            testPerson);

        verify(timedAbsenceSpansMock, times(1)).calculatePresenceRatio();

        List<DayAvailability> availabilityList = personsAvailabilities.getAvailabilities();
        Assert.assertEquals("Wrong number of Availabilities returned", 1, availabilityList.size());

        DayAvailability availabilityOnDayToTest = availabilityList.get(0);
        Assert.assertEquals("Wrong TimedAbsenceSpans set on return object", timedAbsenceSpansMock,
            availabilityOnDayToTest.getTimedAbsenceSpans());
        Assert.assertEquals("Wrong availability ratio set on return object", expectedAvailabilityRatio,
            availabilityOnDayToTest.getAvailabilityRatio());
    }
}
