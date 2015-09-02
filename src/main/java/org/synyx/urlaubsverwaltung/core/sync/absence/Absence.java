package org.synyx.urlaubsverwaltung.core.sync.absence;

import com.google.common.base.MoreObjects;

import org.joda.time.DateTimeZone;

import org.springframework.util.Assert;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;

import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 * Represents a period of time where a person is not at work.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class Absence {

    private final Date startDate;

    private final Date endDate;

    private final Person person;

    private final EventType eventType;

    private final boolean isAllDay;

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

        long startDateInMilliseconds = application.getStartDate().toDateTime(DateTimeZone.UTC).getMillis();
        long endDateInMilliseconds = application.getEndDate().toDateTime(DateTimeZone.UTC).getMillis();

        switch (application.getDayLength()) {
            case FULL:
                this.startDate = new Date(startDateInMilliseconds);
                this.endDate = new Date(endDateInMilliseconds + TimeUnit.DAYS.toMillis(1));
                this.isAllDay = true;
                break;

            case MORNING:
                this.startDate = new Date(startDateInMilliseconds + absenceTimeConfiguration.getMorningStartAsMillis());
                this.endDate = new Date(endDateInMilliseconds + absenceTimeConfiguration.getMorningEndAsMillis());
                this.isAllDay = false;
                break;

            case NOON:
                this.startDate = new Date(startDateInMilliseconds + absenceTimeConfiguration.getNoonStartAsMillis());
                this.endDate = new Date(endDateInMilliseconds + absenceTimeConfiguration.getNoonEndAsMillis());
                this.isAllDay = false;
                break;

            default:
                throw new IllegalArgumentException("Invalid day length for application!");
        }
    }


    public Absence(SickNote sickNote) {

        Assert.notNull(sickNote.getStartDate(), "No start date set for sick note");
        Assert.notNull(sickNote.getEndDate(), "No end date set for sick note");

        this.eventType = EventType.SICKNOTE;
        this.person = sickNote.getPerson();

        long startDateInMilliseconds = sickNote.getStartDate().toDateTime(DateTimeZone.UTC).getMillis();
        long endDateInMilliseconds = sickNote.getEndDate().toDateTime(DateTimeZone.UTC).getMillis();

        this.startDate = new Date(startDateInMilliseconds);
        this.endDate = new Date(endDateInMilliseconds + TimeUnit.DAYS.toMillis(1));

        // TODO: at the moment sick notes have no day length
        this.isAllDay = true;
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

        MoreObjects.ToStringHelper toStringHelper = MoreObjects.toStringHelper(this);

        toStringHelper.add("person", getPerson().getLoginName());
        toStringHelper.add("startDate", getStartDate());
        toStringHelper.add("endDate", getEndDate());
        toStringHelper.add("isAllDay", isAllDay());

        return toStringHelper.toString();
    }
}
