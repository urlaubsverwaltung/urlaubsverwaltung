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
  
                            <script type="text/javascript">
                              $(document).ready(function() {
  
                                $("table.sortable").tablesorter({
                                  sortList: [[3,0]],
                                  headers: {
                                    3: { sorter: 'germanDate' },
                                    4: { sorter: 'commaNumber' }
                                  },
                                  textExtraction: function(node) {
  
                                    var sortable = $(node).find(".sortable");
  
                                    if(sortable.length > 0) {
                                      return sortable[0].innerHTML;
                                    }
  
                                    return node.innerHTML;
                                  }
                                });
  
                              });
                            </script>
  
                            <table class="list-table selectable-table sortable tablesorter" cellspacing="0">
                              <thead class="hidden-xs hidden-sm">
                              <tr>
                                <th class="hidden-print"><%-- placeholder to ensure correct number of th --%></th>
                                <th class="sortable-field"><spring:message code="firstname" /></th>
                                <th class="sortable-field"><spring:message code="lastname" /></th>
                                <th class="sortable-field"><spring:message code="time" /></th>
                                <th class="sortable-field"><spring:message code="application.vacation.days" /></th>
                              </tr>
                              </thead>
                              <tbody>
                              <c:forEach items="${applications}" var="app" varStatus="loopStatus">
                                <c:choose>
                                  <c:when test="${app.status == 'CANCELLED' || app.status == 'REJECTED'}">
                                    <c:set var="CSS_CLASS" value="inactive"/>
                                  </c:when>
                                  <c:otherwise>
                                    <c:set var="CSS_CLASS" value="active"/>
                                  </c:otherwise>
                                </c:choose>
                                <tr class="${CSS_CLASS}" onclick="navigate('${URL_PREFIX}/application/${app.id}');">
                                  <td class="hidden-print is-centered">
                                    <img class="img-circle" src="<c:out value='${gravatarUrls[app]}?d=mm&s=60'/>"/>&nbsp;
                                  </td>
                                  <td class="hidden-xs">
                                    <p><c:out value="${app.person.firstName}"/></p>
                                  </td>
                                  <td class="hidden-xs">
                                    <p><c:out value="${app.person.lastName}"/></p>
                                  </td>
                                  <td>
                                    <a class="vacation ${app.vacationType} hidden-print" href="${URL_PREFIX}/application/${app.id}">
                                      <h4><spring:message code="${app.vacationType}"/></h4>
                                    </a>
  
                                    <p class="visible-print">
                                      <spring:message code="${app.vacationType}"/>
                                    </p>
  
                                    <p class="sortable">
                                      <c:choose>
                                        <c:when test="${app.startDate == app.endDate}">
                                          <uv:date date="${app.startDate}"/>, <spring:message
                                            code="${app.howLong}"/>
                                        </c:when>
                                        <c:otherwise>
                                          <uv:date date="${app.startDate}"/> - <uv:date date="${app.endDate}"/>
                                        </c:otherwise>
                                      </c:choose>
                                    </p>
                                  </td>
                                  <td class="hidden-xs">
                          <span>
                              <span class="sortable"><uv:number number="${app.workDays}" /></span> <spring:message code="duration.days" />
                          </span>
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


