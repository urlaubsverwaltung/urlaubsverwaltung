<%-- 
    Document   : mitarbeiter
    Created on : 26.10.2011, 11:35:53
    Author     : aljona
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
        <title><spring:message code="title" /></title>
    </head>
    
    <body>
        
        <table>
            <tr>
                <th colspan="3"><spring:message code="table.title" /></th>
            </tr>
            <tr>
                <th><spring:message code="table.person" /></th>
                <th><spring:message code="table.vac" /></th>
                <th><spring:message code="table.resturlaub" /></th>
            </tr>
        <c:forEach items="${mitarbeiter}" var="person" varStatus="loopStatus">
            <tr class="${loopStatus.index % 2 == 0 ? 'even' : 'odd'}">
                <td><c:out value="${person.lastname}"/>&nbsp;<c:out value="${person.firstname}"/></td>
                <td><c:out value="${person.remainingVacationDays}"/></td>
                <td><c:out value="${person.restUrlaub}"/></td>
            </tr>    
        </c:forEach>
        </table>
        
    </body>
    
</html>
