import $ from 'jquery';
import { findWhere } from 'underscore';
import datepicker from './datepicker';
import { isWeekend } from 'date-fns';

export default async function createDatepickerInstances(selectors, regional, urlPrefix, getPerson, onSelect) {

  var highlighted;
  var highlightedAbsences;

  var selector = selectors.join(",");

  switch(regional) {
    case 'de': {
      const de = await import(/* webpackChunkName: "jquery-ui-datepicker-de" */'jquery-ui/ui/i18n/datepicker-de');
      datepicker.setDefaults({
        ...de,
        weekHeader: 'Wo'
      });
      break;
    }

    default: {
      const en = await import(/* webpackChunkName: "jquery-ui-datepicker-en" */'jquery-ui/ui/i18n/datepicker-en-GB');
      datepicker.setDefaults({
        ...en,
        dateFormat: 'dd.mm.yy'
      });
    }
  }

  $(selector).datepicker({
    numberOfMonths: 1,
    showOtherMonths: true,
    selectOtherMonths: false,
    beforeShow: function (input, inst) {

      var calendrier = inst.dpDiv;
      var top = $(this).offset().top + $(this).outerHeight();
      var left = $(this).offset().left;
      setTimeout(function () {
        calendrier.css({'top': top, 'left': left});
      }, 10);

      var date;

      if ($(input).datepicker("getDate") == null) {
        date = new Date();
      } else {
        date = $(input).datepicker("getDate");
      }

      var year = date.getFullYear();
      var month = date.getMonth() + 1;

      var personId = getPerson();

      getHighlighted(urlPrefix + "/holidays?year=" + year + "&month=" + month+ "&person=" + personId, function (data) {
        highlighted = getPublicHolidays(data);
      });

      getHighlighted(urlPrefix + "/absences?year=" + year + "&month=" + month + "&person=" + personId, function (data) {
        highlightedAbsences = getAbsences(data);
      });

    },
    onChangeMonthYear: function (year, month) {

      var personId = getPerson();

      getHighlighted(urlPrefix + "/holidays?year=" + year + "&month=" + month+ "&person=" + personId, function (data) {
        highlighted = getPublicHolidays(data);
      });


      getHighlighted(urlPrefix + "/absences?year=" + year + "&month=" + month + "&person=" + personId, function (data) {
        highlightedAbsences = getAbsences(data);
      });

    },
    beforeShowDay: function (date) {

      return colorizeDate(date, highlighted, highlightedAbsences);

    },
    onSelect: onSelect
  });
}

function getAbsences(data) {

  var absences = [];

  for (var i = 0; i < data.response.absences.length; i++) {
    var value = data.response.absences[i];
    if ($.inArray(value, absences) == -1) {
      absences.push(value);
    }
  }

  return absences;

}

function getPublicHolidays(data) {

  var publicHolidayDates = [];

  for (var i = 0; i < data.response.publicHolidays.length; i++) {
    var value = data.response.publicHolidays[i];
    publicHolidayDates.push(value);
  }

  return publicHolidayDates;

}

function colorizeDate(date, publicHolidays, absences) {

  if (isWeekend(date)) {
    return [true, "notworkday"];
  } else {

    var dateString = $.datepicker.formatDate("yy-mm-dd", date);

    var isPublicHoliday = isSpecialDay(dateString, publicHolidays);

    var absenceType;
    if (isSpecialDay(dateString, absences)) {
      absenceType = getAbsenceType(dateString, absences);
    }

    var isSickDay = absenceType === "SICK_NOTE";
    var isPersonalHoliday = absenceType === "VACATION";

    var isHalfWorkDay = isHalfWorkday(dateString, publicHolidays) || isHalfWorkday(dateString, absences);

    var cssClasses = [];

    if (isPublicHoliday) {
      cssClasses.push("notworkday");
    }

    if (isHalfWorkDay) {
      cssClasses.push("halfworkday");
    }

    if (isSickDay) {
      cssClasses.push("sickday");
    }

    if (isPersonalHoliday) {
      cssClasses.push("holiday");
    }

    return [true, cssClasses.join(" ")];

  }

}

function isSpecialDay(formattedDate, specialDays) {

  var day = findWhere(specialDays, {date: formattedDate});

  return day !== undefined && day.dayLength <= 1;

}

function getAbsenceType(formattedDate, absences) {

  var absence = findWhere(absences, {date: formattedDate});

  return absence.type;
}

function isHalfWorkday(formattedDate, holidays) {

  return findWhere(holidays, {date: formattedDate, dayLength: 0.5}) !== undefined;

}

function getHighlighted(url, callback) {

  $.ajax({
    url: url,
    async: false,
    dataType: "json",
    type: "GET",
    success: function (data) {
      callback(data);

    }
  });

}
