package org.synyx.urlaubsverwaltung.application.vacationtype;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Converter
final class VacationTypeLabelJpaConverter implements AttributeConverter<Map<Locale, String>, String> {

    private final TypeReference<Map<Locale, String>> typeReference = new TypeReference<>() {};

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private static final ObjectMapper om = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<Locale, String> attribute) {

        if (attribute == null) {
            return null;
        }

        try {
            return om.writeValueAsString(attribute);
        } catch (JsonProcessingException ex) {
            LOG.error("could not write value as string", ex);
            return null;
        }
    }

    @Override
    public Map<Locale, String> convertToEntityAttribute(String dbData) {

        if (dbData == null) {
            return null;
        }

        try {
            return om.readValue(dbData, typeReference);
        } catch (IOException ex) {
            LOG.error("could not convert to entity attribute", ex);
            return Map.of();
        }
    }
}
