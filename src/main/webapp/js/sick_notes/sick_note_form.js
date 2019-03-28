import $ from 'jquery';
import { createDatepickerInstances } from '../../components/datepicker';

$(document).ready(function () {

  var person = window.uv.params.person;
  $("#employee").val(person);

  var datepickerLocale = window.navigator.language;
  var urlPrefix = window.uv.apiPrefix;
  var sickNoteId = window.uv.sickNote.id;
  var sickNotePersonId = window.uv.sickNote.person.id;

  function getPersonId() {
    if (!sickNoteId) {
      return $("#employee option:selected").val();
    }
    return sickNoteId || sickNotePersonId;
  }

  var onSelect = function (selectedDate) {
    if (this.id == "from" && $("#to").val() === "") {
      $("#to").datepicker("setDate", selectedDate);
    }
  };

  var onSelectAUB = function (selectedDate) {
    if (this.id == "aubFrom" && $("#aubTo").val() === "") {
      $("#aubTo").datepicker("setDate", selectedDate);
    }
  };

  createDatepickerInstances(["#from", "#to"], datepickerLocale, urlPrefix, getPersonId, onSelect);
  createDatepickerInstances(["#aubFrom", "#aubTo"], datepickerLocale, urlPrefix, getPersonId, onSelectAUB);

});
