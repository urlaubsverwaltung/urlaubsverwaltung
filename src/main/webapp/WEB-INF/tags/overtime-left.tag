<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<%@attribute name="hours" type="java.math.BigDecimal" required="true" %>
<%@attribute name="cssClass" type="java.lang.String" required="false" %>

<div class="box tw-flex ${cssClass}">
    <span class="tw-mr-6 tw-bg-green-500 tw-text-white tw-rounded-full tw-p-1 tw-h-16 tw-w-16 tw-flex tw-items-center tw-justify-center hidden-print">
        <uv:icon-sort-descending className="tw-w-8 tw-h-8" />
    </span>
    <span class="tw-flex-1 box-text">
        <spring:message code="overtime.person.left" arguments="${hours}"/>
    </span>
</div>
