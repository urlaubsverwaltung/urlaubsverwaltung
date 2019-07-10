package org.synyx.urlaubsverwaltung.calendarintegration.absence;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.util.Assert;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.ZonedDateTime;

import static java.time.ZoneOffset.UTC;


/**
 * Represents a period of time where a person is not at work.
 */
public class Absence {

    private final ZonedDateTime startDate;
    private final ZonedDateTime endDate;
    private final Person person;
    private final boolean isAllDay;

    public Absence(Person person, Period period,
                   AbsenceTimeConfiguration absenceTimeConfiguration) {

        Assert.notNull(person, "Person must be given");
        Assert.notNull(period, "Period must be given");
        Assert.notNull(absenceTimeConfiguration, "Time configuration must be given");

        this.person = person;

        ZonedDateTime periodStartDate = period.getStartDate().atStartOfDay(UTC);
        ZonedDateTime periodEndDate = period.getEndDate().atStartOfDay(UTC);

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

        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("person", getPerson().getId())
            .append("startDate", getStartDate())
            .append("endDate", getEndDate())
            .append("isAllDay", isAllDay())
            .toString();
    }
}
