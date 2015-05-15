<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<spring:url var="URL_PREFIX" value="/web"/>

<sec:authorize access="hasRole('USER')">
  <uv:print/>
</sec:authorize>

<sec:authorize access="hasRole('USER')">
  <c:set var="IS_USER" value="${true}"/>
</sec:authorize>

<sec:authorize access="hasRole('BOSS')">
  <c:set var="IS_BOSS" value="${true}"/>
</sec:authorize>

<sec:authorize access="hasRole('OFFICE')">
  <c:set var="IS_OFFICE" value="${true}"/>
</sec:authorize>


<c:if test="${application.status == 'WAITING' || (application.status == 'ALLOWED' && IS_OFFICE)}">

  <c:if test="${application.status == 'WAITING'}">
    <c:if test="${IS_BOSS}">
      <a href="#" class="fa-action pull-right" data-title="<spring:message code='action.refer'/>"
         onclick="$('#reject').hide(); $('#allow').hide(); $('#cancel').hide(); $('#refer').show();">
        <i class="fa fa-share-alt"></i>
      </a>
      <a href="#" class="fa-action negative pull-right" data-title="<spring:message code='action.reject'/>"
         onclick="$('#refer').hide(); $('#allow').hide(); $('#cancel').hide(); $('#reject').show();">
        <i class="fa fa-ban"></i>
      </a>
      <a href="#" class="fa-action positive pull-right" data-title="<spring:message code='action.allow'/>"
         onclick="$('#reject').hide(); $('#refer').hide(); $('#cancel').hide(); $('#allow').show();">
        <i class="fa fa-check"></i>
      </a>
    </c:if>
    <c:if test="${(IS_USER && application.person.id == loggedUser.id) || IS_OFFICE}">
      <a href="#" class="fa-action negative pull-right" data-title="<spring:message code='action.delete'/>"
         onclick="$('#reject').hide(); $('#allow').hide(); $('#refer').hide(); $('#cancel').show();">
        <i class="fa fa-trash"></i>
      </a>
    </c:if>
    <c:if test="${IS_USER && application.person.id == loggedUser.id}">
      <a href="#" class="fa-action pull-right" data-title="<spring:message code='action.remind'/>"
         onclick="$('form#remind').submit();">
        <i class="fa fa-bullhorn"></i>
      </a>
    </c:if>
  </c:if>
  <c:if test="${application.status == 'ALLOWED' && IS_OFFICE}">
    <a href="#" class="fa-action negative pull-right" data-title="<spring:message code='action.delete'/>"
       onclick="$('#reject').hide(); $('#allow').hide(); $('#refer').hide(); $('#cancel').show();">
      <i class="fa fa-trash"></i>
    </a>
  </c:if>
</c:if>