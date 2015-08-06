package org.synyx.urlaubsverwaltung.core.sync;

import microsoft.exchange.webservices.data.autodiscover.IAutodiscoverRedirectionUrl;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.enumeration.service.SendInvitationsMode;
import microsoft.exchange.webservices.data.core.service.folder.CalendarFolder;
import microsoft.exchange.webservices.data.core.service.folder.Folder;
import microsoft.exchange.webservices.data.core.service.item.Appointment;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.search.FindFoldersResults;
import microsoft.exchange.webservices.data.search.FolderView;

import org.apache.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;


/**
 * Provides sync of absences with exchange server calendar.
 *
 * <p>Daniel Hammann - <hammann@synyx.de>.</p>
 */
@Service("calendarSyncService")
public class ExchangeCalendarSyncService implements CalendarSyncService {

    private static final Logger LOG = Logger.getLogger(ExchangeCalendarSyncService.class);

    private ExchangeService service;
    private CalendarFolder calendarFolder;

    @Autowired
    public ExchangeCalendarSyncService(@Value("${ews.email}") String emailAddress,
        @Value("${ews.password}") String password,
        @Value("${ews.calendar}") String calendarName) {

        try {
            service = new ExchangeService();
            service.setCredentials(new WebCredentials(emailAddress, password));
            service.autodiscoverUrl(emailAddress, new RedirectionUrlCallback());
            service.setTraceEnabled(true);

            calendarFolder = findCalendar(calendarName);

            if (calendarFolder == null) {
                calendarFolder = createCalendar(calendarName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private CalendarFolder findCalendar(String searchedCalendarName) throws Exception {

        FindFoldersResults calendarRoot = service.findFolders(WellKnownFolderName.Calendar,
                new FolderView(Integer.MAX_VALUE));

        for (Folder folder : calendarRoot.getFolders()) {
            if (folder.getDisplayName().equals(searchedCalendarName)) {
                return (CalendarFolder) folder;
            }
        }

        return null;
    }


    private CalendarFolder createCalendar(String calendarName) throws Exception {

        CalendarFolder folder = new CalendarFolder(service);
        folder.setDisplayName(calendarName);
        folder.save(WellKnownFolderName.Calendar);

        LOG.info(String.format("New calendar folder '%s' created.", folder.getDisplayName()));

        return CalendarFolder.bind(service, folder.getId());
    }


    @Override
    public String addAbsence(Absence absence) {

        try {
            Appointment appointment = new Appointment(service);

            appointment.setSubject(String.format("Urlaub %s", absence.getPerson().getNiceName()));
            appointment.setStart(absence.getStartDate());
            appointment.setEnd(absence.getEndDate());
            appointment.setIsAllDayEvent(absence.isAllDay());
            appointment.getRequiredAttendees().add(absence.getPerson().getEmail());

            appointment.save(calendarFolder.getId(), SendInvitationsMode.SendToAllAndSaveCopy);

            LOG.info(String.format("Appointment %s for %s added to exchange calendar '%s'.", appointment.getId(),
                    absence.getPerson().getNiceName(), calendarFolder.getDisplayName()));

            return appointment.getId().getUniqueId();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private class RedirectionUrlCallback implements IAutodiscoverRedirectionUrl {

        @Override
        public boolean autodiscoverRedirectionUrlValidationCallback(String redirectionUrl) {

            return redirectionUrl.toLowerCase().startsWith("https://");
        }
    }
}
