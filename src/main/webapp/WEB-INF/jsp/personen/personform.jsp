<%-- 
    Document   : personform
    Created on : 31.10.2011, 10:00:10
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
        
        <spring:url var="formUrlPrefix" value="/web" />
        
        <c:choose>
            <c:when test="${person.id == null}">
                <c:set var="method" value="post" />
                <c:set var="url" value="${formUrlPrefix}/mitarbeiter/new" />
            </c:when>
            <c:otherwise>
                <c:set var="method" value="put" />
                <c:set var="url" value="${formUrlPrefix}/mitarbeiter/${person.id}/edit" />
            </c:otherwise>
        </c:choose>
        
        <form:form method="${method}" action="${url}" modelAttribute="person"> 
            <form:hidden path="id" />
                <table>
                    <tr>
                        <td><label for="username"><spring:message code="username" />:</label></td>
                        <td><form:input id="username" path="loginName" /></td>
                    </tr>
                    <tr>
                        <td><label for="nachname"><spring:message code="lastname" />:</label></td>
                        <td><form:input id="nachname" path="lastName" /></td>
                    </tr>
                    <tr>
                        <td><label for="vorname"><spring:message code="firstname" />:</label></td>
                        <td><form:input id="vorname" path="firstName" /></td>
                    </tr>
                    <tr>
                        <td><label for="email"><spring:message code="email" />:</label></td>
                        <td><form:input id="email" path="email" /></td>
                    </tr>
                    <tr>
                        <td><label for="urlaubsanspruch"><spring:message code="anspruch" />:</label></td>
                        <td><form:input id="urlaubsanspruch" path="currentUrlaubsanspruch" /></td>
                    </tr>
                    <tr>
                        <td><label for="jahr"><spring:message code="jahr" />:</label></td>
                        <td><form:input id="jahr" path="yearForCurrentUrlaubsanspruch" /></td>
                    </tr>
                    <%--
                    <c:if test="${person.id == null}">
                    <tr>
                        <td><label for="unterschrift"><spring:message code="sign" />:</label></td>
                        <td><input type="file" name="sign" id="unterschrift" size="17%" /></td>
                    </tr>
                    </c:if>
                    --%>
                    <tr>
                        <td>
                            <input type="submit" name="<spring:message code="save" />" value="<spring:message code="save" />" />                        
                        </td>
                        <td>
                            <!--  weiss noch nicht, wo der Link hin zeigen soll -->
                            <a href=""><spring:message code="cancel" /></a>
                        </td>
                    </tr>
                </table>
        </form:form>
        
    </body>
    
</html>
