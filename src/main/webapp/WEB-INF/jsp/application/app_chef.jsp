<%-- 
    Document   : applicationform
    Created on : 26.10.2011, 15:05:51
    Author     : aljona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>


<!DOCTYPE html>
<html>
    
    <%--  Ansicht des Urlaubsantrags vom Chef aus, der Antrag genehmigen/ablehnen muss  --%>
    
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" type="text/css" href="<spring:url value='/css/main.css' />" />
        <title><spring:message code="title" /></title>
    </head>
    
    <body>
        
        <spring:url var="formUrlPrefix" value="/web/urlaubsverwaltung" />
        
        <%@include file="../include/header.jsp" %>

        <div id="content">
            
        <h2><spring:message code="application.title" /></h2>    
            
        <table>
            <tr>
                <td>
                    <spring:message code="name" />:&nbsp;
                </td>
                <td>
                    <c:out value="${application.person.lastName}" />&nbsp;<c:out value="${application.person.firstName}" />
                </td> 
            </tr>
            <tr>
                <td colspan="2">&nbsp;</td>
            </tr>
            <tr>
                <td>
                    <spring:message code="application.application" />
                </td>
                <td>
                    <spring:message code="${application.vacationType.vacationTypeName}" />
                </td>
            </tr>
            <tr>
                <td colspan="2">&nbsp;</td>
            </tr>
            <tr>
                <td>
                    <spring:message code="overview.vac.time" />
                </td>
                <td>
                   <c:choose>
                                <c:when test="${application.startDate == application.endDate}">
                                    am&nbsp;<joda:format style="M-" value="${application.startDate}"/>
                                </c:when>
                                <c:otherwise>
                                    <joda:format style="M-" value="${application.startDate}"/>&nbsp;-&nbsp;<joda:format style="M-" value="${application.endDate}"/>
                                </c:otherwise>    
                   </c:choose> 
                </td>
            </tr>
            <tr>
                <td colspan="2">&nbsp;</td>
            </tr>
            <tr>
                <td>
                    <label for="grund"><spring:message code='application.reason' />:</label>
                </td>
                <td>
                    <c:out value="${application.reason}" />
                </td>
            </tr>
            <tr>
                <td>
                   <label for="vertreter"><spring:message code='application.vertreter' />:</label> 
                </td>
                <td>
                    <c:out value="${application.rep}" />
                </td>
            </tr>
            <tr>
                <td>
                    <label for="anschrift"><spring:message code='application.anschrift' />:</label>
                </td>
                <td colspan="4">
                    <c:out value="${application.address}" />
                </td>
            </tr>
            <tr>
                <td>
                    <label for="telefon"><spring:message code='application.phone' />:</label>
                </td>
                <td colspan="4">
                    <c:out value="${application.phone}" />
                </td>
            </tr>
            <tr>
                <td colspan="2">&nbsp;</td>
            </tr>
            <tr>
                <td>
                    <spring:message code='application.ort' />&nbsp;<joda:format style="M-" value="${application.applicationDate}"/>
                </td>
                <td>&nbsp;</td>
            </tr>
            <tr>
                <td colspan="2">&nbsp;</td>
            </tr>
        </table>
                
       <form:form method="put" action="${formUrlPrefix}/application/${application.id}/allow"> 
            <input type="submit" name="<spring:message code='application.state.ok' />" value="<spring:message code='application.state.ok' />" class="button" />    
       </form:form>  
       
       <form:form method="put" action="${formUrlPrefix}/application/${application.id}/reject"> 
            <input type="submit" name="<spring:message code='application.state.no' />" value="<spring:message code='application.state.no' />" class="button" />      
       </form:form>
       
        </div>
        
    </body>
    
</html>
