package org.synyx.urlaubsverwaltung.sicknote.statistics;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

record SickNoteStatisticsDto(
    int year,
    LocalDate getAsOfDate,
    List<BigDecimal> numberOfSickDaysByMonth,
    List<BigDecimal> numberOfChildSickDaysByMonth,
    List<BigDecimal> sickRateByMonth,
    BigDecimal sickRate,
    int totalNumberOfAllSickNotes,
    BigDecimal totalNumberOfSickNotes,
    BigDecimal totalNumberOfChildSickNotes,
    BigDecimal atLeastOneSickNotePercent,
    Long numberOfPersonsWithMinimumOneSickNote,
    Long numberOfPersonsWithoutSickNote,
    BigDecimal totalNumberOfSickDaysAllCategories,
    BigDecimal totalNumberOfSickDays,
    BigDecimal totalNumberOfChildSickDays,
    BigDecimal averageDurationOfAllSickNotes,
    BigDecimal averageDurationOfSickNote,
    BigDecimal averageDurationOfChildSickNote,
    BigDecimal averageDurationOfDiseasePerPerson,
    BigDecimal averageDurationOfDiseasePerPersonAndSick,
    BigDecimal averageDurationOfDiseasePerPersonAndChildSick
) {
}
