import { getJSON } from "../../js/fetch";
import {
  addDays,
  differenceInCalendarDays,
  endOfYear,
  formatISO,
  getYear,
  parseISO,
  startOfYear,
  subDays,
} from "date-fns";
import format from "../../lib/date-fns/format";
import {
  isNoWorkday,
  isPersonalHolidayApprovedFull,
  isPersonalHolidayApprovedMorning,
  isPersonalHolidayApprovedNoon,
  isPersonalHolidayCancellationRequestedFull,
  isPersonalHolidayCancellationRequestedMorning,
  isPersonalHolidayCancellationRequestedNoon,
  isPersonalHolidayTemporaryFull,
  isPersonalHolidayTemporaryMorning,
  isPersonalHolidayTemporaryNoon,
  isPersonalHolidayWaitingFull,
  isPersonalHolidayWaitingMorning,
  isPersonalHolidayWaitingNoon,
  isSickNoteActiveFull,
  isSickNoteActiveMorning,
  isSickNoteActiveNoon,
  isSickNoteFull,
  isSickNoteMorning,
  isSickNoteNoon,
  isSickNoteWaitingFull,
  isSickNoteWaitingMorning,
  isSickNoteWaitingNoon,
} from "../../js/absence";
import { isPublicHoliday, isPublicHolidayMorning, isPublicHolidayNoon } from "../../js/public-holiday";
import parse from "../../lib/date-fns/parse";

function paramize(parameters) {
  return "?" + new URLSearchParams(parameters).toString();
}

export const HolidayService = (function () {
  const _CACHE = {};
  // the requests of the years not cached yet by cache key and year. a year requested again meanwhile waits for the
  // same request, a second response would add its data to the cache once more.
  const _PENDING = {};

  let webPrefix;
  let apiPrefix;
  let personId;

  /**
   *
   * @param {string} endpoint
   * @param {{}} parameters
   * @returns {Promise}
   */
  function fetch(endpoint, parameters) {
    const url = apiPrefix + endpoint + paramize(parameters);
    return getJSON(url);
  }

  function cacheAbsences(year) {
    const absenceCache = (_CACHE["absences"] = _CACHE["absences"] || {});

    return function (data) {
      absenceCache[year] = absenceCache[year] || [];
      barExtents = [];

      for (let absence of data.absences) {
        absenceCache[year].push(absence);
      }
    };
  }

  function cachePublicHoliday(year) {
    const publicHolidayCache = (_CACHE["publicHoliday"] = _CACHE["publicHoliday"] || {});

    return function (data) {
      publicHolidayCache[year] = publicHolidayCache[year] || [];
      barExtents = [];

      for (let publicHoliday of data.publicHolidays) {
        publicHolidayCache[year].push(publicHoliday);
      }
    };
  }

  // own cache, so a failed request is retried and never hides the public holidays (or the other way round)
  function cacheCompanyClosures(year) {
    const companyClosureCache = (_CACHE["companyClosure"] = _CACHE["companyClosure"] || {});

    return function (data) {
      companyClosureCache[year] = data.companyClosures;
      barExtents = [];
    };
  }

  function cacheBlackoutPeriods(year) {
    const blackoutPeriodCache = (_CACHE["blackoutPeriod"] = _CACHE["blackoutPeriod"] || {});

    return function (data) {
      blackoutPeriodCache[year] = blackoutPeriodCache[year] || [];

      for (let blackoutPeriod of data.blackoutPeriods) {
        blackoutPeriodCache[year].push(blackoutPeriod);
      }
    };
  }

  function getAbsencesForDate(date) {
    const year = getYear(date);
    const formattedDate = format(date, "yyyy-MM-dd");
    const cache = _CACHE["absences"] || {};
    const absencesForYear = cache[year] || [];

    return absencesForYear.filter((absence) => absence.date === formattedDate);
  }

  const HALVES = { FULL: ["MORNING", "NOON"], MORNING: ["MORNING"], NOON: ["NOON"] };

  const isLoaded = (cacheKey, year) => Boolean(_CACHE[cacheKey]?.[year]);

  // the years of the vacation or sick note whose absences or public holidays are not loaded yet
  function missingYearsOf(absence) {
    const missingYears = [];
    for (let year = getYear(parseISO(absence.from)); year <= getYear(parseISO(absence.to)); year++) {
      if (!isLoaded("absences", year) || !isLoaded("publicHoliday", year)) {
        missingYears.push(year);
      }
    }
    return missingYears;
  }

  function getPublicHolidaysForDate(date) {
    const year = getYear(date);
    const formattedDate = format(date, "yyyy-MM-dd");
    // company closures are shown like public holidays
    const publicHolidaysForYear = [
      ...(_CACHE["publicHoliday"]?.[year] || []),
      ...(_CACHE["companyClosure"]?.[year] || []),
    ];

    return publicHolidaysForYear.filter((absence) => absence.date === formattedDate);
  }

  const covers = (absent, half) => absent === "FULL" || absent === half;

  // the vacation or sick note on the half ("MORNING" or "NOON") of a day
  function personalAbsenceOn({ date, half }) {
    return getAbsencesForDate(date).find(
      (absence) =>
        (absence.absenceType === "VACATION" || absence.absenceType === "SICK_NOTE") && covers(absence.absent, half),
    );
  }

  // neither no workday nor public holiday. an absence bar runs across the half days without work, and a sick note
  // has days on them as well, which are no work days.
  function isWorkingHalf({ date, half }) {
    return (
      !getAbsencesForDate(date).some(
        (absence) => absence.absenceType === "NO_WORKDAY" && covers(absence.absent, half),
      ) && !getPublicHolidaysForDate(date).some((publicHoliday) => covers(publicHoliday.absencePeriodName, half))
    );
  }

  const nextHalfDay = ({ date, half }) =>
    half === "MORNING" ? { date, half: "NOON" } : { date: addDays(date, 1), half: "MORNING" };
  const previousHalfDay = ({ date, half }) =>
    half === "NOON" ? { date, half: "MORNING" } : { date: subDays(date, 1), half: "NOON" };

  /**
   * @returns the vacation or sick note next to the half day, skipping half days off;
   *          `undefined` when a working half day without one comes first. ends with the loaded years.
   */
  function adjacentAbsence(halfDay, step) {
    let current = step(halfDay);
    while (!personalAbsenceOn(current) && !isWorkingHalf(current)) {
      current = step(current);
    }
    return personalAbsenceOn(current);
  }

  const isSameAbsence = (one, other) =>
    Boolean(one && other) && one.absenceType === other.absenceType && one.id === other.id;

  const halfDayIndex = ({ date, half }) =>
    differenceInCalendarDays(date, new Date(2000, 0, 1)) * 2 + (half === "NOON" ? 1 : 0);

  // the outermost half day of the absence the bar reaches in the direction of step, across half days off
  function barEnd(halfDay, absence, step) {
    let end = halfDay;
    let current = step(halfDay);
    let absenceOnCurrent = personalAbsenceOn(current);
    while (isSameAbsence(absenceOnCurrent, absence) || (!absenceOnCurrent && !isWorkingHalf(current))) {
      if (absenceOnCurrent) {
        end = current;
      }
      current = step(current);
      absenceOnCurrent = personalAbsenceOn(current);
    }
    return end;
  }

  // the first and last half day index of the bars drawn so far. an absence interrupted by another one has two bars.
  let barExtents = [];

  function barExtent(halfDay, absence) {
    const index = halfDayIndex(halfDay);
    let extent = barExtents.find(
      (candidate) => isSameAbsence(candidate.absence, absence) && candidate.first <= index && index <= candidate.last,
    );
    if (!extent) {
      extent = {
        absence,
        first: halfDayIndex(barEnd(halfDay, absence, previousHalfDay)),
        last: halfDayIndex(barEnd(halfDay, absence, nextHalfDay)),
      };
      barExtents.push(extent);
    }
    return extent;
  }

  // where the half day is in its bar, counted in half days
  function barPosition(halfDay, absence) {
    const { first, last } = barExtent(halfDay, absence);
    return { barOffset: halfDayIndex(halfDay) - first, barLength: last - first + 1 };
  }

  // an empty piece when no bar runs across the half day
  function absenceBarPiece(halfDay) {
    const before = adjacentAbsence(halfDay, previousHalfDay);
    const after = adjacentAbsence(halfDay, nextHalfDay);

    const absence = personalAbsenceOn(halfDay);
    if (absence) {
      return {
        absence,
        bridge: false,
        continuesBefore: isSameAbsence(absence, before),
        continuesAfter: isSameAbsence(absence, after),
        ...barPosition(halfDay, absence),
      };
    }
    if (!isWorkingHalf(halfDay) && isSameAbsence(before, after)) {
      return {
        absence: before,
        bridge: true,
        continuesBefore: true,
        continuesAfter: true,
        ...barPosition(halfDay, before),
      };
    }
    return {};
  }

  function getBlackoutPeriodsForDate(date) {
    const year = getYear(date);
    const formattedDate = format(date, "yyyy-MM-dd");
    const cache = _CACHE["blackoutPeriod"] || {};
    const blackoutPeriodsForYear = cache[year] || [];

    return blackoutPeriodsForYear.filter((blackoutPeriod) => blackoutPeriod.date === formattedDate);
  }

  const HolidayService = {
    isNoWorkday(date) {
      return isNoWorkday(getAbsencesForDate(date));
    },

    isSickDayFull(date) {
      return isSickNoteFull(getAbsencesForDate(date));
    },

    isSickDayFullWaiting(date) {
      return isSickNoteWaitingFull(getAbsencesForDate(date));
    },

    isSickDayFullActive(date) {
      return isSickNoteActiveFull(getAbsencesForDate(date));
    },

    isSickDayMorning(date) {
      return isSickNoteMorning(getAbsencesForDate(date));
    },

    isSickDayMorningWaiting(date) {
      return isSickNoteWaitingMorning(getAbsencesForDate(date));
    },

    isSickDayMorningActive(date) {
      return isSickNoteActiveMorning(getAbsencesForDate(date));
    },

    isSickDayNoon(date) {
      return isSickNoteNoon(getAbsencesForDate(date));
    },

    isSickDayNoonWaiting(date) {
      return isSickNoteWaitingNoon(getAbsencesForDate(date));
    },

    isSickDayNoonActive(date) {
      return isSickNoteActiveNoon(getAbsencesForDate(date));
    },

    isPersonalHolidayFull: function (date) {
      return (
        this.isPersonalHolidayFullWaiting(date) ||
        this.isPersonalHolidayFullTemporaryApproved(date) ||
        this.isPersonalHolidayFullApproved(date) ||
        this.isPersonalHolidayFullCancellationRequest(date)
      );
    },

    isPersonalHolidayFullWaiting(date) {
      return isPersonalHolidayWaitingFull(getAbsencesForDate(date));
    },

    isPersonalHolidayFullTemporaryApproved(date) {
      return isPersonalHolidayTemporaryFull(getAbsencesForDate(date));
    },

    isPersonalHolidayFullApproved(date) {
      return isPersonalHolidayApprovedFull(getAbsencesForDate(date));
    },

    isPersonalHolidayFullCancellationRequest(date) {
      return isPersonalHolidayCancellationRequestedFull(getAbsencesForDate(date));
    },

    isHalfDayAbsence: function (date) {
      return (
        this.isPersonalHolidayMorning(date) ||
        this.isPersonalHolidayNoon(date) ||
        this.isSickDayMorning(date) ||
        this.isSickDayNoon(date)
      );
    },

    isPersonalHolidayMorning: function (date) {
      return (
        this.isPersonalHolidayMorningWaiting(date) ||
        this.isPersonalHolidayMorningTemporaryApproved(date) ||
        this.isPersonalHolidayMorningApproved(date) ||
        this.isPersonalHolidayMorningCancellationRequest(date)
      );
    },

    isPersonalHolidayMorningWaiting(date) {
      return isPersonalHolidayWaitingMorning(getAbsencesForDate(date));
    },

    isPersonalHolidayMorningTemporaryApproved(date) {
      return isPersonalHolidayTemporaryMorning(getAbsencesForDate(date));
    },

    isPersonalHolidayMorningApproved(date) {
      return isPersonalHolidayApprovedMorning(getAbsencesForDate(date));
    },

    isPersonalHolidayMorningCancellationRequest(date) {
      return isPersonalHolidayCancellationRequestedMorning(getAbsencesForDate(date));
    },

    isPersonalHolidayNoon: function (date) {
      return (
        this.isPersonalHolidayNoonWaiting(date) ||
        this.isPersonalHolidayNoonTemporaryApproved(date) ||
        this.isPersonalHolidayNoonApproved(date) ||
        this.isPersonalHolidayNoonCancellationRequest(date)
      );
    },

    isPersonalHolidayNoonWaiting(date) {
      return isPersonalHolidayWaitingNoon(getAbsencesForDate(date));
    },

    isPersonalHolidayNoonTemporaryApproved(date) {
      return isPersonalHolidayTemporaryNoon(getAbsencesForDate(date));
    },

    isPersonalHolidayNoonApproved(date) {
      return isPersonalHolidayApprovedNoon(getAbsencesForDate(date));
    },

    isPersonalHolidayNoonCancellationRequest(date) {
      return isPersonalHolidayCancellationRequestedNoon(getAbsencesForDate(date));
    },

    isPublicHolidayFull(date) {
      return isPublicHoliday(getPublicHolidaysForDate(date));
    },

    isPublicHolidayMorning(date) {
      return isPublicHolidayMorning(getPublicHolidaysForDate(date));
    },

    isPublicHolidayNoon(date) {
      return isPublicHolidayNoon(getPublicHolidaysForDate(date));
    },

    isBlackoutPeriod(date) {
      return getBlackoutPeriodsForDate(date).length > 0;
    },

    getBlackoutPeriodDescription(date) {
      return getBlackoutPeriodsForDate(date)
        .map((blackoutPeriod) => blackoutPeriod.description)
        .join(" · ");
    },

    getDescription: function (date) {
      // there can be more than one public holiday on the same day (e.g. a fixed and a movable one
      // falling on the same date in a special year), so join the descriptions of all of them.
      return getPublicHolidaysForDate(date)
        .map((publicHoliday) => publicHoliday.description)
        .join(", ");
    },

    /**
     * A vacation or sick note of several days is drawn as one bar. It runs across no workdays and public holidays
     * in between, which are drawn as a bridge.
     *
     * @param {Date} date
     * @returns {Array} the pieces of the bars on the day, as
     *          `{absent, absence, bridge, continuesBefore, continuesAfter, barOffset, barLength}`.
     *          `absent` is "FULL" for a piece over the whole day, else "MORNING" or "NOON". `barOffset` is the number of
     *          half days of the bar before the piece, `barLength` the number of half days of the whole bar.
     */
    getAbsenceBarPieces: function (date) {
      const morning = absenceBarPiece({ date, half: "MORNING" });
      const noon = absenceBarPiece({ date, half: "NOON" });

      if (morning.bridge === noon.bridge && isSameAbsence(morning.absence, noon.absence)) {
        return [{ ...morning, absent: "FULL", continuesAfter: noon.continuesAfter }];
      }
      return [
        { ...morning, absent: "MORNING" },
        { ...noon, absent: "NOON" },
      ].filter((piece) => piece.absence);
    },

    /**
     * @returns {Array} the vacation or sick note of the day as `[full, morning, noon]`, `undefined` for free parts
     */
    getPersonalAbsences: function (date) {
      const personal = getAbsencesForDate(date).filter(
        (absence) => absence.absenceType === "VACATION" || absence.absenceType === "SICK_NOTE",
      );
      return ["FULL", "MORNING", "NOON"].map((absent) => personal.find((absence) => absence.absent === absent));
    },

    /**
     * @param {Date} date
     * @param {"MORNING"|"NOON"} half
     * @returns {boolean} whether the half is neither no workday nor public holiday
     */
    isWorkingHalf: function (date, half) {
      return isWorkingHalf({ date, half });
    },

    /**
     * @returns {"FULL"|"MORNING"|"NOON"|undefined} the part of the day without work, `undefined` on a workday
     */
    getNoWorkday: function (date) {
      return getAbsencesForDate(date).find((absence) => absence.absenceType === "NO_WORKDAY")?.absent;
    },

    /**
     * @returns {"FULL"|"MORNING"|"NOON"|undefined} the part of the day that is a public holiday, `undefined` on none
     */
    getPublicHoliday: function (date) {
      return getPublicHolidaysForDate(date)[0]?.absencePeriodName;
    },

    /**
     * @returns {Array} the vacations and sick notes whose bar runs across the day without being on it,
     *          see {@link getAbsenceBarPieces}
     */
    getBridgedAbsences: function (date) {
      const personal = this.getPersonalAbsences(date).filter(Boolean);
      return this.getAbsenceBarPieces(date)
        .filter(({ bridge }) => bridge)
        .map(({ absence }) => absence)
        .filter((absence) => !personal.some((other) => isSameAbsence(absence, other)));
    },

    getTypeId: function (date) {
      let morningOrFull;
      let noon;

      const absences = getAbsencesForDate(date);
      for (let absence of absences) {
        if (absence.absenceType === "VACATION") {
          if (absence.absent === "NOON") {
            noon = absence.typeId;
          } else {
            morningOrFull = absence.typeId;
          }
        }
      }

      return [morningOrFull, noon];
    },

    /**
     *
     * @param {Date} from
     * @param {Date} [to]
     */
    bookHoliday: function (from, to) {
      document.location.href = this.getNewHolidayUrl(from, to);
    },

    /**
     * @param {Date} from
     * @param {Date} [to]
     * @param {"MORNING"|"NOON"} [dayLength] the form preselects a full day without it
     */
    getNewHolidayUrl: function (from, to, dayLength) {
      const parameters = {
        personId: personId,
        from: format(from, "yyyy-MM-dd"),
        to: to ? format(to, "yyyy-MM-dd") : undefined,
      };
      if (dayLength) {
        parameters.dayLength = dayLength;
      }

      return webPrefix + "/application/new" + paramize(parameters);
    },

    /**
     * The whole vacation or sick note. Its work days are known once all of its years are loaded,
     * see {@link fetchAbsencePeriod}.
     *
     * @returns {{from: Date, to: Date, workDays: number|undefined}} `workDays` is `undefined` while a year is missing
     */
    getAbsencePeriod: function (absence) {
      const from = parseISO(absence.from);
      const to = parseISO(absence.to);
      if (missingYearsOf(absence).length > 0) {
        return { from, to, workDays: undefined };
      }
      const workingHalves = Object.values(_CACHE["absences"] || {})
        .flat()
        .filter((day) => isSameAbsence(day, absence))
        .flatMap((day) => HALVES[day.absent].filter((half) => isWorkingHalf({ date: parseISO(day.date), half })));
      return { from, to, workDays: workingHalves.length / 2 };
    },

    /**
     * Loads the years of the vacation or sick note the calendar has not loaded.
     *
     * @returns {Promise<boolean>} whether a year has been loaded
     */
    fetchAbsencePeriod: async function (absence) {
      const missingYears = missingYearsOf(absence);
      await Promise.all(missingYears.flatMap((year) => [this.fetchAbsences(year), this.fetchPublicHolidays(year)]));
      return missingYears.length > 0;
    },

    getAbsenceUrl: function (absence) {
      return webPrefix + (absence.absenceType === "SICK_NOTE" ? "/sicknote/" : "/application/") + absence.id;
    },

    /**
     * Shared function to fetch and cache data for a given endpoint and cache key.
     * @param {string} endpoint
     * @param {number} year
     * @param {Object} parameters
     * @param {string} cacheKey
     * @param {Function} cacheHandler
     * @returns {Promise}
     */
    fetchAndCache: function (endpoint, year, parameters, cacheKey, cacheHandler) {
      _CACHE[cacheKey] = _CACHE[cacheKey] || {};

      if (_CACHE[cacheKey][year]) {
        return Promise.resolve(_CACHE[cacheKey][year]);
      }

      _PENDING[cacheKey] = _PENDING[cacheKey] || {};
      if (_PENDING[cacheKey][year]) {
        return _PENDING[cacheKey][year];
      }

      const firstDayOfYear = formatISO(startOfYear(parse(year.toString(), "yyyy", new Date())), {
        representation: "date",
      });
      const lastDayOfYear = formatISO(endOfYear(parse(year.toString(), "yyyy", new Date())), {
        representation: "date",
      });

      const request = fetch(endpoint, { from: firstDayOfYear, to: lastDayOfYear, ...parameters })
        .then(cacheHandler(year))
        .finally(() => {
          // a failed request is sent again the next time the year is asked for
          delete _PENDING[cacheKey][year];
        });
      _PENDING[cacheKey][year] = request;
      return request;
    },

    /**
     * Fetches the public holidays and the company closures of the year.
     * A failing company closures request is not fatal, it is retried by the next call.
     *
     * @param {number} year
     * @returns {Promise}
     */
    fetchPublicHolidays: function (year) {
      return Promise.all([
        this.fetchAndCache("/persons/" + personId + "/public-holidays", year, {}, "publicHoliday", cachePublicHoliday),
        this.fetchAndCache("/company-closures", year, {}, "companyClosure", cacheCompanyClosures).catch(() => []),
      ]);
    },

    fetchAbsences: function (year) {
      return this.fetchAndCache(
        "/persons/" + personId + "/absences",
        year,
        { "absence-types": "vacation,sick_note,no_workday" },
        "absences",
        cacheAbsences,
      );
    },

    fetchBlackoutPeriods: function (year) {
      return this.fetchAndCache(
        "/persons/" + personId + "/blackout-periods",
        year,
        {},
        "blackoutPeriod",
        cacheBlackoutPeriods,
      );
    },
  };

  return {
    create: function (_webPrefix, _apiPrefix, _personId) {
      webPrefix = _webPrefix;
      apiPrefix = _apiPrefix;
      personId = _personId;
      return HolidayService;
    },
  };
})();
