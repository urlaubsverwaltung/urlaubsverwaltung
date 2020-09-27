<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="asset" uri = "/WEB-INF/asset.tld"%>


<!DOCTYPE html>
<html lang="${language}">

<spring:url var="URL_PREFIX" value="/web"/>

<head>
    <title>
        <spring:message code="person.overview.header.title"/>
    </title>
    <uv:custom-head/>
    <c:if test="${not empty persons}">
    <script defer src="<asset:url value='npm.tablesorter.js' />"></script>
    <script defer src="<asset:url value='npm.string-natural-compare.js' />"></script>
    <script defer src="<asset:url value='npm.list.js.js' />"></script>
    <script defer src="<asset:url value='person_view.js' />"></script>
    </c:if>
</head>

<body>

<uv:menu/>

<div class="print-info--only-landscape">
    <h4><spring:message code="print.info.landscape"/></h4>
</div>

<h1 class="tw-sr-only"><spring:message code="nav.person.title"/></h1>

<div class="content print--only-landscape">
    <div class="container">
        <uv:section-heading>
            <jsp:attribute name="actions">
                <uv:print/>
            </jsp:attribute>
            <jsp:body>
                <div class="legend-dropdown dropdown">
                    <a id="active-state" href="#" data-toggle="dropdown" aria-haspopup="true" role="button" aria-expanded="false">
                        <c:choose>
                            <c:when test="${param.active}">
                                <c:choose>
                                    <c:when test="${department != null}">
                                        <c:out value="${department.name}"/><span class="caret"></span>
                                    </c:when>
                                    <c:otherwise>
                                        <spring:message code="persons.active"/><span class="caret"></span>
                                    </c:otherwise>
                                </c:choose>
                            </c:when>
                            <c:otherwise>
                                <spring:message code="persons.inactive"/><span class="caret"></span>
                            </c:otherwise>
                        </c:choose>
                    </a>
                    <ul class="dropdown-menu" role="menu" aria-labelledby="active-state">
                        <li>
                            <a href="${URL_PREFIX}/person?active=true&year=${year}" class="tw-flex tw-items-center">
                                    <span class="tw-w-5 tw-flex tw-items-center">
                                        <uv:icon-eye className="tw-w-4 tw-h-4" solid="true" />
                                    </span>
                                &nbsp;<spring:message code="persons.active"/>
                            </a>
                        </li>
                        <c:if test="${departments.size() > 1}">
                            <c:forEach items="${departments}" var="department">
                                <li>
                                    <a href='${URL_PREFIX}/person?active=true&year=${year}&department=${department.id}'>
                                        <span class="tw-w-5 tw-inline-block"></span>
                                        <c:out value="${department.name}"/>
                                    </a>
                                </li>
                            </c:forEach>
                            <li role="separator" class="divider"></li>
                        </c:if>
                        <li>
                            <a href="${URL_PREFIX}/person?active=false&year=${year}" class="tw-flex tw-items-center">
                                    <span class="tw-w-5 tw-flex tw-items-center">
                                        <uv:icon-eye-off className="tw-w-4 tw-h-4" solid="true" />
                                    </span>
                                &nbsp;<spring:message code="persons.inactive"/>
                            </a>
                        </li>
                    </ul>
                </div>
                <uv:year-selector year="${year}" hrefPrefix="${URL_PREFIX}/person?active=${param.active}&department=${department.id}&year="/>
            </jsp:body>
        </uv:section-heading>
        <div class="row">
            <div class="col-xs-12">
                <c:choose>
                    <c:when test="${empty persons}">
                        <p><spring:message code="persons.none"/></p>
                    </c:when>
                    <c:otherwise>
                        <%@include file="include/person_list.jsp" %>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</div>

</body>

</html>
