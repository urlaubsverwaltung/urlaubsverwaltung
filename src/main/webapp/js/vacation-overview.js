import $ from 'jquery'
import 'tablesorter'
import html from '../js/html-literal'
import '../css/vacation-overview.css'

function compare(currentDay, currentValue, status, type, absencePeriodName) {
  if (currentDay.dayText === currentValue.date
    && currentValue.status === status
    && currentValue.type === type
    && currentValue.absencePeriodName === absencePeriodName) {
    return true;
  }
}

$(function () {

    function selectedItemChange() {
      const selectedYear = document.querySelector('#yearSelect');
      const selectedMonth = document.querySelector('#monthSelect');
      const selectedDepartment = document.querySelector('#departmentSelect');
      const selectedDepartmentValue = selectedDepartment.options[selectedDepartment.selectedIndex].text;
      const selectedYearValue = selectedYear.options[selectedYear.selectedIndex].text;
      const selectedMonthValue = selectedMonth.options[selectedMonth.selectedIndex].value;

      if (selectedYearValue != null && selectedMonthValue != null && selectedDepartmentValue != null) {
        const url = location.protocol + "//" + location.host
          + "/api/vacationoverview?selectedYear="
          + encodeURIComponent(selectedYearValue) + "&selectedMonth="
          + encodeURIComponent(selectedMonthValue) + "&selectedDepartment="
          + encodeURIComponent(selectedDepartmentValue);

        const xhttp = new XMLHttpRequest();
        xhttp.open("GET", url, false);
        xhttp.setRequestHeader("Content-type", "application/json");
        xhttp.send();
        const holyDayOverviewResponse = JSON.parse(xhttp.responseText);
        if (holyDayOverviewResponse) {

          const overViewList = holyDayOverviewResponse.list;
          overViewList
            .forEach(function (listItem) {
              const personId = listItem.personID;
              const url = location.protocol + "//"
                + location.host + "/api/absences?year="
                + encodeURIComponent(selectedYearValue) + "&month="
                + encodeURIComponent(selectedMonthValue) + "&person="
                + encodeURIComponent(personId);
              const xhttp = new XMLHttpRequest();
              xhttp.open("GET", url, false);
              xhttp.setRequestHeader("Content-type",
                "application/json");
              xhttp.send();

              const response = JSON.parse(xhttp.responseText);
              if (response) {

                listItem.days
                  .forEach(currentDay => {
                      let absences = response.absences;

                      currentDay.cssClass = '';

                      if (absences.find(currentValue => compare(currentDay, currentValue, "WAITING", "VACATION", 'FULL'))) {
                        currentDay.cssClass += ' vacationOverview-day-personal-holiday-status-WAITING';
                      }

                      if (absences.find(currentValue => compare(currentDay, currentValue, "WAITING", "VACATION", 'MORNING'))) {
                        currentDay.cssClass += ' vacationOverview-day-personal-holiday-half-day-status-WAITING-morning';
                      }
                      if (absences.find(currentValue => compare(currentDay, currentValue, "WAITING", "VACATION", 'NOON'))) {
                        currentDay.cssClass += ' vacationOverview-day-personal-holiday-half-day-status-WAITING-noon';
                      }

                      if (absences.find(currentValue => compare(currentDay, currentValue, "ALLOWED", "VACATION", 'MORNING'))) {
                        currentDay.cssClass += ' vacationOverview-day-personal-holiday-half-day-status-ALLOWED-morning';
                      }
                      if (absences.find(currentValue => compare(currentDay, currentValue, "ALLOWED", "VACATION", 'NOON'))) {
                        currentDay.cssClass += ' vacationOverview-day-personal-holiday-half-day-status-ALLOWED-noon';
                      }

                      if (absences.find(currentValue => compare(currentDay, currentValue, "ALLOWED", "VACATION", 'FULL'))) {
                        currentDay.cssClass += ' vacationOverview-day-personal-holiday-status-ALLOWED';
                      }

                      if (absences.find(currentValue => compare(currentDay, currentValue, "ACTIVE", "SICK_NOTE", 'FULL'))) {
                        currentDay.cssClass += ' vacationOverview-day-sick-note';
                      }

                      if (absences.find(currentValue => compare(currentDay, currentValue, "ACTIVE", "SICK_NOTE", 'MORNING'))) {
                        currentDay.cssClass += ' vacationOverview-day-sick-note-half-day-morning';
                      }
                      if (absences.find(currentValue => compare(currentDay, currentValue, "ACTIVE", "SICK_NOTE", 'NOON'))) {
                        currentDay.cssClass += ' vacationOverview-day-sick-note-half-day-noon';
                      }

                      if (currentDay.cssClass !== '') {
                        currentDay.cssClass += ' vacationOverview-day-item';
                      }

                  }, this);
              }
            });

          const vacationOverviewTableHtml = html`<table id="vacationOverviewTable" class="list-table sortable tablesorter vacationOverview-table">
            <thead class="hidden-xs">
              <tr>
                <th class="sortable-field"><spring:message code="person.data.firstName"/></th>
                <th class="sortable-field"><spring:message code="person.data.lastName"/></th>
                ${overViewList[0].days.map(item => item.typeOfDay === "WEEKEND"
                  ? html`<th class="non-sortable vacationOverview-day-item vacationOverview-day-weekend">${item.dayNumber}</th>`
                  : html`<th class="non-sortable vacationOverview-day-item">${item.dayNumber}</th>`
                )}
              </tr>
            </thead>
            <tbody class="vacationOverview-tbody">
              ${overViewList.map(item => html`
                <tr>
                  <td class="hidden-xs">${item.person.firstName}</td>
                  <td class="hidden-xs">${item.person.lastName}</td>
                  ${item.days.map(dayItem => {
                    if (dayItem.typeOfDay === "WEEKEND") {
                      dayItem.cssClass = 'vacationOverview-day-weekend vacationOverview-day-item';
                    } else if (!dayItem.cssClass) {
                      dayItem.cssClass = 'vacationOverview-day vacationOverview-day-item ';
                    }
                    return html`<td class="${dayItem.cssClass}"></td>`;
                  })}
                </tr>
              `)}
            </tbody>
          </table>`;

          document.querySelector("#vacationOverview").innerHTML = vacationOverviewTableHtml;
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

    const selectedYear = document.querySelector('#yearSelect');
    const selectedMonth = document.querySelector('#monthSelect');
    const selectedDepartment = document.querySelector('#departmentSelect');

    selectedYear.addEventListener("change", () => selectedItemChange());
    selectedMonth.addEventListener("change", () => selectedItemChange());
    selectedDepartment.addEventListener("change", () => selectedItemChange());

    let event;
    if (typeof (Event) === "function") {
      event = new Event("change");
    } else {
      event = document.createEvent("Event");
      event.initEvent("change", true, true);
    }
    selectedYear.dispatchEvent(event);
  }
);
