package org.synyx.urlaubsverwaltung.extension.backup.backup;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureEntity;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureImportService;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureVersionEntity;
import org.synyx.urlaubsverwaltung.extension.backup.model.CompanyClosureDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.CompanyClosureVersionDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.DayLengthDTO;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static java.time.Month.JANUARY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyClosureDataCollectionServiceTest {

    private static final Instant LAST_MODIFIED_AT = Instant.parse("2026-11-06T10:00:00Z");

    @InjectMocks
    private CompanyClosureDataCollectionService sut;

    @Mock
    private CompanyClosureImportService companyClosureImportService;

    @Test
    void collectsAllVersions() {
        final CompanyClosureEntity christmasEve = closure(1L, 24, "companyclosure.name.christmasEve");
        final CompanyClosureEntity newYearsEve = closure(2L, 31, "companyclosure.name.newYearsEve");
        when(companyClosureImportService.findAllClosures()).thenReturn(List.of(newYearsEve, christmasEve));
        when(companyClosureImportService.findAllVersions()).thenReturn(List.of(
            changedVersion(1L, DayLength.FULL, LocalDate.of(2027, JANUARY, 1)),
            version(2L, DayLength.MORNING, null),
            version(1L, DayLength.NOON, null)));

        final List<CompanyClosureDTO> actual = sut.collectCompanyClosures();

        assertThat(actual).containsExactly(
            new CompanyClosureDTO(12, 24, "companyclosure.name.christmasEve", null, null, List.of(
                new CompanyClosureVersionDTO(DayLengthDTO.NOON, null, null, null),
                new CompanyClosureVersionDTO(DayLengthDTO.FULL, LocalDate.of(2027, JANUARY, 1), LAST_MODIFIED_AT, "office"))),
            new CompanyClosureDTO(12, 31, "companyclosure.name.newYearsEve", null, null, List.of(
                new CompanyClosureVersionDTO(DayLengthDTO.MORNING, null, null, null))));
    }

    private static CompanyClosureEntity closure(Long id, int dayOfMonth, String messageKey) {
        final CompanyClosureEntity entity = new CompanyClosureEntity();
        entity.setId(id);
        entity.setMonth(12);
        entity.setDayOfMonth(dayOfMonth);
        entity.setMessageKey(messageKey);
        return entity;
    }

    private static CompanyClosureVersionEntity changedVersion(Long closureId, DayLength dayLength, LocalDate validFrom) {
        final CompanyClosureVersionEntity entity = version(closureId, dayLength, validFrom);
        entity.setLastModifiedAt(LAST_MODIFIED_AT);
        entity.setLastModifiedBy(new Person("office", "Muster", "Marlene", "muster@example.org"));
        return entity;
    }

    private static CompanyClosureVersionEntity version(Long closureId, DayLength dayLength, LocalDate validFrom) {
        final CompanyClosureVersionEntity entity = new CompanyClosureVersionEntity();
        entity.setCompanyClosureId(closureId);
        entity.setDayLength(dayLength);
        entity.setValidFrom(validFrom);
        return entity;
    }
}
