import { getJSON } from "../../js/fetch";
import { endOfYear, formatISO, getYear, startOfYear } from "date-fns";
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
import { findWhere } from "underscore";
import parse from "../../lib/date-fns/parse";

function paramize(parameters) {
  return "?" + new URLSearchParams(parameters).toString();
}

export const HolidayService = (function () {
  const _CACHE = {};

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

      for (let absence of data.absences) {
        absenceCache[year].push(absence);
      }
    };
  }

  function cachePublicHoliday(year) {
    const publicHolidayCache = (_CACHE["publicHoliday"] = _CACHE["publicHoliday"] || {});

    return function (data) {
      publicHolidayCache[year] = publicHolidayCache[year] || [];

      for (let publicHoliday of data.publicHolidays) {
        publicHolidayCache[year].push(publicHoliday);
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

  function getPublicHolidaysForDate(date) {
    const year = getYear(date);
    const formattedDate = format(date, "yyyy-MM-dd");
    const cache = _CACHE["publicHoliday"] || {};
    const publicHolidaysForYear = cache[year] || [];

    return publicHolidaysForYear.filter((absence) => absence.date === formattedDate);
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

    isPersonalHolidayFull(date) {
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

    isPersonalHolidayMorning(date) {
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

    isPersonalHolidayNoon(date) {
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

    getDescription: function (date) {
      const year = getYear(date);
      const formattedDate = format(date, "yyyy-MM-dd");

      if (_CACHE["publicHoliday"] && _CACHE["publicHoliday"][year]) {
        const publicHoliday = findWhere(_CACHE["publicHoliday"][year], {
          date: formattedDate,
        });
        if (publicHoliday) {
          return publicHoliday.description;
        }
      }

      return "";
    },

    getAbsenceId: function (date) {
      const absences = getAbsencesForDate(date);
      if (absences[0]) {
        return absences[0].id;
      }
      return "-1";
    },

    getAbsenceType: function (date) {
      const absences = getAbsencesForDate(date);
      if (absences[0]) {
        return absences[0].absenceType;
      }
      return "";
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
      const parameters = {
        personId: personId,
        from: format(from, "yyyy-MM-dd"),
        to: to ? format(to, "yyyy-MM-dd") : undefined,
      };

      document.location.href = webPrefix + "/application/new" + paramize(parameters);
    },

    navigateToApplicationForLeave: function (applicationId) {
      document.location.href = webPrefix + "/application/" + applicationId;
    },

    navigateToSickNote: function (sickNoteId) {
      document.location.href = webPrefix + "/sicknote/" + sickNoteId;
    },

    /**
     *
     * @param {number} year
     * @returns {Promise}
     */
    fetchPublic: function (year) {
      _CACHE["publicHoliday"] = _CACHE["publicHoliday"] || {};

      if (_CACHE["publicHoliday"][year]) {
        return Promise.resolve(_CACHE["publicHoliday"][year]);
      }

      const firstDayOfYear = formatISO(startOfYear(parse(year.toString(), "yyyy", new Date())), {
        representation: "date",
      });
      const lastDayOfYear = formatISO(endOfYear(parse(year.toString(), "yyyy", new Date())), {
        representation: "date",
      });

      return fetch("/persons/" + personId + "/public-holidays", {
        from: firstDayOfYear,
        to: lastDayOfYear,
      }).then(cachePublicHoliday(year));
    },

    fetchAbsences: function (year) {
      _CACHE["absences"] = _CACHE["absences"] || {};

      if (_CACHE["absences"][year]) {
        return Promise.resolve(_CACHE["absences"][year]);
      }

      const firstDayOfYear = formatISO(startOfYear(parse(year.toString(), "yyyy", new Date())), {
        representation: "date",
      });
      const lastDayOfYear = formatISO(endOfYear(parse(year.toString(), "yyyy", new Date())), {
        representation: "date",
      });

      return fetch("/persons/" + personId + "/absences", {
        from: firstDayOfYear,
        to: lastDayOfYear,
        "absence-types": "vacation,sick_note,no_workday",
      }).then(cacheAbsences(year));
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
