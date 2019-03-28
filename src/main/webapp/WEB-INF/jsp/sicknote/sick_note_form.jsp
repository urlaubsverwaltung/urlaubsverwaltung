<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<!DOCTYPE html>
<html>
<head>
    <uv:head/>
    <script>
        window.uv = {};
        window.uv.personId = '<c:out value="${person.id}" />';
        window.uv.webPrefix = "<spring:url value='/web' />";
        window.uv.apiPrefix = "<spring:url value='/api' />";
        window.uv.sickNote = {};
        window.uv.sickNote.id = "<c:out value="${sickNote.id}" />";
        window.uv.sickNote.person = {};
        window.uv.sickNote.person.id = "<c:out value="${sickNote.person.id}" />";
        window.uv.params = {};
        window.uv.params.person = "${param.person}";
    </script>
    <link rel="stylesheet" type="text/css" href="<spring:url value='/assets/app_form~overtime_form~sick_note_form.28f2739d827299b4c8a8' />" />
    <link rel="stylesheet" type="text/css" href="<spring:url value='/assets/npm.jquery-ui.14d0a75394fc92805828.css' />" />
    <script defer src="<spring:url value='/assets/npm.jquery-ui.f82489a9e7eb2a4806ba.min.js' />"></script>
    <script defer src="<spring:url value='/assets/npm.date-fns.5a87bdcafa645ad64030.min.js' />"></script>
    <script defer src="<spring:url value='/assets/date-fns-localized.f977abd85880a1f57b92.min' />"></script>
    <script defer src="<spring:url value='/assets/app_form~overtime_form~sick_note_form.521b6dd44d7691f214bf.min.js' />"></script>
    <script defer src="<spring:url value='/assets/sick_note_form.a4fa812224bfb88265b4.min.js' />"></script>
</head>
<body>

<spring:url var="URL_PREFIX" value="/web"/>

<uv:menu/>

<c:set var="DATE_PATTERN">
    <spring:message code="pattern.date"/>
</c:set>

<div class="content">

    <div class="container">

        <div class="row">

            <c:choose>
                <c:when test="${sickNote.id == null}">
                    <c:set var="ACTION" value="${URL_PREFIX}/sicknote"/>
                </c:when>
                <c:otherwise>
                    <c:set var="ACTION" value="${URL_PREFIX}/sicknote/${sickNote.id}/edit"/>
                </c:otherwise>
            </c:choose>

            <form:form method="POST" action="${ACTION}" modelAttribute="sickNote" class="form-horizontal">
                <div class="form-section">

                    <div class="col-xs-12">
                        <legend>
                            <c:choose>
                                <c:when test="${sickNote.id == null}">
                                    <spring:message code="sicknote.create.title"/>
                                </c:when>
                                <c:otherwise>
                                    <spring:message code="sicknote.edit.title"/>
                                </c:otherwise>
                            </c:choose>
                        </legend>
                    </div>

                    <div class="col-xs-12">
                        <c:set var="formErrors"><form:errors/></c:set>
                        <c:if test="${not empty errors && not empty formErrors}">
                            <div class="alert alert-danger">
                                <form:errors/>
                            </div>
                        </c:if>
                    </div>

                    <div class="col-md-4 col-md-push-8">
                <span class="help-block">
                    <i class="fa fa-fw fa-info-circle" aria-hidden="true"></i>
                    <spring:message code="sicknote.data.description"/>
                </span>
                    </div>

                    <div class="col-md-8 col-md-pull-4">

                        <div class="form-group is-required">
                            <label class="control-label col-md-3" for="employee">
                                <spring:message code='sicknote.data.staff'/>:
                            </label>

                            <div class="col-md-9">
                                <c:choose>
                                    <c:when test="${sickNote.id == null}">
                                        <form:select path="person" id="employee" class="form-control"
                                                     cssErrorClass="form-control error">
                                            <c:forEach items="${persons}" var="person">
                                                <c:choose>
                                                    <c:when test="${sickNote.person.id == person.id}">
                                                        <form:option value="${person.id}"
                                                                     selected="selected">${person.niceName}</form:option>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <form:option
                                                            value="${person.id}">${person.niceName}</form:option>
                                                    </c:otherwise>
                                                </c:choose>
                                            </c:forEach>
                                        </form:select>
                                        <span class="help-inline"><form:errors path="person" cssClass="error"/></span>
                                    </c:when>
                                    <c:otherwise>
                                        <form:hidden path="id"/>
                                        <form:hidden path="person" value="${sickNote.person.id}"/>
                                        <c:out value="${sickNote.person.niceName}"/>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>

                        <div class="form-group is-required">
                            <label class="control-label col-md-3" for="sickNoteType">
                                <spring:message code="sicknote.data.type"/>:
                            </label>

                            <div class="col-md-9">
                                <form:select path="sickNoteType" id="sickNoteType" class="form-control"
                                             cssErrorClass="form-control error">
                                    <c:forEach items="${sickNoteTypes}" var="sickNoteType">
                                        <c:choose>
                                            <c:when test="${sickNoteType == sickNote.sickNoteType}">
                                                <form:option value="${sickNoteType.id}" selected="selected">
                                                    <spring:message code="${sickNoteType.messageKey}"/>
                                                </form:option>
                                            </c:when>
                                            <c:otherwise>
                                                <form:option value="${sickNoteType.id}">
                                                    <spring:message code="${sickNoteType.messageKey}"/>
                                                </form:option>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:forEach>
                                </form:select>
                            </div>
                        </div>

                        <div class="form-group is-required">
                            <label class="control-label col-md-3" for="dayLength">
                                <spring:message code="absence.period"/>:
                            </label>
                            <div class="col-md-9">
                                <div class="radio">
                                    <label class="thirds">
                                        <form:radiobutton path="dayLength" value="FULL" checked="checked"/>
                                        <spring:message code="FULL"/>
                                    </label>
                                    <label class="thirds">
                                        <form:radiobutton path="dayLength" value="MORNING"/>
                                        <spring:message code="MORNING"/>
                                    </label>
                                    <label class="thirds">
                                        <form:radiobutton path="dayLength" value="NOON"/>
                                        <spring:message code="NOON"/>
                                    </label>
                                </div>
                                <span class="help-inline"><form:errors path="dayLength" cssClass="error"/></span>
                            </div>
                        </div>

                        <div class="form-group is-required">
                            <label class="control-label col-md-3" for="from">
                                <spring:message code="absence.period.startDate"/>:
                            </label>
                            <div class="col-md-9">
                                <form:input id="from" path="startDate" class="form-control"
                                            cssErrorClass="form-control error" autocomplete="off"
                                            placeholder="${DATE_PATTERN}"/>
                                <span class="help-inline"><form:errors path="startDate" cssClass="error"/></span>
                            </div>
                        </div>

                        <div class="form-group is-required">
                            <label class="control-label col-md-3" for="to">
                                <spring:message code="absence.period.endDate"/>:
                            </label>
                            <div class="col-md-9">
                                <form:input id="to" path="endDate" class="form-control"
                                            cssErrorClass="form-control error" autocomplete="off"
                                            placeholder="${DATE_PATTERN}"/>
                                <span class="help-inline"><form:errors path="endDate" cssClass="error"/></span>
                            </div>
                        </div>

                    </div>

                </div>

                <div class="form-section">
                    <div class="col-xs-12">
                        <legend>
                            <spring:message code="sicknote.data.aub.short"/>
                        </legend>
                    </div>
                    <div class="col-md-4 col-md-push-8">
                <span class="help-block">
                    <i class="fa fa-fw fa-info-circle" aria-hidden="true"></i>
                    <spring:message code="sicknote.data.aub.description"/>
                </span>
                    </div>
                    <div class="col-md-8 col-md-pull-4">
                        <div class="form-group AU">
                            <label class="control-label col-md-3" for="aubFrom">
                                <spring:message code="absence.period.startDate"/>:
                            </label>

                            <div class="col-md-9">
                                <form:input id="aubFrom" path="aubStartDate" class="form-control"
                                            cssErrorClass="form-control error" autocomplete="off"
                                            placeholder="${DATE_PATTERN}"/>
                                <span class="help-inline"><form:errors path="aubStartDate" cssClass="error"/></span>
                            </div>
                        </div>
                        <div class="form-group AU">
                            <label class="control-label col-md-3" for="aubTo">
                                <spring:message code="absence.period.endDate"/>
                            </label>

                            <div class="col-md-9">
                                <form:input id="aubTo" path="aubEndDate" class="form-control"
                                            cssErrorClass="form-control error" autocomplete="off"
                                            placeholder="${DATE_PATTERN}"/>
                                <span class="help-inline"><form:errors path="aubEndDate" cssClass="error"/></span>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="form-section">
                    <div class="col-xs-12">
                        <hr/>
                        <button class="btn btn-success col-xs-12 col-sm-5 col-md-2" type="submit">
                            <spring:message code="action.save"/>
                        </button>
                        <button class="btn btn-default back col-xs-12 col-sm-5 col-md-2 pull-right" type="button">
                            <spring:message code="action.cancel"/>
                        </button>
                    </div>
                </div>

            </form:form>

        </div>

    </div>

</div>

</body>
</html>
