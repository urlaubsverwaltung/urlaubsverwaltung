import $ from "jquery";
import { getYear, setYear, startOfYear, subMonths, addMonths } from "date-fns";
import "../../components/calendar";

$(document).ready(function () {
  const personId = globalThis.uv.personId;
  const webPrefix = globalThis.uv.webPrefix;
  const apiPrefix = globalThis.uv.apiPrefix;

  function i18n(messageKey) {
    return globalThis.uv.i18n[messageKey] || `/i18n:${messageKey}/`;
  }

  function getUrlParameter(name) {
    return new URL(globalThis.location).searchParams.get(name);
  }

  function initCalendar() {
    const year = getUrlParameter("year");
    let date = new Date();

    if (year && year != getYear(date)) {
      date = startOfYear(setYear(date, year));
    }

    const holidayService = Urlaubsverwaltung.HolidayService.create(webPrefix, apiPrefix, +personId);

    const shownNumberOfMonths = 10;
    const startDate = subMonths(date, shownNumberOfMonths / 2);
    const endDate = addMonths(date, shownNumberOfMonths / 2);

    const yearOfStartDate = getYear(startDate);
    const yearOfEndDate = getYear(endDate);

    // TODO Performance reduce calls when yearOfStartDate === yearOfEndDate
    $.when(
      holidayService.fetchPublic(yearOfStartDate),
      holidayService.fetchAbsences(yearOfStartDate),

      holidayService.fetchPublic(yearOfEndDate),
      holidayService.fetchAbsences(yearOfEndDate),
    ).always(function () {
      const calendarParentElement = document.querySelector("#datepicker");
      Urlaubsverwaltung.Calendar.init(calendarParentElement, holidayService, date, i18n);
    });
  }

  initCalendar();
});
