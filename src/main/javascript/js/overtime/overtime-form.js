import $ from "jquery";
import { createDatepicker } from "../../components/datepicker";

$(document).ready(async function () {
  const urlPrefix = window.uv.apiPrefix;
  const personId = window.uv.personId;

  let startDateElement;
  let endDateElement;

  function getPersonId() {
    return personId;
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
