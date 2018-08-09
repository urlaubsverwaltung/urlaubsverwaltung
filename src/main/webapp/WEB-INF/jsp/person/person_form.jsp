<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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

<div class="content">
<div class="container">

<c:choose>
    <c:when test="${person.id == null}">
        <c:set var="ACTION" value="${URL_PREFIX}/staff"/>
    </c:when>
    <c:otherwise>
        <c:set var="ACTION" value="${URL_PREFIX}/staff/${person.id}/edit"/>
    </c:otherwise>
</c:choose>

<form:form method="POST" action="${ACTION}" modelAttribute="person" class="form-horizontal">
<form:hidden path="id" />
<form:hidden path="password" />

<div class="row">

<div class="form-section">

    <div class="col-xs-12">
        <legend><spring:message code="person.form.data.title"/></legend>
    </div>

    <div class="col-md-4 col-md-push-8">
        <span class="help-block">
            <i class="fa fa-fw fa-info-circle"></i>
            <spring:message code="person.form.data.description"/>
        </span>
    </div>

    <div class="col-md-8 col-md-pull-4">
        <c:set var="LOGIN_IS_REQUIRED" value="${person.id == null ? 'is-required' : ''}"/>
        <div class="form-group ${LOGIN_IS_REQUIRED}">
            <label class="control-label col-md-3" for="loginName">
                <spring:message code="person.form.data.login"/>:
            </label>

            <div class="col-md-9">
                <c:choose>
                    <c:when test="${person.id == null}">
                        <form:input path="loginName" class="form-control" cssErrorClass="form-control error"/>
                        <span class="help-inline"><form:errors path="loginName" cssClass="error"/></span>
                    </c:when>
                    <c:otherwise>
                        <form:input path="loginName" class="form-control" disabled="true" />
                        <form:hidden path="loginName" value="${person.loginName}"/>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <div class="form-group is-required">
            <label class="control-label col-md-3" for="firstName">
                <spring:message code="person.form.data.firstName"/>:
            </label>

            <div class="col-md-9">
                <form:input id="firstName" path="firstName" class="form-control" cssErrorClass="form-control error" />
                <span class="help-inline"><form:errors path="firstName" cssClass="error"/></span>
            </div>
        </div>

        <div class="form-group is-required">
            <label class="control-label col-md-3" for="lastName">
                <spring:message code="person.form.data.lastName"/>:
            </label>

            <div class="col-md-9">
                <form:input id="lastName" path="lastName" class="form-control" cssErrorClass="form-control error" />
                <span class="help-inline"><form:errors path="lastName" cssClass="error"/></span>
            </div>
        </div>

        <div class="form-group is-required">
            <label class="control-label col-md-3" for="email">
                <spring:message code="person.form.data.email"/>:
            </label>

            <div class="col-md-9">
                <form:input id="email" path="email" class="form-control" cssErrorClass="form-control error" />
                <span class="help-inline"><form:errors path="email" cssClass="error"/></span>
            </div>
        </div>
    </div>

</div>

<div class="form-section">

    <div class="col-xs-12">
        <legend><spring:message code="person.form.permissions.title"/></legend>
    </div>

    <c:set var="permissionsError">
        <form:errors path="permissions" cssClass="error"/>
    </c:set>

    <c:set var="notificationsError">
        <form:errors path="notifications" cssClass="error"/>
    </c:set>

    <c:if test="${not empty permissionsError}">
        <div class="col-xs-12">
            <div class="alert alert-danger">${permissionsError}</div>
        </div>
    </c:if>

    <c:if test="${not empty notificationsError}">
        <div class="col-xs-12">
            <div class="alert alert-danger">${notificationsError}</div>
        </div>
    </c:if>

    <div class="col-md-4 col-md-push-8">
    <span class="help-block">
        <i class="fa fa-fw fa-info-circle"></i>
        <spring:message code="person.form.permissions.description"/>
    </span>
    </div>

    <div class="col-md-8 col-md-pull-4">
        <div class="form-group is-required">

            <label class="control-label col-md-3">
                <spring:message code="person.form.permissions.roles"/>:
            </label>

            <div class="col-md-9">

                <div class="person--role checkbox">
                    <label>
                        <form:checkbox path="permissions" value="INACTIVE"/><spring:message code="person.form.permissions.roles.INACTIVE"/>
                    </label>
                </div>

                <div class="person--role checkbox">
                    <label>
                        <form:checkbox path="permissions" value="USER"/><spring:message code="person.form.permissions.roles.USER"/>
                    </label>
                        <%-- It's obligatory for now that users get mail notifications about progress of their own applications for leave --%>
                    <form:hidden path="notifications" value="NOTIFICATION_USER" />
                </div>

                <div class="person--role checkbox">
                    <label>
                        <form:checkbox path="permissions" value="DEPARTMENT_HEAD"/><spring:message code="person.form.permissions.roles.DEPARTMENT_HEAD"/>
                    </label>
                    <label class="person--mail-notification">
                        <form:checkbox path="notifications" value="NOTIFICATION_DEPARTMENT_HEAD"/>
                        <spring:message code="person.form.notifications.DEPARTMENT_HEAD"/>
                    </label>
                    <label class="${!empty departments ? 'info' : ''}">
                        <i class="fa fa-fw fa-info-circle"></i>
                        <c:choose>
                            <c:when test="${empty departments}">
                                <spring:message code="person.form.permissions.roles.DEPARTMENT_HEAD.departments.none"/>
                            </c:when>
                            <c:otherwise>
                                <spring:message code="person.form.permissions.roles.DEPARTMENT_HEAD.departments"/>
                                <c:forEach items="${departments}" var="department" varStatus="loop">
                                    <c:out value="${department.name}${!loop.last ? ',' : ''}"/>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </label>
                </div>

                <div class="person--role checkbox">
                    <label>
                        <form:checkbox path="permissions" value="SECOND_STAGE_AUTHORITY"/><spring:message code="person.form.permissions.roles.SECOND_STAGE_AUTHORITY"/>
                    </label>
                    <label class="person--mail-notification">
                        <form:checkbox path="notifications" value="NOTIFICATION_SECOND_STAGE_AUTHORITY"/>
                        <spring:message code="person.form.notifications.SECOND_STAGE_AUTHORITY"/>
                    </label>
                    <label class="${!empty secondStageDepartments ? 'info' : ''}">
                        <i class="fa fa-fw fa-info-circle"></i>
                        <c:choose>
                            <c:when test="${empty secondStageDepartments}">
                                <spring:message code="person.form.permissions.roles.SECOND_STAGE_AUTHORITY.departments.none"/>
                            </c:when>
                            <c:otherwise>
                                <spring:message code="person.form.permissions.roles.SECOND_STAGE_AUTHORITY.departments"/>
                                <c:forEach items="${secondStageDepartments}" var="department" varStatus="loop">
                                    <c:out value="${department.name}${!loop.last ? ',' : ''}"/>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </label>
                </div>

                <div class="person--role checkbox">
                    <label>
                        <form:checkbox path="permissions" value="BOSS"/><spring:message code="person.form.permissions.roles.BOSS"/>
                    </label>
                    <label class="person--mail-notification">
                        <form:checkbox path="notifications" value="NOTIFICATION_BOSS"/>
                        <spring:message code="person.form.notifications.BOSS"/>
                    </label>
                </div>

                <div class="person--role checkbox">
                    <label>
                        <form:checkbox path="permissions" value="OFFICE"/><spring:message code="person.form.permissions.roles.OFFICE"/>
                    </label>
                    <label class="person--mail-notification">
                        <form:checkbox path="notifications" value="NOTIFICATION_OFFICE"/>
                        <spring:message code="person.form.notifications.OFFICE"/>
                    </label>
                    <label class="person--mail-notification">
                        <form:checkbox path="notifications" value="OVERTIME_NOTIFICATION_OFFICE"/>
                        <spring:message code="person.form.notifications.OFFICE.overtime"/>
                    </label>
                </div>

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
