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
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.XProperty;
import net.fortuna.ical4j.validate.ValidationException;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.absence.Absence;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static java.util.Date.from;
import static java.util.stream.Collectors.toList;
import static net.fortuna.ical4j.model.parameter.Role.REQ_PARTICIPANT;
import static net.fortuna.ical4j.model.property.CalScale.GREGORIAN;
import static net.fortuna.ical4j.model.property.Version.VERSION_2_0;


@Service
public class ICalService {

    private final CalendarProperties calendarProperties;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Autowired
    ICalService(CalendarProperties calendarProperties) {
        this.calendarProperties = calendarProperties;
    }

    public File getCalendar(String title, List<Absence> absences) {
        final Calendar calendar = generateCalendar(title, absences);

        final File file;
        try {
            file = File.createTempFile("calendar-", ".ical");
        } catch (IOException e) {
            throw new CalendarException("Could not generate temp file for " + title + " calendar", e);
        }

        try (final FileWriter calendarFileWriter = new FileWriter(file)) {
            final CalendarOutputter calendarOutputter = new CalendarOutputter();
            calendarOutputter.output(calendar, calendarFileWriter);
        } catch (ValidationException | IOException e) {
            throw new CalendarException("iCal calender could not be written to file", e);
        }

        return file;
    }

    private Calendar generateCalendar(String title, List<Absence> absences) {
        final Calendar calendar = new Calendar();
        calendar.getProperties().add(VERSION_2_0);
        calendar.getProperties().add(new ProdId("-//Urlaubsverwaltung//iCal4j 1.0//DE"));
        calendar.getProperties().add(GREGORIAN);
        calendar.getProperties().add(new XProperty("X-WR-CALNAME", title));
        calendar.getProperties().add(new XProperty("X-MICROSOFT-CALSCALE", GREGORIAN.getValue()));
        calendar.getProperties().add(new RefreshInterval(new ParameterList(), calendarProperties.getRefreshInterval()));

        final List<VEvent> absencesVEvents = absences.stream()
            .map(this::toVEvent)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(toList());
        calendar.getComponents().addAll(absencesVEvents);

        return calendar;
    }

    private Optional<VEvent> toVEvent(Absence absence) {

        final ZonedDateTime startDateTime = absence.getStartDate();
        final ZonedDateTime endDateTime = absence.getEndDate();

        final VEvent event;
        if (absence.isAllDay()) {
            try {
                Date startDate = new Date(startDateTime.format(formatter));
                if (isSameDay(startDateTime, endDateTime)) {
                    event = new VEvent(startDate, absence.getEventSubject());
                } else {
                    Date endDate = new Date(endDateTime.format(formatter));
                    event = new VEvent(new Date(startDate), new Date(endDate), absence.getEventSubject());
                }
            } catch (ParseException e) {
                // TODO: log
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
        calendarProperties.getOrganizer()
            .ifPresent(organizer -> event.getProperties().add(new Organizer(URI.create("mailto:" + organizer))));

        return Optional.of(event);
    }

    private Attendee generateAttendee(Absence absence) {
        final Attendee attendee;
        attendee = new Attendee(URI.create("mailto:" + absence.getPerson().getEmail()));
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
}
