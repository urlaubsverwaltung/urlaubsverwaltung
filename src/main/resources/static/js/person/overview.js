$(function () {

  var personId = window.uv.personId;
  var webPrefix = window.uv.webPrefix;
  var apiPrefix = window.uv.apiPrefix;

  // calendar is initialised when moment.js AND moment.language.js are loaded
  function initCalendar() {
    var year = getUrlParam("year");
    var date = moment();

    if (year.length > 0 && year != date.year()) {
      date.year(year).month(0).date(1);
    }

    var holidayService = Urlaubsverwaltung.HolidayService.create(webPrefix, apiPrefix, +personId);

    // NOTE: All moments are mutable!
    var startDateToCalculate = date.clone();
    var endDateToCalculate = date.clone();
    var shownNumberOfMonths = 10;
    var startDate = startDateToCalculate.subtract(shownNumberOfMonths / 2, 'months');
    var endDate = endDateToCalculate.add(shownNumberOfMonths / 2, 'months');

    var yearOfStartDate = startDate.year();
    var yearOfEndDate = endDate.year();

    $.when(
      holidayService.fetchPublic(yearOfStartDate),
      holidayService.fetchPersonal(yearOfStartDate),
      holidayService.fetchSickDays(yearOfStartDate),

      holidayService.fetchPublic(yearOfEndDate),
      holidayService.fetchPersonal(yearOfEndDate),
      holidayService.fetchSickDays(yearOfEndDate)
    ).always(function () {
      Urlaubsverwaltung.Calendar.init(holidayService, date);
    });
  }

  initCalendar();

  var resizeTimer = null;

  $(window).on('resize', function () {

    if (resizeTimer !== null) {
      clearTimeout(resizeTimer);
    }

    resizeTimer = setTimeout(function () {
      Urlaubsverwaltung.Calendar.reRender();
      resizeTimer = null;
    }, 30)

  });

});
