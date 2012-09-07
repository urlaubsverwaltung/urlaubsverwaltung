<%-- 
    Document   : app_detail
    Created on : 09.01.2012, 10:12:13
    Author     : Aljona Murygina
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html>

    <head>
        <%@include file="../include/header.jsp" %>
        <title><spring:message code="title" /></title>
    </head>

    <body>

        <spring:url var="formUrlPrefix" value="/web" />

        <%@include file="../include/menu_header.jsp" %>

        <div id="content">
            <div class="container_12">

                <div class="grid_12">&nbsp;</div>


                <div class="grid_12">

                    <table class="overview-header">
                        <tr>
                            <td><spring:message code="app.title" /></td>
                        </tr>
                    </table>
                </div>

                <div class="grid_6">

                    <%@include file="./include/app-detail-elements/app_info.jsp" %>

                    <div style="margin-bottom: 8em">
                    
                    <c:choose>
                        <c:when test="${application.person.id == loggedUser.id}">
                            <%@include file="./include/app-detail-elements/actions_user.jsp" %>
                        </c:when>
                        <c:otherwise>
                            <sec:authorize access="hasRole('role.boss')">
                                <%@include file="./include/app-detail-elements/actions_boss.jsp" %>
                            </sec:authorize>
                            <sec:authorize access="hasRole('role.office')">
                                <%@include file="./include/app-detail-elements/actions_office.jsp" %>
                            </sec:authorize>
                        </c:otherwise>
                    </c:choose>
                        
                    </div>

                </div>

                <div class="grid_6">

                    <table class="app-detail" cellspacing="0">
                        <tr class="odd">
                            <th><c:out value="${application.person.firstName} ${application.person.lastName}" /></th>
                            <td><c:out value="${application.person.email}" /></td>
                        </tr>
                        <%@include file="./include/account_days_for_app_view.jsp" %>
                    </table>

                    <%@include file="./include/app-detail-elements/app_progress.jsp" %>

                </div>

            </div> <!-- end of grid container -->

        </div> <!-- end of content -->

    </body>

</html>
