package org.synyx.urlaubsverwaltung.company;

import java.time.LocalDate;

record CompanyStatisticsDto(LocalDate from, LocalDate to, OvertimeStatDto overtime) {
}
