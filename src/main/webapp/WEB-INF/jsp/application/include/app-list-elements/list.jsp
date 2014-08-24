<%-- 
    Document   : wartend
    Created on : 26.10.2011, 15:03:30
    Author     : aljona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>


<c:choose>

    <c:when test="${empty applications}">

        <spring:message code="no.apps" />

    </c:when>

    <c:otherwise>     

        <table class="data-table is-centered zebra-table tablesorter sortable" cellspacing="0">
            <thead>
            <tr>
                <th>
                    <spring:message code="state" />
                </th>
                <th>
                    <spring:message code="${touchedDate}" />
                </th>
                <th>
                    <spring:message code="staff" />
                </th>
                <th>
                    <spring:message code="type" />
                </th>
                <th>
                    <spring:message code="time" />
                </th>
                <th>
                    <spring:message code="days.vac" />
                </th>
            </tr>
            </thead>

            <tbody>
            <c:forEach items="${applications}" var="app" varStatus="loopStatus">
                <tr onclick="navigate('${formUrlPrefix}/application/${app.id}');">
                    <td>
                        <spring:message code="${app.status.state}" />
                    </td>
                    <td>
                    
                    <%-- 0 : applications are waiting --%>
                    <c:if test="${app.status.number == 0}">
                        <c:set var="appTouched" value="${app.applicationDate}" />
                    </c:if>        

                    <%-- 1 and 2 : applications are allowed or rejected --%>
                    <c:if test="${app.status.number == 1 || app.status.number == 2}">
                        <c:set var="appTouched" value="${app.editedDate}" />
                    </c:if>

                    <%-- 3 : applications are cancelled --%>
                    <c:if test="${app.status.number == 3}">
                        <c:set var="appTouched" value="${app.cancelDate}" />
                    </c:if>
                   
                    <uv:date date="${appTouched}" />
                    </td>
                    <td>
                        <c:out value="${app.person.firstName}" />&nbsp;<c:out value="${app.person.lastName}" />
                    </td>
                    <td class="${app.vacationType}">
                        <spring:message code="${app.vacationType.vacationTypeName}"/>
                    </td>
                    <td>
                        <c:choose>
                            <c:when test="${app.startDate == app.endDate}">
                                <uv:date date="${app.startDate}" />, <spring:message code="${app.howLong.dayLength}" />
                            </c:when>
                            <c:otherwise>
                                <uv:date date="${app.startDate}" /> - <uv:date date="${app.endDate}" />
                            </c:otherwise>    
                        </c:choose>
                    </td>
                    <td>
                        <fmt:formatNumber maxFractionDigits="1" value="${app.days}" />
                    </td>

                </tr>
            </c:forEach>
            </tbody>
        </table>
    </c:otherwise> 
</c:choose>  

