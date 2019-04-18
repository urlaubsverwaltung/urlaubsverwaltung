import $ from 'jquery';
import { findWhere } from 'underscore';
import datepicker from './datepicker';
import { isWeekend } from 'date-fns';

export default async function createDatepickerInstances(selectors, regional, urlPrefix, getPerson, onSelect) {

  let highlighted;
  let highlightedAbsences;

  const selector = selectors.join(",");

  if (regional === 'de') {
    const de = await import(/* webpackChunkName: "jquery-ui-datepicker-de" */'jquery-ui/ui/i18n/datepicker-de');
    datepicker.setDefaults({
      ...de,
      weekHeader: 'Wo'
    });
  }
  else {
    const en = await import(/* webpackChunkName: "jquery-ui-datepicker-en" */'jquery-ui/ui/i18n/datepicker-en-GB');
    datepicker.setDefaults({
      ...en,
      dateFormat: 'dd.mm.yy'
    });
  }

  $(selector).datepicker({
    numberOfMonths: 1,
    showOtherMonths: true,
    selectOtherMonths: false,
    beforeShow: function (input, inst) {

      const calendrier = inst.dpDiv;
      const top = $(this).offset().top + $(this).outerHeight();
      const left = $(this).offset().left;
      setTimeout(function () {
        calendrier.css({'top': top, 'left': left});
      }, 10);

      let date;

      if ($(input).datepicker("getDate") == null) {
        date = new Date();
      } else {
        date = $(input).datepicker("getDate");
      }

      const year = date.getFullYear();
      const month = date.getMonth() + 1;

      const personId = getPerson();

      getHighlighted(urlPrefix + "/holidays?year=" + year + "&month=" + month+ "&person=" + personId, function (data) {
        highlighted = getPublicHolidays(data);
      });

      getHighlighted(urlPrefix + "/absences?year=" + year + "&month=" + month + "&person=" + personId, function (data) {
        highlightedAbsences = getAbsences(data);
      });

    },
    onChangeMonthYear: function (year, month) {

      const personId = getPerson();

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

  const absences = [];

  for (let i = 0; i < data.response.absences.length; i++) {
    const value = data.response.absences[i];
    if ($.inArray(value, absences) == -1) {
      absences.push(value);
    }
  }

  return absences;
}

function getPublicHolidays(data) {

  const publicHolidayDates = [];

  for (let i = 0; i < data.response.publicHolidays.length; i++) {
    const value = data.response.publicHolidays[i];
    publicHolidayDates.push(value);
  }

  return publicHolidayDates;
}

function colorizeDate(date, publicHolidays, absences) {

  if (isWeekend(date)) {
    return [true, "notworkday"];
  } else {

    const dateString = $.datepicker.formatDate("yy-mm-dd", date);

    const isPublicHoliday = isSpecialDay(dateString, publicHolidays);

    let absenceType;
    if (isSpecialDay(dateString, absences)) {
      absenceType = getAbsenceType(dateString, absences);
    }

    const isSickDay = absenceType === "SICK_NOTE";
    const isPersonalHoliday = absenceType === "VACATION";

    const isHalfWorkDayMorning = isHalfWorkday(dateString, publicHolidays, 'MORNING') || isHalfWorkday(dateString, absences, 'MORNING');
    const isHalfWorkDayNoon = isHalfWorkday(dateString, publicHolidays, 'NOON') || isHalfWorkday(dateString, absences, 'NOON');

    const cssClasses = [];

    if (isPublicHoliday) {
      cssClasses.push("notworkday");
    }

    if (isHalfWorkDayMorning) {
      cssClasses.push("halfworkdaymorning");
    }

    if (isHalfWorkDayNoon) {
      cssClasses.push("halfworkdaynoon");
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
  const day = findWhere(specialDays, {date: formattedDate});
  return day !== undefined && day.dayLength <= 1;
}

function getAbsenceType(formattedDate, absences) {
  const absence = findWhere(absences, {date: formattedDate});
  return absence.type;
}

function isHalfWorkday(formattedDate, holidays, absencePeriodName) {
  return findWhere(holidays, {date: formattedDate, absencePeriodName}) !== undefined;
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
