package org.synyx.urlaubsverwaltung.core.holiday;

import org.synyx.urlaubsverwaltung.restapi.person.PersonResponse;

import java.util.List;

public class VacationOverview {

	private PersonResponse person;
	private Integer personID;
	private List<DayOfMonth> days;

	public PersonResponse getPerson() {
		return person;
	}

	public void setPerson(PersonResponse person) {
		this.person = person;
	}

	public List<DayOfMonth> getDays() {
		return days;
	}

	public void setDays(List<DayOfMonth> days) {
		this.days = days;
	}

	public Integer getPersonID() {
		return personID;
	}

	public void setPersonID(Integer personID) {
		this.personID = personID;
	}
}
