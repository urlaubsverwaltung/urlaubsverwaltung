<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<%@attribute name="id" type="java.lang.String" required="true" %>
<%@attribute name="name" type="java.lang.String" required="true" %>
<%@attribute name="cssClass" type="java.lang.String" required="false" %>
<%@attribute name="onchange" type="java.lang.String" required="false" %>

<c:choose>
    <c:when test="${not empty onchange}">
        <c:set var="onchangeAttribute" value="onchange='${onchange}'"/>
    </c:when>
    <c:otherwise>
        <c:set var="onchangeAttribute" value=""/>
    </c:otherwise>
</c:choose>

<div class="tw-inline-block tw-relative tw-w-full">
    <select multiple id="${id}" name="${name}" class="form-control ${cssClass}" ${onchangeAttribute}>
        <jsp:doBody />
    </select>
</div>
