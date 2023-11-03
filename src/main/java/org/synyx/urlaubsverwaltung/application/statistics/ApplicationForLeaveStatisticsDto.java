package org.synyx.urlaubsverwaltung.application.statistics;

import java.math.BigDecimal;
import java.util.Map;

import static java.math.BigDecimal.ZERO;

public final class ApplicationForLeaveStatisticsDto {

    private final Long id;
    private final String firstName;
    private final String lastName;
    private final String niceName;
    private final String gravatarURL;

    private final String personnelNumber;

    private final BigDecimal totalAllowedVacationDays;
    private final Map<ApplicationForLeaveStatisticsVacationTypeDto, BigDecimal> allowedVacationDays;

    private final BigDecimal totalWaitingVacationDays;
    private final Map<ApplicationForLeaveStatisticsVacationTypeDto, BigDecimal> waitingVacationDays;

    private final BigDecimal leftVacationDaysForPeriod;
    private final BigDecimal remainingLeftVacationDaysForPeriod;
    private final BigDecimal leftVacationDays;
    private final BigDecimal remainingLeftVacationDays;

    private final String leftOvertime;

    private final String leftOvertimeForPeriod;

    ApplicationForLeaveStatisticsDto(Long id, String firstName, String lastName, String niceName, String gravatarURL, String personnelNumber,
                                     BigDecimal totalAllowedVacationDays, Map<ApplicationForLeaveStatisticsVacationTypeDto, BigDecimal> allowedVacationDays,
                                     BigDecimal totalWaitingVacationDays, Map<ApplicationForLeaveStatisticsVacationTypeDto, BigDecimal> waitingVacationDays,
                                     BigDecimal leftVacationDaysForPeriod, BigDecimal remainingLeftVacationDaysForPeriod, BigDecimal leftVacationDays,
                                     BigDecimal remainingLeftVacationDays, String leftOvertime, String leftOvertimeForPeriod) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.niceName = niceName;
        this.gravatarURL = gravatarURL;
        this.personnelNumber = personnelNumber;
        this.totalAllowedVacationDays = totalAllowedVacationDays;
        this.allowedVacationDays = allowedVacationDays;
        this.totalWaitingVacationDays = totalWaitingVacationDays;
        this.waitingVacationDays = waitingVacationDays;
        this.leftVacationDaysForPeriod = leftVacationDaysForPeriod;
        this.remainingLeftVacationDaysForPeriod = remainingLeftVacationDaysForPeriod;
        this.leftVacationDays = leftVacationDays;
        this.remainingLeftVacationDays = remainingLeftVacationDays;
        this.leftOvertime = leftOvertime;
        this.leftOvertimeForPeriod = leftOvertimeForPeriod;
    }

    public Long getId() {
        return id;
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

    public Map<ApplicationForLeaveStatisticsVacationTypeDto, BigDecimal> getAllowedVacationDays() {
        return allowedVacationDays;
    }

    public BigDecimal getTotalWaitingVacationDays() {
        return totalWaitingVacationDays;
    }

    public Map<ApplicationForLeaveStatisticsVacationTypeDto, BigDecimal> getWaitingVacationDays() {
        return waitingVacationDays;
    }

    public BigDecimal getLeftVacationDaysForPeriod() {
        return leftVacationDaysForPeriod;
    }

    public BigDecimal getRemainingLeftVacationDaysForPeriod() {
        return remainingLeftVacationDaysForPeriod;
    }

    public BigDecimal getLeftVacationDays() {
        return leftVacationDays;
    }

    public BigDecimal getRemainingLeftVacationDays() {
        return remainingLeftVacationDays;
    }

    public String getLeftOvertime() {
        return leftOvertime;
    }

    public String getLeftOvertimeForPeriod() {
        return leftOvertimeForPeriod;
    }

    public boolean hasVacationType(ApplicationForLeaveStatisticsVacationTypeDto type) {
        return waitingVacationDays.containsKey(type) || allowedVacationDays.containsKey(type);
    }

    public BigDecimal getWaitingVacationDays(ApplicationForLeaveStatisticsVacationTypeDto type) {
        return waitingVacationDays.getOrDefault(type, ZERO);
    }

    public BigDecimal getAllowedVacationDays(ApplicationForLeaveStatisticsVacationTypeDto type) {
        return allowedVacationDays.getOrDefault(type, ZERO);
    }
}
