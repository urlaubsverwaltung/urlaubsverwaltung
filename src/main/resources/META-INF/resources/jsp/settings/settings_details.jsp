<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<!DOCTYPE html>
<html>

<spring:url var="URL_PREFIX" value="/web"/>

<head>
    <uv:head/>
</head>

<body>

<uv:menu/>

<div class="content">
    <div class="container">
        <div class="feedback">
            <c:if test="${updateSuccess}">
                <div class="alert alert-success">
                    <spring:message code="settings.action.update.success"/>
                </div>
            </c:if>
        </div>

        <div class="row">
            <div class="col-xs-12">
                <%-- TODO: Add new message property --%>
                <legend><spring:message code="nav.settings.title"/></legend>
            </div>
            <div class="col-xs-12 col-sm-12 col-md-6">
                <div class="box">
                    <span class="box-icon bg-blue"><i class="fa fa-calendar-times-o"></i></span>
                    <span class="box-text">
                        <a href="${URL_PREFIX}/settings/absence">
                            <spring:message code="settings.tabs.absence"/>
                        </a>
                    </span>
                </div>

                <div class="box">
                    <span class="box-icon bg-blue"><i class="fa fa-clock-o"></i></span>
                    <span class="box-text">
                        <a href="${URL_PREFIX}/settings/workingtime">
                            <spring:message code="settings.tabs.workingTime"/>
                        </a>
                    </span>
                </div>
            </div>
            <div class="col-xs-12 col-sm-12 col-md-6">
                <div class="box">
                    <span class="box-icon bg-blue"><i class="fa fa-envelope-o"></i></span>
                    <span class="box-text">
                        <a href="${URL_PREFIX}/settings/mail">
                            <spring:message code="settings.tabs.mail"/>
                        </a>
                    </span>
                </div>

                <div class="box">
                    <span class="box-icon bg-blue"><i class="fa fa-refresh"></i></span>
                    <span class="box-text">
                        <a href="${URL_PREFIX}/settings/calendar">
                            <spring:message code="settings.tabs.calendar"/>
                        </a>
                    </span>
                </div>
            </div>
        </div>
    </div>
</div>

</body>

</html>
