package org.synyx.urlaubsverwaltung.application.statistics;

import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static java.math.BigDecimal.ZERO;

public final class ApplicationForLeaveStatisticsDto {

    private final String firstName;
    private final String lastName;
    private final String niceName;
    private final String gravatarURL;

    private final String personnelNumber;

    private final BigDecimal totalAllowedVacationDays;
    private final Map<VacationType, BigDecimal> allowedVacationDays = new HashMap<>();

    private final BigDecimal totalWaitingVacationDays;
    private final Map<VacationType, BigDecimal> waitingVacationDays = new HashMap<>();

    private final BigDecimal leftVacationDays;

    private final Duration leftOvertime;

    ApplicationForLeaveStatisticsDto(String firstName, String lastName, String niceName, String gravatarURL,
                                            String personnelNumber, BigDecimal totalAllowedVacationDays,
                                            BigDecimal totalWaitingVacationDays, BigDecimal leftVacationDays,
                                            Duration leftOvertime) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.niceName = niceName;
        this.gravatarURL = gravatarURL;
        this.personnelNumber = personnelNumber;
        this.totalAllowedVacationDays = totalAllowedVacationDays;
        this.totalWaitingVacationDays = totalWaitingVacationDays;
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

    public Duration getLeftOvertime() {
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
