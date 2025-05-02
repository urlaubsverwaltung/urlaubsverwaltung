package org.synyx.urlaubsverwaltung.publicholiday;

import de.focus_shift.jollyday.core.HolidayCalendar;
import de.focus_shift.jollyday.core.HolidayManager;
import de.focus_shift.jollyday.core.ManagerParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.period.DayLength;

import java.time.LocalDate;
import java.time.Month;
import java.util.Map;
import java.util.Optional;

import static java.math.BigDecimal.ZERO;
import static java.time.LocalDate.of;
import static java.time.Month.AUGUST;
import static java.time.Month.DECEMBER;
import static java.time.Month.MAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BADEN_WUERTTEMBERG;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BAYERN_MUENCHEN;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BERLIN;

@ExtendWith(MockitoExtension.class)
class PublicHolidaysServiceImplTest {

    private PublicHolidaysService sut;

    @BeforeEach
    void setUp() {
        sut = new PublicHolidaysServiceImpl(Map.of("de", getHolidayManager()));
    }

    @Test
    void ensureCorrectWorkingDurationForWorkDay() {
        final LocalDate localDate = of(2013, Month.NOVEMBER, 27);
        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(localDate, GERMANY_BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).isEmpty();
    }

    @Test
    void ensureCorrectWorkingDurationForPublicHoliday() {
        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2013, DECEMBER, 25), GERMANY_BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).hasValueSatisfying(publicHoliday -> assertThat(publicHoliday.getWorkingDuration()).isEqualByComparingTo(ZERO));
    }

    @Test
    void ensureCorrectWorkingDurationForAssumptionDayForBerlin() {
        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2015, AUGUST, 15), GERMANY_BERLIN);
        assertThat(maybePublicHoliday).isEmpty();
    }

    @Test
    void ensureCorrectWorkingDurationForAssumptionDayForBadenWuerttemberg() {
        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2015, AUGUST, 15), GERMANY_BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).isEmpty();
    }

    @Test
    void ensureCorrectWorkingDurationForAssumptionDayForBayernMuenchen() {
        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2015, DECEMBER, 15), GERMANY_BAYERN_MUENCHEN);
        assertThat(maybePublicHoliday).isEmpty();
    }

    @Test
    void ensureGetDayLengthReturnsFullForCorpusChristi() {
        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2019, MAY, 30), GERMANY_BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).hasValueSatisfying(publicHoliday -> assertThat(publicHoliday.dayLength()).isEqualTo(DayLength.FULL));
    }

    @Test
    void ensureGetDayLengthReturnsFullForAssumptionDayInBayernMunich() {
        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2019, AUGUST, 15), GERMANY_BAYERN_MUENCHEN);
        assertThat(maybePublicHoliday).hasValueSatisfying(publicHoliday -> assertThat(publicHoliday.dayLength()).isEqualTo(DayLength.FULL));
    }

    @Test
    void ensureGetDayLengthReturnsZeroForAssumptionDayInBerlin() {
        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2019, AUGUST, 15), GERMANY_BERLIN);
        assertThat(maybePublicHoliday).isEmpty();
    }

    @Test
    void ensureGetDayLengthReturnsZeroForAssumptionDayInBadenWuerttemberg() {
        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2019, AUGUST, 15), GERMANY_BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).isEmpty();
    }

    private HolidayManager getHolidayManager() {
        return HolidayManager.getInstance(ManagerParameters.create(HolidayCalendar.GERMANY));
    }
}
