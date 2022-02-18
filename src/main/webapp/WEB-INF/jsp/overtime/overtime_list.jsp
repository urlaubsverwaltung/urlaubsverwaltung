<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>
<%@taglib prefix="asset" uri="/WEB-INF/asset.tld" %>

<!DOCTYPE html>
<html lang="${language}" class="tw-<c:out value='${theme}' />">
<head>
    <title>
        <spring:message code="overtime.header.title" arguments="${person.niceName}"/>
    </title>
    <uv:custom-head/>
    <script defer src="<asset:url value='overtime_overview.js' />"></script>
</head>
<body>

<spring:url var="URL_PREFIX" value="/web"/>

<uv:menu/>

<div class="content">
    <div class="container">
        <div class="row tw-mb-2">
            <div class="col-xs-12">
                <uv:section-heading>
                    <jsp:attribute name="actions">
                        <c:if test="${userIsAllowedToWriteOvertime}">
                            <a href="${URL_PREFIX}/overtime/new?person=${person.id}" class="icon-link tw-px-1"
                               data-title="<spring:message code="action.overtime.new"/>">
                                <icon:plus-circle className="tw-w-5 tw-h-5"/>
                            </a>
                        </c:if>
                    </jsp:attribute>
                    <jsp:body>
                        <h1>
                            <spring:message code="overtime.title"/>
                        </h1>
                        <uv:year-selector year="${year}" hrefPrefix="${URL_PREFIX}/overtime?person=${person.id}&year="/>
                    </jsp:body>
                </uv:section-heading>
            </div>
        </div>
        <div class="row lg:tw-mb-8">
            <div class="sm:tw-flex">
                <div class="sm:tw-flex-1 lg:tw-flex-none lg:tw-w-1/3">
                    <uv:person person="${person}" cssClass="tw-h-24 lg:tw-h-32 tw-border-none"/>
                </div>
                <div class="sm:tw-flex-1 lg:tw-flex">
                    <div class="tw-flex-1">
                        <uv:overtime-total hours="${overtimeTotal}" cssClass="tw-h-32 tw-items-center tw-border-none"/>
                    </div>
                    <div class="tw-flex-1">
                        <uv:overtime-left hours="${overtimeLeft}" cssClass="tw-h-32 tw-items-center tw-border-none"/>
                    </div>
                </div>
            </div>
        </div>
        <div class="row">
            <div class="col-xs-12">
                <c:choose>
                    <c:when test="${empty records}">
                        <p class="tw-text-center tw-mt-4 lg:tw-mt-8"><spring:message code="overtime.none"/></p>
                    </c:when>
                    <c:otherwise>
                        <table class="list-table selectable-table tw-text-sm">
                            <caption class="tw-sr-only"><spring:message code="overtime.list.title"/></caption>
                            <thead class="tw-sr-only">
                            <tr>
                                <th scope="col"><spring:message code="overtime.list.col.icon"/></th>
                                <th scope="col"><spring:message code="overtime.list.col.date"/></th>
                                <th scope="col"><spring:message code="overtime.list.col.duration"/></th>
                                <th scope="col"><spring:message code="overtime.list.col.last-edited"/></th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach items="${records}" var="record">

                                <c:choose>
                                    <c:when test="${record.status == 'CANCELLED' || record.status == 'REJECTED' || record.status == 'REVOKED'}">
                                        <c:set var="CSS_CLASS" value="inactive"/>
                                    </c:when>
                                    <c:otherwise>
                                        <c:set var="CSS_CLASS" value="active"/>
                                    </c:otherwise>
                                </c:choose>

                                <tr class="${CSS_CLASS}" onclick="navigate('${URL_PREFIX}/overtime/${record.id}');">
                                    <td class="is-centered">
                                        <c:if test="${record.overtimeListRecordType == 'ABSENCE'}">
                                            <c:choose>
                                                <c:when test="${record.status == 'WAITING'}">
                                                    <icon:question-mark-circle className="tw-w-6 tw-h-6" />
                                                </c:when>
                                                <c:when test="${record.status == 'ALLOWED'}">
                                                    <icon:check-circle className="tw-w-6 tw-h-6" />
                                                </c:when>
                                                <c:when test="${record.status == 'TEMPORARY_ALLOWED'}">
                                                    <icon:check-circle className="tw-w-6 tw-h-6" />
                                                </c:when>
                                                <c:when test="${record.status == 'ALLOWED_CANCELLATION_REQUESTED'}">
                                                    <icon:check-circle className="tw-w-6 tw-h-6" />
                                                    <icon:arrow-narrow-right className="tw-w-5 tw-h-5" />
                                                    <icon:trash className="tw-w-6 tw-h-6" />
                                                </c:when>
                                                <c:when test="${record.status == 'REJECTED'}">
                                                    <icon:ban className="tw-w-6 tw-h-6" />
                                                </c:when>
                                                <c:when test="${record.status == 'CANCELLED'  || record.status == 'REVOKED'}">
                                                    <icon:trash className="tw-w-6 tw-h-6" />
                                                </c:when>
                                                <c:otherwise>
                                                    &nbsp;
                                                </c:otherwise>
                                            </c:choose>
                                        </c:if>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${record.overtimeListRecordType == 'OVERTIME'}">
                                                <span class="visible-print">
                                                    <spring:message code="overtime.link.overtime"/>
                                                </span>
                                                <a class="print:tw-hidden tw-text-lg tw-mb-1"
                                                   href="${URL_PREFIX}/overtime/${record.id}">
                                                    <spring:message code="overtime.link.overtime"/>
                                                </a>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="visible-print">
                                                    <spring:message code="overtime.link.absence"/>
                                                </span>
                                                <a class="print:tw-hidden tw-text-lg tw-mb-1"
                                                   href="${URL_PREFIX}/application/${record.id}">
                                                    <spring:message code="overtime.link.absence"/>
                                                </a>
                                            </c:otherwise>
                                        </c:choose>
                                        <p>
                                            <uv:date-range from="${record.startDate}" to="${record.endDate}"/>
                                        </p>
                                    </td>
                                    <td class="is-centered">
                                        <uv:duration duration="${record.duration}"/>
                                    </td>
                                    <td class="print:tw-hidden is-centered hidden-xs">
                                        <div class="tw-flex tw-items-center tw-justify-end">
                                            <icon:clock className="tw-w-4 tw-h-4"/>
                                            &nbsp;<spring:message code="overtime.progress.lastEdited"/>
                                            <uv:date date="${record.lastModificationDate}"/>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                            <tr class="active" onclick="navigate('${URL_PREFIX}/overtime?person=${person.id}&year=${year-1}');">
                                <td></td>
                                <td>
                                    <span class="visible-print">
                                        <spring:message code="overtime.link.last-year"/>
                                    </span>
                                    <a class="print:tw-hidden tw-text-lg tw-mb-1"
                                       href="${URL_PREFIX}/overtime?person=${person.id}&year=${year-1}">
                                        <spring:message code="overtime.link.last-year"/>
                                    </a>
                                    <p>
                                        <spring:message code="overtime.list.last-year-details" arguments="${year-1}"/>
                                    </p>
                                </td>
                                <td class="is-centered">
                                    <uv:duration duration="${overtimeTotalLastYear}"/>
                                </td>
                                <td>
                                </td>
                            </tr>
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
