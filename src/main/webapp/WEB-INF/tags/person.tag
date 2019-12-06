<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<%@attribute name="person" type="org.synyx.urlaubsverwaltung.person.Person" required="true" %>
<%@attribute name="cssClass" type="java.lang.String" required="false" %>
<%@attribute name="nameIsNoLink" type="java.lang.Boolean" required="false" %>

<spring:url var="URL_PREFIX" value="/web"/>

<div class="box ${cssClass}">
    <div class="box-image gravatar img-circle" data-gravatar="<c:out value='${person.gravatarURL}?d=mm&s=60'/>"></div>
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
        <i class="fa fa-envelope-o" aria-hidden="true"></i>
        <a href="mailto:<c:out value='${person.email}'/>">
            <span class="hidden-print"><c:out value="${person.email}"/></span>
        </a>
    </span>
</div>
