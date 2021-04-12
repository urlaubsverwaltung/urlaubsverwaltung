<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>
<%@taglib prefix="asset" uri = "/WEB-INF/asset.tld"%>

<!DOCTYPE html>
<html lang="${language}">

<head>
    <title>
        TODO
    </title>
    <uv:custom-head/>
    <script>
        window.uv = {};
        window.uv.personId = '<c:out value="${person.id}" />';
        window.uv.webPrefix = "<spring:url value='/web' />";
        window.uv.apiPrefix = "<spring:url value='/api' />";
    </script>
    <uv:datepicker-localisation />
    <script defer src="<asset:url value='npm.duetds.js' />"></script>
    <script defer src="<asset:url value='npm.date-fns.js' />"></script>
</head>

<body>

<spring:url var="URL_PREFIX" value="/web"/>

<uv:menu/>

<div class="content">
    <div class="container">

        <uv:section-heading>
            <jsp:attribute name="actions">
                <uv:print/>
            </jsp:attribute>
            <jsp:attribute name="below">
                <p class="tw-text-sm">
                    <a href="#filterModal" data-toggle="modal">
                        <spring:message code="filter.period"/>:&nbsp;<uv:date date="${from}"/> - <uv:date date="${to}"/>
                    </a>
                </p>
                <p class="pull-right visible-print">
                    <spring:message code="filter.validity"/> <uv:date date="${today}"/>
                </p>
                <uv:filter-modal id="filterModal" actionUrl="${URL_PREFIX}/overtime/statistics/filter"/>
            </jsp:attribute>
            <jsp:body>
                <h1>
                    <spring:message code="overtime.statistics.title"/>
                </h1>
            </jsp:body>
        </uv:section-heading>

        TODO Some content here

    </div>
</div>

</body>

</html>
