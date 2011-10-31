<%-- 
    Document   : antragform
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
    
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title><spring:message code="title" /></title>
    </head>
    
    <body>
        
        <spring:url var="formUrlPrefix" value="/web/urlaubsverwaltung" />
        
        <h1><spring:message code="antrag.title" /></h1>
        
        <form:form method="post" action="${formUrlPrefix}/antrag/${person.id}/new" modelAttribute="antrag"> 
            <form:hidden path="id"/>
            
        <table>
            <tr>
                <td>
                    <spring:message code="lastname" />:&nbsp;<c:out value="${person.lastName}" />
                </td>
                <td>
                    <spring:message code="firstname" />:&nbsp;<c:out value="${person.firstName}" />
                </td> 
                <td colspan="3">&nbsp;</td>
            </tr>
            <tr>
                <td colspan="2">
                    <spring:message code="antrag.anspruch" />:
                </td>    
                <td>    
                    <c:out value="${person.remainingVacationDays}" />&nbsp;<spring:message code="days" />
                </td>
                <td colspan="2">&nbsp;</td>
            </tr>
            <tr>
                <td colspan="2">
                  <spring:message code="resturlaub" />  
                </td>
                <td>
                    <c:out value="${person.restUrlaub}" />:&nbsp;<spring:message code="resturlaub" />&nbsp;<spring:message code="days" />
                </td>
                <td colspan="2">&nbsp;</td>
            </tr>
            <tr>
                <td colspan="2">
                   <spring:message code="antrag.used" />:&nbsp; 
                </td>
                <td>
                    <c:out value="${person.usedVacationDays}" />&nbsp;+&nbsp;<c:out value="${person.usedRestUrlaub}" />&nbsp;<spring:message code="days" />
                </td>
                <td colspan="2">&nbsp;</td>
            </tr>
            <tr>
                <td colspan="5">&nbsp;</td>
            </tr>
            <tr>
                <td colspan="5">
                    <spring:message code="antrag.antrag" />
                </td>
            </tr>
            <tr>
                <td colspan="2">
                   <select id="type" name="type" size="1" items="${vacTypes}" itemLabel="text">
                        <c:forEach items="${vacTypes}" var="vacType">
                                <option value="${vacType}">
                                    <c:out value='${vacType.vacationTypeName}' />
                                </option>
                        </c:forEach>
                    </select>
                </td>
                <td>&nbsp;</td>
                <td>
                    <label for="grund"><spring:message code='antrag.reason' />:</label>
                </td>
                <td>
                    <form:input id="reason" path="reason" />
                </td>
            </tr>
            <tr>
                <!-- hierhin kommt der DatePicker + Tage gesamt + Urlaubstage + verbleibender Urlaubsanspruch -->
            </tr>
            <tr>
                <td>
                   <label for="vertreter"><spring:message code='antrag.vertreter' />:</label> 
                </td>
                <td colspan="2">
                    <select id="person" name="person" size="1" items="${mitarbeiter}" itemLabel="text">
                        <c:forEach items="${mitarbeiter}" var="einmitarbeiter">
                                <option value="${einmitarbeiter}">
                                    <c:out value='${einmitarbeiter.lastName}' />&nbsp;<c:out value="${einmitarbeiter.firstName}" />
                                </option>
                        </c:forEach>
                    </select>
                </td>
                <td colspan="2">&nbsp;</td>
            </tr>
            <tr>
                <td>
                    <label for="anschrift"><spring:message code='antrag.anschrift' />:</label>
                </td>
                <td colspan="4">
                    <form:input id="anschrift" path="anschrift" />
                </td>
            </tr>
            <tr>
                <td>
                    <label for="telefon"><spring:message code='antrag.phone' />:</label>
                </td>
                <td colspan="4">
                    <form:input id="phone" path="phone" />
                </td>
            </tr>
            <tr>
                <td colspan="2">
                    <spring:message code='antrag.ort' />&nbsp;<c:out value="${date}" />
                </td>
                <td>&nbsp;</td>
                <td colspan="2">
                    <spring:message code='antrag.ort' />&nbsp;
                </td>
            </tr>
            <tr>
                <td colspan="2">
                    <!-- Hier kommt das Bild der Unterschrift des Users rein  -->
                </td>
                <td>&nbsp;</td>
                <td colspan="2">
                    &nbsp;
                    <!--  bleibt hier erstmal leer, Platz fuer Chef Unterschrift  -->
                </td>
            </tr>
            <tr>
                <td colspan="2">
                    <spring:message code='antrag.sign' />  
                </td>
                <td>&nbsp;</td>
                <td colspan="2">
                    <spring:message code='antrag.sign.chef' /> 
                </td>
            </tr>
            
        </table>
                
        </form:form>        
        
    </body>
    
</html>
