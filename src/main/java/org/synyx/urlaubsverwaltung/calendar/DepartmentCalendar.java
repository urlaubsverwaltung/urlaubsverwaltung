package org.synyx.urlaubsverwaltung.calendar;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.annotations.OnDelete;
import org.hibernate.validator.constraints.Length;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.tenancy.tenant.AbstractTenantAwareEntity;

import java.time.Period;

import static jakarta.persistence.GenerationType.SEQUENCE;
import static org.hibernate.annotations.OnDeleteAction.CASCADE;

@Entity
public class DepartmentCalendar extends AbstractTenantAwareEntity {

    private static final int SECRET_LENGTH = 32;

    @Id
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    @GeneratedValue(strategy = SEQUENCE, generator = "department_calendar_generator")
    @SequenceGenerator(name = "department_calendar_generator", sequenceName = "department_calendar_id_seq")
    private Long id;

    @NotNull
    @Column(name = "department_id")
    @OnDelete(action = CASCADE)
    private Long departmentId;

    @NotNull
    @OneToOne
    private Person person;

    @NotNull
    @Length(min = SECRET_LENGTH, max = SECRET_LENGTH)
    private String secret;

    @NotNull
    @Convert(converter = PeriodConverter.class)
    private Period calendarPeriod;

    public DepartmentCalendar() {
        // for hibernate - do not use this
    }

    DepartmentCalendar(Long departmentId, Person person) {
        generateSecret();
        this.departmentId = departmentId;
        this.person = person;
    }

    public DepartmentCalendar(Long departmentId, Person person, Period calendarPeriod, String secret) {
        this.departmentId = departmentId;
        this.person = person;
        this.calendarPeriod = calendarPeriod;
        this.secret = secret;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public void generateSecret() {
        secret = RandomStringUtils.secure().nextAlphanumeric(SECRET_LENGTH);
    }

    public String getSecret() {
        return secret;
    }

    public Period getCalendarPeriod() {
        return calendarPeriod;
    }

    public void setCalendarPeriod(Period calendarPeriod) {
        this.calendarPeriod = calendarPeriod;
    }
}
