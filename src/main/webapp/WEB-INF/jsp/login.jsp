<%-- 
    Document   : login
    Created on : 31.10.2011, 10:00:23
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
        
        <form method="post" action="j_security_check">
            
            <table>
                <tr>
                   <td><label for="username"><spring:message code='username' />:</label></td> 
                   <td><input id="username" type="text" name="j_username" /></td>    
                <tr/>
                <tr>
                   <td><label for="password"><spring:message code='pw' />:</label></td>
                   <td><input id="password" type="password" name="j_password" /></td>
                </tr>    
                <tr>
                   <td colspan="2">&nbsp;</td>
                </tr>
                <tr>
                   <td>&nbsp;</td>
                   <td><input type="submit" name="<spring:message code='login' />" value="<spring:message code='login' />" /></td>
                </tr>
             </table>
                
        </form>   
        
    </body>
    
</html>
