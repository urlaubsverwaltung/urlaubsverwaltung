package org.synyx.urlaubsverwaltung.domain;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Entity;


/**
 * @author  aljona
 */

@Entity
public class Person extends AbstractPersistable<Integer> {

    private static final long serialVersionUID = 1L;

    private String lastName;

    private String firstName;

    private String email;

    // vacation days - Jahresanspruch Urlaub
    private Integer vacationDays;

    // amount of remaining days - Verbleibender Urlaubsanspruch dieses Jahr
    private Integer remainingVacationDays;

    // residual vacation days of last year - Resturlaub
    private Integer restUrlaub;

    // used vacation days this year of 'normal' vacation days - Jahresurlaubsanspruch dieses Jahr in Anspruch genommen
    private Integer usedVacationDays;

    // used vacation days this year of last year's Resturlaub - Resturlaub dieses Jahr in Anspruch genommen
    private Integer usedRestUrlaub;

    private Role role;

    // eventuell fuer die Mitarbeiter-Liste - nur als Idee bisher
    // private Image userPicture;

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

        return restUrlaub;
    }


    public void setResidualVacationDays(Integer residualVacationDays) {

        this.restUrlaub = residualVacationDays;
    }


    public Integer getRemainingVacationDays() {

        return remainingVacationDays;
    }


    public Role getRole() {

        return role;
    }


    public void setRemainingVacationDays(Integer remainingVacationDays) {

        this.remainingVacationDays = remainingVacationDays;
    }


    public void setRole(Role role) {

        this.role = role;
    }


    public Integer getRestUrlaub() {

        return restUrlaub;
    }


    public void setRestUrlaub(Integer restUrlaub) {

        this.restUrlaub = restUrlaub;
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

// eventuell fuer die Mitarbeiter-Liste - nur als Idee bisher
// public Image getUserPicture() {
// return userPicture;
// }
//
// public void setUserPicture(String userPicture) {
// this.userPicture = userPicture;
// }
