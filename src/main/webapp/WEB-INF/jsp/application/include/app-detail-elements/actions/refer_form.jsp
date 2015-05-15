<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<spring:url var="URL_PREFIX" value="/web"/>

<form:form id="refer" cssClass="form action-form confirm alert alert-info" method="PUT"
           action="${URL_PREFIX}/application/${application.id}/refer" modelAttribute="modelPerson">

    <div class="form-group">
        <div class="control-label">
            <b><spring:message code='please.refer'/></b>
        </div>
    </div>

    <div class="form-group">
        <form:select path="loginName" cssClass="form-control">
            <c:forEach items="${bosses}" var="boss">
                <option value="${boss.loginName}"><c:out value="${boss.firstName} ${boss.lastName}"/></option>
            </c:forEach>
        </form:select>
    </div>

    <div class="form-group is-sticky row">
        <button type="submit" class="btn btn-info col-xs-12 col-sm-5">
            <i class="fa fa-share-alt"></i>&nbsp;<spring:message code='action.refer'/>
        </button>
        <button type="button" class="btn btn-default col-xs-12 col-sm-5 pull-right" onclick="$('#refer').hide();">
            <i class="fa fa-remove"></i>&nbsp;<spring:message code="action.cancel"/>
        </button>
    </div>

</form:form>
