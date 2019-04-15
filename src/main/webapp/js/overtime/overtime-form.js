import $ from 'jquery'
import { createDatepickerInstances } from '../../components/datepicker'

$(document).ready(async function () {
  var locale = window.navigator.language;
  var urlPrefix = window.uv.apiPrefix;

  var onSelect = function (selectedDate) {
    var $endDate = $("#endDate");

    if (this.id === "startDate" && $endDate.val() === "") {
      $endDate.datepicker("setDate", selectedDate);
    }
  };

  var getPersonId = function () {
    return window.uv.personId;
  };

  await createDatepickerInstances(["#startDate", "#endDate"], locale, urlPrefix, getPersonId, onSelect);
});
