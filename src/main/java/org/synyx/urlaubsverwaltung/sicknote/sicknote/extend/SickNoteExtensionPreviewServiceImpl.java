package org.synyx.urlaubsverwaltung.sicknote.sicknote.extend;

import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.synyx.urlaubsverwaltung.sicknote.sicknote.extend.SickNoteExtensionStatus.SUBMITTED;

@Service
class SickNoteExtensionPreviewServiceImpl implements SickNoteExtensionPreviewService {

    private final SickNoteExtensionRepository repository;
    private final SickNoteService sickNoteService;
    private final WorkingTimeCalendarService workingTimeCalendarService;

    SickNoteExtensionPreviewServiceImpl(SickNoteExtensionRepository repository, SickNoteService sickNoteService,
                                        WorkingTimeCalendarService workingTimeCalendarService) {
        this.repository = repository;
        this.sickNoteService = sickNoteService;
        this.workingTimeCalendarService = workingTimeCalendarService;
    }

    @Override
    public Optional<SickNoteExtensionPreview> findExtensionPreviewOfSickNote(Long sickNoteId) {

        final SickNote sickNote = getSickNote(sickNoteId);
        final List<SickNoteExtensionEntity> extensions = repository.findAllBySickNoteIdOrderByCreatedAtDesc(sickNoteId);

        return extensions.stream()
            .findFirst()
            .filter(extension -> SUBMITTED.equals(extension.getStatus()))
            .map(extensionEntity -> new SickNoteExtensionPreview(
                extensionEntity.getId(),
                sickNote.getStartDate(),
                extensionEntity.getNewEndDate(),
                getWorkdays(sickNote.getPerson(), sickNote.getStartDate(), extensionEntity.getNewEndDate())
            ));
    }

    private BigDecimal getWorkdays(Person person, LocalDate start, LocalDate end) {
        final DateRange dateRange = new DateRange(start, end);
        final WorkingTimeCalendar workingTimeCalendar = workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), dateRange).get(person);
        return workingTimeCalendar.workingTime(start, end);
    }

    private SickNote getSickNote(Long id) {
        return sickNoteService.getById(id)
            .orElseThrow(() -> new IllegalStateException("could not find referenced sickNote with id=" + id));
    }
}
