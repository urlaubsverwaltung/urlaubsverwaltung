<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>
<%@taglib prefix="asset" uri="/WEB-INF/asset.tld" %>

<!DOCTYPE html>
<html lang="${language}">

<head>
    <title>
        <c:choose>
            <c:when test="${department.id == null}">
                <spring:message code="department.data.header.title.new"/>
            </c:when>
            <c:otherwise>
                <spring:message code="department.data.header.title.edit"/>
            </c:otherwise>
        </c:choose>
    </title>
    <uv:custom-head/>
    <script defer src="<asset:url value='department_form.js' />"></script>
</head>

<spring:url var="URL_PREFIX" value="/web"/>

<body>
<uv:menu/>
<div class="content">
    <div class="container">

        <c:choose>
            <c:when test="${department.id == null}">
                <c:set var="ACTION" value="${URL_PREFIX}/department"/>
            </c:when>
            <c:otherwise>
                <c:set var="ACTION" value="${URL_PREFIX}/department/${department.id}"/>
            </c:otherwise>
        </c:choose>

        <form:form method="POST" action="${ACTION}" modelAttribute="department" class="form-horizontal">
            <form:hidden path="id"/>

            <div class="form-section">
                <uv:section-heading>
                    <h1>
                        <spring:message code="department.data"/>
                    </h1>
                </uv:section-heading>

                <div class="row">
                    <div class="col-md-4 col-md-push-8">
                        <span class="help-block tw-text-sm">
                            <icon:information-circle className="tw-w-4 tw-h-4" solid="true" />
                            <spring:message code="department.data.description"/>
                        </span>
                    </div>
                    <div class="col-md-8 col-md-pull-4">
                        <div class="form-group is-required">
                            <label class="control-label col-md-3" for="name">
                                <spring:message code='department.data.name'/>:
                            </label>
                            <div class="col-md-9">
                                <form:input id="name" path="name" class="form-control" cssErrorClass="form-control error"/>
                                <uv:error-text>
                                    <form:errors path="name" />
                                </uv:error-text>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="control-label col-md-3" for="description">
                                <spring:message code='department.data.info'/>:
                            </label>
                            <div class="col-md-9">
                                <small>
                                    <span id="text-description"></span><spring:message code='action.comment.maxChars'/>
                                </small>
                                <form:textarea id="description" rows="3" path="description" class="form-control"
                                               cssErrorClass="form-control error"
                                               onkeyup="count(this.value, 'text-description');"
                                               onkeydown="maxChars(this,200); count(this.value, 'text-description');"/>
                                <uv:error-text>
                                    <form:errors path="description" />
                                </uv:error-text>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="row tw-mb-8">
                    <div class="col-md-4 col-md-push-8">
                        <span class="help-block tw-text-sm">
                            <icon:information-circle className="tw-w-4 tw-h-4" solid="true" />
                            <spring:message code="department.data.twoStageApproval.help.members"/><br/>
                            <icon:information-circle className="tw-w-4 tw-h-4" solid="true" />
                            <spring:message code="department.data.twoStageApproval.help.departmentHead"/>
                        </span>
                    </div>
                    <div class="col-md-8 col-md-pull-4">
                        <div class="form-group">
                            <label class="control-label col-md-3" for="twoStageApproval">
                                <spring:message code="department.data.twoStageApproval"/>:
                            </label>
                            <div class="col-md-9 checkbox">
                                <uv:error-text>
                                    <form:errors path="twoStageApproval" />
                                </uv:error-text>
                                <label>
                                    <form:checkbox id="twoStageApproval" path="twoStageApproval" cssErrorClass="error"/>
                                    <spring:message code="department.data.twoStageApproval.activate"/>
                                </label>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="form-section">
                <uv:section-heading>
                    <h2>
                        <spring:message code="department.members"/>
                    </h2>
                </uv:section-heading>

                <c:set var="departmentHeadsError">
                    <form:errors path="departmentHeads" />
                </c:set>
                <c:set var="secondStageAuthoritiesError">
                    <form:errors path="secondStageAuthorities" />
                </c:set>

                <div class="row">
                    <c:if test="${not empty departmentHeadsError}">
                        <div class="col-xs-12">
                            <div class="alert alert-danger tw-text-red-800">${departmentHeadsError}</div>
                        </div>
                    </c:if>
                    <c:if test="${not empty secondStageAuthoritiesError}">
                        <div class="col-xs-12">
                            <div class="alert alert-danger tw-text-red-800">${secondStageAuthoritiesError}</div>
                        </div>
                    </c:if>
                </div>

                <div class="row">
                    <div class="col-md-4 col-md-push-8">
                        <span class="help-block tw-text-sm">
                            <icon:information-circle className="tw-w-4 tw-h-4" solid="true" />
                            <spring:message code="department.members.description"/>
                        </span>
                        <span class="help-block tw-text-sm">
                            <icon:information-circle className="tw-w-4 tw-h-4" solid="true" />
                            <spring:message code="department.members.secondStageAuthority.description"/>
                        </span>
                    </div>

                    <div class="col-md-8 col-md-pull-4">
                        <div class="form-group">
                            <label class="control-label col-md-3" for="members">
                                <spring:message code='department.members.person'/>:
                            </label>
                            <div class="col-md-9">
                                <div class="department--members">
                                    <c:forEach items="${persons}" var="person">
                                        <c:set var="IS_INACTIVE" value="${fn:contains(person.permissions, 'INACTIVE')}"/>
                                        <c:set var="IS_MEMBER" value="${fn:contains(department.members, person)}"/>
                                        <c:set var="MEMBER_CSS_CLASS" value="${IS_MEMBER ? ( IS_INACTIVE ? 'is-inactive' : 'is-assigned') : ''}"/>

                                        <div class="department--member ${MEMBER_CSS_CLASS}">
                                            <div class="department--member-image">
                                                <uv:avatar
                                                    url="${person.gravatarURL}?d=mm&s=40"
                                                    username="${person.niceName}"
                                                    width="40px"
                                                    height="40px"
                                                />
                                            </div>
                                            <div class="department--member-assignment">
                                                <p class="department--member-info tw-mb-2">
                                                    <c:out value="${person.niceName}"/>
                                                </p>
                                                <c:choose>
                                                    <c:when test="${IS_INACTIVE}">
                                                    <div class="tw-flex tw-items-center">
                                                        <icon:information-circle className="tw-text-amber-300 tw-w-5 tw-h-5 tw-m-0 tw-mr-1" solid="true"/>
                                                        <spring:message code="department.members.assigned.inactive"/>
                                                    </div>
                                                    </c:when>
                                                </c:choose>
                                                <div>
                                                    <label class="tw-font-normal tw-m-0 tw-flex tw-items-center">
                                                        <form:checkbox path="members" value="${person}" cssClass="tw-m-0 tw-mr-2" />
                                                        <spring:message code="department.members.assigned"/>
                                                    </label>
                                                </div>
                                                <c:if test="${fn:contains(person.permissions, 'DEPARTMENT_HEAD')}">
                                                    <div>
                                                        <label class="tw-font-normal tw-m-0 tw-flex tw-items-center">
                                                            <form:checkbox path="departmentHeads" value="${person}" cssClass="tw-m-0 tw-mr-2" />
                                                            <spring:message code="department.members.departmentHead"/>
                                                        </label>
                                                    </div>
                                                </c:if>
                                                <c:if test="${fn:contains(person.permissions, 'SECOND_STAGE_AUTHORITY')}">
                                                    <div>
                                                        <label class="tw-font-normal tw-m-0 tw-flex tw-items-center">
                                                            <form:checkbox path="secondStageAuthorities" value="${person}" cssClass="tw-m-0 tw-mr-2" />
                                                            <spring:message code="department.members.secondStageAuthority"/>
                                                        </label>
                                                    </div>
                                                </c:if>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="form-section">
                <div class="row tw-mb-16">
                    <div class="col-xs-12">
                        <hr />
                        <button class="button-main-green dark:button-main col-xs-12 col-sm-5 col-md-2" type="submit">
                            <spring:message code="action.save"/>
                        </button>
                        <a class="button col-xs-12 col-sm-5 col-md-2 pull-right" href="${URL_PREFIX}/department">
                            <spring:message code="action.cancel"/>
                        </a>
                    </div>
                </div>
            </div>
        </form:form>
    </div>
</div>

</body>
</html>
