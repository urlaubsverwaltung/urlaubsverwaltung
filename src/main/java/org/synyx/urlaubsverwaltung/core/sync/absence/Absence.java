package org.synyx.urlaubsverwaltung.core.sync.absence;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import org.joda.time.DateTimeZone;

import org.springframework.util.Assert;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.period.Period;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;

import java.util.Date;


/**
 * Represents a period of time where a person is not at work.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class Absence {

    private final String identifier;
    private final Date startDate;
    private final Date endDate;
    private final Person person;
    private final EventType eventType;
    private final boolean isAllDay;

    public Absence(String identifier, Person person, Period period, EventType eventType,
        AbsenceTimeConfiguration absenceTimeConfiguration) {

        Assert.notNull(identifier, "Identifier must be given");
        Assert.notNull(person, "Person must be given");
        Assert.notNull(period, "Period must be given");
        Assert.notNull(eventType, "Type of absence must be given");
        Assert.notNull(absenceTimeConfiguration, "Time configuration must be given");

        this.identifier = identifier;
        this.person = person;
        this.eventType = eventType;

        long startDateInMilliseconds = period.getStartDate().toDateTime(DateTimeZone.UTC).getMillis();
        long endDateInMilliseconds = period.getEndDate().toDateTime(DateTimeZone.UTC).getMillis();

        switch (period.getDayLength()) {
            case FULL:
                this.startDate = new Date(startDateInMilliseconds);
                this.endDate = new Date(period.getEndDate().plusDays(1).toDateTime(DateTimeZone.UTC).getMillis());
                this.isAllDay = true;
                break;

            case MORNING:
                this.startDate = new Date(startDateInMilliseconds
                        + absenceTimeConfiguration.getMorningStartAsMillis());
                this.endDate = new Date(endDateInMilliseconds + absenceTimeConfiguration.getMorningEndAsMillis());
                this.isAllDay = false;
                break;

            case NOON:
                this.startDate = new Date(startDateInMilliseconds + absenceTimeConfiguration.getNoonStartAsMillis());
                this.endDate = new Date(endDateInMilliseconds + absenceTimeConfiguration.getNoonEndAsMillis());
                this.isAllDay = false;
                break;

            default:
                throw new IllegalArgumentException("Invalid day length!");
        }
    }

    public static Absence of(Application application, AbsenceTimeConfiguration timeConfig) {

        String identifier = AbsenceType.VACATION.name() + "_" + application.getId();

        return new Absence(identifier, application.getPerson(), application.getPeriod(), application.getEventType(),
                timeConfig);
    }


    public static Absence of(SickNote sickNote, AbsenceTimeConfiguration timeConfig) {

        String identifier = AbsenceType.SICKNOTE.name() + "_" + sickNote.getId();

        return new Absence(identifier, sickNote.getPerson(), sickNote.getPeriod(), EventType.SICKNOTE, timeConfig);
    }


    public EventType getEventType() {

        return eventType;
    }


    public Date getStartDate() {

        return startDate;
    }


    public Date getEndDate() {

        return endDate;
    }


    public Person getPerson() {

        return person;
    }


    public boolean isAllDay() {

        return isAllDay;
    }


    public String getEventSubject() {

        switch (eventType) {
            case ALLOWED_APPLICATION:
                return String.format("Urlaub %s", person.getNiceName());

            case WAITING_APPLICATION:
                return String.format("Antrag auf Urlaub %s", person.getNiceName());

            case SICKNOTE:
                return String.format("%s krank", person.getNiceName());

            default:
                throw new IllegalStateException("Event type is not properly set.");
        }
    }


    public String getIdentifier() {

        return identifier;
    }


    @Override
    public String toString() {

        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("person", getPerson().getLoginName())
            .append("startDate", getStartDate())
            .append("endDate", getEndDate())
            .append("isAllDay", isAllDay())
            .toString();
    }
}
