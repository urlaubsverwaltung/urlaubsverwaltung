<%-- 
    Document   : staff_list
    Created on : 31.10.2011, 11:49:42
    Author     : Aljona Murygina
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>


<!DOCTYPE html>
<html>
    
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" type="text/css" href="<spring:url value='/css/main.css' />" /> 
        <title><spring:message code="title" /></title>
    </head>
    
    <body>
        
        <spring:url var="formUrlPrefix" value="/web" />
        
        <%@include file="../include/header.jsp" %>
        
        <div id="show-navi">
        <a href="${formUrlPrefix}/staff/list"><spring:message code="table.list" /></a>
        <a href="${formUrlPrefix}/staff/detail"><spring:message code="table.detail" /></a>
        </div>
        
        
        <div id="content">
            
        <table id="staff-list" cellspacing="0" border="0.5">
            <tr>
                <th class="attributes"><spring:message code="login" /></th>
                <th class="attributes"><spring:message code="name" /></th>
                <th class="attributes"><spring:message code="email" /></th>
                <th class="vac"><spring:message code="table.vac" /></th>
                <th class="vac"><spring:message code="table.urlaub" /></th>
                <th>&nbsp;</th>
            </tr>
        <c:forEach items="${persons}" var="person" varStatus="loopStatus">
            <tr class="${loopStatus.index % 2 == 0 ? 'even' : 'odd'}">
                <td><c:out value="${person.loginName}"/></td>
                <td><c:out value="${person.lastName}"/>&nbsp;<c:out value="${person.firstName}"/></td>
                <td><a href="mailto:${person.email}"><c:out value="${person.email}"/></a></td>
                <td class="vac"><c:out value="${entitlements[person].vacationDays + entitlements[person].remainingVacationDays}"/></td>
                <td class="vac">
                    <c:choose>
                        <c:when test="${april == 1}">
                            <c:out value="${accounts[person].vacationDays + accounts[person].remainingVacationDays}"/>
                        </c:when>
                        <c:otherwise>
                            <c:out value="${accounts[person].vacationDays}"/>
                        </c:otherwise>
                    </c:choose>
                </td>
                <td class="td-edit"><a href="${formUrlPrefix}/staff/${person.id}/edit"><img src="<spring:url value='/images/edit.png' />" /></a></td>
            </tr>    
        </c:forEach>
        </table>
        </div>        
        
    </body>
    
</html>
