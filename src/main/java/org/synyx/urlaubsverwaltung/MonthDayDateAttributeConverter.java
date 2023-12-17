package org.synyx.urlaubsverwaltung;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDate;
import java.time.MonthDay;

@Converter
public class MonthDayDateAttributeConverter implements AttributeConverter<MonthDay, LocalDate> {

    @Override
    public LocalDate convertToDatabaseColumn(MonthDay monthDay) {
        if (monthDay != null) {
            return monthDay.atYear(1);
        }
        return null;
    }

    @Override
    public MonthDay convertToEntityAttribute(LocalDate localDate) {
        if (localDate != null) {
            return MonthDay.of(
                localDate.getMonth(),
                localDate.getDayOfMonth()
            );
        }
        return null;
    }
}
