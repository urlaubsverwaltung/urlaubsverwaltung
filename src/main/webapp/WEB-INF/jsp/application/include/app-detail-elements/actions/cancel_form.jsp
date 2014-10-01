<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>


<script type="text/javascript">
    $(document).ready(function () {
        <c:if test="${action == 'cancel'}">
        $("#cancel").show();
        </c:if>
    });
</script>

<spring:url var="formUrlPrefix" value="/web"/>

<form:form method="put" action="${formUrlPrefix}/application/${application.id}/cancel" modelAttribute="comment">
    <div id="cancel" class="confirm alert alert-danger" style="display: none">
        <b><spring:message code='cancel.confirm'/></b>
        <br/><br/>
        <spring:message code='comment'/>,
        <c:choose>
            <%-- comment is obligat if it's not the own application or if the application is in status allowed --%>
            <c:when test="${application.person.id != loggedUser.id || application.status == 'ALLOWED'}">
                <spring:message code="obligat"/>
            </c:when>
            <%-- otherwise comment is not obligat --%>
            <c:otherwise>
                <spring:message code="optional"/>
            </c:otherwise>
        </c:choose>
        : (<span id="text-cancel"></span><spring:message code="max.chars"/>)
        <br/>
        <form:textarea rows="1" path="reason" cssClass="form-control" cssErrorClass="form-control error"
                       onkeyup="count(this.value, 'text-cancel');"
                       onkeydown="maxChars(this,200); count(this.value, 'text-cancel');"/>
        <br/>
        <button type="submit" class="btn btn-danger">
            <i class="fa fa-trash"></i>&nbsp;<spring:message code='delete'/>
        </button>
        <button type="button" class="btn btn-default" onclick="$('#cancel').hide();">
            <i class="fa fa-remove"></i>&nbsp;<spring:message code='cancel'/>
        </button>
    </div>
</form:form>
