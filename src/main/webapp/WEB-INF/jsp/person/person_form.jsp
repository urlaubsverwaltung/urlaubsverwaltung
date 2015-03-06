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

<form:form method="${METHOD}" action="${ACTION}" modelAttribute="personForm" class="form-horizontal">
<form:hidden path="id" />

<div class="row">

<div class="col-xs-12 col-md-6">

    <div class="header">

        <legend>
            <p>
                <spring:message code="person.data"/>
            </p>
        </legend>

    </div>
    
    <div class="form-group">
        <label class="control-label col-md-4" for="login"><spring:message code='login'/></label>

        <div class="col-md-7">
            <c:choose>
                <c:when test="${personForm.id == null}">
                    <form:input id="login" path="loginName" class="form-control" cssErrorClass="form-control error"/>
                    <span class="help-inline"><form:errors path="loginName" cssClass="error"/></span>
                </c:when>
                <c:otherwise>
                    <c:out value="${personForm.loginName}"/>
                    <form:hidden path="loginName" value="${personForm.loginName}"/>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <div class="form-group">
        <label class="control-label col-md-4" for="firstName"><spring:message code='firstname'/></label>

        <div class="col-md-7">
            <form:input id="firstName" path="firstName" class="form-control" cssErrorClass="form-control error" />
            <span class="help-inline"><form:errors path="firstName" cssClass="error"/></span>
        </div>
    </div>

    <div class="form-group">
        <label class="control-label col-md-4" for="lastName"><spring:message code='lastname'/></label>

        <div class="col-md-7">
            <form:input id="lastName" path="lastName" class="form-control" cssErrorClass="form-control error" />
            <span class="help-inline"><form:errors path="lastName" cssClass="error"/></span>
        </div>
    </div>

    <div class="form-group">
        <label class="control-label col-md-4" for="email"><spring:message code='email'/></label>

        <div class="col-md-7">
            <form:input id="email" path="email" class="form-control" cssErrorClass="form-control error" />
            <span class="help-inline"><form:errors path="email" cssClass="error"/></span>
        </div>
    </div>
    
</div>

<div class="col-xs-12 col-md-6">

    <div class="header">

        <legend>
            <p>
                <spring:message code="role"/>
            </p>
        </legend>

    </div>

    <div class="form-group">

        <div class="col-md-11 checkbox">

            <span class="help-inline">
                <form:errors path="permissions" cssClass="error"/>
                <form:errors path="notifications" cssClass="error"/>
            </span>

            <div class="person--role">
                <label class="checkbox">
                    <form:checkbox path="permissions" value="INACTIVE"/><b><spring:message code="role.inactive"/></b>:
                    <spring:message code="role.inactive.dsc"/>
                </label>
            </div>

            <div class="person--role">
                <label class="checkbox">
                    <form:checkbox path="permissions" value="USER"/><b><spring:message code="role.user"/></b>:
                    <spring:message code="role.user.dsc"/>
                </label>
                <%-- It's obligatory for now that users get mail notifications about progress of their own applications for leave --%>
                <form:hidden path="notifications" value="NOTIFICATION_USER" />
            </div>

            <div class="person--role">
                <label class="checkbox">
                    <form:checkbox path="permissions" value="BOSS"/><b><spring:message code="role.boss"/></b>:
                    <spring:message code="role.boss.dsc"/>
                </label>
                <label class="checkbox person--mail-notification">
                    <form:checkbox path="notifications" value="NOTIFICATION_BOSS"/>
                    <spring:message code="notification.boss"/>
                </label>
            </div>

            <div class="person--role">
                <label class="checkbox">
                    <form:checkbox path="permissions" value="OFFICE"/><b><spring:message code="role.office"/></b>:
                    <spring:message code="role.office.dsc"/>
                </label>
                <label class="checkbox person--mail-notification">
                    <form:checkbox path="notifications" value="NOTIFICATION_OFFICE"/>
                    <spring:message code="notification.office"/>
                </label>
            </div>

        </div>

    </div>

</div>

</div>


<div class="row">

<div class="col-xs-12 col-md-6">

    <div class="header">

        <legend>
            <p><spring:message code="entitlement"/></p>
            <p><spring:message code="for"/></p>
            <p>
                <c:choose>
                    <c:when test="${personForm.id == null}">
                        <c:out value="${personForm.holidaysAccountYear}" />
                    </c:when>
                    <c:otherwise>
                        <uv:year-selector year="${personForm.holidaysAccountYear}"/>
                    </c:otherwise>
                </c:choose>
            </p>
        </legend>

    </div>

    <c:if test="${not empty errors}">
        <div class="col-md-11 alert alert-danger"><form:errors cssClass="error"/></div>
    </c:if>
    
    <form:hidden path="holidaysAccountYear" />

    <div class="form-group">
        <label for="holidaysAccountValidFrom" class="control-label col-md-4">
            <spring:message code="From"/>
        </label>

        <div class="col-md-7">
            <form:input id="holidaysAccountValidFrom" path="holidaysAccountValidFrom" class="form-control"
                        cssErrorClass="form-control error" placeholder="dd.MM.yyyy"/>
            <span class="help-inline"><form:errors path="holidaysAccountValidFrom" cssClass="error"/></span>
        </div>
    </div>

    <div class="form-group">
        <label for="holidaysAccountValidTo" class="control-label col-md-4">
            <spring:message code="To"/>
        </label>

        <div class="col-md-7">
            <form:input id="holidaysAccountValidTo" path="holidaysAccountValidTo" class="form-control"
                        cssErrorClass="form-control error" placeholder="dd.MM.yyyy"/>
            <span class="help-inline"><form:errors path="holidaysAccountValidTo" cssClass="error"/></span>
        </div>
    </div>

    <div class="form-group">
        <label class="control-label col-md-4" for="annualVacationDays"><spring:message code='person.annual.vacation'/></label>

        <div class="col-md-7">
            <form:input path="annualVacationDays" class="form-control" cssErrorClass="form-control error" size="1" id="annualVacationDays"/>
            <span class="help-inline"><form:errors path="annualVacationDays" cssClass="error"/></span>
        </div>
    </div>

    <div class="form-group">
        <label class="control-label col-md-4" for="remainingVacationDays">
            <spring:message code="remaining"/>&nbsp;<spring:message code="last.year"/>
        </label>

        <div class="col-md-7">
            <form:input path="remainingVacationDays" class="form-control" cssErrorClass="form-control error" size="1" id="remainingVacationDays"/>
            <span class="help-inline"><form:errors path="remainingVacationDays" cssClass="error"/></span>
        </div>
    </div>

    <div class="form-group">
        <label class="control-label col-md-4">
            <spring:message code='person.expire'/>
        </label>

        <div class="col-md-7 radio">
            <label class="halves">
                <form:radiobutton id="expireYes" path="remainingVacationDaysExpire" value="true"/>
                <spring:message code='yes'/>
            </label>

            <label class="halves">
                <form:radiobutton id="expireNo" path="remainingVacationDaysExpire" value="false"/>
                <spring:message code='no'/>
            </label>
        </div>
    </div>

</div>

<div class="col-xs-12 col-md-6">

    <div class="header">

        <legend>
            <p>
                <spring:message code="working.times"/>
            </p>
        </legend>

    </div>

    <c:if test="${fn:length(workingTimes) > 1}">

        <div class="form-group">
            <label class="control-label col-md-4"><spring:message code='working.times'/></label>

            <div class="controls col-md-7">
                <c:forEach items="${workingTimes}" var="time">
                    <spring:message code="working.time.valid.from" />
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
        <label class="control-label col-md-4">
            <spring:message code="working.time.valid.from" />
        </label>
        <div class="col-md-7">
            <form:input id="validFrom" path="validFrom" class="form-control" cssErrorClass="form-control error" placeholder="dd.MM.yyyy" />
            <span class="help-inline"><form:errors path="validFrom" cssClass="error"/></span>
        </div>
    </div>

    <div class="form-group">
        <label class="control-label col-md-4">
            <spring:message code="working.time.days" />
        </label>
        <div class="col-md-7 checkbox">
            <c:forEach items="${weekDays}" var="weekDay">
                <label class="checkbox" for="${weekDay}">
                    <form:checkbox id="${weekDay}" path="workingDays" value="${weekDay.dayOfWeek}" />
                    &nbsp;<spring:message code='${weekDay}'/>
                </label>
            </c:forEach>
        </div>
    </div>

</div>

</div>

<div class="row">
    <div class="col-xs-12">

        <hr/>

        <button class="btn btn-success col-xs-12 col-sm-5 col-md-2" type="submit"><i class='fa fa-check'></i>&nbsp;<spring:message code="action.save" /></button>
        <a class="btn btn-default col-xs-12 col-sm-5 col-md-2 pull-right" href="${URL_PREFIX}/staff"><i class='fa fa-remove'></i>&nbsp;<spring:message code="action.cancel"/></a>

    </div>
</div>

</form:form>

</div>
</div>

</body>

</html>
