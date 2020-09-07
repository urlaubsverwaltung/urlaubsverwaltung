import $ from 'jquery';
import { getYear, setYear, startOfYear, subMonths, addMonths } from 'date-fns';

import getUrlParameter from '../get-url-parameter';
import '../../components/calendar';

$(document).ready(function () {

  var personId = window.uv.personId;
  var webPrefix = window.uv.webPrefix;
  var apiPrefix = window.uv.apiPrefix;

  function initCalendar() {

    var year = getUrlParameter("year");
    var date = new Date();

    if (year.length > 0 && year != getYear(date)) {
      date = startOfYear(setYear(date, year));
    }

    var holidayService = Urlaubsverwaltung.HolidayService.create(webPrefix, apiPrefix, +personId);

    var shownNumberOfMonths = 10;
    var startDate = subMonths(date, shownNumberOfMonths / 2);
    var endDate = addMonths(date, shownNumberOfMonths / 2);

    var yearOfStartDate = getYear(startDate);
    var yearOfEndDate = getYear(endDate);

    $.when(
      holidayService.fetchPublic(yearOfStartDate),
      holidayService.fetchPersonal(yearOfStartDate),
      holidayService.fetchSickDays(yearOfStartDate),

      holidayService.fetchPublic(yearOfEndDate),
      holidayService.fetchPersonal(yearOfEndDate),
      holidayService.fetchSickDays(yearOfEndDate)
    ).always(function () {
      Urlaubsverwaltung.Calendar.init(holidayService, date);
    });
  }

  initCalendar();

  var resizeTimer;

  $(window).on('resize', function () {

    if (resizeTimer) {
      clearTimeout(resizeTimer);
    }

    resizeTimer = setTimeout(function () {
      Urlaubsverwaltung.Calendar.reRender();
      resizeTimer = false;
    }, 30)

  });

});
