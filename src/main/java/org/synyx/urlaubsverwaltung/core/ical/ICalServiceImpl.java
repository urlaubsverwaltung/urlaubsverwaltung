package org.synyx.urlaubsverwaltung.core.ical;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.model.property.XProperty;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.absence.AbsenceService;
import org.synyx.urlaubsverwaltung.core.sync.absence.Absence;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class ICalServiceImpl implements ICalService {

    private final AbsenceService absenceService;

    @Autowired
    public ICalServiceImpl(AbsenceService absenceService) {

        this.absenceService = absenceService;
    }

    @Override
    public String getICal() {

        return this.getICalObject().toString();
    }


    Calendar getICalObject() {

        List<Absence> absences = absenceService.getOpenAbsences();

        List<VEvent> vEvents = absences.stream().map(this::toVEvent).collect(Collectors.toList());
        Calendar iCal = new Calendar();
        iCal.getProperties().add(new ProdId("-//Urlaubsverwaltung//iCal4j 1.0//DE"));
        iCal.getProperties().add(new XProperty("X-WR-CALNAME", "Urlaube"));
        iCal.getProperties().add(Version.VERSION_2_0);
        iCal.getComponents().addAll(vEvents);

        return iCal;
    }


    private VEvent toVEvent(Absence absence) {

        DateTime end = new DateTime(absence.getEndDate());
        VEvent event;

        if (absence.isAllDay()) {
            org.joda.time.DateTime dateTime = new org.joda.time.DateTime(absence.getStartDate());
            DateTime start = new DateTime(dateTime.plusDays(1).toDate());

            event = new VEvent(new Date(start.getTime()), absence.getEventSubject());
        } else {
            DateTime start = new DateTime(absence.getStartDate());
            event = new VEvent(start, end, absence.getEventSubject());
        }

        event.getProperties().add(new Uid(absence.getIdentifier()));

        return event;
    }
}
