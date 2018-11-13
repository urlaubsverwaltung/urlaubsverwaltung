<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<script type="text/javascript">
    $(document).ready(function () {
        <c:if test="${action == 'allow'}">
        $("#allow").show();
        </c:if>
    });
</script>

<spring:url var="URL_PREFIX" value="/web"/>

<c:if test="${shortcut == true}">
    <c:set var="ACTION_URL" value="?redirect=/application/"/>
</c:if>

<c:choose>
      <c:when test="${shortcut == true}">
        <c:set var="ACTION_URL" value="${URL_PREFIX}/application/${application.id}/allow?redirect=/web/application/"/>
      </c:when>

      <c:otherwise>
        <c:set var="ACTION_URL" value="${URL_PREFIX}/application/${application.id}/allow"/>
      </c:otherwise>
</c:choose>

<sec:authorize access="hasRole('DEPARTMENT_HEAD')">
    <c:set var="IS_DEPARTMENT_HEAD" value="${true}"/>
</sec:authorize>

<form:form id="allow" cssClass="form action-form confirm alert alert-success" method="POST"
           action="${ACTION_URL}" modelAttribute="comment">

    <div class="form-group">
        <div class="control-label">
            <c:choose>
                <c:when test="${IS_DEPARTMENT_HEAD && application.twoStageApproval && application.status == 'WAITING'}">
                    <b><spring:message code='action.temporary_allow.confirm'/></b>
                </c:when>
                <c:otherwise>
                    <b><spring:message code='action.allow.confirm'/></b>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <div class="form-group">
        <div class="control-label">
            <spring:message code="action.comment.optional"/>: (<span
                id="text-confirm"></span><spring:message code="action.comment.maxChars"/>)
        </div>
        <form:textarea rows="2" cssClass="form-control" cssErrorClass="form-control error" path="text"
                       onkeyup="count(this.value, 'text-confirm');"
                       onkeydown="maxChars(this,200); count(this.value, 'text-confirm');"/>
    </div>

    <div class="form-group is-sticky row">
        <button type="submit" class="btn btn-success col-xs-12 col-sm-5">
            <spring:message code='action.allow'/>
        </button>
        <button type="button" class="btn btn-default col-xs-12 col-sm-5 pull-right" onclick="$('#allow').hide();">
            <spring:message code="action.cancel"/>
        </button>
    </div>

</form:form>
