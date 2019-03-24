import $ from 'jquery'
import '../../lib/datepicker'
import createDatepickerInstances from '../createDatepickerInstances'

$(document).ready(function () {
  var locale = window.navigator.language;
  var urlPrefix = window.uv.apiPrefix;

  var onSelect = function (selectedDate) {
    var $endDate = $("#endDate");
    if (this.id == "startDate" && $endDate.val() === "") {
      $endDate.datepicker("setDate", selectedDate);
    }
  };

  var getPersonId = function () {
    return window.uv.personId;
  };

  createDatepickerInstances(["#startDate", "#endDate"], locale, urlPrefix, getPersonId, onSelect);
});
