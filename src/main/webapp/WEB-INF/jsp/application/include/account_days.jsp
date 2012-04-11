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


        <th>
            <spring:message code="entitlement" />&nbsp;<spring:message code="in.year" />&nbsp;<c:out value="${displayYear}"/>
        </th>
        <td>
            <c:choose>
                
                <c:when test="${entitlement != null}">
                    <c:set var ="ent" value="${entitlement.vacationDays + entitlement.remainingVacationDays}" />
                    <c:choose>
                        <c:when test="${ent <= 1.00 && ent > 0.50}">
                            <c:set var="numberOfDays" value="day" />
                        </c:when>
                        <c:otherwise>
                            <c:set var="numberOfDays" value="days" />
                        </c:otherwise>
                    </c:choose>
                    <spring:message code="${numberOfDays}" arguments="${ent}" />
                    
                    <c:choose>
                        <c:when test="${entitlement.remainingVacationDays == null}">
                            <c:set var="remDays" value="0" />
                        </c:when>
                        <c:otherwise>
                            <c:set var="remDays" value="${entitlement.remainingVacationDays}" />
                        </c:otherwise>
                    </c:choose>
                    <c:choose>
                        <c:when test="${remDays <= 1.00 && remDays > 0.50}">
                            <c:set var="remaining" value="remaining.sing"/>
                        </c:when>
                        <c:otherwise>
                            <c:set var="remaining" value="remaining.plural"/>
                        </c:otherwise>
                    </c:choose>
                    <spring:message code="${remaining}" arguments="${remDays}" />
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
                    <c:choose>
                        <c:when test="${usedDays <= 1.00 && usedDays > 0.50}">
                            <c:set var="numberOfUsedDays" value="day" />
                        </c:when>
                        <c:otherwise>
                            <c:set var="numberOfUsedDays" value="days" />
                        </c:otherwise>
                    </c:choose>
                    <spring:message code="${numberOfUsedDays}" arguments="${usedDays}" />
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
