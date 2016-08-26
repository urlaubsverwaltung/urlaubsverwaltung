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
    <uv:head/>
</head>

<body>

<uv:menu/>

<spring:url var="URL_PREFIX" value="/web"/>

<div class="content">
    <div class="container">
        <form:form method="POST" action="${URL_PREFIX}/settings/workingtime" modelAttribute="settings"
                   class="form-horizontal" role="form">
            <div class="row">
                <div class="form-section">
                    <div class="col-xs-12">
                        <legend><spring:message code="settings.publicHolidays.title"/></legend>
                    </div>
                    <div class="col-md-4 col-md-push-8">
                        <span class="help-block">
                            <i class="fa fa-fw fa-info-circle"></i>
                            <spring:message code="settings.publicHolidays.description"/>
                        </span>
                    </div>
                    <div class="col-md-8 col-md-pull-4">
                        <div class="form-group is-required">
                            <label class="control-label col-md-4" for="workingDurationForChristmasEve">
                                <spring:message code='settings.publicHolidays.workingDuration.christmasEve'/>:
                            </label>

                            <div class="col-md-8">
                                <form:select path="workingDurationForChristmasEve" id="dayLengthTypes"
                                             class="form-control" cssErrorClass="form-control error">
                                    <c:forEach items="${dayLengthTypes}" var="dayLengthType">
                                        <form:option value="${dayLengthType}">
                                            <spring:message code="${dayLengthType}"/>
                                        </form:option>
                                    </c:forEach>
                                </form:select>
                            </div>
                        </div>
                        <div class="form-group is-required">
                            <label class="control-label col-md-4" for="workingDurationForNewYearsEve">
                                <spring:message code='settings.publicHolidays.workingDuration.newYearsEve'/>:
                            </label>

                            <div class="col-md-8">
                                <form:select path="workingDurationForNewYearsEve" id="dayLengthTypes"
                                             class="form-control" cssErrorClass="form-control error">
                                    <c:forEach items="${dayLengthTypes}" var="dayLengthType">
                                        <form:option value="${dayLengthType}">
                                            <spring:message code="${dayLengthType}"/>
                                        </form:option>
                                    </c:forEach>
                                </form:select>
                            </div>
                        </div>
                        <div class="form-group is-required">
                            <label class="control-label col-md-4" for="federalStateType">
                                <spring:message code='settings.publicHolidays.federalState'/>:
                            </label>

                            <div class="col-md-8">
                                <form:select path="federalState" id="federalStateType" class="form-control"
                                             cssErrorClass="form-control error">
                                    <c:forEach items="${federalStateTypes}" var="federalStateType">
                                        <form:option value="${federalStateType}">
                                            <spring:message code="federalState.${federalStateType}"/>
                                        </form:option>
                                    </c:forEach>
                                </form:select>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="form-section">
                    <div class="col-xs-12">
                        <legend><spring:message code="settings.overtime.title"/></legend>
                    </div>
                    <div class="col-md-4 col-md-push-8">
                        <span class="help-block">
                            <i class="fa fa-fw fa-info-circle"></i>
                            <spring:message code="settings.overtime.description"/>
                        </span>
                    </div>
                    <div class="col-md-8 col-md-pull-4">

                        <div class="form-group is-required">
                            <label class="control-label col-md-4" for="overtimeActive">
                                <spring:message code='settings.overtime.overtimeActive'/>:
                            </label>
                            <div class="col-md-8 radio">
                                <label class="halves">
                                    <form:radiobutton id="overtimeActive" path="overtimeActive"
                                                      value="true"/>
                                    <spring:message code="settings.overtime.overtimeActive.true"/>
                                </label>
                                <label class="halves">
                                    <form:radiobutton id="overtimeActive" path="overtimeActive"
                                                      value="false"/>
                                    <spring:message code="settings.overtime.overtimeActive.false"/>
                                </label>
                            </div>
                        </div>

                        <div class="form-group is-required">
                            <label class="control-label col-md-4" for="maximumOvertime">
                                <spring:message code="settings.overtime.maximum"/>:
                            </label>
                            <div class="col-md-8">
                                <form:input id="maximumOvertime" path="maximumOvertime"
                                            class="form-control" cssErrorClass="form-control error"/>
                                <span class="help-inline">
                                    <form:errors path="maximumOvertime" cssClass="error"/>
                                </span>
                            </div>
                        </div>
                        <div class="form-group is-required">
                            <label class="control-label col-md-4" for="minimumOvertime">
                                <spring:message code="settings.overtime.minimum"/>:
                            </label>
                            <div class="col-md-8">
                                <form:input id="minimumOvertime" path="minimumOvertime"
                                            class="form-control" cssErrorClass="form-control error"/>
                                <span class="help-inline">
                                    <form:errors path="minimumOvertime" cssClass="error"/>
                                </span>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="form-section">
                    <div class="col-xs-12">
                        <hr/>
                        <button type="submit" class="btn btn-success pull-left col-xs-12 col-sm-5 col-md-2">
                            <spring:message code='action.save'/>
                        </button>
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
