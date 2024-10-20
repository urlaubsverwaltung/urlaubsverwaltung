import { endOfMonth, formatISO, parseISO } from "date-fns";
import parse from "../../lib/date-fns/parse";
import { defineCustomElements } from "@duetds/date-picker/dist/loader";
import { getJSON } from "../../js/fetch";
import { mutation } from "./mutation";
import { createDatepickerLocalization } from "./locale";
import { addDatepickerCssClassesToNode, removeDatepickerCssClassesFromNode } from "./datepicker-css-classes";
import { addAbsenceTypeStyleToNode, isNoWorkday, removeAbsenceTypeStyleFromNode } from "../../js/absence";
import { onTurboBeforeRenderRestore } from "../../js/turbo";
import "@duetds/date-picker/dist/collection/themes/default.css";
import "./datepicker.css";
import "../calendar/calendar.css";

// register @duet/datepicker
defineCustomElements(globalThis);

const noop = () => {};

// there has to be made some considerations for window.history handling (navigating backwards)
// use case:
// - web page renders
// - `<duet-date-picker>` component hydrates
// - `<duet-date-picker>` shows date xx.xx.xxxx
// - user selects date yy.yy.yyyy
//   - `<form>` is submitted
//   - response received
//   - current page snapshot is created (cache for client side history navigation)
//   - history.pushState() is invoked
//   - new page will be rendered
// - user navigates back with history.back()
//
// client side routing (with `hotwire/turbo` in our case) results in:
// - rendering the cached page.
//   this cached page already includes the html of the hydrated `duet-date-picker` instead of the server side rendered `input[date]` element.
//   - `<duet-date-picker>` hydrates again. because the lib is implemented like this.
//   - `<duet-date-picker>` contains two `input[date]` elements...
//
// to workaround the "duplicated hydration", we're going to remove all children of `<duet-date-picker>` :shrug:
//

// fallback values when the URL does not provide one on history.popstate.
const initialValues = new Map();
// date-picker configuration stuff required for rehydration on history.popstate
const optionsByName = new Map();

onTurboBeforeRenderRestore(function (event) {
  const { newBody } = event.detail;

  for (const duetDatePicker of newBody.querySelectorAll("duet-date-picker")) {
    // remove all children which are rendered again by <duet-date-picker> stencil implementation.
    // otherwise there would be two `input[date]`.
    while (duetDatePicker.firstElementChild) {
      duetDatePicker.firstElementChild.remove();
    }

    // after history.popstate we have to update the value to match the URL or initial one.
    // the cached turbo snapshot does not contain the "previous" value but the already changed one.
    // (form submit and rendering happens on submit-click AFTER selecting a date. snapshot is created on form submit.)
    const name = duetDatePicker.getAttribute("name");
    const value = new URLSearchParams(globalThis.location.search).get(name) ?? initialValues.get(name) ?? "";
    duetDatePicker.setAttribute("value", value);

    waitForDatePickerHydration(duetDatePicker).then(() => {
      hydrateDatepicker(duetDatePicker, optionsByName.get(name));
    });
  }
});

export async function createDatepicker(selector, options) {
  const { localisation } = globalThis.uv.datepicker;
  const { dateAdapter, dateFormatShort } = createDatepickerLocalization({ locale: localisation.locale });

  const duetDateElement = await replaceNativeDateInputWithDuetDatePicker(selector, dateAdapter, localisation);

  const optionsWithDuetConfig = {
    ...options,
    dateFormatShort,
    dateAdapter,
    localisation,
  };

  // cache configuration for possible history.popstate handling
  const name = duetDateElement.getAttribute("name");
  optionsByName.set(name, optionsWithDuetConfig);

  hydrateDatepicker(duetDateElement, optionsWithDuetConfig);

  return duetDateElement;
}

function hydrateDatepicker(duetDateElement, options) {
  const { urlPrefix, getPersonId, onSelect = noop, dateFormatShort, dateAdapter, localisation } = options;

  duetDateElement.dateAdapter = dateAdapter;
  duetDateElement.localization = localisation;

  mutation(duetDateElement.querySelector(".duet-date__input"))
    .attributeChanged(["readonly"])
    .subscribe(function (event) {
      if (event.target.hasAttribute("readonly")) {
        duetDateElement.setAttribute("readonly", "");
      } else {
        duetDateElement.removeAttribute("readonly");
      }
    });

  const monthElement = duetDateElement.querySelector(".duet-date__select--month");
  const yearElement = duetDateElement.querySelector(".duet-date__select--year");

  const showAbsences = () => {
    // clear all days
    for (const element of duetDateElement.querySelectorAll(".duet-date__day")) {
      element.querySelector("[data-uv-icon]")?.remove();
      removeDatepickerCssClassesFromNode(element);
      removeAbsenceTypeStyleFromNode(element);
    }

    const firstDayOfMonth = `${yearElement.value}-${twoDigit(Number(monthElement.value) + 1)}-01`;
    const lastDayOfMonth = formatISO(endOfMonth(parseISO(firstDayOfMonth)), { representation: "date" });

    const personId = getPersonId();
    if (!personId) {
      return;
    }

    Promise.allSettled([
      getJSON(`${urlPrefix}/persons/${personId}/public-holidays?from=${firstDayOfMonth}&to=${lastDayOfMonth}`).then(
        pick("publicHolidays"),
      ),
      getJSON(
        `${urlPrefix}/persons/${personId}/absences?from=${firstDayOfMonth}&to=${lastDayOfMonth}&absence-types=vacation,sick_note,no_workday`,
      ).then(pick("absences")),
    ]).then(([publicHolidays, absences]) => {
      const selectedMonth = Number(monthElement.value);
      const selectedYear = Number(yearElement.value);
      for (let dayElement of duetDateElement.querySelectorAll(".duet-date__day")) {
        const dayAndMonthString = dayElement.querySelector(".duet-date__vhidden").textContent;
        const date = parse(dayAndMonthString, dateFormatShort, new Date());
        // dayAndMonthString is a hard coded duet-date-picker screen-reader-only value which does not contain the year.
        // therefore the parsed date will always be assigned to the current year and we have to adjust it when:
        if (selectedMonth === 0 && date.getMonth() === 11) {
          // datepicker selected month is january, but the rendered day item is december of the previous year
          // (e.g. december 31) to fill the week row.
          date.setFullYear(selectedYear - 1);
        } else if (selectedMonth === 11 && date.getMonth() === 0) {
          // datepicker selected month is december, but the rendered day item is january of the next year
          // (e.g. january 1) to fill the week row.
          date.setFullYear(selectedYear + 1);
        } else {
          date.setFullYear(selectedYear);
        }

        const absencesForDate = findByDate(absences.value, date);
        const publicHolidaysForDate = findByDate(publicHolidays.value, date);
        addDatepickerCssClassesToNode(dayElement, date, absencesForDate, publicHolidaysForDate);
        addAbsenceTypeStyleToNode(dayElement, absencesForDate);

        let icon;

        if (isNoWorkday(absencesForDate)) {
          const temporary = document.createElement("span");
          temporary.innerHTML = `<svg viewBox="0 0 20 20" class="tw-w-3 tw-h-3 tw-opacity-50 tw-stroke-2" fill="currentColor" width="16" height="16" role="img" aria-hidden="true" focusable="false"><path fill-rule="evenodd" d="M13.477 14.89A6 6 0 015.11 6.524l8.367 8.368zm1.414-1.414L6.524 5.11a6 6 0 018.367 8.367zM18 10a8 8 0 11-16 0 8 8 0 0116 0z" clip-rule="evenodd"></path></svg>`;
          icon = temporary.firstChild;
        } else {
          icon = document.createElement("span");
          icon.classList.add("tw-w-3", "tw-h-3", "tw-inline-block");
        }

        icon.dataset.uvIcon = "";
        dayElement.append(icon);
      }
    });
  };

  const toggleButton = duetDateElement.querySelector("button.duet-date__toggle");
  toggleButton.addEventListener("click", showAbsences);
  duetDateElement.addEventListener("duetChange", (event) => onSelect(event));

  duetDateElement.querySelector(".duet-date__prev").addEventListener("click", showAbsences);
  duetDateElement.querySelector(".duet-date__next").addEventListener("click", showAbsences);

  monthElement.addEventListener("change", showAbsences);
  yearElement.addEventListener("change", showAbsences);
}

async function replaceNativeDateInputWithDuetDatePicker(selector, dateAdapter, localisation) {
  const dateElement = document.querySelector(selector);
  const duetDateElement = document.createElement("duet-date-picker");

  duetDateElement.dateAdapter = dateAdapter;
  duetDateElement.localization = localisation;

  const name = dateElement.getAttribute("name");
  const originalStyle = dateElement.getAttribute("style") ?? "";
  const value = dateElement.dataset.isoValue || "";

  duetDateElement.setAttribute("style", "--duet-radius=0;" + originalStyle);
  duetDateElement.setAttribute("class", dateElement.getAttribute("class"));
  duetDateElement.setAttribute("identifier", dateElement.getAttribute("id"));
  duetDateElement.setAttribute("min", dateElement.getAttribute("min"));
  duetDateElement.setAttribute("max", dateElement.getAttribute("max"));
  duetDateElement.setAttribute("name", name);
  duetDateElement.setAttribute("value", value);

  for (const attributeName of dateElement.getAttributeNames()) {
    if (attributeName.startsWith("data-") && attributeName !== "data-test-id") {
      duetDateElement.setAttribute(attributeName, dateElement.getAttribute(attributeName));
    }
  }

  // cache initial value as fallback for history.popstate handling.
  // first it is looked at the URL. If there is no value set for `name` then this initial value is used.
  initialValues.set(name, value);

  if (dateElement.getAttribute("readonly")) {
    duetDateElement.setAttribute("readonly", "");
  }

  if (dateElement.dataset.min) {
    duetDateElement.setAttribute("min", dateElement.dataset.min);
  }

  if (dateElement.dataset.max) {
    duetDateElement.setAttribute("max", dateElement.dataset.max);
  }

  dateElement.replaceWith(duetDateElement);

  await waitForDatePickerHydration(duetDateElement);

  const duetDateInputElement = duetDateElement.querySelector("input.duet-date__input");
  for (const [key, value] of Object.entries(dateElement.dataset)) {
    duetDateInputElement.dataset[key] = value;
  }

  return duetDateElement;
}

function waitForDatePickerHydration(rootElement) {
  return new Promise((resolve) => {
    const observer = new MutationObserver((mutationsList) => {
      for (const mutation of mutationsList) {
        if (mutation.target.classList.contains("hydrated")) {
          resolve();
          observer.disconnect();
          return true;
        }
      }
    });
    observer.observe(rootElement, {
      attributes: true,
      attributeFilter: ["class"],
    });
  });
}

function dateToString(date) {
  return date ? formatISO(date, { representation: "date" }) : "";
}

function findByDate(list, date) {
  const dateString = dateToString(date);
  return list.filter((item) => item.date === dateString);
}

function pick(name) {
  return function (object) {
    return object[name];
  };
}

function twoDigit(nr) {
  return ("0" + nr).slice(-2);
}
