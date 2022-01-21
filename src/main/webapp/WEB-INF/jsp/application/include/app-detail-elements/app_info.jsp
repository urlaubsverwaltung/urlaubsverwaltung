<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>


<c:choose>
    <c:when test="${application.status == 'ALLOWED' && application.editedDate == null}">
        <c:set var="APPLIER_APPLIED">
            <spring:message code="application.applier.appliedDirectly"/>
        </c:set>
    </c:when>
    <c:otherwise>
        <c:set var="APPLIER_APPLIED">
            <spring:message code="application.applier.applied"/>
        </c:set>
    </c:otherwise>
</c:choose>

<uv:box className="tw-h-32 tw-mb-4">
    <jsp:attribute name="icon">
        <uv:box-icon className="tw-bg-amber-300 tw-text-white dark:tw-bg-amber-400 dark:tw-text-zinc-900">
            <c:choose>
                <c:when test="${application.vacationType.category == 'HOLIDAY'}">
                    <icon:sun className="tw-w-8 tw-h-8" />
                </c:when>
                <c:otherwise>
                    <icon:flag className="tw-w-8 tw-h-8" />
                </c:otherwise>
            </c:choose>
        </uv:box-icon>
    </jsp:attribute>
    <jsp:body>
        <span class="tw-text-sm tw-text-black tw-text-opacity-75 dark:tw-text-zinc-300 dark:tw-text-opacity-100">
            <c:out value="${application.person.niceName}"/> ${APPLIER_APPLIED}
        </span>
        <span class="tw-my-1 tw-text-lg tw-font-medium">
            <spring:message code="${application.vacationType.messageKey}"/>
            <span class="state ${application.status} pull-right print:tw-hidden hidden-xs" title="<spring:message code='${application.status}' />">
                <c:choose>
                    <c:when test="${application.status == 'WAITING'}">
                        <icon:question-mark-circle className="tw-w-6 tw-h-6" />
                    </c:when>
                    <c:when test="${application.status == 'TEMPORARY_ALLOWED'}">
                        <icon:check-circle className="tw-w-6 tw-h-6" />
                    </c:when>
                    <c:when test="${application.status == 'ALLOWED'}">
                        <icon:check-circle className="tw-w-6 tw-h-6" />
                    </c:when>
                    <c:when test="${application.status == 'ALLOWED_CANCELLATION_REQUESTED'}">
                        <icon:check-circle className="tw-w-6 tw-h-6" />
                        <icon:arrow-narrow-right className="tw-w-6 tw-h-6" />
                        <icon:trash className="tw-w-6 tw-h-6" />
                    </c:when>
                    <c:when test="${application.status == 'REJECTED'}">
                        <icon:ban className="tw-w-6 tw-h-6" />
                    </c:when>
                    <c:when test="${application.status == 'CANCELLED' || application.status == 'REVOKED'}">
                        <icon:trash className="tw-w-6 tw-h-6" />
                    </c:when>
                    <c:otherwise>
                        &nbsp;
                    </c:otherwise>
                </c:choose>
            </span>
        </span>
        <span class="tw-text-sm tw-text-black tw-text-opacity-75 dark:tw-text-zinc-300 dark:tw-text-opacity-100">
            <c:choose>
                <c:when test="${application.startDate == application.endDate}">
                    <c:set var="APPLICATION_DATE">
                        <spring:message code="${application.weekDayOfStartDate}.short"/>,
                        <uv:date date="${application.startDate}"/>
                    </c:set>
                    <c:set var="APPLICATION_DAY_LENGTH">
                        <spring:message code="${application.dayLength}"/>
                    </c:set>
                    <spring:message code="absence.period.singleDay" arguments="${APPLICATION_DATE};${APPLICATION_DAY_LENGTH}" argumentSeparator=";"/>
                </c:when>
                <c:otherwise>
                    <c:set var="APPLICATION_START_DATE">
                        <spring:message code="${application.weekDayOfStartDate}.short"/>,
                        <uv:date date="${application.startDate}"/>
                    </c:set>
                    <c:set var="APPLICATION_END_DATE">
                        <spring:message code="${application.weekDayOfEndDate}.short"/>,
                        <uv:date date="${application.endDate}"/>
                    </c:set>
                    <spring:message code="absence.period.multipleDays" arguments="${APPLICATION_START_DATE};${APPLICATION_END_DATE}" argumentSeparator=";"/>
                </c:otherwise>
            </c:choose>
        </span>
    </jsp:body>
</uv:box>

<table class="list-table striped-table bordered-table tw-text-sm tw-table-fixed">

    <tr>
        <td><spring:message code="absence.period.duration"/></td>
        <td>
            <strong>
                <uv:number number="${application.workDays}"/> <spring:message code="duration.days"/>
            </strong>
            <c:if test="${application.startDate.year != application.endDate.year}">
                <span class="text-muted days">
                    <%-- filled by javascript --%>
                    <%-- see app_info.js --%>
                </span>
            </c:if>
            <c:if test="${application.vacationType.category == 'OVERTIME' && application.hours != null}">
                <span class="text-muted">
                    <br/>
                    <uv:duration duration="${application.hours}"/>
                    <spring:message code="application.data.hours.number"/>
                </span>
            </c:if>
        </td>
    </tr>
    <tr class="visible-print">
        <td><spring:message code="application.data.status"/></td>
        <td><spring:message code="${application.status}"/></td>
    </tr>
    <tr><%-- needed for correct altering of table rows: there is a problem because the only in print visible row is altered too --%></tr>
    <tr>
        <td>
            <spring:message code="application.data.time"/>
        </td>
        <td>
            <c:choose>
                <c:when test="${application.startTime != null && application.endTime != null}">
                    <p>
                        <c:set var="APPLICATION_START_TIME">
                            <uv:time dateTime="${application.startDateWithTime}"/>
                        </c:set>
                        <c:set var="APPLICATION_END_TIME">
                            <uv:time dateTime="${application.endDateWithTime}"/>
                        </c:set>
                        <spring:message code="absence.period.time"
                                        arguments="${APPLICATION_START_TIME};${APPLICATION_END_TIME}"
                                        argumentSeparator=";"/>
                    </p>
                </c:when>
                <c:otherwise>
                    <spring:message code="application.data.furtherInformation.notSpecified"/>
                </c:otherwise>
            </c:choose>
        </td>
    </tr>
    <tr>
        <td>
            <spring:message code='application.data.reason'/>
        </td>
        <td class="tw-break-words">
            <c:choose>
                <c:when test="${application.reason != null && !empty application.reason}">
                    <c:out value="${application.reason}"/>
                </c:when>
                <c:otherwise>
                    <spring:message code="application.data.furtherInformation.notSpecified"/>
                </c:otherwise>
            </c:choose>
        </td>
    </tr>

    <tr>
        <td>
            <spring:message code='application.data.holidayReplacement'/>
        </td>
        <td>
            <c:choose>
                <c:when test="${not empty application.holidayReplacements}">
                    <ul class="tw-list-none tw-m-0 tw-p-0" data-test-id="holiday-replacement-list">
                        <c:forEach items="${application.holidayReplacements}" var="replacementInfo">
                        <li>
                            <c:out value="${replacementInfo.person.niceName}"/>
                        </li>
                        </c:forEach>
                    </ul>
                </c:when>
                <c:otherwise>
                    <spring:message code="application.data.furtherInformation.notSpecified"/>
                </c:otherwise>
            </c:choose>
        </td>
    </tr>
    <tr>
        <td>
            <spring:message code="application.data.furtherInformation.address"/>
        </td>
        <td class="tw-break-words">
            <c:choose>
                <c:when test="${application.address!= null && !empty application.address}">
                    <c:out value="${application.address}"/>
                </c:when>
                <c:otherwise>
                    <spring:message code="application.data.furtherInformation.notSpecified"/>
                </c:otherwise>
            </c:choose>
        </td>
    </tr>
    <tr>
        <td>
            <spring:message code='application.data.teamInformed'/>
        </td>
        <td>
            <div class="tw-flex tw-items-center">
            <c:choose>
                <c:when test="${application.teamInformed == true}">
                    <icon:emoji-happy className="tw-w-4 tw-h-4" />
                    &nbsp;<spring:message code='application.data.teamInformed.true'/>
                </c:when>
                <c:otherwise>
                    <icon:emoji-sad className="tw-w-4 tw-h-4" />
                    &nbsp;<spring:message code='application.data.teamInformed.false'/>
                </c:otherwise>
            </c:choose>
            </div>
        </td>
    </tr>
</table>
