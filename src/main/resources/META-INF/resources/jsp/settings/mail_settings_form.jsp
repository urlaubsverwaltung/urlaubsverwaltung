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
        <form:form method="POST" action="${URL_PREFIX}/settings/mail" modelAttribute="settings"
                   class="form-horizontal" role="form">
            <div class="row">
                <div class="form-section">
                    <div class="col-xs-12">
                        <legend><spring:message code="settings.mail.title"/></legend>
                    </div>
                    <div class="col-md-4 col-md-push-8">
                        <span class="help-block">
                            <i class="fa fa-fw fa-info-circle"></i>
                            <spring:message code="settings.mail.description"/>
                        </span>
                    </div>
                    <div class="col-md-8 col-md-pull-4">
                        <div class="form-group is-required">
                            <label class="control-label col-md-4" for="active">
                                <spring:message code='settings.mail.active'/>:
                            </label>
                            <div class="col-md-8 radio">
                                <label class="halves">
                                    <form:radiobutton id="active" path="active" value="true"/>
                                    <spring:message code="settings.mail.active.true"/>
                                </label>
                                <label class="halves">
                                    <form:radiobutton id="active" path="active"
                                                      value="false"/>
                                    <spring:message code="settings.mail.active.false"/>
                                </label>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="control-label col-md-4" for="from">
                                <spring:message code='settings.mail.from'/>:
                            </label>
                            <div class="col-md-8">
                                <form:input id="from" path="from" class="form-control"
                                            cssErrorClass="form-control error"/>
                                <span class="help-inline">
                                    <form:errors path="from" cssClass="error"/>
                                </span>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="control-label col-md-4" for="administrator">
                                <spring:message code='settings.mail.administrator'/>:
                            </label>
                            <div class="col-md-8">
                                <form:input id="administrator" path="administrator"
                                            class="form-control" cssErrorClass="form-control error"/>
                                <span class="help-inline">
                                    <form:errors path="administrator" cssClass="error"/>
                                </span>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="control-label col-md-4" for="baseLinkURL">
                                <spring:message code='settings.mail.baseURL'/>:
                            </label>
                            <div class="col-md-8">
                                <form:input id="baseLinkURL" path="baseLinkURL"
                                            placeholder="http://urlaubsverwaltung.mydomain.com/" class="form-control"
                                            cssErrorClass="form-control error"/>
                                <span class="help-inline">
                                    <form:errors path="baseLinkURL" cssClass="error"/>
                                </span>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="control-label col-md-4" for="host">
                                <spring:message code='settings.mail.host'/>:
                            </label>
                            <div class="col-md-8">
                                <form:input id="host" path="host" class="form-control"
                                            cssErrorClass="form-control error"/>
                                <span class="help-inline">
                                    <form:errors path="host" cssClass="error"/>
                                </span>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="control-label col-md-4" for="port">
                                <spring:message code='settings.mail.port'/>:
                            </label>
                            <div class="col-md-8">
                                <form:input id="port" path="port" class="form-control"
                                            cssErrorClass="form-control error"/>
                                <span class="help-inline">
                                    <form:errors path="port" cssClass="error"/>
                                </span>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="control-label col-md-4" for="username">
                                <spring:message code='settings.mail.username'/>:
                            </label>
                            <div class="col-md-8">
                                <form:input id="username" path="username" class="form-control"
                                            cssErrorClass="form-control error"/>
                                <span class="help-inline">
                                    <form:errors path="username" cssClass="error"/>
                                </span>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="control-label col-md-4" for="password">
                                <spring:message code='settings.mail.password'/>:
                            </label>
                            <div class="col-md-8">
                                <form:password showPassword="true" id="password"
                                               path="password" class="form-control"
                                               cssErrorClass="form-control error"/>
                                <span class="help-inline">
                                    <form:errors path="password" cssClass="error"/>
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
