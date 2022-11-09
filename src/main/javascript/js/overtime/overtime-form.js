import $ from "jquery";
import { createDatepicker } from "../../components/datepicker";

function getPersonId() {
  return document.querySelector("[name='person']").value;
}

$(document).ready(async function () {
  const person = window.uv.params.person;
  if (person) {
    const personsSelect = document.querySelector("#person-select");
    if (personsSelect) {
      personsSelect.value = person;
    }
  }

  const urlPrefix = window.uv.apiPrefix;

  let startDateElement;
  let endDateElement;

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
