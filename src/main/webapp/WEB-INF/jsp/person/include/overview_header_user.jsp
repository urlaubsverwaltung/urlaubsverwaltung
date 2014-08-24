<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>


<div class="header">

    <legend>
        <p>
            <img class="overview--user-pic print--invisible" src="<c:out value='${gravatar}?d=mm&s=40'/>"/>&nbsp;
            <c:out value="${person.firstName}"/>&nbsp;<c:out value="${person.lastName}"/>&nbsp;&ndash;&nbsp;<spring:message
            code="table.overview"/><c:out value="${displayYear}"/>
        </p>
        <uv:year-selector year="${year}" />
    </legend>
    
</div>
