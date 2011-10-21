package org.synyx.urlaubsverwaltung.domain;

import javax.persistence.Entity;

import org.springframework.data.jpa.domain.AbstractPersistable;

/**
 * @author aljona
 */

@Entity
public class Person extends AbstractPersistable<Integer> {

	private static final long serialVersionUID = 1L;

	private String lastName;

	private String firstName;

	private String email;

	// vacation days
	private Integer vacationDays;

	// amount of remaining days
	private Integer remainingVacationDays;

	// residual vacation days of last year
	private Integer restUrlaub;

	// used vacation days of this year
	private Integer usedVacationDays;

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
