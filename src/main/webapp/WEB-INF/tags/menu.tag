<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<spring:url var="formUrlPrefix" value="/web" />

<nav class="navbar navbar-default" role="navigation">
    <div class="container-fluid">
        <!-- Brand and toggle get grouped for better mobile display -->
        <div class="navbar-header">
            <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar-collapse">
                <span class="sr-only">Toggle navigation</span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <a class="navbar-brand" href="#">
                <img class="visible-xs hidden-sm hidden-md hidden-lg" src="<spring:url value='/images/synyx-logo-transparent.png' />" height="24" width="15" />
                <img class="hidden-xs visible-sm visible-md visible-lg" src="<spring:url value='/images/synyx-logo.jpg' />" height="64" width="127" />
            </a>
        </div>

        <!-- Collect the nav links, forms, and other content for toggling -->
        <div class="collapse navbar-collapse" id="navbar-collapse">
            <ul class="nav navbar-nav navbar-right">
                <sec:authorize access="hasRole('USER')">
                    <li>
                        <a href="${formUrlPrefix}/overview">
                            <span class="icon hidden-xs hidden-sm visible-md visible-lg" title="<spring:message code='overview'/>">
                                <i class="fa fa-home"></i>
                            </span>
                            <span class="visible-xs visible-sm hidden-md hidden-lg">
                                <i class="fa fa-home"></i>&nbsp;<spring:message code="overview"/>
                            </span>
                        </a>  
                    </li>
                    <li>
                        <a href="${formUrlPrefix}/application/new">
                            <span class="icon hidden-xs hidden-sm visible-md visible-lg" title="<spring:message code='ov.apply'/>">
                                <i class="fa fa-pencil"></i>
                            </span>
                            <span class="visible-xs visible-sm hidden-md hidden-lg">
                                <i class="fa fa-pencil"></i>&nbsp;<spring:message code="overview"/>
                            </span>
                        </a>
                    </li>
                    </li>
                </sec:authorize>

                <sec:authorize access="hasAnyRole('BOSS', 'OFFICE')">
                    <li>
                        <a href="${formUrlPrefix}/application/all">
                            <span class="icon hidden-xs hidden-sm visible-md visible-lg" title="<spring:message code='apps.vac'/>">
                                <i class="fa fa-calendar"></i>
                            </span>
                            <span class="visible-xs visible-sm hidden-md hidden-lg">
                                <i class="fa fa-calendar"></i>&nbsp;<spring:message code="apps.vac"/>
                            </span>
                        </a>
                    </li>
                </sec:authorize>

                <sec:authorize access="hasRole('OFFICE')">
                    <li>
                        <a href="${formUrlPrefix}/sicknote/quartal">
                            <span class="icon hidden-xs hidden-sm visible-md visible-lg" title="<spring:message code='sicknotes'/>">
                                <i class="fa fa-medkit"></i>
                            </span>
                            <span class="visible-xs visible-sm hidden-md hidden-lg">
                                <i class="fa fa-medkit"></i>&nbsp;<spring:message code="sicknotes"/>
                            </span>
                        </a>
                    </li>
                </sec:authorize>

                <sec:authorize access="hasAnyRole('BOSS', 'OFFICE')">

                    <li>
                        <a href="${formUrlPrefix}/staff">
                            <span class="icon hidden-xs hidden-sm visible-md visible-lg" title="<spring:message code='staff.manager'/>">
                                <i class="fa fa-user"></i>
                            </span>
                            <span class="visible-xs visible-sm hidden-md hidden-lg">
                                <i class="fa fa-user"></i>&nbsp;<spring:message code="staff.manager"/>
                            </span>
                        </a>
                    </li>

                </sec:authorize>

                <li>
                    <a href="<spring:url value='/j_spring_security_logout' />">
                        <span class="icon hidden-xs hidden-sm visible-md visible-lg" title="Logout">
                            <i class="fa fa-sign-out"></i>
                        </span>
                        <span class="visible-xs visible-sm hidden-md hidden-lg">
                            <i class="fa fa-sign-out"></i>&nbsp;Logout
                        </span>
                    </a>
                </li>
            </ul>
        </div><!-- /.navbar-collapse -->
    </div><!-- /.container-fluid -->
</nav>