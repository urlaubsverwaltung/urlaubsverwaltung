<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<%@attribute name="year" type="java.lang.Integer" required="true" %>
<%@attribute name="hrefPrefix" type="java.lang.String" required="true" %>

<script type="text/javascript">

    $(function () {

        var currentYear = new Date().getFullYear();

        var $dropdown = $('#year-selection').find('.dropdown-menu');

        $dropdown.append('<li><a href="${hrefPrefix}' + (currentYear + 1) + '">' + (currentYear + 1) + '</a></li>');
        $dropdown.append('<li><a href="${hrefPrefix}' + currentYear + '">' + currentYear + '</a></li>');

        for (var i = 1; i < 10; i++) {
            $dropdown.append('<li><a href="${hrefPrefix}' + (currentYear - i) + '">' + (currentYear - i) + '</a></li>');
        }

    });

</script>

<div id="year-selection" class="legend-dropdown dropdown">
    <a id="dropdownLabel" data-target="#" href="#" data-toggle="dropdown"
       aria-haspopup="true" role="button" aria-expanded="false">
        <c:out value="${year}" /><span class="caret"></span>
    </a>

    <ul class="dropdown-menu" role="menu" aria-labelledby="dropdownLabel"></ul>
</div>