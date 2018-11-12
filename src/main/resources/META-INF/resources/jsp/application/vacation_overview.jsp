<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="sec"
	uri="http://www.springframework.org/security/tags"%>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags"%>

<sec:authorize access="hasAuthority('USER')">
	<c:set var="IS_USER" value="${true}" />
</sec:authorize>
<sec:authorize access="hasAuthority('BOSS')">
	<c:set var="IS_BOSS" value="${true}" />
</sec:authorize>
<sec:authorize access="hasAuthority('DEPARTMENT_HEAD')">
	<c:set var="IS_DEPARTMENT_HEAD" value="${true}" />
</sec:authorize>
<sec:authorize access="hasAuthority('OFFICE')">
	<c:set var="IS_OFFICE" value="${true}" />
</sec:authorize>
<c:set var="IS_ALLOWED" value="${IS_USER || IS_BOSS || IS_DEPARTMENT_HEAD || IS_OFFICE }" />
<html>

<head>
<uv:head />
<%@include file="include/app-detail-elements/vacation_overview_js.jsp"%>
</head>

<body>
	<spring:url var="URL_PREFIX" value="/web" />

	<sec:authorize access="hasAuthority('OFFICE')">
		<c:set var="IS_OFFICE" value="true" />
	</sec:authorize>

	<uv:menu />

	<div class="content print--only-portrait">
		<div class="container">

			<c:if test="${IS_ALLOWED}">

				<div class="row">
					<div class="col-xs-12">
						<legend id="vacation">
							<spring:message code="overview.vacationOverview.title" />
						</legend>
					</div>
				</div>

                <div class="col-md-8">
                    <div class="form-group">
                    <div class="row">
                        <label class="control-label col-md-3" for="yearSelect">
								Jahr:
                        </label>
						<div class="col-md-6">
							<select id="yearSelect" name="yearSelect" size="1" path="" class="form-control">
								<c:forEach var="i" begin="1" end="9">
									<option value="${currentYear - 10 + i}">
										<c:out value="${currentYear - 10 + i}" />
									</option>
								</c:forEach>
								<option value="${currentYear}" selected="${currentYear}">
									<c:out value="${currentYear}" />
								</option>
								<option value="${currentYear +1}">
									<c:out value="${currentYear +1}" />
								</option>
							</select>
						</div>
						</div>
					</div>
                    <div class="form-group">
                    <div class="row">
						<label class="control-label col-md-3" for="monthSelect">
							Monat:
						</label>
						<div class="col-md-6">
							<select id="monthSelect" name="monthSelect" size="1" path="" class="form-control">
								<c:forEach var="i" begin="1" end="12">
									<c:if test="${currentMonth == i }">
										<option value="${i}" selected="${i}">
											<spring:eval expression="T(org.synyx.urlaubsverwaltung.core.util.DateUtil).getMonthName(i)" var="month" />
											<c:out value="${month}" />
										</option>
									</c:if>
									<c:if test="${currentMonth != i }">
										<option value="${i}">
											<spring:eval expression="T(org.synyx.urlaubsverwaltung.core.util.DateUtil).getMonthName(i)" var="month" />
											<c:out value="${month}" />
										</option>
									</c:if>
								</c:forEach>
							</select>
						</div>
						</div>
					</div>
                    <div class="form-group">
                    <div class="row">
                        <label class="control-label col-md-3" for="departmentSelect">
								Abteilung:
                        </label>
                        <div class="col-md-6">
							<select id="departmentSelect" name="departmentSelect" size="1" path="" class="form-control">
								<c:forEach items="${departments}" var="department">
									<option value="${department}">
										<c:out value="${department.name}" />
									</option>
								</c:forEach>
							</select>
						</div>
					</div>
					</div>
				</div>

				 <div class="row">
           			<div class="col-xs-12">
                		<hr/>
						<div id="vacationOverview"></div>
					</div>
				</div>

                <div id="vacationOverviewLegend" class="row">
					<label class="col-md-1">
                         <spring:message code="overview.vacationOverview.legendTitle" />
                     </label>
                     <div class="col-md-3">
                         <table>
                             <tr>
                                 <td class='vacationOverview-legend-colorbox vacationOverview-day-weekend'></td>
                                 <td class='vacationOverview-legend-text'><spring:message code="overview.vacationOverview.weekend" /></td>
                             </tr>
                             <tr>
                                 <td class='vacationOverview-legend-colorbox vacationOverview-day-personal-holiday-status-ALLOWED'></td>
                                 <td class='vacationOverview-legend-text'><spring:message code="overview.vacationOverview.allowed" /></td>
                             </tr>
                             <tr>
                                 <td class='vacationOverview-legend-colorbox vacationOverview-day-personal-holiday-status-WAITING'></td>
                                 <td class='vacationOverview-legend-text'><spring:message code="overview.vacationOverview.vacation" /></td>
                             </tr>
                             <tr>
                                 <td class='vacationOverview-legend-colorbox vacationOverview-day-sick-note'></td>
                                 <td class='vacationOverview-legend-text'><spring:message code="overview.vacationOverview.sick" /></td>
                             </tr>
                         </table>
                     </div>
                </div>

			</c:if>
		</div>

	</div>

</body>