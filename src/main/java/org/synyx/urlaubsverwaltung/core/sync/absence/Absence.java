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

    private static final int ONE_HOUR_IN_MILLISECONDS = 60 * 60 * 1000;

    private static final int MORNING_START = 8 * ONE_HOUR_IN_MILLISECONDS;
    private static final int MORNING_END = 12 * ONE_HOUR_IN_MILLISECONDS;
    private static final int NOON_START = 13 * ONE_HOUR_IN_MILLISECONDS;
    private static final int NOON_END = 17 * ONE_HOUR_IN_MILLISECONDS;

    private Date startDate;

    private Date endDate;

    private Person person;

    private EventType eventType;

    private boolean isAllDay = false;

    public Absence(Application application) {

        Assert.notNull(application.getHowLong(), "No day length set for application");
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
        }

        this.person = application.getPerson();

        long startDateInMilliseconds = application.getStartDate().toDateTime(DateTimeZone.UTC).getMillis();
        long endDateInMilliseconds = application.getEndDate().toDateTime(DateTimeZone.UTC).getMillis();

        switch (application.getHowLong()) {
            case FULL:
                this.startDate = new Date(startDateInMilliseconds);
                this.endDate = new Date(endDateInMilliseconds + TimeUnit.DAYS.toMillis(1));
                this.isAllDay = true;
                break;

            case MORNING:
                this.startDate = new Date(startDateInMilliseconds + MORNING_START);
                this.endDate = new Date(endDateInMilliseconds + MORNING_END);
                break;

            case NOON:
                this.startDate = new Date(startDateInMilliseconds + NOON_START);
                this.endDate = new Date(endDateInMilliseconds + NOON_END);
                break;

            default:
                throw new IllegalArgumentException("Invalid day length for application!");
        }
    }


    public Absence(SickNote sickNote) {

        Assert.notNull(sickNote.getStartDate(), "No start date set for application");
        Assert.notNull(sickNote.getEndDate(), "No end date set for application");

        this.eventType = EventType.SICKNOTE;
        this.startDate = sickNote.getStartDate().toDate();
        this.endDate = sickNote.getEndDate().toDate();
        this.person = sickNote.getPerson();

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
