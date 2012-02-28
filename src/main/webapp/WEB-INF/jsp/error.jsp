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
        <title><spring:message code="title" /></title>
        <%@include file="./include/header.jsp" %>
    </head>
    
    <body>
        
        <spring:url var="formUrlPrefix" value="/web" />
        
        <%@include file="./include/menu_header.jsp" %>
        
        <div class="container_12">
            <div class="grid_12" id="errorpage">
                <a href="${formUrlPrefix}/overview" id="error-link">&nbsp;</a>
            </div>
        </div>
    </body>
    
</html>
