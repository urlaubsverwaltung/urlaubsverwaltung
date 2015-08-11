<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<spring:url var="URL_PREFIX" value="/web" />

<div class="print-info--only-landscape">
    <h4><spring:message code="print.info.landscape" /></h4>
</div>

<p class="pull-right visible-print">
    <spring:message code="Effective"/> <uv:date date="${now}" />
</p>

<script type="text/javascript">
    $(document).ready(function() {

        $("table.sortable").tablesorter({
            sortList: [[0,0]]
        });

    });
</script>


<c:choose>
  <c:when test="${empty departments}">
    <spring:message code="no.departments"/>
  </c:when>
  <c:otherwise>
    <table cellspacing="0" class="list-table selectable-table sortable tablesorter print--only-landscape">
        <thead class="hidden-xs hidden-sm">
        <tr>
            <th class="sortable-field"><spring:message code="department.name" /></th>
            <th class="sortable-field"><spring:message code="department.description" /></th>
            <th class="sortable-field"><spring:message code='department.lastModification' /></th>
            <sec:authorize access="hasRole('OFFICE')">
                <th><%-- placeholder to ensure correct number of th --%></th>
            </sec:authorize>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${departments}" var="department" varStatus="loopStatus">
            <tr onclick="navigate('${URL_PREFIX}/department/${department.id}/overview');">
                <td><c:out value="${department.name}"/></td>
                <td><c:out value="${department.description}"/></td>
                <td><c:out value="${department.lastModification}"/></td>
                <sec:authorize access="hasRole('OFFICE')">
                <td class="hidden-print hidden-xs">
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