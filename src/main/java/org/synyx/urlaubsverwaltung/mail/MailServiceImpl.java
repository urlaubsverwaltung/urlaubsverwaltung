package org.synyx.urlaubsverwaltung.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.MailSettings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Collections.singletonList;


/**
 * Implementation of interface {@link MailService}.
 */
@Service("mailService")
class MailServiceImpl implements MailService {

    private static final Locale LOCALE = Locale.GERMAN;

    private final MessageSource messageSource;
    private final MailBuilder mailBuilder;
    private final MailSender mailSender;
    private final RecipientService recipientService;
    private final SettingsService settingsService;

    @Autowired
    MailServiceImpl(MessageSource messageSource, MailBuilder mailBuilder, MailSender mailSender,
                    RecipientService recipientService, SettingsService settingsService) {

        this.messageSource = messageSource;
        this.mailBuilder = mailBuilder;
        this.mailSender = mailSender;
        this.recipientService = recipientService;
        this.settingsService = settingsService;
    }

    @Override
    public void sendMailTo(MailNotification mailNotification, String subjectMessageKey, String templateName, Map<String, Object> model) {

        final List<Person> persons = recipientService.getRecipientsWithNotificationType(mailNotification);
        sendMailToPersons(persons, subjectMessageKey, templateName, model);
    }

    @Override
    public void sendMailTo(Person person, String subjectMessageKey, String templateName, Map<String, Object> model) {

        final List<Person> persons = singletonList(person);
        sendMailToPersons(persons, subjectMessageKey, templateName, model);
    }

    @Override
    public void sendMailToEach(List<Person> persons, String subjectMessageKey, String templateName, Map<String, Object> model, Object... args) {

        persons.forEach(person -> {
            model.put("recipient", person);
            final List<String> mailAddress = recipientService.getMailAddresses(person);
            sendMailToRecipients(mailAddress, subjectMessageKey, templateName, model, args);
        });
    }

    @Override
    public void sendTechnicalMail(String subjectMessageKey, String templateName, Map<String, Object> model) {

        MailSettings mailSettings = settingsService.getSettings().getMailSettings();
        sendMailToRecipients(singletonList(mailSettings.getAdministrator()), subjectMessageKey, templateName, model);
    }

    private void sendMailToPersons(List<Person> persons, String subjectMessageKey, String templateName, Map<String, Object> model) {

        final List<String> recipients = recipientService.getMailAddresses(persons);
        sendMailToRecipients(recipients, subjectMessageKey, templateName, model);
    }

    private void sendMailToRecipients(List<String> recipients, String subjectMessageKey, String templateName, Map<String, Object> model, Object... args) {

        MailSettings mailSettings = getMailSettings();
        model.put("baseLinkURL", mailSettings.getBaseLinkURL());

        final String subject = getTranslation(subjectMessageKey, args);
        final String text = mailBuilder.buildMailBody(templateName, model, LOCALE);

        mailSender.sendEmail(mailSettings, recipients, subject, text);
    }

    /*
    @Override
    public void sendRemindForWaitingApplicationsReminderNotification(List<Application> waitingApplications) {

        /*
         * whats happening here?
         *
         * application a
         * person p
         *
         * map application to list of boss/department head
         * a_1 -> (p_1, p_2); a_2 -> (p_1, p_3)
         *
         * collect list of application grouped by boss/department head
         * p_1 -> (a_1, a_2); p_2 -> (a_1); (p_3 -> a_2)
         *
         * See: http://stackoverflow.com/questions/33086686/java-8-stream-collect-and-group-by-objects-that-map-to-multiple-keys
         *
        Map<Person, List<Application>> applicationsPerRecipient = waitingApplications.stream()
            .flatMap(application ->
                recipientService.getRecipientsForAllowAndRemind(application)
                    .stream()
                    .map(person -> new AbstractMap.SimpleEntry<>(person, application)))
            .collect(groupingBy(Map.Entry::getKey, mapping(Map.Entry::getValue, toList())));

        for (Map.Entry<Person, List<Application>> entry : applicationsPerRecipient.entrySet()) {
            MailSettings mailSettings = getMailSettings();

            List<Application> applications = entry.getValue();
            Person recipient = entry.getKey();

            Map<String, Object> model = new HashMap<>();
            model.put("applicationList", applications);
            model.put("recipient", recipient);
            model.put("settings", mailSettings);

            final List<String> recipients = recipientService.getMailAddresses(recipient);
            final String subject = getTranslation("subject.application.cronRemind");
            final String text = mailBuilder.buildMailBody("cron_remind", model, LOCALE);

            mailSender.sendEmail(mailSettings, recipients, subject, text);
        }
    }
*/

    /*
    @Test
    public void ensureSendRemindForWaitingApplicationsReminderNotification() throws Exception {

        // PERSONs
        Person personDepartmentA = TestDataCreator.createPerson("personDepartmentA");
        Person personDepartmentB = TestDataCreator.createPerson("personDepartmentB");
        Person personDepartmentC = TestDataCreator.createPerson("personDepartmentC");

        // APPLICATIONs
        Application applicationA = createApplication(personDepartmentA);
        Application applicationB = createApplication(personDepartmentB);
        Application applicationC = createApplication(personDepartmentC);

        // DEPARTMENT HEADs
        Person departmentHeadA = TestDataCreator.createPerson("headAC", "Heinz", "Wurst", "headAC@firma.test");
        Person departmentHeadB = TestDataCreator.createPerson("headB", "Michel", "Mustermann", "headB@firma.test");

        when(personService.getPersonsWithNotificationType(NOTIFICATION_DEPARTMENT_HEAD))
            .thenReturn(Arrays.asList(departmentHeadA, departmentHeadB));

        when(departmentService.isDepartmentHeadOfPerson(eq(departmentHeadA), eq(personDepartmentA)))
            .thenReturn(true);
        when(departmentService.isDepartmentHeadOfPerson(eq(departmentHeadB), eq(personDepartmentB)))
            .thenReturn(true);
        when(departmentService.isDepartmentHeadOfPerson(eq(departmentHeadA), eq(personDepartmentC)))
            .thenReturn(true);

        sut.sendRemindForWaitingApplicationsReminderNotification(Arrays.asList(applicationA, applicationB,
            applicationC));

        verifyInbox(boss, Arrays.asList(applicationA, applicationB, applicationC));
        verifyInbox(departmentHeadA, Arrays.asList(applicationA, applicationC));
        verifyInbox(departmentHeadB, singletonList(applicationB));
    }



    private void verifyInbox(Person inboxOwner, List<Application> applications) throws MessagingException, IOException {

        List<Message> inbox = Mailbox.get(inboxOwner.getEmail());
        assertTrue(inboxOwner.getLoginName() + " should get one email", inbox.size() == 1);

        Message msg = inbox.get(0);

        assertTrue("Wrong subject in Mail for " + inboxOwner.getLoginName(),
            msg.getSubject().contains("Erinnerung für wartende Urlaubsanträge"));

        String content = (String) msg.getContent();

        assertTrue(content.contains("Hallo " + inboxOwner.getNiceName()));

        for (Application application : applications) {
            assertTrue(content.contains(application.getApplier().getNiceName()));
            assertTrue(content.contains("http://urlaubsverwaltung/web/application/1234"));
        }
    }
     */
    private String getTranslation(String key, Object... args) {

        return messageSource.getMessage(key, args, LOCALE);
    }


    private MailSettings getMailSettings() {

        return settingsService.getSettings().getMailSettings();
    }
}
