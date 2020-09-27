<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="asset" uri = "/WEB-INF/asset.tld"%>

<!DOCTYPE html>
<html lang="${language}">
<head>
    <c:choose>
        <c:when test="${sickNote.id == null}">
            <spring:message code="sicknote.create.header.title"/>
        </c:when>
        <c:otherwise>
            <spring:message code="sicknote.edit.header.title"/>
        </c:otherwise>
    </c:choose>
    <uv:custom-head/>
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
    <link rel="stylesheet" type="text/css" href="<asset:url value='app_form~overtime_form~sick_note_form.css' />" />
    <link rel="stylesheet" type="text/css" href="<asset:url value='app_form~overtime_form~person_overview~sick_note_form.css' />" />
    <link rel="stylesheet" type="text/css" href="<asset:url value='npm.jquery-ui-themes.css' />" />
    <script defer src="<asset:url value='npm.date-fns.js' />"></script>
    <script defer src="<asset:url value='date-fns-localized.js' />"></script>
    <script defer src="<asset:url value='npm.jquery-ui.js' />"></script>
    <script defer src="<asset:url value='npm.jquery-ui-themes.js' />"></script>
    <script defer src="<asset:url value='app_form~overtime_form~sick_note_form.js' />"></script>
    <script defer src="<asset:url value='app_form~overtime_form~person_overview~sick_note_form.js' />"></script>
    <script defer src="<asset:url value='sick_note_form.js' />"></script>
</head>
<body>

<spring:url var="URL_PREFIX" value="/web"/>

<uv:menu/>

<c:set var="DATE_PATTERN">
    <spring:message code="pattern.date"/>
</c:set>

<div class="content">
    <div class="container">

        <c:choose>
            <c:when test="${sickNote.id == null}">
                <c:set var="ACTION" value="${URL_PREFIX}/sicknote"/>
            </c:when>
            <c:otherwise>
                <c:set var="ACTION" value="${URL_PREFIX}/sicknote/${sickNote.id}/edit"/>
            </c:otherwise>
        </c:choose>

        <form:form method="POST" action="${ACTION}" modelAttribute="sickNote" class="form-horizontal">

            <c:if test="${not empty errors}">
                <div class="row">
                    <div class="col-xs-12 alert alert-danger">
                        <form:errors/>
                    </div>
                </div>
            </c:if>

            <div class="form-section tw-mb-4 lg:tw-mb-6">
                <div class="row">
                    <uv:section-heading>
                        <h1>
                            <c:choose>
                                <c:when test="${sickNote.id == null}">
                                    <spring:message code="sicknote.create.title"/>
                                </c:when>
                                <c:otherwise>
                                    <spring:message code="sicknote.edit.title"/>
                                </c:otherwise>
                            </c:choose>
                        </h1>
                    </uv:section-heading>

                    <div class="col-md-4 col-md-push-8">
                        <span class="help-block help-block tw-text-sm">
                            <uv:icon-information-circle className="tw-w-4 tw-h-4" solid="true" />
                            <spring:message code="sicknote.data.description"/>
                        </span>
                    </div>

                    <div class="col-md-8 col-md-pull-4">
                        <div class="form-group is-required">
                            <label class="control-label col-md-3" for="employee">
                                <spring:message code='sicknote.data.person'/>:
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
            </div>

            <div class="form-section tw-mb-4 lg:tw-mb-6">
                <div class="row">
                    <uv:section-heading>
                        <h2>
                            <spring:message code="sicknote.data.aub.short"/>
                        </h2>
                    </uv:section-heading>
                    <div class="col-md-4 col-md-push-8">
                        <span class="help-block help-block tw-text-sm">
                            <uv:icon-information-circle className="tw-w-4 tw-h-4" solid="true" />
                            <spring:message code="sicknote.data.person"/>
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
            </div>

            <div class="form-section tw-mb-16">
                <div class="row">
                    <uv:section-heading>
                        <h2>
                            <spring:message code="sicknote.data.furtherInformation.title"/>
                        </h2>
                    </uv:section-heading>
                    <div class="col-md-4 col-md-push-8">
                        <span class="help-block help-block tw-text-sm">
                            <uv:icon-information-circle className="tw-w-4 tw-h-4" solid="true" />
                            <spring:message code="sicknote.data.furtherInformation.description"/>
                        </span>
                    </div>
                    <div class="col-md-8 col-md-pull-4">
                        <div class="form-group">
                            <label class="control-label col-md-3" for="comment">
                                <spring:message code="sicknote.data.furtherInformation.comment"/>:
                            </label>
                            <div class="col-md-9">
                                <small>
                                    <span id="text-comment"></span><spring:message code="action.comment.maxChars"/>
                                </small>
                                <form:textarea id="comment" rows="1" path="comment" class="form-control"
                                               cssErrorClass="form-control error"
                                               onkeyup="count(this.value, 'text-comment');"
                                               onkeydown="maxChars(this,200); count(this.value, 'text-comment');"/>
                                <form:errors path="comment" cssClass="error"/>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="form-section">
                <div class="col-xs-12">
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

</body>
</html>
