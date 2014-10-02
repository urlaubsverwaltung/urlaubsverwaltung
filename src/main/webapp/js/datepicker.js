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
            
            getHighlighted(urlPrefix + "/public-holiday?year=" + year + "&month=" + month, function(data) {
                highlighted = getDatesOfPublicHolidays(data);
            });

            var personId = getPerson();
            getHighlighted(urlPrefix + "/vacation/application-info?year=" + year + "&month=" + month + "&person=" + personId, function(data) {

                highlightedVacation = new Array();
                
                for(var i = 0; i < data.length; i++) {
                    var value = data[i].date;
                    if($.inArray(value, highlightedVacation) == -1) {
                        highlightedVacation.push(value);
                    }
                }
                
            });

        },
        onChangeMonthYear: function(year, month) {

            getHighlighted(urlPrefix + "/public-holiday?year=" + year + "&month=" + month, function(data) {
                highlighted = getDatesOfPublicHolidays(data);
            });

            var personId = getPerson();
            getHighlighted(urlPrefix + "/vacation/application-info?year=" + year + "&month=" + month + "&person=" + personId, function(data) {
                highlightedVacation = new Array();

                for(var i = 0; i < data.length; i++) {
                    var value = data[i].date;
                    if($.inArray(value, highlightedVacation) == -1) {
                        highlightedVacation.push(value);
                    }
                }
            });
            
        },
        beforeShowDay: function (date) {

            return colorizeDate(date, highlighted, highlightedVacation);

        },
        onSelect: onSelect
    });
}

function getDatesOfPublicHolidays(data) {

    var publicHolidayDates = new Array();

    for(var i = 0; i < data.response.publicHolidays.length; i++) {
        var value = data.response.publicHolidays[i].date;
        publicHolidayDates.push(value);
    }

    return publicHolidayDates; 
    
}

function colorizeDate(date, publicHolidays, vacation) {

    // 24.12.xx and 31.12.xx are half workdays
    function isHalfWorkday(date) {
        var d;
        return date && date.getMonth() === 11 && (d = date.getDate()) && (d === 24 || d === 31);
    }

    // if day is saturday or sunday, highlight it
    if (date.getDay() == 6 || date.getDay() == 0) {
        return [true, "notworkday"];
    } else if (isHalfWorkday(date)) {
        return [true, 'halfworkday'];
    } else {
        // if date is a work day, check if it is a public holiday
        // if so highlight it

        var dateString = $.datepicker.formatDate("yy-mm-dd", date);

        if($.inArray(dateString, publicHolidays) != -1) {
//            console.log(dateString + " is a public holiday");
            return [true, "notworkday"];
        } else if($.inArray(dateString, vacation) != -1) {
//            console.log(dateString + " is vacation");
            return [true, "holiday"];
        } else {
            return [true, ""];
        }

    }

}
    