package org.synyx.urlaubsverwaltung.extension.backup.restore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureEntity;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureImportService;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureVersionEntity;
import org.synyx.urlaubsverwaltung.extension.backup.model.CompanyClosureDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.CompanyClosureVersionDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.DayLengthDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.FederalStateDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.PublicHolidaysSettingsDTO;
import org.synyx.urlaubsverwaltung.period.DayLength;

import java.time.LocalDate;
import java.util.List;

import static java.time.Month.JANUARY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CompanyClosureRestoreServiceTest {

    private CompanyClosureRestoreService sut;

    @Mock
    private CompanyClosureImportService companyClosureImportService;

    @Captor
    private ArgumentCaptor<CompanyClosureEntity> closureCaptor;
    @Captor
    private ArgumentCaptor<List<CompanyClosureVersionEntity>> versionsCaptor;

    @BeforeEach
    void setUp() {
        sut = new CompanyClosureRestoreService(companyClosureImportService);
    }

    @Test
    void restoresCompanyClosures() {
        final var christmasEve = new CompanyClosureDTO(12, 24, "Heiligabend", List.of(
            new CompanyClosureVersionDTO(DayLengthDTO.NOON, null),
            new CompanyClosureVersionDTO(DayLengthDTO.FULL, LocalDate.of(2027, JANUARY, 1))));
        final var newYearsEve = new CompanyClosureDTO(12, 31, "Silvester", List.of(
            new CompanyClosureVersionDTO(DayLengthDTO.MORNING, null)));

        sut.restore(List.of(christmasEve, newYearsEve), null);

        final InOrder inOrder = inOrder(companyClosureImportService);
        inOrder.verify(companyClosureImportService).deleteAll();
        inOrder.verify(companyClosureImportService, times(2)).importCompanyClosure(closureCaptor.capture(), versionsCaptor.capture());

        assertThat(closureCaptor.getAllValues()).extracting(CompanyClosureEntity::getName).containsExactly("Heiligabend", "Silvester");
        assertThat(closureCaptor.getAllValues()).extracting(CompanyClosureEntity::getDayOfMonth).containsExactly(24, 31);
        assertThat(closureCaptor.getAllValues()).extracting(CompanyClosureEntity::getMonth).containsExactly(12, 12);
        assertThat(versionsCaptor.getAllValues().get(0))
            .extracting(CompanyClosureVersionEntity::getDayLength, CompanyClosureVersionEntity::getValidFrom)
            .containsExactly(
                tuple(DayLength.NOON, null),
                tuple(DayLength.FULL, LocalDate.of(2027, JANUARY, 1)));
        assertThat(versionsCaptor.getAllValues().get(1))
            .extracting(CompanyClosureVersionEntity::getDayLength)
            .containsExactly(DayLength.MORNING);
    }

    @Test
    void restoresFromLegacyPublicHolidaySettings() {
        final var settings = new PublicHolidaysSettingsDTO(DayLengthDTO.FULL, DayLengthDTO.MORNING, FederalStateDTO.GERMANY_BADEN_WUERTTEMBERG);

        sut.restore(null, settings);

        verify(companyClosureImportService).deleteAll();
        verify(companyClosureImportService, times(2)).importCompanyClosure(closureCaptor.capture(), versionsCaptor.capture());
        assertThat(closureCaptor.getAllValues()).extracting(CompanyClosureEntity::getName).containsExactly("Heiligabend", "Silvester");
        assertThat(versionsCaptor.getAllValues().get(0))
            .extracting(CompanyClosureVersionEntity::getDayLength, CompanyClosureVersionEntity::getValidFrom)
            .containsExactly(tuple(DayLength.ZERO, null));
        assertThat(versionsCaptor.getAllValues().get(1))
            .extracting(CompanyClosureVersionEntity::getDayLength, CompanyClosureVersionEntity::getValidFrom)
            .containsExactly(tuple(DayLength.NOON, null));
    }

    @Test
    void restoresDefaultsWhenNothingIsKnown() {
        final var settings = new PublicHolidaysSettingsDTO(null, null, FederalStateDTO.GERMANY_BADEN_WUERTTEMBERG);

        sut.restore(null, settings);

        verify(companyClosureImportService, times(2)).importCompanyClosure(closureCaptor.capture(), versionsCaptor.capture());
        assertThat(versionsCaptor.getAllValues()).allSatisfy(versions ->
            assertThat(versions).extracting(CompanyClosureVersionEntity::getDayLength).containsExactly(DayLength.NOON));
    }
}
