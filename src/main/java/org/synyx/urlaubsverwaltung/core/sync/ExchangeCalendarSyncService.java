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

import org.springframework.context.annotation.Conditional;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.mail.MailService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.sync.condition.ExchangeCalendarCondition;

import java.util.Optional;


/**
 * Provides sync of absences with exchange server calendar.
 *
 * @author  Daniel Hammann - <hammann@synyx.de>
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service("calendarSyncService")
@Conditional(ExchangeCalendarCondition.class)
public class ExchangeCalendarSyncService implements CalendarSyncService {

    private static final Logger LOG = Logger.getLogger(ExchangeCalendarSyncService.class);

    private MailService mailService;
    private ExchangeService exchangeService;

    private String calendarName;
    private CalendarFolder calendarFolder;

    @Autowired
    public ExchangeCalendarSyncService(MailService mailService,
        @Value("${ews.email}") String emailAddress,
        @Value("${ews.password}") String password,
        @Value("${ews.calendar}") String calendarName) {

        this.mailService = mailService;
        this.calendarName = calendarName;

        try {
            exchangeService = new ExchangeService();
            exchangeService.setCredentials(new WebCredentials(emailAddress, password));
            exchangeService.autodiscoverUrl(emailAddress, new RedirectionUrlCallback());
            exchangeService.setTraceEnabled(true);

            Optional<CalendarFolder> calendarOptional = findCalendar(calendarName);

            if (calendarOptional.isPresent()) {
                calendarFolder = calendarOptional.get();
            } else {
                calendarFolder = createCalendar(calendarName);
            }
        } catch (Exception e) {
            // NOTE: If an exception is thrown at this point, probably there is an error within the configuration

            LOG.error("No connection could be established to the Exchange calendar.");
            LOG.error("Please check your configuration!");
            LOG.error("Shutting down with system exit...");

            System.exit(1);
        }
    }

    private Optional<CalendarFolder> findCalendar(String calendarName) throws Exception {

        FindFoldersResults calendarRoot = exchangeService.findFolders(WellKnownFolderName.Calendar,
                new FolderView(Integer.MAX_VALUE));

        for (Folder folder : calendarRoot.getFolders()) {
            if (folder.getDisplayName().equals(calendarName)) {
                return Optional.of((CalendarFolder) folder);
            }
        }

        return Optional.empty();
    }


    private CalendarFolder createCalendar(String calendarName) throws Exception {

        CalendarFolder folder = new CalendarFolder(exchangeService);
        folder.setDisplayName(calendarName);
        folder.save(WellKnownFolderName.Calendar);

        LOG.info(String.format("New calendar folder '%s' created.", folder.getDisplayName()));

        return CalendarFolder.bind(exchangeService, folder.getId());
    }


    @Override
    public Optional<String> addAbsence(Absence absence) {

        try {
            Appointment appointment = new Appointment(exchangeService);

            Person person = absence.getPerson();

            appointment.setSubject(String.format("Urlaub %s", person.getNiceName()));
            appointment.setStart(absence.getStartDate());
            appointment.setEnd(absence.getEndDate());
            appointment.setIsAllDayEvent(absence.isAllDay());
            appointment.getRequiredAttendees().add(person.getEmail());

            appointment.save(calendarFolder.getId(), SendInvitationsMode.SendToAllAndSaveCopy);

            LOG.info(String.format("Appointment %s for '%s' added to exchange calendar '%s'.", appointment.getId(),
                    person.getNiceName(), calendarFolder.getDisplayName()));

            return Optional.ofNullable(appointment.getId().getUniqueId());
        } catch (Exception ex) {
            mailService.sendCalendarSyncErrorNotification(calendarName, absence, ex.getMessage());
        }

        return Optional.empty();
    }

    private class RedirectionUrlCallback implements IAutodiscoverRedirectionUrl {

        @Override
        public boolean autodiscoverRedirectionUrlValidationCallback(String redirectionUrl) {

            return redirectionUrl.toLowerCase().startsWith("https://");
        }
    }
}
