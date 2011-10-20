package org.synyx.urlaubsverwaltung.domain;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Entity;


/**
 * @author  aljona
 */

@Entity
public class Person extends AbstractPersistable<Integer> {

    private String lastName;

    private String firstName;

    private String email;

    // residual vacation days of last year
    private Integer residualVacationDays;

    // vacation days
    private Integer vacationDays;

    // used vacation days of this year
    private Integer usedVacationDays;

//    eventuell fuer die Mitarbeiter-Liste - nur als Idee bisher
//    private Image userPicture;

    public String getEmail() {

        return email;
    }


    public void setEmail(String email) {

        this.email = email;
    }


    public String getFirstName() {

        return firstName;
    }


    public void setFirstName(String firstName) {

        this.firstName = firstName;
    }


    public String getLastName() {

        return lastName;
    }


    public void setLastName(String lastName) {

        this.lastName = lastName;
    }


    public Integer getResidualVacationDays() {

        return residualVacationDays;
    }


    public void setResidualVacationDays(Integer residualVacationDays) {

        this.residualVacationDays = residualVacationDays;
    }


    public Integer getUsedVacationDays() {

        return usedVacationDays;
    }


    public void setUsedVacationDays(Integer usedVacationDays) {

        this.usedVacationDays = usedVacationDays;
    }


    public Integer getVacationDays() {

        return vacationDays;
    }


    public void setVacationDays(Integer vacationDays) {

        this.vacationDays = vacationDays;
    }
}

//    eventuell fuer die Mitarbeiter-Liste - nur als Idee bisher
//public Image getUserPicture() {
//        return userPicture;
//    }
//
//public void setUserPicture(String userPicture) {
//        this.userPicture = userPicture;
//    }
