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
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>


<!DOCTYPE html>
<html>

    <head>
        <uv:head />

        <spring:url var="formUrlPrefix" value="/web" />

        <c:choose>
            <c:when test="${!empty param.year}">
                <c:set var="displayYear" value="${param.year}" />
            </c:when>
            <c:otherwise>
                <c:set var="displayYear" value="${year}" />
            </c:otherwise>
        </c:choose>
        
        <script type="text/javascript">
            $(document).ready(function() {

                $("table.sortable").tablesorter({
                    sortList: [[2,0]],
                    headers: {
                      5: { sorter: 'commaNumber' }, 
                      6: { sorter: 'commaNumber' }
                    }
                });
                
                var path = window.location.pathname;

                var active;
                
                if(path.indexOf("inactive") != -1) {
                    $("div#active-state button").html('<img src="<spring:url value='/images/offline.png' />" />&nbsp;<spring:message code="table.inactive" />&nbsp;<span class="caret"></span>');
                } else {
                    $("div#active-state button").html('<img src="<spring:url value='/images/online.png' />" />&nbsp;<spring:message code="table.active" />&nbsp;<span class="caret"></span>');
                }

            });
        </script>
    </head>

    <body>

        <uv:menu />

        <div class="content">
            <div class="grid-container">

                <div class="grid-100">

                    <div class="header">

                        <legend style="margin-bottom: 17px">
                            
                            <p>
                                <spring:message code="table.overview" /><c:out value="${displayYear}" />
                            </p>

                            <uv:year-selector year="${year}" />

                            <div id="active-state" class="btn-group selector">

                                <button class="btn dropdown-toggle" data-toggle="dropdown">
                                </button>

                                <ul class="dropdown-menu">
                                    <li>
                                        <a href="${formUrlPrefix}/staff">
                                            <img src="<spring:url value='/images/online.png' />" />
                                            <spring:message code="table.active" />
                                        </a>
                                    </li>
                                    <li>
                                        <a href="${formUrlPrefix}/staff/inactive">
                                            <img src="<spring:url value='/images/offline.png' />" />
                                            <spring:message code="table.inactive" />
                                        </a>
                                    </li>
                                </ul>

                            </div>

                            <uv:print />
                            
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
