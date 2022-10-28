<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<%@attribute name="status" type="org.springframework.validation.Errors" required="false" %>
<%@attribute name="reductionFieldName" type="java.lang.String" required="true" %>

<c:set var="durationError">
    <form:errors path="${reductionFieldName}"/>
</c:set>

<div class="tw-flex tw-items-start">
    <span class="tw-flex-1">
        <spring:bind path="hours">
            <uv:input-group hasError="${not empty durationError or status.error}">
                <jsp:attribute name="addon">
                    <spring:message code="hours.abbr"/>
                </jsp:attribute>
                <jsp:body>
                    <spring:message var="hoursPlaceholder"
                                    code='input.hours.input.placeholder'/>
                    <form:input
                        path="hours"
                        cssClass="form-control"
                        placeholder="${hoursPlaceholder}"
                        type="text"
                        inputmode="numeric"
                        autocomplete="off"
                        data-test-id="overtime-hours"
                    />
                </jsp:body>
            </uv:input-group>
        </spring:bind>
        <c:if test="${empty durationError}">
            <uv:error-text>
                <form:errors path="hours"/>
            </uv:error-text>
        </c:if>
    </span>&nbsp;
    <span class="tw-flex-1">
        <spring:bind path="minutes">
            <uv:input-group hasError="${not empty durationError or status.error}">
                <jsp:attribute name="addon">
                    <spring:message code="minutes.abbr"/>
                </jsp:attribute>
                <jsp:body>
                    <spring:message var="minutesPlaceholder"
                                    code='input.minutes.input.placeholder'/>
                    <form:input
                        path="minutes"
                        cssClass="form-control"
                        placeholder="${minutesPlaceholder}"
                        type="text"
                        inputmode="numeric"
                        autocomplete="off"
                        data-test-id="overtime-minutes"
                    />
                </jsp:body>
            </uv:input-group>
        </spring:bind>
        <c:if test="${empty durationError}">
            <uv:error-text>
                <form:errors path="minutes"/>
            </uv:error-text>
        </c:if>
    </span>
</div>
<c:if test="${not empty durationError}">
    <p>
        <uv:error-text>
            ${durationError}
        </uv:error-text>
    </p>
</c:if>
