<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>


<!DOCTYPE html>
<html>

    <head>
        <uv:head />

        <spring:url var="URL_PREFIX" value="/web" />

    </head>

    <body>

        <uv:menu />

        <div class="print-info--only-landscape">
            <h4><spring:message code="print.info.landscape" /></h4>
        </div>

        <div class="content print--only-landscape">
            <div class="container">

                <div class="row">

                    <div class="col-xs-12">

                    <legend>
                        <spring:message code="departments.title" />
                        <uv:print />
                        <sec:authorize access="hasRole('OFFICE')">
                            <a href="${URL_PREFIX}/department/new" class="fa-action pull-right"
                            data-title="<spring:message code="action.department.create"/>">
                                <i class="fa fa-fw fa-plus-circle"></i>
                            </a>
                        </sec:authorize>
                    </legend>

                    <div class="feedback">
                        <c:choose>
                            <c:when test="${not empty createdDepartment}">
                                <div class="alert alert-success">
                                    <spring:message code="department.action.create.success" arguments="${createdDepartment.name}" />
                                </div>
                            </c:when>
                            <c:when test="${not empty updatedDepartment}">
                                <div class="alert alert-success">
                                    <spring:message code="department.action.edit.success" arguments="${updatedDepartment.name}" />
                                </div>
                            </c:when>
                            <c:when test="${not empty deletedDepartment}">
                                <div class="alert alert-success">
                                    <spring:message code="department.action.delete.success" arguments="${deletedDepartment.name}" />
                                </div>
                            </c:when>
                        </c:choose>
                    </div>

                    <script type="text/javascript">
                        $(document).ready(function() {

                            $("table.sortable").tablesorter({
                                sortList: [[0,0]]
                            });

                        });
                    </script>


                    <c:choose>
                        <c:when test="${empty departments}">
                            <spring:message code="departments.none"/>
                        </c:when>
                        <c:otherwise>
                            <table cellspacing="0" class="list-table sortable tablesorter">
                                <thead class="hidden-xs hidden-sm">
                                <tr>
                                    <th class="sortable-field"><spring:message code="department.data.name" /></th>
                                    <th class="sortable-field"><spring:message code="department.members" /></th>
                                    <th class="sortable-field"><spring:message code='department.data.lastModification' /></th>
                                    <sec:authorize access="hasRole('OFFICE')">
                                        <th><%-- placeholder to ensure correct number of th --%></th>
                                    </sec:authorize>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach items="${departments}" var="department" varStatus="loopStatus">
                                    <tr>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty department.description}" >
                                                    <div class="overflow"
                                                         data-toggle="popover"
                                                         data-trigger="hover"
                                                         data-placement="right"
                                                         title="<spring:message code='department.data.info'/>"
                                                         data-content="${department.description}">
                                                        <c:out value="${department.name}"/>
                                                        <i class="fa fa-fw fa-info-circle hidden-print"></i>
                                                    </div>
                                                </c:when>
                                                <c:otherwise>
                                                    <c:out value="${department.name}"/>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="hidden-xs">
                                            <c:out value="${fn:length(department.members)}"/> <spring:message code="department.members"/>
                                        </td>
                                        <td class="hidden-xs">
                                            <uv:dateTime dateTime="${department.lastModification}"/>
                                        </td>
                                        <sec:authorize access="hasRole('OFFICE')">
                                            <td>
                                                <form:form method="DELETE" action="${URL_PREFIX}/department/${department.id}">
                                                    <div id="modal-cancel-${department.id}" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
                                                        <div class="modal-dialog">
                                                            <div class="modal-content">
                                                                <div class="modal-header">
                                                                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true"><i class="fa fa-remove"></i></button>
                                                                    <h4 id="myModalLabel" class="modal-title"><spring:message code="action.department.delete" />?</h4>
                                                                </div>
                                                                <div class="modal-body">
                                                                    <spring:message code="action.department.delete.confirm" arguments="${department.name}" />
                                                                </div>
                                                                <div class="modal-footer">
                                                                    <button class="btn btn-danger is-sticky" type="submit"><spring:message code="action.department.delete" /></button>
                                                                    <button class="btn btn-default is-sticky" data-dismiss="modal" aria-hidden="true"><spring:message code="action.cancel" /></button>
                                                                </div>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </form:form>

                                                <a class="fa-action negative pull-right hidden-xs"
                                                   href="#modal-cancel-${department.id}"
                                                   data-toggle="modal"
                                                   data-title="<spring:message code='action.department.delete' />">
                                                    <i class="fa fa-fw fa-trash"></i>
                                                </a>

                                                <a class="fa-action pull-right" href="${URL_PREFIX}/department/${department.id}/edit"
                                                   data-title="<spring:message code="action.edit" />">
                                                    <i class="fa fa-fw fa-pencil"></i>
                                                </a>
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
