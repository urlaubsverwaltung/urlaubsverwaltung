package org.synyx.urlaubsverwaltung.core.mail;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.apache.commons.lang.CharEncoding;

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

    private static final String PATH = "/org/synyx/urlaubsverwaltung/core/mail/";
    private static final String PROPERTIES_FILE = "messages.properties";
    private static final String TYPE = ".vm";

    @Value("${mail.manager}")
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

    @Override
    public void sendNewApplicationNotification(Application application) {

        String text = prepareMessage(application, "newapplications" + TYPE, Optional.<Comment>absent());
        sendEmail(getBosses(), "subject.new", text);
    }


    /**
     * Prepares an email.
     *
     * @param  application  that should be put in the model
     * @param  fileName  name of the email's template file
     * @param  optionalComment  that should be put in the model if present
     *
     * @return  String text that must be put in the email as text (sending is done by method sendEmail)
     */
    private String prepareMessage(Application application, String fileName, Optional<Comment> optionalComment) {

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

        return VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, PATH + fileName, CharEncoding.UTF_8, model);
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


    @Override
    public void sendRemindBossNotification(Application application) {

        String text = prepareMessage(application, "remind" + TYPE, Optional.<Comment>absent());
        sendEmail(getBosses(), "subject.remind", text);
    }


    @Override
    public void sendAllowedNotification(Application application, Comment comment) {

        // if application has been allowed, two emails must be sent
        // the applicant gets an email and the office gets an email

        // email to office
        String textOffice = prepareMessage(application, "allowed_office" + TYPE, Optional.fromNullable(comment));

        sendEmail(getOfficeMembers(), "subject.allowed.office", textOffice);

        // email to applicant
        String textUser = prepareMessage(application, "allowed_user" + TYPE, Optional.fromNullable(comment));
        sendEmail(Arrays.asList(application.getPerson()), "subject.allowed.user", textUser);
    }


    private List<Person> getOfficeMembers() {

        return personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_OFFICE);
    }


    @Override
    public void sendRejectedNotification(Application application, Comment comment) {

        String text = prepareMessage(application, "rejected" + TYPE, Optional.fromNullable(comment));
        sendEmail(Arrays.asList(application.getPerson()), "subject.rejected", text);
    }


    @Override
    public void sendReferApplicationNotification(Application application, Person recipient, Person sender) {

        Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("link", applicationUrl + "web/application/" + application.getId());
        model.put("recipient", recipient);
        model.put("sender", sender);

        String text = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, PATH + "refer" + TYPE,
                CharEncoding.UTF_8, model);
        sendEmail(Arrays.asList(recipient), "subject.refer", text);
    }


    @Override
    public void sendConfirmation(Application application) {

        String text = prepareMessage(application, "confirm" + TYPE, Optional.<Comment>absent());
        sendEmail(Arrays.asList(application.getPerson()), "subject.confirm", text);
    }


    @Override
    public void sendAppliedForLeaveByOfficeNotification(Application application) {

        String text = prepareMessage(application, "new_application_by_office" + TYPE, Optional.<Comment>absent());
        sendEmail(Arrays.asList(application.getPerson()), "subject.new.app.by.office", text);
    }


    @Override
    public void sendCancelledNotification(Application application, boolean cancelledByOffice, Comment comment) {

        String text;

        if (cancelledByOffice) {
            // mail to applicant anyway
            // not only if application was allowed before cancelling
            text = prepareMessage(application, "cancelled_by_office" + TYPE, Optional.fromNullable(comment));
            sendEmail(Arrays.asList(application.getPerson()), "subject.cancelled.by.office", text);
        } else {
            // application was allowed before cancelling
            // only then office gets an email

            text = prepareMessage(application, "cancelled" + TYPE, Optional.fromNullable(comment));

            // mail to office
            sendEmail(getOfficeMembers(), "subject.cancelled", text);
        }
    }


    @Override
    public void sendKeyGeneratingErrorNotification(String loginName) {

        String text = "An error occurred during key generation for person with login " + loginName + " failed.";

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


    @Override
    public void sendSignErrorNotification(Integer applicationId, String exception) {

        String text = "An error occurred while signing the application with id " + applicationId + "\n" + exception;
        sendTechnicalNotification("subject.sign.error", text);
    }


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


    @Override
    public void sendSickNoteConvertedToVacationNotification(Application application) {

        Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("link", applicationUrl + "web/application/" + application.getId());

        String text = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, PATH + "sicknote_converted" + TYPE,
                CharEncoding.UTF_8, model);

        sendEmail(Arrays.asList(application.getPerson()), "subject.sicknote.converted", text);
    }


    @Override
    public void sendEndOfSickPayNotification(SickNote sickNote) {

        Map<String, Object> model = new HashMap<>();
        model.put("sickNote", sickNote);

        String text = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine,
                PATH + "sicknote_end_of_sick_pay" + TYPE, CharEncoding.UTF_8, model);

        sendEmail(Arrays.asList(sickNote.getPerson()), "subject.sicknote.endOfSickPay", text);
        sendEmail(getOfficeMembers(), "subject.sicknote.endOfSickPay", text);
    }


    @Override
    public void notifyRepresentative(Application application) {

        Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("dayLength", properties.getProperty(application.getHowLong().name()));

        String text = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, PATH + "rep" + TYPE,
                CharEncoding.UTF_8, model);

        sendEmail(Arrays.asList(application.getRep()), "subject.rep", text);
    }
}
