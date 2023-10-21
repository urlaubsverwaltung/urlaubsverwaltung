package org.synyx.urlaubsverwaltung.application.statistics;

import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.math.BigDecimal.ZERO;

/**
 * Encapsulates information about a person and the corresponding vacation days information (how many applications for
 * leave are waiting, how many are allowed, how many vacation days has the person left for using).
 */
public class ApplicationForLeaveStatistics {

    private final Person person;
    private PersonBasedata personBasedata;

    private final Map<VacationType<?>, BigDecimal> waitingVacationDays = new HashMap<>();
    private final Map<VacationType<?>, BigDecimal> allowedVacationDays = new HashMap<>();

    private BigDecimal leftVacationDaysForYear = ZERO;
    private BigDecimal leftRemainingVacationDaysForYear = ZERO;
    private Duration leftOvertimeForYear = Duration.ZERO;
    private BigDecimal leftVacationDaysForPeriod = ZERO;
    private BigDecimal leftRemainingVacationDaysForPeriod = ZERO;
    private Duration leftOvertimeForPeriod = Duration.ZERO;

    ApplicationForLeaveStatistics(Person person, List<VacationType<?>> vacationTypes) {
        this.person = person;

        for (VacationType<?> type : vacationTypes) {
            addWaitingVacationDays(type, ZERO);
            addAllowedVacationDays(type, ZERO);
        }
    }

    public Person getPerson() {
        return person;
    }

    void setPersonBasedata(PersonBasedata personBasedata) {
        this.personBasedata = personBasedata;
    }

    public Optional<PersonBasedata> getPersonBasedata() {
        return Optional.ofNullable(personBasedata);
    }

    public Map<VacationType<?>, BigDecimal> getWaitingVacationDays() {
        return waitingVacationDays;
    }

    public Map<VacationType<?>, BigDecimal> getAllowedVacationDays() {
        return allowedVacationDays;
    }

    public BigDecimal getLeftVacationDaysForYear() {
        return leftVacationDaysForYear;
    }

    public void setLeftVacationDaysForYear(BigDecimal leftVacationDaysForYear) {
        this.leftVacationDaysForYear = leftVacationDaysForYear;
    }

    public BigDecimal getLeftRemainingVacationDaysForYear() {
        return leftRemainingVacationDaysForYear;
    }

    public void setLeftRemainingVacationDaysForYear(BigDecimal leftRemainingVacationDaysForYear) {
        this.leftRemainingVacationDaysForYear = leftRemainingVacationDaysForYear;
    }

    public BigDecimal getLeftVacationDaysForPeriod() {
        return leftVacationDaysForPeriod;
    }

    public void setLeftVacationDaysForPeriod(BigDecimal leftVacationDaysForPeriod) {
        this.leftVacationDaysForPeriod = leftVacationDaysForPeriod;
    }

    public BigDecimal getLeftRemainingVacationDaysForPeriod() {
        return leftRemainingVacationDaysForPeriod;
    }

    public void setLeftRemainingVacationDaysForPeriod(BigDecimal leftRemainingVacationDaysForPeriod) {
        this.leftRemainingVacationDaysForPeriod = leftRemainingVacationDaysForPeriod;
    }

    public Duration getLeftOvertimeForYear() {
        return leftOvertimeForYear;
    }

    public void setLeftOvertimeForYear(Duration leftOvertimeForYear) {
        this.leftOvertimeForYear = leftOvertimeForYear;
    }

    public Duration getLeftOvertimeForPeriod() {
        return leftOvertimeForPeriod;
    }

    public void setLeftOvertimeForPeriod(Duration leftOvertimeForPeriod) {
        this.leftOvertimeForPeriod = leftOvertimeForPeriod;
    }

    public BigDecimal getTotalWaitingVacationDays() {
        return getWaitingVacationDays().values().stream()
            .reduce(ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalAllowedVacationDays() {
        return getAllowedVacationDays().values().stream()
            .reduce(ZERO, BigDecimal::add);
    }

    public void addWaitingVacationDays(VacationType<?> vacationType, BigDecimal waitingVacationDays) {
        final BigDecimal currentWaitingVacationDays = getWaitingVacationDays().getOrDefault(vacationType, ZERO);
        getWaitingVacationDays().put(vacationType, currentWaitingVacationDays.add(waitingVacationDays));
    }

    public void addAllowedVacationDays(VacationType<?> vacationType, BigDecimal allowedVacationDays) {
        final BigDecimal currentAllowedVacationDays = getAllowedVacationDays().getOrDefault(vacationType, ZERO);
        getAllowedVacationDays().put(vacationType, currentAllowedVacationDays.add(allowedVacationDays));
    }

    public boolean hasVacationType(VacationType<?> type) {
        return waitingVacationDays.containsKey(type) || allowedVacationDays.containsKey(type);
    }

    public BigDecimal getWaitingVacationDays(VacationType<?> type) {
        return waitingVacationDays.getOrDefault(type, ZERO);
    }

    public BigDecimal getAllowedVacationDays(VacationType<?> type) {
        return allowedVacationDays.getOrDefault(type, ZERO);
    }
}
