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
    <path d="M11 6a3 3 0 11-6 0 3 3 0 016 0zM14 17a6 6 0 00-12 0h12zM13 8a1 1 0 100 2h4a1 1 0 100-2h-4z"></path>
</svg>
</c:if>

<c:if test="${solid == false}">
<svg fill="none" viewBox="0 0 24 24" stroke="currentColor" width="16px" height="16px" class="${className} ${strokeWidthClass}" role="img" aria-hidden="true" focusable="false">
    <path stroke-linecap="round" stroke-linejoin="round" d="M13 7a4 4 0 11-8 0 4 4 0 018 0zM9 14a6 6 0 00-6 6v1h12v-1a6 6 0 00-6-6zM21 12h-6"></path>
</svg>
</c:if>
