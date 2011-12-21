/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.view;

import org.joda.time.DateMidnight;
import org.joda.time.chrono.GregorianChronology;

import org.synyx.urlaubsverwaltung.domain.HolidayEntitlement;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.service.HolidaysAccountService;


/**
 * @author  Aljona Murygina
 */
public class ViewService {

    private HolidaysAccountService accountService;

    public ViewService(HolidaysAccountService accountService) {

        this.accountService = accountService;
    }

    public PersonForm getPersonForm(Person person) {

        PersonForm personForm = new PersonForm();
        personForm.setLoginName(person.getLoginName());
        personForm.setLastName(person.getLastName());
        personForm.setFirstName(person.getFirstName());
        personForm.setEmail(person.getEmail());

        int year = DateMidnight.now(GregorianChronology.getInstance()).getYear();

        HolidayEntitlement entitlement = accountService.getHolidayEntitlement(year, person);

        if (entitlement != null) {
            personForm.setVacationDays(entitlement.getVacationDays());
        }

        personForm.setYear(year);

        return personForm;
    }
}
