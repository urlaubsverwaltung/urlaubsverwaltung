$(document).ready(function () {

  var datepickerLocale = "${pageContext.response.locale.language}";
  var urlPrefix = "<spring:url value='/api' />";
  var personId = '<c:out value="${person.id}" />';

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
