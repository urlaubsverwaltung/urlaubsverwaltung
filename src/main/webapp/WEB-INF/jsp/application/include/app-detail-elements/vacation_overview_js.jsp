<script>

    $(function () {

            function selectedItemChange() {
                var selectedYear = document.getElementById('yearSelect');
                var selectedMonth = document.getElementById('monthSelect');
                var selectedDepartment = document
                    .getElementById('departmentSelect');
                var selectedDepartmentValue = selectedDepartment.options[selectedDepartment.selectedIndex].text;
                var selectedYearValue = selectedYear.options[selectedYear.selectedIndex].text;
                var selectedMonthValue = selectedMonth.options[selectedMonth.selectedIndex].value;
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
                    if (holyDayOverviewResponse != null
                        && holyDayOverviewResponse != undefined
                        && holyDayOverviewResponse.response != null
                        && holyDayOverviewResponse.response != undefined) {

                        var overViewList = holyDayOverviewResponse.response.list;
                        overViewList
                            .forEach(function (listItem, index, array) {
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
                                if (response != null && response != undefined) {

                                    listItem.days
                                        .forEach(
                                            function (currentDay, index,
                                                      array) {
                                                if (response.response.absences
                                                    .find(
                                                        function (
                                                            currentValue,
                                                            index,
                                                            array) {
                                                            if (this.toString() == currentValue.date
                                                                && currentValue.status === "WAITING"
                                                                && currentValue.type === "VACATION"
                                                                && currentValue.dayLength === 1) {
                                                                return "testdatacreator";
                                                            }
                                                        },
                                                        currentDay.dayText)) {
                                                    currentDay.cssClass = 'vacationOverview-day-personal-holiday-status-WAITING vactionOverview-day-item ';
                                                }
                                                if (response.response.absences
                                                    .find(
                                                        function (
                                                            currentValue,
                                                            index,
                                                            array) {
                                                            if (this.toString() == currentValue.date
                                                                && currentValue.status === "WAITING"
                                                                && currentValue.type === "VACATION"
                                                                && currentValue.dayLength < 1) {
                                                                return "testdatacreator";
                                                            }
                                                        },
                                                        currentDay.dayText)) {
                                                    currentDay.cssClass = ' vacationOverview-day-personal-holiday-half-day-status-WAITING vactionOverview-day-item ';
                                                }

                                                if (response.response.absences
                                                    .find(
                                                        function (
                                                            currentValue,
                                                            index,
                                                            array) {
                                                            if (this.toString() == currentValue.date
                                                                && currentValue.status === "ALLOWED"
                                                                && currentValue.dayLength < 1
                                                                && currentValue.type === "VACATION") {
                                                                return "testdatacreator";
                                                            }
                                                        },
                                                        currentDay.dayText)) {
                                                    currentDay.cssClass = ' vacationOverview-day-personal-holiday-half-day-status-ALLOWED vactionOverview-day-item ';
                                                }
                                                if (response.response.absences
                                                    .find(
                                                        function (
                                                            currentValue,
                                                            index,
                                                            array) {
                                                            if (this.toString() == currentValue.date
                                                                && currentValue.status === "ALLOWED"
                                                                && currentValue.dayLength === 1
                                                                && currentValue.type === "VACATION") {
                                                                return "testdatacreator";
                                                            }
                                                        },
                                                        currentDay.dayText)) {
                                                    currentDay.cssClass = ' vacationOverview-day-personal-holiday-status-ALLOWED vactionOverview-day-item ';
                                                }
                                                if (response.response.absences
                                                    .find(
                                                        function (
                                                            currentValue,
                                                            index,
                                                            array) {
                                                            if (this.toString() == currentValue.date
                                                                && currentValue.type === 'SICK_NOTE'
                                                                && currentValue.dayLength === 1) {
                                                                return "testdatacreator";
                                                            }
                                                        },
                                                        currentDay.dayText)) {
                                                    currentDay.cssClass = ' vacationOverview-day-sick-note vactionOverview-day-item ';
                                                }
                                                if (response.response.absences
                                                    .find(
                                                        function (
                                                            currentValue,
                                                            index,
                                                            array) {
                                                            if (this.toString() == currentValue.date
                                                                && currentValue.type === 'SICK_NOTE'
                                                                && currentValue.dayLength < 1) {
                                                                return "testdatacreator";
                                                            }
                                                        },
                                                        currentDay.dayText)) {
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
                                function (item, index, array) {
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
                                function (item, index, array) {
                                    outputTable += "<tr>";
                                    outputTable += "<td class='hidden-xs'>"
                                        + item.person.firstName
                                        + "</td>";
                                    outputTable += "<td class='hidden-xs'>"
                                        + item.person.lastName
                                        + "</td>";
                                    item.days
                                        .forEach(
                                            function (dayItem,
                                                      dayIndex,
                                                      dayArray) {
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
                        var element = document.getElementById("vacationOverview");
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

            var selectedYear = document.getElementById('yearSelect');
            var selectedMonth = document.getElementById('monthSelect');
            var selectedDepartment = document.getElementById('departmentSelect');

            selectedYear.addEventListener("change", function () {
                selectedItemChange();
            });
            selectedMonth.addEventListener("change", function () {
                selectedItemChange();
            });
            selectedDepartment.addEventListener("change", function () {
                selectedItemChange();
            });
            if (typeof (Event) === "function") {
                var event = new Event("change");
            } else {
                var event = document.createEvent("Event");
                event.initEvent("change", true, true);
            }
            selectedYear.dispatchEvent(event);
        }
    );
</script>
