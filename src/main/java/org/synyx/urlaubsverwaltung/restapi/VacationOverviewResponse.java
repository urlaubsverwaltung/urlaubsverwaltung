package org.synyx.urlaubsverwaltung.restapi;

import org.synyx.urlaubsverwaltung.core.holiday.VacationOverview;

import java.util.List;

public class VacationOverviewResponse {

 public VacationOverviewResponse(List<VacationOverview> list) {
		super();
		this.list = list;
	}

private List<VacationOverview> list;

public List<VacationOverview> getList() {
	return list;
}

public void setList(List<VacationOverview> list) {
	this.list = list;
}
 
}
