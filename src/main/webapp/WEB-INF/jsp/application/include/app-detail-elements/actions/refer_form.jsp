<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<form:form method="put" action="${formUrlPrefix}/application/${application.id}/refer" modelAttribute="modelPerson">
    <div id="refer" style="display: none" class="confirm alert alert-info">
        <b><spring:message code="please.refer" /></b>
        <br /><br />
        <form:select path="loginName" cssClass="form-control">
            <c:forEach items="${bosses}" var="boss">
                <option value="${boss.loginName}"><c:out value="${boss.firstName} ${boss.lastName}" /></option>
            </c:forEach>
        </form:select>
        &nbsp;
        <button type="submit" class="btn btn-info" style="margin-top: 0">
            <i class="fa fa-mail-forward"></i>&nbsp;<spring:message code='app.state.refer.short' />
        </button>
        <button type="button" class="btn btn-default" style="margin-top: 0" onclick="$('#refer').hide();">
            <i class="fa fa-remove"></i>&nbsp;<spring:message code='cancel' />
        </button>
    </div>
</form:form>
