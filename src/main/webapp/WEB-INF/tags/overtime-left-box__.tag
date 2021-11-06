<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>

<%@attribute name="hours" type="java.time.Duration" required="true" %>
<%@attribute name="cssClass" type="java.lang.String" required="false" %>

<uv:box__ className="${cssClass}">
    <jsp:attribute name="icon">
        <uv:box-icon className="tw-bg-emerald-500 tw-text-white dark:tw-bg-green-600 dark:tw-text-neutral-900">
            <icon:sort-descending className="tw-w-8 tw-h-8"/>
        </uv:box-icon>
    </jsp:attribute>
    <jsp:body>
        <span class="tw-text-sm tw-text-black tw-text-opacity-75 dark:tw-text-neutral-300 dark:tw-text-opacity-100">
            <spring:message code="overtime.person.left.1"/>
        </span>
        <span class="tw-my-1 tw-text-lg tw-font-medium">
            <uv:duration duration="${hours}"/>
        </span>
        <span class="tw-text-sm tw-text-black tw-text-opacity-75 dark:tw-text-neutral-300 dark:tw-text-opacity-100">
            <spring:message code="overtime.person.left.2"/>
        </span>
    </jsp:body>
</uv:box__>
