<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<div class="box">
    <span class="box-icon bg-yellow hidden-print">
        <c:choose>
            <c:when test="${application.vacationType.category == 'HOLIDAY'}">
                <i class="fa fa-sun-o" aria-hidden="true"></i>
            </c:when>
            <c:otherwise>
                <i class="fa fa-flag-o" aria-hidden="true"></i>
            </c:otherwise>
        </c:choose>
    </span>
    <span class="box-text">
        <h5 class="is-inline-block is-sticky"><c:out value="${application.person.niceName}"/></h5> <spring:message
        code="application.applier.applied"/>
        <h4>
            <spring:message code="${application.vacationType.messageKey}"/>
            <span class="state ${application.status} pull-right hidden-print hidden-xs"
                  title="<spring:message code='${application.status}' />">
            <c:choose>
                <c:when test="${application.status == 'WAITING'}">
                    <i class="fa fa-question" aria-hidden="true"></i>
                </c:when>
                <c:when test="${application.status == 'TEMPORARY_ALLOWED'}">
                    <i class="fa fa-check" aria-hidden="true"></i>
                </c:when>
                <c:when test="${application.status == 'ALLOWED'}">
                    <i class="fa fa-check" aria-hidden="true"></i>
                </c:when>
                <c:when test="${application.status == 'REJECTED'}">
                    <i class="fa fa-ban" aria-hidden="true"></i>
                </c:when>
                <c:when test="${application.status == 'CANCELLED' || application.status == 'REVOKED'}">
                    <i class="fa fa-trash" aria-hidden="true"></i>
                </c:when>
                <c:otherwise>
                    &nbsp;
                </c:otherwise>
            </c:choose>
        </span>
        </h4>

        <c:choose>
            <c:when test="${application.startDate == application.endDate}">
                <c:set var="APPLICATION_DATE">
                    <h5 class="is-inline-block is-sticky">
                        <spring:message code="${application.weekDayOfStartDate}.short"/>,
                        <uv:date date="${application.startDate}"/>
                    </h5>
                </c:set>
                <c:set var="APPLICATION_DAY_LENGTH">
                    <spring:message code="${application.dayLength}"/>
                </c:set>
                <spring:message code="absence.period.singleDay"
                                arguments="${APPLICATION_DATE};${APPLICATION_DAY_LENGTH}" argumentSeparator=";"/>
            </c:when>
            <c:otherwise>
                <c:set var="APPLICATION_START_DATE">
                    <h5 class="is-inline-block is-sticky">
                        <spring:message code="${application.weekDayOfStartDate}.short"/>,
                        <uv:date date="${application.startDate}"/>
                    </h5>
                </c:set>
                <c:set var="APPLICATION_END_DATE">
                    <h5 class="is-inline-block is-sticky">
                        <spring:message code="${application.weekDayOfEndDate}.short"/>,
                        <uv:date date="${application.endDate}"/>
                    </h5>
                </c:set>
                <spring:message code="absence.period.multipleDays"
                                arguments="${APPLICATION_START_DATE};${APPLICATION_END_DATE}" argumentSeparator=";"/>
            </c:otherwise>
        </c:choose>
    </span>
</div>

<table class="list-table striped-table bordered-table">

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
                    <uv:number number="${application.hours}"/>
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
        <td>
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
                <c:when test="${application.holidayReplacement != null}">
                    <c:out value="${application.holidayReplacement.niceName}"/>
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
        <td>
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
            <c:choose>
                <c:when test="${application.teamInformed == true}">
                    <i class="fa fa-check positive hidden-print" aria-hidden="true"></i>
                    <spring:message code='application.data.teamInformed.true'/>
                </c:when>
                <c:otherwise>
                    <i class="fa fa-remove hidden-print" aria-hidden="true"></i>
                    <spring:message code='application.data.teamInformed.false'/>
                </c:otherwise>
            </c:choose>
        </td>
    </tr>
    <tr>
        <td>
            <spring:message code="application.data.workingTime"/>
        </td>
        <td>
            <c:if test="${workingTime.monday.duration > 0}">
                <spring:message code="MONDAY.short"/>
            </c:if>
            <c:if test="${workingTime.tuesday.duration > 0}">
                <spring:message code="TUESDAY.short"/>
            </c:if>
            <c:if test="${workingTime.wednesday.duration > 0}">
                <spring:message code="WEDNESDAY.short"/>
            </c:if>
            <c:if test="${workingTime.thursday.duration > 0}">
                <spring:message code="THURSDAY.short"/>
            </c:if>
            <c:if test="${workingTime.friday.duration > 0}">
                <spring:message code="FRIDAY.short"/>
            </c:if>
            <c:if test="${workingTime.saturday.duration > 0}">
                <spring:message code="SATURDAY.short"/>
            </c:if>
            <c:if test="${workingTime.sunday.duration > 0}">
                <spring:message code="SUNDAY.short"/>
            </c:if>
        </td>
    </tr>
</table>
