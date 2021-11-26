package org.synyx.urlaubsverwaltung.calendar;

import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.validator.constraints.Length;
import org.synyx.urlaubsverwaltung.person.Person;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import java.time.Period;

@Entity
class CompanyCalendar {

    private static final int SECRET_LENGTH = 32;

    @Id
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    @GenericGenerator(
        name = "company_calendar_id_seq",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
            @Parameter(name = "sequence_name", value = "company_calendar_id_seq"),
            @Parameter(name = "initial_value", value = "1"),
            @Parameter(name = "increment_size", value = "1")
        }
    )
    @GeneratedValue(generator = "company_calendar_id_seq")
    private Long id;

    @NotNull
    @OneToOne
    private Person person;

    @Length(min = SECRET_LENGTH, max = SECRET_LENGTH)
    private String secret;

    @Convert(converter = PeriodConverter.class)
    private Period calendarPeriod;

    public CompanyCalendar() {
        // for hibernate - do not use this
    }

    protected CompanyCalendar(Person person) {
        generateSecret();
        this.person = person;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public void generateSecret() {
        secret = RandomStringUtils.randomAlphanumeric(SECRET_LENGTH);
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
