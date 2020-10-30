import { findWhere } from "underscore";
import { endOfMonth, formatISO, isToday, isWeekend, parse, parseISO } from "date-fns";
import { defineCustomElements } from "@duetds/date-picker/dist/loader";
import { getJSON } from "../../js/fetch";
import DE from "./locale/de";
import "@duetds/date-picker/dist/collection/themes/default.css";
import "./datepicker.css";
import "../calendar/calendar.css";

// register @duet/datepicker
defineCustomElements(window);

export default async function createDatepickerInstances(selectors, urlPrefix, getPerson, onSelect) {
  return Promise.allSettled(selectors.map((selector) => instantiate({ selector, urlPrefix, getPerson, onSelect })));
}

async function instantiate({ selector, urlPrefix, getPerson, onSelect }) {
  const dateFormat = DE.dateFormat;

  const dateElement = document.querySelector(selector);
  const duetDateElement = document.createElement("duet-date-picker");

  const { isoValue } = dateElement.dataset;

  duetDateElement.dateAdapter = DE.dateAdapter;
  duetDateElement.localization = window.uv.datepicker.localisation;

  const parsedDate = parse(isoValue, "yyyy-MM-dd", new Date());
  const isoDateString = dateElement.value ? formatISO(parsedDate, { representation: "date" }) : "";

  duetDateElement.setAttribute("style", "--duet-radius=0");
  duetDateElement.setAttribute("id", dateElement.getAttribute("id"));
  duetDateElement.setAttribute("class", dateElement.getAttribute("class"));
  duetDateElement.setAttribute("name", dateElement.getAttribute("name"));
  duetDateElement.setAttribute("value", isoDateString);
  dateElement.replaceWith(duetDateElement);

  await waitForDatePickerHydration(duetDateElement);

  const monthElement = duetDateElement.querySelector(".duet-date__select--month");
  const yearElement = duetDateElement.querySelector(".duet-date__select--year");

  const showAbsences = () => {
    // clear all days
    [...duetDateElement.querySelectorAll(".duet-date__day")].forEach((element) =>
      element.setAttribute("class", "duet-date__day"),
    );

    const firstDayOfMonth = `${yearElement.value}-${twoDigit(Number(monthElement.value) + 1)}-01`;
    const lastDayOfMonth = formatISO(endOfMonth(parseISO(firstDayOfMonth)), { representation: "date" });

    const personId = getPerson();
    if (!personId) {
      return;
    }

    Promise.allSettled([
      getJSON(`${urlPrefix}/persons/${personId}/public-holidays?from=${firstDayOfMonth}&to=${lastDayOfMonth}`).then(
        pick("publicHolidays"),
      ),
      getJSON(`${urlPrefix}/persons/${personId}/absences?from=${firstDayOfMonth}&to=${lastDayOfMonth}`).then(
        pick("absences"),
      ),
    ]).then(([publicHolidays, absences]) => {
      for (let dayElement of [...duetDateElement.querySelectorAll(".duet-date__day")]) {
        const date = dayElement.querySelector(".duet-date__vhidden").textContent;
        const cssClasses = getCssClassesForDate(
          parse(date, dateFormat, new Date()),
          publicHolidays.value,
          absences.value,
        );
        dayElement.classList.add(...cssClasses);
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

function getCssClassesForDate(date, publicHolidays, absences) {
  if (date && isWeekend(date)) {
    return ["datepicker-day", "datepicker-day-weekend"];
  } else {
    const dateString = date ? formatISO(date, { representation: "date" }) : "";
    const fitsCriteria = (list, filterAttributes) =>
      Boolean(findWhere(list, { ...filterAttributes, date: dateString }));

    const isPast = () => false;
    const isPublicHolidayFull = () => fitsCriteria(publicHolidays, { absencePeriodName: "FULL" });
    const isPublicHolidayMorning = () => fitsCriteria(publicHolidays, { absencePeriodName: "MORNING" });
    const isPublicHolidayNoon = () => fitsCriteria(publicHolidays, { absencePeriodName: "NOON" });
    const isPersonalHolidayFull = () =>
      fitsCriteria(absences, {
        type: "VACATION",
        absencePeriodName: "FULL",
        status: "WAITING",
      });
    const isPersonalHolidayFullApproved = () =>
      fitsCriteria(absences, {
        type: "VACATION",
        absencePeriodName: "FULL",
        status: "ALLOWED",
      });
    const isPersonalHolidayMorning = () =>
      fitsCriteria(absences, {
        type: "VACATION",
        absencePeriodName: "MORNING",
        status: "WAITING",
      });
    const isPersonalHolidayMorningApproved = () =>
      fitsCriteria(absences, {
        type: "VACATION",
        absencePeriodName: "MORNING",
        status: "ALLOWED",
      });
    const isPersonalHolidayNoon = () =>
      fitsCriteria(absences, {
        type: "VACATION",
        absencePeriodName: "NOON",
        status: "WAITING",
      });
    const isPersonalHolidayNoonApproved = () =>
      fitsCriteria(absences, {
        type: "VACATION",
        absencePeriodName: "NOON",
        status: "ALLOWED",
      });
    const isSickDayFull = () => fitsCriteria(absences, { type: "SICK_NOTE", absencePeriodName: "FULL" });
    const isSickDayMorning = () =>
      fitsCriteria(absences, {
        type: "SICK_NOTE",
        absencePeriodName: "MORNING",
      });
    const isSickDayNoon = () => fitsCriteria(absences, { type: "SICK_NOTE", absencePeriodName: "NOON" });

    return [
      "datepicker-day",
      isToday(date) && "datepicker-day-today",
      isPast() && "datepicker-day-past",
      isPublicHolidayFull() && "datepicker-day-public-holiday-full",
      isPublicHolidayMorning() && "datepicker-day-public-holiday-morning",
      isPublicHolidayNoon() && "datepicker-day-public-holiday-noon",
      isPersonalHolidayFull() && "datepicker-day-personal-holiday-full",
      isPersonalHolidayFullApproved() && "datepicker-day-personal-holiday-full-approved",
      isPersonalHolidayMorning() && "datepicker-day-personal-holiday-morning",
      isPersonalHolidayMorningApproved() && "datepicker-day-personal-holiday-morning-approved",
      isPersonalHolidayNoon() && "datepicker-day-personal-holiday-noon",
      isPersonalHolidayNoonApproved() && "datepicker-day-personal-holiday-noon-approved",
      isSickDayFull() && "datepicker-day-sick-note-full",
      isSickDayMorning() && "datepicker-day-sick-note-morning",
      isSickDayNoon() && "datepicker-day-sick-note-noon",
    ].filter(Boolean);
  }
}

function pick(name) {
  return function (object) {
    return object[name];
  };
}

function twoDigit(nr) {
  return ("0" + nr).slice(-2);
}
