$(function () {
  // CALENDAR: PRESET DATE IN APP FORM ON CLICKING DAY

  function preset(id, dateString) {

    var match = dateString.match(/\d+/g);

    var y = match[0];
    var m = match[1] - 1;
    var d = match[2];

    $(id).datepicker('setDate', new Date(y, m, d));
  }

  var from = '${param.from}';
  var to = '${param.to}';

  if (from) {
    preset('#from', from);
    preset('#to', to || from);

    var urlPrefix = "<spring:url value='/api' />";
    var personId = "<c:out value='${person.id}' />";
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
