<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>

<%@attribute name="hours" type="java.time.Duration" required="true" %>
<%@attribute name="cssClass" type="java.lang.String" required="false" %>

<uv:box__ className="${cssClass}">
    <jsp:attribute name="icon">
        <uv:box-icon className="tw-bg-green-500 tw-text-white">
            <icon:briefcase className="tw-w-8 tw-h-8" />
        </uv:box-icon>
    </jsp:attribute>
    <jsp:body>
        <span class="tw-text-sm tw-text-black tw-text-opacity-75">
            <spring:message code="overtime.person.total.1" />
        </span>
        <span class="tw-my-1 tw-text-lg tw-font-medium">
            <uv:duration duration="${hours}"/>
        </span>
        <span class="tw-text-sm tw-text-black tw-text-opacity-75">
            <spring:message code="overtime.person.total.2" />
        </span>
    </jsp:body>
</uv:box__>
