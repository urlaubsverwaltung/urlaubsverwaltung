<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<spring:url var="URL_PREFIX" value="/web" />

<nav class="navbar navbar-default navbar-fixed-top" role="navigation">
    <div class="container">
        <!-- Brand and toggle get grouped for better mobile display -->
        <div class="navbar-header">
            <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar-collapse">
                <span class="sr-only">Toggle navigation</span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <a class="navbar-brand" href="#">
                <img src="<spring:url value='/images/synyx-logo-transparent.png' />" height="24" width="15" />
                <spring:message code="nav.title" />
            </a>
        </div>

        <!-- Collect the nav links, forms, and other content for toggling -->
        <div class="collapse navbar-collapse" id="navbar-collapse">
            <ul class="nav navbar-nav navbar-right">
                <sec:authorize access="hasRole('USER')">
                    <li>
                        <a href="${URL_PREFIX}/overview">
                            <i class="fa fa-home"></i>&nbsp;<spring:message code="nav.overview"/>
                        </a>
                    </li>
                    <li>
                        <a href="${URL_PREFIX}/application/new">
                            <i class="fa fa-pencil"></i>&nbsp;<spring:message code="nav.apply"/>
                        </a>
                    </li>
                    </li>
                </sec:authorize>

                <sec:authorize access="hasAnyRole('BOSS', 'OFFICE')">
                    <li>
                        <a href="${URL_PREFIX}/application/waiting">
                            <i class="fa fa-calendar"></i>&nbsp;<spring:message code="nav.vacations"/>
                        </a>
                    </li>
                </sec:authorize>

                <sec:authorize access="hasRole('OFFICE')">
                    <li>
                        <a href="${URL_PREFIX}/sicknote/"><i class="fa fa-medkit"></i>&nbsp;<spring:message code="nav.sicknotes" /></a>
                    </li>
                </sec:authorize>

                <sec:authorize access="hasAnyRole('BOSS', 'OFFICE')">

                    <li>
                        <a href="${URL_PREFIX}/staff"><i class="fa fa-user"></i>&nbsp;<spring:message code="nav.staff" /></a>
                    </li>

                </sec:authorize>

                <li><a href="<spring:url value='/j_spring_security_logout' />"><i class="fa fa-sign-out"></i>&nbsp;<spring:message code="nav.signout" /></a></li>
            </ul>
        </div><!-- /.navbar-collapse -->
    </div><!-- /.container -->
</nav>