<!---------------------------------------------
kudos
* https://github.com/tailwindlabs/heroicons
---------------------------------------------->

<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

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
    <path fill-rule="evenodd" d="M3 3a1 1 0 011 1v12a1 1 0 11-2 0V4a1 1 0 011-1zm7.707 3.293a1 1 0 010 1.414L9.414 9H17a1 1 0 110 2H9.414l1.293 1.293a1 1 0 01-1.414 1.414l-3-3a1 1 0 010-1.414l3-3a1 1 0 011.414 0z" clip-rule="evenodd"></path>
</svg>
</c:if>

<c:if test="${solid == false}">
<svg fill="none" viewBox="0 0 24 24" stroke="currentColor" width="16px" height="16px" class="${className} ${strokeWidthClass}" role="img" aria-hidden="true" focusable="false">
    <path stroke-linecap="round" stroke-linejoin="round" d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1"></path>
</svg>
</c:if>
