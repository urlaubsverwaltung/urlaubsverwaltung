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

var highlighted = new Array();

function getHolidaysForMonth(year, month, urlPrefix) {

    var url = urlPrefix + "?year=" + year + "&month=" + month;
    
    $.ajax({
        url: url,
        async: false,
        dataType: "json",
        type: "GET",
        success: function (data) {

            highlighted = data;
            
        }
    });

}

function createDatepickerInstances(regional, urlPrefix, vacationUrl) {

    $.datepicker.setDefaults($.datepicker.regional[regional]);
    $("#from, #to, #at").datepicker({
        numberOfMonths: 1,
        beforeShow: function(input) {

            // if there is no selected date use current year and current month
            if($(input).datepicker("getDate") == null) {
                var date = new Date();
                getHolidaysForMonth(date.getFullYear(), date.getMonth() + 1, urlPrefix);
            } else {
               // if there is a selected date use its year and month
                var date = $(input).datepicker("getDate");
                getHolidaysForMonth(date.getFullYear(), date.getMonth() + 1, urlPrefix); 
            }
            
        },
        onChangeMonthYear: function(year, month) {
            getHolidaysForMonth(year, month, urlPrefix);
        },
        beforeShowDay: function (date) {

            // if day is saturday or sunday, highlight it
            if (date.getDay() == 6 || date.getDay() == 0) {
                return [true, "notworkday"];
            } else {
                // if date is a work day, check if it is a public holiday
                // if so highlight it

                var dateString = $.datepicker.formatDate("yy-mm-dd", date);
                
                if($.inArray(dateString, highlighted) != -1) {
                    console.log(dateString + " is a public holiday");
                    return [true, "notworkday"];  
                } else {
                    return [true, ""];
                }
                
            }

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
            
            sendGetDaysRequest(vacationUrl, startDate, toDate, dayLength, ".days", true);

        }
    });
}

