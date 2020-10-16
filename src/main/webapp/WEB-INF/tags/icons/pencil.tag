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
    <path d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z"></path>
</svg>
</c:if>

<c:if test="${solid == false}">
<svg fill="none" viewBox="0 0 24 24" stroke="currentColor" width="16px" height="16px" class="${className} ${strokeWidthClass}" role="img" aria-hidden="true" focusable="false">
    <path stroke-linecap="round" stroke-linejoin="round" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z"></path>
</svg>
</c:if>
