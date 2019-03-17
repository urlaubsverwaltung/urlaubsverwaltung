$(document).ready(function () {

  var datepickerLocale = window.navigator.language;
  var urlPrefix = window.uv.apiPrefix;
  var personId = window.uv.personId;

  var getPersonId = function () {
    return personId;
  };

  var onSelect = function (selectedDate) {

    instance = $(this).data("datepicker"),
      date = $.datepicker.parseDate(
        instance.settings.dateFormat ||
        $.datepicker._defaults.dateFormat,
        selectedDate, instance.settings);


    var $from = $("#from");
    var $to = $("#to");

    if (this.id === "from" && $to.val() === "") {
      $to.datepicker("setDate", selectedDate);
    }

    var dayLength = $('input:radio[name=dayLength]:checked').val();
    var startDate = $from.datepicker("getDate");
    var toDate = $to.datepicker("getDate");

    sendGetDaysRequest(urlPrefix, startDate, toDate, dayLength, getPersonId(), ".days");
    sendGetDepartmentVacationsRequest(urlPrefix, startDate, toDate, personId, "#departmentVacations");

  };

  var selectors = ["#from", "#to", "#at"];

  createDatepickerInstances(selectors, datepickerLocale, urlPrefix, getPersonId, onSelect);

});

$(function () {
  // CALENDAR: PRESET DATE IN APP FORM ON CLICKING DAY

  function preset(id, dateString) {

    var match = dateString.match(/\d+/g);

    var y = match[0];
    var m = match[1] - 1;
    var d = match[2];

    $(id).datepicker('setDate', new Date(y, m, d));
  }

  var from = document.getElementById('from').value;
  var to = document.getElementById('to').value;

  if (from) {
    preset('#from', from);
    preset('#to', to || from);

    var urlPrefix = window.uv.apiPrefix;
    var personId = window.uv.personId;
    var startDate = $("#from").datepicker("getDate");
    var endDate = $("#to").datepicker("getDate");

    sendGetDaysRequest(urlPrefix,
      startDate,
      endDate,
      $('input:radio[name=dayLength]:checked').val(),
      personId, ".days");

    sendGetDepartmentVacationsRequest(urlPrefix, startDate, endDate, personId, "#departmentVacations");
  }

  // Timepicker for optional startTime and endTime

  $('#startTime').timepicker({
    'step': 15,
    'timeFormat': 'H:i',
    'forceRoundTime': true,
    'scrollDefault': 'now'
  });
  $('#endTime').timepicker({
    'step': 15,
    'timeFormat': 'H:i',
    'forceRoundTime': true,
    'scrollDefault': 'now'
  });

});
