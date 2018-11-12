package org.synyx.urlaubsverwaltung.restapi.vacationoverview;

import org.synyx.urlaubsverwaltung.core.holiday.VacationOverview;

import java.util.List;

public class VacationOverviewResponse {

    private List<VacationOverview> list;

    public VacationOverviewResponse(List<VacationOverview> list) {
        this.list = list;
    }

    public List<VacationOverview> getList() {
        return list;
    }

    public void setList(List<VacationOverview> list) {
        this.list = list;
    }
}
