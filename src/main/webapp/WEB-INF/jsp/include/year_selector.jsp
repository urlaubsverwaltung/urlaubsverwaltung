<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<div class="btn-group year-selector">

    <button class="btn dropdown-toggle" data-toggle="dropdown">
        <i class="icon-time"></i>
        <spring:message code="ov.header.year" />&nbsp;<span class="caret"></span>
    </button>

    <ul class="dropdown-menu">

        <script type="text/javascript">
            $(document).ready(function () {

                var year = ${year};

                var nextYear = year + 1;
                $("div.year-selector ul.dropdown-menu").append(
                        '<li><a href="?year=' + nextYear + '">' + nextYear + '</a></li>'
                );
                
                for (var i = 0; i <= 10; i++) {

                    var pastYear = year - i;

                    $("div.year-selector ul.dropdown-menu").append(
                            '<li><a href="?year=' + pastYear + '">' + pastYear + '</a></li>'
                    );

                }

            });
        </script>

    </ul>

</div>