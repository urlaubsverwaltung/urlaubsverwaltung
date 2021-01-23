<!---------------------------------------------
kudos
* https://github.com/tailwindlabs/heroicons
---------------------------------------------->

<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%@attribute name="className" type="java.lang.String" required="false" %>
<%@attribute name="strokeWidth" type="java.lang.Integer" required="false" %>
<%@attribute name="solid" type="java.lang.Boolean" required="false" %>

<c:set var="strokeWidth" value="${(strokeWidth == null) ? 2 : strokeWidth}" />
<c:set var="solid" value="${(solid == null) ? false : solid}" />

<c:set var="strokeWidthClass" value="${strokeWidth == 3 ? 'tw-stroke-3' : ''}" />
<c:set var="strokeWidthClass" value="${strokeWidth == 2 ? 'tw-stroke-2' : strokeWidthClass}" />
<c:set var="strokeWidthClass" value="${strokeWidth == 1 ? 'tw-stroke-1' : strokeWidthClass}" />
<c:set var="strokeWidthClass" value="${strokeWidth == 0 ? 'tw-stroke-0' : strokeWidthClass}" />

<c:if test="${solid == true}">
<svg viewBox="0 0 20 20" fill="currentColor" width="16px" height="16px" class="${className}" role="img" aria-hidden="true" focusable="false">
    <path d="M10 2a6 6 0 00-6 6v3.586l-.707.707A1 1 0 004 14h12a1 1 0 00.707-1.707L16 11.586V8a6 6 0 00-6-6zM10 18a3 3 0 01-3-3h6a3 3 0 01-3 3z"></path>
</svg>
</c:if>

<c:if test="${solid == false}">
<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" width="16px" height="16px" class="${className}" role="img" aria-hidden="true" focusable="false">
    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"></path>
</svg>
</c:if>
