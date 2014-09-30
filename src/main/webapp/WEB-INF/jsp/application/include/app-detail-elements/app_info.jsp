<%-- 
    Document   : app_info
    Created on : 07.09.2012, 11:19:45
    Author     : Aljona Murygina - murygina@synyx.de
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<div class="box">
    <span class="thirds">
       <span class="box-icon bg-yellow"><i class="fa fa-sun-o"></i></span>
        <spring:message code="app.apply" /> <h4><spring:message code="${application.vacationType.vacationTypeName}" /></h4>

        <c:choose>
            <c:when test="${application.startDate == application.endDate}">
                <spring:message code="at" /> <h4 class="is-inline-block"><uv:date date="${application.startDate}" />, <spring:message code="${application.howLong.dayLength}" /></h4>
            </c:when>
            <c:otherwise>
                <spring:message code="from" /> <h4 class="is-inline-block"><uv:date date="${application.startDate}" /></h4> <spring:message code="to" /> <h4 class="is-inline-block"><uv:date date="${application.endDate}" /></h4>
            </c:otherwise>
        </c:choose>

        <c:if test="${application.startDate.year != application.endDate.year}">
            <p class="days">
                <%-- filled by javascript --%>
                <fmt:formatNumber maxFractionDigits="1" value="${application.days}"/> Tage
            </p>
            <script type="text/javascript">

                $(document).ready(function() {

                    var dayLength = '<c:out value="${application.howLong}" />';
                    var personId = '<c:out value="${application.person.id}" />';

                    var startDate= "<joda:format pattern='yyyy/MM/dd' value='${application.startDate}' />";
                    var endDate = "<joda:format pattern='yyyy/MM/dd' value='${application.endDate}' />";

                    var from = new Date(startDate);
                    var to = new Date(endDate);

                    sendGetDaysRequest("<spring:url value='/api' />", from, to, dayLength, personId, ".days", true);

                });

            </script>

        </c:if>

    </span>
</div>

<table class="detail-table" cellspacing="0">

<tr class="odd">
    <td>
        <spring:message code='reason' />
    </td>
    <td>
        <c:choose>
            <c:when test="${application.reason != null && !empty application.reason}">
                <c:out value="${application.reason}" />
            </c:when>
            <c:otherwise>
                <spring:message code="not.stated" />
            </c:otherwise>
        </c:choose>
    </td>
</tr>
<tr class="even">
    <td>
        <spring:message code='app.rep' />
    </td>
    <td>
        <c:choose>
            <c:when test="${application.repDeprecated != null}">
                <c:out value="${application.repDeprecated}" />
            </c:when>
            <c:otherwise>
                <c:choose>
                    <c:when test="${application.rep != null}">
                        <c:out value="${application.rep.niceName}" /> 
                    </c:when>
                    <c:otherwise>
                        <spring:message code="not.stated" />
                    </c:otherwise>
                </c:choose>
            </c:otherwise>
        </c:choose>
    </td>
</tr>
<tr class="odd">
    <td>
        <spring:message code='app.address' />
    </td>
    <td>
        <c:choose>
            <c:when test="${application.address!= null && !empty application.address}">
                <c:out value="${application.address}" />
            </c:when>
            <c:otherwise>
                <spring:message code="not.stated" />
            </c:otherwise>
        </c:choose>
    </td>
</tr>
<tr class="even">
    <td>
        <spring:message code='app.team' />
    </td>
    <td>
        <c:choose>
            <c:when test="${application.teamInformed == true}">
                <spring:message code='yes' />
            </c:when>
            <c:otherwise>
                <spring:message code='no' />
            </c:otherwise>
        </c:choose>
    </td>
</tr>
<tr class="odd">
    <td>
        <spring:message code='comment' />
    </td>
    <td>
        <c:choose>
            <c:when test="${application.comment != null && !empty application.comment}">
                <c:out value="${application.comment}" />
            </c:when>
            <c:otherwise>
                <spring:message code="not.stated" />
            </c:otherwise>
        </c:choose>
    </td>
</tr>
</table>
