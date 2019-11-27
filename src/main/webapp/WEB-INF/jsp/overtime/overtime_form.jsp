<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="asset" uri = "/WEB-INF/asset.tld"%>

<!DOCTYPE html>
<html lang="${language}">
<head>
    <uv:custom-head/>
    <script>
        window.uv = {};
        window.uv.personId = '<c:out value="${person.id}" />';
        window.uv.apiPrefix = "<spring:url value='/api' />";
    </script>
    <link rel="stylesheet" type="text/css" href="<asset:url value='npm.jquery-ui-themes.css' />" />
    <link rel="stylesheet" type="text/css" href="<asset:url value='app_form~overtime_form~sick_note_form.css' />" />
    <link rel="stylesheet" type="text/css" href="<asset:url value='app_form~overtime_form~person_overview~sick_note_form.css' />" />
    <script defer src="<asset:url value='npm.date-fns.js' />"></script>
    <script defer src="<asset:url value='date-fns-localized.js' />"></script>
    <script defer src="<asset:url value='npm.jquery-ui.js' />"></script>
    <script defer src="<asset:url value='npm.jquery-ui-themes.js' />"></script>
    <script defer src="<asset:url value='app_form~overtime_form~sick_note_form.js' />"></script>
    <script defer src="<asset:url value='app_form~overtime_form~person_overview~sick_note_form.js' />"></script>
    <script defer src="<asset:url value='overtime_form.js' />"></script>
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
                <c:when test="${overtime.id == null}">
                    <c:set var="ACTION" value="${URL_PREFIX}/overtime"/>
                </c:when>
                <c:otherwise>
                    <c:set var="ACTION" value="${URL_PREFIX}/overtime/${overtime.id}"/>
                </c:otherwise>
            </c:choose>

            <form:form method="POST" action="${ACTION}" modelAttribute="overtime" cssClass="form-horizontal">
                <form:hidden path="id" value="${overtime.id}"/>
                <form:hidden path="person" value="${overtime.person.id}"/>
                <div class="form-section">
                    <div class="col-xs-12">
                        <c:set var="formErrors"><form:errors/></c:set>
                        <c:if test="${not empty formErrors}">
                            <div class="alert alert-danger">
                                <form:errors/>
                            </div>
                        </c:if>
                    </div>
                    <div class="col-xs-12">
                        <legend>
                            <c:choose>
                                <c:when test="${overtime.id == null}">
                                    <spring:message code="overtime.record.new"/>
                                </c:when>
                                <c:otherwise>
                                    <spring:message code="overtime.record.edit"/>
                                </c:otherwise>
                            </c:choose>
                        </legend>
                    </div>
                    <div class="col-md-4 col-md-push-8">
                        <span class="help-block">
                            <i class="fa fa-fw fa-info-circle" aria-hidden="true"></i>
                            <spring:message code="overtime.data.description"/>
                        </span>
                    </div>
                    <div class="col-md-8 col-md-pull-4">
                        <c:if test="${signedInUser.id != overtime.person.id}">
                            <div class="form-group">
                                <label class="control-label col-md-3">
                                    <spring:message code="overtime.data.person"/>:
                                </label>
                                <div class="col-md-9">
                                    <p class="form-control-static"><c:out value="${overtime.person.niceName}"/></p>
                                </div>
                            </div><%-- End of person form group --%>
                        </c:if>
                        <div class="form-group is-required">
                            <label class="control-label col-md-3" for="startDate">
                                <spring:message code="overtime.data.startDate"/>:
                            </label>
                            <div class="col-md-9">
                                <form:input path="startDate" cssClass="form-control" cssErrorClass="form-control error"
                                            autocomplete="off" placeholder="${DATE_PATTERN}"/>
                                <span class="help-inline"><form:errors path="startDate" cssClass="error"/></span>
                            </div>
                        </div>
                            <%-- End of start date form group --%>
                        <div class="form-group is-required">
                            <label class="control-label col-md-3" for="endDate">
                                <spring:message code="overtime.data.endDate"/>:
                            </label>
                            <div class="col-md-9">
                                <form:input path="endDate" cssClass="form-control" cssErrorClass="form-control error"
                                            autocomplete="off" placeholder="${DATE_PATTERN}"/>
                                <span class="help-inline"><form:errors path="endDate" cssClass="error"/></span>
                            </div>
                        </div>
                            <%-- End of end date form group --%>
                        <div class="form-group is-required">
                            <label class="control-label col-md-3" for="numberOfHours">
                                <spring:message code="overtime.data.numberOfHours"/>:
                            </label>
                            <div class="col-md-9">
                                <uv:input-number id="numberOfHours" path="numberOfHours" cssClass="form-control"
                                                 cssErrorClass="form-control error" step="0.25"
                                                 value="${overtime.numberOfHours}"/>
                                <span class="help-inline"><form:errors path="numberOfHours" cssClass="error"/></span>
                            </div>
                        </div>
                            <%-- End of number of overtime form group --%>
                        <div class="form-group">
                            <label class="control-label col-md-3" for="comment">
                                <spring:message code="overtime.data.comment"/>:
                            </label>
                            <div class="col-md-9">
                                <span id="char-counter"></span><spring:message code="action.comment.maxChars"/>
                                <form:textarea path="comment" cssClass="form-control" rows="2"
                                               onkeyup="count(this.value, 'char-counter');"
                                               onkeydown="maxChars(this,200); count(this.value, 'char-counter');"/>
                                <span class="help-inline"><form:errors path="comment" cssClass="error"/></span>
                            </div>
                        </div>
                            <%-- End of comment form group --%>
                    </div>
                </div><%-- End of first form section --%>
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
                </div><%-- End of second form section --%>
            </form:form>
        </div>
        <%-- End of row --%>
    </div>
    <%-- End of container --%>
</div>
<%-- End of content --%>

</body>
</html>
