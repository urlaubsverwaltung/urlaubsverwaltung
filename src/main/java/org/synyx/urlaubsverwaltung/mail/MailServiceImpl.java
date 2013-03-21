package org.synyx.urlaubsverwaltung.mail;

import org.apache.log4j.Logger;

import org.apache.velocity.app.VelocityEngine;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import org.springframework.ui.velocity.VelocityEngineUtils;

import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.Comment;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.util.PropertiesUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


/**
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 *
 *          <p>nice tutorial: http://static.springsource.org/spring/docs/2.0.5/reference/mail.html</p>
 *
 *          <p>At the moment properties' values are set hard coded in this class (bad solution...): it would be better
 *          to read in the properties file (outcommented methods), but this was not successful yet...</p>
 */
class MailServiceImpl implements MailService {

    private static final Logger LOG = Logger.getLogger(MailServiceImpl.class);
    private static final String DATE_FORMAT = "dd.MM.yyyy";
    private static final String PATH = "/email/";
    private static final String PROPERTIES_FILE = "messages_de.properties"; // general properties
    private static final String MAIL_PROPERTIES_FILE = "mail.properties"; // custom configuration like email

    // addresses, etc.
    private static final String APPLICATION = "application";
    private static final String PERSON = "person";
    private static final String PERSONS = "persons";

    // File names
    private static final String TYPE = ".vm";
    private static final String FILE_ALLOWED_OFFICE = "allowed_office" + TYPE;
    private static final String FILE_ALLOWED_USER = "allowed_user" + TYPE;
    private static final String FILE_CANCELLED = "cancelled" + TYPE;
    private static final String FILE_CANCELLED_BY_OFFICE = "cancelled_by_office" + TYPE;
    private static final String FILE_CONFIRM = "confirm" + TYPE;
    private static final String FILE_EXPIRE = "expire" + TYPE;
    private static final String FILE_NEW_BY_OFFICE = "new_application_by_office" + TYPE;
    private static final String FILE_NEW = "newapplications" + TYPE;
    private static final String FILE_REJECTED = "rejected" + TYPE;
    private static final String FILE_REFER = "refer" + TYPE;
    private static final String FILE_REMIND = "remind" + TYPE;
    private static final String FILE_WEEKLY = "weekly" + TYPE;
    private JavaMailSender mailSender;
    private VelocityEngine velocityEngine;
    private Properties properties;
    private Properties mailProperties;

    @Value("${email.boss}")
    protected String emailBoss;

    @Value("${email.office}")
    protected String emailOffice;

    @Value("${email.all}")
    protected String emailAll;

    @Value("${email.manager}")
    protected String emailManager;

    @Autowired
    public MailServiceImpl(JavaMailSender mailSender, VelocityEngine velocityEngine) {

        this.mailSender = mailSender;
        this.velocityEngine = velocityEngine;

        try {
            this.properties = PropertiesUtil.load(PROPERTIES_FILE);
            this.mailProperties = PropertiesUtil.load(MAIL_PROPERTIES_FILE);
        } catch (Exception ex) {
            LOG.error(DateMidnight.now().toString(DATE_FORMAT) + "No properties file found.");
            LOG.error(ex.getMessage(), ex);
        }
    }

    /**
     * this method prepares an email:
     *
     * @param  object  e.g. person or application that should be put in a model object
     * @param  modelName
     * @param  fileName  name of email's template file
     *
     * @return  String text that must be put in the email as text (sending is done by method sendEmail)
     */
    private String prepareMessage(Object object, String modelName, String fileName, String reciever, String sender,
        Comment comment) {

        Map model = new HashMap();
        model.put(modelName, object);

        if (reciever != null && sender != null) {
            model.put("reciever", reciever);
            model.put("sender", sender);
        }

        if (comment != null) {
            model.put("comment", comment);
        }

        if (object.getClass().equals(Application.class)) {
            Application a = (Application) object;
            String vacType = a.getVacationType().getVacationTypeName();
            String length = a.getHowLong().getDayLength();
            model.put("vacationType", properties.getProperty(vacType));
            model.put("dayLength", properties.getProperty(length));
            model.put("link", "http://urlaubsverwaltung/web/application/" + a.getId());
        }

        String text = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, PATH + fileName, model);

        return text;
    }


    /**
     * this method gets the recipient email address, the email's subject and text with this parameters an email is build
     * and sent
     *
     * @param  recipient
     * @param  subject
     * @param  text
     */
    private void sendEmail(final String recipient, final String subject, final String text) {

        MimeMessagePreparator prep = new MimeMessagePreparator() {

            public void prepare(MimeMessage mimeMessage) throws MessagingException {

                mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));

                mimeMessage.setSubject(mailProperties.getProperty(subject));
                mimeMessage.setText(text);
            }
        };

        try {
            this.mailSender.send(prep);
        } catch (MailException ex) {
            LOG.error(DateMidnight.now().toString(DATE_FORMAT) + ": Sending the email with following subject '"
                + subject
                + "' to " + recipient + " failed.");
            LOG.error(ex.getMessage(), ex);
        }
    }


    /**
     * @see  MailService#sendExpireNotification(java.util.List)
     */
    @Override
    public void sendExpireNotification(List<Person> persons) {

        for (Person person : persons) {
            String text = prepareMessage(person, PERSON, FILE_EXPIRE, null, null, null);

            sendEmail(person.getEmail(), "subject.expire", text);
        }
    }


    /**
     * @see  MailService#sendNewApplicationNotification(org.synyx.urlaubsverwaltung.domain.Application)
     */
    @Override
    public void sendNewApplicationNotification(Application application) {

        String text = prepareMessage(application, APPLICATION, FILE_NEW, null, null, null);

        sendEmailToMultipleRecipients(emailBoss, "subject.new", text);
    }


    @Override
    public void sendRemindBossNotification(Application a) {

        String text = prepareMessage(a, APPLICATION, FILE_REMIND, null, null, null);

        sendEmailToMultipleRecipients(emailBoss, "subject.remind", text);
    }


    /**
     * This method is equivalent to the method sendEmail except that it sends the email to multiple recipients instead
     * of only to one.
     *
     * @param  recipients
     * @param  subject
     * @param  text
     */
    private void sendEmailToMultipleRecipients(final String recipients, final String subject, final String text) {

        MimeMessagePreparator prep = new MimeMessagePreparator() {

            public void prepare(MimeMessage mimeMessage) throws MessagingException {

                ArrayList<String> recipientsList = new ArrayList<String>();
                StringTokenizer st = new StringTokenizer(recipients, ",");

                while (st.hasMoreTokens()) {
                    recipientsList.add(st.nextToken());
                }

                int sizeTo = recipientsList.size();

                InternetAddress[] addressTo = new InternetAddress[sizeTo];

                for (int i = 0; i < sizeTo; i++) {
                    addressTo[i] = new InternetAddress(recipientsList.get(i).toString());
                }

                mimeMessage.setRecipients(Message.RecipientType.TO, addressTo);

                mimeMessage.setSubject(mailProperties.getProperty(subject));
                mimeMessage.setText(text);
            }
        };

        try {
            this.mailSender.send(prep);
        } catch (MailException ex) {
            LOG.error(DateMidnight.now().toString(DATE_FORMAT) + ": Sending the email with following subject '"
                + subject
                + "' to following recipients " + " failed.");
            LOG.error(ex.getMessage(), ex);
        }
    }


    /**
     * @see  MailService#sendAllowedNotification(org.synyx.urlaubsverwaltung.domain.Application)
     */
    @Override
    public void sendAllowedNotification(Application application, Comment comment) {

        // if application has been allowed, two emails must be sent
        // the applicant gets an email and the office gets an email

        // email to office
        String textOffice = prepareMessage(application, APPLICATION, FILE_ALLOWED_OFFICE, null, null, comment);
        sendEmail(emailOffice, "subject.allowed.office", textOffice);

        // email to applicant
        String textUser = prepareMessage(application, APPLICATION, FILE_ALLOWED_USER, null, null, comment);
        sendEmail(application.getPerson().getEmail(), "subject.allowed.user", textUser);
    }


    /**
     * @see  MailService#sendRejectedNotification(org.synyx.urlaubsverwaltung.domain.Application)
     */
    @Override
    public void sendRejectedNotification(Application application, Comment comment) {

        String text = prepareMessage(application, APPLICATION, FILE_REJECTED, null, null, comment);
        sendEmail(application.getPerson().getEmail(), "subject.rejected", text);
    }


    /**
     * @see  MailService#sendReferApplicationNotification(org.synyx.urlaubsverwaltung.domain.Application, org.synyx.urlaubsverwaltung.domain.Person)
     */
    @Override
    public void sendReferApplicationNotification(Application a, Person reciever, String sender) {

        String text = prepareMessage(a, APPLICATION, FILE_REFER, reciever.getFirstName(), sender, null);
        sendEmail(reciever.getEmail(), "subject.refer", text);
    }


    /**
     * @see  MailService#sendConfirmation(org.synyx.urlaubsverwaltung.domain.Application)
     */
    @Override
    public void sendConfirmation(Application application) {

        String text = prepareMessage(application, APPLICATION, FILE_CONFIRM, null, null, null);
        sendEmail(application.getPerson().getEmail(), "subject.confirm", text);
    }


    @Override
    public void sendAppliedForLeaveByOfficeNotification(Application application) {

        String text = prepareMessage(application, APPLICATION, FILE_NEW_BY_OFFICE, null, null, null);
        sendEmail(application.getPerson().getEmail(), "subject.new.app.by.office", text);
    }


    /**
     * @see  MailService#sendWeeklyVacationForecast(java.util.List)
     */
    @Override
    public void sendWeeklyVacationForecast(Map<String, Person> persons) {

        Map<String, Object> model = new HashMap();
        model.put("persons", persons);

        String text = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, PATH + FILE_WEEKLY, model);

        sendEmail(emailAll, "subject.weekly", text);
    }


    /**
     * @see  MailService#sendCancelledNotification(org.synyx.urlaubsverwaltung.domain.Application, boolean)
     */
    @Override
    public void sendCancelledNotification(Application application, boolean cancelledByOffice, Comment comment) {

        String text;

        if (cancelledByOffice) {
            // mail to applicant anyway
            // not only if application was allowed before cancelling
            text = prepareMessage(application, APPLICATION, FILE_CANCELLED_BY_OFFICE, null, null, comment);
            sendEmail(application.getPerson().getEmail(), "subject.cancelled.by.office", text);
        } else {
            // application was allowed before cancelling
            // only then office and bosses get an email

            text = prepareMessage(application, APPLICATION, FILE_CANCELLED, null, null, comment);

            // mail to office
            sendEmail(emailOffice, "subject.cancelled", text);

            // mail to bosses
            sendEmailToMultipleRecipients(emailBoss, "subject.cancelled", text);
        }
    }


    @Override
    public void sendKeyGeneratingErrorNotification(String loginName) {

        String text = "An error occured during key generation for person with login " + loginName + " failed.";
        sendEmail(emailManager, "subject.key.error", text);
    }


    @Override
    public void sendSignErrorNotification(Integer applicationId, String exception) {

        String text = "An error occured while signing the application with id " + applicationId + "\n" + exception;

        sendEmail(emailManager, "subject.sign.error", text);
    }


    @Override
    public void sendPropertiesErrorNotification(String propertyName) {

        String text = "The value of the property key '" + propertyName
            + "' seems to be invalid. Please control and correct it if necessary.";
        sendEmail(emailManager, "subject.prop.error", text);
    }


    @Override
    public void sendRemindingBossAboutWaitingApplicationsNotification(List<Application> apps) {

        Map<String, Object> model = new HashMap();
        model.put("applications", apps);

        String text = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, PATH + "jmx_remind_boss.vm", model);

        sendEmail(emailManager, "subject.remind.boss", text);
    }


    @Override
    public void sendSuccessfullyUpdatedAccounts(String content) {

        String text = "Stand Resturlaubstage zum 1. Januar " + DateMidnight.now().getYear() + " (mitgenommene Resturlaubstage aus dem Vorjahr)" + "\n\n" + content;

        // send email to office for printing statistic
        sendEmail(emailOffice, "subject.account.update", text);
        
        // send email to manager to notify about update of accounts
        sendEmail(emailManager, "subject.account.update", text);
        
    }

}
