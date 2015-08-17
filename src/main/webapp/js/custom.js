// script to count number of chars in textarea 
function count(val, id) {
    document.getElementById(id).innerHTML = val.length + "/";
}

function maxChars(elem, max) {
    if (elem.value.length > max) {
        elem.value = elem.value.substring(0, max);
    }
}

function navigate(url) {
    window.location.href = url;
}

function formatNumber(number) {

    return new Number(number).toLocaleString("de", {
      maximumFractionDigits: 1,
      minimumFractionDigits: 0
    });
}

function sendGetDaysRequest(urlPrefix, startDate, toDate, dayLength, personId, el) {

  $(el).empty();

  if (startDate !== undefined && toDate !== undefined && startDate !== null && toDate !== null) {

    var startDateString = startDate.getFullYear() + '-' + (startDate.getMonth() + 1) + '-' + startDate.getDate();
    var toDateString = toDate.getFullYear() + '-' + (toDate.getMonth() + 1) + '-' + toDate.getDate();

    var requestUrl = urlPrefix + "/workdays";

    var url = buildUrl(requestUrl, startDateString, toDateString, dayLength, personId);

    $.get(url, function (data) {

      var text;

      if (data == 1) {
        text = formatNumber(data) + " Tag";
      } else {
        text = formatNumber(data) + " Tage";
      }

      $(el).html(text);

      if (startDate.getFullYear() != toDate.getFullYear()) {
        $(el).append('<span class="days-turn-of-the-year"></span>');
        sendGetDaysRequestForTurnOfTheYear(urlPrefix, startDate, toDate, dayLength, personId, el + ' .days-turn-of-the-year');
      }

    });

  }

}

function sendGetDaysRequestForTurnOfTheYear(urlPrefix, startDate, toDate, dayLength, personId, el) {

  $(el).empty();

  if (startDate !== undefined && toDate !== undefined && startDate !== null && toDate !== null) {

    var requestUrl = urlPrefix + "/workdays";

    var text;

    var before;
    var after;

    if (startDate.getFullYear() < toDate.getFullYear()) {
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
    var url = buildUrl(requestUrl, startString, toString, dayLength, personId);

    $.get(url, function (data) {
      daysBefore = formatNumber(data);

      startString = after.getFullYear() + '-1-1';
      toString = after.getFullYear() + "-" + (after.getMonth() + 1) + '-' + after.getDate();
      url = buildUrl(requestUrl, startString, toString, dayLength, personId);

      $.get(url, function (data) {
        daysAfter = formatNumber(data);

        text = "<br />(" + daysBefore + " in " + before.getFullYear()
          + " und " + daysAfter + " in " + after.getFullYear() + ")";

        $(el).html(text);
      });

    });

  }

}

function buildUrl(urlPrefix, startDate, endDate, dayLength, personId) {

    return urlPrefix + "?from=" + startDate + "&to=" + endDate + "&length=" + dayLength + "&person=" + personId; 
    
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

function sendGetDepartmentVacationsRequest(urlPrefix, startDate, endDate, personId, el) {

  var startDateString = startDate.getFullYear() + '-' + (startDate.getMonth() + 1) + '-' + startDate.getDate();
  var toDateString = endDate.getFullYear() + '-' + (endDate.getMonth() + 1) + '-' + endDate.getDate();

  var requestUrl = urlPrefix + "/vacations?departmentMembers=true&from=" + startDateString + "&to=" + toDateString
    + "&person=" + personId;

  $.get(requestUrl, function (data) {

    var vacations = data.response.vacations;

    var $vacations = $(el);

    $vacations.html("&Uuml;berschneidende Antr&auml;ge:");

    if(vacations.length > 0) {
      $.each(vacations, function (idx, vacation) {
        var startDate = moment(vacation.from).format("DD.MM.YYYY");
        var endDate = moment(vacation.to).format("DD.MM.YYYY");
        var person = vacation.person.niceName;

        $vacations.append("<br/>" + person + ": " + startDate + " - " + endDate);
      });
    } else {
      $vacations.append(" Keine");
    }

  });

}

// toggling of full/half day in app form
$(document).ready(function()
    {
        $(".dayLength-full").click(function() {
            $('.full-day').show();
            $('.half-day').hide();
        });

        $(".dayLength-half").click(function() {
            $('.full-day').hide();
            $('.half-day').show();
        });

    }
);