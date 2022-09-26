import { endOfMonth, formatISO, parseISO } from "date-fns";
import parse from "../../lib/date-fns/parse";
import { defineCustomElements } from "@duetds/date-picker/dist/loader";
import { getJSON } from "../../js/fetch";
import { mutation } from "./mutation";
import { createDatepickerLocalization } from "./locale";
import { addDatepickerCssClassesToNode, removeDatepickerCssClassesFromNode } from "./datepicker-css-classes";
import { addAbsenceTypeStyleToNode, isNoWorkday, removeAbsenceTypeStyleFromNode } from "../../js/absence";
import "@duetds/date-picker/dist/collection/themes/default.css";
import "./datepicker.css";
import "../calendar/calendar.css";

// register @duet/datepicker
defineCustomElements(window);

const noop = () => {};

export async function createDatepicker(selector, { urlPrefix, getPersonId, onSelect = noop }) {
  const { localisation } = window.uv.datepicker;
  const { dateAdapter, dateFormatShort } = createDatepickerLocalization({ locale: localisation.locale });

  const duetDateElement = await replaceNativeDateInputWithDuetDatePicker(selector, dateAdapter, localisation);

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
        `${urlPrefix}/persons/${personId}/absences?from=${firstDayOfMonth}&to=${lastDayOfMonth}&noWorkdaysInclusive=true`,
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

  return duetDateElement;
}

async function replaceNativeDateInputWithDuetDatePicker(selector, dateAdapter, localization) {
  const dateElement = document.querySelector(selector);
  const duetDateElement = document.createElement("duet-date-picker");

  duetDateElement.dateAdapter = dateAdapter;
  duetDateElement.localization = localization;

  duetDateElement.setAttribute("style", "--duet-radius=0");
  duetDateElement.setAttribute("class", dateElement.getAttribute("class"));
  duetDateElement.setAttribute("value", dateElement.dataset.isoValue || "");
  duetDateElement.setAttribute("identifier", dateElement.getAttribute("id"));

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

  // name attribute must be set to the actual visible input element
  // the backend handles the raw user input for progressive enhancement reasons.
  // (german locale is 'dd.MM.yyyy', while english locale would be 'yyyy/MM/dd' for instance)
  const duetDateInputElement = duetDateElement.querySelector("input.duet-date__input");
  duetDateInputElement.setAttribute("name", dateElement.getAttribute("name"));

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
    observer.observe(rootElement, { attributes: true });
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
