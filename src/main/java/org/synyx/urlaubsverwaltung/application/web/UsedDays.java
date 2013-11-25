package org.synyx.urlaubsverwaltung.application.web;

import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;

import java.math.BigDecimal;

import java.util.HashMap;
import java.util.Map;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class UsedDays {

    private VacationType type;

    private Map<ApplicationStatus, BigDecimal> map;

    public UsedDays(VacationType type) {

        this.type = type;
        this.map = new HashMap<ApplicationStatus, BigDecimal>();
    }

    public Map<ApplicationStatus, BigDecimal> getMap() {

        return map;
    }


    public void addDays(ApplicationStatus status, BigDecimal days) {

        if (map.containsKey(status)) {
            BigDecimal addedDays = map.get(status).add(days);
            map.put(status, addedDays);
        } else {
            map.put(status, days);
        }
    }
}
