package org.synyx.urlaubsverwaltung.account;

import java.util.Objects;

public record HolidayAccountVacationDays(Account account, VacationDaysLeft vacationDaysYear,
                                         VacationDaysLeft vacationDaysDateRange) {

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
}
