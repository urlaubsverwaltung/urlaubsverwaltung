
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>


<!DOCTYPE html>
<html>

    <head>
        <uv:head />
        <script type="text/javascript">
            $(document).ready(function() {

                $(".sortable-tbl").tablesorter({
                    sortList: [[2,0]]
                });

            });
        </script>
    </head>

    <body>

        <spring:url var="formUrlPrefix" value="/web" />

        <uv:menu />

        <div class="content">
            <div class="container_12">

                <div class="grid_12">

                    <div class="overview-header">

                        <legend>
                            <p>
                                <spring:message code="role.management" />
                            </p> 
                        </legend>

                    </div>
                    
                    <c:choose>

                        <c:when test="${notexistent == true}">

                            <spring:message code="table.empty" />

                        </c:when>

                        <c:otherwise>
                            <table class="data-table sortable-tbl tablesorter zebra-table" cellspacing="0">
                                <thead>
                                <tr>
                                    <th colspan="2"><spring:message code="login" /></th>
                                    <th><spring:message code="firstname" /></th>
                                    <th><spring:message code="name" /></th>
                                    <th><spring:message code="email" /></th>
                                    <th><spring:message code="role" /></th>
                                    <th><spring:message code="user.state" /></th>
                                    <th><spring:message code="edit" /></th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach items="${persons}" var="person" varStatus="loopStatus">
                                    <tr>
                                        <td class="is-centered"><img src="<c:out value='${gravatarUrls[person]}?s=20&d=mm'/>" /></td>
                                        <td><c:out value="${person.loginName}"/></td>
                                        <td><c:out value="${person.firstName}"/></td>
                                        <td><c:out value="${person.lastName}"/></td>
                                        <td><a href="mailto:${person.email}"><c:out value="${person.email}"/></a></td>
                                        <td>
                                            <c:forEach items="${person.permissions}" var="permission" varStatus="status">
                                                <spring:message code="${permission.propertyKey}"/><c:if test="${!status.last}">, </c:if>
                                            </c:forEach>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${person.active == true}">
                                                    <spring:message code="user.activated" />
                                                </c:when>
                                                <c:otherwise>
                                                    <spring:message code="user.deactivated" />
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="is-centered"><a href="${formUrlPrefix}/management/${person.id}"><img src="<spring:url value='/images/edit.png' />" /></a></td>
                                    </tr>    
                                </c:forEach>
                                </tbody>
                            </table>
                        </c:otherwise>

                    </c:choose>

                </div>
            </div> 
        </div>        

    </body>

</html>
