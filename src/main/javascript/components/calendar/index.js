import $ from "jquery";
import { findWhere } from "underscore";
import {
  addDays,
  addMonths,
  // addWeeks,
  endOfYear,
  formatISO,
  getDay,
  getMonth,
  getYear,
  isBefore,
  isPast,
  isToday,
  isValid as isValidDate,
  isWeekend,
  isWithinInterval,
  parse,
  parseISO,
  startOfMonth,
  startOfYear,
  subMonths,
} from "date-fns";
import format from "../../lib/date-fns/format";
import startOfWeek from "../../lib/date-fns/start-of-week";
import tooltip from "../tooltip";
import { getJSON } from "../../js/fetch";
import {
  isNoWorkday,
  isSickNoteMorning,
  isSickNoteNoon,
  isSickNoteFull,
  isPersonalHolidayWaitingFull,
  isPersonalHolidayTemporaryFull,
  isPersonalHolidayApprovedFull,
  isPersonalHolidayCancellationRequestedFull,
  isPersonalHolidayWaitingMorning,
  isPersonalHolidayTemporaryMorning,
  isPersonalHolidayApprovedMorning,
  isPersonalHolidayCancellationRequestedMorning,
  isPersonalHolidayWaitingNoon,
  isPersonalHolidayApprovedNoon,
  isPersonalHolidayCancellationRequestedNoon,
  isPersonalHolidayTemporaryNoon,
} from "../../js/absence";
import { isPublicHoliday, isPublicHolidayMorning, isPublicHolidayNoon } from "../../js/public-holiday";
import "./calendar.css";

function paramize(p) {
  let result = "?";
  for (let v in p) {
    if (p[v]) {
      result += v + "=" + p[v] + "&";
    }
  }
  return result.replace(/[&?]$/, "");
}

$(function () {
  const $datepicker = $("#datepicker");

  const numberOfMonths = 10;

  const keyCodes = {
    escape: 27,
  };

  const mouseButtons = {
    left: 0,
    middle: 1,
    right: 2,
  };

  const CSS = {
    day: "datepicker-day",
    daySelected: "datepicker-day-selected",
    dayToday: "datepicker-day-today",
    dayWeekend: "datepicker-day-weekend",
    dayPast: "datepicker-day-past",
    dayPublicHolidayFull: "datepicker-day-public-holiday-full",
    dayPublicHolidayMorning: "datepicker-day-public-holiday-morning",
    dayPublicHolidayNoon: "datepicker-day-public-holiday-noon",
    dayPersonalHolidayFull: "datepicker-day-absence-full absence-full--outline",
    dayPersonalHolidayFullApproved: "datepicker-day-absence-full absence-full--solid",
    dayPersonalHolidayMorning: "datepicker-day-absence-morning absence-morning--outline",
    dayPersonalHolidayMorningApproved: "datepicker-day-absence-morning absence-morning--solid",
    dayPersonalHolidayNoon: "datepicker-day-absence-noon absence-noon--outline",
    dayPersonalHolidayNoonApproved: "datepicker-day-absence-noon absence-noon--solid",
    daySickDayFull: "datepicker-day-sick-note-full",
    daySickDayMorning: "datepicker-day-sick-note-morning",
    daySickDayNoon: "datepicker-day-sick-note-noon",
    next: "datepicker-next",
    previous: "datepicker-prev",
    month: "datepicker-month",
    mousedown: "mousedown",
  };

  const DATA = {
    date: "datepickerDate",
    month: "datepickerMonth",
    year: "datepickerYear",
    selected: "datepickerSelected",
    selectFrom: "datepickerSelectFrom",
    selectTo: "datepickerSelectTo",
    selectable: "datepickerSelectable",
  };

  const icons = {
    chevronRight: `<svg viewBox="0 0 20 20" fill="currentColor" class="tw-w-6 tw-h-6" role="img" aria-hidden="true"><path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd"></path></svg>`,
    chevronLeft: `<svg viewBox="0 0 20 20" fill="currentColor" class="tw-w-6 tw-h-6" role="img" aria-hidden="true"><path fill-rule="evenodd" d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z" clip-rule="evenodd"></path></svg>`,
  };

  function getDateFromElement(element) {
    return parseISO($(element).data(DATA.date));
  }

  const Assertion = (function () {
    let holidayService;

    const assert = {
      isToday: function (date) {
        return isToday(date);
      },
      isWeekend: function (date) {
        return isWeekend(date);
      },
      isPast: function (date) {
        /* NOTE: Today is not in the past! */
        return !isToday(date) && isPast(date);
      },
      isNoWorkday: function (date) {
        return holidayService.isNoWorkday(date);
      },
      isHalfDayAbsence: function (date) {
        if (assert.isPersonalHolidayMorning(date) || assert.isPersonalHolidayNoon(date)) {
          return true;
        }
        if (assert.isSickDayMorning(date) || assert.isSickDayNoon(date)) {
          return true;
        }
        return assert.isPublicHolidayMorning(date) || assert.isPublicHolidayNoon(date);
      },
      isPublicHolidayFull: function (date) {
        return holidayService.isPublicHolidayFull(date);
      },
      isPublicHolidayMorning: function (date) {
        return holidayService.isPublicHolidayMorning(date);
      },
      isPublicHolidayNoon: function (date) {
        return holidayService.isPublicHolidayNoon(date);
      },
      isPersonalHolidayFull: function (date) {
        return holidayService.isPersonalHolidayFull(date);
      },
      isPersonalHolidayFullTemporaryApproved: function (date) {
        return holidayService.isPersonalHolidayFullTemporaryApproved(date);
      },
      isPersonalHolidayFullApproved: function (date) {
        return holidayService.isPersonalHolidayFullApproved(date);
      },
      isPersonalHolidayFullCancellationRequest: function (date) {
        return holidayService.isPersonalHolidayFullCancellationRequest(date);
      },
      isPersonalHolidayMorning: function (date) {
        return holidayService.isPersonalHolidayMorning(date);
      },
      isPersonalHolidayMorningTemporaryApproved: function (date) {
        return holidayService.isPersonalHolidayMorningTemporaryApproved(date);
      },
      isPersonalHolidayMorningApproved: function (date) {
        return holidayService.isPersonalHolidayMorningApproved(date);
      },
      isPersonalHolidayMorningCancellationRequest: function (date) {
        return holidayService.isPersonalHolidayMorningCancellationRequest(date);
      },
      isPersonalHolidayNoon: function (date) {
        return holidayService.isPersonalHolidayNoon(date);
      },
      isPersonalHolidayNoonTemporaryApproved: function (date) {
        return holidayService.isPersonalHolidayNoonTemporaryApproved(date);
      },
      isPersonalHolidayNoonApproved: function (date) {
        return holidayService.isPersonalHolidayNoonApproved(date);
      },
      isPersonalHolidayNoonCancellationRequest: function (date) {
        return holidayService.isPersonalHolidayNoonCancellationRequest(date);
      },
      isSickDayFull: function (date) {
        return holidayService.isSickDayFull(date);
      },
      isSickDayMorning: function (date) {
        return holidayService.isSickDayMorning(date);
      },
      isSickDayNoon: function (date) {
        return holidayService.isSickDayNoon(date);
      },
      title: function (date) {
        return holidayService.getDescription(date);
      },
      absenceId: function (date) {
        return holidayService.getAbsenceId(date);
      },
      absenceType: function (date) {
        return holidayService.getAbsenceType(date);
      },
      vacationTypeId: function (date) {
        return holidayService.getVacationTypeId(date);
      },
    };

    return {
      create: function (_holidayService) {
        holidayService = _holidayService;
        return assert;
      },
    };
  })();

  const HolidayService = (function () {
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

      isSickDayMorning(date) {
        return isSickNoteMorning(getAbsencesForDate(date));
      },

      isSickDayNoon(date) {
        return isSickNoteNoon(getAbsencesForDate(date));
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
          return absences[0].href;
        }
        return "-1";
      },

      getAbsenceType: function (date) {
        const absences = getAbsencesForDate(date);
        if (absences[0]) {
          return absences[0].type;
        }
        return "";
      },

      getVacationTypeId: function (date) {
        let morningOrFull;
        let noon;

        const absences = getAbsencesForDate(date);
        for (let absence of absences) {
          if (absence.type === "VACATION") {
            if (absence.absencePeriodName === "NOON") {
              noon = absence.vacationTypeId;
            } else {
              morningOrFull = absence.vacationTypeId;
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

        const firstDayOfYear = formatISO(startOfYear(parse(year, "yyyy", new Date())), { representation: "date" });
        const lastDayOfYear = formatISO(endOfYear(parse(year, "yyyy", new Date())), { representation: "date" });

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

        const firstDayOfYear = formatISO(startOfYear(parse(year, "yyyy", new Date())), { representation: "date" });
        const lastDayOfYear = formatISO(endOfYear(parse(year, "yyyy", new Date())), { representation: "date" });

        return fetch("/persons/" + personId + "/absences", {
          from: firstDayOfYear,
          to: lastDayOfYear,
          noWorkdaysInclusive: true,
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

  const View = (function () {
    let assert;
    let i18n = () => "";

    const TMPL = {
      container: '{{previousButton}}<div class="datepicker-months-container">{{months}}</div>{{nextButton}}',

      button: '<button class="{{css}}">{{text}}</button>',

      month:
        '<div class="datepicker-month {{css}}" data-datepicker-month="{{month}}" data-datepicker-year="{{year}}"><p id="calendar-month-{{month}}" class="datepicker-month-caption">{{caption}}</p><ol class="datepicker-table tw-m-0 tw-p-0 tw-list-none tw-grid" style="grid-template-columns: repeat(7, 1fr);" aria-describedby="calendar calendar-month-{{month}}">{{weekdays}}{{weeks}}</ol></div>',

      // {{0}}......{{6}}
      weekdays: `{{${[0, 1, 2, 3, 4, 5, 6].join("}}{{")}}}`,

      weekday: `<li role="none" aria-hidden="true" class="datepicker-day-header print:tw-hidden">{{text}}</li>`,

      day: '<li class="tw-border-b tw-border-r dark:tw-border-zinc-900" style="{{cellStyle}}"><span class="tw-sr-only print:tw-hidden">{{ariaDay}}</span><div class="datepicker-day {{css}}" style="{{style}}" data-title="{{title}}" data-datepicker-absence-id={{absenceId}} data-datepicker-absence-type="{{absenceType}}" data-datepicker-date="{{date}}" data-datepicker-selectable="{{selectable}}"><span aria-hidden="true">{{day}}</span>{{icon}}</div></li>',

      iconPlaceholder: '<span class="tw-w-3 tw-h-3 tw-inline-block"></span>',

      noWorkdayIcon:
        '<svg viewBox="0 0 20 20" class="tw-w-3 tw-h-3 tw-opacity-50 tw-stroke-2" fill="currentColor" width="16" height="16" role="img" aria-hidden="true" focusable="false"><path fill-rule="evenodd" d="M13.477 14.89A6 6 0 015.11 6.524l8.367 8.368zm1.414-1.414L6.524 5.11a6 6 0 018.367 8.367zM18 10a8 8 0 11-16 0 8 8 0 0116 0z" clip-rule="evenodd"></path></svg>',
    };

    // eslint-disable-next-line unicorn/consistent-function-scoping
    function render(tmpl, data) {
      return tmpl.replace(/{{(\w+)}}/g, function (_, type) {
        if (typeof data === "function") {
          return data.apply(this, arguments);
        }

        const value = data[type];
        return typeof value === "function" ? value() : value;
      });
    }

    function renderCalendar(date) {
      let monthsToShow = numberOfMonths;

      return render(TMPL.container, {
        previousButton: renderButton(
          CSS.previous,
          `${icons.chevronLeft}<span class="sm:tw-sr-only">${i18n("overview.calendar.button.previous.label")}</span>`,
        ),
        nextButton: renderButton(
          CSS.next,
          `${icons.chevronRight}<span class="sm:tw-sr-only">${i18n("overview.calendar.button.next.label")}</span>`,
        ),

        months: function () {
          let html = "";
          let d = subMonths(date, 4);
          while (monthsToShow--) {
            html += renderMonth(d);
            d = addMonths(d, 1);
          }
          return html;
        },
      });
    }

    function renderButton(css, text) {
      return render(TMPL.button, {
        css: css,
        text: text,
      });
    }

    function renderMonth(date, cssClasses) {
      const m = getMonth(date);
      let d = startOfMonth(date);

      return render(TMPL.month, {
        css: cssClasses || "",
        caption: format(d, "MMMM yyyy"),
        month: getMonth(d),
        year: getYear(d),
        weekdays: renderWeekdaysHeader(d),

        weeks: function () {
          let html = "";
          while (getMonth(d) === m) {
            html += renderDay(d);
            d = addDays(d, 1);
          }
          return html;
        },
      });
    }

    function renderWeekdaysHeader(date) {
      const startOfWeekDate = startOfWeek(date);

      const renderWeekday = (day) =>
        render(TMPL.weekday, {
          // abbreviation (e.g. Mo)
          text: format(day, "EEEEEE"),
          // long word (e.g. Monday)
          ariaLabel: format(day, "EEEE"),
        });

      return render(TMPL.weekdays, {
        0: renderWeekday(startOfWeekDate),
        1: renderWeekday(addDays(startOfWeekDate, 1)),
        2: renderWeekday(addDays(startOfWeekDate, 2)),
        3: renderWeekday(addDays(startOfWeekDate, 3)),
        4: renderWeekday(addDays(startOfWeekDate, 4)),
        5: renderWeekday(addDays(startOfWeekDate, 5)),
        6: renderWeekday(addDays(startOfWeekDate, 6)),
      });
    }

    function renderDay(date) {
      function classes() {
        return [
          assert.isToday(date) ? CSS.dayToday : "",
          assert.isWeekend(date) ? CSS.dayWeekend : "",
          assert.isPast(date) ? CSS.dayPast : "",
          assert.isPublicHolidayFull(date) ? CSS.dayPublicHolidayFull : "",
          assert.isPublicHolidayMorning(date) ? CSS.dayPublicHolidayMorning : "",
          assert.isPublicHolidayNoon(date) ? CSS.dayPublicHolidayNoon : "",
          assert.isPersonalHolidayFull(date) ? CSS.dayPersonalHolidayFull : "",
          assert.isPersonalHolidayFullTemporaryApproved(date) ? CSS.dayPersonalHolidayFull : "",
          assert.isPersonalHolidayFullApproved(date) ? CSS.dayPersonalHolidayFullApproved : "",
          assert.isPersonalHolidayFullCancellationRequest(date) ? CSS.dayPersonalHolidayFullApproved : "",
          assert.isPersonalHolidayMorning(date) ? CSS.dayPersonalHolidayMorning : "",
          assert.isPersonalHolidayMorningTemporaryApproved(date) ? CSS.dayPersonalHolidayMorning : "",
          assert.isPersonalHolidayMorningApproved(date) ? CSS.dayPersonalHolidayMorningApproved : "",
          assert.isPersonalHolidayMorningCancellationRequest(date) ? CSS.dayPersonalHolidayMorningApproved : "",
          assert.isPersonalHolidayNoon(date) ? CSS.dayPersonalHolidayNoon : "",
          assert.isPersonalHolidayNoonTemporaryApproved(date) ? CSS.dayPersonalHolidayNoon : "",
          assert.isPersonalHolidayNoonApproved(date) ? CSS.dayPersonalHolidayNoonApproved : "",
          assert.isPersonalHolidayNoonCancellationRequest(date) ? CSS.dayPersonalHolidayNoonApproved : "",
          assert.isSickDayFull(date) ? CSS.daySickDayFull : "",
          assert.isSickDayMorning(date) ? CSS.daySickDayMorning : "",
          assert.isSickDayNoon(date) ? CSS.daySickDayNoon : "",
        ]
          .filter(Boolean)
          .join(" ");
      }

      function style() {
        // could be morning=sick and noon=vacation
        const [idMorningOrFull, idNoon] = assert.vacationTypeId(date);
        const colorMorningOrFull = `var(--absence-color-${window.uv.vacationTypes.colors[idMorningOrFull]})`;
        const colorNoon = `var(--absence-color-${window.uv.vacationTypes.colors[idNoon]})`;
        return [
          assert.isPublicHolidayFull(date) ? `--absence-bar-color:${colorMorningOrFull}` : ``,
          assert.isPublicHolidayMorning(date) ? `--absence-bar-color-morning:${colorMorningOrFull}` : ``,
          assert.isPublicHolidayNoon(date) ? `--absence-bar-color-noon:${colorNoon}` : ``,
          assert.isPersonalHolidayFull(date) ? `--absence-bar-color:${colorMorningOrFull}` : ``,
          assert.isPersonalHolidayFullTemporaryApproved(date) ? `--absence-bar-color:${colorMorningOrFull}` : ``,
          assert.isPersonalHolidayFullApproved(date) ? `--absence-bar-color:${colorMorningOrFull}` : ``,
          assert.isPersonalHolidayFullCancellationRequest(date) ? `--absence-bar-color:${colorMorningOrFull}` : ``,
          assert.isPersonalHolidayMorning(date) ? `--absence-bar-color-morning:${colorMorningOrFull}` : ``,
          assert.isPersonalHolidayMorningTemporaryApproved(date)
            ? `--absence-bar-color-morning:${colorMorningOrFull}`
            : ``,
          assert.isPersonalHolidayMorningApproved(date) ? `--absence-bar-color-morning:${colorMorningOrFull}` : ``,
          assert.isPersonalHolidayMorningCancellationRequest(date)
            ? `--absence-bar-color-morning:${colorMorningOrFull}`
            : ``,
          assert.isPersonalHolidayNoon(date) ? `--absence-bar-color-noon:${colorNoon}` : ``,
          assert.isPersonalHolidayNoonTemporaryApproved(date) ? `--absence-bar-color-noon:${colorNoon}` : ``,
          assert.isPersonalHolidayNoonApproved(date) ? `--absence-bar-color-noon:${colorNoon}` : ``,
          assert.isPersonalHolidayNoonCancellationRequest(date) ? `--absence-bar-color-noon:${colorNoon}` : ``,
        ]
          .filter(Boolean)
          .join(";");
      }

      function isSelectable() {
        // NOTE: Order is important here!

        const isPersonalHoliday = assert.isPersonalHolidayFull(date);
        const isPersonalHolidayApproved = assert.isPersonalHolidayFullApproved(date);
        const isPersonalHolidayCancellationRequest = assert.isPersonalHolidayFullCancellationRequest(date);
        const isPersonalHolidayTemporaryApproved = assert.isPersonalHolidayFullTemporaryApproved(date);
        const isSickDay = assert.isSickDayFull(date);

        if (
          isPersonalHoliday ||
          isPersonalHolidayApproved ||
          isPersonalHolidayTemporaryApproved ||
          isPersonalHolidayCancellationRequest ||
          isSickDay
        ) {
          return true;
        }

        const isPast = assert.isPast(date);

        if (isPast) {
          return false;
        }

        return assert.isHalfDayAbsence(date) || !assert.isPublicHolidayFull(date);
      }

      function cellStyle() {
        if (date.getDate() === 1) {
          const day = getDay(date);
          // when first of month is sunday -> col_start == 7 otherwise the day value. monday is the first column
          const gridColumnStart = date.getDate() === 1 ? (day === 0 ? 7 : day) : 0;
          return `grid-column-start: ${gridColumnStart};`;
        } else {
          // every other day just follows the flow
          return ``;
        }
      }

      return render(TMPL.day, {
        date: format(date, "yyyy-MM-dd"),
        day: format(date, "dd"),
        ariaDay: format(date, "dd. MMMM"),
        css: classes(),
        style: style(),
        cellStyle: cellStyle(),
        selectable: isSelectable(),
        title: assert.title(date),
        absenceId: assert.absenceId(date),
        absenceType: assert.absenceType(date),
        icon: assert.isNoWorkday(date) ? TMPL.noWorkdayIcon : TMPL.iconPlaceholder,
      });
    }

    const View = {
      display: function (date) {
        $datepicker.html(renderCalendar(date)).addClass("unselectable");
        tooltip();
      },

      displayNext: function () {
        const elements = $datepicker.find("." + CSS.month).get();
        const length_ = elements.length;

        $(elements[0]).remove();

        const $lastMonth = $(elements[length_ - 1]);
        const month = Number($lastMonth.data(DATA.month));
        const year = Number($lastMonth.data(DATA.year));

        const $nextMonth = $(renderMonth(addMonths(new Date(year, month), 1)));

        $lastMonth.after($nextMonth);
        tooltip();
      },

      displayPrevious: function () {
        const elements = $datepicker.find("." + CSS.month).get();
        const length_ = elements.length;

        $(elements[length_ - 1]).remove();

        const $firstMonth = $(elements[0]);
        const month = Number($firstMonth.data(DATA.month));
        const year = Number($firstMonth.data(DATA.year));

        const previousMonth = $(renderMonth(subMonths(new Date(year, month), 1)));

        $firstMonth.before(previousMonth);
        tooltip();
      },
    };

    return {
      create: function (_assert, _i18n) {
        assert = _assert;
        i18n = _i18n;
        return View;
      },
    };
  })();

  const Controller = (function () {
    let view;
    let holidayService;

    const datepickerHandlers = {
      mousedown: function (event) {
        if (event.button != mouseButtons.left) {
          return;
        }

        $(document.body).addClass(CSS.mousedown);

        const dateThis = getDateFromElement(this);

        const start = selectionFrom();
        const end = selectionTo();
        if (!isValidDate(start) || !isValidDate(end) || !isWithinInterval(dateThis, { start, end })) {
          clearSelection();

          $datepicker.data(DATA.selected, dateThis);

          selectionFrom(dateThis);
          selectionTo(dateThis);
        }
      },

      mouseup: function () {
        $(document.body).removeClass(CSS.mousedown);
      },

      mouseover: function () {
        if ($(document.body).hasClass(CSS.mousedown)) {
          const dateThis = getDateFromElement(this);
          const dateSelected = $datepicker.data(DATA.selected);

          const isThisBefore = isBefore(dateThis, dateSelected);

          selectionFrom(isThisBefore ? dateThis : dateSelected);
          selectionTo(isThisBefore ? dateSelected : dateThis);
        }
      },

      click: function () {
        const dateFrom = selectionFrom();
        const dateTo = selectionTo();

        const dateThis = getDateFromElement(this);

        const isSelectable = $(this).attr("data-datepicker-selectable");
        const absenceId = $(this).attr("data-datepicker-absence-id");
        const absenceType = $(this).attr("data-datepicker-absence-type");

        if (isSelectable === "true" && absenceType === "VACATION" && absenceId !== "-1") {
          holidayService.navigateToApplicationForLeave(absenceId);
        } else if (isSelectable === "true" && absenceType === "SICK_NOTE" && absenceId !== "-1") {
          holidayService.navigateToSickNote(absenceId);
        } else if (
          isSelectable === "true" &&
          isValidDate(dateFrom) &&
          isValidDate(dateTo) &&
          isWithinInterval(dateThis, {
            start: dateFrom,
            end: dateTo,
          })
        ) {
          holidayService.bookHoliday(dateFrom, dateTo);
        }
      },

      clickNext: function () {
        // last month of calendar
        const $month = $($datepicker.find("." + CSS.month)[numberOfMonths - 1]);

        const y = $month.data(DATA.year);
        const m = $month.data(DATA.month);

        // to load data for the new (invisible) prev month
        const date = addMonths(new Date(y, m, 1), 1);

        Promise.all([holidayService.fetchPublic(getYear(date)), holidayService.fetchAbsences(getYear(date))]).then(
          view.displayNext,
        );
      },

      clickPrevious: function () {
        // first month of calendar
        const $month = $($datepicker.find("." + CSS.month)[0]);

        const y = $month.data(DATA.year);
        const m = $month.data(DATA.month);

        // to load data for the new (invisible) prev month
        const date = subMonths(new Date(y, m, 1), 1);

        Promise.all([holidayService.fetchPublic(getYear(date)), holidayService.fetchAbsences(getYear(date))]).then(
          view.displayPrevious,
        );
      },
    };

    function selectionFrom(date) {
      if (!date) {
        const d = $datepicker.data(DATA.selectFrom);
        return parseISO(d);
      }

      $datepicker.data(DATA.selectFrom, format(date, "yyyy-MM-dd"));
      refreshDatepicker();
    }

    function selectionTo(date) {
      if (!date) {
        return parseISO($datepicker.data(DATA.selectTo));
      }

      $datepicker.data(DATA.selectTo, format(date, "yyyy-MM-dd"));
      refreshDatepicker();
    }

    function clearSelection() {
      $datepicker.removeData(DATA.selectFrom);
      $datepicker.removeData(DATA.selectTo);
      refreshDatepicker();
    }

    function refreshDatepicker() {
      const start = selectionFrom();
      const end = selectionTo();
      const startIsValid = isValidDate(start);
      const endIsValid = isValidDate(end);

      $("." + CSS.day).each(function () {
        if (!startIsValid || !endIsValid) {
          select(this, false);
        } else {
          const date = parseISO($(this).data(DATA.date));
          select(this, isWithinInterval(date, { start, end }));
        }
      });
    }

    // eslint-disable-next-line unicorn/consistent-function-scoping
    function select(element, select) {
      const element_ = $(element);

      if (!element_.data(DATA.selectable)) {
        return;
      }

      if (select) {
        element_.addClass(CSS.daySelected);
      } else {
        element_.removeClass(CSS.daySelected);
      }
    }

    const Controller = {
      bind: function () {
        $datepicker.on("mousedown", "." + CSS.day, datepickerHandlers.mousedown);
        $datepicker.on("mouseover", "." + CSS.day, datepickerHandlers.mouseover);
        $datepicker.on("click", "." + CSS.day, datepickerHandlers.click);

        $datepicker.on("click", "." + CSS.previous, datepickerHandlers.clickPrevious);
        $datepicker.on("click", "." + CSS.next, datepickerHandlers.clickNext);

        $(document.body).on("keyup", function (event) {
          if (event.keyCode === keyCodes.escape) {
            clearSelection();
          }
        });

        $(document.body).on("mouseup", function () {
          $(document.body).removeClass(CSS.mousedown);
        });
      },
    };

    return {
      create: function (_holidayService, _view) {
        holidayService = _holidayService;
        view = _view;
        return Controller;
      },
    };
  })();

  const Calendar = (function () {
    let view;
    let date;

    return {
      init: function (holidayService, referenceDate, i18n) {
        date = referenceDate;

        const a = Assertion.create(holidayService);
        view = View.create(a, i18n);
        const c = Controller.create(holidayService, view);

        view.display(date);
        c.bind();
      },
    };
  })();

  /**
   * @export
   */
  window.Urlaubsverwaltung = {
    Calendar: Calendar,
    HolidayService: HolidayService,
  };
});
