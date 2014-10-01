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

    <spring:url var="formUrlPrefix" value="/web"/>
    
        <uv:menu />

        <div class="print-info--only-portrait">
            <h4><spring:message code="print.info.portrait" /></h4>
        </div>

        <div class="content print--only-portrait">
            <div class="container">

                <div class="row">

                <div class="col-xs-12 col-sm-12 col-md-6">

                    <div class="header">

                        <legend>
                            <p>
                                <spring:message code="app.title" />
                            </p>
                            <jsp:include page="./include/app-detail-elements/action-buttons.jsp" />
                        </legend>

                    </div>

                    <div class="feedback">
                        <c:choose>
                            <c:when test="${remindAlreadySent == true}">
                            <div class="alert alert-danger">
                                <spring:message code='application.action.remind.error.alreadySent' />
                            </div>
                            </c:when>
                            <c:when test="${remindNoWay == true}">
                                <div class="alert alert-danger">
                                    <spring:message code='application.action.remind.error.impatient' />
                                </div>
                            </c:when>
                            <c:when test="${remindIsSent == true}">
                                <div class="alert alert-success">
                                    <spring:message code='application.action.remind.success'/>
                                </div>
                            </c:when>
                            <c:when test="${!empty errors}">
                                <div class="alert alert-danger">
                                    <spring:message code="application.action.reason.error" />
                                </div>
                            </c:when>
                            <c:when test="${allowSuccess == true}">
                                <div class="alert alert-success">
                                    <spring:message code="application.action.allow.success" />
                                </div>
                            </c:when>
                            <c:when test="${rejectSuccess == true}">
                                <div class="alert alert-success">
                                    <spring:message code="application.action.reject.success" />
                                </div>
                            </c:when>
                            <c:when test="${referSuccess == true}">
                                <div class="alert alert-success">
                                    <spring:message code="application.action.refer.success" />
                                </div>
                            </c:when>
                            <c:when test="${cancelSuccess == true}">
                                <div class="alert alert-success">
                                    <spring:message code="application.action.cancel.success" />
                                </div>
                            </c:when>
                        </c:choose>
                    </div>

                    <div class="actions">
                        <jsp:include page="./include/app-detail-elements/actions.jsp" />
                    </div>

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
                        <h4>
                            <a href="${formUrlPrefix}/staff/${application.person.id}/overview">
                                <c:out value="${application.person.niceName}"/>
                            </a>
                        </h4>
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
