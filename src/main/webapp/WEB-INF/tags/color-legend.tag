<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>

<%@attribute name="isPrivileged" type="java.lang.Boolean" required="true"%>
<%@attribute name="hiddenOnLoad" type="java.lang.String" required="true" %>
<%@attribute name="cssClass" type="java.lang.String" required="false" %>
<%@attribute name="legendBodyCssClass" type="java.lang.String" required="false" %>

<c:set var="hidden" value="${hiddenOnLoad ? 'tw-hidden' : ''}" />

<div id="color-legend" class="print:tw-no-break-inside ${cssClass}">
    <table aria-hidden="true" class="tw-text-sm print:tw-font-mono ${legendBodyCssClass}">
        <caption>
            <c:choose>
                <c:when test="${hiddenOnLoad}">
                <span class="tw-w-5 tw-flex tw-items-center">
                    <icon:eye className="tw-w-4 tw-h-4" solid="true" />
                </span>
                </c:when>
                <c:otherwise>
                    <icon:eye-off className="tw-w-4 tw-h-4" solid="true" />
                </c:otherwise>
            </c:choose>
            &nbsp;<spring:message code="absences.overview.legendTitle"/>
        </caption>
        <tbody class="${hidden}">
        <tr>
            <td class='vacationOverview-legend-colorbox vacationOverview-day-today-legend'></td>
            <td class='vacationOverview-legend-text'>
                <spring:message code="absences.overview.today"/>
            </td>
        </tr>
        <tr>
            <td class='vacationOverview-legend-colorbox weekend'></td>
            <td class='vacationOverview-legend-text'>
                <spring:message code="absences.overview.weekend"/>
            </td>
        </tr>
        <c:choose>
            <c:when test="${isPrivileged}">
                <tr>
                    <td class='vacationOverview-legend-colorbox vacation-full-approved'>
                        <span class="tw-hidden print:tw-inline">
                            <spring:message code="absences.overview.allowed.abbr"/>
                        </span>
                    </td>
                    <td class='vacationOverview-legend-text'>
                        <spring:message code="absences.overview.allowed"/>
                    </td>
                </tr>
                <tr>
                    <td class='vacationOverview-legend-colorbox vacation-morning-approved'>
                        <span class="tw-hidden print:tw-inline">
                            <spring:message code="absences.overview.allowed.morning.abbr"/>
                        </span>
                    </td>
                    <td class='vacationOverview-legend-text'>
                        <spring:message code="absences.overview.allowed.morning"/>
                    </td>
                </tr>
                <tr>
                    <td class='vacationOverview-legend-colorbox vacation-noon-approved'>
                        <span class="tw-hidden print:tw-inline">
                            <spring:message code="absences.overview.allowed.noon.abbr"/>
                        </span>
                    </td>
                    <td class='vacationOverview-legend-text'>
                        <spring:message code="absences.overview.allowed.noon"/>
                    </td>
                </tr>
                <tr>
                    <td class='vacationOverview-legend-colorbox vacation-full-waiting'>
                        <span class="tw-hidden print:tw-inline">
                            <spring:message code="absences.overview.vacation.abbr"/>
                        </span>
                    </td>
                    <td class='vacationOverview-legend-text'>
                        <spring:message code="absences.overview.vacation"/>
                    </td>
                </tr>
                <tr>
                    <td class='vacationOverview-legend-colorbox vacation-morning-waiting'>
                        <span class="tw-hidden print:tw-inline">
                            <spring:message code="absences.overview.vacation.morning.abbr"/>
                        </span>
                    </td>
                    <td class='vacationOverview-legend-text'>
                        <spring:message code="absences.overview.vacation.morning"/>
                    </td>
                </tr>
                <tr>
                    <td class='vacationOverview-legend-colorbox vacation-noon-waiting'>
                        <span class="tw-hidden print:tw-inline">
                            <spring:message code="absences.overview.vacation.noon.abbr"/>
                        </span>
                    </td>
                    <td class='vacationOverview-legend-text'>
                        <spring:message code="absences.overview.vacation.noon"/>
                    </td>
                </tr>
                <tr>
                    <td class='vacationOverview-legend-colorbox sick-note-full'>
                        <span class="tw-hidden print:tw-inline">
                            <spring:message code="absences.overview.sick.abbr"/>
                        </span>
                    </td>
                    <td class='vacationOverview-legend-text'>
                        <spring:message code="absences.overview.sick"/>
                    </td>
                </tr>
                <tr>
                    <td class='vacationOverview-legend-colorbox sick-note-morning'>
                        <span class="tw-hidden print:tw-inline">
                            <spring:message code="absences.overview.sick.morning.abbr"/>
                        </span>
                    </td>
                    <td class='vacationOverview-legend-text'>
                        <spring:message code="absences.overview.sick.morning"/>
                    </td>
                </tr>
                <tr>
                    <td class='vacationOverview-legend-colorbox sick-note-noon'>
                        <span class="tw-hidden print:tw-inline">
                            <spring:message code="absences.overview.sick.noon.abbr"/>
                        </span>
                    </td>
                    <td class='vacationOverview-legend-text'>
                        <spring:message code="absences.overview.sick.noon"/>
                    </td>
                </tr>
            </c:when>
            <c:otherwise>
                <tr>
                    <td class='vacationOverview-legend-colorbox absence-full'>
                        <span class="tw-hidden print:tw-inline">
                            <spring:message code="absences.overview.absence.abbr"/>
                        </span>
                    </td>
                    <td class='vacationOverview-legend-text'>
                        <spring:message code="absences.overview.absence"/>
                    </td>
                </tr>
                <tr>
                    <td class='vacationOverview-legend-colorbox absence-morning'>
                        <span class="tw-hidden print:tw-inline">
                            <spring:message code="absences.overview.absence.morning.abbr"/>
                        </span>
                    </td>
                    <td class='vacationOverview-legend-text'>
                        <spring:message code="absences.overview.absence.morning"/>
                    </td>
                </tr>
                <tr>
                    <td class='vacationOverview-legend-colorbox absence-noon'>
                        <span class="tw-hidden print:tw-inline">
                            <spring:message code="absences.overview.absence.noon.abbr"/>
                        </span>
                    </td>
                    <td class='vacationOverview-legend-text'>
                        <spring:message code="absences.overview.absence.noon"/>
                    </td>
                </tr>
            </c:otherwise>
        </c:choose>
        </tbody>
    </table>
</div>


