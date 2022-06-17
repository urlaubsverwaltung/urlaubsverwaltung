package org.synyx.urlaubsverwaltung.application.statistics;

import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;

import java.math.BigDecimal;
import java.util.Map;

import static java.math.BigDecimal.ZERO;

public final class ApplicationForLeaveStatisticsDto {

    private final String firstName;
    private final String lastName;
    private final String niceName;
    private final String gravatarURL;

    private final String personnelNumber;

    private final BigDecimal totalAllowedVacationDays;
    private final Map<VacationType, BigDecimal> allowedVacationDays;

    private final BigDecimal totalWaitingVacationDays;
    private final Map<VacationType, BigDecimal> waitingVacationDays;

    private final BigDecimal leftVacationDays;

    private final String leftOvertime;

    ApplicationForLeaveStatisticsDto(String firstName, String lastName, String niceName, String gravatarURL, String personnelNumber,
                                     BigDecimal totalAllowedVacationDays, Map<VacationType, BigDecimal> allowedVacationDays,
                                     BigDecimal totalWaitingVacationDays, Map<VacationType, BigDecimal> waitingVacationDays,
                                     BigDecimal leftVacationDays, String leftOvertime) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.niceName = niceName;
        this.gravatarURL = gravatarURL;
        this.personnelNumber = personnelNumber;
        this.totalAllowedVacationDays = totalAllowedVacationDays;
        this.allowedVacationDays = allowedVacationDays;
        this.totalWaitingVacationDays = totalWaitingVacationDays;
        this.waitingVacationDays = waitingVacationDays;
        this.leftVacationDays = leftVacationDays;
        this.leftOvertime = leftOvertime;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getNiceName() {
        return niceName;
    }

    public String getGravatarURL() {
        return gravatarURL;
    }

    public String getPersonnelNumber() {
        return personnelNumber;
    }

    public BigDecimal getTotalAllowedVacationDays() {
        return totalAllowedVacationDays;
    }

    public Map<VacationType, BigDecimal> getAllowedVacationDays() {
        return allowedVacationDays;
    }

    public BigDecimal getTotalWaitingVacationDays() {
        return totalWaitingVacationDays;
    }

    public Map<VacationType, BigDecimal> getWaitingVacationDays() {
        return waitingVacationDays;
    }

    public BigDecimal getLeftVacationDays() {
        return leftVacationDays;
    }

    public String getLeftOvertime() {
        return leftOvertime;
    }

    public boolean hasVacationType(VacationType type) {
        return waitingVacationDays.containsKey(type) || allowedVacationDays.containsKey(type);
    }

    public BigDecimal getWaitingVacationDays(VacationType type) {
        return waitingVacationDays.getOrDefault(type, ZERO);
    }

    public BigDecimal getAllowedVacationDays(VacationType type) {
        return allowedVacationDays.getOrDefault(type, ZERO);
    }
}
