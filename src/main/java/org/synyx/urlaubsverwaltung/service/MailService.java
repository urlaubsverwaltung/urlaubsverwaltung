/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.service;

import org.synyx.urlaubsverwaltung.domain.Antrag;
import org.synyx.urlaubsverwaltung.domain.Person;

import java.util.List;


/**
 * the mailservice provides the sending of notification-mails to the users
 *
 * @author  johannes
 */
public interface MailService {

    /**
     * this method sends to all persons of the list a notification, that there resturlaub-days will be void very soon
     *
     * @param  persons  the list of persons which should receive the mail
     */
    void sendDecayNotification(List<Person> persons);


    /**
     * this method sends to all persons of the first list a notification that there are some new requests (and a few
     * information about the requests maybe)
     *
     * @param  persons  the list of persons which should receive the mail
     * @param  requests  the list of requests which should be processed
     */
    void sendNewRequestsNotification(List<Person> persons, List<Antrag> requests);


    /**
     * this method sends to the applicant of a request and the office the message that its approved
     *
     * @param  request  the request which got approved
     * @param  officeUser  the user representing the office because of the mail-address
     */
    void sendApprovedNotification(Person officeUser, Antrag request);


    /**
     * this method sends to the applicant of a request the message that its declined
     *
     * @param  request  the request which got declined
     */
    void sendDeclinedNotification(Antrag request);


    /**
     * this method sends a mail to the applicant of a request that it is in the system
     *
     * @param  request  the request which got saved
     */
    void sendConfirmation(Antrag request);


    /**
     * This method sends a yearly balance to the certain user
     *
     * @param  balanceObject
     */
    // hierfür müssen wir noch inen data-bean machen, dass bilanz daten hält und vllt auch ausrechnet
    void sendBalance(Object balanceObject);


    /**
     * this method sends a mail to stern@synyx.de mentioning all requests in the list
     *
     * @param  requests  the requests to mention in the mail
     */
    void sendWeeklyVacationForecast(List<Person> urlauber);


    /**
     * this method sends a notification to a bunch of persons if a specified request got canceled by the applicant
     *
     * @param  persons  the list of persons that will receive a notification-mail
     * @param  request  the request which got canceled
     */
    void sendCanceledNotification(List<Person> persons, Antrag request);
}
