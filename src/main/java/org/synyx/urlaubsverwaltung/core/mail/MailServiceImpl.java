package org.synyx.urlaubsverwaltung.core.mail;

import org.apache.log4j.Logger;
import org.apache.velocity.app.VelocityEngine;
import org.joda.time.DateMidnight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.springframework.ui.velocity.VelocityEngineUtils;
import org.synyx.urlaubsverwaltung.DateFormat;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.Comment;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.util.PropertiesUtil;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.*;


/**
 * Implementation of interface {@link MailService}.
 *
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */
@Service
class MailServiceImpl implements MailService {

    private static final Logger LOG = Logger.getLogger(MailServiceImpl.class);

    private static final String PATH = "/email/";
    private static final String PROPERTIES_FILE = "messages.properties"; // general properties
    private static final String TYPE = ".vm";

    // MODEL NAMES
    private static final String APPLICATION = "application";
    private static final String PERSON = "person";

    private JavaMailSender mailSender;
    private VelocityEngine velocityEngine;
    private Properties properties;

    @Value("${email.boss}")
    protected String emailBoss;

    @Value("${email.office}")
    protected String emailOffice;

    @Value("${email.all}")
    protected String emailAll;

    @Value("${email.manager}")
    protected String emailManager;

    @Value("${application.url}")
    protected String applicationUrl;

    @Autowired
    public MailServiceImpl(JavaMailSender mailSender, VelocityEngine velocityEngine) {

        this.mailSender = mailSender;
        this.velocityEngine = velocityEngine;

        try {
            this.properties = PropertiesUtil.load(PROPERTIES_FILE);
        } catch (Exception ex) {
            LOG.error(DateMidnight.now().toString(DateFormat.PATTERN) + "No properties file found.");
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
    private String prepareMessage(Object object, String modelName, String fileName, String recipient, String sender,
        Comment comment) {

        Map model = new HashMap();
        model.put(modelName, object);

        if (recipient != null && sender != null) {
            model.put("recipient", recipient);
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
            model.put("link", applicationUrl + "web/application/" + a.getId());
        }

        String text = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, PATH + fileName, model);

        return text;
    }


    /**
     * this method gets the recipient email address, the email's subject and text with this parameters an email is build
     * and sent.
     *
     * @param  recipient
     * @param  subject
     * @param  text
     */
    private void sendEmail(final String recipient, final String subject, final String text) {

        MimeMessagePreparator prep = new MimeMessagePreparator() {

            @Override
            public void prepare(MimeMessage mimeMessage) throws javax.mail.MessagingException {

                mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));

                mimeMessage.setSubject(properties.getProperty(subject));
                mimeMessage.setText(text);
            }
        };

        try {
            this.mailSender.send(prep);
        } catch (MailException ex) {
            LOG.error(DateMidnight.now().toString(DateFormat.PATTERN) + ": Sending the email with following subject '"
                + properties.getProperty(subject)
                + "' to " + recipient + " failed.");
            LOG.error(ex.getMessage(), ex);
        }
    }


    /**
     * @see  MailService#sendNewApplicationNotification(org.synyx.urlaubsverwaltung.core.application.domain.Application)
     */
    @Override
    public void sendNewApplicationNotification(Application application) {

        String text = prepareMessage(application, APPLICATION, "newapplications" + TYPE, null, null, null);

        sendEmailToMultipleRecipients(emailBoss, "subject.new", text);
    }


    /**
     * @see  MailService#sendRemindBossNotification(org.synyx.urlaubsverwaltung.core.application.domain.Application)
     */
    @Override
    public void sendRemindBossNotification(Application a) {

        String text = prepareMessage(a, APPLICATION, "remind" + TYPE, null, null, null);

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

            @Override
            public void prepare(MimeMessage mimeMessage) throws javax.mail.MessagingException {

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

                mimeMessage.setSubject(properties.getProperty(subject));
                mimeMessage.setText(text);
            }
        };

        try {
            this.mailSender.send(prep);
        } catch (MailException ex) {
            LOG.error(DateMidnight.now().toString(DateFormat.PATTERN) + ": Sending the email with following subject '"
                + properties.getProperty(subject)
                + "' to following recipients " + " failed.");
            LOG.error(ex.getMessage(), ex);
        }
    }


    /**
     * @see  MailService#sendAllowedNotification(org.synyx.urlaubsverwaltung.core.application.domain.Application, org.synyx.urlaubsverwaltung.core.application.domain.Comment)
     */
    @Override
    public void sendAllowedNotification(Application application, Comment comment) {

        // if application has been allowed, two emails must be sent
        // the applicant gets an email and the office gets an email

        // email to office
        String textOffice = prepareMessage(application, APPLICATION, "allowed_office" + TYPE, null, null, comment);
        sendEmail(emailOffice, "subject.allowed.office", textOffice);

        // email to applicant
        String textUser = prepareMessage(application, APPLICATION, "allowed_user" + TYPE, null, null, comment);
        sendEmail(application.getPerson().getEmail(), "subject.allowed.user", textUser);
    }


    /**
     * @see  MailService#sendRejectedNotification(org.synyx.urlaubsverwaltung.core.application.domain.Application, org.synyx.urlaubsverwaltung.core.application.domain.Comment)
     */
    @Override
    public void sendRejectedNotification(Application application, Comment comment) {

        String text = prepareMessage(application, APPLICATION, "rejected" + TYPE, null, null, comment);
        sendEmail(application.getPerson().getEmail(), "subject.rejected", text);
    }


    /**
     * @see  MailService#sendReferApplicationNotification(org.synyx.urlaubsverwaltung.core.application.domain.Application,
     *       org.synyx.urlaubsverwaltung.core.person.Person, String)
     */
    @Override
    public void sendReferApplicationNotification(Application a, Person recipient, String sender) {

        String text = prepareMessage(a, APPLICATION, "refer" + TYPE, recipient.getFirstName(), sender, null);
        sendEmail(recipient.getEmail(), "subject.refer", text);
    }


    /**
     * @see  MailService#sendConfirmation(org.synyx.urlaubsverwaltung.core.application.domain.Application)
     */
    @Override
    public void sendConfirmation(Application application) {

        String text = prepareMessage(application, APPLICATION, "confirm" + TYPE, null, null, null);
        sendEmail(application.getPerson().getEmail(), "subject.confirm", text);
    }


    /**
     * @see  MailService#sendAppliedForLeaveByOfficeNotification(org.synyx.urlaubsverwaltung.core.application.domain.Application)
     */
    @Override
    public void sendAppliedForLeaveByOfficeNotification(Application application) {

        String text = prepareMessage(application, APPLICATION, "new_application_by_office" + TYPE, null, null, null);
        sendEmail(application.getPerson().getEmail(), "subject.new.app.by.office", text);
    }


    /**
     * @see  MailService#sendCancelledNotification(org.synyx.urlaubsverwaltung.core.application.domain.Application, boolean,
     *       org.synyx.urlaubsverwaltung.core.application.domain.Comment)
     */
    @Override
    public void sendCancelledNotification(Application application, boolean cancelledByOffice, Comment comment) {

        String text;

        if (cancelledByOffice) {
            // mail to applicant anyway
            // not only if application was allowed before cancelling
            text = prepareMessage(application, APPLICATION, "cancelled_by_office" + TYPE, null, null, comment);
            sendEmail(application.getPerson().getEmail(), "subject.cancelled.by.office", text);
        } else {
            // application was allowed before cancelling
            // only then office and bosses get an email

            text = prepareMessage(application, APPLICATION, "cancelled" + TYPE, null, null, comment);

            // mail to office
            sendEmail(emailOffice, "subject.cancelled", text);

            // mail to bosses
            sendEmailToMultipleRecipients(emailBoss, "subject.cancelled", text);
        }
    }


    /**
     * @see  MailService#sendKeyGeneratingErrorNotification(String)
     */
    @Override
    public void sendKeyGeneratingErrorNotification(String loginName) {

        String text = "An error occured during key generation for person with login " + loginName + " failed.";
        sendEmail(emailManager, "subject.key.error", text);
    }


    /**
     * @see  MailService#sendSignErrorNotification(Integer, String)
     */
    @Override
    public void sendSignErrorNotification(Integer applicationId, String exception) {

        String text = "An error occured while signing the application with id " + applicationId + "\n" + exception;

        sendEmail(emailManager, "subject.sign.error", text);
    }


    /**
     * @see  MailService#sendSuccessfullyUpdatedAccounts(String)
     */
    @Override
    public void sendSuccessfullyUpdatedAccounts(String content) {

        // TODO: oje...hard coded text...
        String text = "Stand Resturlaubstage zum 1. Januar " + DateMidnight.now().getYear()
            + " (mitgenommene Resturlaubstage aus dem Vorjahr)" + "\n\n" + content;

        // send email to office for printing statistic
        sendEmail(emailOffice, "subject.account.update", text);

        // send email to manager to notify about update of accounts
        sendEmail(emailManager, "subject.account.update", text);
    }


    /**
     * @see  org.synyx.urlaubsverwaltung.core.mail.MailService#sendSickNoteConvertedToVacationNotification(Application)
     */
    @Override
    public void sendSickNoteConvertedToVacationNotification(Application application) {

        Map<String, Object> model = new HashMap();
        model.put("application", application);
        model.put("link", applicationUrl + "web/application/" + application.getId());

        String text = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, PATH + "sicknote_converted.vm",
                model);

        sendEmail(application.getPerson().getEmail(), "subject.sicknote.converted", text);
    }


    /**
     * @see  MailService#sendEndOfSickPayNotification(org.synyx.urlaubsverwaltung.core.sicknote.SickNote)
     */
    @Override
    public void sendEndOfSickPayNotification(SickNote sickNote) {

        Map<String, Object> model = new HashMap();
        model.put("sickNote", sickNote);

        String text = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, PATH + "sicknote_end_of_sick_pay.vm",
                model);

        sendEmail(sickNote.getPerson().getEmail(), "subject.sicknote.endOfSickPay", text);
        sendEmail(emailOffice, "subject.sicknote.endOfSickPay", text);
    }


    @Override
    public void notifyRepresentative(Application application) {

        Map<String, Object> model = new HashMap();
        model.put("application", application);
        model.put("dayLength", properties.getProperty(application.getHowLong().getDayLength()));

        String text = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, PATH + "rep.vm", model);

        sendEmail(application.getRep().getEmail(), "subject.rep", text);
    }
}
