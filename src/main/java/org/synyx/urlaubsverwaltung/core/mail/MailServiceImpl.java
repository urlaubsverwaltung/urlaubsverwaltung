package org.synyx.urlaubsverwaltung.core.mail;

import org.apache.commons.lang.CharEncoding;

import org.apache.log4j.Logger;

import org.apache.velocity.app.VelocityEngine;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import org.springframework.stereotype.Service;

import org.springframework.ui.velocity.VelocityEngineUtils;

import org.springframework.util.StringUtils;

import org.synyx.urlaubsverwaltung.DateFormat;
import org.synyx.urlaubsverwaltung.core.account.domain.Account;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.Comment;
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sync.absence.Absence;
import org.synyx.urlaubsverwaltung.core.util.PropertiesUtil;

import java.io.IOException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Implementation of interface {@link MailService}.
 *
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */
@Service("mailService")
class MailServiceImpl implements MailService {

    private static final Logger LOG = Logger.getLogger(MailServiceImpl.class);

    private static final String TEMPLATE_PATH = "/org/synyx/urlaubsverwaltung/core/mail/";
    private static final String TEMPLATE_TYPE = ".vm";
    private static final String PROPERTIES_FILE = "messages.properties";

    private final JavaMailSender mailSender;
    private final VelocityEngine velocityEngine;
    private final PersonService personService;
    private final DepartmentService departmentService;

    private final String emailFrom;
    private final String emailManager;
    private final String applicationUrl;

    private Properties properties;

    @Autowired
    public MailServiceImpl(JavaMailSender mailSender, VelocityEngine velocityEngine, PersonService personService,
        DepartmentService departmentService,
        @Value("${mail.from}") String emailFrom,
        @Value("${mail.manager}") String emailManager,
        @Value("${application.url}") String applicationUrl) {

        this.mailSender = mailSender;
        this.velocityEngine = velocityEngine;
        this.personService = personService;
        this.departmentService = departmentService;

        this.emailFrom = emailFrom;
        this.emailManager = emailManager;
        this.applicationUrl = applicationUrl;

        try {
            this.properties = PropertiesUtil.load(PROPERTIES_FILE);
        } catch (IOException ex) {
            LOG.error(DateMidnight.now().toString(DateFormat.PATTERN) + "No properties file found.");
            LOG.error(ex.getMessage(), ex);
        }
    }

    @Override
    public void sendNewApplicationNotification(Application application, Comment comment) {

        Map<String, Object> model = createModelForApplicationStatusChangeMail(application,
                Optional.ofNullable(comment));
        String text = buildMailBody("new_applications", model);
        sendEmail(getBossesAndDepartmentHeads(application), "subject.application.applied.boss", text);
    }


    private Map<String, Object> createModelForApplicationStatusChangeMail(Application application,
        Optional<Comment> optionalComment) {

        Map<String, Object> model = new HashMap<>();
        model.put("application", application);

        String vacType = application.getVacationType().name();
        String length = application.getHowLong().name();
        model.put("vacationType", properties.getProperty(vacType));
        model.put("dayLength", properties.getProperty(length));
        model.put("link", applicationUrl + "web/application/" + application.getId());

        if (optionalComment.isPresent()) {
            model.put("comment", optionalComment.get());
        }

        return model;
    }


    /**
     * Build text that can be set as mail body using the given model to fill the template with the given name.
     *
     * @param  templateName  of the template to be used
     * @param  model  to fill the template
     *
     * @return  the text representation of the filled template
     */
    private String buildMailBody(String templateName, Map<String, Object> model) {

        return VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, getFullyQualifiedTemplateName(templateName),
                CharEncoding.UTF_8, model);
    }


    /**
     * Get fully qualified template name including path and file extension of the given template name.
     *
     * @param  templateName  to get the fully qualified template name of
     *
     * @return  the fully qualified template name using {@value #TEMPLATE_PATH} as path and {@value #TEMPLATE_TYPE} as
     *          file extension
     */
    private String getFullyQualifiedTemplateName(String templateName) {

        return TEMPLATE_PATH + templateName + TEMPLATE_TYPE;
    }


    private List<Person> getBossesAndDepartmentHeads(Application application) {

        List<Person> bosses = personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_BOSS);

        List<Person> allDepartmentHeads = personService.getPersonsWithNotificationType(
                MailNotification.NOTIFICATION_DEPARTMENT_HEAD);

        List<Person> departmentHeads = allDepartmentHeads.stream()
            .filter(person -> departmentService.isDepartmentHeadOfPerson(person, application.getPerson()))
            .collect(Collectors.toList());

        /**
         * NOTE:
         *
         * It's not possible that someone has both roles,
         * {@link org.synyx.urlaubsverwaltung.security.Role.BOSS} and
         * {@link org.synyx.urlaubsverwaltung.security.Role.DEPARTMENT_HEAD}.
         *
         * Thus no need to use a {@link java.util.Set} to avoid person duplicates within the returned list.
         */
        return Stream.concat(bosses.stream(), departmentHeads.stream()).collect(Collectors.toList());
    }


    protected void sendEmail(final List<Person> recipients, final String subject, final String text) {

        final String internationalizedSubject = properties.getProperty(subject);

        final List<Person> recipientsWithMailAddress = recipients.stream().filter(person ->
                    StringUtils.hasText(person.getEmail())).collect(Collectors.toList());

        if (!recipientsWithMailAddress.isEmpty()) {
            SimpleMailMessage mailMessage = new SimpleMailMessage();

            String[] addressTo = new String[recipientsWithMailAddress.size()];

            for (int i = 0; i < recipientsWithMailAddress.size(); i++) {
                Person recipient = recipientsWithMailAddress.get(i);
                addressTo[i] = recipient.getEmail();
            }

            mailMessage.setFrom(emailFrom);
            mailMessage.setTo(addressTo);
            mailMessage.setSubject(internationalizedSubject);
            mailMessage.setText(text);

            try {
                this.mailSender.send(mailMessage);

                for (Person recipient : recipientsWithMailAddress) {
                    LOG.info("Sent email to " + recipient.getEmail() + " with subject '" + internationalizedSubject
                        + "'");
                }
            } catch (MailException ex) {
                LOG.error("Sending email to " + recipientsWithMailAddress + " failed", ex);
            }
        }
    }


    @Override
    public void sendRemindBossNotification(Application application) {

        Map<String, Object> model = createModelForApplicationStatusChangeMail(application, Optional.<Comment>empty());
        String text = buildMailBody("remind", model);
        sendEmail(getBossesAndDepartmentHeads(application), "subject.application.remind", text);
    }


    @Override
    public void sendAllowedNotification(Application application, Comment comment) {

        // if application has been allowed, two emails must be sent
        // the applicant gets an email and the office gets an email

        // email to office
        Map<String, Object> modelForOffice = createModelForApplicationStatusChangeMail(application,
                Optional.ofNullable(comment));
        String textOffice = buildMailBody("allowed_office", modelForOffice);
        sendEmail(getOfficeMembers(), "subject.application.allowed.office", textOffice);

        // email to applicant
        Map<String, Object> modelForUser = createModelForApplicationStatusChangeMail(application,
                Optional.ofNullable(comment));
        String textUser = buildMailBody("allowed_user", modelForUser);
        sendEmail(Arrays.asList(application.getPerson()), "subject.application.allowed.user", textUser);
    }


    private List<Person> getOfficeMembers() {

        return personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_OFFICE);
    }


    @Override
    public void sendRejectedNotification(Application application, Comment comment) {

        Map<String, Object> model = createModelForApplicationStatusChangeMail(application,
                Optional.ofNullable(comment));
        String text = buildMailBody("rejected", model);
        sendEmail(Arrays.asList(application.getPerson()), "subject.application.rejected", text);
    }


    @Override
    public void sendReferApplicationNotification(Application application, Person recipient, Person sender) {

        Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("link", applicationUrl + "web/application/" + application.getId());
        model.put("recipient", recipient);
        model.put("sender", sender);

        String text = buildMailBody("refer", model);
        sendEmail(Arrays.asList(recipient), "subject.application.refer", text);
    }


    @Override
    public void sendConfirmation(Application application, Comment comment) {

        Map<String, Object> model = createModelForApplicationStatusChangeMail(application,
                Optional.ofNullable(comment));
        String text = buildMailBody("confirm", model);
        sendEmail(Arrays.asList(application.getPerson()), "subject.application.applied.user", text);
    }


    @Override
    public void sendAppliedForLeaveByOfficeNotification(Application application, Comment comment) {

        Map<String, Object> model = createModelForApplicationStatusChangeMail(application,
                Optional.ofNullable(comment));
        String text = buildMailBody("new_application_by_office", model);
        sendEmail(Arrays.asList(application.getPerson()), "subject.application.appliedByOffice", text);
    }


    @Override
    public void sendCancelledNotification(Application application, boolean cancelledByOffice, Comment comment) {

        String text;
        Map<String, Object> model = createModelForApplicationStatusChangeMail(application,
                Optional.ofNullable(comment));

        if (cancelledByOffice) {
            // mail to applicant anyway
            // not only if application was allowed before cancelling
            text = buildMailBody("cancelled_by_office", model);

            sendEmail(Arrays.asList(application.getPerson()), "subject.application.cancelled.user", text);
        } else {
            // application was allowed before cancelling
            // only then office gets an email
            text = buildMailBody("cancelled", model);

            // mail to office
            sendEmail(getOfficeMembers(), "subject.application.cancelled.office", text);
        }
    }


    @Override
    public void sendKeyGeneratingErrorNotification(String loginName, String exception) {

        Map<String, Object> model = new HashMap<>();
        model.put("loginName", loginName);
        model.put("exception", exception);

        String text = buildMailBody("error_key_generation", model);

        sendTechnicalNotification("subject.error.keys.generate", text);
    }


    /**
     * Sends an email to the manager of the application to inform about a technical event, e.g. if an error occurred.
     *
     * @param  subject  of the email
     * @param  text  of the body of the email
     */
    private void sendTechnicalNotification(final String subject, final String text) {

        SimpleMailMessage mailMessage = new SimpleMailMessage();

        mailMessage.setFrom(emailFrom);
        mailMessage.setTo(emailManager);
        mailMessage.setSubject(properties.getProperty(subject));
        mailMessage.setText(text);

        try {
            this.mailSender.send(mailMessage);
        } catch (MailException ex) {
            LOG.error("Sending email to " + emailManager + " failed", ex);
        }
    }


    @Override
    public void sendSignErrorNotification(Integer applicationId, String exception) {

        Map<String, Object> model = new HashMap<>();
        model.put("applicationId", applicationId);
        model.put("exception", exception);

        String text = buildMailBody("error_sign_application", model);

        sendTechnicalNotification("subject.error.keys.sign", text);
    }


    @Override
    public void sendCalendarSyncErrorNotification(String calendar, Absence absence, String exception) {

        Map<String, Object> model = new HashMap<>();
        model.put("calendar", calendar);
        model.put("absence", absence);
        model.put("exception", exception);

        String text = buildMailBody("error_calendar_sync", model);

        sendTechnicalNotification("subject.error.calendar.sync", text);
    }


    @Override
    public void sendCalendarUpdateErrorNotification(String calendar, Absence absence, String eventId,
        String exception) {

        Map<String, Object> model = new HashMap<>();
        model.put("calendar", calendar);
        model.put("absence", absence);
        model.put("eventId", eventId);
        model.put("exception", exception);

        String text = buildMailBody("error_calendar_update", model);

        sendTechnicalNotification("subject.error.calendar.update", text);
    }


    @Override
    public void sendCalendarDeleteErrorNotification(String calendar, String eventId, String exception) {

        Map<String, Object> model = new HashMap<>();
        model.put("calendar", calendar);
        model.put("eventId", eventId);
        model.put("exception", exception);

        String text = buildMailBody("error_calendar_delete", model);

        sendTechnicalNotification("subject.error.calendar.delete", text);
    }


    @Override
    public void sendSuccessfullyUpdatedAccounts(List<Account> updatedAccounts) {

        Map<String, Object> model = new HashMap<>();
        model.put("accounts", updatedAccounts);
        model.put("year", DateMidnight.now().getYear());

        String text = buildMailBody("updated_accounts", model);

        // send email to office for printing statistic
        sendEmail(getOfficeMembers(), "subject.account.updatedRemainingDays", text);

        // send email to manager to notify about update of accounts
        sendTechnicalNotification("subject.account.updatedRemainingDays", text);
    }


    @Override
    public void sendSickNoteConvertedToVacationNotification(Application application) {

        Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("link", applicationUrl + "web/application/" + application.getId());

        String text = buildMailBody("sicknote_converted", model);
        sendEmail(Arrays.asList(application.getPerson()), "subject.sicknote.converted", text);
    }


    @Override
    public void sendEndOfSickPayNotification(SickNote sickNote) {

        Map<String, Object> model = new HashMap<>();
        model.put("sickNote", sickNote);

        String text = buildMailBody("sicknote_end_of_sick_pay", model);

        sendEmail(Arrays.asList(sickNote.getPerson()), "subject.sicknote.endOfSickPay", text);
        sendEmail(getOfficeMembers(), "subject.sicknote.endOfSickPay", text);
    }


    @Override
    public void notifyHolidayReplacement(Application application) {

        Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("dayLength", properties.getProperty(application.getHowLong().name()));

        String text = buildMailBody("notify_holiday_replacement", model);

        sendEmail(Arrays.asList(application.getHolidayReplacement()), "subject.application.holidayReplacement", text);
    }
}
