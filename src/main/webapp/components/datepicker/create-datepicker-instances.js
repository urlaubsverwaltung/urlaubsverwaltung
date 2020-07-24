import $ from 'jquery';
import { findWhere } from 'underscore';
import datepicker from './datepicker';
import { isWeekend, isToday } from 'date-fns';

import '../calendar/calendar.css';

export default async function createDatepickerInstances(selectors, regional, urlPrefix, getPerson, onSelect) {

  let highlighted;
  let highlightedAbsences;

  const selector = selectors.join(",");

  if (regional === 'de') {
    const { default: de } = await import(/* webpackChunkName: "jquery-ui-datepicker-de" */'jquery-ui/ui/i18n/datepicker-de');
    datepicker.setDefaults({
      ...de,
      weekHeader: 'Wo'
    });
  }
  else {
    const { default: en } = await import(/* webpackChunkName: "jquery-ui-datepicker-en" */'jquery-ui/ui/i18n/datepicker-en-GB');
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

      if(!personId) {
        return;
      }

      getHighlighted(urlPrefix + "/holidays?year=" + year + "&month=" + month+ "&person=" + personId, function (data) {
        highlighted = getPublicHolidays(data);
      });

      getHighlighted(urlPrefix + "/absences?year=" + year + "&month=" + month + "&person=" + personId, function (data) {
        highlightedAbsences = getAbsences(data);
      });

    },
    onChangeMonthYear: function (year, month) {

      const personId = getPerson();

      if(!personId) {
        return;
      }

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

  for (let i = 0; i < data.absences.length; i++) {
    const value = data.absences[i];
    if ($.inArray(value, absences) == -1) {
      absences.push(value);
    }
  }

  return absences;
}

function getPublicHolidays(data) {

  const publicHolidayDates = [];

  for (let i = 0; i < data.publicHolidays.length; i++) {
    const value = data.publicHolidays[i];
    publicHolidayDates.push(value);
  }

  return publicHolidayDates;
}

function colorizeDate(date, publicHolidays, absences) {

  if (isWeekend(date)) {
    return [true, "datepicker-day datepicker-day-weekend"];
  } else {

    const dateString = $.datepicker.formatDate("yy-mm-dd", date);

    const fitsCriteria = (list, filterAttributes) => Boolean(findWhere(list, { ...filterAttributes, date: dateString }));

    const isPast = () => false;
    const isPublicHolidayFull = () => fitsCriteria(publicHolidays, { absencePeriodName: 'FULL'});
    const isPublicHolidayMorning = () => fitsCriteria(publicHolidays, { absencePeriodName: 'MORNING'});
    const isPublicHolidayNoon = () => fitsCriteria(publicHolidays, { absencePeriodName: 'NOON'});
    const isPersonalHolidayFull = () => fitsCriteria(absences, { type: 'VACATION', absencePeriodName: 'FULL', status: 'WAITING'});
    const isPersonalHolidayFullApproved = () => fitsCriteria(absences, { type: 'VACATION', absencePeriodName: 'FULL', status: 'ALLOWED'});
    const isPersonalHolidayMorning = () => fitsCriteria(absences, { type: 'VACATION', absencePeriodName: 'MORNING', status: 'WAITING'});
    const isPersonalHolidayMorningApproved = () => fitsCriteria(absences, { type: 'VACATION', absencePeriodName: 'MORNING', status: 'ALLOWED'});
    const isPersonalHolidayNoon = () => fitsCriteria(absences, { type: 'VACATION', absencePeriodName: 'NOON', status: 'WAITING'});
    const isPersonalHolidayNoonApproved = () => fitsCriteria(absences, { type: 'VACATION', absencePeriodName: 'NOON', status: 'ALLOWED'});
    const isSickDayFull = () => fitsCriteria(absences, { type: 'SICK_NOTE', absencePeriodName: 'FULL'});
    const isSickDayMorning = () => fitsCriteria(absences, { type: 'SICK_NOTE', absencePeriodName: 'MORNING'});
    const isSickDayNoon = () => fitsCriteria(absences, { type: 'SICK_NOTE', absencePeriodName: 'NOON'});

    const cssClasses = [
      'datepicker-day',
      isToday(date) && 'datepicker-day-today',
      isPast() && 'datepicker-day-past',
      isPublicHolidayFull() && 'datepicker-day-public-holiday-full',
      isPublicHolidayMorning() && 'datepicker-day-public-holiday-morning',
      isPublicHolidayNoon() && 'datepicker-day-public-holiday-noon',
      isPersonalHolidayFull() && 'datepicker-day-personal-holiday-full',
      isPersonalHolidayFullApproved() && 'datepicker-day-personal-holiday-full-approved',
      isPersonalHolidayMorning() && 'datepicker-day-personal-holiday-morning',
      isPersonalHolidayMorningApproved() && 'datepicker-day-personal-holiday-morning-approved',
      isPersonalHolidayNoon() && 'datepicker-day-personal-holiday-noon',
      isPersonalHolidayNoonApproved() && 'datepicker-day-personal-holiday-noon-approved',
      isSickDayFull() && 'datepicker-day-sick-note-full',
      isSickDayMorning() && 'datepicker-day-sick-note-morning',
      isSickDayNoon() && 'datepicker-day-sick-note-noon',
    ].filter(Boolean);

    return [true, cssClasses.join(" ").trim()];
  }
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
