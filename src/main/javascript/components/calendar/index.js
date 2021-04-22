import $ from "jquery";
import { findWhere } from "underscore";
import {
  addDays,
  addMonths,
  addWeeks,
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
    dayPersonalHolidayFull: "datepicker-day-personal-holiday-full",
    dayPersonalHolidayFullApproved: "datepicker-day-personal-holiday-full-approved",
    dayPersonalHolidayMorning: "datepicker-day-personal-holiday-morning",
    dayPersonalHolidayMorningApproved: "datepicker-day-personal-holiday-morning-approved",
    dayPersonalHolidayNoon: "datepicker-day-personal-holiday-noon",
    dayPersonalHolidayNoonApproved: "datepicker-day-personal-holiday-noon-approved",
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
    chevronRight: `<svg viewBox="0 0 20 20" fill="currentColor" class="tw-w-6 tw-h-6"><path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd"></path></svg>`,
    chevronLeft: `<svg viewBox="0 0 20 20" fill="currentColor" class="tw-w-6 tw-h-6"><path fill-rule="evenodd" d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z" clip-rule="evenodd"></path></svg>`,
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
        return !isWeekend(date) && holidayService.isPersonalHolidayFull(date);
      },
      isPersonalHolidayFullApproved: function (date) {
        return !isWeekend(date) && holidayService.isPersonalHolidayFullApproved(date);
      },
      isPersonalHolidayFullCancellationRequest: function (date) {
        return !isWeekend(date) && holidayService.isPersonalHolidayFullCancellationRequest(date);
      },
      isPersonalHolidayMorning: function (date) {
        return !isWeekend(date) && holidayService.isPersonalHolidayMorning(date);
      },
      isPersonalHolidayMorningApproved: function (date) {
        return !isWeekend(date) && holidayService.isPersonalHolidayMorningApproved(date);
      },
      isPersonalHolidayMorningCancellationRequest: function (date) {
        return !isWeekend(date) && holidayService.isPersonalHolidayMorningCancellationRequest(date);
      },
      isPersonalHolidayNoon: function (date) {
        return !isWeekend(date) && holidayService.isPersonalHolidayNoon(date);
      },
      isPersonalHolidayNoonApproved: function (date) {
        return !isWeekend(date) && holidayService.isPersonalHolidayNoonApproved(date);
      },
      isPersonalHolidayNoonCancellationRequest: function (date) {
        return !isWeekend(date) && holidayService.isPersonalHolidayNoonCancellationRequest(date);
      },
      isSickDayFull: function (date) {
        return !isWeekend(date) && holidayService.isSickDayFull(date);
      },
      isSickDayMorning: function (date) {
        return !isWeekend(date) && holidayService.isSickDayMorning(date);
      },
      isSickDayNoon: function (date) {
        return !isWeekend(date) && holidayService.isSickDayNoon(date);
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
      status: function (date) {
        return holidayService.getStatus(date);
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

    function cacheAbsences(type, year) {
      const c = (_CACHE[type] = _CACHE[type] || {});

      return function (data) {
        const absences = data.absences;

        if (absences.length > 0) {
          $.each(absences, function (idx, absence) {
            c[year] = c[year] || [];
            c[year].push(absence);
          });
        } else {
          c[year] = [];
        }
      };
    }

    function cachePublicHoliday(year) {
      const c = (_CACHE["publicHoliday"] = _CACHE["publicHoliday"] || {});

      return function (data) {
        const publicHolidays = data.publicHolidays;

        if (publicHolidays.length > 0) {
          $.each(publicHolidays, function (idx, publicHoliday) {
            c[year] = c[year] || [];
            c[year].push(publicHoliday);
          });
        } else {
          c[year] = c[year] || [];
        }
      };
    }

    function isOfType(type, matcherAttributes) {
      return function (date) {
        const year = getYear(date);
        const formattedDate = format(date, "yyyy-MM-dd");

        if (!_CACHE[type]) {
          return false;
        }

        if (_CACHE[type][year]) {
          const absence = findWhere(_CACHE[type][year], {
            ...matcherAttributes,
            date: formattedDate,
          });
          return Boolean(absence);
        }

        return false;
      };
    }

    const absencePeriod = Object.freeze({
      FULL: "FULL",
      MORNING: "MORNING",
      NOON: "NOON",
    });

    const HolidayService = {
      isSickDayFull: isOfType("sick", {
        absencePeriodName: absencePeriod.FULL,
      }),
      isSickDayMorning: isOfType("sick", {
        absencePeriodName: absencePeriod.MORNING,
      }),
      isSickDayNoon: isOfType("sick", {
        absencePeriodName: absencePeriod.NOON,
      }),

      isPersonalHolidayFull: isOfType("holiday", {
        absencePeriodName: absencePeriod.FULL,
        status: "WAITING",
      }),
      isPersonalHolidayFullApproved: isOfType("holiday", {
        absencePeriodName: absencePeriod.FULL,
        status: "ALLOWED",
      }),
      isPersonalHolidayFullCancellationRequest: isOfType("holiday", {
        absencePeriodName: absencePeriod.FULL,
        status: "ALLOWED_CANCELLATION_REQUESTED",
      }),

      isPersonalHolidayMorning: isOfType("holiday", {
        absencePeriodName: absencePeriod.MORNING,
        status: "WAITING",
      }),
      isPersonalHolidayMorningApproved: isOfType("holiday", {
        absencePeriodName: absencePeriod.MORNING,
        status: "ALLOWED",
      }),
      isPersonalHolidayMorningCancellationRequest: isOfType("holiday", {
        absencePeriodName: absencePeriod.MORNING,
        status: "ALLOWED_CANCELLATION_REQUESTED",
      }),

      isPersonalHolidayNoon: isOfType("holiday", {
        absencePeriodName: absencePeriod.NOON,
        status: "WAITING",
      }),
      isPersonalHolidayNoonApproved: isOfType("holiday", {
        absencePeriodName: absencePeriod.NOON,
        status: "ALLOWED",
      }),
      isPersonalHolidayNoonCancellationRequest: isOfType("holiday", {
        absencePeriodName: absencePeriod.NOON,
        status: "ALLOWED_CANCELLATION_REQUESTED",
      }),

      isPublicHolidayFull: isOfType("publicHoliday", {
        absencePeriodName: absencePeriod.FULL,
      }),
      isPublicHolidayMorning: isOfType("publicHoliday", {
        absencePeriodName: absencePeriod.MORNING,
      }),
      isPublicHolidayNoon: isOfType("publicHoliday", {
        absencePeriodName: absencePeriod.NOON,
      }),

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

      getStatus: function (date) {
        const year = getYear(date);
        const formattedDate = format(date, "yyyy-MM-dd");

        if (_CACHE["holiday"] && _CACHE["holiday"][year]) {
          const holiday = findWhere(_CACHE["holiday"][year], {
            date: formattedDate,
          });
          if (holiday) {
            return holiday.status;
          }
        }

        return undefined;
      },

      getAbsenceId: function (date) {
        const year = getYear(date);
        const formattedDate = format(date, "yyyy-MM-dd");

        if (_CACHE["holiday"] && _CACHE["holiday"][year]) {
          const holiday = findWhere(_CACHE["holiday"][year], {
            date: formattedDate,
          });
          if (holiday) {
            return holiday.href;
          }
        }

        if (_CACHE["sick"] && _CACHE["sick"][year]) {
          const sickDay = findWhere(_CACHE["sick"][year], {
            date: formattedDate,
          });
          if (sickDay) {
            return sickDay.href;
          }
        }

        return "-1";
      },

      getAbsenceType: function (date) {
        const year = getYear(date);
        const formattedDate = format(date, "yyyy-MM-dd");

        if (_CACHE["holiday"] && _CACHE["holiday"][year]) {
          const holiday = findWhere(_CACHE["holiday"][year], {
            date: formattedDate,
          });
          if (holiday) {
            return holiday.type;
          }
        }

        if (_CACHE["sick"] && _CACHE["sick"][year]) {
          const sickDay = findWhere(_CACHE["sick"][year], {
            date: formattedDate,
          });
          if (sickDay) {
            return sickDay.type;
          }
        }

        return "";
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
          return Promise.resolve(_CACHE[year]);
        }

        const firstDayOfYear = formatISO(startOfYear(parse(year, "yyyy", new Date())), { representation: "date" });
        const lastDayOfYear = formatISO(endOfYear(parse(year, "yyyy", new Date())), { representation: "date" });

        return fetch("/persons/" + personId + "/public-holidays", {
          from: firstDayOfYear,
          to: lastDayOfYear,
        }).then(cachePublicHoliday(year));
      },

      fetchPersonal: function (year) {
        _CACHE["holiday"] = _CACHE["holiday"] || {};

        if (_CACHE["holiday"][year]) {
          return Promise.resolve(_CACHE[year]);
        }

        const firstDayOfYear = formatISO(startOfYear(parse(year, "yyyy", new Date())), { representation: "date" });
        const lastDayOfYear = formatISO(endOfYear(parse(year, "yyyy", new Date())), { representation: "date" });

        return fetch("/persons/" + personId + "/absences", {
          from: firstDayOfYear,
          to: lastDayOfYear,
          type: "VACATION",
        }).then(cacheAbsences("holiday", year));
      },

      fetchSickDays: function (year) {
        _CACHE["sick"] = _CACHE["sick"] || {};

        if (_CACHE["sick"][year]) {
          return Promise.resolve(_CACHE[year]);
        }

        const firstDayOfYear = formatISO(startOfYear(parse(year, "yyyy", new Date())), { representation: "date" });
        const lastDayOfYear = formatISO(endOfYear(parse(year, "yyyy", new Date())), { representation: "date" });

        return fetch("/persons/" + personId + "/absences", {
          from: firstDayOfYear,
          to: lastDayOfYear,
          type: "SICK_NOTE",
        }).then(cacheAbsences("sick", year));
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

    const TMPL = {
      container: '{{previousButton}}<div class="datepicker-months-container">{{months}}</div>{{nextButton}}',

      button: '<button class="{{css}}">{{text}}</button>',

      month:
        '<div class="datepicker-month {{css}}" data-datepicker-month="{{month}}" data-datepicker-year="{{year}}">{{title}}<table class="datepicker-table"><thead>{{weekdays}}</thead><tbody>{{weeks}}</tbody></table></div>',

      title: "<h3>{{title}}</h3>",

      // <tr><th>{{0}}</th>......<th>{{6}}</th></tr>
      weekdays: "<tr><th>{{" + [0, 1, 2, 3, 4, 5, 6].join("}}</th><th>{{") + "}}</th></tr>",

      // <tr><td>{{0}}</td>......<td>{{6}}</td></tr>
      week: "<tr><td>{{" + [0, 1, 2, 3, 4, 5, 6].join("}}</td><td>{{") + "}}</td></tr>",

      day:
        '<span class="datepicker-day {{css}}" data-title="{{title}}" data-datepicker-absence-id={{absenceId}} data-datepicker-absence-type="{{absenceType}}" data-datepicker-date="{{date}}" data-datepicker-selectable="{{selectable}}">{{day}}</span>',
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
        previousButton: renderButton(CSS.previous, `<span>${icons.chevronLeft}</span>`),
        nextButton: renderButton(CSS.next, `<span>${icons.chevronRight}</span>`),

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
        month: getMonth(d),
        year: getYear(d),
        title: renderMonthTitle(d),
        weekdays: renderWeekdaysHeader(d),

        weeks: function () {
          let html = "";
          while (getMonth(d) === m) {
            html += renderWeek(d);
            d = addWeeks(d, 1);
            d = startOfWeek(d);
          }
          return html;
        },
      });
    }

    function renderMonthTitle(date) {
      return render(TMPL.title, {
        title: format(date, "MMMM yyyy"),
      });
    }

    function renderWeekdaysHeader(date) {
      const d = startOfWeek(date);

      return render(TMPL.weekdays, {
        0: format(d, "EEEEEE"),
        1: format(addDays(d, 1), "EEEEEE"),
        2: format(addDays(d, 2), "EEEEEE"),
        3: format(addDays(d, 3), "EEEEEE"),
        4: format(addDays(d, 4), "EEEEEE"),
        5: format(addDays(d, 5), "EEEEEE"),
        6: format(addDays(d, 6), "EEEEEE"),
      });
    }

    function renderWeek(date) {
      let d = date;
      const m = getMonth(d);

      return render(TMPL.week, function (_, dayIdx) {
        let dayIndexToRender = Number(dayIdx) + window.uv.weekStartsOn;
        if (dayIndexToRender === 7) {
          dayIndexToRender = 0;
        }

        let html = "&nbsp;";

        if (dayIndexToRender === getDay(d) && m === getMonth(d)) {
          html = renderDay(d);
          d = addDays(d, 1);
        }

        return html;
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
          assert.isPersonalHolidayFullApproved(date) ? CSS.dayPersonalHolidayFullApproved : "",
          assert.isPersonalHolidayFullCancellationRequest(date) ? CSS.dayPersonalHolidayFullApproved : "",
          assert.isPersonalHolidayMorning(date) ? CSS.dayPersonalHolidayMorning : "",
          assert.isPersonalHolidayMorningApproved(date) ? CSS.dayPersonalHolidayMorningApproved : "",
          assert.isPersonalHolidayMorningCancellationRequest(date) ? CSS.dayPersonalHolidayMorningApproved : "",
          assert.isPersonalHolidayNoon(date) ? CSS.dayPersonalHolidayNoon : "",
          assert.isPersonalHolidayNoonApproved(date) ? CSS.dayPersonalHolidayNoonApproved : "",
          assert.isPersonalHolidayNoonCancellationRequest(date) ? CSS.dayPersonalHolidayNoonApproved : "",
          assert.isSickDayFull(date) ? CSS.daySickDayFull : "",
          assert.isSickDayMorning(date) ? CSS.daySickDayMorning : "",
          assert.isSickDayNoon(date) ? CSS.daySickDayNoon : "",
        ]
          .filter(Boolean)
          .join(" ");
      }

      function isSelectable() {
        // NOTE: Order is important here!

        const isPersonalHoliday = assert.isPersonalHolidayFull(date);
        const isPersonalHolidayApproved = assert.isPersonalHolidayFullApproved(date);
        const isPersonalHolidayCancellationRequest = assert.isPersonalHolidayFullCancellationRequest(date);
        const isSickDay = assert.isSickDayFull(date);

        if (isPersonalHoliday || isPersonalHolidayApproved || isPersonalHolidayCancellationRequest || isSickDay) {
          return true;
        }

        const isPast = assert.isPast(date);

        if (isPast) {
          return false;
        }

        return assert.isHalfDayAbsence(date) || !assert.isPublicHolidayFull(date);
      }

      return render(TMPL.day, {
        date: format(date, "yyyy-MM-dd"),
        day: format(date, "dd"),
        css: classes(),
        selectable: isSelectable(),
        title: assert.title(date),
        absenceId: assert.absenceId(date),
        absenceType: assert.absenceType(date),
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
      create: function (_assert) {
        assert = _assert;
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

        Promise.all([
          holidayService.fetchPublic(getYear(date)),
          holidayService.fetchPersonal(getYear(date)),
          holidayService.fetchSickDays(getYear(date)),
        ]).then(view.displayNext);
      },

      clickPrevious: function () {
        // first month of calendar
        const $month = $($datepicker.find("." + CSS.month)[0]);

        const y = $month.data(DATA.year);
        const m = $month.data(DATA.month);

        // to load data for the new (invisible) prev month
        const date = subMonths(new Date(y, m, 1), 1);

        Promise.all([
          holidayService.fetchPublic(getYear(date)),
          holidayService.fetchPersonal(getYear(date)),
          holidayService.fetchSickDays(getYear(date)),
        ]).then(view.displayPrevious);
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
      init: function (holidayService, referenceDate) {
        date = referenceDate;

        const a = Assertion.create(holidayService);
        view = View.create(a);
        const c = Controller.create(holidayService, view);

        view.display(date);
        c.bind();
      },

      reRender: function () {
        view.display(date);
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
