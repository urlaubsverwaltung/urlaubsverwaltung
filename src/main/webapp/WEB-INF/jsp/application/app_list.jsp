<%-- 
    Document   : app_list_boss
    Created on : 13.02.2012, 14:31:35
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
        <title><spring:message code="title" /></title>
        <%@include file="../include/header.jsp" %>
    </head>

    <body>

        <spring:url var="formUrlPrefix" value="/web" />
        <c:set var="linkPrefix" value="${formUrlPrefix}/application" />

        <%@include file="../include/menu_header.jsp" %>

        <div id="content">

            <div class="container_12">

                <div class="grid_12">

                    <%@include file="./include/app-list-elements/list-header.jsp" %> 

                    <%@include file="./include/app-list-elements/list.jsp" %> 

                </div>
            </div>
        </div>            

    </body>

</html>


