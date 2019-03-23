$.datepicker.regional['en'] = {
  closeText: 'Done',
  prevText: 'Prev',
  nextText: 'Next',
  currentText: 'Today',
  monthNames: ['January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'],
  monthNamesShort: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
    'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
  dayNames: ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'],
  dayNamesShort: ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'],
  dayNamesMin: ['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'],
  weekHeader: 'Wk',
  dateFormat: 'dd.mm.yy',
  firstDay: 1,
  isRTL: false,
  showMonthAfterYear: false,
  yearSuffix: ''
};
$.datepicker.regional['de'] = {
  closeText: 'schließen',
  prevText: 'Zurück',
  nextText: 'Vor',
  currentText: 'heute',
  monthNames: ['Januar', 'Februar', 'März', 'April', 'Mai', 'Juni',
    'Juli', 'August', 'September', 'Oktober', 'November', 'Dezember'],
  monthNamesShort: ['Jan', 'Feb', 'Mär', 'Apr', 'Mai', 'Jun',
    'Jul', 'Aug', 'Sep', 'Okt', 'Nov', 'Dez'],
  dayNames: ['Sonntag', 'Montag', 'Dienstag', 'Mittwoch', 'Donnerstag', 'Freitag', 'Samstag'],
  dayNamesShort: ['So', 'Mo', 'Di', 'Mi', 'Do', 'Fr', 'Sa'],
  dayNamesMin: ['So', 'Mo', 'Di', 'Mi', 'Do', 'Fr', 'Sa'],
  weekHeader: 'Wo',
  dateFormat: 'dd.mm.yy',
  firstDay: 1,
  isRTL: false,
  showMonthAfterYear: false,
  yearSuffix: ''
};


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

function createDatepickerInstances(selectors, regional, urlPrefix, getPerson, onSelect) {

  var highlighted;
  var highlightedAbsences;

  var selector = selectors.join(",");

  $.datepicker.setDefaults($.datepicker.regional[regional]);
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

  if (dateFns.isWeekend(date)) {
    return [true, "notworkday"];
  } else {

    var dateString = $.datepicker.formatDate("yy-mm-dd", date);

    var isPublicHoliday = isSpecialDay(dateString, publicHolidays);

    var absenceType;
    var absenceCategory;
    if (isSpecialDay(dateString, absences)) {
      var absence = getAbsence(dateString, absences);
      absenceType = absence.type;
      absenceCategory = absence.category;
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
      cssClasses.push("sickday-"+absenceCategory)
    }

    if (isPersonalHoliday) {
      cssClasses.push("holiday");
      cssClasses.push("holiday-"+absenceCategory);
    }

    return [true, cssClasses.join(" ")];

  }

}

function isSpecialDay(formattedDate, specialDays) {

  var day = _.findWhere(specialDays, {date: formattedDate});

  return day !== undefined && day.dayLength <= 1;

}

function getAbsence(formattedDate, absences) {

  var absence = _.findWhere(absences, {date: formattedDate});

  return absence;
}

function isHalfWorkday(formattedDate, holidays) {

  return _.findWhere(holidays, {date: formattedDate, dayLength: 0.5}) !== undefined;

}
