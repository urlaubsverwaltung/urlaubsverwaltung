<script>

	//https://tc39.github.io/ecma262/#sec-array.prototype.find
	if (!Array.prototype.find) {
	  Object.defineProperty(Array.prototype, 'find', {
	    value: function(predicate) {
	     // 1. Let O be ? ToObject(this value).
	      if (this == null) {
	        throw new TypeError('"this" is null or not defined');
	      }
	
	      var o = Object(this);
	
	      // 2. Let len be ? ToLength(? Get(O, "length")).
	      var len = o.length >>> 0;
	
	      // 3. If IsCallable(predicate) is false, throw a TypeError exception.
	      if (typeof predicate !== 'function') {
	        throw new TypeError('predicate must be a function');
	      }
	
	      // 4. If thisArg was supplied, let T be thisArg; else let T be undefined.
	      var thisArg = arguments[1];
	
	      // 5. Let k be 0.
	      var k = 0;
	
	      // 6. Repeat, while k < len
	      while (k < len) {
	        // a. Let Pk be ! ToString(k).
	        // b. Let kValue be ? Get(O, Pk).
	        // c. Let testResult be ToBoolean(? Call(predicate, T, « kValue, k, O »)).
	        // d. If testResult is true, return kValue.
	        var kValue = o[k];
	        if (predicate.call(thisArg, kValue, k, o)) {
	          return kValue;
	        }
	        // e. Increase k by 1.
	        k++;
	      }
	
	      // 7. Return undefined.
	      return undefined;
	    }
	  });
	}
	
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
		if (typeof(Event) === "'function") {
			var event = new Event("change");
	    } else {
	        var event = document.createEvent("Event");
	        event.initEvent("change", true, true);
	    }
		selectedYear.dispatchEvent(event);
	}

	);
</script>