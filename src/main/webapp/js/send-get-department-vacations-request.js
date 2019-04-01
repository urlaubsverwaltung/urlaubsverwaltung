import $ from 'jquery'
import format from '../lib/date-fns/format'

export default function sendGetDepartmentVacationsRequest(urlPrefix, startDate, endDate, personId, element) {

  var startDateString = startDate.getFullYear() + '-' + (startDate.getMonth() + 1) + '-' + startDate.getDate();
  var toDateString = endDate.getFullYear() + '-' + (endDate.getMonth() + 1) + '-' + endDate.getDate();

  var requestUrl = urlPrefix + "/vacations?departmentMembers=true&from=" + startDateString + "&to=" + toDateString
    + "&person=" + personId;

  $.get(requestUrl, function (data) {

    var vacations = data.response.vacations;

    var $vacations = $(element);

    $vacations.html("Antr&auml;ge von Mitarbeitern:");

    if(vacations.length > 0) {
      $.each(vacations, function (idx, vacation) {
        var startDate = format(vacation.from, "DD.MM.YYYY");
        var endDate = format(vacation.to, "DD.MM.YYYY");
        var person = vacation.person.niceName;

        $vacations.append("<br/>" + person + ": " + startDate + " - " + endDate);

        if(vacation.status === "ALLOWED") {
          $vacations.append(" <i class='fa fa-check positive' aria-hidden='true'></i>");
        }

      });
    } else {
      $vacations.append(" Keine");
    }

  });

}
