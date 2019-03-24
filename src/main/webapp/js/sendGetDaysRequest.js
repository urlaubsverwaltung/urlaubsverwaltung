import buildUrl from './buildUrl';
import formatNumber from './formatNumber';
import sendGetDaysRequestForTurnOfTheYear from './sendGetDaysRequestForTurnOfTheYear';

export default function sendGetDaysRequest(urlPrefix, startDate, toDate, dayLength, personId, el) {

  $(el).empty();

  if (startDate !== undefined && toDate !== undefined && startDate !== null && toDate !== null) {

    var startDateString = startDate.getFullYear() + '-' + (startDate.getMonth() + 1) + '-' + startDate.getDate();
    var toDateString = toDate.getFullYear() + '-' + (toDate.getMonth() + 1) + '-' + toDate.getDate();

    var requestUrl = urlPrefix + "/workdays";

    var url = buildUrl(requestUrl, startDateString, toDateString, dayLength, personId);

    $.get(url, function (data) {

      var workDays = data.response.workDays;

      var text;

      if(isNaN(workDays)) {
        text = "Ung&uuml;ltiger Zeitraum"
      } else if (workDays == 1) {
        text = formatNumber(workDays) + " Tag";
      } else {
        text = formatNumber(workDays) + " Tage";
      }

      $(el).html(text);

      if (startDate.getFullYear() != toDate.getFullYear()) {
        $(el).append('<span class="days-turn-of-the-year"></span>');
        sendGetDaysRequestForTurnOfTheYear(urlPrefix, startDate, toDate, dayLength, personId, el + ' .days-turn-of-the-year');
      }

    });

  }

}
