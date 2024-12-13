package org.synyx.urlaubsverwaltung.calendarintegration;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class AbsenceMappingExportServiceTest {

    private final AbsenceMappingRepository absenceMappingRepository = Mockito.mock(AbsenceMappingRepository.class);
    private final AbsenceMappingExportService absenceMappingExportService = new AbsenceMappingExportService(absenceMappingRepository);

    @Test
    void getAbsenceMappings_returnsAllMappingsOrderedById() {
        AbsenceMapping one = new AbsenceMapping();
        one.setId(1L);
        one.setAbsenceId(100L);
        one.setAbsenceType(AbsenceMappingType.VACATION);
        one.setEventId("event-id-of-mapping-1");

        AbsenceMapping two = new AbsenceMapping();
        two.setId(2L);
        two.setAbsenceId(200L);
        two.setAbsenceType(AbsenceMappingType.SICKNOTE);
        two.setEventId("event-id-of-mapping-2");

        when(absenceMappingRepository.findAllByOrderByIdAsc()).thenReturn(List.of(one, two));

        List<AbsenceMapping> result = absenceMappingExportService.getAbsenceMappings();

        assertThat(result).hasSize(2).containsExactly(one, two);
    }

    @Test
    void getAbsenceMappings_returnsEmptyListWhenNoMappingsFound() {
        when(absenceMappingRepository.findAllByOrderByIdAsc()).thenReturn(List.of());

        List<AbsenceMapping> result = absenceMappingExportService.getAbsenceMappings();

        assertThat(result).isEmpty();
    }
}
