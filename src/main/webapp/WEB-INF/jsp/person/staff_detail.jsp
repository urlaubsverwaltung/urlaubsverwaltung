<%-- 
    Document   : staff_detail
    Created on : 31.10.2011, 11:49:53
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
        
        <a href="${formUrlPrefix}/staff/list"><spring:message code="table.list" /></a>
        <a href="${formUrlPrefix}/staff/detail"><spring:message code="table.detail" /></a>
        
        <c:forEach items="${accounts}" var="account">
        <table>
            <tr>
                <td rowspan="3">
                    <!--  Bild des Mitarbeiters  -->
                </td>
                <td>
                   <spring:message code="name" />: 
                </td>
                <td>
                   <c:out value="${account.person.lastName}"/>&nbsp;<c:out value="${account.person.firstName}"/> 
                </td>
            </tr>
            <tr>
                <td><a href="mailto:${account.person.email}"><c:out value="${account.person.email}"/></a></td>
            </tr>
            <tr>
                <td>
                   <spring:message code="table.vac" />
                </td>
                <td>
                    <c:out value="${account.vacationDays}"/>
                </td>
            </tr>
            <tr>
                <td>
                    <spring:message code="table.resturlaub" />
                </td>
                <td>
                    <c:out value="${account.remainingVacationDays}"/>
                </td>
            </tr>    
            <tr>
                <td colspan="2">
                    <a href="${formUrlPrefix}/apply/${account.person.id}"><spring:message code="table.antrag" /></a>
                </td>
            </tr>   
        </table>    
        <br />        
        </c:forEach>
        
    </body>
    
</html>
