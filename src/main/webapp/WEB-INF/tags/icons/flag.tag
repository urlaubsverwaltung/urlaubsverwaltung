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
    <path fill-rule="evenodd" d="M3 6a3 3 0 013-3h10a1 1 0 01.8 1.6L14.25 8l2.55 3.4A1 1 0 0116 13H6a1 1 0 00-1 1v3a1 1 0 11-2 0V6z" clip-rule="evenodd"></path>
</svg>
</c:if>

<c:if test="${solid == false}">
<svg fill="none" viewBox="0 0 24 24" stroke="currentColor" width="16px" height="16px" class="${className} ${strokeWidthClass}" role="img" aria-hidden="true" focusable="false">
    <path stroke-linecap="round" stroke-linejoin="round" d="M3 21v-4m0 0V5a2 2 0 012-2h6.5l1 1H21l-3 6 3 6h-8.5l-1-1H5a2 2 0 00-2 2zm9-13.5V9"></path>
</svg>
</c:if>

