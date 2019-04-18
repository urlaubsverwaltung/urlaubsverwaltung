import $ from 'jquery'
import 'tablesorter'
import '../css/vacation-overview.css'

$(function () {

    function selectedItemChange() {
      var selectedYear = document.querySelector('#yearSelect');
      var selectedMonth = document.querySelector('#monthSelect');
      var selectedDepartment = document.querySelector('#departmentSelect');
      var selectedDepartmentValue = selectedDepartment.options[selectedDepartment.selectedIndex].text;
      var selectedYearValue = selectedYear.options[selectedYear.selectedIndex].text;
      var selectedMonthValue = selectedMonth.options[selectedMonth.selectedIndex].value;

      function compare(currentDay, currentValue, status, type, dayLength) {
        if (currentDay.dayText === currentValue.date
          && currentValue.status === status
          && currentValue.type === type
          && currentValue.dayLength === dayLength) {
          return true;
        }
      }

      if (selectedYearValue != null && selectedMonthValue != null
        && selectedDepartmentValue != null) {
        var url = location.protocol + "//" + location.host
          + "/api/vacationoverview?selectedYear="
          + encodeURIComponent(selectedYearValue) + "&selectedMonth="
          + encodeURIComponent(selectedMonthValue) + "&selectedDepartment="
          + encodeURIComponent(selectedDepartmentValue);

        var xhttp = new XMLHttpRequest();
        xhttp.open("GET", url, false);
        xhttp.setRequestHeader("Content-type", "application/json");
        xhttp.send();
        var holyDayOverviewResponse = JSON.parse(xhttp.responseText);
        if (holyDayOverviewResponse && holyDayOverviewResponse.response) {

          var overViewList = holyDayOverviewResponse.response.list;
          overViewList
            .forEach(function (listItem) {
              var personId = listItem.personID;
              var url = location.protocol + "//"
                + location.host + "/api/absences?year="
                + encodeURIComponent(selectedYearValue) + "&month="
                + encodeURIComponent(selectedMonthValue) + "&person="
                + encodeURIComponent(personId);
              var xhttp = new XMLHttpRequest();
              xhttp.open("GET", url, false);
              xhttp.setRequestHeader("Content-type",
                "application/json");
              xhttp.send();
              var response = JSON.parse(xhttp.responseText);
              if (response) {

                listItem.days
                  .forEach(
                    function (currentDay) {
                      let absences = response.response.absences;

                      if (absences.find(currentValue => compare(currentDay, currentValue,"WAITING", "VACATION", 1))) {
                        currentDay.cssClass = 'vacationOverview-day-personal-holiday-status-WAITING vactionOverview-day-item ';
                      }

                      if (absences.find(currentValue => compare(currentDay, currentValue,"WAITING", "VACATION", 0.5))) {
                        currentDay.cssClass = ' vacationOverview-day-personal-holiday-half-day-status-WAITING vactionOverview-day-item ';
                      }

                      if (absences.find(currentValue => compare(currentDay, currentValue,"ALLOWED", "VACATION", 0.5))) {
                        currentDay.cssClass = ' vacationOverview-day-personal-holiday-half-day-status-ALLOWED vactionOverview-day-item ';
                      }

                      if (absences.find(currentValue => compare(currentDay, currentValue,"ALLOWED", "VACATION", 1))) {
                        currentDay.cssClass = ' vacationOverview-day-personal-holiday-status-ALLOWED vactionOverview-day-item ';
                      }

                      if (absences.find(currentValue => compare(currentDay, currentValue,"ACTIVE", "SICK_NOTE", 1))) {
                        currentDay.cssClass = ' vacationOverview-day-sick-note vactionOverview-day-item ';
                      }

                      if (absences.find(currentValue => compare(currentDay, currentValue,"ACTIVE", "SICK_NOTE", 0.5))) {
                        currentDay.cssClass = ' vacationOverview-day-sick-note-half-day vactionOverview-day-item ';
                      }

                    }, this);
              }
            });

          var outputTable = "<table cellspacing='0' id='vacationOverviewTable' class='list-table sortable tablesorter vacationOverview-table'>";
          outputTable += "<thead class='hidden-xs'>";
          outputTable += "<tr>";
          outputTable += "<th class='sortable-field'><spring:message code='person.data.firstName'/></th>";
          outputTable += "<th class='sortable-field'><spring:message code='person.data.lastName'/></th>";
          overViewList[0].days
            .forEach(
              function (item) {
                let defaultClasses = "non-sortable vactionOverview-day-item";
                if (item.typeOfDay === "WEEKEND") {
                  outputTable += "<th class='" + defaultClasses + "vacationOverview-day-weekend'>" + item.dayNumber + "</th>";
                } else {
                  outputTable += "<th class='" + defaultClasses + "'>" + item.dayNumber + "</th>";
                }
              }, outputTable);
          outputTable += "</tr><tbody class='vacationOverview-tbody'>";
          overViewList
            .forEach(
              function (item) {
                outputTable += "<tr>";
                outputTable += "<td class='hidden-xs'>"
                  + item.person.firstName
                  + "</td>";
                outputTable += "<td class='hidden-xs'>"
                  + item.person.lastName
                  + "</td>";
                item.days
                  .forEach(
                    function (dayItem) {
                      if (dayItem.typeOfDay === "WEEKEND") {
                        dayItem.cssClass = ' vacationOverview-day-weekend vactionOverview-day-item';
                      } else {
                        if (!dayItem.cssClass) {
                          dayItem.cssClass = ' vacationOverview-day vactionOverview-day-item ';
                        }
                      }
                      outputTable += "<td class='" + dayItem.cssClass + "'></td>";
                    }, outputTable);
                outputTable += "</tr>";
              }, outputTable);

          outputTable += "</tbody></table>";
          var element = document.querySelector("#vacationOverview");
          element.innerHTML = outputTable;
        }
      }

      $("table.sortable").tablesorter({
        sortList: [[0, 0]],
        headers: {
          '.non-sortable': {
            sorter: false
          }
        }
      });
    }

    var selectedYear = document.querySelector('#yearSelect');
    var selectedMonth = document.querySelector('#monthSelect');
    var selectedDepartment = document.querySelector('#departmentSelect');

    selectedYear.addEventListener("change", function () {
      selectedItemChange();
    });
    selectedMonth.addEventListener("change", function () {
      selectedItemChange();
    });
    selectedDepartment.addEventListener("change", function () {
      selectedItemChange();
    });
    var event;
    if (typeof (Event) === "function") {
      event = new Event("change");
    } else {
      event = document.createEvent("Event");
      event.initEvent("change", true, true);
    }
    selectedYear.dispatchEvent(event);
  }
);
