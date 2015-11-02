<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<%@attribute name="person" type="org.synyx.urlaubsverwaltung.core.person.Person" required="true" %>
<%@attribute name="cssClass" type="java.lang.String" required="false" %>

<spring:url var="URL_PREFIX" value="/web"/>

<div class="box ${cssClass}">
    <div class="box-image gravatar img-circle" data-gravatar="<c:out value='${person.gravatarURL}?d=mm&s=60'/>"></div>
    <span class="box-text">
        <i class="fa fa-at"></i> <c:out value="${person.loginName}"/>
        <h4>
            <a class="hidden-print" href="${URL_PREFIX}/staff/${person.id}/overview">
                <c:out value="${person.niceName}"/>
            </a>
            <span class="visible-print">
                <c:out value="${person.niceName}"/>
            </span>
        </h4>
        <i class="fa fa-envelope-o"></i> <c:out value="${person.email}"/>
    </span>
</div>
