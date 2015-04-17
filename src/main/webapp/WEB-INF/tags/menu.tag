<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<spring:url var="URL_PREFIX" value="/web"/>

<script type="text/javascript">

  <%-- UGLY FIX BECAUSE BOOTSTRAP DROPDOWN IN NAVBAR NOT WORKING ON SOME MOBILE PHONES --%>
  $('.dropdown-toggle').click(function (event) {
    event.preventDefault();
    setTimeout($.proxy(function () {
      if ('ontouchstart' in document.documentElement) {
        $(this).siblings('.dropdown-backdrop').off().remove();
      }
    }, this), 0);
  });

</script>

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
      <a class="navbar-brand" href="${URL_PREFIX}/overview">
        <img src="<spring:url value='/images/synyx-logo-transparent.png' />" height="24" width="15"/>
        <spring:message code="nav.title"/>
      </a>
    </div>

    <!-- Collect the nav links, forms, and other content for toggling -->
    <div class="collapse navbar-collapse" id="navbar-collapse">
      <ul class="nav navbar-nav navbar-right">
        <sec:authorize access="hasRole('USER')">
          <li class="dropdown">
            <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false">
              <i class="fa fa-fw fa-home"></i>
              <span class="nav-title"><spring:message code="nav.home.title"/></span>
              <span class="caret"></span>
            </a>
            <ul class="dropdown-menu" role="menu">
              <li>
                <a href="${URL_PREFIX}/overview">
                  <i class="fa fa-fw fa-list"></i> <spring:message code="nav.home.overview"/>
                </a>
              </li>
              <li>
                <a href="${URL_PREFIX}/application/new">
                  <i class="fa fa-fw fa-pencil"></i> <spring:message code="nav.home.apply"/>
                </a>
              </li>
              <li class="divider"></li>
              <li>
                <a href="<spring:url value='/j_spring_security_logout' />">
                  <i class="fa fa-fw fa-sign-out"></i> <spring:message code="nav.home.signout"/>
                </a>
              </li>
            </ul>
          </li>
        </sec:authorize>

        <sec:authorize access="hasAnyRole('BOSS', 'OFFICE')">
          <li class="dropdown">
            <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false">
              <i class="fa fa-fw fa-calendar"></i>
              <span class="nav-title"><spring:message code="nav.vacation.title"/></span>
              <span class="caret"></span>
            </a>
            <ul class="dropdown-menu" role="menu">
              <li>
                <a href="${URL_PREFIX}/application">
                  <i class="fa fa-fw fa-list"></i> <spring:message code="nav.vacation.overview"/>
                </a>
              </li>

              <sec:authorize access="hasRole('OFFICE')">
              <li>
                <a href="${URL_PREFIX}/application/new?appliesOnOnesBehalf=true">
                  <i class="fa fa-fw fa-pencil"></i> <spring:message code="nav.vacation.apply"/>
                </a>
              </li>
              </sec:authorize>
              <li>
                <a href="${URL_PREFIX}/application/statistics">
                  <i class="fa fa-fw fa-bar-chart"></i> <spring:message code="nav.vacation.statistics"/>
                </a>
              </li>
            </ul>
          </li>
        </sec:authorize>

        <sec:authorize access="hasRole('OFFICE')">
          <li class="dropdown">
            <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false">
              <i class="fa fa-medkit"></i>
              <span class="nav-title"><spring:message code="nav.sicknote.title"/></span>
              <span class="caret"></span>
            </a>
            <ul class="dropdown-menu" role="menu">
              <li>
                <a href="${URL_PREFIX}/sicknote/">
                  <i class="fa fa-fw fa-list"></i> <spring:message code="nav.sicknote.overview"/>
                </a>
              </li>
              <li>
                <a href="${URL_PREFIX}/sicknote/new">
                  <i class="fa fa-fw fa-pencil"></i> <spring:message code="nav.sicknote.create"/>
                </a>
              </li>
              <li>
                <a href="${URL_PREFIX}/sicknote/statistics">
                  <i class="fa fa-fw fa-bar-chart"></i> <spring:message code="nav.sicknote.statistics"/>
                </a>
              </li>
            </ul>
          </li>
        </sec:authorize>

        <sec:authorize access="hasRole('BOSS') and !hasRole('OFFICE')">
          <li>
            <a href="${URL_PREFIX}/staff">
              <i class="fa fa-user"></i>
              <span class="nav-title"><spring:message code="nav.staff.title"/></span>
            </a>
          </li>
        </sec:authorize>

        <sec:authorize access="hasRole('OFFICE')">
          <li class="dropdown">
            <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false">
              <i class="fa fa-user"></i>
              <span class="nav-title"><spring:message code="nav.staff.title"/></span>
              <span class="caret"></span>
            </a>
            <ul class="dropdown-menu" role="menu">
              <li>
                <a href="${URL_PREFIX}/staff">
                  <i class="fa fa-fw fa-list"></i> <spring:message code="nav.staff.overview"/>
                </a>
              </li>
              <li>
                <a href="${URL_PREFIX}/staff/new">
                  <i class="fa fa-fw fa-user-plus"></i> <spring:message code="nav.staff.create"/>
                </a>
              </li>
            </ul>
          </li>
        </sec:authorize>

        <sec:authorize access="hasAnyRole('OFFICE')">
          <li class="dropdown">
            <a href="${URL_PREFIX}/settings">
              <i class="fa fa-fw fa-cogs"></i>
              <span class="nav-title"><spring:message code="nav.settings.title"/></span>
            </a>
          </li>
        </sec:authorize>
      </ul>
    </div>
    <!-- /.navbar-collapse -->
  </div>
  <!-- /.container -->
</nav>