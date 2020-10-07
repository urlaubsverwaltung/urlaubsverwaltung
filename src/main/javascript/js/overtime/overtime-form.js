import $ from "jquery";
import { createDatepickerInstances } from "../../components/datepicker";

function onSelect(event) {
  const endDateElement = document.querySelector("#endDate");

  if (event.target.matches('#startDate') && !endDateElement.value) {
    endDateElement.value = event.target.value;
  }
}

function getPersonId() {
  return window.uv.personId;
}

$(document).ready(async function () {
  const locale = window.navigator.language;
  const urlPrefix = window.uv.apiPrefix;

  await createDatepickerInstances(["#startDate", "#endDate"], locale, urlPrefix, getPersonId, onSelect);
});
