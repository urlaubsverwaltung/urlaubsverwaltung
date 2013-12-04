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

function createDatepickerInstances(regional, urlPrefix, vacationUrl, personId) {

    var highlighted;
    var highlightedVacation;
    
    $.datepicker.setDefaults($.datepicker.regional[regional]);
    $("#from, #to, #at").datepicker({
        numberOfMonths: 1,
        beforeShow: function(input) {

            var date;
            
            if($(input).datepicker("getDate") == null) {
                date = Date.today();
            } else {
                date = $(input).datepicker("getDate");
            }

            var year = date.getFullYear();
            var month = date.getMonth() + 1;
            
            getHighlighted(urlPrefix + "public-holiday?year=" + year + "&month=" + month, function(data) {
                highlighted = data;
            });

            getHighlighted(urlPrefix + "holiday?year=" + year + "&month=" + month + "&person=" + personId, function(data) {

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

            getHighlighted(urlPrefix + "public-holiday?year=" + year + "&month=" + month, function(data) {
                highlighted = data;
            });

            getHighlighted(urlPrefix + "holiday?year=" + year + "&month=" + month + "&person=" + personId, function(data) {
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
        onSelect: function (selectedDate) {
            instance = $(this).data("datepicker"),
                date = $.datepicker.parseDate(
                    instance.settings.dateFormat ||
                        $.datepicker._defaults.dateFormat,
                    selectedDate, instance.settings);

            if (this.id == "from") {
                $("#to").datepicker("setDate", selectedDate);
            }


            var dayLength = $('input:radio[name=howLong]:checked').val();
            var startDate = "";
            var toDate = "";

            if (dayLength === "FULL") {
                startDate = $("#from").datepicker("getDate");
                toDate = $("#to").datepicker("getDate");
            } else {
                startDate = $("#at").datepicker("getDate");
                toDate = $("#at").datepicker("getDate");
            }
            
            sendGetDaysRequest(vacationUrl, startDate, toDate, dayLength, personId, ".days", true);

        }
    });
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


function createDatepickerInstanceForSickNote(regional, from, to) {

    $.datepicker.setDefaults($.datepicker.regional[regional]);

    var selector = "#" + from + ", #" + to;
    
    $(selector).datepicker({
        numberOfMonths: 1,
        beforeShow: function(input, inst){
            var calendrier = inst.dpDiv;
            var top  = $(this).offset().top + $(this).outerHeight();
            var left = $(this).offset().left;
            setTimeout(function(){ calendrier.css({'top' : top, 'left': left}); },10);
        },
        onSelect: function(date) {
            if (this.id == from) {
                $("#" + to).datepicker("setDate", date);
            }
        },
        beforeShowDay: function (date) {

            // if day is saturday or sunday, highlight it
            if (date.getDay() == 6 || date.getDay() == 0) {
                return [true, "notworkday"];
            } else {
                return [true, ""];
            }

        }
    });
    
}

var publicHolidays = new Array();
function getPublicHolidays(year, month, urlPrefix) {

    var url = urlPrefix + "public-holiday?year=" + year + "&month=" + month;

    console.log("Load public holidays for year=" + year + " and month=" + month);
    
    getHighlighted(url, function(data) {

        // do add loaded data only if not already in publicHolidays array
        for(var i = 0; i < data.length; i++) {
            var value = data[i];
            if($.inArray(value, publicHolidays) == -1) {
                publicHolidays.push(value);
            }
        } 
        
    })
    
}

var holidays = new Array();
var ids = new Array();
function getHolidays(year, month, urlPrefix, personId) {

    var url = urlPrefix + "holiday?year=" + year + "&month=" + month + "&person=" + personId;

    console.log("Load vacations for year=" + year + " and month=" + month);
    
    getHighlighted(url, function(data) {

        // do add loaded data only if not already in holidays array
        for(var i = 0; i < data.length; i++) {
            var value = data[i].date;
            if($.inArray(value, holidays) == -1) {
                holidays.push(value);
                ids[value] = data[i].applicationId;
            }
        }  
        
    })

}

function fetchHighlightedDays(date, urlPrefix, personId) {

    publicHolidays = new Array();
    holidays = new Array();
    ids = new Array();

    // last month
    date.addMonths(-1);
    var year = date.getFullYear();
    var month = date.getMonth() + 1;
    getPublicHolidays(year, month, urlPrefix);
    getHolidays(year, month, urlPrefix, personId);

    // 4 times: current month, next month, next month + 1, next month + 2 
    for(var i = 1; i < 5; i++) {
        date.addMonths(+1);
        year = date.getFullYear();
        month = date.getMonth() + 1;
        getPublicHolidays(year, month, urlPrefix);
        getHolidays(year, month, urlPrefix, personId); 
    }
    
}

function calculateNumberOfMonths() {
    
    var width = $(window).width();
    
    var numberOfMonths = 1;
    var size = width;
    
    while(size > 361 && numberOfMonths < 5) {
        size = width / numberOfMonths;
        numberOfMonths++;
    }

    console.log("Window width=" + width + " displaying " + numberOfMonths + " months");
    
    return numberOfMonths;
    
}
    
function createDatepickerForVacationOverview(div, regional, urlPrefix, personId, defaultDate) {

    var calenderUrl = urlPrefix + "/calendar/";
    
    $.datepicker.setDefaults($.datepicker.regional[regional]);

    $(div).datepicker({
        numberOfMonths: calculateNumberOfMonths(),
        defaultDate: defaultDate,
        onChangeMonthYear: function(year, month) {

            // month can be changed in two directions: previous/next
            // so holidays have to be loaded for previous month and next month in datepicker container
            
            var date = Date.today().set({ year: year, month: (month-1), day: 1 });
            
            getPublicHolidays(date.getFullYear(), (date.getMonth() + 1), calenderUrl);
            getHolidays(date.getFullYear(), (date.getMonth() + 1), calenderUrl, personId);
            
            // +4 because showing 5 months
            date.addMonths(+4);

            getPublicHolidays(date.getFullYear(), (date.getMonth() + 1), calenderUrl);
            getHolidays(date.getFullYear(), (date.getMonth() + 1), calenderUrl, personId);
            
        },
        beforeShowDay: function (date) {

            return colorizeDate(date, publicHolidays, holidays);

        },
        onSelect: function (dateString) {
            
            var date = Date.parse(dateString);
            
            var id = ids[date.toString('yyyy-MM-dd')];

            if(id !== undefined) {
                document.location.href = urlPrefix + "/application/" + id;  
            }
            
        }
    });
    
}
