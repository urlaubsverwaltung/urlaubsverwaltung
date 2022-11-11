package org.synyx.urlaubsverwaltung.account;

import java.util.Objects;

public class HolidayAccountVacationDays {

    private final Account account;
    private final VacationDaysLeft vacationDaysYear;
    private final VacationDaysLeft vacationDaysDateRange;

    public HolidayAccountVacationDays(Account account, VacationDaysLeft vacationDaysYear, VacationDaysLeft vacationDaysDateRange) {
        this.account = account;
        this.vacationDaysYear = vacationDaysYear;
        this.vacationDaysDateRange = vacationDaysDateRange;
    }

    public Account getAccount() {
        return account;
    }

    public VacationDaysLeft getVacationDaysYear() {
        return vacationDaysYear;
    }

    public VacationDaysLeft getVacationDaysDateRange() {
        return vacationDaysDateRange;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HolidayAccountVacationDays that = (HolidayAccountVacationDays) o;
        return Objects.equals(account, that.account);
    }

    @Override
    public int hashCode() {
        return Objects.hash(account);
    }

    @Override
    public String toString() {
        return "HolidayAccountVacationDays{" +
            "account=" + account +
            ", vacationDaysYear=" + vacationDaysYear +
            ", vacationDaysDateRange=" + vacationDaysDateRange +
            '}';
    }
}
