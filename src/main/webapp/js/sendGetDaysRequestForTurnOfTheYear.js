import buildUrl from './buildUrl';
import formatNumber from './formatNumber'

export default function sendGetDaysRequestForTurnOfTheYear(urlPrefix, startDate, toDate, dayLength, personId, el) {

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
      var workDaysBefore = data.response.workDays;

      daysBefore = formatNumber(workDaysBefore);

      startString = after.getFullYear() + '-1-1';
      toString = after.getFullYear() + "-" + (after.getMonth() + 1) + '-' + after.getDate();
      url = buildUrl(requestUrl, startString, toString, dayLength, personId);

      $.get(url, function (data) {
        var workDaysAfter = data.response.workDays;
        daysAfter = formatNumber(workDaysAfter);

        text = "<br />(" + daysBefore + " in " + before.getFullYear()
          + " und " + daysAfter + " in " + after.getFullYear() + ")";

        $(el).html(text);
      });

    });

  }

}
