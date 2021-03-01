import $ from "jquery";
import { parseISO } from "date-fns";
import parseQueryString from "../parse-query-string";
import { createDatepicker } from "../../components/datepicker";
import "../../components/timepicker";
import sendGetDaysRequest from "../send-get-days-request";
import sendGetDepartmentVacationsRequest from "../send-get-department-vacations-request";

$(document).ready(async function () {
  const { apiPrefix: urlPrefix, personId } = window.uv;
  let fromDateElement;
  let toDateElement;

  function updateSelectionHints() {
    const dayLength = $("input:radio[name=dayLength]:checked").val();
    const startDate = parseISO(fromDateElement.value);
    const toDate = parseISO(toDateElement.value);

    sendGetDaysRequest(urlPrefix, startDate, toDate, dayLength, personId, ".days");
    sendGetDepartmentVacationsRequest(urlPrefix, startDate, toDate, personId, "#departmentVacations");
  }

  function getPersonId() {
    return personId;
  }

  function setDefaultToDateValue() {
    if (!toDateElement.value) {
      toDateElement.value = fromDateElement.value;
    }
  }

  const [fromDateResult, toDateResult] = await Promise.allSettled([
    createDatepicker("#from", {
      urlPrefix,
      getPersonId,
      onSelect: compose(updateSelectionHints, setDefaultToDateValue),
    }),
    createDatepicker("#to", { urlPrefix, getPersonId, onSelect: updateSelectionHints }),
    createDatepicker("#at", { urlPrefix, getPersonId, onSelect: updateSelectionHints }),
  ]);

  fromDateElement = fromDateResult.value;
  toDateElement = toDateResult.value;

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

  let applicationSubmitPressed = false;
  document.querySelector("#apply-application").addEventListener("click", (event) => {
    event.preventDefault();

    const button = event.target || event.srcElement;
    if (!applicationSubmitPressed) {
      button.form.submit();
    }
    applicationSubmitPressed = true;
  });


});

function updateHolidayReplacementDtos() {

  let form = document.querySelector("#applicationForm");
  form.method = "get";
  form.action = window.location.pathname;
  form.submit();
}

window.updateHolidayReplacementDtos = updateHolidayReplacementDtos;

function compose(...functions) {
  return functions.reduce((a, b) => (...arguments_) => a(b(...arguments_)));
}
