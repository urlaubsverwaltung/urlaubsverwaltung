<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>

<uv:section-heading>
    <h2>
        <spring:message code="settings.workingTime.title"/>
    </h2>
</uv:section-heading>
<c:set var="workingTimeError">
    <form:errors path="workingTimeSettings.workingDays" />
</c:set>
<c:if test="${not empty workingTimeError}">
    <div class="row tw-mb-8">
        <div class="col-xs-12">
            <div class="alert alert-danger tw-text-red-800">${workingTimeError}</div>
        </div>
    </div>
</c:if>
<div class="row">
    <div class="col-md-4 col-md-push-8">
        <span class="help-block tw-text-sm">
            <icon:information-circle className="tw-w-4 tw-h-4" solid="true" />
            <spring:message code="settings.workingTime.description"/>
        </span>
    </div>
    <div class="col-md-8 col-md-pull-4">
        <div class="form-group is-required">
            <label class="control-label col-md-4 tw-mb-4">
                <spring:message code="settings.workingTime.weekdays"/>:
            </label>
            <div class="col-md-8">
                <c:forEach items="${weekDays}" var="weekDay">
                    <div class="checkbox">
                        <label for="${weekDay}">
                            <form:checkbox id="${weekDay}" path="workingTimeSettings.workingDays" value="${weekDay.value}"/>
                            <spring:message code='${weekDay}'/>
                        </label>
                    </div>
                </c:forEach>
            </div>
        </div>
    </div>
</div>
