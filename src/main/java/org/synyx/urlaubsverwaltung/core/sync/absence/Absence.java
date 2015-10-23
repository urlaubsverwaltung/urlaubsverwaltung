package org.synyx.urlaubsverwaltung.core.sync.absence;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeZone;

import org.springframework.util.Assert;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;

import java.util.Date;


/**
 * Represents a period of time where a person is not at work.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class Absence {

    private Date startDate;

    private Date endDate;

    private final Person person;

    private final EventType eventType;

    private boolean isAllDay;

    public Absence(Application application, AbsenceTimeConfiguration absenceTimeConfiguration) {

        Assert.notNull(application.getDayLength(), "No day length set for application");
        Assert.notNull(application.getStartDate(), "No start date set for application");
        Assert.notNull(application.getEndDate(), "No end date set for application");
        Assert.isTrue(application.hasStatus(ApplicationStatus.ALLOWED)
            || application.hasStatus(ApplicationStatus.WAITING),
            "Non expected application status. Application must have status WAITING or ALLOWED.");

        switch (application.getStatus()) {
            case ALLOWED:
                eventType = EventType.ALLOWED_APPLICATION;
                break;

            case WAITING:
                eventType = EventType.WAITING_APPLICATION;
                break;

            default:
                throw new IllegalStateException("Status of application is in an unexpected state.");
        }

        this.person = application.getPerson();

        setPeriod(application.getStartDate(), application.getEndDate(), application.getDayLength(),
            absenceTimeConfiguration);
    }


    public Absence(SickNote sickNote, AbsenceTimeConfiguration absenceTimeConfiguration) {

        Assert.notNull(sickNote.getDayLength(), "No day length set for sick note");
        Assert.notNull(sickNote.getStartDate(), "No start date set for sick note");
        Assert.notNull(sickNote.getEndDate(), "No end date set for sick note");

        this.eventType = EventType.SICKNOTE;
        this.person = sickNote.getPerson();

        setPeriod(sickNote.getStartDate(), sickNote.getEndDate(), sickNote.getDayLength(), absenceTimeConfiguration);
    }

    private void setPeriod(DateMidnight start, DateMidnight end, DayLength dayLength,
        AbsenceTimeConfiguration timeConfiguration) {

        long startDateInMilliseconds = start.toDateTime(DateTimeZone.UTC).getMillis();
        long endDateInMilliseconds = end.toDateTime(DateTimeZone.UTC).getMillis();

        switch (dayLength) {
            case FULL:
                this.startDate = new Date(startDateInMilliseconds);
                this.endDate = new Date(end.plusDays(1).toDateTime(DateTimeZone.UTC).getMillis());
                this.isAllDay = true;
                break;

            case MORNING:
                this.startDate = new Date(startDateInMilliseconds + timeConfiguration.getMorningStartAsMillis());
                this.endDate = new Date(endDateInMilliseconds + timeConfiguration.getMorningEndAsMillis());
                this.isAllDay = false;
                break;

            case NOON:
                this.startDate = new Date(startDateInMilliseconds + timeConfiguration.getNoonStartAsMillis());
                this.endDate = new Date(endDateInMilliseconds + timeConfiguration.getNoonEndAsMillis());
                this.isAllDay = false;
                break;

            default:
                throw new IllegalArgumentException("Invalid day length!");
        }
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


    @Override
    public String toString() {

        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("person", getPerson().getLoginName())
            .append("startDate", getStartDate())
            .append("endDate", getEndDate())
            .append("isAllDay", isAllDay())
            .toString();
    }
}
