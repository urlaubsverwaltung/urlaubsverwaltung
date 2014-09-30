<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>


<!DOCTYPE html>
<html>
    
    <head>
        <uv:head />
    </head>
    
    <body>
        
        <spring:url var="formUrlPrefix" value="/web" />
        
        <uv:menu />
        
        <div class="container">
            <div class="row">
                <c:choose>
                    <c:when test="${not empty exception.message}">
                        <div class="col-xs-12" id="errorpage-exception">
                            <p><c:out value="${exception.message}" /></p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="col-xs-12" id="errorpage">
                            <a href="${formUrlPrefix}/overview" id="error-link">&nbsp;</a>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </body>
    
</html>
