package org.synyx.urlaubsverwaltung.absence;

import org.springframework.util.Assert;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;


/**
 * Represents a period of time where a person is not at work.
 */
public class Absence {

    private final Instant startDate;
    private final Instant endDate;
    private final Person person;
    private final boolean isAllDay;

    public Absence(Person person, Period period,
                   AbsenceTimeConfiguration absenceTimeConfiguration, Clock clock) {

        Assert.notNull(person, "Person must be given");
        Assert.notNull(period, "Period must be given");
        Assert.notNull(absenceTimeConfiguration, "Time configuration must be given");

        this.person = person;

        LocalDate.from(period.getStartDate()).atStartOfDay(clock.getZone()).toInstant();

        Instant periodStartDate = LocalDate.from(period.getStartDate()).atStartOfDay(clock.getZone()).toInstant();
        Instant periodEndDate = LocalDate.from(period.getEndDate()).atStartOfDay(clock.getZone()).toInstant();

        switch (period.getDayLength()) {
            case FULL:
                this.startDate = periodStartDate;
                this.endDate = periodEndDate.plus(1, ChronoUnit.DAYS);
                this.isAllDay = true;
                break;

            case MORNING:
                this.startDate = periodStartDate.plus(absenceTimeConfiguration.getMorningStart(), ChronoUnit.HOURS);
                this.endDate = periodEndDate.plus(absenceTimeConfiguration.getMorningEnd(), ChronoUnit.HOURS);
                this.isAllDay = false;
                break;

            case NOON:
                this.startDate = periodStartDate.plus(absenceTimeConfiguration.getNoonStart(), ChronoUnit.HOURS);
                this.endDate = periodEndDate.plus(absenceTimeConfiguration.getNoonEnd(), ChronoUnit.HOURS);
                this.isAllDay = false;
                break;

            default:
                throw new IllegalArgumentException("Invalid day length!");
        }
    }


    public Instant getStartDate() {

        return startDate;
    }


    public Instant getEndDate() {

        return endDate;
    }


    public Person getPerson() {

        return person;
    }


    public boolean isAllDay() {

        return isAllDay;
    }


    public String getEventSubject() {

        return String.format("%s abwesend", person.getNiceName());
    }

    @Override
    public String toString() {
        return "Absence{" +
            "startDate=" + startDate +
            ", endDate=" + endDate +
            ", person=" + person +
            ", isAllDay=" + isAllDay +
            '}';
    }
}
