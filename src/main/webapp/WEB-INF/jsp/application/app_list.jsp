<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>


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

                        <div class="header">
  
                          <legend>
  
                            <p><spring:message code="applications.waiting"/></p>
  
                          </legend>
  
                        </div>

                        <c:choose>
  
                          <c:when test="${empty applications}">
  
                            <spring:message code="no.apps"/>
  
                          </c:when>
  
                          <c:otherwise>
  
                            <table class="list-table bordered-table selectable-table" cellspacing="0">
                              <tbody>
                              <c:forEach items="${applications}" var="application" varStatus="loopStatus">
                                <tr class="active" onclick="navigate('${URL_PREFIX}/application/${application.id}');">
                                  <td class="hidden-print is-centered">
                                    <img class="img-circle" src="<c:out value='${gravatarUrls[application]}?d=mm&s=60'/>"/>&nbsp;
                                  </td>
                                  <td>
                                    <h4><c:out value="${application.person.niceName}"/></h4>
                                    <p><spring:message code="app.apply"/></p>
                                  </td>
                                  <td>
                                    <a class="vacation ${application.vacationType} hidden-print" href="${URL_PREFIX}/application/${application.id}">
                                      <h4><uv:number number="${application.workDays}" /> <spring:message code="duration.days" /> <spring:message code="${application.vacationType}"/></h4>
                                    </a>
                                    <p>
                                      <c:choose>
                                        <c:when test="${application.startDate == application.endDate}">
                                          <spring:message code="at"/> <uv:date date="${application.startDate}"/>,
                                          <spring:message code="${application.howLong}"/>
                                        </c:when>
                                        <c:otherwise>
                                          <spring:message code="from"/> <uv:date date="${application.startDate}"/> <spring:message code="to"/> <uv:date date="${application.endDate}"/>
                                        </c:otherwise>
                                      </c:choose>
                                    </p>
                                  </td>
                                  <td>
                                    <i class="fa fa-clock-o"></i>
                                    <spring:message code="application.progress.WAITING"/>
                                    <uv:date date="${application.applicationDate}"/>
                                  </td>
                                  <td>
                                    <a class="fa-action" href="${URL_PREFIX}/application/${application.id}?action=allow">
                                        <i class="fa fa-check"></i>
                                    </a>
                                    <a class="fa-action negative" href="${URL_PREFIX}/application/${application.id}?action=reject">
                                        <i class="fa fa-ban"></i>
                                    </a>
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


