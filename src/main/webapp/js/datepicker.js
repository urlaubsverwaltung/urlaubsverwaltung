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
    var highlightedVacation;

    var selector = selectors.join(",");

    $.datepicker.setDefaults($.datepicker.regional[regional]);
    $(selector).datepicker({
        numberOfMonths: 1,
        beforeShow: function(input, inst) {

            var calendrier = inst.dpDiv;
            var top  = $(this).offset().top + $(this).outerHeight();
            var left = $(this).offset().left;
            setTimeout(function(){ calendrier.css({'top' : top, 'left': left}); },10);

            var date;
            
            if($(input).datepicker("getDate") == null) {
                date = Date.today();
            } else {
                date = $(input).datepicker("getDate");
            }

            var year = date.getFullYear();
            var month = date.getMonth() + 1;
            
            getHighlighted(urlPrefix + "/holidays?year=" + year + "&month=" + month, function(data) {
                highlighted = getPublicHolidays(data);
            });

            var personId = getPerson();
            getHighlighted(urlPrefix + "/vacations/days?year=" + year + "&month=" + month + "&person=" + personId, function(data) {
                highlightedVacation = getPersonalHolidays(data);
            });

        },
        onChangeMonthYear: function(year, month) {

            getHighlighted(urlPrefix + "/holidays?year=" + year + "&month=" + month, function(data) {
                highlighted = getPublicHolidays(data);
            });

            var personId = getPerson();
            getHighlighted(urlPrefix + "/vacations/days?year=" + year + "&month=" + month + "&person=" + personId, function(data) {
                highlightedVacation = getPersonalHolidays(data);
            });
            
        },
        beforeShowDay: function (date) {

            return colorizeDate(date, highlighted, highlightedVacation);

        },
        onSelect: onSelect
    });
}

function getPersonalHolidays(data) {

  var personalHolidays = new Array();

  for(var i = 0; i < data.length; i++) {
    var value = data[i];
    if($.inArray(value, personalHolidays) == -1) {
      personalHolidays.push(value);
    }
  }

  return personalHolidays;

}

function getPublicHolidays(data) {

    var publicHolidayDates = new Array();

    for(var i = 0; i < data.response.publicHolidays.length; i++) {
        var value = data.response.publicHolidays[i];
        publicHolidayDates.push(value);
    }

    return publicHolidayDates;
    
}

function colorizeDate(date, publicHolidays, vacation) {

    // if day is saturday or sunday, highlight it
    if (date.getDay() == 6 || date.getDay() == 0) {
        return [true, "notworkday"];
    } else {
      var dateString = $.datepicker.formatDate("yy-mm-dd", date);

      var isPublicHoliday = isHoliday(dateString, publicHolidays);
      var isPersonalHoliday = isHoliday(dateString, vacation);
      var isHalfWorkDay = isHalfWorkday(dateString, publicHolidays) || isHalfWorkday(dateString, vacation);

        var cssClass = "";

        if(isPublicHoliday) {
            cssClass += " notworkday";
        }

        if(isHalfWorkDay) {
            cssClass += " halfworkday";
        }

        if(isPersonalHoliday) {
            cssClass += " holiday";
        }

        return [true, cssClass];

    }

}

function isHoliday(formattedDate, holidays) {

  var holiday = _.findWhere(holidays, {date: formattedDate});

  return holiday !== undefined && holiday.dayLength <= 1;

}

function isHalfWorkday(formattedDate, holidays) {

    return _.findWhere(holidays, {date: formattedDate, dayLength: 0.5}) !== undefined;

}
