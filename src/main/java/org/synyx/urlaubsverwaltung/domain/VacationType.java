package org.synyx.urlaubsverwaltung.domain;

/**
 * @author johannes
 */
public enum VacationType {

	ERHOLUNGSURLAUB("erholungsurlaub"), SONDERURLAUB("sonderurlaub"), UNBEZAHLTERURLAUB(
			"unbezahlterurlaub"), UEBERSTUNDENABBUMMELN("ueberstundenabbummeln");

	private String vacationTypeName;

	private VacationType(String vacationTypeName) {

		this.vacationTypeName = vacationTypeName;
	}

	public String getStateName() {

		return this.vacationTypeName;
	}

}
