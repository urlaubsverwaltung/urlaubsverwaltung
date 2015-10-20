package org.synyx.urlaubsverwaltung.web.statistics;

import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.person.Person;

import java.math.BigDecimal;

import java.util.HashMap;
import java.util.Map;


/**
 * Encapsulates information about a person and the corresponding vacation days information (how many applications for
 * leave are waiting, how many are allowed, how many vacation days has the person left for using).
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class ApplicationForLeaveStatistics {

    private final Person person;

    private Map<VacationType, BigDecimal> waitingVacationDays = new HashMap<>();
    private Map<VacationType, BigDecimal> allowedVacationDays = new HashMap<>();

    private BigDecimal leftVacationDays = BigDecimal.ZERO;

    public ApplicationForLeaveStatistics(Person person) {

        this.person = person;

        for (VacationType vacationType : VacationType.values()) {
            waitingVacationDays.put(vacationType, BigDecimal.ZERO);
            allowedVacationDays.put(vacationType, BigDecimal.ZERO);
        }
    }

    public Person getPerson() {

        return person;
    }


    public BigDecimal getTotalWaitingVacationDays() {

        BigDecimal total = BigDecimal.ZERO;

        for (BigDecimal days : getWaitingVacationDays().values()) {
            total = total.add(days);
        }

        return total;
    }


    public BigDecimal getTotalAllowedVacationDays() {

        BigDecimal total = BigDecimal.ZERO;

        for (BigDecimal days : getAllowedVacationDays().values()) {
            total = total.add(days);
        }

        return total;
    }


    public BigDecimal getLeftVacationDays() {

        return leftVacationDays;
    }


    public Map<VacationType, BigDecimal> getWaitingVacationDays() {

        return waitingVacationDays;
    }


    public Map<VacationType, BigDecimal> getAllowedVacationDays() {

        return allowedVacationDays;
    }


    public void setLeftVacationDays(BigDecimal leftVacationDays) {

        this.leftVacationDays = leftVacationDays;
    }


    public void addWaitingVacationDays(VacationType vacationType, BigDecimal waitingVacationDays) {

        BigDecimal currentWaitingVacationDays = getWaitingVacationDays().get(vacationType);

        getWaitingVacationDays().put(vacationType, currentWaitingVacationDays.add(waitingVacationDays));
    }


    public void addAllowedVacationDays(VacationType vacationType, BigDecimal allowedVacationDays) {

        BigDecimal currentAllowedVacationDays = getAllowedVacationDays().get(vacationType);

        getAllowedVacationDays().put(vacationType, currentAllowedVacationDays.add(allowedVacationDays));
    }
}
