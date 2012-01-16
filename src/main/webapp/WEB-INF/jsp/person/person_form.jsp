<%-- 
    Document   : person_form
    Created on : 31.10.2011, 10:00:10
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
        <script src="<spring:url value='/jquery/js/jquery-1.6.2.min.js' />" type="text/javascript" ></script>
        <script src="<spring:url value='/jquery/js/jquery-ui-1.8.16.custom.min.js' />" type="text/javascript" ></script>
        <link rel="stylesheet" type="text/css" href="<spring:url value='/css/main.css' />" />
        <title><spring:message code="title" /></title>
    </head>
    
    <body>
        
        <%@include file="../include/header.jsp" %>
        
        <spring:url var="formUrlPrefix" value="/web" />
        
        
        <div id="content">
        
        <form:form method="put" action="${formUrlPrefix}/staff/${person.id}/edit" modelAttribute="personForm"> 
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
                        <td><label for="jahr"><spring:message code="year" />:</label></td>
                        <td><form:input id="jahr" path="year" /></td>
                    </tr>
                    <tr>
                        <td><label for="urlaubsanspruch"><spring:message code="entitlement" />:</label></td>
                        <td><form:input id="urlaubsanspruch" path="vacationDays" /></td>
                    </tr>
                    <tr>
                        <td colspan="2">&nbsp;</td>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <input type="submit" name="<spring:message code="save" />" value="<spring:message code="save" />" />
                            <a class="button" href="${formUrlPrefix}/staff/list"><spring:message code='cancel' /></a>
                            <input type="button" onclick="$('#deactivate').show();" name="<spring:message code='person.deactivate' />" value="<spring:message code='person.deactivate' />" />
                        </td>
                    </tr>
                </table>
       </form:form>
            
          <br />
          <br />
            
        <form:form method="put" action="${formUrlPrefix}/staff/${person.id}/deactivate">                  
            <div id="deactivate"style="display: none;">
                <spring:message code='person.deactivate.confirm' />&nbsp;
                    <input type="submit" name="<spring:message code="yes" />" value="<spring:message code="yes" />" />
                    <input type="button" onclick="$('#deactivate').hide();" name="<spring:message code="no" />" value="<spring:message code="no" />" /> 
                </div>         
        </form:form>
            
        </div>    
        
    </body>
    
</html>
