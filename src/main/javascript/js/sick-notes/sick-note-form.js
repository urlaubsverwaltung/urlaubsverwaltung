import $ from "jquery";
import { createDatepicker } from "../../components/datepicker";

function getPersonId() {
  return document.querySelector("[name='person']").value;
}

$(document).ready(async function () {
  const urlPrefix = globalThis.uv.apiPrefix;

  let fromDateElement;
  let toDateElement;
  let aubFromDateElement;
  let aubToDateElement;

  function handleFromSelect() {
    if (!toDateElement.value) {
      toDateElement.value = fromDateElement.value;
    }
  }

  function handleAubFromSelect() {
    if (!aubToDateElement.value) {
      aubToDateElement.value = aubFromDateElement.value;
    }
  }

  const [fromResult, toResult, aubFromResult, aubToResult] = await Promise.allSettled([
    createDatepicker("#from", { urlPrefix, getPersonId, onSelect: handleFromSelect }),
    createDatepicker("#to", { urlPrefix, getPersonId }),
    createDatepicker("#aubFrom", { urlPrefix, getPersonId, onSelect: handleAubFromSelect }),
    createDatepicker("#aubTo", { urlPrefix, getPersonId }),
  ]);

  fromDateElement = fromResult.value;
  toDateElement = toResult.value;
  aubFromDateElement = aubFromResult.value;
  aubToDateElement = aubToResult.value;
});
