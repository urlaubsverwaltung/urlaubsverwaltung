<%-- 
    Document   : app_form
    Created on : 26.10.2011, 15:05:51
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
        
        <div id="content">
        
        <h2><spring:message code="antrag.title" /></h2>
        
        <br />
        
        <form:form method="post" action="${formUrlPrefix}/application/${person.id}/new" modelAttribute="application"> 
            <form:hidden path="id" />
            
        <table>
            <tr>
                <td>
                    <spring:message code="name" />:&nbsp;
                </td>
                <td>
                    <c:out value="${person.lastName}" />,&nbsp;<c:out value="${person.firstName}" />
                </td> 
            </tr>
            <tr>
                <td>
                    <spring:message code="antrag.anspruch" />:
                </td>    
                <td>    
                    <c:out value="${account.vacationDays}" />&nbsp;<spring:message code="days" />
                </td>
            </tr>
            <tr>
                <td>
                  <spring:message code="resturlaub" />:  
                </td>
                <td>
                    <c:out value="${account.remainingVacationDays}" />&nbsp;<spring:message code="days" />
                </td>
            </tr>
            <tr>
                <td colspan="3">&nbsp;</td>
            </tr>
            <tr>
                <td colspan="3">&nbsp;</td>
            </tr>
            <tr>
                <td>
                    <spring:message code="antrag.antrag" />
                </td>
                <td>
                   <select id="type" name="type" size="1" items="${vacTypes}" itemLabel="text">
                        <c:forEach items="${vacTypes}" var="vacType">
                                <option value="${vacType}">
                                    <spring:message code='${vacType.vacationTypeName}' />
                                </option>
                        </c:forEach>
                    </select>
                </td>
            </tr>
            <tr>
                <td colspan="3">&nbsp;</td>
            </tr>
            <tr>
                <td>
                    <label for="reason"><spring:message code='antrag.reason' />:</label>
                </td>
                <td>
                    <form:input id="reason" path="reason" />
                </td>
            </tr>
            <tr>
                <td colspan="3">&nbsp;</td>
            </tr>
            <tr>
                <!-- hierhin kommt der DatePicker + Tage gesamt + Urlaubstage + verbleibender Urlaubsanspruch -->
            </tr>
            <tr>
                <td>
                   <label for="vertreter"><spring:message code='antrag.vertreter' />:</label> 
                </td>
                <td colspan="2">
                    <select id="vertreter" name="vertreter" size="1" items="${persons}" itemLabel="text">
                        <c:forEach items="${persons}" var="einmitarbeiter">
                                <option value="${einmitarbeiter}">
                                    <c:out value='${einmitarbeiter.lastName}' />&nbsp;<c:out value="${einmitarbeiter.firstName}" />
                                </option>
                        </c:forEach>
                    </select>
                </td>
            </tr>
            <tr>
                <td colspan="3">&nbsp;</td>
            </tr>
            <tr>
                <td>
                    <label for="anschrift"><spring:message code='antrag.anschrift' />:</label>
                </td>
                <td colspan="4">
                    <form:input id="anschrift" path="address" />
                </td>
            </tr>
            <tr>
                <td>
                    <label for="telefon"><spring:message code='antrag.phone' />:</label>
                </td>
                <td colspan="4">
                    <form:input id="telefon" path="phone" />
                </td>
            </tr>
            <tr>
                <td colspan="3">&nbsp;</td>
            </tr>
            <tr>
                <td colspan="3">&nbsp;</td>
            </tr>
            <tr>
                <td colspan="2">
                    <spring:message code='antrag.ort' />&nbsp;<c:out value="${date}" /><form:hidden path="applicationDate" />
                </td>
            </tr>
<%--            <tr>
                <td colspan="2">
                    <img src="${person.sign}" />
                </td>
                <td>&nbsp;</td>
                <td colspan="2">
                    &nbsp;
                      bleibt hier erstmal leer, Platz fuer Chef Unterschrift  
                </td>
            </tr>-->
            <tr>
                <td colspan="2">
                    <spring:message code='antrag.sign' />  
                </td>
                <td>&nbsp;</td>
                <td colspan="2">
                    <spring:message code='antrag.sign.chef' /> 
                </td>
            </tr> --%>
            
        </table>
                
        </form:form>    
        
        
        </div>
        
    </body>
    
</html>
