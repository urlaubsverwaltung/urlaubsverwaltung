<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="sec"
	uri="http://www.springframework.org/security/tags"%>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags"%>

<sec:authorize access="hasAuthority('USER')">
	<c:set var="IS_USER" value="${true}" />
</sec:authorize>

<sec:authorize access="hasAuthority('BOSS')">
	<c:set var="IS_BOSS" value="${true}" />
</sec:authorize>
<sec:authorize access="hasAuthority('DEPARTMENT_HEAD')">
	<c:set var="IS_DEPARTMENT_HEAD" value="${true}" />
</sec:authorize>
<sec:authorize access="hasAuthority('OFFICE')">
	<c:set var="IS_OFFICE" value="${true}" />
</sec:authorize>
<c:set var="IS_ALLOWED" value="${IS_BOSS || IS_USER || IS_DEPARTNER }" />
<html>

<head>
<uv:head />

</head>

<body>
	<spring:url var="URL_PREFIX" value="/web" />

	<sec:authorize access="hasAuthority('OFFICE')">
		<c:set var="IS_OFFICE" value="true" />
	</sec:authorize>

	<uv:menu />

	<div class="content print--only-portrait">
		<div class="container">
		
			<c:if test="${IS_ALLOWED}">
				<div class="row">
					<div class="col-xs-12">
						<legend id="vacation"><spring:message code="overview.vacationOverview.title" /></legend>
					</div>
				</div>
				<script>
					$(function() {

						function selectedItemChange() {
							var selectedYear = document
									.getElementById('yearSelect');
							var selectedMonth = document
									.getElementById('monthSelect');
							var selectedDepartment = document
									.getElementById('departmentSelect');
							var selectedDepartmentValue = selectedDepartment.options[selectedDepartment.selectedIndex].text;
							var selectedYearValue = selectedYear.options[selectedYear.selectedIndex].text;
							var selectedMonthValue = selectedMonth.options[selectedMonth.selectedIndex].text;
							if (selectedYearValue != null
									&& selectedMonthValue != null
									&& selectedDepartmentValue != null) {
								var url = location.protocol + "//"
										+ location.host
										+ "/api/vacationoverview?selectedYear="
										+ selectedYearValue + "&selectedMonth="
										+ selectedMonthValue
										+ "&selectedDepartment="
										+ selectedDepartmentValue;

								var xhttp = new XMLHttpRequest();
								xhttp.open("GET", url, false);
								xhttp.setRequestHeader("Content-type",
										"application/json");
								xhttp.send();
								var holyDayOverviewResponse = JSON
										.parse(xhttp.responseText);
								if (holyDayOverviewResponse != null
										&& holyDayOverviewResponse != undefined
										&& holyDayOverviewResponse.response != null
										&& holyDayOverviewResponse.response != undefined) {

									var overViewList = holyDayOverviewResponse.response.list;
									overViewList
											.forEach(function(listItem, index,
													arr) {
												var personId = listItem.personID;
												var personFullName = listItem.person.niceName;
												var url = location.protocol
														+ "//" + location.host
														+ "/api/absences?year="
														+ selectedYearValue
														+ "&month="
														+ selectedMonthValue
														+ "&person=" + personId;
												var xhttp = new XMLHttpRequest();
												xhttp.open("GET", url, false);
												xhttp.setRequestHeader(
														"Content-type",
														"application/json");
												xhttp.send();
												var response = JSON
														.parse(xhttp.responseText);
												if (response != null
														&& response != undefined) {

													listItem.days
															.forEach(
																	function(
																			cV,
																			index,
																			arr) {
																			if (response.response.absences
																					.find(
																							function(
																									currentValue,
																									index,
																									arr) {
																								if (this == currentValue.date
																										&& currentValue.status === "WAITING"
																										&& currentValue.type === "VACATION"
																										&& currentValue.dayLength === 1) {
																									return "test";
																								}
																							},
																							cV.day)) {
																				cV.cssClass = 'vacationOverview-day-personal-holiday-status-WAITING ';
																			}
																			if (response.response.absences
																					.find(
																							function(
																									currentValue,
																									index,
																									arr) {
																								if (this == currentValue.date
																										&& currentValue.status === "WAITING"
																										&& currentValue.type === "VACATION"
																										&& currentValue.dayLength < 1) {
																									return "test";
																								}
																							},
																							cV.day)) {
																				cV.cssClass = ' vacationOverview-day-personal-holiday-half-day-status-WAITING ';
																			}

																			if (response.response.absences
																					.find(
																							function(
																									currentValue,
																									index,
																									arr) {
																								if (this == currentValue.date
																										&& currentValue.status === "ALLOWED"
																										&& currentValue.dayLength < 1
																										&& currentValue.type === "VACATION") {
																									return "test";
																								}
																							},
																							cV.day)) {
																				cV.cssClass = ' vacationOverview-day-personal-holiday-half-day-status-ALLOWED ';
																			}
																			if (response.response.absences
																					.find(
																							function(
																									currentValue,
																									index,
																									arr) {
																								if (this == currentValue.date
																										&& currentValue.status === "ALLOWED"
																										&& currentValue.dayLength === 1
																										&& currentValue.type === "VACATION") {
																									return "test";
																								}
																							},
																							cV.day)) {
																				cV.cssClass = ' vacationOverview-day-personal-holiday-status-ALLOWED ';
																			}
																			if (response.response.absences
																					.find(
																							function(
																									currentValue,
																									index,
																									arr) {
																								if (this == currentValue.date
																										&& currentValue.type === 'SICK_NOTE'
																											&& currentValue.dayLength === 1) {
																									return "test";
																								}
																							},
																							cV.day)) {
																				cV.cssClass = ' vacationOverview-day-sick-note ';
																			}
																			if (response.response.absences
																					.find(
																							function(
																									currentValue,
																									index,
																									arr) {
																								if (this == currentValue.date
																										&& currentValue.type === 'SICK_NOTE'
																											&& currentValue.dayLength < 1) {
																									return "test";
																								}
																							},
																							cV.day)) {
																				cV.cssClass = ' vacationOverview-day-sick-note-half-day ';
																			}
																	}, this);
												}
											});

									//--------------------------------------------------------------------- generate Outout;

									var outputTable = "<table cellspacing='0' class='list-table selectable-table sortable tablesorter'>";
									outputTable += "<thead class='hidden-xs hidden-sm'>";
									outputTable += "<tr><th><spring:message code='overview.vacationOverview.tableTitle' /></th>";
									overViewList[0].days
											.forEach(
													function(item, index, arr) {
														if (item.colorCode != "") {
															outputTable += "<th style='background-color: " + item.colorCode + "'>"
																	+ item.intValue
																	+ "</th>";
														} else {
															outputTable += "<th>"
																	+ item.intValue
																	+ "</th>";
														}
													}, outputTable);
									outputTable += "</tr></thead><tbody class='list'>";
									overViewList
											.forEach(
													function(item, index, arr) {
														outputTable += "<tr><td>"
																+ item.person.niceName
																+ "</td>";
														item.days
																.forEach(
																		function(
																				dayItem,
																				idx,
																				dayarr) {
																			outputTable += "<td class='" + dayItem.cssClass + "'></td>";
																		},
																		outputTable);
														outputTable += "</tr>";
													}, outputTable);

									outputTable += "</tbody></table>";
									var element = document
											.getElementById("vacationOverview");
									element.innerHTML = outputTable
								}
							}
						}
						var selectedYear = document
								.getElementById('yearSelect');
						var selectedMonth = document
								.getElementById('monthSelect');
						var selectedDepartment = document
								.getElementById('departmentSelect');
						selectedYear.addEventListener("change", function() {
							selectedItemChange();
						});
						selectedMonth.addEventListener("change", function() {
							selectedItemChange();
						});
						selectedDepartment.addEventListener("change",
								function() {
									selectedItemChange();
								});
						var event = new Event("change");
						selectedYear.dispatchEvent(event);
					}

					);
				</script>
				<table cellspacing='0' width="300px">
					<thead class='hidden-xs hidden-sm'>
						<tr>
							<th><spring:message code="overview.vacationOverview.legendTitle" /></th>
						<tr>
					</thead>
					<tbody>
						<tr>
							<td class='vacationOverview-day-weekend'><spring:message code="overview.vacationOverview.weekend" /></td>
						</tr>
						<tr>
							<td class='vacationOverview-day-personal-holiday-status-ALLOWED'><spring:message code="overview.vacationOverview.allowed" /></td>
						</tr>
						<tr>
							<td class='vacationOverview-day-personal-holiday-status-WAITING'><spring:message code="overview.vacationOverview.vacation" /></td>
						</tr>
						<tr>
							<td class='vacationOverview-day-sick-note'><spring:message code="overview.vacationOverview.sick" /></td>
						</tr>

					</tbody>
				</table>
				<p style='margin-top: 1cm; margin-bottom: 1cm;' />
				<select id="yearSelect" name="yearSelect" size="1" path="">
					<option value="${currentYear}" selected="${currentYear}">
						<c:out value="${currentYear}" />
					</option>
					<option value="${currentYear +1}">
						<c:out value="${currentYear +1}" />
					</option>
					<c:forEach var="i" begin="1" end="10">
						<option value="${currentYear - i}">
							<c:out value="${currentYear - i}" />
						</option>
					</c:forEach>
				</select>
				<select id="monthSelect" name="monthSelect" size="1" path="">
					<c:forEach var="i" begin="1" end="12">
						<c:if test="${currentMonth == i }">
							<option value="${i}" selected="${i}">
								<c:out value="${i}" />
							</option>

						</c:if>
						<c:if test="${currentMonth != i }">
							<option value="${i}">
								<c:out value="${i}" />
							</option>
						</c:if>
					</c:forEach>
				</select>
				<select id="departmentSelect" name="departmentSelect" size="1"
					path="">
					<c:forEach items="${departments}" var="department">
						<option value="${department}">
							<c:out value="${department.name}" />
						</option>
					</c:forEach>
				</select>
				<div id="vacationOverview"></div>
			</c:if>
		</div>
		
	</div>

</body>