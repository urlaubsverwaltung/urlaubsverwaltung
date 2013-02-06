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

                <div class="grid_12">

                    <table class="overview-header">
                        <tr>
                            <td><spring:message code="app.title" /></td>
                        </tr>
                    </table>
                </div>

                <div class="grid_6">

                    <%@include file="./include/app-detail-elements/app_info.jsp" %>

                    <div class="actions" style="margin-bottom: 8em">

                        <c:set var="isOffice" value="false" />
                        <c:forEach var="item" items="${loggedUser.permissions}">
                            <c:if test="${item eq 'OFFICE'}">
                                <c:set var="isOffice" value="true" />
                            </c:if>
                        </c:forEach>

                        <%-- permission dependant buttons START --%>
                        
                        <sec:authorize access="hasRole('role.user')">
                            <%@include file="./include/app-detail-elements/actions/print.jsp" %>
                            <c:if test="${application.person.id == loggedUser.id && application.status.number == 0}">
                                <%@include file="./include/app-detail-elements/actions/remind.jsp" %>
                            </c:if>
                            
                            <%-- if role is office then allowed applications for leave may be cancelled --%>
                            
                            <c:choose>
                                <c:when test="${isOffice}">
                                    <c:if test="${application.person.id == loggedUser.id && (application.status.number == 0 || application.status.number == 1)}">
                                        <%@include file="./include/app-detail-elements/actions/cancel.jsp" %>
                                    </c:if>
                                </c:when>
                                <c:otherwise>
                                    <c:if test="${application.person.id == loggedUser.id && application.status.number == 0}">
                                        <%@include file="./include/app-detail-elements/actions/cancel.jsp" %>
                                    </c:if>
                                </c:otherwise>
                            </c:choose>
                            
                        </sec:authorize>
                        
                        <sec:authorize access="hasRole('role.boss')">
                            <c:if test="${application.person.id != loggedUser.id && application.status.number == 0}">
                                <%@include file="./include/app-detail-elements/actions/allow.jsp" %>
                                <%@include file="./include/app-detail-elements/actions/reject.jsp" %>
                                <%@include file="./include/app-detail-elements/actions/refer.jsp" %>
                            </c:if>
                        </sec:authorize> 
                        
                        <sec:authorize access="hasRole('role.office')">
                            <c:if test="${application.person.id != loggedUser.id && (application.status.number == 0 || application.status.number == 1)}">
                                <%@include file="./include/app-detail-elements/actions/cancel_for_other.jsp" %>
                            </c:if>
                            <c:if test="${application.person.id != loggedUser.id}">
                                <%@include file="./include/app-detail-elements/actions/back_to_member.jsp" %>
                            </c:if>
                        </sec:authorize>

                        <%-- permission dependant buttons END --%>

                        <%-- permission dependant forms to the buttons above START --%>

                        <sec:authorize access="hasRole('role.user')">

                            <%-- if role is office then allowed applications for leave may be cancelled --%>

                            <c:choose>
                                <c:when test="${isOffice}">
                                    <c:if test="${application.person.id == loggedUser.id && (application.status.number == 0 || application.status.number == 1)}">
                                        <%@include file="./include/app-detail-elements/actions/cancel_form.jsp" %>
                                    </c:if>
                                </c:when>
                                <c:otherwise>
                                    <c:if test="${application.person.id == loggedUser.id && application.status.number == 0}">
                                        <%@include file="./include/app-detail-elements/actions/cancel_form.jsp" %>
                                    </c:if>
                                </c:otherwise>
                            </c:choose>

                        </sec:authorize>

                        <sec:authorize access="hasRole('role.boss')">
                                <c:if test="${application.person.id != loggedUser.id && application.status.number == 0}">
                                    <%@include file="./include/app-detail-elements/actions/allow_form.jsp" %>
                                    <%@include file="./include/app-detail-elements/actions/reject_form.jsp" %>
                                    <%@include file="./include/app-detail-elements/actions/refer_form.jsp" %>
                                </c:if>
                        </sec:authorize>

                        <sec:authorize access="hasRole('role.office')">
                                <c:if test="${application.person.id != loggedUser.id && (application.status.number == 0 || application.status.number == 1)}">
                                    <%@include file="./include/app-detail-elements/actions/cancel_for_other_form.jsp" %>
                                </c:if>
                        </sec:authorize>    

                        <%-- permission dependant forms to the buttons above END --%>

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
