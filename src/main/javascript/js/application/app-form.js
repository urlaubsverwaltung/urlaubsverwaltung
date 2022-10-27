import $ from "jquery";
import { parseISO } from "date-fns";
import parseQueryString from "../parse-query-string";
import { createDatepicker } from "../../components/datepicker";
import "../../components/timepicker";
import sendGetDaysRequest from "../send-get-days-request";
import sendGetDepartmentVacationsRequest from "../send-get-department-vacations-request";

$(document).ready(async function () {
  const person = window.uv.params.personId;
  if (person) {
    const personSelectElement = document.querySelector("#person-select");
    if (personSelectElement) {
      personSelectElement.value = person;
    }
  }

  const apiPrefix = window.uv.apiPrefix;
  const applicationForLeavePersonId = window.uv.application.person.id;

  let fromDateElement;
  let toDateElement;

  function getPersonId() {
    if (!applicationForLeavePersonId) {
      return $("#person-select option:selected").val();
    }
    return applicationForLeavePersonId;
  }

  function updateSelectionHints() {
    const dayLength = $("input:radio[name=dayLength]:checked").val();
    const startDate = parseISO(fromDateElement.value);
    const toDate = parseISO(toDateElement.value);

    sendGetDaysRequest(apiPrefix, startDate, toDate, dayLength, getPersonId(), ".days");
    sendGetDepartmentVacationsRequest(apiPrefix, startDate, toDate, getPersonId(), "#departmentVacations");
  }

  function setDefaultToDateValue() {
    if (!toDateElement.value) {
      toDateElement.value = fromDateElement.value;
    }
  }

  const [fromDateResult, toDateResult] = await Promise.allSettled([
    createDatepicker("#from", {
      urlPrefix: apiPrefix,
      getPersonId,
      onSelect: compose(updateSelectionHints, setDefaultToDateValue),
    }),
    createDatepicker("#to", { urlPrefix: apiPrefix, getPersonId, onSelect: updateSelectionHints }),
    createDatepicker("#at", { urlPrefix: apiPrefix, getPersonId, onSelect: updateSelectionHints }),
  ]);

  fromDateElement = fromDateResult.value;
  toDateElement = toDateResult.value;

  // CALENDAR: PRESET DATE IN APP FORM ON CLICKING DAY
  const { from, to } = parseQueryString(window.location.search);
  if (from) {
    const startDate = parseISO(from);
    const endDate = parseISO(to || from);

    let dayLength = $("input:radio[name=dayLength]:checked").val();
    sendGetDaysRequest(apiPrefix, startDate, endDate, dayLength, getPersonId(), ".days");
    sendGetDepartmentVacationsRequest(apiPrefix, startDate, endDate, getPersonId(), "#departmentVacations");
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

function compose(...functions) {
  // eslint-disable-next-line unicorn/no-array-reduce
  return functions.reduce(
    (a, b) =>
      (...arguments_) =>
        a(b(...arguments_)),
  );
}
