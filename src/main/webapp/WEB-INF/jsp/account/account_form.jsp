<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>
<%@taglib prefix="asset" uri = "/WEB-INF/asset.tld"%>

<!DOCTYPE html>
<html lang="${language}" class="tw-<c:out value='${theme}' />">
<head>
    <title>
        <spring:message code="person.form.annualVacation.header.title" arguments="${person.niceName}"/>
    </title>
    <uv:custom-head/>
    <link rel="stylesheet" href="<asset:url value='account_form~app_form~app_statistics~overtime_form~person_overview~sick_note_form~sick_notes~workingtime_form.css' />" />
    <link rel="stylesheet" href="<asset:url value='account_form~app_form~app_statistics~overtime_form~sick_note_form~sick_notes~workingtime_form.css' />" />
    <script>
        window.uv = {};
        window.uv.personId = '<c:out value="${person.id}" />';
        window.uv.webPrefix = "<spring:url value='/web' />";
        window.uv.apiPrefix = "<spring:url value='/api' />";
    </script>
    <uv:datepicker-localisation />
    <uv:vacation-type-colors-script />
    <script defer src="<asset:url value='npm.duetds.js' />"></script>
    <script defer src="<asset:url value='npm.date-fns.js' />"></script>
    <script defer src="<asset:url value='account_form~app_detail~app_form~app_statistics~overtime_form~person_overview~sick_note_form~sick_no~704d57c1.js' />"></script>
    <script defer src="<asset:url value='account_form~app_form~app_statistics~overtime_form~person_overview~sick_note_form~sick_notes~workingtime_form.js' />"></script>
    <script defer src="<asset:url value='account_form~app_form~app_statistics~overtime_form~sick_note_form~sick_notes~workingtime_form.js' />"></script>
    <script defer src="<asset:url value='account_form.js' />"></script>
</head>

<body>

<uv:menu/>

<spring:url var="URL_PREFIX" value="/web"/>

<c:set var="DATE_PATTERN">
    <spring:message code="pattern.date"/>
</c:set>

<c:set var="COMMENT_PLACEHOLDER">
    <spring:message code="person.form.annualVacation.comment.placeholder"/>
</c:set>

<div class="content">
    <div class="container">

        <form:form method="POST" action="${URL_PREFIX}/person/${person.id}/account"
                   modelAttribute="account" class="form-horizontal">

            <form:hidden path="holidaysAccountYear"/>

            <div class="form-section">

                <uv:section-heading>
                    <h1>
                        <spring:message code="person.form.annualVacation.title" arguments="${person.niceName}"/>
                    </h1>
                    <uv:year-selector year="${year}" hrefPrefix="${URL_PREFIX}/person/${person.id}/account?year="/>
                </uv:section-heading>

                <spring:hasBindErrors name="account">
                    <div class="row tw-mb-8">
                        <div class="col-xs-12">
                            <div class="alert alert-danger tw-text-sm">
                                <spring:message code="error.info.message"/>
                            </div>
                        </div>
                    </div>
                </spring:hasBindErrors>

                <div class="row tw-mb-16">
                    <div class="col-md-4 col-md-push-8">
                        <span class="help-block tw-text-sm">
                            <icon:information-circle className="tw-w-4 tw-h-4" solid="true" />
                            <spring:message code="person.form.annualVacation.description"/>
                        </span>
                    </div>

                    <div class="col-md-8 col-md-pull-4">
                        <div class="form-group is-required">
                            <label for="holidaysAccountValidFrom" class="control-label col-md-3 tw-leading-snug">
                                <spring:message code="person.form.annualVacation.period.start"/>:
                            </label>

                            <div class="col-md-9">
                                <form:input
                                    id="holidaysAccountValidFrom"
                                    path="holidaysAccountValidFrom"
                                    data-iso-value="${account.holidaysAccountValidFromIsoValue}"
                                    data-min="${year}-01-01"
                                    data-max="${year}-12-31"
                                    placeholder="${DATE_PATTERN}"
                                    class="form-control"
                                    cssErrorClass="form-control error"
                                />
                                <uv:error-text>
                                    <form:errors path="holidaysAccountValidFrom" />
                                </uv:error-text>
                            </div>
                        </div>

                        <div class="form-group is-required">
                            <label for="holidaysAccountValidTo" class="control-label col-md-3 tw-leading-snug">
                                <spring:message code="person.form.annualVacation.period.end"/>:
                            </label>

                            <div class="col-md-9">
                                <form:input
                                    id="holidaysAccountValidTo"
                                    path="holidaysAccountValidTo"
                                    data-iso-value="${account.holidaysAccountValidToIsoValue}"
                                    data-min="${year}-01-01"
                                    data-max="${year}-12-31"
                                    placeholder="${DATE_PATTERN}"
                                    class="form-control"
                                    cssErrorClass="form-control error"
                                />
                                <uv:error-text>
                                    <form:errors path="holidaysAccountValidTo" />
                                </uv:error-text>
                            </div>
                        </div>

                        <div class="form-group is-required">
                            <label class="control-label col-md-3 tw-leading-snug" for="annualVacationDays">
                                <spring:message code='person.form.annualVacation.annualVacation'/>:
                            </label>

                            <div class="col-md-9">
                                <uv:input-number id="annualVacationDays"
                                            path="annualVacationDays" cssClass="form-control"
                                            cssErrorClass="form-control error"
                                            value="${account.annualVacationDays}"/>
                                <uv:error-text>
                                    <form:errors path="annualVacationDays" />
                                </uv:error-text>
                            </div>
                        </div>

                        <div class="form-group is-required">
                            <label class="control-label col-md-3 tw-leading-snug" for="actualVacationDays">
                                <spring:message code='person.form.annualVacation.actualVacation'/>:
                            </label>

                            <div class="col-md-9">
                                <uv:input-number id="actualVacationDays"
                                                 path="actualVacationDays" cssClass="form-control"
                                                 cssErrorClass="form-control error" step="0.5"
                                                 value="${account.actualVacationDays}"/>
                                <uv:error-text>
                                    <form:errors path="actualVacationDays" />
                                </uv:error-text>
                            </div>
                        </div>

                        <br/>
                        <div class="form-group is-required">
                            <label for="expiryDate" class="control-label col-md-3 tw-leading-snug">
                                <spring:message code="person.form.annualVacation.remainingVacation.expiryDate"/>:
                            </label>

                            <div class="col-md-9">
                                <form:input
                                    id="expiryDate"
                                    path="expiryDate"
                                    data-iso-value="${account.expiryDateToIsoValue}"
                                    data-min="${year}-01-01"
                                    data-max="${year}-12-31"
                                    placeholder="${DATE_PATTERN}"
                                    class="form-control"
                                    cssErrorClass="form-control error"
                                />
                                <uv:error-text>
                                    <form:errors path="expiryDate" />
                                </uv:error-text>
                            </div>
                        </div>

                        <div class="form-group is-required">
                            <label class="control-label col-md-3 tw-leading-snug" for="remainingVacationDays">
                                <spring:message code="person.form.annualVacation.remainingVacation"/>:
                            </label>

                            <div class="col-md-9">
                                <uv:input-number id="remainingVacationDays" path="remainingVacationDays" cssClass="form-control"
                                                 cssErrorClass="form-control error" step="0.5"
                                                 value="${account.remainingVacationDays}"/>
                                <uv:error-text>
                                    <form:errors path="remainingVacationDays" />
                                </uv:error-text>
                            </div>
                        </div>

                        <div class="form-group is-required">
                            <label class="control-label col-md-3 tw-leading-snug" for="remainingVacationDaysNotExpiring">
                                <spring:message code="person.form.annualVacation.remainingVacation.notExpiring"/>:
                            </label>

                            <div class="col-md-9">
                                <uv:input-number id="remainingVacationDaysNotExpiring" path="remainingVacationDaysNotExpiring" cssClass="form-control"
                                                 cssErrorClass="form-control error" step="0.5"
                                                 value="${account.remainingVacationDaysNotExpiring}"/>
                                <uv:error-text>
                                    <form:errors path="remainingVacationDaysNotExpiring" />
                                </uv:error-text>
                            </div>
                        </div>

                        <br/>
                        <div class="form-group">
                            <label class="control-label col-md-3 tw-leading-snug" for="comment">
                                <spring:message code='person.form.annualVacation.comment'/>:
                            </label>
                            <div class="col-md-9">
                                <small>
                                    <span id="text-comment"></span><spring:message code='action.comment.maxChars'/>
                                </small>
                                <form:textarea id="comment" rows="3" path="comment" class="form-control"
                                               cssErrorClass="form-control error"
                                               onkeyup="count(this.value, 'text-comment');"
                                               onkeydown="maxChars(this,200); count(this.value, 'text-comment');"
                                               placeholder="${COMMENT_PLACEHOLDER}"/>
                                <uv:error-text>
                                    <form:errors path="comment" />
                                </uv:error-text>
                            </div>
                        </div>

                    </div>
                </div>
            </div>

            <div class="form-section">
                <div class="row tw-mb-16">
                    <div class="col-xs-12">
                        <hr />
                        <button class="button-main-green col-xs-12 col-sm-5 col-md-2" type="submit"><spring:message
                            code="action.save"/></button>
                        <button type="button" class="button col-xs-12 col-sm-5 col-md-2 pull-right" data-back-button>
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
