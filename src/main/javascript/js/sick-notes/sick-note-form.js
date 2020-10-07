import $ from "jquery";
import { createDatepickerInstances } from "../../components/datepicker";

function onSelect (event) {
  const toElement = document.querySelector("#to");

  if (event.target.matches("#from") && !toElement.value) {
    toElement.value = event.target.value;
  }
}

function onSelectAUB (event) {
  const aubToElement = document.querySelector("#aubTo");

  if (event.target.matches("aubFrom") && !aubToElement.value) {
    aubToElement.value = event.target.value;
  }
}

$(document).ready(async function () {
  const person = window.uv.params.person;
  $("#employee").val(person);

  const urlPrefix = window.uv.apiPrefix;
  const sickNotePersonId = window.uv.sickNote.person.id;

  function getPersonId() {
    if (!sickNotePersonId) {
      return $("#employee option:selected").val();
    }
    return sickNotePersonId;
  }

  await createDatepickerInstances(["#from", "#to"], urlPrefix, getPersonId, onSelect);
  await createDatepickerInstances(["#aubFrom", "#aubTo"], urlPrefix, getPersonId, onSelectAUB);
});
