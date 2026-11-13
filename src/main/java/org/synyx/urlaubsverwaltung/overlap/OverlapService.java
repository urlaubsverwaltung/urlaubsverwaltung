package org.synyx.urlaubsverwaltung.overlap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;

import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;

/**
 * This service handles the validation of {@link Application} for leave concerning overlapping, i.e. if there is already
 * an existent {@link Application} for leave in the same period, the user may not apply for leave in this period.
 */
@Service
public class OverlapService {

    private final ApplicationService applicationService;
    private final SickNoteService sickNoteService;

    @Autowired
    public OverlapService(ApplicationService applicationService, SickNoteService sickNoteService) {
        this.applicationService = applicationService;
        this.sickNoteService = sickNoteService;
    }

    /**
     * Check if there are any overlapping applications for leave or sick notes for the given application for leave.
     *
     * @param application to be checked if there are any overlaps
     * @return {@code true} if an application for leave or a sick note overlaps the given one
     */
    public boolean isOverlapping(final Application application) {

        final Person person = application.getPerson();
        final LocalDate startDate = application.getStartDate();
        final LocalDate endDate = application.getEndDate();

        List<Application> applications = getRelevantApplicationsForLeave(person, startDate, endDate, application.getDayLength());
        if (application.getId() != null) {
            applications = applications.stream()
                .filter(input -> input.getId() != null && !input.getId().equals(application.getId()))
                .toList();
        }

        final List<SickNote> sickNotes = getRelevantSickNotes(person, startDate, endDate, application.getDayLength());

        return !applications.isEmpty() || !sickNotes.isEmpty();
    }

    /**
     * Check if there are any overlapping applications for leave or sick notes for the given sick note.
     *
     * @param sickNote to be checked if there are any overlaps
     * @return {@code true} if an application for leave or a sick note overlaps the given one
     */
    public boolean isOverlapping(final SickNote sickNote) {

        final Person person = sickNote.getPerson();
        final LocalDate startDate = sickNote.getStartDate();
        final LocalDate endDate = sickNote.getEndDate();

        final List<Application> applications = getRelevantApplicationsForLeave(person, startDate, endDate, sickNote.getDayLength());

        List<SickNote> sickNotes = getRelevantSickNotes(person, startDate, endDate, sickNote.getDayLength());
        if (sickNote.getId() != null) {
            sickNotes = sickNotes.stream()
                .filter(input -> input.getId() != null && !input.getId().equals(sickNote.getId()))
                .toList();
        }

        return !applications.isEmpty() || !sickNotes.isEmpty();
    }

    /**
     * Get all active applications for leave of the given person that are in the given period.
     *
     * @param person    to get overlapping applications for leave for
     * @param startDate defines the start of the period
     * @param endDate   defines the end of the period
     * @param dayLength defines the time of day of the period
     * @return {@link List} of {@link Application}s overlapping with the period
     */
    private List<Application> getRelevantApplicationsForLeave(Person person, LocalDate startDate, LocalDate endDate, DayLength dayLength) {

        // get all applications for leave
        final List<Application> applicationsForLeave = applicationService.getApplicationsForACertainPeriodAndPerson(startDate, endDate, person);

        // remove the non-relevant ones
        return applicationsForLeave.stream()
            .filter(withConflictingStatus().and(withOverlappingDayLength(dayLength)))
            .toList();
    }

    private Predicate<Application> withOverlappingDayLength(DayLength dayLength) {
        return application -> application.getDayLength().isFull() || dayLength.isFull() || application.getDayLength().equals(dayLength);
    }

    private Predicate<Application> withConflictingStatus() {
        return application -> application.hasStatus(WAITING) ||
            application.hasStatus(ALLOWED) ||
            application.hasStatus(ALLOWED_CANCELLATION_REQUESTED) ||
            application.hasStatus(TEMPORARY_ALLOWED);
    }

    /**
     * Get all active sick notes of the given person that are in the given period.
     *
     * @param person    to get overlapping sick notes for
     * @param startDate defines the start of the period
     * @param endDate   defines the end of the period
     * @param dayLength defines the time of day of the period
     * @return {@link List} of {@link SickNote}s overlapping with the period of the given {@link Application}
     */
    private List<SickNote> getRelevantSickNotes(Person person, LocalDate startDate, LocalDate endDate, DayLength dayLength) {
        // only active sick notes are relevant, a half day one only for the same half or a full day
        return sickNoteService.getByPersonAndPeriod(person, startDate, endDate)
            .stream()
            .filter(SickNote::isActive)
            .filter(sickNote -> sickNote.getDayLength().isFull() || dayLength.isFull() || sickNote.getDayLength().equals(dayLength))
            .toList();
    }
}
