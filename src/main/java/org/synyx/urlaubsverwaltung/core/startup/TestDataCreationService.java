package org.synyx.urlaubsverwaltung.core.startup;

import org.apache.log4j.Logger;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.springframework.web.context.ServletContextAware;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.Comment;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationInteractionService;
import org.synyx.urlaubsverwaltung.core.calendar.Day;
import org.synyx.urlaubsverwaltung.core.mail.MailNotification;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonInteractionService;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteType;
import org.synyx.urlaubsverwaltung.core.sicknote.comment.SickNoteStatus;
import org.synyx.urlaubsverwaltung.security.Role;
import org.synyx.urlaubsverwaltung.web.person.PersonForm;

import java.security.NoSuchAlgorithmException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;

import javax.servlet.ServletContext;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service
public class TestDataCreationService {

    private static final Logger LOG = Logger.getLogger(TestDataCreationService.class);

    private static final String ENVIRONMENT_PROPERTY = "env";
    private static final String DEV_ENVIRONMENT = "dev";

    private static final String USER = "test";

    private static final Boolean ACTIVE = true;
    private static final Boolean INACTIVE = false;

    @Autowired
    private PersonInteractionService personInteractionService;

    @Autowired
    private ApplicationInteractionService applicationInteractionService;

    @Autowired
    private SickNoteService sickNoteService;

    private Person user;
    private Person boss;
    private Person office;

    @PostConstruct
    public void createTestData() throws NoSuchAlgorithmException {

        String environment = System.getProperties().getProperty(ENVIRONMENT_PROPERTY);

        if (environment == null) {
            environment = DEV_ENVIRONMENT;
        }

        LOG.info("ENVIRONMENT=" + environment);

        if (environment.equals(DEV_ENVIRONMENT)) {
            LOG.info("Test data will be created...");

            user = createTestPerson(USER, "Marlene", "Muster", "mmuster@muster.de", ACTIVE, Role.USER, Role.BOSS,
                    Role.OFFICE);

            boss = createTestPerson("max", "Max", "Mustermann", "maxMuster@muster.de", ACTIVE, Role.USER, Role.BOSS);

            office = createTestPerson("klaus", "Klaus", "Müller", "mueller@muster.de", ACTIVE, Role.USER, Role.BOSS,
                    Role.OFFICE);

            createTestPerson("hdampf", "Hans", "Dampf", "dampf@foo.bar", true, Role.USER, Role.OFFICE);

            createTestPerson("horst", "Horst", "Dieter", "hdieter@muster.de", INACTIVE, Role.INACTIVE);

            createTestData(user);
            createTestData(boss);
        } else {
            LOG.info("No test data is created.");
        }
    }


    private Person createTestPerson(String login, String firstName, String lastName, String email, boolean active,
        Role... roles) throws NoSuchAlgorithmException {

        Person person = new Person();

        int currentYear = DateMidnight.now().getYear();
        Locale locale = Locale.GERMAN;

        PersonForm personForm = new PersonForm();
        personForm.setLoginName(login);
        personForm.setLastName(lastName);
        personForm.setFirstName(firstName);
        personForm.setEmail(email);
        personForm.setActive(active);
        personForm.setYear(String.valueOf(currentYear));
        personForm.setAnnualVacationDays("28.5");
        personForm.setRemainingVacationDays("5");
        personForm.setRemainingVacationDaysExpire(true);
        personForm.setValidFrom(new DateMidnight(currentYear, 1, 1));
        personForm.setWorkingDays(Arrays.asList(Day.MONDAY.getDayOfWeek(), Day.TUESDAY.getDayOfWeek(),
                Day.WEDNESDAY.getDayOfWeek(), Day.THURSDAY.getDayOfWeek(), Day.FRIDAY.getDayOfWeek()));
        personForm.setPermissions(Arrays.asList(roles));

        List<MailNotification> notifications = new ArrayList<>();

        notifications.add(MailNotification.NOTIFICATION_USER);

        if (personForm.getPermissions().contains(Role.BOSS)) {
            notifications.add(MailNotification.NOTIFICATION_BOSS);
        }

        if (personForm.getPermissions().contains(Role.OFFICE)) {
            notifications.add(MailNotification.NOTIFICATION_OFFICE);
        }

        personForm.setNotifications(notifications);

        personInteractionService.createOrUpdate(person, personForm, locale);

        return person;
    }


    private void createTestData(Person person) {

        DateMidnight now = DateMidnight.now();

        // FUTURE APPLICATIONS FOR LEAVE
        createWaitingApplication(person, VacationType.HOLIDAY, DayLength.FULL, now.plusDays(10), now.plusDays(16)); // NOSONAR
        createWaitingApplication(person, VacationType.OVERTIME, DayLength.FULL, now.plusDays(1), now.plusDays(2)); // NOSONAR
        createWaitingApplication(person, VacationType.SPECIALLEAVE, DayLength.FULL, now.plusDays(4), now.plusDays(6)); // NOSONAR

        // PAST APPLICATIONS FOR LEAVE
        createAllowedApplication(person, VacationType.HOLIDAY, DayLength.FULL, now.minusDays(20), now.minusDays(13)); // NOSONAR
        createAllowedApplication(person, VacationType.HOLIDAY, DayLength.MORNING, now.minusDays(5), now.minusDays(5)); // NOSONAR
        createAllowedApplication(person, VacationType.SPECIALLEAVE, DayLength.MORNING, now.minusDays(9), // NOSONAR
            now.minusDays(9)); // NOSONAR

        createRejectedApplication(person, VacationType.HOLIDAY, DayLength.FULL, now.minusDays(33), now.minusDays(30)); // NOSONAR

        createCancelledApplication(person, VacationType.HOLIDAY, DayLength.FULL, now.minusDays(11), now.minusDays(10)); // NOSONAR

        // SICK NOTES
        createSickNote(person, now.minusDays(10), now.minusDays(10), SickNoteType.SICK_NOTE, false); // NOSONAR
        createSickNote(person, now.minusDays(30), now.minusDays(25), SickNoteType.SICK_NOTE, true); // NOSONAR
        createSickNote(person, now.minusDays(60), now.minusDays(55), SickNoteType.SICK_NOTE_CHILD, true); // NOSONAR
        createSickNote(person, now.minusDays(44), now.minusDays(44), SickNoteType.SICK_NOTE_CHILD, false); // NOSONAR
    }


    private Application createWaitingApplication(Person person, VacationType vacationType, DayLength dayLength,
        DateMidnight startDate, DateMidnight endDate) {

        Application application = null;

        if (startAndEndDatesAreInCurrentYear(startDate, endDate)) {
            application = new Application();
            application.setPerson(person);
            application.setStartDate(startDate);
            application.setEndDate(endDate);
            application.setVacationType(vacationType);
            application.setHowLong(dayLength);
            application.setComment("Ich hätte gerne Urlaub");

            applicationInteractionService.apply(application, person);
        }

        return application;
    }


    private boolean startAndEndDatesAreInCurrentYear(DateMidnight start, DateMidnight end) {

        int currentYear = DateMidnight.now().getYear();

        return start.getYear() == currentYear && end.getYear() == currentYear;
    }


    private Application createAllowedApplication(Person person, VacationType vacationType, DayLength dayLength,
        DateMidnight startDate, DateMidnight endDate) {

        Application application = createWaitingApplication(person, vacationType, dayLength, startDate, endDate);

        if (application != null) {
            Comment comment = new Comment();
            comment.setReason("Ist ok");

            applicationInteractionService.allow(application, boss, comment);
        }

        return application;
    }


    private Application createRejectedApplication(Person person, VacationType vacationType, DayLength dayLength,
        DateMidnight startDate, DateMidnight endDate) {

        Application application = createWaitingApplication(person, vacationType, dayLength, startDate, endDate);

        if (application != null) {
            Comment comment = new Comment();
            comment.setReason("Leider nicht möglich");

            applicationInteractionService.reject(application, boss, comment);
        }

        return application;
    }


    private Application createCancelledApplication(Person person, VacationType vacationType, DayLength dayLength,
        DateMidnight startDate, DateMidnight endDate) {

        Application application = createAllowedApplication(person, vacationType, dayLength, startDate, endDate);

        if (application != null) {
            Comment comment = new Comment();
            comment.setReason("Urlaub wurde doch nicht genommen");

            applicationInteractionService.cancel(application, office, comment);
        }

        return application;
    }


    private SickNote createSickNote(Person person, DateMidnight startDate, DateMidnight endDate, SickNoteType type,
        boolean withAUB) {

        SickNote sickNote = null;

        if (startAndEndDatesAreInCurrentYear(startDate, endDate)) {
            sickNote = new SickNote();
            sickNote.setPerson(person);
            sickNote.setStartDate(startDate);
            sickNote.setEndDate(endDate);
            sickNote.setActive(ACTIVE);
            sickNote.setType(type);

            if (withAUB) {
                sickNote.setAubPresent(true);
                sickNote.setAubStartDate(startDate);
                sickNote.setAubEndDate(endDate);
            }

            sickNoteService.touch(sickNote, SickNoteStatus.CREATED, office);
        }

        return sickNote;
    }
}
