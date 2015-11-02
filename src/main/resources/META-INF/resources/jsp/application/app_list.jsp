<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>


<!DOCTYPE html>
<html>

    <head>
        <uv:head />
    </head>

    <body>

        <spring:url var="URL_PREFIX" value="/web" />
        <c:set var="linkPrefix" value="${URL_PREFIX}/application" />

        <uv:menu />

        <div class="content">

            <div class="container">

                <div class="row">

                    <div class="col-xs-12">

                        <legend>

                          <spring:message code="applications.waiting"/>

                          <a href="${URL_PREFIX}/application/statistics" class="fa-action pull-right"
                              data-title="<spring:message code="action.applications.statistics"/>">
                            <i class="fa fa-fw fa-bar-chart"></i>
                          </a>

                          <sec:authorize access="hasRole('OFFICE')">
                          <a href="${URL_PREFIX}/application/new" class="fa-action pull-right"
                              data-title="<spring:message code="action.apply.vacation"/>">
                            <i class="fa fa-fw fa-plus-circle"></i>
                          </a>
                          </sec:authorize>

                        </legend>

                        <div class="feedback">
                            <c:choose>
                                <c:when test="${allowSuccess}">
                                    <div class="alert alert-success">
                                        <spring:message code="application.action.allow.success" />
                                    </div>
                                </c:when>
                                <c:when test="${rejectSuccess}">
                                    <div class="alert alert-success">
                                        <spring:message code="application.action.reject.success" />
                                    </div>
                                </c:when>
                            </c:choose>
                        </div>

                        <c:choose>
  
                          <c:when test="${empty applications}">
  
                            <spring:message code="applications.none"/>
  
                          </c:when>
  
                          <c:otherwise>
  
                            <table class="list-table bordered-table selectable-table" cellspacing="0">
                              <tbody>
                              <c:forEach items="${applications}" var="application" varStatus="loopStatus">
                                <tr class="active" onclick="navigate('${URL_PREFIX}/application/${application.id}');">
                                  <td class="hidden-print is-centered">
                                    <div class="gravatar img-circle" data-gravatar="<c:out value='${application.person.gravatarURL}?d=mm&s=60'/>"></div>
                                  </td>
                                  <td class="hidden-xs">
                                    <h5><c:out value="${application.person.niceName}"/></h5>
                                    <p><spring:message code="application.applier.applied"/></p>
                                  </td>
                                  <td class="halves">
                                    <a class="vacation ${application.vacationType} hidden-print" href="${URL_PREFIX}/application/${application.id}">
                                      <h4><uv:number number="${application.workDays}" /> <spring:message code="duration.days" /> <spring:message code="${application.vacationType}"/></h4>
                                    </a>
                                    <p>
                                      <c:choose>
                                        <c:when test="${application.startDate == application.endDate}">
                                            <c:set var="APPLICATION_DATE">
                                                <uv:date date="${application.startDate}"/>
                                            </c:set>
                                            <c:set var="APPLICATION_DAY_LENGTH">
                                                <spring:message code="${application.dayLength}"/>
                                            </c:set>
                                            <spring:message code="absence.period.singleDay" arguments="${APPLICATION_DATE};${APPLICATION_DAY_LENGTH}" argumentSeparator=";"/>
                                        </c:when>
                                        <c:otherwise>
                                            <c:set var="APPLICATION_START_DATE">
                                                <uv:date date="${application.startDate}"/>
                                            </c:set>
                                            <c:set var="APPLICATION_END_DATE">
                                                <uv:date date="${application.endDate}"/>
                                            </c:set>
                                            <spring:message code="absence.period.multipleDays" arguments="${APPLICATION_START_DATE};${APPLICATION_END_DATE}" argumentSeparator=";"/>
                                        </c:otherwise>
                                      </c:choose>
                                    </p>
                                  </td>
                                  <td class="hidden-print hidden-xs hidden-sm">
                                    <p>
                                       <c:choose>
                                           <c:when test="${application.teamInformed == true}">
                                               <i class="fa fa-check hidden-print"></i>
                                               <spring:message code="applications.waiting.teamInformed.true" />
                                           </c:when>
                                           <c:otherwise>
                                               <i class="fa fa-remove hidden-print"></i>
                                               <spring:message code="applications.waiting.teamInformed.false" />
                                           </c:otherwise>
                                       </c:choose>
                                    </p>
                                    <c:choose>
                                        <c:when test="${application.reason != null && !empty application.reason}">
                                            <div class="overflow" data-toggle="popover" data-trigger="hover" data-placement="right" title="<spring:message code='application.data.reason'/>" data-content="${application.reason}">                                    
                                                <i class="fa fa-comments"></i>
                                                ${application.reason}
                                            </div>
                                        </c:when>
                                    </c:choose>
                                  </td>
                                  <td class="hidden-xs hidden-sm">
                                      <sec:authorize access="hasAnyRole('DEPARTMENT_HEAD', 'BOSS')">
                                          <a class="fa-action positive" href="${URL_PREFIX}/application/${application.id}?action=allow&shortcut=true"
                                             data-title="<spring:message code='action.allow'/>">
                                              <i class="fa fa-check"></i>
                                          </a>
                                      </sec:authorize>
                                  </td>
                                  <td class="hidden-xs hidden-sm">
                                      <sec:authorize access="hasAnyRole('DEPARTMENT_HEAD', 'BOSS')">
                                          <a class="fa-action negative" href="${URL_PREFIX}/application/${application.id}?action=reject&shortcut=true"
                                             data-title="<spring:message code='action.reject'/>">
                                              <i class="fa fa-ban"></i>
                                          </a>
                                      </sec:authorize>
                                  </td>
                                </tr>
                              </c:forEach>
                              </tbody>
                            </table>
                          </c:otherwise>
                        </c:choose>

                    </div>

                </div>
            </div>
        </div>            

    </body>

</html>


