<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<script type="text/javascript">
    $(document).ready(function() {
        <c:if test="${action == 'allow'}">
        $("#allow").show();
        </c:if>
    });
</script>

<spring:url var="formUrlPrefix" value="/web" />

<form:form method="put" action="${formUrlPrefix}/application/${application.id}/allow" modelAttribute="comment">
    <div id="confirm" style="display:none" class="confirm alert alert-success">
        <b><spring:message code='allow.confirm' /></b>
        <br /><br />
        <spring:message code='comment' />, <spring:message code="optional" />: (<span id="text-confirm"></span><spring:message code="max.chars" />)
        <br />
        <form:textarea rows="1" cssClass="form-control" cssErrorClass="form-control error" path="reason" onkeyup="count(this.value, 'text-confirm');" onkeydown="maxChars(this,200); count(this.value, 'text-confirm');" />
        <br />
        <button type="submit" class="btn btn-success">
            <i class="fa fa-check"></i>&nbsp;<spring:message code='app.state.ok.short' />
        </button>
        <button type="button" class="btn btn-default" onclick="$('#confirm').hide();">
            <i class="fa fa-remove"></i>&nbsp;<spring:message code='cancel' />
        </button>
    </div>
</form:form>
