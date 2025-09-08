import {
  addDays,
  addMonths,
  getDay,
  getMonth,
  getYear,
  isBefore,
  isPast,
  isToday,
  isValid as isValidDate,
  isWeekend,
  isWithinInterval,
  parseISO as dateFnsParseISO,
  startOfMonth,
  subMonths,
} from "date-fns";
import format from "../../lib/date-fns/format";
import startOfWeek from "../../lib/date-fns/start-of-week";
import tooltip from "../tooltip";
import "./calendar.css";

const numberOfMonths = 10;

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
  dayPersonalHolidayFullWaiting: "datepicker-day-absence-full absence-full--outline",
  dayPersonalHolidayHalf: "datepicker-day-absence-full absence-full--outline-solid-half",
  dayPersonalHolidaySecondHalf: "datepicker-day-absence-full absence-full--outline-solid-second-half",
  dayPersonalHolidayFullApproved: "datepicker-day-absence-full absence-full--solid",
  dayPersonalHolidayMorningWaiting: "datepicker-day-absence-morning absence-morning--outline",
  dayPersonalHolidayMorningHalf: "datepicker-day-absence-morning absence-morning--outline-solid-half",
  dayPersonalHolidayMorningSecondHalf: "datepicker-day-absence-morning absence-morning--outline-solid-second-half",
  dayPersonalHolidayMorningApproved: "datepicker-day-absence-morning absence-morning--solid",
  dayPersonalHolidayNoonWaiting: "datepicker-day-absence-noon absence-noon--outline",
  dayPersonalHolidayNoonHalf: "datepicker-day-absence-noon absence-noon--outline-solid-half",
  dayPersonalHolidayNoonSecondHalf: "datepicker-day-absence-noon absence-noon--outline-solid-second-half",
  dayPersonalHolidayNoonApproved: "datepicker-day-absence-noon absence-noon--solid",
  daySickDayFullWaiting: "datepicker-day-sick-note-full absence-full--outline",
  daySickDayFullActive: "datepicker-day-sick-note-full absence-full--solid",
  daySickDayMorningWaiting: "datepicker-day-sick-note-morning absence-morning--outline",
  daySickDayMorningActive: "datepicker-day-sick-note-morning absence-morning--solid",
  daySickDayNoonWaiting: "datepicker-day-sick-note-noon absence-noon--outline",
  daySickDayNoonActive: "datepicker-day-sick-note-noon absence-noon--solid",
  next: "datepicker-next",
  previous: "datepicker-prev",
  month: "calendar-month-container",
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
  chevronRight: `<svg viewBox="0 0 20 20" fill="currentColor" class="tw:w-6 tw:h-6" role="img" aria-hidden="true"><path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd"></path></svg>`,
  chevronLeft: `<svg viewBox="0 0 20 20" fill="currentColor" class="tw:w-6 tw:h-6" role="img" aria-hidden="true"><path fill-rule="evenodd" d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z" clip-rule="evenodd"></path></svg>`,
};

function getDateFromElement(element) {
  return parseISO(element.dataset[DATA.date]);
}

function parseISO(dateStringValue) {
  // date-fns v2.x returned Date(NaN) previously. so just keep using this for falsy argument...
  return dateStringValue ? dateFnsParseISO(dateStringValue) : new Date(Number.NaN);
}

const Assertion = (function () {
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
  };

  return {
    create: function () {
      return assert;
    },
  };
})();

const View = (function () {
  let rootElement;
  let assert;
  let holidayService;
  let i18n;

  const TMPL = {
    container: '{{previousButton}}<div class="calendar-container">{{months}}</div>{{nextButton}}',

    button: '<button class="{{css}}">{{text}}</button>',

    month:
      '<div class="calendar-month-container {{css}}" data-datepicker-month="{{month}}" data-datepicker-year="{{year}}"><p id="calendar-month-{{month}}" class="calendar-month-caption">{{caption}}</p><ol class="calendar-month-grid tw:m-0 tw:p-0 tw:list-none tw:grid" style="grid-template-columns: repeat(7, 1fr);" aria-describedby="calendar calendar-month-{{month}}">{{weekdays}}{{weeks}}</ol></div>',

    // {{0}}......{{6}}
    weekdays: `{{${[0, 1, 2, 3, 4, 5, 6].join("}}{{")}}}`,

    weekday: `<li role="none" aria-hidden="true" class="calendar-month-day-header tw:print:hidden">{{text}}</li>`,

    day: '<li class="tw:border-b tw:border-r tw:border-white tw:dark:border-zinc-900" style="{{cellStyle}}"><span class="tw:sr-only tw:print:hidden">{{ariaDay}}</span><div class="datepicker-day {{css}}" style="{{style}}" data-title="{{title}}" data-datepicker-absence-id={{absenceId}} data-datepicker-absence-type="{{absenceType}}" data-datepicker-date="{{date}}" data-datepicker-selectable="{{selectable}}"><span aria-hidden="true">{{day}}</span>{{icon}}</div></li>',

    iconPlaceholder: '<span class="tw:w-3 tw:h-3 tw:inline-block"></span>',

    noWorkdayIcon:
      '<svg viewBox="0 0 20 20" class="tw:w-3 tw:h-3 tw:opacity-50 tw:stroke-2" fill="currentColor" width="16" height="16" role="img" aria-hidden="true" focusable="false"><path fill-rule="evenodd" d="M13.477 14.89A6 6 0 015.11 6.524l8.367 8.368zm1.414-1.414L6.524 5.11a6 6 0 018.367 8.367zM18 10a8 8 0 11-16 0 8 8 0 0116 0z" clip-rule="evenodd"></path></svg>',
  };

  function render(tmpl, data) {
    return tmpl.replaceAll(/{{(\w+)}}/g, function (_, type) {
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
        `${icons.chevronLeft}<span class="tw:sm:sr-only">${i18n("overview.calendar.button.previous.label")}</span>`,
      ),
      nextButton: renderButton(
        CSS.next,
        `${icons.chevronRight}<span class="tw:sm:sr-only">${i18n("overview.calendar.button.next.label")}</span>`,
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
        holidayService.isPublicHolidayFull(date) ? CSS.dayPublicHolidayFull : "",
        holidayService.isPublicHolidayMorning(date) ? CSS.dayPublicHolidayMorning : "",
        holidayService.isPublicHolidayNoon(date) ? CSS.dayPublicHolidayNoon : "",
        holidayService.isPersonalHolidayFullWaiting(date) ? CSS.dayPersonalHolidayFullWaiting : "",
        holidayService.isPersonalHolidayFullTemporaryApproved(date) ? CSS.dayPersonalHolidayHalf : "",
        holidayService.isPersonalHolidayFullApproved(date) ? CSS.dayPersonalHolidayFullApproved : "",
        holidayService.isPersonalHolidayFullCancellationRequest(date) ? CSS.dayPersonalHolidaySecondHalf : "",
        holidayService.isPersonalHolidayMorningWaiting(date) ? CSS.dayPersonalHolidayMorningWaiting : "",
        holidayService.isPersonalHolidayMorningTemporaryApproved(date) ? CSS.dayPersonalHolidayMorningHalf : "",
        holidayService.isPersonalHolidayMorningApproved(date) ? CSS.dayPersonalHolidayMorningApproved : "",
        holidayService.isPersonalHolidayMorningCancellationRequest(date) ? CSS.dayPersonalHolidayMorningSecondHalf : "",
        holidayService.isPersonalHolidayNoonWaiting(date) ? CSS.dayPersonalHolidayNoonWaiting : "",
        holidayService.isPersonalHolidayNoonTemporaryApproved(date) ? CSS.dayPersonalHolidayNoonHalf : "",
        holidayService.isPersonalHolidayNoonApproved(date) ? CSS.dayPersonalHolidayNoonApproved : "",
        holidayService.isPersonalHolidayNoonCancellationRequest(date) ? CSS.dayPersonalHolidayNoonSecondHalf : "",
        holidayService.isSickDayFullWaiting(date) ? CSS.daySickDayFullWaiting : "",
        holidayService.isSickDayFullActive(date) ? CSS.daySickDayFullActive : "",
        holidayService.isSickDayMorningWaiting(date) ? CSS.daySickDayMorningWaiting : "",
        holidayService.isSickDayMorningActive(date) ? CSS.daySickDayMorningActive : "",
        holidayService.isSickDayNoonWaiting(date) ? CSS.daySickDayNoonWaiting : "",
        holidayService.isSickDayNoonActive(date) ? CSS.daySickDayNoonActive : "",
      ]
        .filter(Boolean)
        .join(" ");
    }

    function style() {
      // could be morning=sick and noon=vacation
      const [idMorningOrFull, idNoon] = holidayService.getTypeId(date);
      const colorMorningOrFull = `var(--absence-color-${globalThis.uv.vacationTypes.colors[idMorningOrFull]})`;
      const colorNoon = `var(--absence-color-${globalThis.uv.vacationTypes.colors[idNoon]})`;

      return [
        holidayService.isPersonalHolidayFull(date) ? `--absence-bar-color:${colorMorningOrFull}` : ``,
        holidayService.isPersonalHolidayMorning(date) ? `--absence-bar-color-morning:${colorMorningOrFull}` : ``,
        holidayService.isPersonalHolidayNoon(date) ? `--absence-bar-color-noon:${colorNoon}` : ``,

        holidayService.isSickDayFullWaiting(date) || holidayService.isSickDayFullActive(date)
          ? "--absence-bar-color: var(--sick-note-color)"
          : "",
        holidayService.isSickDayMorningWaiting(date) || holidayService.isSickDayMorningActive(date)
          ? "--absence-bar-color-morning: var(--sick-note-color)"
          : "",
        holidayService.isSickDayNoonWaiting(date) || holidayService.isSickDayNoonActive(date)
          ? "--absence-bar-color-noon: var(--sick-note-color)"
          : "",
      ]
        .filter(Boolean)
        .join(";");
    }

    function isSelectable() {
      // NOTE: Order is important here!

      const isPersonalHolidayWaiting = holidayService.isPersonalHolidayFullWaiting(date);
      const isPersonalHolidayApproved = holidayService.isPersonalHolidayFullApproved(date);
      const isPersonalHolidayCancellationRequest = holidayService.isPersonalHolidayFullCancellationRequest(date);
      const isPersonalHolidayTemporaryApproved = holidayService.isPersonalHolidayFullTemporaryApproved(date);
      const isSickDayActive = holidayService.isSickDayFullActive(date);
      const isSickDayWaiting = holidayService.isSickDayFullWaiting(date);

      if (
        isPersonalHolidayWaiting ||
        isPersonalHolidayApproved ||
        isPersonalHolidayTemporaryApproved ||
        isPersonalHolidayCancellationRequest ||
        isSickDayActive ||
        isSickDayWaiting
      ) {
        return true;
      }

      const isPast = assert.isPast(date);

      if (isPast) {
        return false;
      }

      return holidayService.isHalfDayAbsence(date) || !holidayService.isPublicHolidayFull(date);
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
      title: holidayService.getDescription(date),
      absenceId: holidayService.getAbsenceId(date),
      absenceType: holidayService.getAbsenceType(date),
      icon: holidayService.isNoWorkday(date) ? TMPL.noWorkdayIcon : TMPL.iconPlaceholder,
    });
  }

  const View = {
    display: function (date) {
      rootElement.innerHTML = renderCalendar(date);
      rootElement.classList.add("unselectable");
      tooltip();
    },

    getRootElement() {
      return rootElement;
    },

    displayNext: function () {
      const elements = [...rootElement.querySelectorAll("." + CSS.month)];

      elements[0]?.remove();

      const lastMonthElement = elements.at(-1);
      const month = Number(lastMonthElement.dataset[DATA.month]);
      const year = Number(lastMonthElement.dataset[DATA.year]);

      const nextMonthHtml = renderMonth(addMonths(new Date(year, month), 1));
      const nextMonthParentElement = document.createElement("div");
      nextMonthParentElement.innerHTML = nextMonthHtml;

      lastMonthElement.insertAdjacentElement("afterend", nextMonthParentElement.firstElementChild);
      tooltip();
    },

    displayPrevious: function () {
      const elements = [...rootElement.querySelectorAll("." + CSS.month)];

      elements.at(-1)?.remove();

      const firstMonthElement = elements[0];
      const month = Number(firstMonthElement.dataset[DATA.month]);
      const year = Number(firstMonthElement.dataset[DATA.year]);

      let previousMonthHtml = renderMonth(subMonths(new Date(year, month), 1));
      const previousMonthParentElement = document.createElement("div");
      previousMonthParentElement.innerHTML = previousMonthHtml;

      firstMonthElement.insertAdjacentElement("beforebegin", previousMonthParentElement.firstElementChild);
      tooltip();
    },
  };

  return {
    create: function (_rootElement, _assert, _holidayService, _i18n) {
      rootElement = _rootElement;
      assert = _assert;
      holidayService = _holidayService;
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
      if (event.button !== mouseButtons.left) {
        return;
      }

      document.body.classList.add(CSS.mousedown);

      const dateThis = getDateFromElement(this);

      const start = selectionFrom();
      const end = selectionTo();
      if (!isValidDate(start) || !isValidDate(end) || !isWithinInterval(dateThis, { start, end })) {
        clearSelection();

        view.getRootElement().dataset[DATA.selected] = dateThis;

        selectionFrom(dateThis);
        selectionTo(dateThis);
      }
    },

    mouseup: function () {
      document.body.classList.remove(CSS.mousedown);
    },

    mouseover: function () {
      if (document.body.classList.contains(CSS.mousedown)) {
        const dateThis = getDateFromElement(this);
        const dateSelected = new Date(view.getRootElement().dataset[DATA.selected]);

        const isThisBefore = isBefore(dateThis, dateSelected);

        selectionFrom(isThisBefore ? dateThis : dateSelected);
        selectionTo(isThisBefore ? dateSelected : dateThis);
      }
    },

    click: function () {
      const dateFrom = selectionFrom();
      const dateTo = selectionTo();

      const dateThis = getDateFromElement(this);

      const isSelectable = this.dataset.datepickerSelectable;
      const absenceId = this.dataset.datepickerAbsenceId;
      const absenceType = this.dataset.datepickerAbsenceType;

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
      const monthElement = [...view.getRootElement().querySelectorAll("." + CSS.month)][numberOfMonths - 1];

      const y = monthElement.dataset[DATA.year];
      const m = monthElement.dataset[DATA.month];

      // to load data for the new (invisible) prev month
      const date = addMonths(new Date(y, m, 1), 1);

      Promise.all([
        holidayService.fetchPublicHolidays(getYear(date)),
        holidayService.fetchAbsences(getYear(date)),
      ]).then(view.displayNext);
    },

    clickPrevious: function () {
      // first month of calendar
      const monthElement = [...view.getRootElement().querySelectorAll("." + CSS.month)][0];

      const y = monthElement.dataset[DATA.year];
      const m = monthElement.dataset[DATA.month];

      // to load data for the new (invisible) prev month
      const date = subMonths(new Date(y, m, 1), 1);

      Promise.all([
        holidayService.fetchPublicHolidays(getYear(date)),
        holidayService.fetchAbsences(getYear(date)),
      ]).then(view.displayPrevious);
    },
  };

  function selectionFrom(date) {
    if (date) {
      view.getRootElement().dataset[DATA.selectFrom] = format(date, "yyyy-MM-dd");
      refreshDatepicker();
    } else {
      const d = view.getRootElement().dataset[DATA.selectFrom];
      return parseISO(d);
    }
  }

  function selectionTo(date) {
    if (date) {
      view.getRootElement().dataset[DATA.selectTo] = format(date, "yyyy-MM-dd");
      refreshDatepicker();
    } else {
      return parseISO(view.getRootElement().dataset[DATA.selectTo]);
    }
  }

  function clearSelection() {
    delete view.getRootElement().dataset[DATA.selectFrom];
    delete view.getRootElement().dataset[DATA.selectTo];
    refreshDatepicker();
  }

  function refreshDatepicker() {
    const start = selectionFrom();
    const end = selectionTo();
    const startIsValid = isValidDate(start);
    const endIsValid = isValidDate(end);

    for (let dayElement of document.querySelectorAll(`.${CSS.day}`)) {
      if (!startIsValid || !endIsValid) {
        select(dayElement, false);
      } else {
        const date = parseISO(dayElement.dataset[DATA.date]);
        select(dayElement, isWithinInterval(date, { start, end }));
      }
    }
  }

  function select(element, select) {
    if (!element.dataset[DATA.selectable]) {
      return;
    }

    if (select) {
      element.classList.add(CSS.daySelected);
    } else {
      element.classList.remove(CSS.daySelected);
    }
  }

  const matches = (element, query) => {
    if (!element) {
      return;
    }
    if (element.matches(query)) {
      return element;
    }
    return matches(element.parentElement, query);
  };

  const Controller = {
    bind: function () {
      view.getRootElement().addEventListener("mousedown", function (event) {
        const element = matches(event.target, `.${CSS.day}`);
        if (element) {
          datepickerHandlers.mousedown.call(element, event);
        }
      });

      view.getRootElement().addEventListener("mouseover", function (event) {
        const element = matches(event.target, `.${CSS.day}`);
        if (element) {
          datepickerHandlers.mouseover.call(element, event);
        }
      });

      view.getRootElement().addEventListener("click", function (event) {
        let element = matches(event.target, `.${CSS.day}`);
        if (element) {
          datepickerHandlers.click.call(element, event);
        } else if ((element = matches(event.target, `.${CSS.previous}`))) {
          datepickerHandlers.clickPrevious.call(element, event);
        } else if ((element = matches(event.target, `.${CSS.next}`))) {
          datepickerHandlers.clickNext.call(element, event);
        }
      });

      document.body.addEventListener("keyup", function (event) {
        if (event.key === "Escape") {
          clearSelection();
        }
      });

      document.body.addEventListener("mouseup", function () {
        document.body.classList.remove(CSS.mousedown);
      });

      const smScreenQuery = globalThis.matchMedia("(max-width: 640px)");
      if (smScreenQuery.matches) {
        for (const button of view.getRootElement().querySelectorAll("button")) {
          button.classList.add("button");
        }
      }

      smScreenQuery.addEventListener("change", function () {
        for (const button of view.getRootElement().querySelectorAll("button")) {
          button.classList.toggle("button");
        }
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

export const Calendar = (function () {
  let view;
  let date;

  return {
    init: function (rootElement, holidayService, referenceDate, i18n) {
      date = referenceDate;

      const assertions = Assertion.create();
      view = View.create(rootElement, assertions, holidayService, i18n);
      const controller = Controller.create(holidayService, view);

      view.display(date);
      controller.bind();
    },
  };
})();
