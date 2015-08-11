<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="person" tagdir="/WEB-INF/tags/person" %>


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
    <c:when test="${personForm.id == null}">
        <c:set var="METHOD" value="POST"/>
        <c:set var="ACTION" value="${URL_PREFIX}/staff/new"/>
    </c:when>
    <c:otherwise>
        <c:set var="METHOD" value="PUT"/>
        <c:set var="ACTION" value="${URL_PREFIX}/staff/${personForm.id}/edit"/>
    </c:otherwise>
</c:choose>

<form:form method="${METHOD}" action="${ACTION}" modelAttribute="personForm" class="form-horizontal person--form">
<form:hidden path="id" />

<div class="row">

<div class="person--form-part">

    <div class="header col-xs-12">
        <legend><spring:message code="person.form.data.title"/></legend>
    </div>

    <div class="col-md-4 col-md-push-8">
        <span class="help-block">
            <i class="fa fa-fw fa-info-circle"></i>
            <spring:message code="person.form.data.description"/>
        </span>
    </div>
    
    <div class="col-md-8 col-md-pull-4">
        <div class="form-group">
            <label class="control-label col-md-3" for="loginName"><spring:message code="person.form.data.login"/></label>

            <div class="col-md-9">
                <c:choose>
                    <c:when test="${personForm.id == null}">
                        <form:input path="loginName" class="form-control" cssErrorClass="form-control error"/>
                        <span class="help-inline"><form:errors path="loginName" cssClass="error"/></span>
                    </c:when>
                    <c:otherwise>
                        <form:input path="loginName" class="form-control" disabled="true" />
                        <form:hidden path="loginName" value="${personForm.loginName}"/>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <div class="form-group">
            <label class="control-label col-md-3" for="firstName"><spring:message code="person.form.data.firstName"/></label>

            <div class="col-md-9">
                <form:input id="firstName" path="firstName" class="form-control" cssErrorClass="form-control error" />
                <span class="help-inline"><form:errors path="firstName" cssClass="error"/></span>
            </div>
        </div>

        <div class="form-group">
            <label class="control-label col-md-3" for="lastName"><spring:message code="person.form.data.lastName"/></label>

            <div class="col-md-9">
                <form:input id="lastName" path="lastName" class="form-control" cssErrorClass="form-control error" />
                <span class="help-inline"><form:errors path="lastName" cssClass="error"/></span>
            </div>
        </div>

        <div class="form-group">
            <label class="control-label col-md-3" for="email"><spring:message code="person.form.data.email"/></label>

            <div class="col-md-9">
                <form:input id="email" path="email" class="form-control" cssErrorClass="form-control error" />
                <span class="help-inline"><form:errors path="email" cssClass="error"/></span>
            </div>
        </div>
    </div>
    
</div>

<div class="person--form-part">

    <div class="header col-xs-12">
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
        <div class="form-group">

            <label class="control-label col-md-3"><spring:message code="person.form.permissions.roles"/></label>

            <div class="col-md-9">

                <div class="person--role checkbox">
                    <label>
                        <form:checkbox path="permissions" value="INACTIVE"/><spring:message code="person.form.permissions.roles.inactive"/>:
                        <spring:message code="person.form.permissions.roles.inactive.description"/>
                    </label>
                </div>

                <div class="person--role checkbox">
                    <label>
                        <form:checkbox path="permissions" value="USER"/><spring:message code="person.form.permissions.roles.user"/>:
                        <spring:message code="person.form.permissions.roles.user.description"/>
                    </label>
                    <%-- It's obligatory for now that users get mail notifications about progress of their own applications for leave --%>
                    <form:hidden path="notifications" value="NOTIFICATION_USER" />
                </div>

                <div class="person--role checkbox">
                    <label>
                        <form:checkbox path="permissions" value="BOSS"/><spring:message code="person.form.permissions.roles.boss"/>:
                        <spring:message code="person.form.permissions.roles.boss.description"/>
                    </label>
                    <label class="person--mail-notification">
                        <form:checkbox path="notifications" value="NOTIFICATION_BOSS"/>
                        <spring:message code="person.form.notifications.boss"/>
                    </label>
                </div>

                <div class="person--role checkbox">
                    <label>
                        <form:checkbox path="permissions" value="OFFICE"/><spring:message code="person.form.permissions.roles.office"/>:
                        <spring:message code="person.form.permissions.roles.office.description"/>
                    </label>
                    <label class="person--mail-notification">
                        <form:checkbox path="notifications" value="NOTIFICATION_OFFICE"/>
                        <spring:message code="person.form.notifications.office"/>
                    </label>
                </div>

            </div>

        </div>
    </div>

</div>

<div class="person--form-part">

    <div class="header col-xs-12">
        <legend>
            <spring:message code="person.form.annualVacation.title"/>
            <c:choose>
                <c:when test="${personForm.id == null}">
                    <c:out value="${personForm.holidaysAccountYear}" />
                </c:when>
                <c:otherwise>
                    <uv:year-selector year="${personForm.holidaysAccountYear}"/>
                </c:otherwise>
            </c:choose>
        </legend>
    </div>

    <c:if test="${not empty errors}">
        <div class="col-xs-12">
            <div class="alert alert-danger"><form:errors cssClass="error"/></div>
        </div>
    </c:if>
    
    <form:hidden path="holidaysAccountYear" />

    <div class="col-md-4 col-md-push-8">
        <span class="help-block">
            <i class="fa fa-fw fa-info-circle"></i>
            <spring:message code="person.form.annualVacation.description"/>
        </span>
    </div>

    <div class="col-md-8 col-md-pull-4">
        <div class="form-group">
            <label for="holidaysAccountValidFrom" class="control-label col-md-3">
                <spring:message code="person.form.annualVacation.period.start"/>
            </label>

            <div class="col-md-9">
                <form:input id="holidaysAccountValidFrom" path="holidaysAccountValidFrom" class="form-control"
                            cssErrorClass="form-control error" placeholder="dd.MM.yyyy"/>
                <span class="help-inline"><form:errors path="holidaysAccountValidFrom" cssClass="error"/></span>
            </div>
        </div>

        <div class="form-group">
            <label for="holidaysAccountValidTo" class="control-label col-md-3">
                <spring:message code="person.form.annualVacation.period.end"/>
            </label>

            <div class="col-md-9">
                <form:input id="holidaysAccountValidTo" path="holidaysAccountValidTo" class="form-control"
                            cssErrorClass="form-control error" placeholder="dd.MM.yyyy"/>
                <span class="help-inline"><form:errors path="holidaysAccountValidTo" cssClass="error"/></span>
            </div>
        </div>

        <div class="form-group">
            <label class="control-label col-md-3" for="annualVacationDays"><spring:message code='person.form.annualVacation.annualVacation'/></label>

            <div class="col-md-9">
                <form:input path="annualVacationDays" class="form-control" cssErrorClass="form-control error" size="1" id="annualVacationDays"/>
                <span class="help-inline"><form:errors path="annualVacationDays" cssClass="error"/></span>
            </div>
        </div>

        <div class="form-group">
            <label class="control-label col-md-3" for="remainingVacationDays">
                <spring:message code="person.form.annualVacation.remainingVacation"/>
            </label>

            <div class="col-md-9">
                <form:input path="remainingVacationDays" class="form-control" cssErrorClass="form-control error" size="1" id="remainingVacationDays"/>
                <span class="help-inline"><form:errors path="remainingVacationDays" cssClass="error"/></span>
            </div>
        </div>

        <div class="form-group">
            <label class="control-label col-md-3">
                <spring:message code="person.form.annualVacation.remainingVacation.notExpiring"/>
            </label>

            <div class="col-md-9">
                <form:input path="remainingVacationDaysNotExpiring" class="form-control" cssErrorClass="form-control error" size="1" id="remainingVacationDaysNotExpiring"/>
                <span class="help-inline"><form:errors path="remainingVacationDaysNotExpiring" cssClass="error"/></span>
            </div>
        </div>
    </div>

</div>

<div class="person--form-part">

    <div class="header col-xs-12">
        <legend><spring:message code="person.form.workingTime.title"/></legend>
    </div>

    <div class="col-md-4 col-md-push-8">
        <span class="help-block">
            <i class="fa fa-fw fa-info-circle"></i>
            <spring:message code="person.form.workingTime.description"/>
        </span>
    </div>

    <div class="col-md-8 col-md-pull-4">
        <c:if test="${fn:length(workingTimes) > 1}">

            <div class="form-group">
                <label class="control-label col-md-3"><spring:message code='person.form.workingTime.title'/></label>

                <div class="controls col-md-8">
                    <c:forEach items="${workingTimes}" var="time">
                        <spring:message code="person.form.workingTime.validityPeriod" />
                        <uv:date date="${time.validFrom}" />
                        <br />
                        <c:if test="${time.monday.duration > 0}">
                            <spring:message code="MONDAY" />
                        </c:if>
                        <c:if test="${time.tuesday.duration > 0}">
                            <spring:message code="TUESDAY" />
                        </c:if>
                        <c:if test="${time.wednesday.duration > 0}">
                            <spring:message code="WEDNESDAY" />
                        </c:if>
                        <c:if test="${time.thursday.duration > 0}">
                            <spring:message code="THURSDAY" />
                        </c:if>
                        <c:if test="${time.friday.duration > 0}">
                            <spring:message code="FRIDAY" />
                        </c:if>
                        <c:if test="${time.saturday.duration > 0}">
                            <spring:message code="SATURDAY" />
                        </c:if>
                        <c:if test="${time.sunday.duration > 0}">
                            <spring:message code="SUNDAY" />
                        </c:if>
                        <br />
                        <br />
                    </c:forEach>
                </div>
            </div>

        </c:if>

        <div class="form-group">
            <label class="control-label col-md-3">
                <spring:message code="person.form.workingTime.validityPeriod" />
            </label>
            <div class="col-md-9">
                <form:input id="validFrom" path="validFrom" class="form-control" cssErrorClass="form-control error" placeholder="dd.MM.yyyy" />
                <span class="help-inline"><form:errors path="validFrom" cssClass="error"/></span>
            </div>
        </div>

        <div class="form-group">
            <label class="control-label col-md-3">
                <spring:message code="person.form.workingTime.weekDays" />
            </label>
            <div class="col-md-8 checkbox">
                <c:forEach items="${weekDays}" var="weekDay">
                    <label class="checkbox" for="${weekDay}">
                        <form:checkbox id="${weekDay}" path="workingDays" value="${weekDay.dayOfWeek}" />
                        <spring:message code='${weekDay}'/>
                    </label>
                </c:forEach>
            </div>
        </div>
    </div>

</div>

<div class="person--form-part">
    <div class="col-xs-12">
        <hr/>
        <button class="btn btn-success col-xs-12 col-sm-5 col-md-2" type="submit"><spring:message code="action.save" /></button>
        <a class="btn btn-default col-xs-12 col-sm-5 col-md-2 pull-right" href="${URL_PREFIX}/staff"><spring:message code="action.cancel"/></a>
    </div>
</div>

</div>

</form:form>

</div>
</div>

</body>

</html>
