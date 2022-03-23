<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>
<%@taglib prefix="asset" uri="/WEB-INF/asset.tld" %>

<!DOCTYPE html>
<html lang="${language}" class="tw-<c:out value='${theme}' />">
<head>
    <title>
        <spring:message code="person.form.basedata.title" arguments="${personBasedata.niceName}"/>
    </title>
    <uv:custom-head/>
    <script defer src="<asset:url value='person_basedata.js' />"></script>
</head>

<body>

<uv:menu/>

<spring:url var="URL_PREFIX" value="/web"/>

<div class="content">
    <div class="container">

        <uv:section-heading>
            <h2>
                <spring:message code="person.form.basedata.title" arguments="${personBasedata.niceName}"/>
            </h2>
        </uv:section-heading>

        <div class="tw-flex tw-items-center tw-gap-4 sm:tw-gap-6 tw-mb-4 md:tw-mb-12">
            <div class="tw-p-1">
                <uv:avatar url="${personBasedata.gravatarURL}?d=mm&s=120" username="${personBasedata.niceName}"
                           width="60px" height="60px" border="true"/>
            </div>
            <div>
                <div class="tw-mb-1">
                    <a href="${URL_PREFIX}/person/${personBasedata.personId}/overview" class="tw-text-lg print:no-link">
                        <c:out value="${personBasedata.niceName}"/>
                    </a>
                </div>
                <a href="mailto:<c:out value='${personBasedata.email}'/>" class="tw-text-sm print:no-link">
                    <span class="tw-flex tw-items-center">
                        <icon:mail className="tw-w-4 tw-h-4"/>
                        &nbsp;<c:out value="${personBasedata.email}"/>
                    </span>
                </a>
            </div>
        </div>


        <form:form method="POST" action="${URL_PREFIX}/person/${personBasedata.personId}/basedata" modelAttribute="personBasedata" class="form-horizontal">
            <form:hidden path="personId"/>
            <form:hidden path="niceName"/>
            <form:hidden path="gravatarURL"/>
            <form:hidden path="email"/>

            <div class="form-section">
                <div class="row tw-mb-16">
                    <div class="col-md-4 col-md-push-8">
                        <span class="help-block tw-text-sm">
                        </span>
                    </div>
                    <div class="col-md-8 col-md-pull-4">
                        <div class="form-group">
                            <label class="control-label col-md-3 tw-leading-snug" for="personnelNumber">
                                <spring:message code='person.form.basedata.personnelNumber'/>:
                            </label>

                            <div class="col-md-9">
                                <form:input id="personnelNumber"
                                            path="personnelNumber" cssClass="form-control"
                                            cssErrorClass="form-control error"
                                            value="${personBasedata.personnelNumber}"/>
                                <uv:error-text>
                                    <form:errors path="personnelNumber"/>
                                </uv:error-text>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="form-section ">
                <div class="row tw-mb-16">
                    <div class="col-md-4 col-md-push-8">
                        <span class="help-block tw-text-sm">
                        </span>
                    </div>
                    <div class="col-md-8 col-md-pull-4">

                        <div class="form-group">
                            <label class="control-label col-md-3 tw-leading-snug" for="additionalInfo">
                                <spring:message code='person.form.basedata.additionalInformation'/>:
                            </label>

                            <div class="col-md-9">
                                <small>
                                    <span id="text-additional-info"></span><spring:message
                                    code='person.form.basedata.additionalInformation.maxChars'/>
                                </small>
                                <form:textarea id="additionalInfo" rows="5" path="additionalInfo"
                                               cssClass="form-control"
                                               cssErrorClass="form-control error"
                                               onkeyup="count(this.value, 'text-additional-info');"
                                               onkeydown="maxChars(this,255); count(this.value, 'text-additional-info');"
                                               value="${personBasedata.additionalInfo}"/>
                                <uv:error-text>
                                    <form:errors path="additionalInfo"/>
                                </uv:error-text>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="form-section tw-mb-16">
                <div class="row">
                    <div class="col-xs-12">
                        <hr/>
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
