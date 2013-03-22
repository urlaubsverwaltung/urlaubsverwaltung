// script to count number of chars in textarea 
function count(val, id) {
    document.getElementById(id).innerHTML = val.length + "/";
}

function maxChars(elem, max) {
    if (elem.value.length > max) {
        elem.value = elem.value.substring(0, max);
    }
}

function sendGetDaysRequest(urlPrefix) {
    
    $(".days").empty();
    
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

    if(startDate !== undefined && toDate !== undefined && startDate !== null && toDate !== null) {

    var startDateString = startDate.getFullYear() + '-' + (startDate.getMonth() + 1) + '-' + startDate.getDate();
    var toDateString = toDate.getFullYear() + '-' + (toDate.getMonth() + 1) + '-' + toDate.getDate();

    var url = urlPrefix + "?start=" + startDateString + "&end=" + toDateString + "&length=" + dayLength;

    $.get(url, function(data) {

        $(".days").html("entspricht " + data + " Urlaubstag(e)");
    });
    }
}


