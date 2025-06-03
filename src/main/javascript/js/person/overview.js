import $ from "jquery";
import { addMonths, getYear, setYear, startOfYear, subMonths } from "date-fns";
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
    const yearParameter = getUrlParameter("year");
    let date = new Date();

    if (yearParameter && Number(yearParameter) !== getYear(date)) {
      date = startOfYear(setYear(date, Number(yearParameter)));
    }

    const holidayService = Urlaubsverwaltung.HolidayService.create(webPrefix, apiPrefix, +personId);

    const shownNumberOfMonths = 10;
    const startDate = subMonths(date, shownNumberOfMonths / 2);
    const endDate = addMonths(date, shownNumberOfMonths / 2);

    const yearOfStartDate = getYear(startDate);
    const fetchPromises = [
      holidayService.fetchPublicHolidays(yearOfStartDate),
      holidayService.fetchAbsences(yearOfStartDate),
    ];

    const yearOfEndDate = getYear(endDate);
    if (yearOfStartDate !== yearOfEndDate) {
      fetchPromises.push(
        holidayService.fetchPublicHolidays(yearOfEndDate),
        holidayService.fetchAbsences(yearOfEndDate),
      );
    }

    $.when(...fetchPromises).always(function () {
      const calendarParentElement = document.querySelector("#datepicker");
      Urlaubsverwaltung.Calendar.init(calendarParentElement, holidayService, date, i18n);
    });
  }

  initCalendar();
});
