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
        <spring:message code="person.form.masterdata.title"/>
    </title>
    <uv:custom-head/>
    <script defer src="<asset:url value='person_form.js' />"></script>
</head>

<body>

<uv:menu/>

<spring:url var="URL_PREFIX" value="/web"/>

<div class="content">
    <div class="container">

        <uv:section-heading>
            <h2>
                <spring:message code="person.form.masterdata.title" arguments="${person.niceName}"/>
            </h2>
        </uv:section-heading>

        <div class="tw-flex tw-items-center tw-gap-4 sm:tw-gap-6 tw-mb-4 md:tw-mb-12">
            <div class="tw-p-1">
                <uv:avatar url="${person.gravatarURL}?d=mm&s=120" username="${person.niceName}" width="60px" height="60px" border="true" />
            </div>
            <div>
                <div class="tw-mb-1">
                    <a href="${URL_PREFIX}/person/${person.id}/overview" class="tw-text-lg print:no-link">
                        <c:out value="${person.niceName}"/>
                    </a>
                </div>
                <a href="mailto:<c:out value='${person.email}'/>" class="tw-text-sm print:no-link">
                    <span class="tw-flex tw-items-center">
                        <icon:mail className="tw-w-4 tw-h-4" />
                        &nbsp;<c:out value="${person.email}"/>
                    </span>
                </a>
            </div>
        </div>

        <form:form method="POST" action="${URL_PREFIX}/person/${person.id}/masterdata" modelAttribute="person" class="form-horizontal">
            <form:hidden path="id" />
            <form:hidden path="niceName" />
            <form:hidden path="gravatarURL" />
            <form:hidden path="email" />

            <div class="form-section tw-mb-16">
                <div class="row">
                    <div class="form-group">
                        <label class="control-label col-md-3 tw-leading-snug" for="personnelNumber">
                            <spring:message code='person.form.masterdata.personnel_number'/>:
                        </label>

                        <div class="col-md-9">
                            <form:input id="personnelNumber"
                                             path="personnelNumber" cssClass="form-control"
                                             cssErrorClass="form-control error"
                                             value="${account.personnelNumber}"/>
                            <uv:error-text>
                                <form:errors path="personnelNumber" />
                            </uv:error-text>
                        </div>
                    </div>
                </div>
            </div>

            <div class="form-section tw-mb-16">
                <div class="row">
                    <div class="form-group">
                        <label class="control-label col-md-3 tw-leading-snug" for="additionalInfo">
                            <spring:message code='person.form.masterdata.additional_info'/>:
                        </label>

                        <div class="col-md-9">
                            <form:input id="additionalInfo"
                                        path="additionalInfo" cssClass="form-control"
                                        cssErrorClass="form-control error"
                                        value="${account.additionalInfo}"/>
                            <uv:error-text>
                                <form:errors path="additionalInfo" />
                            </uv:error-text>
                        </div>
                    </div>
                </div>
            </div>

            <div class="form-section tw-mb-16">
                <div class="row">
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
