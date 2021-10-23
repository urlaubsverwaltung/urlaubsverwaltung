<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<%@attribute name="id" type="java.lang.String" required="true" %>
<%@attribute name="name" type="java.lang.String" required="true" %>
<%@attribute name="cssClass" type="java.lang.String" required="false" %>
<%@attribute name="onchange" type="java.lang.String" required="false" %>
<%@attribute name="testId" type="java.lang.String" required="false" %>

<c:choose>
    <c:when test="${not empty onchange}">
        <c:set var="onchangeAttribute" value="onchange='${onchange}'"/>
    </c:when>
    <c:otherwise>
        <c:set var="onchangeAttribute" value=""/>
    </c:otherwise>
</c:choose>

<c:choose>
    <c:when test="${not empty testId}">
        <c:set var="testid" value="data-test-id='${testId}'"/>
    </c:when>
    <c:otherwise>
        <c:set var="testid" value=""/>
    </c:otherwise>
</c:choose>

<div class="tw-inline-block tw-relative tw-w-full">
    <select id="${id}" name="${name}" class="form-control tw-appearance-none tw-pr-8 ${cssClass}" ${onchangeAttribute} ${testid}>
        <jsp:doBody />
    </select>
    <div class="tw-pointer-events-none tw-absolute tw-inset-y-0 tw-right-0 tw-flex tw-items-center tw-px-2 tw-text-zinc-700">
        <svg class="tw-fill-current tw-h-4 tw-w-4" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20">
            <path d="M9.293 12.95l.707.707L15.657 8l-1.414-1.414L10 10.828 5.757 6.586 4.343 8z"></path>
        </svg>
    </div>
</div>
