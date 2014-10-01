<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<script type="text/javascript">
    $(document).ready(function() {
        <c:if test="${action == 'reject'}">
        $("#reject").show();
        </c:if>
    });
</script>

<spring:url var="formUrlPrefix" value="/web" />

<form:form method="put" action="${formUrlPrefix}/application/${application.id}/reject" modelAttribute="comment">
    <div id="reject" class="confirm alert alert-danger" style="display: none">

        <b><spring:message code='reject.confirm'/></b>
        <br/><br/>
        <spring:message code='reason'/>, <spring:message code="obligat"/>: (<span
            id="text-reject"></span><spring:message code="max.chars"/>)
        <br/>
        <form:textarea rows="1" path="reason" cssClass="form-control" cssErrorClass="form-control error"
                       onkeyup="count(this.value, 'text-reject');"
                       onkeydown="maxChars(this,200); count(this.value, 'text-reject');"/>

        <br/>

        <button type="submit" class="btn btn-danger">
            <i class="fa fa-ban"></i>&nbsp;<spring:message code='app.state.no.short'/>
        </button>
        <button type="button" class="btn btn-default" onclick="$('#reject').hide();">
            <i class="fa fa-remove"></i>&nbsp;<spring:message code='cancel'/>
        </button>

    </div>
</form:form>
