import $ from "jquery";
import { createDatepicker } from "../../components/datepicker";

document.addEventListener("DOMContentLoaded", async () => {
  const person = window.uv.params.person;
  if (person) {
    const employeeElement = document.querySelector("#employee");
    if (employeeElement) {
      employeeElement.value = person;
    }
  }

  const urlPrefix = window.uv.apiPrefix;
  const sickNotePersonId = window.uv.sickNote.person.id;

  let fromDateElement;
  let toDateElement;
  let aubFromDateElement;
  let aubToDateElement;

  function getPersonId() {
    if (!sickNotePersonId) {
      return $("#employee option:selected").val();
    }
    return sickNotePersonId;
  }

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
