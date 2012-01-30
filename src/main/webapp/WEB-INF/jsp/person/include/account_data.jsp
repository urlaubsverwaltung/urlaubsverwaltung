<%-- 
    Document   : account_data
    Created on : 30.01.2012, 14:20:28
    Author     : Aljona Murygina
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

    <tr>
        <td rowspan="6"><img class="user-pic" src="<c:out value='${gravatar}?d=mm'/>" /></td>
    </tr>
    <tr>
        <th>
            <spring:message code="entitlement" />&nbsp;<spring:message code="in.year" />&nbsp;<c:out value="${year}"/>
        </th>
        <td>
            <c:choose>
                <c:when test="${entitlement != null}">
                    <c:out value="${entitlement.vacationDays + entitlement.remainingVacationDays}"/>
                    <spring:message code="days" />&nbsp;(<spring:message code="davon" />
                    <c:choose>
                        <c:when test="${entitlement.remainingVacationDays == null}">
                            0
                        </c:when>
                        <c:otherwise>
                            <c:out value="${entitlement.remainingVacationDays}"/>
                        </c:otherwise>
                    </c:choose>
                    <spring:message code="days" />&nbsp;<spring:message code="remaining" />)
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
                    <c:out value="${(entitlement.vacationDays - account.vacationDays) + (entitlement.remainingVacationDays - account.remainingVacationDays)}"/>&nbsp;<spring:message code="days" />
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
            <c:choose>
                <c:when test="${account != null}">
                    <c:choose>
                        <c:when test="${april == 1}">
                            <c:out value="${account.vacationDays + account.remainingVacationDays}"/>&nbsp;<spring:message code="days" />
                            &nbsp;(<spring:message code="davon" />&nbsp;<c:out value="${account.remainingVacationDays}"/>
                            <spring:message code="days" />&nbsp;<spring:message code="remaining" />)
                        </c:when>
                        <c:otherwise>
                            <c:out value="${account.vacationDays}"/>&nbsp;<spring:message code="days" />
                        </c:otherwise>
                    </c:choose>
                </c:when>
                <c:otherwise>
                    <spring:message code='not.specified' />
                </c:otherwise>    
            </c:choose>
        </td>
    </tr>

