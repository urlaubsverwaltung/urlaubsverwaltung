package org.synyx.urlaubsverwaltung.overtime.web;

import org.springframework.util.Assert;
import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


/**
 * View class to record overtime for a certain period of time.
 */
public class OvertimeForm {

    private Integer id;

    private Person person;

    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal numberOfHours;

    private String comment;

    OvertimeForm() {

        // OK
    }


    public OvertimeForm(Person person) {

        Assert.notNull(person, "Person must be given.");

        this.person = person;
    }


    public OvertimeForm(Overtime overtime) {

        Assert.notNull(overtime, "Overtime must be given.");

        this.id = overtime.getId();
        this.person = overtime.getPerson();
        this.startDate = overtime.getStartDate();
        this.endDate = overtime.getEndDate();
        this.numberOfHours = overtime.getHours();
    }

    public Integer getId() {

        return id;
    }


    public void setId(Integer id) {

        this.id = id;
    }


    public Person getPerson() {

        return person;
    }


    public void setPerson(Person person) {

        this.person = person;
    }

    public String getStartDateIsoValue() {
        if (startDate == null) {
            return "";
        }

        return startDate.format(DateTimeFormatter.ISO_DATE);
    }

    public LocalDate getStartDate() {

        return startDate;
    }


    public void setStartDate(LocalDate startDate) {

        this.startDate = startDate;
    }

    public String getEndDateIsoValue() {
        if (endDate == null) {
            return "";
        }

        return endDate.format(DateTimeFormatter.ISO_DATE);
    }


    public LocalDate getEndDate() {

        return endDate;
    }


    public void setEndDate(LocalDate endDate) {

        this.endDate = endDate;
    }


    public BigDecimal getNumberOfHours() {

        return numberOfHours;
    }


    public void setNumberOfHours(BigDecimal numberOfHours) {

        this.numberOfHours = numberOfHours;
    }


    public String getComment() {

        return comment;
    }


    public void setComment(String comment) {

        this.comment = comment;
    }


    public Overtime generateOvertime() {

        return new Overtime(getPerson(), getStartDate(), getEndDate(), getNumberOfHours());
    }


    public void updateOvertime(Overtime overtime) {

        overtime.setPerson(getPerson());
        overtime.setHours(getNumberOfHours());
        overtime.setStartDate(getStartDate());
        overtime.setEndDate(getEndDate());
    }
}
