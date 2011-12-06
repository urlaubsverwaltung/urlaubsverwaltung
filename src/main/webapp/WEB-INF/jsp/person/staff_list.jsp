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
        <link rel="stylesheet" type="text/css" href="<spring:url value='css/main.css' />" /> 
        <title><spring:message code="title" /></title>
    </head>
    
    <body>
        
        <spring:url var="formUrlPrefix" value="/web" />
        
        <%@include file="include/header.jsp" %>
        
        <a href="${formUrlPrefix}/staff/list"><spring:message code="table.list" /></a>
        <a href="${formUrlPrefix}/staff/detail"><spring:message code="table.detail" /></a>
        
        <table cellspacing="0">
            <tr>
                <th colspan="3"><spring:message code="table.title" /></th>
            </tr>
            <tr>
                <th><spring:message code="name" /></th>
                <th><spring:message code="email" /></th>
                <th><spring:message code="table.vac" /></th>
                <th><spring:message code="table.resturlaub" /></th>
                <th>&nbsp;</th>
            </tr>
        <c:forEach items="${accounts}" var="account" varStatus="loopStatus">
            <tr class="${loopStatus.index % 2 == 0 ? 'even' : 'odd'}">
                <td><c:out value="${account.person.lastName}"/>&nbsp;<c:out value="${account.person.firstName}"/></td>
                <td><a href="mailto:${account.person.email}"><c:out value="${account.person.email}"/></a></td>
                <td><c:out value="${account.vacationDays}"/></td>
                <td><c:out value="${account.remainingVacationDays}"/></td>
                <td><a href="${formUrlPrefix}/apply/${account.person.id}"><spring:message code="table.antrag" /></a></td>
            </tr>    
        </c:forEach>
        </table>
        
    </body>
    
</html>
