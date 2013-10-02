// script to count number of chars in textarea 
function count(val, id) {
    document.getElementById(id).innerHTML = val.length + "/";
}

function maxChars(elem, max) {
    if (elem.value.length > max) {
        elem.value = elem.value.substring(0, max);
    }
}

function formatNumber(number) {
    
    var num = new Number(number).toFixed(1);
    
    var nArr = num.split(".");
    
    if(nArr[1] == 0) {
        num = new Number(number).toFixed(0);
    }
    
    return num;
}

function sendGetDaysRequest(urlPrefix, startDate, toDate, dayLength, el, long) {
    
    $(el).empty();
    
    if(startDate !== undefined && toDate !== undefined && startDate !== null && toDate !== null) {

    var startDateString = startDate.getFullYear() + '-' + (startDate.getMonth() + 1) + '-' + startDate.getDate();
    var toDateString = toDate.getFullYear() + '-' + (toDate.getMonth() + 1) + '-' + toDate.getDate();

    var url = urlPrefix + "?start=" + startDateString + "&end=" + toDateString + "&length=" + dayLength;

    $.get(url, function(data) {

        var text;
        
        if(long) {
            text = "entspricht " + formatNumber(data) + " Urlaubstag(e)";  
        } else {
            text = formatNumber(data);
        }

        if(startDate.getFullYear() != toDate.getFullYear()) {

            var before;
            var after;

            if(startDate.getFullYear() < toDate.getFullYear()) {
                before = startDate;
                after = toDate;
            } else {
                before = toDate;
                after = startDate;
            }

            // before - 31.12.
            // 1.1.   - after

            var daysBefore;
            var daysAfter;

            var startString = before.getFullYear() + "-" + (before.getMonth() + 1) + '-' + before.getDate();
            var toString = before.getFullYear() + '-12-31';
            var url = urlPrefix + "?start=" + startString + "&end=" + toString + "&length=" + dayLength;

            $.get(url, function(data) {
                daysBefore = formatNumber(data);

                startString = after.getFullYear() + '-1-1';
                toString = after.getFullYear() + "-" + (after.getMonth() + 1) + '-' + after.getDate();
                url = urlPrefix + "?start=" + startString + "&end=" + toString + "&length=" + dayLength;

                $.get(url, function(data) {
                    daysAfter = formatNumber(data);

                    if(long) {
                        text += "<br />(davon " + daysBefore + " in " + before.getFullYear()
                            + " und " + daysAfter + " in " + after.getFullYear() + ")";  
                    } else {
                        text += "<br />(" + before.getFullYear() + ": " + daysBefore + ", " 
                            + after.getFullYear() + ": " + daysAfter + ")";
                    }
                    
                    $(el).html(text);
                });

            });

        } else {
            $(el).html(text); 
        }

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