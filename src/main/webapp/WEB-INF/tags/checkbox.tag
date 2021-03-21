<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<%@attribute name="label" type="java.lang.String" required="true" %>
<%@attribute name="name" type="java.lang.String" required="true" %>
<%@attribute name="value" type="java.lang.Object" required="true" %>
<%@attribute name="checked" type="java.lang.Boolean" required="false" %>

<c:set var="random"><%= java.util.UUID.randomUUID().toString().substring(0, 8) %></c:set>

<label for="checkbox-${random}" class="tw-m-0">
    <span class="tw-flex tw-items-center">
        <input
            class="tw-m-0"
            type="checkbox"
            id="checkbox-${random}"
            name="${name}"
            value="${value}"
            ${checked ? "checked" : ""}
        >
        <span class="tw-ml-1.5 tw-font-normal">
            ${label}
        </span>
    </span>
</label>
