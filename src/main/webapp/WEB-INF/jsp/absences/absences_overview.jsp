<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="asset" uri="/WEB-INF/asset.tld" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>

<%@ page trimDirectiveWhitespaces="true" %>

<!DOCTYPE html>
<html lang="${language}">

<head>
    <title>
        <spring:message code="absences.overview.header.title"/>
    </title>
    <link rel="stylesheet" href="<asset:url value='absences_overview.css' />"/>
    <uv:custom-head/>
    <script defer src="<asset:url value='npm.tablesorter.js' />"></script>
    <script defer src="<asset:url value='absences_overview.js' />"></script>
</head>

<body>
<spring:url var="URL_PREFIX" value="/web"/>

<uv:menu/>

<div class="content">
    <div class="container">
        <uv:section-heading>
            <jsp:attribute name="actions">
                <uv:print/>
            </jsp:attribute>
            <jsp:body>
                <h1 id="absence">
                    <spring:message code="absences.overview.title"/>
                </h1>
            </jsp:body>
        </uv:section-heading>

        <form:form method="GET" action="${URL_PREFIX}/absences" id="absenceOverviewForm" class="print:tw-hidden">
            <div class="col-md-12">
                <div class="form-group">
                    <div class="row">
                        <label class="control-label col-md-1" for="yearSelect">
                            <spring:message code="absences.overview.year"/>:
                        </label>
                        <div class="col-md-4">
                            <uv:select id="yearSelect" name="year">
                                <c:forEach var="i" begin="1" end="9">
                                    <option value="${currentYear - 10 + i}" ${(currentYear - 10 + i) == selectedYear ? 'selected="selected"' : ''}>
                                        <c:out value="${currentYear - 10 + i}"/>
                                    </option>
                                </c:forEach>
                                <option value="${currentYear}" ${currentYear == selectedYear ? 'selected="selected"' : ''}>
                                    <c:out value="${currentYear}"/>
                                </option>
                                <option value="${currentYear + 1}" ${(currentYear + 1) == selectedYear ? 'selected="selected"' : ''}>
                                    <c:out value="${currentYear + 1}"/>
                                </option>
                            </uv:select>
                        </div>
                    </div>
                </div>
                <div class="form-group">
                    <div class="row">
                        <label class="control-label col-md-1" for="monthSelect">
                            <spring:message code="absences.overview.month"/>:
                        </label>
                        <div class="col-md-4">
                            <uv:select id="monthSelect" name="month">
                                <option value="" ${selectedMonth == '' ? 'selected="selected"' : ''}>
                                    <spring:message code="month.all"/>
                                </option>
                                <option disabled>──────────</option>
                                <c:forEach var="i" begin="1" end="12">
                                    <option value="${i}" ${i == selectedMonth ? 'selected="selected"' : ''}>
                                        <c:choose>
                                            <c:when test="${i == 1}"><spring:message code="month.january"/></c:when>
                                            <c:when test="${i == 2}"><spring:message code="month.february"/></c:when>
                                            <c:when test="${i == 3}"><spring:message code="month.march"/></c:when>
                                            <c:when test="${i == 4}"><spring:message code="month.april"/></c:when>
                                            <c:when test="${i == 5}"><spring:message code="month.may"/></c:when>
                                            <c:when test="${i == 6}"><spring:message code="month.june"/></c:when>
                                            <c:when test="${i == 7}"><spring:message code="month.july"/></c:when>
                                            <c:when test="${i == 8}"><spring:message code="month.august"/></c:when>
                                            <c:when test="${i == 9}"><spring:message code="month.september"/></c:when>
                                            <c:when test="${i == 10}"><spring:message code="month.october"/></c:when>
                                            <c:when test="${i == 11}"><spring:message code="month.november"/></c:when>
                                            <c:when test="${i == 12}"><spring:message code="month.december"/></c:when>
                                        </c:choose>
                                    </option>
                                </c:forEach>
                            </uv:select>
                        </div>
                    </div>
                </div>
                <c:if test="${not empty visibleDepartments}">
                    <div class="form-group">
                        <div class="row">
                            <label class="control-label col-md-1" for="departmentSelect">
                                <spring:message code="absences.overview.department"/>:
                            </label>
                            <div class="col-md-4">
                                <uv:multi-select id="departmentSelect" name="department">
                                    <c:forEach items="${visibleDepartments}" var="department">
                                        <uv:multi-select-item value="${department.name}"
                                                              selected="${selectedDepartments.contains(department.name)}">
                                            <c:out value="${department.name}"/>
                                        </uv:multi-select-item>
                                    </c:forEach>
                                </uv:multi-select>
                            </div>
                            <div class="col-md-5 hidden-xs">
                                <span class="help-block tw-text-sm">
                                    <icon:information-circle className="tw-w-4 tw-h-4" solid="true"/>
                                    <spring:message code="absences.overview.department.help"/>
                                </span>
                            </div>
                        </div>
                    </div>
                </c:if>
            </div>
        </form:form>
    </div>

    <div class="tw-mx-auto tw-px-4 tw-min-w-max xl:tw-max-w-max">
        <hr class="print:tw-hidden"/>
        <c:forEach items="${absenceOverview.months}" var="month">
            <div class="tw-mb-10 print:tw-break-inside-avoid">
                <h2
                    id="absence-table-${month.nameOfMonth}"
                    class="tw-text-2xl tw-m-0 tw-mb-5 print:tw-mb-1 ${fn:length(absenceOverview.months) == 1 ? 'tw-hidden print:tw-block' : ''}"
                >
                    <c:out value="${month.nameOfMonth}"/>
                    <span class="hidden print:tw-inline"> <c:out value="${selectedYear}"/></span>
                </h2>
                <table
                    id="absence-table"
                    class="vacationOverview-table tw-text-sm"
                    role="grid"
                    aria-describedby="absence-table-${month.nameOfMonth}"
                >
                    <thead>
                        <tr>
                            <th scope="col" class="print:tw-hidden tw-cursor-default tw-p-2">&nbsp;</th>
                            <th scope="col" class="sortable-field tw-cursor-pointer tw-p-2">&nbsp;</th>
                            <c:forEach items="${month.days}" var="day">
                                <th
                                    scope="col"
                                    class="non-sortable tw-cursor-default text-zinc-700 vacationOverview-cal-head ${day.today ? ' today' : ''}"
                                    style="${day.today ? '--vacation-overview-rows: '.concat(month.persons.size()) : ''}"
                                >
                                    <div class="tw-p-2 cal-day ${day.weekend ? 'weekend' : 'tw-bg-transparent'}
                                            ${(day.type.publicHolidayFull) ? ' public-holiday-full' : ''}
                                            ${(day.type.publicHolidayMorning) ? ' public-holiday-morning' : ''}
                                            ${(day.type.publicHolidayNoon) ? ' public-holiday-noon' : ''}"
                                    >
                                        <c:out value="${day.dayOfMonth}"/>
                                    </div>
                                </th>
                            </c:forEach>
                        </tr>
                    </thead>
                    <tbody class="vacationOverview-tbody">
                    <c:forEach var="person" items="${month.persons}">
                        <tr role="row">
                            <th
                                scope="row"
                                class="tw-p-0.5 print:tw-hidden tw-sticky tw-left-0 tw-bg-gradient-to-r tw-from-white dark:tw-from-neutral-900 tw-border-l-0 tw-z-10"
                            >
                                <uv:avatar
                                    url="${person.gravatarUrl}?d=mm&s=32"
                                    username="${person.firstName} ${person.lastName}"
                                    width="32px"
                                    height="32px"
                                    border="true"
                                />
                            </th>
                            <th
                                scope="row"
                                class="tw-py-0.5 tw-pl-2 tw-pr-4 print:tw-py-1.5"
                            >
                                <div class="tw-flex tw-flex-col tw-justify-center tw-leading-tight">
                                    <c:out value="${person.firstName}"/>&nbsp;
                                    <span><c:out value="${person.lastName}"/></span>
                                </div>
                            </th>
                            <c:forEach var="absence" items="${person.days}">
                                <td>
                                    <div class="cal-day
                                        ${(absence.weekend) ? ' weekend' : ''}
                                        ${(absence.type.absenceFull) ? ' absence-full' : ''}
                                        ${(absence.type.absenceMorning) ? ' absence-morning' : ''}
                                        ${(absence.type.absenceNoon) ? ' absence-noon' : ''}
                                        ${(absence.type.waitingVacationFull) ? ' vacation-full-waiting' : ''}
                                        ${(absence.type.waitingVacationMorning) ? ' vacation-morning-waiting' : ''}
                                        ${(absence.type.waitingVacationNoon) ? ' vacation-noon-waiting' : ''}
                                        ${(absence.type.allowedVacationFull) ? ' vacation-full-approved' : ''}
                                        ${(absence.type.allowedVacationMorning) ? ' vacation-morning-approved' : ''}
                                        ${(absence.type.allowedVacationNoon) ? ' vacation-noon-approved' : ''}
                                        ${(absence.type.sickNoteFull) ? ' sick-note-full' : ''}
                                        ${(absence.type.sickNoteMorning) ? ' sick-note-morning' : ''}
                                        ${(absence.type.sickNoteNoon) ? ' sick-note-noon' : ''}
                                        ${(absence.type.publicHolidayFull) ? ' public-holiday-full' : ''}
                                        ${(absence.type.publicHolidayMorning) ? ' public-holiday-morning' : ''}
                                        ${(absence.type.publicHolidayNoon) ? ' public-holiday-noon' : ''}
                                    ">
                                        <span class="tw-hidden print:tw-inline print:tw-font-mono">
                                            <c:if test="${absence.type.absenceMorning}">
                                                <spring:message code="absences.overview.absence.morning.abbr"/>
                                            </c:if>
                                            <c:if test="${absence.type.absenceNoon}">
                                                <spring:message code="absences.overview.absence.noon.abbr"/>
                                            </c:if>
                                            <c:if test="${absence.type.absenceFull}">
                                                <spring:message code="absences.overview.absence.abbr"/>
                                            </c:if>
                                            <c:if test="${absence.type.waitingVacationMorning}">
                                                <spring:message code="absences.overview.vacation.morning.abbr"/>
                                            </c:if>
                                            <c:if test="${absence.type.allowedVacationMorning}">
                                                <spring:message code="absences.overview.allowed.morning.abbr"/>
                                            </c:if>
                                            <c:if test="${absence.type.sickNoteMorning}">
                                                <spring:message code="absences.overview.sick.morning.abbr"/>
                                            </c:if>
                                            <c:if test="${absence.type.waitingVacationNoon}">
                                                <spring:message code="absences.overview.vacation.noon.abbr"/>
                                            </c:if>
                                            <c:if test="${absence.type.allowedVacationNoon}">
                                                <spring:message code="absences.overview.allowed.noon.abbr"/>
                                            </c:if>
                                            <c:if test="${absence.type.sickNoteNoon}">
                                                <spring:message code="absences.overview.sick.noon.abbr"/>
                                            </c:if>
                                            <c:if test="${absence.type.allowedVacationFull}">
                                                <spring:message code="absences.overview.allowed.abbr"/>
                                            </c:if>
                                            <c:if test="${absence.type.waitingVacationFull}">
                                                <spring:message code="absences.overview.vacation.abbr"/>
                                            </c:if>
                                            <c:if test="${absence.type.sickNoteFull}">
                                                <spring:message code="absences.overview.sick.abbr"/>
                                            </c:if>
                                        </span>
                                    </div>
                                </td>
                            </c:forEach>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:forEach>
        <div id="vacationOverviewLegend" class="tw-mb-8 print:tw-break-inside-avoid">
            <table aria-hidden="true" class="vacationOverview-legend-table tw-sticky tw-left-4 tw-text-sm print:tw-font-mono">
                <caption>
                    <spring:message code="absences.overview.legendTitle"/>
                </caption>
                <tbody>
                <tr>
                    <td class="vacationOverview-legend-colorbox vacationOverview-day-today-legend"></td>
                    <td class="vacationOverview-legend-text">
                        <spring:message code="absences.overview.today"/>
                    </td>
                </tr>
                <tr>
                    <td class="vacationOverview-legend-colorbox">
                        <div class="cal-day weekend"></div>
                    </td>
                    <td class="vacationOverview-legend-text">
                        <spring:message code="absences.overview.weekend"/>
                    </td>
                </tr>
                <c:choose>
                    <c:when test="${isPrivileged}">
                        <tr>
                            <td class="vacationOverview-legend-colorbox">
                                <div class="cal-day vacation-full-approved"></div>
                                <span class="tw-hidden print:tw-inline">
                                    <spring:message code="absences.overview.allowed.abbr"/>
                                </span>
                            </td>
                            <td class="vacationOverview-legend-text">
                                <spring:message code="absences.overview.allowed"/>
                            </td>
                        </tr>
                        <tr>
                            <td class="vacationOverview-legend-colorbox">
                                <div class="cal-day vacation-morning-approved"></div>
                                <span class="tw-hidden print:tw-inline">
                                    <spring:message code="absences.overview.allowed.morning.abbr"/>
                                </span>
                            </td>
                            <td class="vacationOverview-legend-text">
                                <spring:message code="absences.overview.allowed.morning"/>
                            </td>
                        </tr>
                        <tr>
                            <td class="vacationOverview-legend-colorbox">
                                <div class="cal-day vacation-noon-approved"></div>
                                <span class="tw-hidden print:tw-inline">
                                    <spring:message code="absences.overview.allowed.noon.abbr"/>
                                </span>
                            </td>
                            <td class="vacationOverview-legend-text">
                                <spring:message code="absences.overview.allowed.noon"/>
                            </td>
                        </tr>
                        <tr>
                            <td class="vacationOverview-legend-colorbox">
                                <div class="cal-day vacation-full-waiting"></div>
                                <span class="tw-hidden print:tw-inline">
                                    <spring:message code="absences.overview.vacation.abbr"/>
                                </span>
                            </td>
                            <td class="vacationOverview-legend-text">
                                <spring:message code="absences.overview.vacation"/>
                            </td>
                        </tr>
                        <tr>
                            <td class="vacationOverview-legend-colorbox">
                                <div class="cal-day vacation-morning-waiting"></div>
                                <span class="tw-hidden print:tw-inline">
                                    <spring:message code="absences.overview.vacation.morning.abbr"/>
                                </span>
                            </td>
                            <td class="vacationOverview-legend-text">
                                <spring:message code="absences.overview.vacation.morning"/>
                            </td>
                        </tr>
                        <tr>
                            <td class="vacationOverview-legend-colorbox">
                                <div class="cal-day vacation-noon-waiting"></div>
                                <span class="tw-hidden print:tw-inline">
                                    <spring:message code="absences.overview.vacation.noon.abbr"/>
                                </span>
                            </td>
                            <td class="vacationOverview-legend-text">
                                <spring:message code="absences.overview.vacation.noon"/>
                            </td>
                        </tr>
                        <tr>
                            <td class="vacationOverview-legend-colorbox">
                                <div class="cal-day sick-note-full"></div>
                                <span class="tw-hidden print:tw-inline">
                                    <spring:message code="absences.overview.sick.abbr"/>
                                </span>
                            </td>
                            <td class="vacationOverview-legend-text">
                                <spring:message code="absences.overview.sick"/>
                            </td>
                        </tr>
                        <tr>
                            <td class="vacationOverview-legend-colorbox">
                                <div class="cal-day sick-note-morning"></div>
                                <span class="tw-hidden print:tw-inline">
                                    <spring:message code="absences.overview.sick.morning.abbr"/>
                                </span>
                            </td>
                            <td class="vacationOverview-legend-text">
                                <spring:message code="absences.overview.sick.morning"/>
                            </td>
                        </tr>
                        <tr>
                            <td class="vacationOverview-legend-colorbox">
                                <div class="cal-day sick-note-noon"></div>
                                <span class="tw-hidden print:tw-inline">
                                    <spring:message code="absences.overview.sick.noon.abbr"/>
                                </span>
                            </td>
                            <td class="vacationOverview-legend-text">
                                <spring:message code="absences.overview.sick.noon"/>
                            </td>
                        </tr>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td class="vacationOverview-legend-colorbox">
                                <div class="cal-day absence-full"></div>
                                <span class="tw-hidden print:tw-inline">
                                    <spring:message code="absences.overview.absence.abbr"/>
                                </span>
                            </td>
                            <td class="vacationOverview-legend-text">
                                <spring:message code="absences.overview.absence"/>
                            </td>
                        </tr>
                        <tr>
                            <td class="vacationOverview-legend-colorbox">
                                <div class="cal-day absence-morning"></div>
                                <span class="tw-hidden print:tw-inline">
                                    <spring:message code="absences.overview.absence.morning.abbr"/>
                                </span>
                            </td>
                            <td class="vacationOverview-legend-text">
                                <spring:message code="absences.overview.absence.morning"/>
                            </td>
                        </tr>
                        <tr>
                            <td class="vacationOverview-legend-colorbox">
                                <div class="cal-day absence-noon"></div>
                                <span class="tw-hidden print:tw-inline">
                                    <spring:message code="absences.overview.absence.noon.abbr"/>
                                </span>
                            </td>
                            <td class="vacationOverview-legend-text">
                                <spring:message code="absences.overview.absence.noon"/>
                            </td>
                        </tr>
                    </c:otherwise>
                </c:choose>
                </tbody>
            </table>
        </div>
    </div>
</div>
</body>
</html>
