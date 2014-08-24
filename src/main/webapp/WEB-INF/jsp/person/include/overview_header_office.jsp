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

        <div class="btn-group selector">

            <button class="btn dropdown-toggle" data-toggle="dropdown">
                <i class="icon-user"></i>
                <spring:message code="ov.header.person" />&nbsp;<span class="caret"></span>
            </button>

            <ul class="dropdown-menu">

                <c:forEach items="${persons}" var="person">
                    <li>
                        <a href="${formUrlPrefix}/staff/<c:out value='${person.id}' />/overview">
                            <c:out value="${person.niceName}"/>
                        </a>
                    </li>
                </c:forEach>

            </ul>

        </div>
        
        <a href="${formUrlPrefix}/staff/${person.id}/edit" class="btn btn-right"><i class="icon-pencil"></i> Edit</a>

        <uv:print />

    </legend>
    
</div>