<%-- 
    Document   : account_days
    Created on : 08.02.2012, 18:31:36
    Author     : Aljona Murygina
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>


<tr>
        <th>
            <spring:message code="entitlement" />&nbsp;<spring:message code="in.year" />&nbsp;<c:out value="${displayYear}"/>
        </th>
        <td>
            <c:choose>
                
                <c:when test="${entitlement != null}">
                    <c:set var ="ent" value="${entitlement.vacationDays + entitlement.remainingVacationDays}" />
                    <c:out value="${ent}" />
                    <c:choose>
                        <c:when test="${ent <= 1.00 && ent > 0.50}">
                            <spring:message code="day" />
                        </c:when>
                        <c:otherwise>
                            <spring:message code="days" />
                        </c:otherwise>
                    </c:choose>
                    (<spring:message code="davon" />
                    <c:choose>
                        <c:when test="${entitlement.remainingVacationDays == null}">
                            <c:set var="remaining" value="0.00" />
                        </c:when>
                        <c:otherwise>
                            <c:set var="remaining" value="${entitlement.remainingVacationDays}" />
                        </c:otherwise>
                    </c:choose>
                    <c:out value="${remaining}" />
                    <c:choose>
                        <c:when test="${remaining <= 1.00 && remaining > 0.50}">
                            <spring:message code="day" />
                        </c:when>
                        <c:otherwise>
                            <spring:message code="days" />
                        </c:otherwise>
                    </c:choose>
                    <spring:message code="remaining" />)
                </c:when>
                    
                <c:otherwise>
                    <spring:message code='not.specified' />
                </c:otherwise> 
                    
            </c:choose>    
        </td>
    </tr>
    <tr>
        <th>
            <spring:message code="overview.used" />
        </th>
        <td>
            <c:choose>
                <c:when test="${account != null && entitlement != null}">
                    <c:set var="used" value="${(entitlement.vacationDays - account.vacationDays) + (entitlement.remainingVacationDays - account.remainingVacationDays)}" />
                    <c:out value="${used}" />
                    <c:choose>
                        <c:when test="${used <= 1.00 && used > 0.50}">
                            <spring:message code="day" />
                        </c:when>
                        <c:otherwise>
                            <spring:message code="days" />
                        </c:otherwise>
                    </c:choose>
                </c:when>
                <c:otherwise>
                    <spring:message code='not.specified' />
                </c:otherwise>    
            </c:choose>
        </td>
    </tr>
    <tr>
        <th>
            <spring:message code="overview.left" />
        </th>
        <td>
            <%@include file="../../application/include/left_days.jsp" %>
        </td>
