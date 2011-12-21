<%-- 
    Document   : antragdetailchef
    Created on : 02.11.2011, 17:19:17
    Author     : aljona
--%>


<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>


<!DOCTYPE html>
<html>
    
    <!--  Ansicht des Urlaubsantrags fuer Office: kann Krankheitstage eintragen oder Antrag drucken -->
    
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title><spring:message code="title" /></title>
    </head>
    
    <body>
        
        <spring:url var="formUrlPrefix" value="/web/urlaubsverwaltung" />
        
        <h1><spring:message code="antrag.title" /></h1>
            
        
                
        <!--    Button um Antrag zu drucken      -->
        
        <!--    Button um Antrag zu editieren    -->
        
    </body>
    
</html>
