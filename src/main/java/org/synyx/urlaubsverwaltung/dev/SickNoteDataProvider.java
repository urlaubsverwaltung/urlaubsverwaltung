package org.synyx.urlaubsverwaltung.dev;

import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteInteractionService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteTypeService;

import java.time.LocalDate;
import java.util.List;

import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.ACTIVE;

/**
 * Provides sick note demo data.
 */
class SickNoteDataProvider {

    private final SickNoteInteractionService sickNoteInteractionService;
    private final SickNoteService sickNoteService;
    private final DurationChecker durationChecker;
    private final SickNoteTypeService sickNoteTypeService;

    SickNoteDataProvider(
        SickNoteInteractionService sickNoteInteractionService, SickNoteService sickNoteService,
        DurationChecker durationChecker,
        SickNoteTypeService sickNoteTypeService
    ) {
        this.sickNoteInteractionService = sickNoteInteractionService;
        this.sickNoteService = sickNoteService;
        this.durationChecker = durationChecker;
        this.sickNoteTypeService = sickNoteTypeService;
    }

    void createSickNote(Person person, Person office, DayLength dayLength, LocalDate startDate, LocalDate endDate, SickNoteCategory sickNoteCategory, boolean withAUB) {
        if (durationChecker.durationIsGreaterThanZero(startDate, endDate, person)) {

            final SickNoteType type = getSickNoteType(sickNoteCategory);

            SickNote.Builder builder = SickNote.builder()
                .person(person)
                .applier(office)
                .startDate(startDate)
                .endDate(endDate)
                .status(ACTIVE)
                .sickNoteType(type)
                .dayLength(dayLength);

            if (withAUB) {
                builder = builder.aubStartDate(startDate).aubEndDate(endDate);
            }

            sickNoteInteractionService.create(builder.build(), office);
        }
    }

    boolean personHasNoSickNotes(Person person) {
        return sickNoteService.getForStatesAndPerson(List.of(SickNoteStatus.values()), List.of(person)).isEmpty();
    }

    private SickNoteType getSickNoteType(SickNoteCategory sickNoteCategory) {

        SickNoteType type = null;
        final List<SickNoteType> sickNoteTypes = sickNoteTypeService.getSickNoteTypes();
        for (SickNoteType sickNoteType : sickNoteTypes) {
            if (sickNoteType.isOfCategory(sickNoteCategory)) {
                type = sickNoteType;
            }
        }
        return type;
    }
}
