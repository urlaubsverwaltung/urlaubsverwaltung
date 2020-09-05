<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<%@attribute name="person" type="org.synyx.urlaubsverwaltung.person.Person" required="true" %>
<%@attribute name="cssClass" type="java.lang.String" required="false" %>
<%@attribute name="nameIsNoLink" type="java.lang.Boolean" required="false" %>

<spring:url var="URL_PREFIX" value="/web"/>

<div class="box ${cssClass}">
    <div class="box-icon gravatar" data-gravatar="<c:out value='${person.gravatarURL}?d=mm&s=60'/>"></div>
    <span class="box-text">
        <h4>
            <c:choose>
                <c:when test="${nameIsNoLink}">
                    <span class="hidden-print"><c:out value="${person.niceName}"/></span>
                </c:when>
                <c:otherwise>
                    <a class="hidden-print" href="${URL_PREFIX}/person/${person.id}/overview">
                        <c:out value="${person.niceName}"/>
                    </a>
                </c:otherwise>
            </c:choose>
            <span class="visible-print">
                <c:out value="${person.niceName}"/>
            </span>
        </h4>
        <a href="mailto:<c:out value='${person.email}'/>" class="tw-flex tw-items-center">
            <uv:icon-mail className="tw-w-4 tw-h-4" />
            &nbsp;<c:out value="${person.email}"/>
        </a>
    </span>
</div>
