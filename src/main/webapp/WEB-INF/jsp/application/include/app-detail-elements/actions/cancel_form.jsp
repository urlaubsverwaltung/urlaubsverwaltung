<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<sec:authorize access="hasAuthority('OFFICE')">
    <c:set var="IS_OFFICE" value="${true}"/>
</sec:authorize>

<c:if test="${action == 'cancel'}">
<script type="text/javascript">
    document.addEventListener('DOMContentLoaded', function() {
        $("#cancel").show();
    })
</script>
</c:if>

<spring:url var="URL_PREFIX" value="/web"/>

<form:form id="cancel" cssClass="form action-form confirm alert alert-danger" method="POST"
           action="${URL_PREFIX}/application/${application.id}/cancel" modelAttribute="comment">

    <div class="form-group">
        <strong class="tw-font-medium">
            <c:choose>
                <c:when test="${(application.status == 'ALLOWED' || application.status == 'TEMPORARY_ALLOWED') && !IS_OFFICE}">
                    <spring:message code='action.delete.request.confirm'/>
                </c:when>
                <c:otherwise>
                    <spring:message code='action.delete.confirm'/>
                </c:otherwise>
            </c:choose>
        </strong>
    </div>

    <div class="form-group">
        <div class="tw-text-sm">
            <c:choose>
                <%-- comment is obligat if it's not the own application or if the application is in status allowed --%>
                <c:when
                    test="${application.person.id != signedInUser.id || application.status == 'ALLOWED' || application.status == 'TEMPORARY_ALLOWED' || application.status == 'ALLOWED_CANCELLATION_REQUESTED'}">
                    <spring:message code="action.comment.mandatory"/>
                </c:when>
                <%-- otherwise comment is not obligat --%>
                <c:otherwise>
                    <spring:message code="action.comment.optional"/>
                </c:otherwise>
            </c:choose>
            : (<span id="text-cancel"></span><spring:message code="action.comment.maxChars"/>)
        </div>
        <form:textarea rows="2" path="text" cssClass="form-control" cssErrorClass="form-control error"
                       onkeyup="count(this.value, 'text-cancel');"
                       onkeydown="maxChars(this,200); count(this.value, 'text-cancel');"/>
    </div>

    <div class="form-group is-sticky row">
        <button type="submit" class="button-danger col-xs-12 col-sm-5">
            <c:choose>
                <c:when test="${(application.status == 'ALLOWED' || application.status == 'TEMPORARY_ALLOWED') && !IS_OFFICE}">
                    <spring:message code='action.delete.request'/>
                </c:when>
                <c:otherwise>
                    <spring:message code='action.delete'/>
                </c:otherwise>
            </c:choose>
        </button>
        <button type="button" class="button col-xs-12 col-sm-5 pull-right" onclick="$('#cancel').hide();">
            <spring:message code="action.cancel"/>
        </button>
    </div>

</form:form>
