<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<spring:url var="formUrlPrefix" value="/web" />

<div class="navbar navbar-inverse navbar-fixed-top">
    <div class="navbar-inner">
        <div class="grid-container">
            <div class="grid-100">
            <a class="brand" href="${formUrlPrefix}/overview"><spring:message code="title" /></a>
            <ul class="nav">

                <sec:authorize access="hasRole('USER')">
                    <li class="dropdown">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                            <i class="icon-home"></i>&nbsp;<spring:message code="overview" />
                            <span class="caret"></span>
                        </a>
                        <ul class="dropdown-menu">
                            <li>
                                <a href="${formUrlPrefix}/overview">
                                    <i class="icon-home"></i>&nbsp;<spring:message code="overview" />
                                </a>
                            </li>
                            <li>
                                <a href="${formUrlPrefix}/application/new">
                                    <i class="icon-pencil"></i>&nbsp;<spring:message code="ov.apply"/>
                                </a>
                            </li>
                            <li>
                                <a href="${formUrlPrefix}/overview#sickNotes">
                                    <img src="<spring:url value='/images/agt_virus-off.png' />" />&nbsp;<spring:message code="sicknotes" />
                                </a>
                            </li>
                        </ul>
                    </li>
                </sec:authorize>

                <sec:authorize access="hasAnyRole('BOSS', 'OFFICE')">
                    <li class="dropdown">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                            <i class="icon-list-alt"></i>&nbsp;<spring:message code="apps.vac" />
                            <span class="caret"></span>
                        </a>
                        <ul class="dropdown-menu icons-on-top">
                            <li>
                                <a href="${formUrlPrefix}/application/all">
                                    <i class="icon-th-list"></i>&nbsp;<spring:message code="all.app" />
                                </a>
                            </li>
                            <li>
                                <a href="${formUrlPrefix}/application/waiting">
                                    &nbsp;<b class="waiting-icon">?</b>&nbsp;<spring:message code="waiting.app" />
                                </a>
                            </li>
                            <li>
                                <a href="${formUrlPrefix}/application/allowed">
                                    <i class="icon-ok"></i>&nbsp;<spring:message code="allow.app" />
                                </a>
                            </li>
                            <li>
                                <a href="${formUrlPrefix}/application/rejected">
                                    <i class="icon-ban-circle"></i>&nbsp;<spring:message code="reject.app" />
                                </a>
                            </li>
                            <li>
                                <a href="${formUrlPrefix}/application/cancelled">
                                    <i class="icon-trash"></i>&nbsp;<spring:message code="cancel.app" />
                                </a>
                            </li>
                        </ul>
                    </li>
                </sec:authorize>

                <sec:authorize access="hasRole('OFFICE')">
                    <li class="dropdown">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                            <img src="<spring:url value='/images/agt_virus-off.png' />" />&nbsp;<spring:message code="sicknotes" />
                            <span class="caret"></span>
                        </a>
                        <ul class="dropdown-menu icons-on-top">
                            <li>
                                <a href="${formUrlPrefix}/sicknote">
                                    <spring:message code="sicknotes.nav.current" />
                                </a>
                            </li>
                            <li>
                                <a href="${formUrlPrefix}/sicknote/quartal">
                                    <spring:message code="sicknotes.nav.quartal" />
                                </a>
                            </li>
                            <li>
                                <a href="${formUrlPrefix}/sicknote/year">
                                    <spring:message code="sicknotes.nav.year" />
                                </a>
                            </li>
                            <li class="divider"></li>
                            <li>
                                <a href="${formUrlPrefix}/sicknote/new">
                                    <i class="icon-plus"></i>&nbsp;<spring:message code="sicknotes.nav.new" />
                                </a>
                            </li>
                        </ul>
                    </li>
                </sec:authorize>

                <sec:authorize access="hasAnyRole('BOSS', 'OFFICE')">

                    <li>
                        <a href="${formUrlPrefix}/staff"><i class="icon-user"></i>&nbsp;<spring:message code="staff.manager" /></a>
                    </li>

                </sec:authorize>

                <sec:authorize access="hasRole('ADMIN')">
                    <li><a href="${formUrlPrefix}/management"><i class="icon-wrench"></i>&nbsp;<spring:message code="role.management" /></a></li>
                </sec:authorize>

                <li><a href="<spring:url value='/j_spring_security_logout' />"><i class="icon-off"></i>&nbsp;Logout</a></li>

            </ul>
            </div>
        </div>
    </div>
</div>