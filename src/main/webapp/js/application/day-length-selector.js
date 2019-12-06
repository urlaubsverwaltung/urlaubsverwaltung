import $ from 'jquery';
import sendGetDaysRequest from '../send-get-days-request';
import sendGetDepartmentVacationsRequest from '../send-get-department-vacations-request';

$(document).ready(function () {

  // re-calculate vacation days when changing the day length

  var urlPrefix = window.uv.apiPrefix;
  var personId = window.uv.personId;

  $('input[name="dayLength"]').on('change', function () {

    var dayLength = this.value;
    var startDate = $('input#from').datepicker("getDate");
    var toDate = $('input#to').datepicker("getDate");

    sendGetDaysRequest(urlPrefix, startDate, toDate, dayLength, personId, ".days");
    sendGetDepartmentVacationsRequest(urlPrefix, startDate, toDate, personId, "#departmentVacations");

  });

});
