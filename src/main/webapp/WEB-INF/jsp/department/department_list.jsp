<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="asset" uri = "/WEB-INF/asset.tld"%>

<!DOCTYPE html>
<html lang="${language}" class="tw-<c:out value='${theme}' />">

<head>
    <title>
        <spring:message code="departments.header.title"/>
    </title>
    <uv:custom-head/>
    <script defer src="<asset:url value='npm.tablesorter.js' />"></script>
    <script defer src="<asset:url value='department_list.js' />"></script>
    <spring:url var="URL_PREFIX" value="/web"/>
</head>

<body>
<uv:menu/>
<div class="content">
    <div class="container">

        <uv:section-heading>
            <jsp:attribute name="actions">
                <sec:authorize access="hasAuthority('OFFICE')">
                <a href="${URL_PREFIX}/department/new" class="icon-link tw-px-1" data-title="<spring:message code="action.department.create"/>">
                    <icon:plus-circle className="tw-w-5 tw-h-5" />
                </a>
                </sec:authorize>
                <a href="${URL_PREFIX}/absences" class="icon-link tw-px-1" data-title="<spring:message code="action.applications.absences_overview"/>">
                    <icon:calendar className="tw-w-5 tw-h-5" />
                </a>
                <uv:print/>
            </jsp:attribute>
            <jsp:body>
                <h1>
                    <spring:message code="departments.title"/>
                </h1>
            </jsp:body>
        </uv:section-heading>

        <div class="row">
            <div class="col-xs-12">
                <div class="feedback">
                    <c:choose>
                        <c:when test="${not empty createdDepartment}">
                            <div class="alert alert-success">
                                <spring:message code="department.action.create.success" arguments="${createdDepartment.name}"/>
                            </div>
                        </c:when>
                        <c:when test="${not empty updatedDepartment}">
                            <div class="alert alert-success">
                                <spring:message code="department.action.edit.success" arguments="${updatedDepartment.name}"/>
                            </div>
                        </c:when>
                        <c:when test="${not empty deletedDepartment}">
                            <div class="alert alert-success">
                                <spring:message code="department.action.delete.success" arguments="${deletedDepartment.name}"/>
                            </div>
                        </c:when>
                    </c:choose>
                </div>

                <c:choose>
                    <c:when test="${empty departments}">
                        <spring:message code="departments.none"/>
                    </c:when>
                    <c:otherwise>
                        <table id="department-table" class="list-table tw-text-sm">
                            <thead class="hidden-xs hidden-sm">
                            <tr>
                                <th scope="col" class="sortable-field"><spring:message code="department.data.name"/></th>
                                <th scope="col" class="sortable-field"><spring:message code="department.members"/></th>
                                <th scope="col" class="sortable-field"><spring:message code='department.data.twoStageApproval'/></th>
                                <th scope="col" class="sortable-field"><spring:message code='department.data.lastModification'/></th>
                                <sec:authorize access="hasAuthority('OFFICE')">
                                    <th scope="col"><%-- placeholder to ensure correct number of th --%></th>
                                </sec:authorize>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach items="${departments}" var="department" varStatus="loopStatus">
                                <tr>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty department.description}">
                                                <div class="overflow"
                                                     data-toggle="popover"
                                                     data-trigger="hover"
                                                     data-placement="right"
                                                     title="<spring:message code='department.data.info'/>"
                                                     data-content="<c:out value="${department.description}"/>">
                                                    <c:out value="${department.name}"/>
                                                    <icon:information-circle className="tw-w-4 tw-h-4" solid="true" />
                                                </div>
                                            </c:when>
                                            <c:otherwise>
                                                <c:out value="${department.name}"/>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="hidden-xs">
                                        <c:if test="${department.activeMembersCount > 0}">
                                            <a href="${URL_PREFIX}/person?active=true&department=${department.id}">
                                                <c:out value="${department.activeMembersCount}"/>
                                                <spring:message code="department.members.active"/>
                                            </a>
                                        </c:if>
                                        <c:if test="${department.activeMembersCount > 0 && department.inactiveMembersCount > 0}">
                                            <c:out value="/"/>
                                        </c:if>
                                        <c:if test="${department.inactiveMembersCount > 0}">
                                            <a href="${URL_PREFIX}/person?active=false&department=${department.id}">
                                                <c:out value="${department.inactiveMembersCount}"/>
                                                <spring:message code="department.members.inactive"/>
                                            </a>
                                        </c:if>
                                    </td>
                                    <td class="is-centered hidden-xs">
                                        <c:if test="${department.twoStageApproval}">
                                            <icon:check className="tw-w-5 tw-h-5" solid="true" />
                                        </c:if>
                                    </td>
                                    <fmt:parseDate value="${department.lastModification}" pattern="yyyy-MM-dd" var="parsedDate" type="date"/>
                                    <td class="hidden-xs" data-date-format="yyyymmdd" data-value="<fmt:formatDate pattern="yyyy-MM-dd" value="${parsedDate}" type="date" />">
                                        <uv:date date="${department.lastModification}"/>
                                    </td>
                                    <sec:authorize access="hasAuthority('OFFICE')">
                                    <td>
                                            <form:form method="POST" action="${URL_PREFIX}/department/${department.id}/delete">
                                                <div id="modal-cancel-${department.id}" class="modal fade" tabindex="-1"
                                                     role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
                                                    <div class="modal-dialog">
                                                        <div class="modal-content">
                                                            <div class="modal-header">
                                                                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">
                                                                    <icon:x-circle className="tw-w-8 tw-h-8" solid="true" />
                                                                </button>
                                                                <h4 id="myModalLabel" class="modal-title">
                                                                    <spring:message code="action.department.delete"/>?
                                                                </h4>
                                                            </div>
                                                            <div class="modal-body">
                                                                <spring:message code="action.department.delete.confirm" arguments="${department.name}"/>
                                                            </div>
                                                            <div class="modal-footer tw-flex tw-justify-end tw-space-x-2">
                                                                <button class="button-danger" type="submit">
                                                                    <spring:message code="action.department.delete"/>
                                                                </button>
                                                                <button class="button" data-dismiss="modal" aria-hidden="true">
                                                                    <spring:message code="action.cancel"/>
                                                                </button>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>
                                            </form:form>

                                            <div class="tw-flex tw-space-x-4 tw-justify-end print:tw-hidden">
                                                <a class="action-link" href="${URL_PREFIX}/department/${department.id}/edit">
                                                    <icon:pencil className="tw-w-4 tw-h-4 tw-mr-1" />
                                                    <spring:message code="action.edit" />
                                                </a>
                                                <a class="action-link" data-toggle="modal" href="#modal-cancel-${department.id}">
                                                    <icon:trash className="tw-w-4 tw-h-4 tw-mr-1" />
                                                    <spring:message code='action.department.delete' />
                                                </a>
                                            </div>

                                        </td>
                                    </sec:authorize>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</div>
</body>
</html>
