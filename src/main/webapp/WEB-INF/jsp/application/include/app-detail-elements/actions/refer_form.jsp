<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<spring:url var="URL_PREFIX" value="/web"/>

<form:form id="refer" cssClass="form action-form confirm alert alert-info" method="POST"
           action="${URL_PREFIX}/application/${application.id}/refer" modelAttribute="referredPerson">

    <div class="form-group">
        <strong class="tw-font-medium"><spring:message code='action.refer.confirm'/></strong>
    </div>

    <div class="form-group">
        <uv:select id="username" name="username">
            <c:forEach items="${bosses}" var="boss">
                <option value="${boss.username}" ${person.username == boss.username ? 'selected="selected"' : ''}>
                    <c:out value="${boss.firstName} ${boss.lastName}"/>
                </option>
            </c:forEach>
        </uv:select>
    </div>

    <div class="form-group is-sticky row">
        <button type="submit" class="button-info col-xs-12 col-sm-5">
            <spring:message code='action.refer'/>
        </button>
        <button type="button" class="button col-xs-12 col-sm-5 pull-right" onclick="$('#refer').hide();">
            <spring:message code="action.cancel"/>
        </button>
    </div>

</form:form>
