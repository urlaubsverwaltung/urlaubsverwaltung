import $ from 'jquery';
import buildUrl from './build-url';
import formatNumber from './format-number';

export default function sendGetDaysRequestForTurnOfTheYear(urlPrefix, startDate, toDate, dayLength, personId, element) {

  $(element).empty();

  if (startDate !== undefined && toDate !== undefined && startDate !== null && toDate !== null) {

    if (startDate <= toDate) {

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

      var startString = before.getFullYear() + "-" + padZeros(before.getMonth() + 1) + '-' + padZeros(before.getDate());
      var toString = before.getFullYear() + '-12-31';
      var url = buildUrl(requestUrl, startString, toString, dayLength, personId);

      $.get(url, function (data) {
        var workDaysBefore = data.response.workDays;

        daysBefore = formatNumber(workDaysBefore);

        startString = after.getFullYear() + '-01-01';
        toString = after.getFullYear() + "-" + padZeros(after.getMonth() + 1) + '-' + padZeros(after.getDate());
        url = buildUrl(requestUrl, startString, toString, dayLength, personId);

        $.get(url, function (data) {
          var workDaysAfter = data.response.workDays;
          daysAfter = formatNumber(workDaysAfter);

          text = "<br />(" + daysBefore + " in " + before.getFullYear()
            + " und " + daysAfter + " in " + after.getFullYear() + ")";

          $(element).html(text);
        });

      });
    }

  }

}

function padZeros(number){
  return number <10? '0'+ number:''+ number;
}
