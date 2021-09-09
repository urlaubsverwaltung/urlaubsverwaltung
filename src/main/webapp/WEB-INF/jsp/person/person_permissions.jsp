<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>
<%@taglib prefix="asset" uri="/WEB-INF/asset.tld" %>

<!DOCTYPE html>
<html lang="${language}">
<head>
    <title>
        <spring:message code="person.form.data.header.title"/>
    </title>
    <uv:custom-head/>
    <script defer src="<asset:url value='person_form.js' />"></script>
</head>

<body>

<uv:menu/>

<spring:url var="URL_PREFIX" value="/web"/>

<div class="content">
    <div class="container">

        <form:form method="POST" action="${URL_PREFIX}/person/${person.id}/permissions" modelAttribute="person" class="form-horizontal">
            <form:hidden path="id"/>

            <div class="form-section tw-mb-16">
                <uv:section-heading>
                    <h2>
                        <spring:message code="person.form.permissions.title" arguments="${person.niceName}"/>
                    </h2>
                </uv:section-heading>

                <div class="row">
                    <c:set var="permissionsError">
                        <form:errors path="permissions" />
                    </c:set>

                    <c:set var="notificationsError">
                        <form:errors path="notifications" />
                    </c:set>

                    <c:if test="${not empty permissionsError}">
                        <div class="col-xs-12">
                            <div class="alert alert-danger tw-text-red-800">${permissionsError}</div>
                        </div>
                    </c:if>

                    <c:if test="${not empty notificationsError}">
                        <div class="col-xs-12">
                            <div class="alert alert-danger tw-text-red-800">${notificationsError}</div>
                        </div>
                    </c:if>

                    <div class="col-md-4 col-md-push-8">
                        <span class="help-block tw-text-sm">
                            <icon:information-circle className="tw-w-4 tw-h-4" solid="true" />
                            <spring:message code="person.form.permissions.description"/>
                        </span>
                    </div>

                    <div class="col-md-8 col-md-pull-4">
                        <div class="form-group is-required">

                            <label class="control-label col-md-3">
                                <spring:message code="person.form.permissions.roles"/>:
                            </label>

                            <div class="col-md-9">

                                <div class="checkbox tw-pb-2 tw-mb-2">
                                    <strong class="tw-mb-2 tw-block">
                                        <spring:message code="person.form.permissions.roles.INACTIVE"/>
                                    </strong>
                                    <label class="tw-leading-snug">
                                        <form:checkbox path="permissions" value="INACTIVE"/><spring:message
                                        code="person.form.permissions.roles.INACTIVE.description"/>
                                    </label>
                                </div>

                                <div class="checkbox tw-pb-2 tw-mb-2">
                                    <strong class="tw-mb-2 tw-block">
                                        <spring:message code="person.form.permissions.roles.USER"/>
                                    </strong>
                                    <label class="tw-leading-snug">
                                        <form:checkbox path="permissions" value="USER"/><spring:message
                                        code="person.form.permissions.roles.USER.description"/>
                                    </label>
                                        <%-- It's obligatory for now that users get mail notifications about progress of their own applications for leave --%>
                                    <form:hidden path="notifications" value="NOTIFICATION_USER"/>
                                </div>

                                <div class="checkbox tw-pb-2 tw-mb-2">
                                    <strong class="tw-mb-2 tw-block">
                                        <spring:message code="person.form.permissions.roles.DEPARTMENT_HEAD"/>
                                    </strong>
                                    <label class="tw-leading-snug tw-mb-2">
                                        <form:checkbox path="permissions" value="DEPARTMENT_HEAD"/><spring:message
                                        code="person.form.permissions.roles.DEPARTMENT_HEAD.description"/>
                                    </label>
                                    <label class="tw-leading-snug tw-ml-5 tw-pb-2">
                                        <form:checkbox path="notifications" value="NOTIFICATION_DEPARTMENT_HEAD"/>
                                        <spring:message code="person.form.notifications.DEPARTMENT_HEAD"/>
                                    </label>
                                    <label class="tw-leading-snug ${!empty departments ? 'info' : ''}">
                                        <icon:information-circle className="tw-w-4 tw-h-4" solid="true" />
                                        <c:choose>
                                            <c:when test="${empty departments}">
                                                <spring:message
                                                    code="person.form.permissions.roles.DEPARTMENT_HEAD.departments.none.description"/>
                                            </c:when>
                                            <c:otherwise>
                                                <spring:message
                                                    code="person.form.permissions.roles.DEPARTMENT_HEAD.departments.description"/>
                                                <c:forEach items="${departments}" var="department" varStatus="loop">
                                                    <c:out value="${department.name}${!loop.last ? ',' : ''}"/>
                                                </c:forEach>
                                            </c:otherwise>
                                        </c:choose>
                                    </label>
                                </div>

                                <div class="checkbox tw-pb-2 tw-mb-2">
                                    <strong class="tw-mb-2 tw-block">
                                        <spring:message code="person.form.permissions.roles.SECOND_STAGE_AUTHORITY"/>
                                    </strong>
                                    <label class="tw-leading-snug tw-mb-2">
                                        <form:checkbox path="permissions"
                                                       value="SECOND_STAGE_AUTHORITY"/><spring:message
                                        code="person.form.permissions.roles.SECOND_STAGE_AUTHORITY.description"/>
                                    </label>
                                    <label class="tw-leading-snug tw-ml-5 tw-pb-2">
                                        <form:checkbox path="notifications"
                                                       value="NOTIFICATION_SECOND_STAGE_AUTHORITY"/>
                                        <spring:message code="person.form.notifications.SECOND_STAGE_AUTHORITY"/>
                                    </label>
                                    <label class="tw-leading-snug ${!empty secondStageDepartments ? 'info' : ''}">
                                        <icon:information-circle className="tw-w-4 tw-h-4" solid="true" />
                                        <c:choose>
                                            <c:when test="${empty secondStageDepartments}">
                                                <spring:message
                                                    code="person.form.permissions.roles.SECOND_STAGE_AUTHORITY.departments.none.description"/>
                                            </c:when>
                                            <c:otherwise>
                                                <spring:message
                                                    code="person.form.permissions.roles.SECOND_STAGE_AUTHORITY.departments.description"/>
                                                <c:forEach items="${secondStageDepartments}" var="department"
                                                           varStatus="loop">
                                                    <c:out value="${department.name}${!loop.last ? ',' : ''}"/>
                                                </c:forEach>
                                            </c:otherwise>
                                        </c:choose>
                                    </label>
                                </div>

                                <div class="checkbox tw-pb-2 tw-mb-2">
                                    <strong class="tw-mb-2 tw-block">
                                        <spring:message code="person.form.permissions.roles.BOSS"/>
                                    </strong>
                                    <label class="tw-leading-snug tw-mb-2">
                                        <form:checkbox path="permissions" value="BOSS"/><spring:message
                                        code="person.form.permissions.roles.BOSS.description"/>
                                    </label>
                                    <label class="tw-leading-snug tw-mb-2">
                                        <form:radiobutton path="notifications" value="NOTIFICATION_BOSS_ALL"/>
                                        <spring:message code="person.form.notifications.BOSS.all"/>
                                    </label>
                                    <label class="tw-leading-snug">
                                        <form:radiobutton path="notifications" value="NOTIFICATION_BOSS_DEPARTMENTS"/>
                                        <spring:message code="person.form.notifications.BOSS.departments"/>
                                    </label>
                                    <label class="tw-leading-snug">
                                        <form:radiobutton path="notifications" value=""/>
                                        <spring:message code="person.form.notifications.BOSS.none"/>
                                    </label>
                                </div>

                                <div class="checkbox">
                                    <strong class="tw-mb-2 tw-block">
                                        <spring:message code="person.form.permissions.roles.OFFICE"/>
                                    </strong>
                                    <label class="tw-leading-snug tw-mb-2">
                                        <form:checkbox path="permissions" value="OFFICE"/>
                                        <spring:message code="person.form.permissions.roles.OFFICE.description"/>
                                    </label>
                                    <label class="tw-leading-snug tw-ml-5 tw-pb-2">
                                        <form:checkbox path="notifications" value="NOTIFICATION_OFFICE"/>
                                        <spring:message code="person.form.notifications.OFFICE"/>
                                    </label>
                                    <label class="tw-leading-snug tw-ml-5 tw-pb-2">
                                        <form:checkbox path="notifications" value="OVERTIME_NOTIFICATION_OFFICE"/>
                                        <spring:message code="person.form.notifications.OFFICE.overtime"/>
                                    </label>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="form-section tw-mb-16">
                <div class="row">
                    <div class="col-xs-12">
                        <hr />
                        <button class="btn btn-success col-xs-12 col-sm-5 col-md-2" type="submit"><spring:message
                            code="action.save"/></button>
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
