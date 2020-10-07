import $ from "jquery";
import { parseISO } from "date-fns";
import parseQueryString from "../parse-query-string";
import { createDatepickerInstances } from "../../components/datepicker";
import "../../components/timepicker";
import sendGetDaysRequest from "../send-get-days-request";
import sendGetDepartmentVacationsRequest from "../send-get-department-vacations-request";

$(document).ready(async function () {
  const { apiPrefix: urlPrefix, personId } = window.uv;

  function getPersonId() {
    return personId;
  }

  function onSelect(event) {
    const target = event.target;
    const fromDateElement = document.querySelector("#from");
    const toDateElement = document.querySelector("#to");

    if (target === fromDateElement && !toDateElement.value) {
      toDateElement.value = fromDateElement.value;
    }

    const dayLength = $("input:radio[name=dayLength]:checked").val();
    const startDate = parseISO(fromDateElement.value);
    const toDate = parseISO(toDateElement.value);

    sendGetDaysRequest(urlPrefix, startDate, toDate, dayLength, getPersonId(), ".days");
    sendGetDepartmentVacationsRequest(urlPrefix, startDate, toDate, personId, "#departmentVacations");
  }

  const selectors = ["#from", "#to", "#at"];

  // createDatepickerInstances also initialises the jquery-ui datepicker
  // with the correct locale (en or de or ...)
  // if we don't wait here the datepicker instances below will always be english (default)
  await createDatepickerInstances(selectors, urlPrefix, getPersonId, onSelect);

  // CALENDAR: PRESET DATE IN APP FORM ON CLICKING DAY
  const { from, to } = parseQueryString(window.location.search);
  if (from) {
    const startDate = parseISO(from);
    const endDate = parseISO(to || from);

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
