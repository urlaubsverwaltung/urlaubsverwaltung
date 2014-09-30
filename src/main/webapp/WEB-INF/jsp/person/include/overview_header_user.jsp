<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>


<div class="header">

    <legend>
        <p>
            <spring:message code="table.overview"/> <c:out value="${displayYear}"/>
        </p>
        <span class="hidden-xs">
            <uv:year-selector year="${year}" />
        </span>
        <span class="hidden-sm hidden-xs">
            <uv:print />
        </span>
    </legend>
    
</div>
