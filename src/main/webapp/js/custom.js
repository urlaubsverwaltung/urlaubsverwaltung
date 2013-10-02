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

// sortable tables
$(document).ready(function()
    {
        // set initial striping
        $(".zebra-table tr:even").addClass("alt");

        // bind start and end order events
        $(".sortable-tbl").tablesorter()

            // if sorting start remove striping
            .bind("sortStart", function() {
                $(".zebra-table tr:even").removeClass("alt");
            })

            // if sorting done set striping
            .bind("sortEnd", function() {
                $(".zebra-table tr:even").addClass("alt");
            });

    }
);
    
// mouseover effects: rows highlighted
$(document).ready(function()
    {
        $(".zebra-table tr").mouseover(function() {
            $(this).addClass("over");
        });

        $(".zebra-table tr").mouseout(function() {
            $(this).removeClass("over");
        });

    }
);


function checkSonderurlaub(value) {

    if(value === "SPECIALLEAVE") {
        $('#special-leave-modal').modal("toggle"); 
    }

}

function showErrorDivIfAction(action) {

    var path = window.location.pathname;

    if(path.indexOf(action) != -1) {
        $("div#" + action).attr("style", "display: block")
    } 
    
}


// thanks to http://www.jaqe.de/2009/01/16/url-parameter-mit-javascript-auslesen/
function getUrlParam(name)
{
    name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");

    var regexS = "[\\?&]" + name + "=([^&#]*)";
    var regex = new RegExp(regexS);
    var results = regex.exec(window.location.href);

    if (results == null)
        return "";
    else
        return results[1];
}