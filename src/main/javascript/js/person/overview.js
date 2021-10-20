import $ from "jquery";
import { getYear, setYear, startOfYear, subMonths, addMonths } from "date-fns";

import getUrlParameter from "../get-url-parameter";
import "../../components/calendar";

document.addEventListener("DOMContentLoaded", () => {
  const personId = window.uv.personId;
  const webPrefix = window.uv.webPrefix;
  const apiPrefix = window.uv.apiPrefix;

  function initCalendar() {
    const year = getUrlParameter("year");
    let date = new Date();

    if (year.length > 0 && year != getYear(date)) {
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
      holidayService.fetchPersonal(yearOfStartDate),
      holidayService.fetchSickDays(yearOfStartDate),

      holidayService.fetchPublic(yearOfEndDate),
      holidayService.fetchPersonal(yearOfEndDate),
      holidayService.fetchSickDays(yearOfEndDate),
    ).always(function () {
      Urlaubsverwaltung.Calendar.init(holidayService, date);
    });
  }

  initCalendar();

  let resizeTimer;

  $(window).on("resize", function () {
    if (resizeTimer) {
      clearTimeout(resizeTimer);
    }

    resizeTimer = setTimeout(function () {
      Urlaubsverwaltung.Calendar.reRender();
      resizeTimer = false;
    }, 30);
  });
});
