<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="asset" uri="/WEB-INF/asset.tld" %>

<!DOCTYPE html>
<html lang="${language}">
<head>
    <title>
        <spring:message code="overtime.header.title" arguments="${person.niceName}"/>
    </title>
    <uv:custom-head/>
    <script defer src="<asset:url value='overtime_overview.js' />"></script>
</head>
<body>

<spring:url var="URL_PREFIX" value="/web"/>

<sec:authorize access="hasAuthority('OFFICE')">
    <c:set var="IS_OFFICE" value="${true}"/>
</sec:authorize>

<uv:menu/>

<div class="content">
    <div class="container">
        <div class="row tw-mb-4 lg:tw-mb-6">
            <div class="col-xs-12">
                <legend class="tw-flex">
                    <div class="tw-flex-1">
                        <spring:message code="overtime.title"/>
                        <uv:year-selector year="${year}" hrefPrefix="${URL_PREFIX}/overtime?person=${person.id}&year="/>
                    </div>
                    <c:if test="${IS_OFFICE || signedInUser.id == person.id}">
                    <div class="print:tw-hidden">
                        <a href="${URL_PREFIX}/overtime/new?person=${person.id}" class="icon-link tw-px-1" data-title="<spring:message code="action.overtime.new"/>">
                            <uv:icon-plus-circle className="tw-w-5 tw-h-5" />
                        </a>
                    </div>
                    </c:if>
                </legend>
            </div>

            <div class="tw-space-y-4 lg:tw-space-y-0">
                <div class="col-xs-12 col-sm-12 col-md-4">
                    <uv:person person="${person}" cssClass="tw-h-32" />
                </div>
                <div class="col-xs-12 col-md-4">
                    <uv:overtime-total hours="${overtimeTotal}" cssClass="tw-h-32 tw-items-center" />
                </div>
                <div class="col-xs-12 col-md-4">
                    <uv:overtime-left hours="${overtimeLeft}" cssClass="tw-h-32 tw-items-center" />
                </div>
            </div>
        </div>

        <div class="row">
            <div class="col-xs-12">
                <legend>
                    <spring:message code="overtime.list"/>
                </legend>
                <c:choose>
                    <c:when test="${empty records}">
                        <p><spring:message code="overtime.none"/></p>
                    </c:when>
                    <c:otherwise>
                        <table class="list-table bordered-table selectable-table">
                            <tbody>
                            <c:forEach items="${records}" var="record">
                                <tr onclick="navigate('${URL_PREFIX}/overtime/${record.id}');">
                                    <td class="is-centered state">
                                        <span class="hidden-print">
                                            <uv:icon-briefcase className="tw-w-4 tw-h-4" />
                                        </span>
                                    </td>
                                    <td>
                                        <h4 class="visible-print">
                                            <spring:message code="overtime.title"/>
                                        </h4>
                                        <a class="hidden-print" href="${URL_PREFIX}/overtime/${record.id}">
                                            <h4><spring:message code="overtime.title"/></h4>
                                        </a>
                                        <p><uv:date date="${record.startDate}"/> - <uv:date
                                            date="${record.endDate}"/></p>
                                    </td>
                                    <td class="is-centered hidden-xs">
                                        <uv:number number="${record.hours}"/>
                                        <spring:message code="duration.hours"/>
                                    </td>
                                    <td class="hidden-print is-centered hidden-xs">
                                        <div class="tw-flex tw-items-center">
                                            <uv:icon-clock className="tw-w-4 tw-h-4" />
                                            &nbsp;<spring:message code="overtime.progress.lastEdited"/>
                                            <uv:date date="${record.lastModificationDate}"/>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </c:otherwise>
                </c:choose>
            </div>

        </div>
        <%-- End of row --%>
    </div>
    <%-- End of container --%>
</div>
<%-- End of content --%>

</body>
</html>
