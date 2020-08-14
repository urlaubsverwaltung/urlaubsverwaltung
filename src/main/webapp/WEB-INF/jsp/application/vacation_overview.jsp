<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="sec"
          uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="asset" uri = "/WEB-INF/asset.tld"%>

<%@ page trimDirectiveWhitespaces="true" %>

<sec:authorize access="hasAuthority('USER')">
    <c:set var="IS_USER" value="${true}"/>
</sec:authorize>
<sec:authorize access="hasAuthority('BOSS')">
    <c:set var="IS_BOSS" value="${true}"/>
</sec:authorize>
<sec:authorize access="hasAuthority('DEPARTMENT_HEAD')">
    <c:set var="IS_DEPARTMENT_HEAD" value="${true}"/>
</sec:authorize>
<sec:authorize access="hasAuthority('OFFICE')">
    <c:set var="IS_OFFICE" value="${true}"/>
</sec:authorize>
<c:set var="IS_ALLOWED" value="${IS_USER || IS_BOSS || IS_DEPARTMENT_HEAD || IS_OFFICE }"/>

<!DOCTYPE html>
<html lang="${language}">

<head>
    <title>
        <spring:message code="overview.vacationOverview.header.title"/>
    </title>
    <link rel="stylesheet" href="<asset:url value='vacation_overview.css' />" />
    <uv:custom-head/>
    <script defer src="<asset:url value='npm.tablesorter.js' />"></script>
    <script defer src="<asset:url value='vacation_overview.js' />"></script>
</head>

<body>
<spring:url var="URL_PREFIX" value="/web"/>

<sec:authorize access="hasAuthority('OFFICE')">
    <c:set var="IS_OFFICE" value="true"/>
</sec:authorize>

<uv:menu/>

<div class="content print--only-portrait">
    <div class="container">

        <c:if test="${IS_ALLOWED}">

            <div class="row">
                <div class="col-xs-12">
                    <legend id="vacation">
                        <spring:message code="overview.vacationOverview.title"/>
                    </legend>
                </div>
            </div>

            <form method="GET" action="${URL_PREFIX}/application/vacationoverview" id="absenceOverviewForm">
                <div class="col-md-8">
                    <div class="form-group">
                        <div class="row">
                            <label class="control-label col-md-3" for="yearSelect">
                                <spring:message code="overview.vacationOverview.year"/>:
                            </label>
                            <div class="col-md-6">
                                <select id="yearSelect" name="year" size="1" class="form-control">
                                    <c:forEach var="i" begin="1" end="9">
                                        <option value="${currentYear - 10 + i}" ${(currentYear - 10 + i) == selectedYear ? 'selected="selected"' : ''}">
                                            <c:out value="${currentYear - 10 + i}"/>
                                        </option>
                                    </c:forEach>
                                    <option value="${currentYear}" ${currentYear == selectedYear ? 'selected="selected"' : ''}">
                                        <c:out value="${currentYear}"/>
                                    </option>
                                    <option value="${currentYear + 1}" ${(currentYear + 1) == selectedYear ? 'selected="selected"' : ''}">
                                        <c:out value="${currentYear + 1}"/>
                                    </option>
                                </select>
                            </div>
                        </div>
                    </div>
                    <div class="form-group">
                        <div class="row">
                            <label class="control-label col-md-3" for="monthSelect">
                                <spring:message code="overview.vacationOverview.month"/>:
                            </label>
                            <div class="col-md-6">
                                <select id="monthSelect" name="month" size="1" class="form-control">
                                    <option value="" ${selectedMonth == '' ? 'selected="selected"' : ''}">
                                        <spring:message code="month.all"/>
                                    </option>
                                    <option disabled>──────────</option>
                                    <c:forEach var="i" begin="1" end="12">
                                    <option value="${i}" ${i == selectedMonth ? 'selected="selected"' : ''}">
                                        <spring:eval var="monthName" expression="T(org.synyx.urlaubsverwaltung.util.DateUtil).getMonthName(i)" />
                                        <c:out value="${monthName}"/>
                                    </option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                    </div>
                    <div class="form-group">
                        <div class="row">
                            <label class="control-label col-md-3" for="departmentSelect">
                                <spring:message code="overview.vacationOverview.department"/>:
                            </label>
                            <div class="col-md-6">
                                <select id="departmentSelect" name="department" size="1" class="form-control">
                                    <c:forEach items="${departments}" var="department">
                                        <option value="${department.name}" ${department.name == selectedDepartment ? 'selected="selected"' : ''}>
                                            <c:out value="${department.name}"/>
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                    </div>
                </div>
            </form>

            <div class="row">
                <div class="col-xs-12">
                    <hr/>

                    <c:forEach items="${absenceOverview.months}" var="month">
                        <p id="absence-table-${month.nameOfMonth}" class="text-xl ${fn:length(absenceOverview.months) == 1 ? 'hidden' : ''}">
                            <span class="hidden">Abwesenheiten&nbsp;</span><c:out value="${month.nameOfMonth}" />
                        </p>
                        <table class="sortable vacationOverview-table mb-8" role="grid" aria-describedby="absence-table-${month.nameOfMonth}">
                            <thead>
                                <tr>
                                    <th scope="col" class="sortable-field">&nbsp;</th>
                                    <th scope="col" class="sortable-field">&nbsp;</th>
                                    <c:forEach items="${month.days}" var="day">
                                    <th scope="col" class="non-sortable text-gray-700 ${day.weekend ? 'vacationOverview-day-weekend' : ''}">
                                        <c:out value="${day.dayOfMonth}"/>
                                    </th>
                                    </c:forEach>
                                </tr>
                            </thead>
                            <tbody class="vacationOverview-tbody">
                                <c:forEach var="person" items="${month.persons}">
                                <tr role="row">
                                    <th><c:out value="${person.firstName}"/></th>
                                    <th><c:out value="${person.lastName}"/></th>
                                    <c:forEach var="absence" items="${person.days}">
                                        <td class="vacationOverview-day ${(absence.type eq '') ? 'vacationOverview-day-item' : ''}
                                                ${(absence.type eq 'waitingVacationFull') ? ' vacationOverview-day-personal-holiday-status-WAITING' : ''}
                                                ${(absence.type eq 'waitingVacationMorning') ? ' vacationOverview-day-personal-holiday-half-day-status-WAITING-morning' : ''}
                                                ${(absence.type eq 'waitingVacationNoon') ? ' vacationOverview-day-personal-holiday-half-day-status-WAITING-noon' : ''}
                                                ${(absence.type eq 'allowedVacationFull') ? ' vacationOverview-day-personal-holiday-status-ALLOWED' : ''}
                                                ${(absence.type eq 'allowedVacationMorning') ? ' vacationOverview-day-personal-holiday-half-day-status-ALLOWED-morning' : ''}
                                                ${(absence.type eq 'allowedVacationNoon') ? ' vacationOverview-day-personal-holiday-half-day-status-ALLOWED-noon' : ''}
                                                ${(absence.type eq 'activeSickNoteFull') ? ' vacationOverview-day-sick-note' : ''}
                                                ${(absence.type eq 'activeSickNoteMorning') ? ' vacationOverview-day-sick-note-half-day-morning' : ''}
                                                ${(absence.type eq 'activeSickNoteNoon') ? ' vacationOverview-day-sick-note-half-day-noon' : ''}
                                                ${(absence.weekend) ? ' vacationOverview-day-weekend' : ''}"
                                        ></td>
                                    </c:forEach>
                                </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </c:forEach>
                </div>
            </div>

            <div id="vacationOverviewLegend" class="row">
                <label class="col-md-1">
                    <spring:message code="overview.vacationOverview.legendTitle"/>
                </label>
                <div class="col-md-3">
                    <table>
                        <tr>
                            <td class='vacationOverview-legend-colorbox vacationOverview-day-weekend'></td>
                            <td class='vacationOverview-legend-text'><spring:message
                                code="overview.vacationOverview.weekend"/></td>
                        </tr>
                        <tr>
                            <td class='vacationOverview-legend-colorbox vacationOverview-day-personal-holiday-status-ALLOWED'></td>
                            <td class='vacationOverview-legend-text'><spring:message
                                code="overview.vacationOverview.allowed"/></td>
                        </tr>
                        <tr>
                            <td class='vacationOverview-legend-colorbox vacationOverview-day-personal-holiday-status-WAITING'></td>
                            <td class='vacationOverview-legend-text'><spring:message
                                code="overview.vacationOverview.vacation"/></td>
                        </tr>
                        <tr>
                            <td class='vacationOverview-legend-colorbox vacationOverview-day-sick-note'></td>
                            <td class='vacationOverview-legend-text'><spring:message
                                code="overview.vacationOverview.sick"/></td>
                        </tr>
                    </table>
                </div>
            </div>

        </c:if>
    </div>

</div>

</body>
