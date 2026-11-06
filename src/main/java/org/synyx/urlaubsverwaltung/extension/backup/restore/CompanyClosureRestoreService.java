package org.synyx.urlaubsverwaltung.extension.backup.restore;

import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureImportService;
import org.synyx.urlaubsverwaltung.extension.backup.model.CompanyClosureDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.CompanyClosureVersionDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.DayLengthDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.PublicHolidaysSettingsDTO;

import java.util.List;

import static java.lang.invoke.MethodHandles.lookup;
import static java.time.Month.DECEMBER;
import static org.slf4j.LoggerFactory.getLogger;

@Service
@ConditionalOnBackupRestoreEnabled
class CompanyClosureRestoreService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final CompanyClosureImportService companyClosureImportService;

    CompanyClosureRestoreService(CompanyClosureImportService companyClosureImportService) {
        this.companyClosureImportService = companyClosureImportService;
    }

    /**
     * @param companyClosures    {@code null} for backups created before company closures existed
     * @param publicHolidaysSettings only used to derive the closures of such old backups
     */
    void restore(@Nullable List<CompanyClosureDTO> companyClosures, @Nullable PublicHolidaysSettingsDTO publicHolidaysSettings) {

        final List<CompanyClosureDTO> toRestore = companyClosures != null ? companyClosures : legacyClosures(publicHolidaysSettings);

        // default closures may already exist (created on initial settings), the restore must leave exactly the backup's closures
        companyClosureImportService.deleteAll();

        for (CompanyClosureDTO dto : toRestore) {
            companyClosureImportService.importCompanyClosure(
                dto.toEntity(),
                dto.versions().stream().map(CompanyClosureVersionDTO::toEntity).toList()
            );
        }

        LOG.info("Restored {} companyClosures.", toRestore.size());
    }

    private static List<CompanyClosureDTO> legacyClosures(@Nullable PublicHolidaysSettingsDTO settings) {
        return List.of(
            legacyClosure(24, "Heiligabend", settings == null ? null : settings.workingDurationForChristmasEve()),
            legacyClosure(31, "Silvester", settings == null ? null : settings.workingDurationForNewYearsEve())
        );
    }

    // the old setting stored the working duration, a company closure stores the closed portion
    private static CompanyClosureDTO legacyClosure(int dayOfMonth, String name, @Nullable DayLengthDTO workingDuration) {
        final DayLengthDTO closed = workingDuration == null
            ? DayLengthDTO.NOON
            : DayLengthDTO.valueOf(workingDuration.toDayLength().getInverse().name());
        return new CompanyClosureDTO(DECEMBER.getValue(), dayOfMonth, name, List.of(new CompanyClosureVersionDTO(closed, null)));
    }
}
