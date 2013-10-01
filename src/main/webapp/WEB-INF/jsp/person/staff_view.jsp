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
        <title><spring:message code="title" /></title>
        <%@include file="../include/header.jsp" %>
        <script type="text/javascript">
            $(document).ready(function() {
                var path = window.location.pathname;

                if(path.indexOf("inactive") != -1) {
                    $("div.status-selector button").html('<spring:message code="table.inactive" />&nbsp;<span class="caret"></span>');
                } else {
                    $("div.status-selector button").html('<spring:message code="table.active" />&nbsp;<span class="caret"></span>')
                }
            });
        </script>
    </head>

    <body>

        <spring:url var="formUrlPrefix" value="/web" />

        <%@include file="../include/menu_header.jsp" %>

        <div id="content">
            <div class="container_12">

                <div class="grid_12">

                    <c:choose>
                        <c:when test="${!empty param.year}">
                            <c:set var="displayYear" value="${param.year}" />
                        </c:when>
                        <c:otherwise>
                            <c:set var="displayYear" value="${year}" />
                        </c:otherwise>
                    </c:choose>

                    <div class="overview-header">

                        <legend style="margin-bottom: 17px">
                            
                            <p>
                                <spring:message code="table.overview" /><c:out value="${displayYear}" />
                            </p>

                            <jsp:include page="../include/year_selector.jsp" />

                            <div class="btn-group status-selector">

                                <button class="btn dropdown-toggle" data-toggle="dropdown">
                                </button>

                                <ul class="dropdown-menu">
                                    <li><a href="${formUrlPrefix}/staff"><spring:message code="table.active" /></a></li>
                                    <li><a href="${formUrlPrefix}/staff/inactive"><spring:message code="table.inactive" /></a></li>
                                </ul>

                            </div>
                            
                        </legend>

                    </div>
                    
                    <c:choose>

                        <c:when test="${notexistent == true}">

                            <spring:message code="table.empty" />

                        </c:when>

                        <c:otherwise>
                            <%@include file="./include/staff_list.jsp" %>
                        </c:otherwise>

                    </c:choose>

                </div>
            </div> 
        </div>        

    </body>

</html>
