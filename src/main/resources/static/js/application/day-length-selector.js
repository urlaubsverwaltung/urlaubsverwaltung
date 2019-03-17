$(document).ready(function () {

  // re-calculate vacation days when changing the day length

  var urlPrefix = '<spring:url value="/api" />';
  var personId = '<c:out value="${person.id}" />';

  $('input[name="dayLength"]').on('change', function () {

    var dayLength = this.value;
    var startDate = $('input#from').datepicker("getDate");
    var toDate = $('input#to').datepicker("getDate");

    sendGetDaysRequest(urlPrefix, startDate, toDate, dayLength, personId, ".days");
    sendGetDepartmentVacationsRequest(urlPrefix, startDate, toDate, personId, "#departmentVacations");

  });

});
