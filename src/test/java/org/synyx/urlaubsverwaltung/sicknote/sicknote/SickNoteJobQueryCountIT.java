package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.synyx.urlaubsverwaltung.QueryCountTestSupport;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteTypeService;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.ACTIVE;

/**
 * Guards the end of sick pay notification against N+1 queries: the number of statements must not depend on the
 * number of sick notes. Today is 2026-05-04 (see {@link QueryCountTestSupport}).
 */
class SickNoteJobQueryCountIT extends QueryCountTestSupport {

    @Autowired
    private SickNoteMailService sickNoteMailService;
    @Autowired
    private SickNoteService sickNoteService;
    @Autowired
    private SickNoteTypeService sickNoteTypeService;
    @Autowired
    private SettingsService settingsService;

    @Test
    void sendEndOfSickPayNotificationDoesNotGrowWithSickNotes() {
        final Settings settings = settingsService.getSettings();
        settings.getSickNoteSettings().setMaximumSickPayDays(42);
        settings.getSickNoteSettings().setDaysBeforeEndOfSickPayNotification(7);
        settingsService.save(settings);

        final Measurement small = seedAndMeasure(1);
        truncatePersonData();
        final Measurement large = seedAndMeasure(4);

        assertThat(small.mails()).isPositive();
        assertThat(large.mails()).isGreaterThan(small.mails());
        assertThat(large.statements()).isEqualTo(small.statements());
    }

    private Measurement seedAndMeasure(int membersPerDepartment) {

        final Staff staff = createStaff(membersPerDepartment);

        final SickNoteType sickNoteType = sickNoteTypeService.getSickNoteTypes().stream()
            .filter(type -> type.isOfCategory(SICK_NOTE))
            .findFirst()
            .orElseThrow();
        for (Person member : staff.members()) {
            sickNoteService.save(SickNote.builder()
                .person(member)
                .applier(staff.office())
                .sickNoteType(sickNoteType)
                .startDate(LocalDate.of(2026, 3, 1))
                .endDate(LocalDate.of(2026, 5, 3))
                .dayLength(FULL)
                .status(ACTIVE)
                .build());
        }

        return measure(sickNoteMailService::sendEndOfSickPayNotification);
    }
}
