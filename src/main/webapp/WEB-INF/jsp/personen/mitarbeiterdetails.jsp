<%-- 
    Document   : mitarbeiterdetails
    Created on : 31.10.2011, 11:49:53
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
        
        <spring:url var="formUrlPrefix" value="/web/urlaubsverwaltung" />
        
        <a href="${formUrlPrefix}/mitarbeiter/list"><spring:message code="table.list" /></a>
        <a href="${formUrlPrefix}/mitarbeiter/detail"><spring:message code="table.detail" /></a>
        
        <c:forEach items="${mitarbeiter}" var="person" varStatus="loopStatus">
        <table>
            <tr>
                <td rowspan="3">
                    <!--  Bild des Mitarbeiters  -->
                </td>
                <td>
                   <spring:message code="name" />: 
                </td>
                <td>
                   <c:out value="${person.lastName}"/>&nbsp;<c:out value="${person.firstName}"/> 
                </td>
            </tr>
            <tr>
                <td><a href="mailto:${person.email}"><c:out value="${person.email}"/></a></td>
            </tr>
            <tr>
                <td>
                   <spring:message code="table.vac" />
                </td>
                <td>
                    <c:out value="${person.remainingVacationDays}"/>
                </td>
            </tr>
            <tr>
                <td>
                    <spring:message code="table.resturlaub" />
                </td>
                <td>
                    <c:out value="${person.restUrlaub}"/>
                </td>
            </tr>    
            <tr>
                <td colspan="2">
                    <a href="${formUrlPrefix}/antraege/${mitarbeiter.id}"><spring:message code="table.antrag" /></a>
                </td>
            </tr>   
            <c:if test="user == office - wie auch immer das mal gehen mag">
                <tr> 
                    <td>
                        <a href="${formUrlPrefix}/antrag/${antrag.id}/edit" />
                    </td>
                </tr>    
            </c:if>
        </table>    
        <br />        
        </c:forEach>
        
    </body>
    
</html>
