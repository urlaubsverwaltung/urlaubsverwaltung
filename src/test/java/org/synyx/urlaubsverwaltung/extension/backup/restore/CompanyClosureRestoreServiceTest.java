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
import org.synyx.urlaubsverwaltung.period.DayLength;

import java.time.LocalDate;
import java.util.List;

import static java.time.Month.JANUARY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;

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
        final CompanyClosureDTO christmasEve = new CompanyClosureDTO(12, 24, "companyclosure.name.christmasEve", List.of(
            new CompanyClosureVersionDTO(DayLengthDTO.NOON, null),
            new CompanyClosureVersionDTO(DayLengthDTO.FULL, LocalDate.of(2027, JANUARY, 1))));
        final CompanyClosureDTO newYearsEve = new CompanyClosureDTO(12, 31, "companyclosure.name.newYearsEve", List.of(
            new CompanyClosureVersionDTO(DayLengthDTO.MORNING, null)));

        sut.restore(List.of(christmasEve, newYearsEve));

        final InOrder inOrder = inOrder(companyClosureImportService);
        inOrder.verify(companyClosureImportService).deleteAll();
        inOrder.verify(companyClosureImportService, times(2)).importCompanyClosure(closureCaptor.capture(), versionsCaptor.capture());

        assertThat(closureCaptor.getAllValues()).extracting(CompanyClosureEntity::getMessageKey).containsExactly("companyclosure.name.christmasEve", "companyclosure.name.newYearsEve");
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
}
