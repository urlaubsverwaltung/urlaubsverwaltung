package org.synyx.urlaubsverwaltung.calendar;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.time.Period;

@Converter(autoApply = true)
class PeriodConverter implements AttributeConverter<Period, String> {

    @Override
    public String convertToDatabaseColumn(Period attribute) {
        return attribute.toString();
    }

    @Override
    public Period convertToEntityAttribute(String dbData) {
        return Period.parse(dbData);
    }
}
