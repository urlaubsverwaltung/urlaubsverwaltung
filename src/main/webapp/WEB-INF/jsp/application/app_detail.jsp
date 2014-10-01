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
            <div class="container">

                <div class="row">

                <div class="col-xs-12 col-sm-12 col-md-6">

                    <div class="header">

                        <legend>
                            <p>
                                <spring:message code="app.title" />
                            </p>

                        </legend>

                    </div>

                    <div class="action-buttons">

                        <sec:authorize access="hasAnyRole('BOSS', 'OFFICE')">
                            <c:if test="${application.person.id != loggedUser.id}">
                                <%@include file="./include/app-detail-elements/actions/back_to_member.jsp" %>
                            </c:if>
                        </sec:authorize>

                        <sec:authorize access="hasRole('USER')">
                            <uv:print />
                        </sec:authorize>

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

                        <sec:authorize access="hasRole('OFFICE')">
                            <c:if test="${application.person.id != loggedUser.id && (application.status.number == 0 || application.status.number == 1)}">
                                <%@include file="./include/app-detail-elements/actions/cancel_for_other.jsp" %>
                            </c:if>
                        </sec:authorize>

                        <sec:authorize access="hasRole('BOSS')">
                            <c:if test="${application.status.number == 0}">
                                <div class="btn-group pull-right">
                                    <a class="btn btn-primary dropdown-toggle" data-toggle="dropdown" href="#">
                                        <i class="fa fa-edit"></i>
                                        <spring:message code="process" />
                                        <span class="caret"></span>
                                    </a>
                                    <ul class="dropdown-menu">
                                        <li>
                                            <a href="#" onclick="$('#reject').hide(); $('#refer').hide();  $('#cancel').hide(); $('#confirm').show();">
                                                <i class="fa fa-check"></i>&nbsp;<spring:message code='app.state.ok.short' />
                                            </a>
                                        </li>
                                        <li>
                                            <a href="#" onclick="$('#refer').hide(); $('#confirm').hide();  $('#cancel').hide(); $('#reject').show();">
                                                <i class="fa fa-ban"></i>&nbsp;<spring:message code='app.state.no.short' />
                                            </a>
                                        </li>
                                        <li>
                                            <a href="#" onclick="$('#reject').hide(); $('#confirm').hide(); $('#cancel').hide(); $('#refer').show();">
                                                <i class="fa fa-mail-forward"></i>&nbsp;<spring:message code='app.state.refer.short' />
                                            </a>
                                        </li>
                                    </ul>
                                </div>
                            </c:if>
                        </sec:authorize>

                    </div>
                    
                    <div class="actions">

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
                                <c:if test="${application.status.number == 0}">
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

                    </div><%--End of actions --%>

                    <%@include file="./include/app-detail-elements/app_info.jsp" %>

                    <div class="header">
                        <legend>
                            <p><spring:message code="progress" /></p>
                        </legend>
                    </div>

                    <%@include file="./include/app-detail-elements/app_progress.jsp" %>

                </div><%--End of first column--%>

                <div class="col-xs-12 col-sm-12 col-md-6 hidden-print">

                    <div class="header">
                        <legend>
                            <p><spring:message code="staff" /></p>
                        </legend>
                    </div>

                    <div class="box">
                    <span class="thirds">
                        <img class="box-image img-circle" src="<c:out value='${gravatar}?d=mm&s=80'/>"/>
                        <i class="fa fa-at"></i> <c:out value="${application.person.loginName}"/>
                        <h4><c:out value="${application.person.niceName}"/></h4>
                        <i class="fa fa-envelope"></i> <c:out value="${application.person.email}"/>
                    </span>
                    </div>

                    <div class="box">
                        <span class="box-icon bg-green"><i class="fa fa-calendar"></i></span>
                        <c:choose>
                            <c:when test="${account != null}">
                            <span class="thirds">
                                <spring:message code="overview.vacation.entitlement" arguments="${account.vacationDays}" />
                                <spring:message code="overview.vacation.entitlement.remaining" arguments="${account.remainingVacationDays}" />
                            </span>
                            </c:when>
                            <c:otherwise>
                                <span class="one"><spring:message code='not.specified'/></span>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <div class="box">
                        <span class="box-icon bg-green"><i class="fa fa-bar-chart"></i></span>
                        <c:choose>
                            <c:when test="${account != null}">
                            <span class="thirds">
                                <spring:message code="overview.vacation.left" arguments="${leftDays}" />
                                <c:choose>
                                    <c:when test="${beforeApril || !account.remainingVacationDaysExpire}">
                                        <spring:message code="overview.vacation.left.remaining" arguments="${remLeftDays}" />
                                    </c:when>
                                    <c:otherwise>
                                        <spring:message code="overview.vacation.left.remaining.expired" />
                                    </c:otherwise>
                                </c:choose>
                            </span>
                            </c:when>
                            <c:otherwise>
                                <span class="one"><spring:message code='not.specified'/></span>
                            </c:otherwise>
                        </c:choose>
                    </div>

                </div><!-- End of second column -->

            </div><!-- End of row -->

            </div> <!-- end of grid container -->

        </div> <!-- end of content -->

    </body>

</html>
