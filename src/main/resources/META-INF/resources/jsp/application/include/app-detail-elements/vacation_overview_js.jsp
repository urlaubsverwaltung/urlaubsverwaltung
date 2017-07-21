<script>
	$(function() {

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
						+ selectedYearValue + "&selectedMonth="
						+ selectedMonthValue + "&selectedDepartment="
						+ selectedDepartmentValue;

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
							.forEach(function(listItem, index, array) {
								var personId = listItem.personID;
								var personFullName = listItem.person.niceName;
								var url = location.protocol + "//"
										+ location.host + "/api/absences?year="
										+ selectedYearValue + "&month="
										+ selectedMonthValue + "&person="
										+ personId;
								var xhttp = new XMLHttpRequest();
								xhttp.open("GET", url, false);
								xhttp.setRequestHeader("Content-type",
										"application/json");
								xhttp.send();
								var response = JSON.parse(xhttp.responseText);
								if (response != null && response != undefined) {

									listItem.days
											.forEach(
													function(currentDay, index,
															array) {
														if (response.response.absences
																.find(
																		function(
																				currentValue,
																				index,
																				array) {
																			if (this.toString() == currentValue.date
																					&& currentValue.status === "WAITING"
																					&& currentValue.type === "VACATION"
																					&& currentValue.dayLength === 1) {
																				return "test";
																			}
																		},
																		currentDay.dayText)) {
															currentDay.cssClass = 'vacationOverview-day-personal-holiday-status-WAITING vactionOverview-day-item ';
														}
														if (response.response.absences
																.find(
																		function(
																				currentValue,
																				index,
																				array) {
																			if (this.toString() == currentValue.date
																					&& currentValue.status === "WAITING"
																					&& currentValue.type === "VACATION"
																					&& currentValue.dayLength < 1) {
																				return "test";
																			}
																		},
																		currentDay.dayText)) {
															currentDay.cssClass = ' vacationOverview-day-personal-holiday-half-day-status-WAITING vactionOverview-day-item ';
														}

														if (response.response.absences
																.find(
																		function(
																				currentValue,
																				index,
																				array) {
																			if (this.toString() == currentValue.date
																					&& currentValue.status === "ALLOWED"
																					&& currentValue.dayLength < 1
																					&& currentValue.type === "VACATION") {
																				return "test";
																			}
																		},
																		currentDay.dayText)) {
															currentDay.cssClass = ' vacationOverview-day-personal-holiday-half-day-status-ALLOWED vactionOverview-day-item ';
														}
														if (response.response.absences
																.find(
																		function(
																				currentValue,
																				index,
																				array) {
																			if (this.toString() == currentValue.date
																					&& currentValue.status === "ALLOWED"
																					&& currentValue.dayLength === 1
																					&& currentValue.type === "VACATION") {
																				return "test";
																			}
																		},
																		currentDay.dayText)) {
															currentDay.cssClass = ' vacationOverview-day-personal-holiday-status-ALLOWED vactionOverview-day-item ';
														}
														if (response.response.absences
																.find(
																		function(
																				currentValue,
																				index,
																				array) {
																			if (this.toString() == currentValue.date
																					&& currentValue.type === 'SICK_NOTE'
																					&& currentValue.dayLength === 1) {
																				return "test";
																			}
																		},
																		currentDay.dayText)) {
															currentDay.cssClass = ' vacationOverview-day-sick-note vactionOverview-day-item ';
														}
														if (response.response.absences
																.find(
																		function(
																				currentValue,
																				index,
																				array) {
																			if (this.toString() == currentValue.date
																					&& currentValue.type === 'SICK_NOTE'
																					&& currentValue.dayLength < 1) {
																				return "test";
																			}
																		},
																		currentDay.dayText)) {
															currentDay.cssClass = ' vacationOverview-day-sick-note-half-day vactionOverview-day-item ';
														}
													}, this);
								}
							});

					var outputTable = "<table cellspacing='0' class='list-table sortable tablesorter vacationOverview-table'>";
					outputTable += "<tr><th><spring:message code='overview.vacationOverview.tableTitle' /></th>";
					overViewList[0].days
							.forEach(
									function(item, index, array) {
										if (item.typeOfDay === "WEEKEND") {
											outputTable += "<th class='vacationOverview-day-weekend vactionOverview-day-item'>"
													+ item.dayNumber + "</th>";
										} else {
											outputTable += "<th class='vactionOverview-day-item'>"
													+ item.dayNumber + "</th>";
										}
									}, outputTable);
					outputTable += "</tr><tbody class='vacationOverview-tbody'>";
					overViewList
							.forEach(
									function(item, index, array) {
										outputTable += "<tr><td>"
												+ item.person.niceName
												+ "</td>";
										item.days
												.forEach(
														function(dayItem,
																dayIndex,
																dayArray) {
															if (dayItem.typeOfDay === "WEEKEND") {
																dayItem.cssClass = ' vacationOverview-day-weekend vactionOverview-day-item';
															} else {
																if (!dayItem.cssClass)
																{
																	dayItem.cssClass = ' vacationOverview-day vactionOverview-day-item ';
																};
															};
															outputTable += "<td class='" + dayItem.cssClass + "'></td>";
														}, outputTable);
										outputTable += "</tr>";
									}, outputTable);

					outputTable += "</tbody></table>";
					var element = document.getElementById("vacationOverview");
					element.innerHTML = outputTable;
				}
			}
		}
		var selectedYear = document.getElementById('yearSelect');
		var selectedMonth = document.getElementById('monthSelect');
		var selectedDepartment = document.getElementById('departmentSelect');
		selectedYear.addEventListener("change", function() {
			selectedItemChange();
		});
		selectedMonth.addEventListener("change", function() {
			selectedItemChange();
		});
		selectedDepartment.addEventListener("change", function() {
			selectedItemChange();
		});
		var event = new Event("change");
		selectedYear.dispatchEvent(event);
	}

	);
</script>