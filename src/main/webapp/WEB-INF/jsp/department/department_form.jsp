<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>


<!DOCTYPE html>
<html>

<head>
    <uv:head />
</head>

<body>

<uv:menu />

<spring:url var="URL_PREFIX" value="/web"/>


<div class="content">
<div class="container">

<c:choose>
    <c:when test="${department.id == null}">
        <c:set var="METHOD" value="POST"/>
        <c:set var="ACTION" value="${URL_PREFIX}/department"/>
    </c:when>
    <c:otherwise>
        <c:set var="METHOD" value="PUT"/>
        <c:set var="ACTION" value="${URL_PREFIX}/department/${department.id}"/>
    </c:otherwise>
</c:choose>

<form:form method="${METHOD}" action="${ACTION}" modelAttribute="department" class="form-horizontal">
<form:hidden path="id" />

<div class="row">

<div class="col-xs-12 col-md-6">

    <div class="header">

        <legend><spring:message code="department.data"/></legend>

    </div>

    <div class="form-group">
        <label class="control-label col-md-4" for="name"><spring:message code='department.data.name'/></label>

        <div class="col-md-7">
            <form:input id="name" path="name" class="form-control" cssErrorClass="form-control error" />
            <span class="help-inline"><form:errors path="name" cssClass="error"/></span>
        </div>
    </div>

    <div class="form-group">
        <label class="control-label col-md-4" for="description"><spring:message code='department.data.description'/></label>

        <div class="col-md-7">
            <span id="text-description"></span><spring:message code='max.chars'/>
            <form:textarea id="description" rows="1" path="description" class="form-control" cssErrorClass="form-control error"
                  onkeyup="count(this.value, 'text-description');"
                  onkeydown="maxChars(this,200); count(this.value, 'text-description');"/>
            <form:errors path="description" cssClass="error"/>
        </div>
    </div>

</div>

<div class="col-xs-12 col-md-6">

    <div class="header">

        <legend><spring:message code="department.members"/></legend>

    </div>

   <c:forEach items="${persons}" var="person">

   <div class="checkbox">
        <label>
            <form:checkbox path="members" value="${person}" />
            <c:out value="${person.niceName}"/>
        </label>
   </div>

   </c:forEach>

</div>

<div class="row">
    <div class="col-xs-12">

        <hr/>

        <button class="btn btn-success col-xs-12 col-sm-5 col-md-2" type="submit"><spring:message code="action.save" /></button>
        <a class="btn btn-default col-xs-12 col-sm-5 col-md-2 pull-right" href="${URL_PREFIX}/department"><spring:message code="action.cancel"/></a>

    </div>
</div>

</form:form>

</div>
</div>

</body>

</html>
