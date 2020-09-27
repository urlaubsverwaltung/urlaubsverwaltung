<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="asset" uri = "/WEB-INF/asset.tld"%>

<fmt:parseDate value="${application.startDate}" pattern="yyyy-MM-dd" var="parsedStartDate" type="date"/>
<fmt:parseDate value="${application.endDate}" pattern="yyyy-MM-dd" var="parsedEndDate" type="date"/>

<!DOCTYPE html>
<html lang="${language}">

<head>
    <title>
        <spring:message code="application.data.header.title" arguments="${application.person.niceName}"/>
    </title>
    <uv:custom-head/>
    <script>
        window.uv = {};
        window.uv.personId = '<c:out value="${application.person.id}" />';
        window.uv.webPrefix = "<spring:url value='/web' />";
        window.uv.apiPrefix = "<spring:url value='/api' />";
        window.uv.dayLength = "<c:out value='${application.dayLength}' />";
        window.uv.startDate = "<fmt:formatDate pattern='yyyy/MM/dd' value='${parsedStartDate}' type="date" />";
        window.uv.endDate = "<fmt:formatDate pattern='yyyy/MM/dd' value='${parsedEndDate}' type="date" />";
    </script>
    <script defer src="<asset:url value='npm.date-fns.js' />"></script>
    <script defer src="<asset:url value='app_detail~app_form~person_overview.js' />"></script>
    <script defer src="<asset:url value='app_detail.js' />"></script>
</head>

<body>

<spring:url var="URL_PREFIX" value="/web"/>

<uv:menu/>

<div class="print-info--only-portrait">
    <h4><spring:message code="print.info.portrait"/></h4>
</div>

<div class="content print--only-portrait">
    <div class="container">

        <div class="row">

            <div class="col-xs-12 col-sm-12 col-md-6">

                <uv:section-heading>
                    <jsp:attribute name="actions">
                        <jsp:include page="include/app-detail-elements/action-buttons.jsp"/>
                    </jsp:attribute>
                    <jsp:body>
                        <h1>
                            <spring:message code="application.data.title"/>
                        </h1>
                    </jsp:body>
                </uv:section-heading>

                <div class="feedback">
                    <c:choose>
                        <c:when test="${applySuccess}">
                            <div class="alert alert-success">
                                <spring:message code='application.action.apply.success'/>
                            </div>
                        </c:when>
                        <c:when test="${remindAlreadySent}">
                            <div class="alert alert-danger">
                                <spring:message code='application.action.remind.error.alreadySent'/>
                            </div>
                        </c:when>
                        <c:when test="${remindNoWay}">
                            <div class="alert alert-danger">
                                <spring:message code='application.action.remind.error.impatient'/>
                            </div>
                        </c:when>
                        <c:when test="${remindIsSent}">
                            <div class="alert alert-success">
                                <spring:message code='application.action.remind.success'/>
                            </div>
                        </c:when>
                        <c:when test="${!empty errors}">
                            <div class="alert alert-danger">
                                <spring:message code="application.action.reason.error"/>
                            </div>
                        </c:when>
                        <c:when test="${allowSuccess}">
                            <div class="alert alert-success">
                                <spring:message code="application.action.allow.success"/>
                            </div>
                        </c:when>
                        <c:when test="${rejectSuccess}">
                            <div class="alert alert-success">
                                <spring:message code="application.action.reject.success"/>
                            </div>
                        </c:when>
                        <c:when test="${referSuccess}">
                            <div class="alert alert-success">
                                <spring:message code="application.action.refer.success"/>
                            </div>
                        </c:when>
                        <c:when test="${cancelSuccess}">
                            <div class="alert alert-success">
                                <spring:message code="application.action.cancel.success"/>
                            </div>
                        </c:when>
                    </c:choose>
                </div>

                <div class="actions">
                    <jsp:include page="include/app-detail-elements/actions.jsp"/>
                </div>

                <%@include file="include/app-detail-elements/app_info.jsp" %>

            </div>
            <%--End of first column--%>

            <div class="col-xs-12 col-sm-12 col-md-6 hidden-print">

                <uv:section-heading>
                    <h2>
                        <spring:message code="person.account.vacation.title"/>
                    </h2>
                    <uv:year-selector year="${year}" hrefPrefix="${URL_PREFIX}/application/${application.id}?year="/>
                </uv:section-heading>

                <uv:person person="${application.person}" cssClass="tw-h-32 tw-mb-4" />
                <uv:account-entitlement account="${account}" className="tw-mb-4" />
                <uv:account-left account="${account}" vacationDaysLeft="${vacationDaysLeft}"
                                 beforeApril="${beforeApril}"/>

            </div><!-- End of second column -->

        </div><!-- End of row -->

        <div class="row">

            <div class="col-xs-12 col-sm-12 col-md-6">
                <%@include file="include/app-detail-elements/app_progress.jsp" %>
            </div>

            <div class="col-xs-12 col-sm-12 col-md-6 hidden-print">
                <uv:section-heading>
                    <h2>
                        <spring:message code="application.department.title"/>
                    </h2>
                </uv:section-heading>
                <table class="list-table striped-table bordered-table tw-text-sm">
                    <tbody>
                    <c:choose>
                        <c:when test="${empty departmentApplications}">
                            <spring:message code="application.department.none"/>
                        </c:when>
                        <c:otherwise>
                            <c:forEach items="${departmentApplications}" var="application">
                                <tr>
                                    <td>
                                        <img
                                            src="<c:out value='${application.person.gravatarURL}?d=mm&s=40'/>"
                                            alt="<spring:message code="gravatar.alt" arguments="${application.person.niceName}"/>"
                                            class="gravatar tw-rounded-full ${cssClass}"
                                            width="40px"
                                            height="40px"
                                            onerror="this.src !== '/images/gravatar.jpg' && (this.src = '/images/gravatar.jpg')"
                                        />
                                    </td>
                                    <td>
                                        <c:out value="${application.person.niceName}"/>
                                    </td>
                                    <td>
                                        <span class="tw-flex tw-items-center">
                                        <c:choose>
                                            <c:when test="${application.startDate == application.endDate}">
                                                <c:set var="APPLICATION_DATE">
                                                    <uv:date date="${application.startDate}"/>
                                                </c:set>
                                                <c:set var="APPLICATION_DAY_LENGTH">
                                                    <spring:message code="${application.dayLength}"/>
                                                </c:set>
                                                <spring:message code="absence.period.singleDay"
                                                                arguments="${APPLICATION_DATE};${APPLICATION_DAY_LENGTH}"
                                                                argumentSeparator=";"/>
                                            </c:when>
                                            <c:otherwise>
                                                <c:set var="APPLICATION_START_DATE">
                                                    <uv:date date="${application.startDate}"/>
                                                </c:set>
                                                <c:set var="APPLICATION_END_DATE">
                                                    <uv:date date="${application.endDate}"/>
                                                </c:set>
                                                <spring:message code="absence.period.multipleDays"
                                                                arguments="${APPLICATION_START_DATE};${APPLICATION_END_DATE}"
                                                                argumentSeparator=";"/>
                                            </c:otherwise>
                                        </c:choose>
                                        <c:if test="${application.status == 'ALLOWED'}">
                                            <span class="tw-text-green-500">
                                                <uv:icon-check className="tw-w-5 tw-h-5" solid="true" />
                                            </span>
                                        </c:if>
                                        </span>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                    </tbody>
                </table>
            </div>

        </div><!-- End of row -->

    </div> <!-- end of grid container -->

</div> <!-- end of content -->

</body>

</html>
