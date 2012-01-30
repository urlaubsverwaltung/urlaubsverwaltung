<%-- 
    Document   : staff_view
    Created on : 17.01.2012, 11:09:37
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
        <script type="text/javascript">
            $(document).ready(function() {
                var path = window.location.pathname;
            
                if(path.indexOf("inactive") != -1) {
                    document.getElementById("staff-inact").setAttribute("class", "a-act");
                    document.getElementById("staff-act").setAttribute("class", "a-inact");
                } else {
                    document.getElementById("staff-inact").setAttribute("class", "a-inact");
                    document.getElementById("staff-act").setAttribute("class", "a-act");
                }
            });
        </script>
        <link rel="stylesheet" type="text/css" href="<spring:url value='/css/fluid_grid.css' />" />
        <link rel="stylesheet" type="text/css" href="<spring:url value='/css/main.css' />" /> 
        <title><spring:message code="title" /></title>
    </head>

    <body>

        <spring:url var="formUrlPrefix" value="/web" />

        <%@include file="../include/header.jsp" %>

        <div id="show-navi">
            <a href="${formUrlPrefix}/staff" id="staff-act"><spring:message code="table.active" /></a>
            <a href="${formUrlPrefix}/staff/inactive" id="staff-inact"><spring:message code="table.inactive" /></a>
        </div>

        <div id="content">
            <div class="container_12">

            <c:choose>

                <c:when test="${notexistent == true}">
                    
                    <br />
                    <br />
                    <spring:message code="table.empty" />

                </c:when>

                <c:otherwise>
                    
                    <a href="?year=2011">2011</a>
                <a href="?year=2012">2012</a>
                <a href="?year=2013">2013</a>    

                    <br />
                    <br />

                        <%@include file="./include/staff_list.jsp" %>
                    
                </c:otherwise>    
                    
            </c:choose>

                    </div> 
        </div>        

    </body>

</html>
