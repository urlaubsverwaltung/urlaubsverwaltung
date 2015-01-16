package org.synyx.urlaubsverwaltung.core.mail;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

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

import org.springframework.util.StringUtils;

import org.synyx.urlaubsverwaltung.DateFormat;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.Comment;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.util.PropertiesUtil;

import java.io.IOException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


/**
 * Implementation of interface {@link MailService}.
 *
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */
@Service("mailService")
class MailServiceImpl implements MailService {

    private static final Logger LOG = Logger.getLogger(MailServiceImpl.class);

    private static final String PATH = "/email/";
    private static final String PROPERTIES_FILE = "messages.properties";
    private static final String TYPE = ".vm";

    // MODEL NAMES
    private static final String APPLICATION = "application";

    @Value("${email.manager}")
    protected String emailManager;

    @Value("${application.url}")
    private String applicationUrl;

    private final JavaMailSender mailSender;
    private final VelocityEngine velocityEngine;
    private final PersonService personService;

    private Properties properties;

    @Autowired
    public MailServiceImpl(JavaMailSender mailSender, VelocityEngine velocityEngine, PersonService personService) {

        this.mailSender = mailSender;
        this.velocityEngine = velocityEngine;
        this.personService = personService;

        try {
            this.properties = PropertiesUtil.load(PROPERTIES_FILE);
        } catch (IOException ex) {
            LOG.error(DateMidnight.now().toString(DateFormat.PATTERN) + "No properties file found.");
            LOG.error(ex.getMessage(), ex);
        }
    }

    /**
     * @see  MailService#sendNewApplicationNotification(org.synyx.urlaubsverwaltung.core.application.domain.Application)
     */
    @Override
    public void sendNewApplicationNotification(Application application) {

        String text = prepareMessage(application, APPLICATION, "newapplications" + TYPE, null, null, null);
        sendEmail(getBosses(), "subject.new", text);
    }


    /**
     * Prepares an email.
     *
     * @param  object  e.g. person or application that should be put in a model object
     * @param  modelName  of the model where the given object is put within
     * @param  fileName  name of the email's template file
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
            String vacType = a.getVacationType().name();
            String length = a.getHowLong().name();
            model.put("vacationType", properties.getProperty(vacType));
            model.put("dayLength", properties.getProperty(length));
            model.put("link", applicationUrl + "web/application/" + a.getId());
        }

        String text = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, PATH + fileName, model);

        return text;
    }


    private List<Person> getBosses() {

        return personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_BOSS);
    }


    protected void sendEmail(final List<Person> recipients, final String subject, final String text) {

        final String internationalizedSubject = properties.getProperty(subject);

        final List<Person> recipientsWithMailAddress = Lists.newArrayList(Iterables.filter(recipients,
                    new Predicate<Person>() {

                        @Override
                        public boolean apply(Person person) {

                            if (StringUtils.hasText(person.getEmail())) {
                                return true;
                            }

                            return false;
                        }
                    }));

        if (recipientsWithMailAddress.size() > 0) {
            MimeMessagePreparator prep = new MimeMessagePreparator() {

                @Override
                public void prepare(MimeMessage mimeMessage) throws javax.mail.MessagingException {

                    InternetAddress[] addressTo = new InternetAddress[recipientsWithMailAddress.size()];

                    for (int i = 0; i < recipientsWithMailAddress.size(); i++) {
                        Person recipient = recipientsWithMailAddress.get(i);
                        addressTo[i] = new InternetAddress(recipient.getEmail());
                    }

                    mimeMessage.setRecipients(Message.RecipientType.TO, addressTo);
                    mimeMessage.setSubject(internationalizedSubject);
                    mimeMessage.setText(text);
                }
            };

            try {
                this.mailSender.send(prep);

                for (Person recipient : recipientsWithMailAddress) {
                    LOG.info("Sent email to " + recipient.getEmail() + " with subject '" + internationalizedSubject
                        + "'");
                }
            } catch (MailException ex) {
                LOG.error("Sending email to " + recipientsWithMailAddress + " failed", ex);
            }
        }
    }


    /**
     * @see  MailService#sendRemindBossNotification(org.synyx.urlaubsverwaltung.core.application.domain.Application)
     */
    @Override
    public void sendRemindBossNotification(Application a) {

        String text = prepareMessage(a, APPLICATION, "remind" + TYPE, null, null, null);
        sendEmail(getBosses(), "subject.remind", text);
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

        sendEmail(getOfficeMembers(), "subject.allowed.office", textOffice);

        // email to applicant
        String textUser = prepareMessage(application, APPLICATION, "allowed_user" + TYPE, null, null, comment);
        sendEmail(Arrays.asList(application.getPerson()), "subject.allowed.user", textUser);
    }


    private List<Person> getOfficeMembers() {

        return personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_OFFICE);
    }


    /**
     * @see  MailService#sendRejectedNotification(org.synyx.urlaubsverwaltung.core.application.domain.Application, org.synyx.urlaubsverwaltung.core.application.domain.Comment)
     */
    @Override
    public void sendRejectedNotification(Application application, Comment comment) {

        String text = prepareMessage(application, APPLICATION, "rejected" + TYPE, null, null, comment);
        sendEmail(Arrays.asList(application.getPerson()), "subject.rejected", text);
    }


    /**
     * @see  MailService#sendReferApplicationNotification(org.synyx.urlaubsverwaltung.core.application.domain.Application,
     *       org.synyx.urlaubsverwaltung.core.person.Person, String)
     */
    @Override
    public void sendReferApplicationNotification(Application a, Person recipient, String sender) {

        String text = prepareMessage(a, APPLICATION, "refer" + TYPE, recipient.getFirstName(), sender, null);
        sendEmail(Arrays.asList(recipient), "subject.refer", text);
    }


    /**
     * @see  MailService#sendConfirmation(org.synyx.urlaubsverwaltung.core.application.domain.Application)
     */
    @Override
    public void sendConfirmation(Application application) {

        String text = prepareMessage(application, APPLICATION, "confirm" + TYPE, null, null, null);
        sendEmail(Arrays.asList(application.getPerson()), "subject.confirm", text);
    }


    /**
     * @see  MailService#sendAppliedForLeaveByOfficeNotification(org.synyx.urlaubsverwaltung.core.application.domain.Application)
     */
    @Override
    public void sendAppliedForLeaveByOfficeNotification(Application application) {

        String text = prepareMessage(application, APPLICATION, "new_application_by_office" + TYPE, null, null, null);
        sendEmail(Arrays.asList(application.getPerson()), "subject.new.app.by.office", text);
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
            sendEmail(Arrays.asList(application.getPerson()), "subject.cancelled.by.office", text);
        } else {
            // application was allowed before cancelling
            // only then office and bosses get an email

            text = prepareMessage(application, APPLICATION, "cancelled" + TYPE, null, null, comment);

            // mail to office
            sendEmail(getOfficeMembers(), "subject.cancelled", text);
        }
    }


    /**
     * @see  MailService#sendKeyGeneratingErrorNotification(String)
     */
    @Override
    public void sendKeyGeneratingErrorNotification(String loginName) {

        String text = "An error occured during key generation for person with login " + loginName + " failed.";

        sendTechnicalNotification("subject.key.error", text);
    }


    /**
     * Sends an email to the manager of the application to inform about a technical event, e.g. if an error occurred.
     *
     * @param  subject  of the email
     * @param  text  of the body of the email
     */
    private void sendTechnicalNotification(final String subject, final String text) {

        MimeMessagePreparator prep = new MimeMessagePreparator() {

            @Override
            public void prepare(MimeMessage mimeMessage) throws javax.mail.MessagingException {

                mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(emailManager));

                mimeMessage.setSubject(properties.getProperty(subject));
                mimeMessage.setText(text);
            }
        };

        try {
            this.mailSender.send(prep);
        } catch (MailException ex) {
            LOG.error("Sending email to " + emailManager + " failed", ex);
        }
    }


    /**
     * @see  MailService#sendSignErrorNotification(Integer, String)
     */
    @Override
    public void sendSignErrorNotification(Integer applicationId, String exception) {

        String text = "An error occured while signing the application with id " + applicationId + "\n" + exception;
        sendTechnicalNotification("subject.sign.error", text);
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
        sendEmail(getOfficeMembers(), "subject.account.update", text);

        // send email to manager to notify about update of accounts
        sendTechnicalNotification("subject.account.update", text);
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

        sendEmail(Arrays.asList(application.getPerson()), "subject.sicknote.converted", text);
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

        sendEmail(Arrays.asList(sickNote.getPerson()), "subject.sicknote.endOfSickPay", text);
        sendEmail(getOfficeMembers(), "subject.sicknote.endOfSickPay", text);
    }


    @Override
    public void notifyRepresentative(Application application) {

        Map<String, Object> model = new HashMap();
        model.put("application", application);
        model.put("dayLength", properties.getProperty(application.getHowLong().name()));

        String text = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, PATH + "rep.vm", model);

        sendEmail(Arrays.asList(application.getRep()), "subject.rep", text);
    }
}
