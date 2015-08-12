<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<spring:url var="URL_PREFIX" value="/web" />

<div class="print-info--only-landscape">
    <h4><spring:message code="print.info.landscape" /></h4>
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
    <table cellspacing="0" class="list-table sortable tablesorter print--only-landscape">
        <thead class="hidden-xs hidden-sm">
        <tr>
            <th class="sortable-field"><spring:message code="department.data.name" /></th>
            <th class="sortable-field"><spring:message code="department.data.countMembers" /></th>
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
                    <div class="overflow"
                         data-toggle="popover"
                         data-trigger="hover"
                         data-placement="right"
                         title="<spring:message code='department.data.description'/>"
                         data-content="${department.description}">
                      <c:out value="${department.name}"/>
                    </div>
                </td>
                <td>
                    <c:out value="${fn:length(department.members)}"/>
                </td>
                <td><uv:dateTime dateTime="${department.lastModification}"/></td>
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