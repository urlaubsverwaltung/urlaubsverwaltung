/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.service;

import org.joda.time.LocalDate;

import org.synyx.urlaubsverwaltung.domain.Person;


/**
 * @author  aljona
 */
public interface CalendarService {

    int getWorkDays(LocalDate start, LocalDate end);


    void addVacation(LocalDate start, LocalDate end, Person person, String comment);
}
