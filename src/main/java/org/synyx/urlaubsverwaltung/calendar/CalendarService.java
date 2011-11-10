/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.calendar;

import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

import org.joda.time.LocalDate;

import org.synyx.urlaubsverwaltung.domain.Person;

import java.io.IOException;


/**
 * @author  aljona
 */
public interface CalendarService {

    int getWorkDays(LocalDate start, LocalDate end) throws AuthenticationException, IOException, ServiceException;


    void addVacation(LocalDate start, LocalDate end, Person person, String comment) throws AuthenticationException,
        IOException, ServiceException;
}
