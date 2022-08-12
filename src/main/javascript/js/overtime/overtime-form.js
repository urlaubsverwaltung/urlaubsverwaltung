import $ from "jquery";
import { createDatepicker } from "../../components/datepicker";

$(document).ready(async function () {
  const urlPrefix = window.uv.apiPrefix;

  let startDateElement;
  let endDateElement;

  const personSelect = document.querySelector("select[name='person.id']");
  if (personSelect) {
    personSelect.addEventListener("change", function (event) {
      updateUrl(event.target);
    });
  }

  function handleStartDateSelect() {
    if (!endDateElement.value) {
      endDateElement.value = startDateElement.value;
    }
  }

  const [startDateResult, endDateResult] = await Promise.allSettled([
    createDatepicker("#startDate", { urlPrefix, getPersonId, onSelect: handleStartDateSelect }),
    createDatepicker("#endDate", { urlPrefix, getPersonId }),
  ]);

  startDateElement = startDateResult.value;
  endDateElement = endDateResult.value;
});

function getPersonId() {
  return document.querySelector("[name='person.id']").value;
}

function updateUrl(htmlFormInputElement) {
  const url = new URL(window.location);
  url.searchParams.set(htmlFormInputElement.getAttribute("name"), htmlFormInputElement.value);
  window.history.replaceState(undefined, "", url);
}
