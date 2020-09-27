<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<spring:url var="URL_PREFIX" value="/web"/>

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
        <sec:authorize access="hasAuthority('USER')">
          <li>
            <a href="${URL_PREFIX}/overview" class="tw-flex tw-items-center">
              <img
                src="<c:out value='${signedInUser.gravatarURL}?d=mm&s=20'/>"
                alt=""
                class="gravatar gravatar--small tw-rounded-full tw-mr-2 print:tw-hidden"
                width="24px"
                height="24px"
                onerror="this.src !== '/images/gravatar.jpg' && (this.src = '/images/gravatar.jpg')"
              />
              <spring:message code="nav.home.title"/>
            </a>
          </li>
        </sec:authorize>
      </ul>
      <ul class="nav navbar-nav navbar-right">
        <sec:authorize access="hasAuthority('USER')">
          <li>
            <a href="${URL_PREFIX}/application/new" id="application-new-link" class="tw-flex tw-items-center">
                <uv:icon-plus-circle className="tw-w-4 tw-h-4" solid="true" />
                &nbsp;<spring:message code="nav.apply.title"/>
            </a>
          </li>
        </sec:authorize>

        <sec:authorize access="hasAnyAuthority('DEPARTMENT_HEAD', 'BOSS', 'OFFICE', 'SECOND_STAGE_AUTHORITY')">
          <li>
            <a href="${URL_PREFIX}/application" class="tw-flex tw-items-center">
                <uv:icon-calendar className="tw-w-4 tw-h-4" solid="true" />
                &nbsp;<spring:message code="nav.vacation.title"/>
            </a>
          </li>
        </sec:authorize>

        <sec:authorize access="hasAuthority('OFFICE')">
          <li>
            <a href="${URL_PREFIX}/sicknote/" class="tw-flex tw-items-center">
                <uv:icon-medkit className="tw-w-4 tw-h-4" />
                &nbsp;<spring:message code="nav.sicknote.title"/>
            </a>
          </li>
        </sec:authorize>

        <sec:authorize access="hasAnyAuthority('DEPARTMENT_HEAD', 'BOSS', 'OFFICE', 'SECOND_STAGE_AUTHORITY')">
          <li>
            <a href="${URL_PREFIX}/person?active=true" class="tw-flex tw-items-center">
                <uv:icon-user className="tw-w-4 tw-h-4" solid="true" />
                &nbsp;<spring:message code="nav.person.title"/>
            </a>
          </li>
        </sec:authorize>

        <sec:authorize access="hasAnyAuthority('BOSS', 'OFFICE')">
          <li>
            <a href="${URL_PREFIX}/department" class="tw-flex tw-items-center">
                <uv:icon-user-group className="tw-w-4 tw-h-4" solid="true" />
                &nbsp;<spring:message code="nav.department.title"/>
            </a>
          </li>
        </sec:authorize>

        <sec:authorize access="hasAuthority('OFFICE')">
          <li>
            <a href="${URL_PREFIX}/settings" class="tw-flex tw-items-center">
                <uv:icon-cog className="tw-w-4 tw-h-4" solid="true" />
                &nbsp;<spring:message code="nav.settings.title"/>
            </a>
          </li>
        </sec:authorize>

        <sec:authorize access="hasAuthority('USER')">
          <li>
            <a href="<spring:url value='/logout' />" class="tw-flex tw-items-center">
                <uv:icon-logout className="tw-w-4 tw-h-4" solid="true" />
                &nbsp;<spring:message code="nav.signout.title"/>
            </a>
          </li>
        </sec:authorize>
      </ul>
    </div>
    <!-- /.navbar-collapse -->
  </div>
  <!-- /.container -->
</nav>
