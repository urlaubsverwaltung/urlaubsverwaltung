package org.synyx.urlaubsverwaltung.domain;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    
    public String getEMailHash() {
        byte[] bytesOfMessage;
        byte[] theDigest;
        try {
            bytesOfMessage = email.trim().toLowerCase(Locale.ENGLISH).getBytes("UTF-8");
            MessageDigest md = MessageDigest.getInstance("MD5");
            theDigest = md.digest(bytesOfMessage);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Person.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Person.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }
        return new String(theDigest);
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


	public Integer getUsedRestUrlaub() {
		return usedRestUrlaub;
	}


	public void setUsedRestUrlaub(Integer usedRestUrlaub) {
		this.usedRestUrlaub = usedRestUrlaub;
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
