package org.synyx.urlaubsverwaltung.core.application.service;

import org.joda.time.DateMidnight;
import org.joda.time.Interval;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.application.dao.ApplicationDAO;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.application.domain.OverlapCase;

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

    private ApplicationDAO applicationDAO;

    @Autowired
    public OverlapService(ApplicationDAO applicationDAO) {

        this.applicationDAO = applicationDAO;
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
     * @param  application {@link Application} (the new application)
     *
     * @return  {@link OverlapCase} (Enum)
     */
    public OverlapCase checkOverlap(Application application) {

        if (application.getHowLong() != DayLength.FULL) {
            return checkOverlapForHalfDay(application, application.getHowLong());
        } else {
            // check if there are existent ANY applications (full day and half day)
            List<Application> apps = applicationDAO.getRelevantActiveApplicationsByPeriodForEveryDayLength(
                    application.getStartDate().toDate(), application.getEndDate().toDate(), application.getPerson());

            return getCaseOfOverlap(application, apps);
        }
    }


    /**
     * With this method you get a list of existent applications that overlap with the given period (information about
     * person and period in application) and have the given day length.
     *
     * @param  app {@link Application}
     * @param  length {@link DayLength}
     *
     * @return  {@link List} of {@link Application}s overlapping with the period of the given {@link Application}
     */
    private List<Application> getApplicationsByPeriodAndDayLength(Application app, DayLength length) {

        if (length == DayLength.MORNING) {
            return applicationDAO.getRelevantActiveApplicationsByPeriodAndDayLength(app.getStartDate().toDate(),
                    app.getEndDate().toDate(), app.getPerson(), DayLength.MORNING);
        } else if (length == DayLength.NOON) {
            return applicationDAO.getRelevantActiveApplicationsByPeriodAndDayLength(app.getStartDate().toDate(),
                    app.getEndDate().toDate(), app.getPerson(), DayLength.NOON);
        } else {
            return applicationDAO.getRelevantActiveApplicationsByPeriodAndDayLength(app.getStartDate().toDate(),
                    app.getEndDate().toDate(), app.getPerson(), DayLength.FULL);
        }
    }


    /**
     * Method to check if the given {@link Application} with {@link DayLength.FULL} may be applied or not. (are there
     * existent {@link Application}s for this period or not?)
     *
     * @param  application {@link Application}
     *
     * @return  int 1 for check is alright: {@link Application} is valid. 2 or 3 for invalid {@link Application}.
     */
    protected OverlapCase checkOverlapForFullDay(Application application) {

        // check if there are existent ANY applications (full day and half day)
        List<Application> apps = getApplicationsByPeriodAndDayLength(application, DayLength.FULL);

        return getCaseOfOverlap(application, apps);
    }


    /**
     * Method to check if the given {@link Application} with {@link DayLength.MORNING} or {@link DayLength.NOON} may be
     * applied or not. (are there existent {@link Application}s for this period or not?)
     *
     * @param  application {@link Application}
     *
     * @return  int 1 for check is alright: {@link Application} is valid. 2 or 3 for invalid {@link Application}.
     */
    protected OverlapCase checkOverlapForHalfDay(Application application, DayLength dayLength) {

        // check if there are overlaps with full day periods
        if (checkOverlapForFullDay(application) == OverlapCase.NO_OVERLAPPING) {
            // if there are no overlaps with full day periods, you have to check if there are overlaps with half day periods
            List<Application> apps = getApplicationsByPeriodAndDayLength(application, dayLength);

            return getCaseOfOverlap(application, apps);
        } else {
            return checkOverlapForFullDay(application);
        }
    }


    /**
     * This method contains the logic how to check if there are existent overlapping {@link Application} for the given
     * period; use this method only for full day {@link Application}s.
     *
     * @param  application {@link Application}
     *
     * @return  1 if there is no overlap at all - 2 if the given period is element of (an) existent {@link Application}
     *          (s) - 3 if the new {@link Application} is part of an existent {@link Application}'s period, but for a
     *          part of it you could apply new vacation
     */
    private OverlapCase getCaseOfOverlap(Application application, List<Application> apps) {

        // case (1): no overlap at all
        if (apps.isEmpty()) {
            /* (1) The
             * period of the new application has no overlap at all with existent applications; i.e. you can calculate
             * the normal way and save the application if there are enough vacation days on person's holidays account.
             */
            return OverlapCase.NO_OVERLAPPING;
        } else {
            // case (2) or (3): overlap

            List<Interval> listOfOverlaps = getListOfOverlaps(application, apps);

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
                    return OverlapCase.FULLY_OVERLAPPING.PARTLY_OVERLAPPING;
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
     * This method gets a list of {@link Application}s that overlap with the period of the given {@link Application};
     * all overlapping intervals are put in this list for further checking (e.g. if there are gaps) and for getting the
     * case of overlap (1, 2 or 3)
     *
     * @param  application {@link Application}
     * @param  apps {@link List} of {@link Application}
     *
     * @return  {@link List} of overlap intervals
     */
    private List<Interval> getListOfOverlaps(Application application, List<Application> apps) {

        Interval interval = new Interval(application.getStartDate(), application.getEndDate());

        List<Interval> listOfOverlaps = new ArrayList<Interval>();

        for (Application a : apps) {
            Interval inti = new Interval(a.getStartDate(), a.getEndDate());
            Interval overlap = inti.overlap(interval);

            // because intervals are inclusive of the start instant, but exclusive of the end instant
            // you have to check if end of interval a is start of interval b

            if (inti.getEnd().equals(interval.getStart())) {
                overlap = new Interval(interval.getStart(), interval.getStart());
            }

            if (inti.getStart().equals(interval.getEnd())) {
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
     * This method gets a list of overlaps and checks with the given {@link Application} if there are any gaps where a
     * user could apply for leave (these gaps are not yet applied for leave) - may be a feature in later version.
     *
     * @param  application {@link Application}
     * @param  listOfOverlaps {@link List} of {@link Interval}
     *
     * @return  {@link List} of {@link Interval} list of gaps
     */
    private List<Interval> getListOfGaps(Application application, List<Interval> listOfOverlaps) {

        List<Interval> listOfGaps = new ArrayList<Interval>();

        // check start and end points

        DateMidnight firstOverlapStart = listOfOverlaps.get(0).getStart().toDateMidnight();
        DateMidnight lastOverlapEnd = listOfOverlaps.get(listOfOverlaps.size() - 1).getEnd().toDateMidnight();

        if (application.getStartDate().isBefore(firstOverlapStart)) {
            Interval gapStart = new Interval(application.getStartDate(), firstOverlapStart);
            listOfGaps.add(gapStart);
        }

        if (application.getEndDate().isAfter(lastOverlapEnd)) {
            Interval gapEnd = new Interval(lastOverlapEnd, application.getEndDate());
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
     * This method checks if the two given intervals have a gap or if they abut. Some examples: (1) if period 1: 16.-18.
     * and period 2: 19.-20 --> they abut (2) if period 1: 16.-18. and period 2: 20.-22 --> they have a gap
     *
     * @param  i1 {@link Interval}
     * @param  i2 {@link Interval}
     *
     * @return  true if they have a gap between or false if they have no gap
     */
    private boolean intervalsHaveGap(Interval i1, Interval i2) {

        // test if end of interval is equals resp. one day plus of start of other interval
        if (!(i1.getEnd().toDateMidnight().equals(i2.getStart().toDateMidnight())
                    || i1.getEnd().toDateMidnight().plusDays(1).equals(i2.getStart().toDateMidnight()))) {
            return true;
        } else {
            return false;
        }
    }
}
