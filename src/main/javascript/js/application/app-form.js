import $ from "jquery";
import parseQueryString from "../parse-query-string";
import { createDatepickerInstances } from "../../components/datepicker";
import "../../components/timepicker";
import sendGetDaysRequest from "../send-get-days-request";
import sendGetDepartmentVacationsRequest from "../send-get-department-vacations-request";

function valueToDate(dateString) {
  var match = dateString.match(/\d+/g);

  var y = match[0];
  var m = match[1] - 1;
  var d = match[2];

  return new Date(y, m, d);
}

$(document).ready(async function () {
  var datepickerLocale = window.navigator.language;
  var urlPrefix = window.uv.apiPrefix;
  var personId = window.uv.personId;

  var getPersonId = function () {
    return personId;
  };

  var onSelect = function (selectedDate) {
    var $from = $("#from");
    var $to = $("#to");

    if (this.id === "from" && $to.val() === "") {
      $to.datepicker("setDate", selectedDate);
    }

    var dayLength = $("input:radio[name=dayLength]:checked").val();
    var startDate = $from.datepicker("getDate");
    var toDate = $to.datepicker("getDate");

    sendGetDaysRequest(urlPrefix, startDate, toDate, dayLength, getPersonId(), ".days");
    sendGetDepartmentVacationsRequest(urlPrefix, startDate, toDate, personId, "#departmentVacations");
  };

  var selectors = ["#from", "#to", "#at"];

  // createDatepickerInstances also initialises the jquery-ui datepicker
  // with the correct locale (en or de or ...)
  // if we don't wait here the datepicker instances below will always be english (default)
  await createDatepickerInstances(selectors, datepickerLocale, urlPrefix, getPersonId, onSelect);

  // CALENDAR: PRESET DATE IN APP FORM ON CLICKING DAY
  const { from, to } = parseQueryString(window.location.search);
  if (from) {
    var startDate = valueToDate(from);
    var endDate = valueToDate(to || from);

    $("#from").datepicker("setDate", startDate);
    $("#to").datepicker("setDate", endDate);

    sendGetDaysRequest(
      urlPrefix,
      startDate,
      endDate,
      $("input:radio[name=dayLength]:checked").val(),
      personId,
      ".days",
    );

    sendGetDepartmentVacationsRequest(urlPrefix, startDate, endDate, personId, "#departmentVacations");
  }

  // Timepicker for optional startTime and endTime

  $("#startTime").timepicker({
    step: 15,
    timeFormat: "H:i",
    forceRoundTime: true,
    scrollDefault: "now",
  });
  $("#endTime").timepicker({
    step: 15,
    timeFormat: "H:i",
    forceRoundTime: true,
    scrollDefault: "now",
  });
});
