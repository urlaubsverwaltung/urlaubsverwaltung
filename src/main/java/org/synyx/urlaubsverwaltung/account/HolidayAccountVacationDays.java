package org.synyx.urlaubsverwaltung.account;

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
}
