<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<%@attribute name="url" type="java.lang.String" required="true" %>
<%@attribute name="username" type="java.lang.String" required="true" %>
<%@attribute name="width" type="java.lang.String" required="false" %>
<%@attribute name="height" type="java.lang.String" required="false" %>
<%@attribute name="border" type="java.lang.Boolean" required="false" %>

<c:set var="avatar">
    <img
        src="${url}"
        alt="<spring:message code="gravatar.alt" arguments="${username}"/>"
        class="gravatar gravatar--medium tw-rounded-full print:tw-hidden"
        width="${width == null ? '32px' : width}"
        height="${height == null ? '32px' : height}"
        onerror="this.src !== '/images/gravatar.jpg' && (this.src = '/images/gravatar.jpg')"
    />
</c:set>

<c:choose>
    <c:when test="${border == true}">
        <span class="tw-bg-gradient-to-br tw-from-blue-50 tw-to-blue-200 tw-rounded-full tw-p-1 tw-inline-block">
            ${avatar}
        </span>
    </c:when>
    <c:otherwise>
        ${avatar}
    </c:otherwise>
</c:choose>

