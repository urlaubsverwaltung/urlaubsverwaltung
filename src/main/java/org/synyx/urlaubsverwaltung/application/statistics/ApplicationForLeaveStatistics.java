package org.synyx.urlaubsverwaltung.application.statistics;

import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.application.service.VacationTypeService;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static java.math.BigDecimal.ZERO;

/**
 * Encapsulates information about a person and the corresponding vacation days information (how many applications for
 * leave are waiting, how many are allowed, how many vacation days has the person left for using).
 */
public class ApplicationForLeaveStatistics {

    private final Person person;

    private final Map<VacationType, BigDecimal> waitingVacationDays = new HashMap<>();
    private final Map<VacationType, BigDecimal> allowedVacationDays = new HashMap<>();

    private BigDecimal leftVacationDays = ZERO;
    private Duration leftOvertime = Duration.ZERO;

    public ApplicationForLeaveStatistics(Person person, VacationTypeService vacationTypeService) {
        this.person = person;

        for (VacationType vacationType : vacationTypeService.getVacationTypes()) {
            waitingVacationDays.put(vacationType, ZERO);
            allowedVacationDays.put(vacationType, ZERO);
        }
    }

    public Person getPerson() {
        return person;
    }

    public BigDecimal getTotalWaitingVacationDays() {

        BigDecimal total = ZERO;
        for (BigDecimal days : getWaitingVacationDays().values()) {
            total = total.add(days);
        }

        return total;
    }

    public BigDecimal getTotalAllowedVacationDays() {

        BigDecimal total = ZERO;
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
        final BigDecimal currentWaitingVacationDays = getWaitingVacationDays().get(vacationType);
        getWaitingVacationDays().put(vacationType, currentWaitingVacationDays.add(waitingVacationDays));
    }

    public void addAllowedVacationDays(VacationType vacationType, BigDecimal allowedVacationDays) {
        final BigDecimal currentAllowedVacationDays = getAllowedVacationDays().get(vacationType);
        getAllowedVacationDays().put(vacationType, currentAllowedVacationDays.add(allowedVacationDays));
    }

    public void setLeftOvertime(Duration leftOvertime) {
        this.leftOvertime = leftOvertime;
    }

    public Duration getLeftOvertime() {
        return leftOvertime;
    }
}
