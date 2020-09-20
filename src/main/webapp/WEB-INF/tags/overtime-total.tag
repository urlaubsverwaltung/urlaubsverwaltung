<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<%@attribute name="hours" type="java.math.BigDecimal" required="true" %>
<%@attribute name="cssClass" type="java.lang.String" required="false" %>

<div class="box tw-flex ${cssClass}">
    <div class="box-icon-container hidden-print">
        <div class="box-icon tw-bg-green-500 tw-text-white">
            <uv:icon-briefcase className="tw-w-8 tw-h-8" />
        </div>
    </div>
    <div class="box-text tw-flex-1">
        <span class="tw-text-sm tw-text-black tw-text-opacity-75">
            <spring:message code="overtime.person.total.1" />
        </span>
        <span class="tw-block tw-mt-2 tw-mb-1 tw-text-lg tw-font-medium">
            <spring:message code="overtime.person.total.2" arguments="${hours}"/>
        </span>
        <span class="tw-text-sm tw-text-black tw-text-opacity-75">
            <spring:message code="overtime.person.total.3" />
        </span>
    </div>
</div>
