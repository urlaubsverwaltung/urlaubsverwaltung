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
import "./calendar.css";
import { createPopper } from "@popperjs/core";

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
  hasAnyAbsence: "datepickerHasAnyAbsence",
};

const icons = {
  chevronRight: `<svg viewBox="0 0 20 20" fill="currentColor" class="w-6 h-6" role="img" aria-hidden="true"><path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd"></path></svg>`,
  chevronLeft: `<svg viewBox="0 0 20 20" fill="currentColor" class="w-6 h-6" role="img" aria-hidden="true"><path fill-rule="evenodd" d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z" clip-rule="evenodd"></path></svg>`,
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
      '<div class="calendar-month-container {{css}}" data-datepicker-month="{{month}}" data-datepicker-year="{{year}}"><p id="calendar-month-{{month}}" class="calendar-month-caption">{{caption}}</p><ol class="calendar-month-grid m-0 p-0 list-none grid" style="grid-template-columns: repeat(7, 1fr);" aria-describedby="calendar calendar-month-{{month}}">{{weekdays}}{{weeks}}</ol></div>',

    // {{0}}......{{6}}
    weekdays: `{{${[0, 1, 2, 3, 4, 5, 6].join("}}{{")}}}`,

    weekday: `<li role="none" aria-hidden="true" class="calendar-month-day-header print:hidden">{{text}}</li>`,

    day: '<li class="border-b border-r border-white dark:border-zinc-900" style="{{cellStyle}}"><span class="sr-only print:hidden">{{ariaDay}}</span><div class="datepicker-day {{css}}" style="{{style}}" data-title="{{title}}" data-datepicker-date="{{date}}" data-datepicker-selectable="{{selectable}}" data-datepicker-has-any-absence="{{hasAnyAbsence}}"><span aria-hidden="true">{{day}}</span>{{icon}}</div></li>',

    dayPopover:
      '<div class="calendar_popover" data-date="{{date}}">{{content}}<div id="arrow" data-popper-arrow></div></div>',

    popoverContentAbsence:
      '<div class="calendar_popover_entry absence-details {{css_classes}}" style="--absence-bar-color:{{color}};">{{title}}<br><a href="{{href}}">{{linkText}}</a></div>',

    popoverContentAbsenceCreation:
      '<div class="calendar_popover_entry" >{{title}}<br><a href="{{href}}">{{linkText}}</a></div>',

    iconPlaceholder: '<span class="w-3 h-3 inline-block"></span>',

    noWorkdayIcon:
      '<svg viewBox="0 0 20 20" class="w-3 h-3 opacity-50 stroke-2" fill="currentColor" width="16" height="16" role="img" aria-hidden="true" focusable="false"><path fill-rule="evenodd" d="M13.477 14.89A6 6 0 015.11 6.524l8.367 8.368zm1.414-1.414L6.524 5.11a6 6 0 018.367 8.367zM18 10a8 8 0 11-16 0 8 8 0 0116 0z" clip-rule="evenodd"></path></svg>',
  };

  function color(absence) {
    if (absence.absenceType === "VACATION") {
      return `var(--absence-color-${globalThis.uv.vacationTypes.colors[absence.typeId]})`;
    }
    if (absence.absenceType === "SICK_NOTE") {
      return `var(--sick-note-color)`;
    }
  }

  function cssClass(absence) {
    const status = absence.status;
    if (status === "WAITING") {
      return "absence--outline";
    }
    if (status === "TEMPORARY_ALLOWED") {
      return "absence--outline-solid-half";
    }
    if (status === "ALLOWED_CANCELLATION_REQUESTED") {
      return "absence--outline-solid-second-half";
    }
    if (status === "ALLOWED" || status === "ACTIVE") {
      return "absence--solid";
    }
  }

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
        `${icons.chevronLeft}<span class="sm:sr-only">${i18n("overview.calendar.button.previous.label")}</span>`,
      ),
      nextButton: renderButton(
        CSS.next,
        `${icons.chevronRight}<span class="sm:sr-only">${i18n("overview.calendar.button.next.label")}</span>`,
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
      const [fullAbsence, morningAbsence, noonAbsence] = holidayService.getPersonalAbsencesOfType(date);

      return [
        fullAbsence ? `--absence-bar-color:${color(fullAbsence)}` : ``,
        morningAbsence ? `--absence-bar-color-morning:${color(morningAbsence)}` : ``,
        noonAbsence ? `--absence-bar-color-noon:${color(noonAbsence)}` : ``,
      ]
        .filter(Boolean)
        .join(";");
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

    let dayHtml = render(TMPL.day, {
      date: format(date, "yyyy-MM-dd"),
      day: format(date, "dd"),
      ariaDay: format(date, "dd. MMMM"),
      css: classes(),
      style: style(),
      cellStyle: cellStyle(),
      selectable: !assert.isPast(date),
      hasAnyAbsence: holidayService.hasAnyPersonalAbsence(date),
      title: holidayService.getDescription(date),
      icon: holidayService.isNoWorkday(date) ? TMPL.noWorkdayIcon : TMPL.iconPlaceholder,
    });

    let popoverHtml = renderPopover(date);
    return dayHtml + popoverHtml;
  }

  function renderPopoverAbsenceContent(absence) {
    let href = "";
    let title = ""; //TODO die public holidays haben eine localized description. Woher?
    if (absence.absenceType === "VACATION" && absence.id !== "-1") {
      href = holidayService.getApplicationForLeaveWebUrl(absence.id);
      title = i18n("overtime.popover.absence.VACATION");
    } else if (absence.absenceType === "SICK_NOTE" && absence.id !== "-1") {
      href = holidayService.getSickNoteWebUrl(absence.id);
      title = i18n("overtime.popover.absence.SICK_NOTE");
    }

    return render(TMPL.popoverContentAbsence, {
      css_classes: cssClass(absence),
      color: color(absence),
      title: title,
      href: href,
      linkText: i18n("overtime.popover.details"),
    });
  }

  function renderPopoverAbsenceCreationContent(date) {
    let href = holidayService.getNewHolidayUrl(date, date);
    let linkText = i18n("overtime.popover.new-application");

    return render(TMPL.popoverContentAbsenceCreation, {
      href: href,
      title: "Keine Abwesenheit (i18n?)",
      linkText: linkText,
    });
  }

  function renderPopover(date) {
    let content = "";

    const [full, morning, noon] = holidayService.getPersonalAbsencesOfType(date);

    if (holidayService.isPersonalAbsenceFull(date)) {
      content = renderPopoverAbsenceContent(full);
    } else if (holidayService.isPersonalHalfDayAbsence(date)) {
      const morningContent = holidayService.isPersonalAbsenceMorning(date)
        ? renderPopoverAbsenceContent(morning)
        : renderPopoverAbsenceCreationContent(date);
      const noonContent = holidayService.isPersonalAbsenceNoon(date)
        ? renderPopoverAbsenceContent(noon)
        : renderPopoverAbsenceCreationContent(date);

      content = morningContent + noonContent;
    }
    if (content) {
      return render(TMPL.dayPopover, {
        date: format(date, "yyyy-MM-dd"),
        content: content,
      });
    }
    return "";
  }

  let daysWithPopover = [];
  const View = {
    display: function (date) {
      rootElement.innerHTML = renderCalendar(date);
      rootElement.classList.add("unselectable");
      this.initializePopovers();
    },

    initializePopovers() {
      for (const popover of document.querySelectorAll(".calendar_popover")) {
        const date = popover.dataset.date;
        const dayButton = document.querySelector(`[data-datepicker-date="${date}"]`);
        daysWithPopover.push(dayButton);

        const popperInstance = createPopper(dayButton, popover, {
          modifiers: [
            {
              name: "offset",
              options: {
                offset: [0, 8],
              },
            },
          ],
        });

        function show() {
          this.dispatchEvent(
            new CustomEvent("closeAllPoppers", {
              detail: { except: [dayButton] },
              bubbles: true,
            }),
          );
          // Make the popover visible
          popover.dataset.show = "";

          // Enable the event listeners
          popperInstance.setOptions((options) => ({
            ...options,
            modifiers: [...options.modifiers, { name: "eventListeners", enabled: true }],
          }));

          // Update its position
          popperInstance.update();
        }

        function hide() {
          // Hide the popover
          delete popover.dataset.show;

          // Disable the event listeners
          popperInstance.setOptions((options) => ({
            ...options,
            modifiers: [...options.modifiers, { name: "eventListeners", enabled: false }],
          }));
        }

        dayButton.addEventListener("openPopper", show);
        dayButton.addEventListener("closePopper", hide);
      }

      // Close all poppers when clicking outside
      const calendarContainer = document.querySelector(`.calendar-container`);
      calendarContainer.addEventListener("closeAllPoppers", (event) => {
        const except = event.detail.except;
        closeAllExcept(except);
      });
      calendarContainer.addEventListener("click", () => {
        closeAllExcept([]);
      });

      function closeAllExcept(except) {
        for (const dayButton of daysWithPopover) {
          if (!except.includes(dayButton)) {
            dayButton.dispatchEvent(new Event("closePopper"));
          }
        }
      }
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
      const hasAnyAbsence = this.dataset.datepickerHasAnyAbsence;
      //These can be different for half days?

      if (hasAnyAbsence === "true") {
        this.dispatchEvent(new Event("openPopper"));
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

    element.classList.toggle(CSS.daySelected, select);
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
