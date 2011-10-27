/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.synyx.urlaubsverwaltung.domain.Antrag;
import org.synyx.urlaubsverwaltung.domain.Person;

/**
 *
 * @author johannes
 */
public class MailServiceImpl implements MailService {
    
    //see here: http://static.springsource.org/spring/docs/2.0.5/reference/mail.html
    
    private MailSender mailSender;
    
    @Autowired
    public MailServiceImpl(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendDecayNotification(List<Person> persons) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void sendNewRequestsNotification(List<Person> persons, List<Antrag> requests) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void sendApprovedNotification(Person officeUser, Antrag request) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void sendDeclinedNotification(Antrag request) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void sendConfirmation(Antrag request) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void sendBalance(Object balanceObject) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void sendWeeklyVacationForecast(List<Antrag> requests) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void sendCanceledNotification(List<Person> persons, Antrag request) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
