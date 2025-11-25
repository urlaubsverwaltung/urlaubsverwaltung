package org.synyx.urlaubsverwaltung.application.vacationtype;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.util.Locale;
import java.util.Map;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Converter
final class VacationTypeLabelJpaConverter implements AttributeConverter<Map<Locale, String>, String> {

    private final TypeReference<Map<Locale, String>> typeReference = new TypeReference<>() {
    };

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private static final JsonMapper JSON_MAPPER = new JsonMapper();

    @Override
    public String convertToDatabaseColumn(Map<Locale, String> attribute) {

        if (attribute == null) {
            return null;
        }

        try {
            return JSON_MAPPER.writeValueAsString(attribute);
        } catch (JacksonException ex) {
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
            return JSON_MAPPER.readValue(dbData, typeReference);
        } catch (JacksonException ex) {
            LOG.error("could not convert to entity attribute", ex);
            return Map.of();
        }
    }
}
