package org.synyx.urlaubsverwaltung.company;

record SickDaysStatDto(double healthRate, int nrOfSickDays, int nrOfShouldWorkDays, SickDaysStatistic.Distribution distribution) {
}
