package org.synyx.urlaubsverwaltung.web.person;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.synyx.urlaubsverwaltung.core.account.domain.Account;
import org.synyx.urlaubsverwaltung.core.account.service.AccountInteractionService;
import org.synyx.urlaubsverwaltung.core.account.service.AccountService;
import org.synyx.urlaubsverwaltung.core.calendar.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.core.department.Department;
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.person.Role;

import java.math.BigDecimal;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service
@Transactional
public class PersonFormProcessorImpl implements PersonFormProcessor {

    private final PersonService personService;
    private final WorkingTimeService workingTimeService;
    private final AccountService accountService;
    private final AccountInteractionService accountInteractionService;
    private final DepartmentService departmentService;

    @Autowired
    public PersonFormProcessorImpl(PersonService personService, WorkingTimeService workingTimeService,
        AccountService accountService, AccountInteractionService accountInteractionService,
        DepartmentService departmentService) {

        this.personService = personService;
        this.workingTimeService = workingTimeService;
        this.accountService = accountService;
        this.accountInteractionService = accountInteractionService;
        this.departmentService = departmentService;
    }

    @Override
    public Person create(PersonForm personForm) {

        Person person = personService.create(personForm.getLoginName(), personForm.getLastName(),
                personForm.getFirstName(), personForm.getEmail(), personForm.getNotifications(),
                personForm.getPermissions());

        touchWorkingTime(person, personForm);

        touchAccount(person, personForm);

        return person;
    }


    private void touchWorkingTime(Person person, PersonForm personForm) {

        workingTimeService.touch(personForm.getWorkingDays(), personForm.getValidFrom(), person);
    }


    private void touchAccount(Person person, PersonForm personForm) {

        DateMidnight validFrom = personForm.getHolidaysAccountValidFrom();
        DateMidnight validTo = personForm.getHolidaysAccountValidTo();

        BigDecimal annualVacationDays = personForm.getAnnualVacationDays();
        BigDecimal remainingVacationDays = personForm.getRemainingVacationDays();
        BigDecimal remainingVacationDaysNotExpiring = personForm.getRemainingVacationDaysNotExpiring();

        // check if there is an existing account
        Optional<Account> account = accountService.getHolidaysAccount(validFrom.getYear(), person);

        if (account.isPresent()) {
            accountInteractionService.editHolidaysAccount(account.get(), validFrom, validTo, annualVacationDays,
                remainingVacationDays, remainingVacationDaysNotExpiring);
        } else {
            accountInteractionService.createHolidaysAccount(person, validFrom, validTo, annualVacationDays,
                remainingVacationDays, remainingVacationDaysNotExpiring);
        }
    }


    @Override
    public Person update(PersonForm personForm) {

        Person person = personService.update(personForm.getId(), personForm.getLoginName(), personForm.getLastName(),
                personForm.getFirstName(), personForm.getEmail(), personForm.getNotifications(),
                personForm.getPermissions());

        touchWorkingTime(person, personForm);

        touchAccount(person, personForm);

        touchDepartmentHeads(person);

        return person;
    }


    /**
     * If the updated person loses department head role, all the department head mappings must be cleaned up.
     *
     * @param  person  to check lost department head role for
     */
    private void touchDepartmentHeads(Person person) {

        if (!person.hasRole(Role.DEPARTMENT_HEAD)) {
            List<Department> departments = departmentService.getManagedDepartmentsOfDepartmentHead(person);

            for (Department department : departments) {
                List<Person> departmentHeads = department.getDepartmentHeads();

                List<Person> updatedDepartmentHeads = departmentHeads.stream().filter(departmentHead ->
                            !departmentHead.equals(person)).collect(Collectors.toList());

                if (departmentHeads.size() != updatedDepartmentHeads.size()) {
                    department.setDepartmentHeads(updatedDepartmentHeads);

                    departmentService.update(department);
                }
            }
        }
    }
}
