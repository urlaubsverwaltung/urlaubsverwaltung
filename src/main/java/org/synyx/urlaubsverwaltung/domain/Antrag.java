package org.synyx.urlaubsverwaltung.domain;

import javax.persistence.Entity;

import org.joda.time.DateMidnight;
import org.springframework.data.jpa.domain.AbstractPersistable;

/**
 * @author johannes
 */

@Entity
public class Antrag extends AbstractPersistable<Integer> {

	private static final long serialVersionUID = 1L;

	private Person person;

	private Integer beantragteTage;

	private DateMidnight startDate;

	private DateMidnight endDate;

	private VacationType vacationType;

	private String reason;

	private Person vertreter;

	private String anschrift;

	private String phone;

	private DateMidnight antragsDate;

	private State state;

	public Person getPerson() {
		return person;
	}

	public Integer getBeantragteTage() {
		return beantragteTage;
	}

	public DateMidnight getStartDate() {
		return startDate;
	}

	public DateMidnight getEndDate() {
		return endDate;
	}

	public VacationType getVacationType() {
		return vacationType;
	}

	public String getReason() {
		return reason;
	}

	public Person getVertreter() {
		return vertreter;
	}

	public String getAnschrift() {
		return anschrift;
	}

	public String getPhone() {
		return phone;
	}

	public DateMidnight getAntragsDate() {
		return antragsDate;
	}

	public State getState() {
		return state;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public void setBeantragteTage(Integer beantragteTage) {
		this.beantragteTage = beantragteTage;
	}

	public void setStartDate(DateMidnight startDate) {
		this.startDate = startDate;
	}

	public void setEndDate(DateMidnight endDate) {
		this.endDate = endDate;
	}

	public void setVacationType(VacationType vacationType) {
		this.vacationType = vacationType;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public void setVertreter(Person vertreter) {
		this.vertreter = vertreter;
	}

	public void setAnschrift(String anschrift) {
		this.anschrift = anschrift;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public void setAntragsDate(DateMidnight antragsDate) {
		this.antragsDate = antragsDate;
	}

	public void setState(State state) {
		this.state = state;
	}

}
