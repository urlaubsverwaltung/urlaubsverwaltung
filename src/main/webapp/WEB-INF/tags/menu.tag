<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<spring:url var="formUrlPrefix" value="/web" />

<script type="text/javascript">
    $(document).ready(function(){

        /* toggle nav */
        $("#menu-icon").on("click", function(){

            $(".content").toggleClass("mobile-menu-active");
            $("#menu-icon").toggleClass("active");

            $(".nav").toggle();

            return false;

        });

    });
</script>

<div class="navbar navbar-inverse navbar-fixed-top">
    <div class="navbar-inner">
        <div class="grid-container">
            <div class="grid-100">
                <span class="brand">
                    <img src="<spring:url value='/images/synyx-logo-2.jpg' />" />
                    <spring:message code="title" />
                   <button type="button" class="btn btn-right" id="menu-icon" href="#"><i class="icon-align-justify"></i></button>
                </span>
            <ul class="nav">

                <sec:authorize access="hasRole('USER')">
                    <li>
                        <a href="${formUrlPrefix}/overview">
                            <i class="icon-home"></i>&nbsp;<spring:message code="overview"/>
                        </a>
                    </li>
                    <li>
                        <a href="${formUrlPrefix}/application/new">
                            <i class="icon-pencil"></i>&nbsp;<spring:message code="ov.apply"/>
                        </a>
                    </li>
                    </li>
                </sec:authorize>

                <sec:authorize access="hasAnyRole('BOSS', 'OFFICE')">
                    <li>
                        <a href="${formUrlPrefix}/application/all">
                            <i class="icon-th-list"></i>&nbsp;<spring:message code="apps.vac"/>
                        </a>
                    </li>
                </sec:authorize>

                <sec:authorize access="hasRole('OFFICE')">
                    <li>
                        <a href="${formUrlPrefix}/sicknote/quartal">
                            <img src="<spring:url value='/images/agt_virus-off.png' />" />&nbsp;<spring:message code="sicknotes" />
                        </a>
                    </li>
                </sec:authorize>

                <sec:authorize access="hasAnyRole('BOSS', 'OFFICE')">

                    <li>
                        <a href="${formUrlPrefix}/staff"><i class="icon-user"></i>&nbsp;<spring:message code="staff.manager" /></a>
                    </li>

                </sec:authorize>

                <li><a href="<spring:url value='/j_spring_security_logout' />"><i class="icon-off"></i>&nbsp;Logout</a></li>

            </ul>
            </div>
        </div>
    </div>
</div>