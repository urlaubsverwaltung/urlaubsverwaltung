package org.synyx.urlaubsverwaltung.calendar;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.RefreshInterval;
import net.fortuna.ical4j.model.property.Sequence;
import net.fortuna.ical4j.model.property.Transp;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.XProperty;
import net.fortuna.ical4j.validate.ValidationException;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.absence.Absence;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Date.from;
import static net.fortuna.ical4j.model.parameter.Role.REQ_PARTICIPANT;
import static net.fortuna.ical4j.model.property.CalScale.GREGORIAN;
import static net.fortuna.ical4j.model.property.Method.CANCEL;
import static net.fortuna.ical4j.model.property.Transp.VALUE_TRANSPARENT;
import static net.fortuna.ical4j.model.property.Version.VERSION_2_0;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.calendar.ICalType.CANCELLED;
import static org.synyx.urlaubsverwaltung.calendar.ICalType.PUBLISHED;


@Service
public class ICalService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final CalendarProperties calendarProperties;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Autowired
    ICalService(CalendarProperties calendarProperties) {
        this.calendarProperties = calendarProperties;
    }

    public ByteArrayResource getCalendar(String title, List<Absence> absences) {
        final Calendar calendar = generateCalendar(title, absences);
        return writeCalenderIntoRessource(calendar);
    }

    public ByteArrayResource getSingleAppointment(Absence absence, ICalType method) {
        final Calendar calendar = generateForSingleAppointment(absence, method);
        return writeCalenderIntoRessource(calendar);
    }

    private Calendar generateCalendar(String title, List<Absence> absences) {
        final Calendar calendar = prepareCalendar(absences, PUBLISHED);
        calendar.getProperties().add(new XProperty("X-WR-CALNAME", title));
        calendar.getProperties().add(new RefreshInterval(new ParameterList(), calendarProperties.getRefreshInterval()));
        return calendar;
    }

    private Calendar generateForSingleAppointment(Absence absence, ICalType method) {
        return prepareCalendar(List.of(absence), method);
    }

    private Calendar prepareCalendar(List<Absence> absences, ICalType method) {
        final Calendar calendar = new Calendar();
        calendar.getProperties().add(VERSION_2_0);
        calendar.getProperties().add(new ProdId("-//Urlaubsverwaltung//iCal4j 1.0//DE"));
        calendar.getProperties().add(GREGORIAN);
        calendar.getProperties().add(new XProperty("X-MICROSOFT-CALSCALE", GREGORIAN.getValue()));

        if (method == CANCELLED) {
            calendar.getProperties().add(CANCEL);
        }

        absences.stream()
            .map(absence -> this.toVEvent(absence, method))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .forEach(event -> calendar.getComponents().add(event));

        return calendar;
    }

    private Optional<VEvent> toVEvent(Absence absence, ICalType method) {

        final ZonedDateTime startDateTime = absence.getStartDate();
        final ZonedDateTime endDateTime = absence.getEndDate();

        final VEvent event;
        if (absence.isAllDay()) {
            try {
                final Date startDate = new Date(startDateTime.format(formatter));
                if (isSameDay(startDateTime, endDateTime)) {
                    event = new VEvent(startDate, absence.getEventSubject());
                } else {
                    final Date endDate = new Date(endDateTime.format(formatter));
                    event = new VEvent(new Date(startDate), new Date(endDate), absence.getEventSubject());
                }
            } catch (ParseException e) {
                LOG.warn("Could not generate all day ical event for absence {}", absence, e);
                return Optional.empty();
            }

            event.getProperties().add(new XProperty("X-MICROSOFT-CDO-ALLDAYEVENT", "TRUE"));
        } else {
            final DateTime start = new DateTime(from(startDateTime.toInstant()));
            start.setUtc(true);
            final DateTime end = new DateTime(from(endDateTime.toInstant()));
            end.setUtc(true);

            event = new VEvent(start, end, absence.getEventSubject());
        }

        event.getProperties().add(new Uid(generateUid(absence)));
        event.getProperties().add(generateAttendee(absence));

        if (absence.isHolidayReplacement()) {
            event.getProperties().add(new Transp(VALUE_TRANSPARENT));
        }

        if (method == CANCELLED) {
            event.getProperties().add(new Sequence(1));
        }

        event.getProperties().add(new Organizer(URI.create("mailto:" + calendarProperties.getOrganizer())));

        return Optional.of(event);
    }

    private Attendee generateAttendee(Absence absence) {
        final Attendee attendee = new Attendee(URI.create("mailto:" + absence.getPerson().getEmail()));
        attendee.getParameters().add(REQ_PARTICIPANT);
        attendee.getParameters().add(new Cn(absence.getPerson().getNiceName()));

        return attendee;
    }

    private boolean isSameDay(ZonedDateTime startDateTime, ZonedDateTime endDate) {
        return startDateTime.toLocalDate().isEqual(endDate.toLocalDate().minusDays(1));
    }

    private String generateUid(Absence absence) {
        final String data = absence.getStartDate() + "" + absence.getEndDate() + "" + absence.getPerson();
        return DigestUtils.md5Hex(data).toUpperCase();
    }

    private ByteArrayResource writeCalenderIntoRessource(Calendar calendar) {
        final ByteArrayResource byteArrayResource;
        try (final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            final CalendarOutputter calendarOutputter = new CalendarOutputter();
            calendarOutputter.output(calendar, byteArrayOutputStream);
            byteArrayResource = new ByteArrayResource(byteArrayOutputStream.toByteArray());
        } catch (ValidationException | IOException e) {
            throw new CalendarException("iCal calendar could not be written to ByteArrayResource", e);
        }
        return byteArrayResource;
    }
}
