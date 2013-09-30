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


<table class="app-detail tbl-margin-bottom" cellspacing="0">
    <tr class="odd">
        <td>
            <spring:message code="app.apply" />
        </td>
        <td class="${application.vacationType}">
            <b><spring:message code="${application.vacationType.vacationTypeName}" /></b>
        </td>
    </tr>
    <tr class="even">
        <td>
            <c:choose>
                <c:when test="${application.startDate == application.endDate}">
                    <spring:message code="at" /> <b><joda:format style="M-" value="${application.startDate}"/></b>
                </c:when>
                <c:otherwise>
                    <spring:message code="from" /> <b><joda:format style="M-" value="${application.startDate}"/></b> <spring:message code="to" /> <b><joda:format style="M-" value="${application.endDate}"/></b>
                </c:otherwise>    
            </c:choose>
        </td>
        <td>
            = <fmt:formatNumber maxFractionDigits="1" value="${application.days}"/> 
            <c:choose>
                <c:when test="${application.days > 0.50 && application.days <= 1.00}">
                    <spring:message code="day.vac" />
                </c:when>
                <c:otherwise>
                    <spring:message code="days.vac" />
                </c:otherwise>
            </c:choose>
</td>
</tr>
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
        <c:out value="${application.rep}" />
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
