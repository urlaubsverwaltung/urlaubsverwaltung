/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.service;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import org.synyx.urlaubsverwaltung.domain.Antrag;
import org.synyx.urlaubsverwaltung.domain.Person;

import java.util.ArrayList;
import java.util.List;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


/**
 * @author  johannes
 */
public class MailServiceImpl implements MailService {

    // see here: http://static.springsource.org/spring/docs/2.0.5/reference/mail.html

    private JavaMailSender mailSender;
    private String absender = "urlaubsverwaltung@synyx.de";
    private String sternchen = "stern@synyx.de";

    @Autowired
    public MailServiceImpl(JavaMailSender mailSender) {

        this.mailSender = mailSender;
    }

    // darf man Parameter wirklich final setzen??
    @Override
    public void sendDecayNotification(List<Person> persons) {

        for (final Person person : persons) {
            MimeMessagePreparator prep = new MimeMessagePreparator() {

                @Override
                public void prepare(MimeMessage mimeMessage) throws Exception {

                    mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(person.getEmail()));
                    mimeMessage.setFrom(new InternetAddress(absender));
                    mimeMessage.setText("Liebe/-r " + person.getFirstName() + " " + person.getLastName() + ","
                        + "\n\ndu hast aus dem letzten Kalenderjahr Resturlaub ins neue Jahr mitgenommen."
                        + "\nBitte beachte, dass dieser zum 1. April des neuen Jahres verfällt."
                        + "\nNimm' dir also rechtzeitig Urlaub, damit der Resturlaub nicht verfällt."
                        + "\n\nUrlaubsverwaltung");
                }
            };

            try {
                this.mailSender.send(prep);
            } catch (MailException ex) {
                System.err.println(ex.getMessage());
            }
        }
    }


    @Override
    public void sendNewRequestsNotification(List<Person> persons, List<Antrag> requests) {

        String details = "";
        String emails = "";

        for (Antrag antrag : requests) {
            details = details + "\n" + antrag.getPerson().getFirstName() + " " + antrag.getPerson().getLastName()
                + " : " + antrag.getStartDate() + " bis " + antrag.getEndDate();
        }

        final String beantragungen = details;

        for (Person beauftragter : persons) {
            emails = emails + beauftragter.getEmail() + ", ";
        }

        final String emailAdressen = emails;

        MimeMessagePreparator prep = new MimeMessagePreparator() {

            @Override
            public void prepare(MimeMessage mimeMessage) throws Exception {

                mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(emailAdressen));
                mimeMessage.setFrom(new InternetAddress(absender));
                mimeMessage.setText("Hallo Chef-Etage, "
                    + "\n\nes liegen neue Urlaubsanträge vor, die es zu bearbeiten gilt: "
                    + "\n" + beantragungen
                    + "\n\nUrlaubsverwaltung");
            }
        };

        try {
            this.mailSender.send(prep);
        } catch (MailException ex) {
            System.err.println(ex.getMessage());
        }
    }


    @Override
    public void sendApprovedNotification(final Person person, final Antrag request) {

        // Email ans Office: es liegt ein neuer Antrag vor
        MimeMessagePreparator prepOffice = new MimeMessagePreparator() {

            @Override
            public void prepare(MimeMessage mimeMessage) throws Exception {

                mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress("office@synyx.de"));
                mimeMessage.setFrom(new InternetAddress(absender));
                mimeMessage.setText("Hallo Office, "
                    + "\n\nes liegt ein neuer bewilligter Antrag vor."
                    + "\n\nUrlaubsverwaltung");
            }
        };

        try {
            this.mailSender.send(prepOffice);
        } catch (MailException ex) {
            System.err.println(ex.getMessage());
        }

        MimeMessagePreparator prepUser = new MimeMessagePreparator() {

            @Override
            public void prepare(MimeMessage mimeMessage) throws Exception {

                mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(person.getEmail()));
                mimeMessage.setFrom(new InternetAddress(absender));
                mimeMessage.setText("Hallo " + person.getFirstName() + " " + person.getLastName() + ","
                    + "\n\ndein Antrag auf Urlaub für den Zeitraum von " + request.getStartDate() + " bis "
                    + request.getEndDate()
                    + " wurde bewilligt."
                    + "\n\nUrlaubsverwaltung");
            }
        };

        try {
            this.mailSender.send(prepUser);
        } catch (MailException ex) {
            System.err.println(ex.getMessage());
        }
    }


    @Override
    public void sendDeclinedNotification(final Antrag request) {

        final Person person = request.getPerson();

        MimeMessagePreparator prep = new MimeMessagePreparator() {

            @Override
            public void prepare(MimeMessage mimeMessage) throws Exception {

                mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(person.getEmail()));
                mimeMessage.setFrom(new InternetAddress(absender));
                mimeMessage.setText("Hallo " + person.getFirstName() + " " + person.getLastName() + ","
                    + "\n\ndein Antrag für Urlaub im Zeitraum vom " + request.getStartDate() + " bis "
                    + request.getEndDate() + " wurde von " + request.getBoss().getFirstName() + " "
                    + request.getBoss().getLastName() + " leider abgelehnt mit folgender Begründung: "
                    + "\n" + request.getReasonToDecline()
                    + "\n\nUrlaubsverwaltung");
            }
        };

        try {
            this.mailSender.send(prep);
        } catch (MailException ex) {
            System.err.println(ex.getMessage());
        }
    }


    @Override
    public void sendConfirmation(Antrag request) {

        final Person person = request.getPerson();

        MimeMessagePreparator prep = new MimeMessagePreparator() {

            @Override
            public void prepare(MimeMessage mimeMessage) throws Exception {

                mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(person.getEmail()));
                mimeMessage.setFrom(new InternetAddress(absender));
                mimeMessage.setText("Hallo " + person.getFirstName() + " " + person.getLastName() + ","
                    + "\n\ndein Antrag wurde erfolgreich gestellt und wird in Kürze durch einen der Chefs bearbeitet werden."
                    + "\n\nUrlaubsverwaltung");
            }
        };

        try {
            this.mailSender.send(prep);
        } catch (MailException ex) {
            System.err.println(ex.getMessage());
        }
    }


    @Override
    public void sendBalance(Object balanceObject) {

        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public void sendWeeklyVacationForecast(List<Person> urlauber) {

        List<String> names = new ArrayList<String>();

        for (Person person : urlauber) {
            names.add("\n" + person.getFirstName() + " " + person.getLastName());
        }

        final String urlaub = names.toString();

        MimeMessagePreparator prep = new MimeMessagePreparator() {

            @Override
            public void prepare(MimeMessage mimeMessage) throws Exception {

                mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(sternchen));
                mimeMessage.setFrom(new InternetAddress(absender));
                mimeMessage.setText("Hallo Sternchen, "
                    + "\n\nfolgende Mitarbeiter haben diese Woche frei: "
                    + "\n\n" + urlaub + "\n\n"
                    + "\n\nUrlaubsverwaltung");
            }
        };

        try {
            this.mailSender.send(prep);
        } catch (MailException ex) {
            System.err.println(ex.getMessage());
        }
    }


    @Override
    public void sendCanceledNotification(List<Person> persons, Antrag request) {

        String urlauber = "";

        for (Person person : persons) {
            urlauber = urlauber + "\n" + person.getFirstName() + " " + person.getLastName();
        }

        final String allUrlauber = urlauber;

        MimeMessagePreparator prep = new MimeMessagePreparator() {

            @Override
            public void prepare(MimeMessage mimeMessage) throws Exception {

                mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(sternchen));
                mimeMessage.setFrom(new InternetAddress(absender));
                mimeMessage.setText("Hallo Sternchen, "
                    + "\n\nfolgende Mitarbeiter haben diese Woche frei: "
                    + "\n\n" + allUrlauber + "\n\n"
                    + "\n\nUrlaubsverwaltung");
            }
        };

        try {
            this.mailSender.send(prep);
        } catch (MailException ex) {
            System.err.println(ex.getMessage());
        }
    }
}
