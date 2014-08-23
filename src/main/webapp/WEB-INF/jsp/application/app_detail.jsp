<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<!DOCTYPE html>
<html>

    <head>
        <uv:head />
    </head>

    <body>

    <c:set var="isOffice" value="false" />
    <c:forEach var="item" items="${loggedUser.permissions}">
        <c:if test="${item eq 'OFFICE'}">
            <c:set var="isOffice" value="true" />
        </c:if>
    </c:forEach>
    
        <spring:url var="formUrlPrefix" value="/web" />

        <uv:menu />

        <div class="content">
            <div class="grid-container">

                <div class="grid-100">

                    <div class="overview-header">

                        <legend>
                            <p>
                                <spring:message code="app.title" />
                            </p>

                            <div style="float: right; display: inline-block">

                                <sec:authorize access="hasAnyRole('BOSS', 'OFFICE')">
                                    <c:if test="${application.person.id != loggedUser.id}">
                                        <%@include file="./include/app-detail-elements/actions/back_to_member.jsp" %>
                                    </c:if>
                                </sec:authorize>

                                <sec:authorize access="hasRole('USER')">
                                <%@include file="./include/app-detail-elements/actions/print.jsp" %>
                                </sec:authorize>

                            </div>    
                        </legend>

                    </div>
                    
                </div>

                <div class="grid-50 print-box">
                    
                    <%@include file="./include/app-detail-elements/app_info.jsp" %>

                    <div style="margin-top: 0.5em">
                        
                        <sec:authorize access="hasRole('USER')">
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
                        
                        <sec:authorize access="hasRole('BOSS')">
                            <c:if test="${application.person.id != loggedUser.id && application.status.number == 0}">
                                <div class="btn-group" style="float:left; margin-right: 0.5em">
                                    <a class="btn btn-primary dropdown-toggle" data-toggle="dropdown" href="#">
                                        <i class="icon-asterisk icon-white"></i>
                                        <spring:message code="process" />
                                        <span class="caret"></span>
                                    </a>
                                    <ul class="dropdown-menu">
                                        <li>
                                            <a href="#" onclick="$('#reject').hide(); $('#refer').hide();  $('#cancel').hide(); $('#confirm').show();">
                                                <i class="icon-ok"></i>&nbsp;<spring:message code='app.state.ok.short' />
                                            </a>
                                        </li>
                                        <li>
                                            <a href="#" onclick="$('#refer').hide(); $('#confirm').hide();  $('#cancel').hide(); $('#reject').show();">
                                                <i class="icon-ban-circle"></i>&nbsp;<spring:message code='app.state.no.short' />
                                            </a>
                                        </li>
                                        <li>
                                            <a href="#" onclick="$('#reject').hide(); $('#confirm').hide(); $('#cancel').hide(); $('#refer').show();">
                                                <i class="icon-share"></i>&nbsp;<spring:message code='app.state.refer.short' />
                                            </a>
                                        </li>
                                    </ul>
                                </div>
                            </c:if>
                        </sec:authorize>

                        <sec:authorize access="hasRole('OFFICE')">
                            <c:if test="${application.person.id != loggedUser.id && (application.status.number == 0 || application.status.number == 1)}">
                                <%@include file="./include/app-detail-elements/actions/cancel_for_other.jsp" %>
                            </c:if>
                        </sec:authorize>
                        
                    </div>
                    
                    <div class="actions" style="margin-bottom: 8em">

                        <%-- permission dependant forms to the buttons above START --%>

                        <sec:authorize access="hasRole('USER')">

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

                        <sec:authorize access="hasRole('BOSS')">
                                <c:if test="${application.person.id != loggedUser.id && application.status.number == 0}">
                                    <%@include file="./include/app-detail-elements/actions/allow_form.jsp" %>
                                    <%@include file="./include/app-detail-elements/actions/reject_form.jsp" %>
                                    <%@include file="./include/app-detail-elements/actions/refer_form.jsp" %>
                                </c:if>
                        </sec:authorize>

                        <sec:authorize access="hasRole('OFFICE')">
                                <c:if test="${application.person.id != loggedUser.id && (application.status.number == 0 || application.status.number == 1)}">
                                    <%@include file="./include/app-detail-elements/actions/cancel_for_other_form.jsp" %>
                                </c:if>
                        </sec:authorize>    

                        <%-- permission dependant forms to the buttons above END --%>

                    </div>

                </div>

                <div class="grid-50 print-box">

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
