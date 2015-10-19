<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<div class="box">
    <span class="box-icon bg-yellow">
        <c:choose>
            <c:when test="${application.vacationType == 'HOLIDAY'}">
                <i class="fa fa-sun-o"></i>
            </c:when>
            <c:otherwise>
                <i class="fa fa-flag-o"></i>
            </c:otherwise>
        </c:choose>
    </span>
    <span class="box-text">
        <h5 class="is-inline-block is-sticky"><c:out value="${application.person.niceName}" /></h5> <spring:message code="application.applier.applied" />
        <h4>
            <spring:message code="${application.vacationType}"/>
            <span class="state ${application.status} pull-right hidden-print hidden-xs" title="<spring:message code='${application.status}' />">
            <c:choose>
                <c:when test="${application.status == 'WAITING'}">
                    <i class="fa fa-question"></i>
                </c:when>
                <c:when test="${application.status == 'ALLOWED'}">
                    <i class="fa fa-check"></i>
                </c:when>
                <c:when test="${application.status == 'REJECTED'}">
                    <i class="fa fa-ban"></i>
                </c:when>
                <c:when test="${application.status == 'CANCELLED'}">
                    <i class="fa fa-trash"></i>
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
                    <h5 class="is-inline-block is-sticky"><uv:date date="${application.startDate}"/></h5>
                </c:set>
                <c:set var="APPLICATION_DAY_LENGTH">
                    <spring:message code="${application.dayLength}"/>
                </c:set>
                <spring:message code="absence.period.singleDay" arguments="${APPLICATION_DATE};${APPLICATION_DAY_LENGTH}" argumentSeparator=";"/>
            </c:when>
            <c:otherwise>
                <c:set var="APPLICATION_START_DATE">
                    <h5 class="is-inline-block is-sticky"><uv:date date="${application.startDate}"/></h5>
                </c:set>
                <c:set var="APPLICATION_END_DATE">
                    <h5 class="is-inline-block is-sticky"><uv:date date="${application.endDate}"/></h5>
                </c:set>
                <spring:message code="absence.period.multipleDays" arguments="${APPLICATION_START_DATE};${APPLICATION_END_DATE}" argumentSeparator=";"/>
            </c:otherwise>
        </c:choose>
    </span>
</div>

<table class="list-table striped-table bordered-table" cellspacing="0">

    <tr>
        <td><spring:message code="absence.period.duration" /></td>
        <td>
            <b>
              <uv:number number="${application.workDays}" /> <spring:message code="duration.days" />
            </b>
            <b class="days">
                <%-- filled by javascript --%>
            </b>
            <script type="text/javascript">

                $(document).ready(function () {

                  <c:if test="${application.startDate.year != application.endDate.year}">

                    var dayLength = '<c:out value="${application.dayLength}" />';
                    var personId = '<c:out value="${application.person.id}" />';

                    var startDate = "<joda:format pattern='yyyy/MM/dd' value='${application.startDate}' />";
                    var endDate = "<joda:format pattern='yyyy/MM/dd' value='${application.endDate}' />";

                    var from = new Date(startDate);
                    var to = new Date(endDate);

                    sendGetDaysRequestForTurnOfTheYear("<spring:url value='/api' />", from, to, dayLength, personId, ".days");

                  </c:if>

                });

            </script>
        </td>
    </tr>
    <c:if test="${application.vacationType == 'OVERTIME'}">
        <tr>
            <td>
                <spring:message code="application.data.hours"/>
            </td>
            <td>
                <uv:number number="${application.hours}"/>
            </td>
        </tr>
    </c:if>
    <tr class="visible-print">
        <td><spring:message code="application.data.status" /></td>
        <td><spring:message code="${application.status}" /></td>
    </tr>
    <tr><%-- needed for correct altering of table rows: there is a problem because the only in print visible row is altered too --%></tr>
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
                    <spring:message code="application.data.furtherInformation.comment.none"/>
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
                    <spring:message code="application.data.furtherInformation.comment.none"/>
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
                    <spring:message code="application.data.furtherInformation.comment.none"/>
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
                    <i class="fa fa-check positive hidden-print"></i>
                    <spring:message code='application.data.teamInformed.true'/>
                </c:when>
                <c:otherwise>
                    <i class="fa fa-remove hidden-print"></i>
                    <spring:message code='application.data.teamInformed.false'/>
                </c:otherwise>
            </c:choose>
        </td>
    </tr>
</table>
