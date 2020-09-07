import $ from "jquery";
import { createDatepickerInstances } from "../../components/datepicker";

$(document).ready(async function () {
  var person = window.uv.params.person;
  $("#employee").val(person);

  var datepickerLocale = window.navigator.language;
  var urlPrefix = window.uv.apiPrefix;
  var sickNotePersonId = window.uv.sickNote.person.id;

  function getPersonId() {
    if (!sickNotePersonId) {
      return $("#employee option:selected").val();
    }
    return sickNotePersonId;
  }

  var onSelect = function (selectedDate) {
    var $to = $("#to");

    if (this.id === "from" && $to.val() === "") {
      $to.datepicker("setDate", selectedDate);
    }
  };

  var onSelectAUB = function (selectedDate) {
    var $aubTo = $("#aubTo");

    if (this.id === "aubFrom" && $aubTo.val() === "") {
      $aubTo.datepicker("setDate", selectedDate);
    }
  };

  await createDatepickerInstances(["#from", "#to"], datepickerLocale, urlPrefix, getPersonId, onSelect);
  await createDatepickerInstances(["#aubFrom", "#aubTo"], datepickerLocale, urlPrefix, getPersonId, onSelectAUB);
});
