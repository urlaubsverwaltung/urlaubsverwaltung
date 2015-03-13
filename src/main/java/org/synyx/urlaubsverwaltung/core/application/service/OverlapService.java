package org.synyx.urlaubsverwaltung.core.application.service;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

import org.joda.time.DateMidnight;
import org.joda.time.Interval;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.application.dao.ApplicationDAO;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.application.domain.OverlapCase;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteDAO;

import java.util.ArrayList;
import java.util.List;


/**
 * This service handles the validation of {@link Application} for leave concerning overlapping, i.e. if there is already
 * an existent {@link Application} for leave in the same period, the user may not apply for leave in this period.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service
public class OverlapService {

    private final ApplicationDAO applicationDAO;
    private final SickNoteDAO sickNoteDAO;

    @Autowired
    public OverlapService(ApplicationDAO applicationDAO, SickNoteDAO sickNoteDAO) {

        this.applicationDAO = applicationDAO;
        this.sickNoteDAO = sickNoteDAO;
    }

    /**
     * Check if the given new {@link Application} is overlapping with an existent {@link Application}. There are three
     * possible cases: (1) The period of the new application has no overlap at all with existent applications; i.e. you
     * can calculate the normal way and save the application if there are enough vacation days on person's holidays
     * account. (2) The period of the new application is element of an existent application's period; i.e. the new
     * application is not necessary because there is already an existent application for this period. (3) The period of
     * the new application is part of an existent application's period, but for a part of it you could apply new
     * vacation; i.e. user must be asked if he wants to apply for leave for the not overlapping period of the new
     * application.
     *
     * @param  application  for leave to check the overlap for
     *
     * @return  {@link OverlapCase} - none, partly, fully
     */
    public OverlapCase checkOverlap(Application application) {

        List<Application> applications = getRelevantApplicationsForLeave(application);
        List<SickNote> sickNotes = getRelevantSickNotes(application);

        // case (1): no overlap at all
        if (applications.isEmpty() && sickNotes.isEmpty()) {
            return OverlapCase.NO_OVERLAPPING;
        } else {
            // case (2) or (3): overlap

            List<Interval> listOfOverlaps = getListOfOverlaps(application, applications, sickNotes);

            if (application.getHowLong() == DayLength.FULL) {
                List<Interval> listOfGaps = getListOfGaps(application, listOfOverlaps);

                // gaps between the intervals mean that you can apply vacation for this periods
                // this is case (3)
                if (listOfGaps.size() > 0) {
                    /* (3) The period of the new application is part
                     * of an existent application's period, but for a part of it you could apply new vacation; i.e. user
                     * must be asked if he wants to apply for leave for the not overlapping period of the new
                     * application.
                     */
                    return OverlapCase.PARTLY_OVERLAPPING;
                }
            }
            // no gaps mean that period of application is element of other periods of applications
            // i.e. you have no free periods to apply vacation for
            // this is case (2)

            /* (2) The period of
             * the new application is element of an existent application's period; i.e. the new application is not
             * necessary because there is already an existent application for this period.
             */
            return OverlapCase.FULLY_OVERLAPPING;
        }
    }


    /**
     * Get all active applications for leave that are in the period of the given application for leave.
     *
     * @param  application  contains information about the period to be checked and the person
     *
     * @return  {@link List} of {@link Application}s overlapping with the period of the given {@link Application}
     */
    private List<Application> getRelevantApplicationsForLeave(Application application) {

        // get all applications for leave
        List<Application> applicationsForLeave = applicationDAO.getApplicationsForACertainTimeAndPerson(
                application.getStartDate().toDate(), application.getEndDate().toDate(), application.getPerson());

        // filter them since only waiting and allowed applications for leave are relevant
        return FluentIterable.from(applicationsForLeave).filter(new Predicate<Application>() {

                    @Override
                    public boolean apply(Application input) {

                        return input.hasStatus(ApplicationStatus.WAITING) || input.hasStatus(ApplicationStatus.ALLOWED);
                    }
                }).toList();
    }


    /**
     * Get all active sick notes that are in the period of the given application for leave.
     *
     * @param  application  contains information about the period to be checked and the person
     *
     * @return  {@link List} of {@link SickNote}s overlapping with the period of the given {@link Application}
     */
    private List<SickNote> getRelevantSickNotes(Application application) {

        // get all sick notes
        List<SickNote> sickNotes = sickNoteDAO.findByPersonAndPeriod(application.getPerson(),
                application.getStartDate().toDate(), application.getEndDate().toDate());

        // filter them since only active sick notes are relevant
        return FluentIterable.from(sickNotes).filter(new Predicate<SickNote>() {

                    @Override
                    public boolean apply(SickNote input) {

                        return input.isActive();
                    }
                }).toList();
    }


    /**
     * Get a list of intervals that overlap with the period of the given {@link Application} for leave.
     *
     * @param  referenceApplicationForLeave  contains the period to get the overlaps for
     * @param  applicationsForLeave  overlapping the reference application for leave
     * @param  sickNotes  overlapping the reference application for leave
     *
     * @return  {@link List} of overlap intervals
     */
    private List<Interval> getListOfOverlaps(Application referenceApplicationForLeave,
        List<Application> applicationsForLeave, List<SickNote> sickNotes) {

        Interval interval = new Interval(referenceApplicationForLeave.getStartDate(),
                referenceApplicationForLeave.getEndDate());

        List<Interval> overlappingIntervals = new ArrayList<>();

        for (Application application : applicationsForLeave) {
            overlappingIntervals.add(new Interval(application.getStartDate(), application.getEndDate()));
        }

        for (SickNote sickNote : sickNotes) {
            overlappingIntervals.add(new Interval(sickNote.getStartDate(), sickNote.getEndDate()));
        }

        List<Interval> listOfOverlaps = new ArrayList<>();

        for (Interval overlappingInterval : overlappingIntervals) {
            Interval overlap = overlappingInterval.overlap(interval);

            // because intervals are inclusive of the start instant, but exclusive of the end instant
            // you have to check if end of interval a is start of interval b

            if (overlappingInterval.getEnd().equals(interval.getStart())) {
                overlap = new Interval(interval.getStart(), interval.getStart());
            }

            if (overlappingInterval.getStart().equals(interval.getEnd())) {
                overlap = new Interval(interval.getEnd(), interval.getEnd());
            }

            // check if they really overlap, else value of overlap would be null
            if (overlap != null) {
                listOfOverlaps.add(overlap);
            }
        }

        return listOfOverlaps;
    }


    /**
     * Get a list of gaps within the given intervals.
     *
     * @param  referenceApplicationForLeave  contains the period to be checked
     * @param  listOfOverlaps  list of overlaps
     *
     * @return  {@link List} of gaps
     */
    private List<Interval> getListOfGaps(Application referenceApplicationForLeave, List<Interval> listOfOverlaps) {

        List<Interval> listOfGaps = new ArrayList<>();

        // check start and end points

        if (listOfOverlaps.isEmpty()) {
            return listOfGaps;
        }

        DateMidnight firstOverlapStart = listOfOverlaps.get(0).getStart().toDateMidnight();
        DateMidnight lastOverlapEnd = listOfOverlaps.get(listOfOverlaps.size() - 1).getEnd().toDateMidnight();

        DateMidnight startDate = referenceApplicationForLeave.getStartDate();

        if (startDate.isBefore(firstOverlapStart)) {
            Interval gapStart = new Interval(startDate, firstOverlapStart);
            listOfGaps.add(gapStart);
        }

        DateMidnight endDate = referenceApplicationForLeave.getEndDate();

        if (endDate.isAfter(lastOverlapEnd)) {
            Interval gapEnd = new Interval(lastOverlapEnd, endDate);
            listOfGaps.add(gapEnd);
        }

        // check if intervals abut or gap
        for (int i = 0; (i + 1) < listOfOverlaps.size(); i++) {
            // if they don't abut, you can calculate the gap
            // test if end of interval is equals resp. one day plus of start of other interval
            // e.g. if period 1: 16.-18. and period 2: 19.-20 --> they abut
            // e.g. if period 1: 16.-18. and period 2: 20.-22 --> they have a gap
            if (intervalsHaveGap(listOfOverlaps.get(i), listOfOverlaps.get(i + 1))) {
                Interval gap = listOfOverlaps.get(i).gap(listOfOverlaps.get(i + 1));
                listOfGaps.add(gap);
            }
        }

        return listOfGaps;
    }


    /**
     * Check if the two given intervals have a gap or if they abut.
     *
     * <p>Some examples:</p>
     *
     * <p>(1) period 16.-18. and period 19.-20. --> they abut</p>
     *
     * <p>(2) period 16.-18. and period 20.-22. --> they have a gap</p>
     *
     * @param  firstInterval
     * @param  secondInterval
     *
     * @return  {@code true} if they have a gap between or {@code false} if they have no gap
     */
    private boolean intervalsHaveGap(Interval firstInterval, Interval secondInterval) {

        // test if end of interval is equals resp. one day plus of start of other interval
        DateMidnight endOfFirstInterval = firstInterval.getEnd().toDateMidnight();
        DateMidnight startOfSecondInterval = secondInterval.getStart().toDateMidnight();

        return !(endOfFirstInterval.equals(startOfSecondInterval)
                || endOfFirstInterval.plusDays(1).equals(startOfSecondInterval));
    }
}
