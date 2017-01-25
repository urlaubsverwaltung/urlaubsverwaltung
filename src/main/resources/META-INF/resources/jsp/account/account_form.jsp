<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<!DOCTYPE html>
<html>
<head>
    <uv:head />
</head>

<body>

<uv:menu />

<spring:url var="URL_PREFIX" value="/web"/>

<c:set var="DATE_PATTERN">
    <spring:message code="pattern.date"/>
</c:set>

<c:set var="COMMENT_PLACEHOLDER">
    <spring:message code="person.form.annualVacation.comment.placeholder"/>
</c:set>

<div class="content">
<div class="container">

<form:form method="POST" action="${URL_PREFIX}/staff/${person.id}/account" modelAttribute="account" class="form-horizontal">

<div class="row">

<div class="form-section">
    <div class="col-xs-12">
        <legend>
            <spring:message code="person.form.annualVacation.title" arguments="${person.niceName}"/>
            <uv:year-selector year="${year}" hrefPrefix="${URL_PREFIX}/staff/${person.id}/account?year="/>
        </legend>
    </div>

    <c:if test="${not empty errors}">
        <div class="col-xs-12">
            <div class="alert alert-danger"><form:errors cssClass="error"/></div>
        </div>
    </c:if>

    <form:hidden path="holidaysAccountYear"/>

    <div class="col-md-4 col-md-push-8">
        <span class="help-block">
            <i class="fa fa-fw fa-info-circle"></i>
            <spring:message code="person.form.annualVacation.description"/>
        </span>
    </div>

    <div class="col-md-8 col-md-pull-4">
        <div class="form-group is-required">
            <label for="holidaysAccountValidFrom" class="control-label col-md-3">
                <spring:message code="person.form.annualVacation.period.start"/>:
            </label>

            <div class="col-md-9">
                <form:input id="holidaysAccountValidFrom" path="holidaysAccountValidFrom" class="form-control"
                            cssErrorClass="form-control error" placeholder="${DATE_PATTERN}"/>
                <span class="help-inline"><form:errors path="holidaysAccountValidFrom" cssClass="error"/></span>
            </div>
        </div>

        <div class="form-group is-required">
            <label for="holidaysAccountValidTo" class="control-label col-md-3">
                <spring:message code="person.form.annualVacation.period.end"/>:
            </label>

            <div class="col-md-9">
                <form:input id="holidaysAccountValidTo" path="holidaysAccountValidTo" class="form-control"
                            cssErrorClass="form-control error" placeholder="${DATE_PATTERN}"/>
                <span class="help-inline"><form:errors path="holidaysAccountValidTo" cssClass="error"/></span>
            </div>
        </div>

        <div class="form-group is-required">
            <label class="control-label col-md-3" for="annualVacationDays">
                <spring:message code='person.form.annualVacation.annualVacation'/>:
            </label>

            <div class="col-md-9">
                <form:input path="annualVacationDays" class="form-control" cssErrorClass="form-control error" size="1"
                            id="annualVacationDays"/>
                <span class="help-inline"><form:errors path="annualVacationDays" cssClass="error"/></span>
            </div>
        </div>

        <div class="form-group is-required">
            <label class="control-label col-md-3" for="actualVacationDays">
                <spring:message code='person.form.annualVacation.actualVacation'/>:
            </label>

            <div class="col-md-9">
                <form:input path="actualVacationDays" class="form-control" cssErrorClass="form-control error" size="1"
                            id="actualVacationDays"/>
                <span class="help-inline"><form:errors path="actualVacationDays" cssClass="error"/></span>
            </div>
        </div>

        <div class="form-group is-required">
            <label class="control-label col-md-3" for="remainingVacationDays">
                <spring:message code="person.form.annualVacation.remainingVacation"/>:
            </label>

            <div class="col-md-9">
                <form:input path="remainingVacationDays" class="form-control" cssErrorClass="form-control error"
                            size="1" id="remainingVacationDays"/>
                <span class="help-inline"><form:errors path="remainingVacationDays" cssClass="error"/></span>
            </div>
        </div>

        <div class="form-group is-required">
            <label class="control-label col-md-3">
                <spring:message code="person.form.annualVacation.remainingVacation.notExpiring"/>:
            </label>

            <div class="col-md-9">
                <form:input path="remainingVacationDaysNotExpiring" class="form-control"
                            cssErrorClass="form-control error" size="1" id="remainingVacationDaysNotExpiring"/>
                <span class="help-inline"><form:errors path="remainingVacationDaysNotExpiring" cssClass="error"/></span>
            </div>
        </div>
        
        <div class="form-group">
            <label class="control-label col-md-3" for="comment">
                <spring:message code='person.form.annualVacation.comment'/>:
            </label>
            <div class="col-md-9">
                <span id="text-comment"></span><spring:message code='action.comment.maxChars'/>
                <form:textarea id="comment" rows="3" path="comment" class="form-control" cssErrorClass="form-control error"
                      onkeyup="count(this.value, 'text-comment');"
                      onkeydown="maxChars(this,200); count(this.value, 'text-comment');"
                      placeholder="${COMMENT_PLACEHOLDER}"/>
                <form:errors path="comment" cssClass="error"/>
            </div>
        </div>
        
    </div>
</div>

<div class="form-section">
    <div class="col-xs-12">
        <hr/>
        <button class="btn btn-success col-xs-12 col-sm-5 col-md-2" type="submit"><spring:message code="action.save" /></button>
        <button type="button" class="btn btn-default back col-xs-12 col-sm-5 col-md-2 pull-right">
            <spring:message code="action.cancel"/>
        </button>
    </div>
</div>

</div>

</form:form>

</div>
</div>

</body>

</html>
