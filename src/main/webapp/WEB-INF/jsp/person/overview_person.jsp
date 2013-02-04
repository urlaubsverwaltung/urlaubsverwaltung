<div id="content">

    <div class="container_12">

        <div class="grid_12"> 

            <c:choose>
                <c:when test="${!empty param.year}">
                    <c:set var="displayYear" value="${param.year}" />
                </c:when>
                <c:otherwise>
                    <c:set var="displayYear" value="${year}" />
                </c:otherwise>
            </c:choose>

            <c:choose>
                <c:when test="${person.id == loggedUser.id}">
                    <%@include file="./include/overview_header_user.jsp" %>
                </c:when>
                <c:otherwise>
                    <sec:authorize access="hasRole('role.office')">
                        <%@include file="./include/overview_header_office.jsp" %>
                    </sec:authorize>
                </c:otherwise>
            </c:choose>

        </div>

        <div class="grid_2">
            <table class="app-detail" cellspacing="0" style="height: 13em; text-align: center">
                <tr class="odd">
                    <td rowspan="6" style="text-align: center">
                        <img class="user-pic" src="<c:out value='${gravatar}?d=mm&s=110'/>" /> 
                    </td>
                </tr>
            </table>
        </div>

        <div class="grid_6">
            <table class="app-detail" cellspacing="0">
                <%@include file="../application/include/account_days.jsp" %>
            </table>
        </div>

        <div class="grid_4">
            <table class="app-detail" cellspacing="0">
                <%@include file="./include/used_days.jsp" %>
            </table>
        </div>

        <div class="grid_12">&nbsp;</div>
        <div class="grid_12">&nbsp;</div>

        <div class="grid_12">

            <sec:authorize access="hasRole('role.office')">
                <c:choose>
                    <c:when test="${person.id == loggedUser.id}">
                        <a class="btn btn-primary" style="margin-top: 1em;" href="${formUrlPrefix}/application/new">
                            <spring:message code="ov.apply" />
                        </a>
                    </c:when>
                    <c:otherwise>
                        <a class="btn btn-primary" style="margin-top: 1em;" href="${formUrlPrefix}/${person.id}/application/new">
                            <c:set var="staff" value="${person.firstName} ${person.lastName}" />
                            <spring:message code="ov.apply.for.user" arguments="${staff}"/>
                        </a>
                    </c:otherwise>
                </c:choose>
            </sec:authorize>

            <sec:authorize access="hasAnyRole('role.user', 'role.boss')">
                <a class="btn btn-primary" href="${formUrlPrefix}/application/new">
                    <spring:message code="ov.apply" />
                </a>
            </sec:authorize>

        </div>
        <div class="grid_12">&nbsp;</div>


        <%@include  file="./include/overview_app_list.jsp" %>


    </div>
</div>


