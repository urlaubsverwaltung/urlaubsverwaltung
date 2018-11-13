<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<spring:url var="URL_PREFIX" value="/web"/>

<script type="text/javascript">

  <%-- UGLY FIX BECAUSE BOOTSTRAP DROPDOWN IN NAVBAR NOT WORKING ON SOME MOBILE PHONES --%>

  $('.dropdown-toggle').on("click", function (event) {
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
    </div>

    <!-- Collect the nav links, forms, and other content for toggling -->
    <div class="collapse navbar-collapse" id="navbar-collapse">
      <ul class="nav navbar-nav">
        <sec:authorize access="hasRole('USER')">
          <li>
            <a href="${URL_PREFIX}/overview">
              <span class="img-circle gravatar gravatar--small hidden-print" data-gravatar="<c:out value='${signedInUser.gravatarURL}?d=mm&s=20'/>"></span> <spring:message code="nav.home.title"/>
            </a>
          </li>
        </sec:authorize>
      </ul>
      <ul class="nav navbar-nav navbar-right">
        <sec:authorize access="hasRole('USER')">
          <li>
            <a href="${URL_PREFIX}/application/new">
              <i class="fa fa-fw fa-plus-circle"></i> <spring:message code="nav.apply.title"/>
            </a>
          </li>
        </sec:authorize>

        <sec:authorize access="hasAnyRole('DEPARTMENT_HEAD', 'BOSS', 'OFFICE', 'SECOND_STAGE_AUTHORITY')">
          <li>
            <a href="${URL_PREFIX}/application">
              <i class="fa fa-fw fa-calendar"></i> <spring:message code="nav.vacation.title"/>
            </a>
          </li>
        </sec:authorize>

        <sec:authorize access="hasRole('OFFICE')">
          <li>
            <a href="${URL_PREFIX}/sicknote/">
              <i class="fa fa-fw fa-medkit"></i> <spring:message code="nav.sicknote.title"/>
            </a>
          </li>
        </sec:authorize>

        <sec:authorize access="hasAnyRole('DEPARTMENT_HEAD', 'BOSS', 'OFFICE', 'SECOND_STAGE_AUTHORITY')">
          <li>
            <a href="${URL_PREFIX}/staff?active=true">
              <i class="fa fa-fw fa-user"></i>
              <spring:message code="nav.staff.title"/>
            </a>
          </li>
        </sec:authorize>

        <sec:authorize access="hasAnyRole('BOSS', 'OFFICE')">
          <li>
            <a href="${URL_PREFIX}/department">
              <i class="fa fa-fw fa-group"></i>
              <spring:message code="nav.department.title"/>
            </a>
          </li>
        </sec:authorize>

        <sec:authorize access="hasRole('OFFICE')">
          <li>
            <a href="${URL_PREFIX}/settings">
              <i class="fa fa-fw fa-cog"></i>
              <spring:message code="nav.settings.title"/>
            </a>
          </li>
        </sec:authorize>

        <sec:authorize access="hasRole('USER')">
          <li>
            <a href="<spring:url value='/logout' />">
              <i class="fa fa-fw fa-sign-out"></i> <spring:message code="nav.signout.title"/>
            </a>
          </li>
        </sec:authorize>
      </ul>
    </div>
    <!-- /.navbar-collapse -->
  </div>
  <!-- /.container -->
</nav>
