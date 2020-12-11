package org.synyx.urlaubsverwaltung.absence;

import org.springframework.util.Assert;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Represents a period of time where a person is not at work.
 */
public class Absence {

    private final ZonedDateTime startDate;
    private final ZonedDateTime endDate;
    private final Person person;
    private final boolean isAllDay;

    public Absence(Person person, Period period, AbsenceTimeConfiguration absenceTimeConfiguration) {

        Assert.notNull(person, "Person must be given");
        Assert.notNull(period, "Period must be given");
        Assert.notNull(absenceTimeConfiguration, "Time configuration must be given");

        this.person = person;

        ZonedDateTime periodStartDate = period.getStartDate().atStartOfDay(ZoneId.of(absenceTimeConfiguration.getTimeZoneId()));
        ZonedDateTime periodEndDate = period.getEndDate().atStartOfDay(ZoneId.of(absenceTimeConfiguration.getTimeZoneId()));

        switch (period.getDayLength()) {
            case FULL:
                this.startDate = periodStartDate;
                this.endDate = periodEndDate.plusDays(1);
                this.isAllDay = true;
                break;

            case MORNING:
                this.startDate = periodStartDate.plusHours(absenceTimeConfiguration.getMorningStart());
                this.endDate = periodEndDate.plusHours(absenceTimeConfiguration.getMorningEnd());
                this.isAllDay = false;
                break;

            case NOON:
                this.startDate = periodStartDate.plusHours(absenceTimeConfiguration.getNoonStart());
                this.endDate = periodEndDate.plusHours(absenceTimeConfiguration.getNoonEnd());
                this.isAllDay = false;
                break;

            default:
                throw new IllegalArgumentException("Invalid day length!");
        }
    }

    public ZonedDateTime getStartDate() {
        return startDate;
    }

    public ZonedDateTime getEndDate() {
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
