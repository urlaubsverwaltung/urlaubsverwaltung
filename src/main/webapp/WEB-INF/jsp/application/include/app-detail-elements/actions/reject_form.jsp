<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<script type="text/javascript">
    $(document).ready(function () {
        <c:if test="${action == 'reject'}">
        $("#reject").show();
        </c:if>
    });
</script>

<spring:url var="URL_PREFIX" value="/web"/>

<form:form id="reject" cssClass="form action-form confirm alert alert-danger" method="PUT"
           action="${URL_PREFIX}/application/${application.id}/reject" modelAttribute="comment">

    <div class="form-group">
        <div class="control-label">
            <b><spring:message code='reject.confirm'/></b>
        </div>
    </div>

    <div class="form-group">
        <div class="control-label">
            <spring:message code='comment'/>, <spring:message code="obligat"/>: (<span
                id="text-reject"></span><spring:message code="max.chars"/>)
        </div>
        <form:textarea rows="2" path="text" cssClass="form-control" cssErrorClass="form-control error"
                       onkeyup="count(this.value, 'text-reject');"
                       onkeydown="maxChars(this,200); count(this.value, 'text-reject');"/>
    </div>

    <div class="form-group is-sticky row">
        <button type="submit" class="btn btn-danger col-xs-12 col-sm-5">
            <i class="fa fa-ban"></i>&nbsp;<spring:message code='app.state.no.short'/>
        </button>
        <button type="button" class="btn btn-default col-xs-12 col-sm-5 pull-right" onclick="$('#reject').hide();">
            <i class="fa fa-remove"></i>&nbsp;<spring:message code="action.cancel"/>
        </button>
    </div>

</form:form>
