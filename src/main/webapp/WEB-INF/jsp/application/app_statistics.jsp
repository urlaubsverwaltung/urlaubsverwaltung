<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>
<%@taglib prefix="asset" uri="/WEB-INF/asset.tld" %>

<!DOCTYPE html>
<html lang="${language}">

<head>
    <title>
        <spring:message code="applications.statistics.header.title"/>
    </title>
    <uv:custom-head/>
    <script>
        window.uv = {};
        window.uv.personId = '<c:out value="${person.id}" />';
        window.uv.webPrefix = "<spring:url value='/web' />";
        window.uv.apiPrefix = "<spring:url value='/api' />";
    </script>
    <uv:datepicker-localisation />
    <link rel="stylesheet" type="text/css" href="<asset:url value='account_form~app_form~app_statistics~overtime_form~sick_note_form~sick_notes~workingtime_form.css' />" />
    <link rel="stylesheet" type="text/css" href="<asset:url value='account_form~app_form~app_statistics~overtime_form~person_overview~sick_note_form~sick_notes~workingtime_form.css' />" />
    <script defer src="<asset:url value='npm.duetds.js' />"></script>
    <script defer src="<asset:url value='npm.date-fns.js' />"></script>
    <script defer src="<asset:url value='npm.tablesorter.js' />"></script>
    <script defer src="<asset:url value='account_form~app_form~app_statistics~overtime_form~sick_note_form~sick_notes~workingtime_form.js' />"></script>
    <script defer src="<asset:url value='account_form~app_form~app_statistics~overtime_form~person_overview~sick_note_form~sick_notes~workingtime_form.js' />"></script>
    <script defer src="<asset:url value='account_form~app_detail~app_form~app_statistics~overtime_form~person_overview~sick_note_form~sick_no~95889e93.js' />"></script>
    <script defer src="<asset:url value='app_statistics.js' />"></script>
</head>

<body>

<spring:url var="URL_PREFIX" value="/web"/>
<c:set var="linkPrefix" value="${URL_PREFIX}/application"/>

<c:set var="fromDate">
    <uv:date date="${from}" pattern="yyyy-MM-dd" />
</c:set>
<c:set var="toDate">
    <uv:date date="${to}" pattern="yyyy-MM-dd" />
</c:set>

<uv:menu/>

<div class="content">

    <div class="container">

        <uv:section-heading>
            <jsp:attribute name="actions">
                <a href="/web/application/statistics/download?from=${fromDate}&to=${toDate}" class="icon-link tw-px-1" data-title="<spring:message code='action.download' />">
                    <icon:download className="tw-w-5 tw-h-5" />
                </a>
                <uv:print/>
            </jsp:attribute>
            <jsp:attribute name="below">
                <p class="tw-text-sm">
                    <c:choose>
                        <c:when test="${not empty errors}">
                            <a href="#filterModal" data-toggle="modal">
                                <spring:message code="applications.statistics.error.period"/>
                            </a>
                        </c:when>
                        <c:otherwise>
                            <a href="#filterModal" data-toggle="modal">
                                <spring:message code="filter.period"/>:&nbsp;<uv:date-range from="${from}" to="${to}" />
                            </a>
                        </c:otherwise>
                    </c:choose>
                </p>
                <uv:filter-modal id="filterModal" actionUrl="${linkPrefix}/statistics"/>
            </jsp:attribute>
            <jsp:body>
                <h1 class="tw-flex-1 tw-text-2xl tw-font-normal tw-m-0">
                    <spring:message code="applications.statistics"/>
                </h1>
            </jsp:body>
        </uv:section-heading>

        <div class="row">

            <div class="col-xs-12">
                <c:choose>
                    <c:when test="${not empty errors}">
                        <div class="alert alert-danger">
                            <spring:message code='applications.statistics.error'/>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <table class="list-table sortable tablesorter tw-text-sm">
                            <thead class="tw-hidden lg:tw-table-header-group">
                            <tr>
                                <th scope="col" class=""><%-- placeholder to ensure correct number of th --%></th>
                                <th scope="col" class="tw-hidden lg:tw-table-cell print:tw-table-cell sortable-field"><spring:message code="person.data.firstName"/></th>
                                <th scope="col" class="tw-hidden lg:tw-table-cell print:tw-table-cell sortable-field"><spring:message code="person.data.lastName"/></th>
                                <th scope="col" class="lg:tw-hidden print:tw-hidden"><%-- placeholder to ensure correct number of th --%></th>
                                <th scope="col" class="md:tw-hidden print:tw-hidden"><%-- placeholder to ensure correct number of th --%></th>
                                <th scope="col" class="tw-hidden md:tw-table-cell print:tw-table-cell"><%-- placeholder to ensure correct number of th --%></th>
                                <th scope="col" class="tw-hidden md:tw-table-cell print:tw-table-cell sortable-field"><spring:message code="applications.statistics.allowed"/></th>
                                <th scope="col" class="tw-hidden md:tw-table-cell print:tw-table-cell sortable-field"><spring:message code="applications.statistics.waiting"/></th>
                                <th scope="col" class="tw-hidden md:tw-table-cell print:tw-table-cell sortable-field"><spring:message code="applications.statistics.left"/> (<c:out
                                    value="${from.year}"/>)
                                </th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach items="${statistics}" var="statistic">
                                <tr>
                                    <td class="is-centered">
                                        <uv:avatar
                                            url="${statistic.person.gravatarURL}?d=mm&s=60"
                                            username="${statistic.person.firstName}"
                                            width="60px"
                                            height="60px"
                                            border="true"
                                        />
                                    </td>
                                    <td class="tw-hidden lg:tw-table-cell print:tw-table-cell"><c:out value="${statistic.person.firstName}"/></td>
                                    <td class="tw-hidden lg:tw-table-cell print:tw-table-cell"><c:out value="${statistic.person.lastName}"/></td>
                                    <td class="lg:tw-hidden print:tw-hidden">
                                        <c:out value="${statistic.person.niceName}"/>
                                    </td>
                                    <td class="md:tw-hidden print:tw-hidden">
                                        <div class="tw-flex tw-items-center">
                                            <span class="tw-w-6">
                                                <icon:check className="tw-w-5 tw-h-5"/>
                                            </span>
                                            <span>
                                                <uv:number number="${statistic.totalAllowedVacationDays}"/>
                                            </span>
                                        </div>
                                        <div class="tw-flex tw-items-center">
                                            <span class="tw-w-6">
                                                <icon:question-mark-circle className="tw-w-4 tw-h-4"/>
                                            </span>
                                            <span>
                                                <uv:number number="${statistic.totalWaitingVacationDays}"/>
                                            </span>
                                        </div>
                                    </td>
                                    <td class="tw-hidden md:tw-table-cell print:tw-table-cell">
                                        <spring:message code="applications.statistics.total"/>:
                                        <c:forEach items="${vacationTypes}" var="type">
                                            <%-- TODO maybe show each type for every user if one has a vacation --%>
                                            <c:if test="${statistic.hasVacationType(type)}">
                                                <br/>
                                                <small>
                                                    <spring:message code="${type.messageKey}"/>:
                                                </small>
                                            </c:if>
                                        </c:forEach>
                                    </td>
                                    <td class="tw-hidden md:tw-table-cell print:tw-table-cell number">
                                        <strong class="sortable">
                                            <uv:number number="${statistic.totalAllowedVacationDays}"/>
                                        </strong>
                                        <spring:message code="duration.days"/>
                                        <c:forEach items="${vacationTypes}" var="type">
                                            <c:if test="${statistic.hasVacationType(type)}">
                                                <br/>
                                                <small>
                                                    <uv:number number="${statistic.getAllowedVacationDays(type)}"/>
                                                </small>
                                            </c:if>
                                        </c:forEach>
                                    </td>
                                    <td class="tw-hidden md:tw-table-cell print:tw-table-cell number">
                                        <strong class="sortable">
                                            <uv:number number="${statistic.totalWaitingVacationDays}"/>
                                        </strong>
                                        <spring:message code="duration.days"/>
                                        <c:forEach items="${vacationTypes}" var="type">
                                            <c:if test="${statistic.hasVacationType(type)}">
                                                <br/>
                                                <small>
                                                    <uv:number number="${statistic.getWaitingVacationDays(type)}"/>
                                                </small>
                                            </c:if>
                                        </c:forEach>
                                    </td>
                                    <td class="tw-hidden md:tw-table-cell print:tw-table-cell">
                                        <strong class="sortable">
                                            <uv:number number="${statistic.leftVacationDays}"/>
                                        </strong>
                                        <spring:message code="duration.vacationDays"/>
                                        <br/>
                                        <strong><uv:duration duration="${statistic.leftOvertime}"/></strong>
                                        <spring:message code="duration.overtime"/>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </c:otherwise>
                </c:choose>

            </div>

        </div>
        <%-- end of row --%>

    </div>
    <%-- end of container --%>

</div>
<%-- end of content --%>

</body>

</html>


