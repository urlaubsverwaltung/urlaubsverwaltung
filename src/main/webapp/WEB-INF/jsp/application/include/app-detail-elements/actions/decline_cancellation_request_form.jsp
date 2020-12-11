<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<c:if test="${action == 'decline-cancellation-request'}">
    <script type="text/javascript">
        document.addEventListener('DOMContentLoaded', function() {
            $("#decline-cancellation-request").show();
        })
    </script>
</c:if>

<spring:url var="URL_PREFIX" value="/web"/>

<form:form id="decline-cancellation-request" cssClass="form action-form confirm alert alert-danger" method="POST"
           action="${URL_PREFIX}/application/${application.id}/decline-cancellation-request" modelAttribute="comment">

    <div class="form-group">
        <strong class="tw-font-medium">
            <spring:message code="action.cancellationRequest.confirm"/>
        </strong>
    </div>

    <div class="form-group">
        <div class="tw-text-sm">
            <c:choose>
                <%-- comment is obligat if it's not the own application or if the application is in status allowed --%>
                <c:when test="${application.status == 'ALLOWED_CANCELLATION_REQUESTED'}">
                    <spring:message code="action.comment.mandatory"/>
                </c:when>
                <c:otherwise>
                    <spring:message code="action.comment.optional"/>
                </c:otherwise>
            </c:choose>
            : (<span id="text-decline-cancellation-request"></span><spring:message code="action.comment.maxChars"/>)
        </div>
        <form:textarea rows="2" path="text" cssClass="form-control" cssErrorClass="form-control error"
                       onkeyup="count(this.value, 'text-decline-cancellation-request');"
                       onkeydown="maxChars(this,200); count(this.value, 'text-decline-cancellation-request');"/>
    </div>

    <div class="form-group is-sticky row">
        <button type="submit" class="btn btn-danger col-xs-12 col-sm-5">
            <spring:message code="action.cancellationRequest"/>
        </button>
        <button type="button" class="btn btn-default col-xs-12 col-sm-5 pull-right" onclick="$('#decline-cancellation-request').hide();">
            <spring:message code="action.cancel"/>
        </button>
    </div>

</form:form>
