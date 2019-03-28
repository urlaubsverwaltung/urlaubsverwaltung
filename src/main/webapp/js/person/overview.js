import $ from 'jquery';
import { getYear, setYear, firstOfYear, subMonths, addMonths } from 'date-fns';

import getUrlParam from '../getUrlParam';
import '../../components/calendar';

$(document).ready(function () {

  var personId = window.uv.personId;
  var webPrefix = window.uv.webPrefix;
  var apiPrefix = window.uv.apiPrefix;

  function initCalendar() {

    var year = getUrlParam("year");
    var date = new Date();

    if (year.length > 0 && year != getYear(date)) {
      date = firstOfYear(setYear(date, year));
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

  var resizeTimer = null;

  $(window).on('resize', function () {

    if (resizeTimer !== null) {
      clearTimeout(resizeTimer);
    }

    resizeTimer = setTimeout(function () {
      Urlaubsverwaltung.Calendar.reRender();
      resizeTimer = null;
    }, 30)

  });

});
